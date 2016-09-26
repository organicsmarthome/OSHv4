package osh.comdriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.signals.PriceSignalGenerator;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.hal.exchange.EpsComExchange;
import osh.utils.time.TimeConversion;


/**
 * 
 * @author Jan Mueller
 *
 */
public class FlexiblePVEpsProviderComDriver extends CALComDriver {

	private EnumMap<AncillaryCommodity,PriceSignal> currentPriceSignal = new EnumMap<AncillaryCommodity, PriceSignal>(AncillaryCommodity.class);
	
	
	/** Time after which a signal is send */
	private int newSignalAfterThisPeriod;
	/** Timestamp of the last price signal sent to global controller */
	private long lastSignalSent = 0L;
	/** Maximum time the signal is available in advance (36h) */
	private int signalPeriod;
	/** Minimum time the signal is available in advance (24h) */
	private int signalAvailableFor;
	/** Signal is constant for 15 minutes */
	private int resolutionOfPriceSignal;
	
	//private List<Double> priceSignalYear;
	
	private double activePowerPrice;
	private double reactivePowerPrice;
	private double naturalGasPowerPrice;
	

	private double activePowerFeedInCHP;
	
	private double activePowerAutoConsumptionPV;
	private double activePowerAutoConsumptionCHP;
	
	/** Path of price signal CSV file */
	private String filePathActivePowerFeedInPVPriceSignal;
	private List<Double> pVPriceSignal;
	
	//the active ancillary commodities for which a price signal should be produced
	private List<AncillaryCommodity> activeAncillaryCommodities = new ArrayList<AncillaryCommodity>();
	
	
	public FlexiblePVEpsProviderComDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
		
		try {
			this.newSignalAfterThisPeriod = Integer.valueOf(getComConfig().getParameter("newSignalAfterThisPeriod"));
		}
		catch (Exception e) {
			this.newSignalAfterThisPeriod = 43200; //12 hours
			getGlobalLogger().logWarning("Can't get newSignalAfterThisPeriod, using the default value: " + this.newSignalAfterThisPeriod);
		}
		
		try {
			this.signalPeriod = Integer.valueOf(getComConfig().getParameter("signalPeriod"));
		}
		catch (Exception e) {
			this.signalPeriod = 129600; //36 hours
			getGlobalLogger().logWarning("Can't get signalPeriod, using the default value: " + this.signalPeriod);
		}
		
		try {
			this.resolutionOfPriceSignal = Integer.valueOf(getComConfig().getParameter("resolutionOfPriceSignal"));
		}
		catch (Exception e) {
			this.resolutionOfPriceSignal = 3600; //15 minutes
			getGlobalLogger().logWarning("Can't get signalConstantPeriod, using the default value: " + this.resolutionOfPriceSignal);
		}
		
		try {
			this.activePowerPrice = Double.valueOf(getComConfig().getParameter("activePowerPrice"));
		}
		catch (Exception e) {
			this.activePowerPrice = 28.0;
			getGlobalLogger().logWarning("Can't get activePowerPrice, using the default value: " + this.activePowerPrice);
		}
		
		try {
			this.reactivePowerPrice = Double.valueOf(getComConfig().getParameter("reactivePowerPrice"));
		}
		catch (Exception e) {
			this.reactivePowerPrice = 0.0;
			getGlobalLogger().logWarning("Can't get reactivePowerPrice, using the default value: " + this.reactivePowerPrice);
		}
		
		try {
			this.naturalGasPowerPrice = Double.valueOf(getComConfig().getParameter("naturalGasPowerPrice"));
		}
		catch (Exception e) {
			this.naturalGasPowerPrice = 7.0;
			getGlobalLogger().logWarning("Can't get naturalGasPowerPrice, using the default value: " + this.naturalGasPowerPrice);
		}

		try {
			this.activePowerFeedInCHP = Double.valueOf(getComConfig().getParameter("activePowerFeedInCHP"));
		}
		catch (Exception e) {
			this.activePowerFeedInCHP = 5.0;
			getGlobalLogger().logWarning("Can't get activePowerFeedInCHP, using the default value: " + this.activePowerFeedInCHP);
		}
		
		try {
			this.activePowerAutoConsumptionPV = Double.valueOf(getComConfig().getParameter("activePowerAutoConsumptionPV"));
		}
		catch (Exception e) {
			this.activePowerAutoConsumptionPV = 0.0;
			getGlobalLogger().logWarning("Can't get activePowerAutoConsumptionPV, using the default value: " + this.activePowerAutoConsumptionPV);
		}
		
		try {
			this.activePowerAutoConsumptionCHP = Double.valueOf(getComConfig().getParameter("activePowerAutoConsumptionCHP"));
		}
		catch (Exception e) {
			this.activePowerAutoConsumptionCHP = 0.0;
			getGlobalLogger().logWarning("Can't get activePowerAutoConsumptionCHP, using the default value: " + this.activePowerAutoConsumptionCHP);
		}
		
		String ancillaryCommoditiesAsArray = null;
		
		try {
			ancillaryCommoditiesAsArray = driverConfig.getParameter("ancillaryCommodities");
			if (ancillaryCommoditiesAsArray == null)
				throw new IllegalArgumentException();
		} 
		catch (Exception e) {
			ancillaryCommoditiesAsArray = "[activepowerexternal, reactivepowerexternal, naturalgaspowerexternal, pvactivepowerfeedin, chpactivepowerfeedin]";
			getGlobalLogger().logWarning("Can't get ancillaryCommoditiesAsArray, using the default value: " + ancillaryCommoditiesAsArray);
		}
		
		if (ancillaryCommoditiesAsArray != null) {
			ancillaryCommoditiesAsArray = ancillaryCommoditiesAsArray.replaceAll("\\[|\\]|\\s", "");
			activeAncillaryCommodities = Stream.of(ancillaryCommoditiesAsArray.split(","))
			        .map(AncillaryCommodity::fromString)
			        .collect(Collectors.toList());
		}
		
		try {
			this.filePathActivePowerFeedInPVPriceSignal = getComConfig().getParameter("filePathActivePowerFeedInPVPriceSignal");
		}
		catch (Exception e) {
			this.filePathActivePowerFeedInPVPriceSignal = "configfiles/externalSignal/priceDynamic.csv";
			getGlobalLogger().logWarning("Can't get filePathPriceSignal, using the default value: " + this.filePathActivePowerFeedInPVPriceSignal);
		}
		
	
		signalAvailableFor = signalPeriod - newSignalAfterThisPeriod;
		
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		long now = getTimer().getUnixTime();

		pVPriceSignal = readCsvPriceSignal(filePathActivePowerFeedInPVPriceSignal);
		
		if (activeAncillaryCommodities.contains(AncillaryCommodity.ACTIVEPOWEREXTERNAL)) {
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.ACTIVEPOWEREXTERNAL, activePowerPrice);
			this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
		}
		if (activeAncillaryCommodities.contains(AncillaryCommodity.REACTIVEPOWEREXTERNAL)) {
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.REACTIVEPOWEREXTERNAL, reactivePowerPrice);
			this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
		}
		if (activeAncillaryCommodities.contains(AncillaryCommodity.NATURALGASPOWEREXTERNAL)) {
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.NATURALGASPOWEREXTERNAL, naturalGasPowerPrice);
			this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
		}
		if (activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERFEEDIN)) {			
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERFEEDIN,  pVPriceSignal);
			this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
		}
		if (activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERFEEDIN)) {
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, activePowerFeedInCHP);
			this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
		}
		if (activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION)) {
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, activePowerAutoConsumptionPV);
			this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, newSignal);
		}
		if (activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION)) {
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, activePowerAutoConsumptionCHP);
			this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, newSignal);
		}
		
		// EPS
		EpsComExchange ex = new EpsComExchange(
				this.getDeviceID(), 
				now, 
				currentPriceSignal);
		this.notifyComManager(ex);
		
		this.lastSignalSent = now;
		
		// register
		this.getTimer().registerComponent(this, 1);
	}
	
	@Override
	public void onNextTimePeriod() {
		
		long now = getTimer().getUnixTime();
		
		if ((now - lastSignalSent) >= newSignalAfterThisPeriod) {
			if (activeAncillaryCommodities.contains(AncillaryCommodity.ACTIVEPOWEREXTERNAL)) {
				PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.ACTIVEPOWEREXTERNAL, activePowerPrice);
				this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
			}
			if (activeAncillaryCommodities.contains(AncillaryCommodity.REACTIVEPOWEREXTERNAL)) {
				PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.REACTIVEPOWEREXTERNAL, reactivePowerPrice);
				this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
			}
			if (activeAncillaryCommodities.contains(AncillaryCommodity.NATURALGASPOWEREXTERNAL)) {
				PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.NATURALGASPOWEREXTERNAL, naturalGasPowerPrice);
				this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
			}
			if (activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERFEEDIN)) {
				PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERFEEDIN,  pVPriceSignal);
				this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
			}
			if (activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERFEEDIN)) {
				PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, activePowerFeedInCHP);
				this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
			}
			if (activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION)) {
				PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, activePowerAutoConsumptionPV);
				this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, newSignal);
			}
			if (activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION)) {
				PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, activePowerAutoConsumptionCHP);
				this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, newSignal);
			}
			
			lastSignalSent = now;
			
			// EPS
			EpsComExchange ex = new EpsComExchange(
					this.getDeviceID(), 
					now, 
					currentPriceSignal);
			this.notifyComManager(ex);
		}
		
	}


	@Override
	public void updateDataFromComManager(ICALExchange hx) {
		//NOTHING
	}
	
	
	private List<Double> readCsvPriceSignal(String filePath) {
		List<Double> priceSignalYear = new ArrayList<Double>();
		try {
			
			BufferedReader csvReader = new BufferedReader(new FileReader(new File(filePath)));
			String priceSignalLine;
			while ((priceSignalLine = csvReader.readLine()) != null){
				String[] splitLine = priceSignalLine.split(";");
				priceSignalYear.add(new Double(splitLine[0]));
			}
			csvReader.close();
		} 
		catch (Exception e) { // TODO: rethrow and handle
			e.printStackTrace();
		}
		return priceSignalYear;
	}
	
	
	
	
	private PriceSignal generatePriceSignal(AncillaryCommodity commodity, List<Double> priceSignalYear) {
		PriceSignal priceSignal = null;
		
		
		if ( currentPriceSignal == null ) {
			// initial price signal
			long now = getTimer().getUnixTime();
			long timeSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
			long timeTillEndOfDay = 86400 - timeSinceMidnight;
			long additionalTime = signalAvailableFor;
			
			priceSignal = PriceSignalGenerator.getFlexiblePriceSignal(
					commodity,
					TimeConversion.convertUnixTime2SecondsFromYearStart(now),
					now, 
					now + timeTillEndOfDay + additionalTime, 
					resolutionOfPriceSignal, 
					priceSignalYear);
					
		}
		else {
			// generate every 12 hours
			long now = getTimer().getUnixTime();
			
			priceSignal = PriceSignalGenerator.getFlexiblePriceSignal(
					commodity,
					TimeConversion.convertUnixTime2SecondsFromYearStart(now),
					now, 
					now + signalPeriod, 
					resolutionOfPriceSignal, 
					priceSignalYear);
		}
		
		return priceSignal;
	}
	
	private PriceSignal generatePriceSignal(AncillaryCommodity commodity, double price) {
		PriceSignal priceSignal = null;
		
		
		if ( currentPriceSignal == null ) {
			// initial price signal
			long now = getTimer().getUnixTime();
			long timeSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
			long timeTillEndOfDay = 86400 - timeSinceMidnight;
			long additionalTime = signalAvailableFor;
			
			priceSignal = PriceSignalGenerator.getConstantPriceSignal(
					commodity, 
					now, 
					now + timeTillEndOfDay + additionalTime, 
					resolutionOfPriceSignal, 
					price);
					
		}
		else {
			// generate every 12 hours
			long now = getTimer().getUnixTime();
			
			priceSignal = PriceSignalGenerator.getConstantPriceSignal(
					commodity, 
					now, 
					now + signalPeriod, 
					resolutionOfPriceSignal, 
					price);
		}
		
		return priceSignal;
	}
	
}
