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
import osh.simulation.exception.SimulationSubjectException;
import osh.utils.time.TimeConversion;


/**
 * 
 * @author Florian Allerding
 *
 */
public class CsvEpsProviderComDriver extends CALComDriver {
	
	private long newSignalAfterThisPeriod;
	private int resolutionOfPriceSignal;
	private String filePathPriceSignal;
	private List<Double> priceSignalYear;
	private int signalPeriod;
	
	private double activePowerFeedInPV;
	private double activePowerFeedInCHP;
	private double naturalGasPowerPrice;
	private double activePowerAutoConsumptionPV;
	private double activePowerAutoConsumptionCHP;
	
	private List<AncillaryCommodity> activeAncillaryCommodities = new ArrayList<AncillaryCommodity>();
	
	private long lastTimeSignalSent;
	

	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 */
	public CsvEpsProviderComDriver(IOSH controllerbox,
			UUID deviceID, OSHParameterCollection driverConfig)
			throws SimulationSubjectException {
		super(controllerbox, deviceID, driverConfig);
		priceSignalYear = new ArrayList<Double>();
		
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
			this.resolutionOfPriceSignal = 3600; //1 hour
			getGlobalLogger().logWarning("Can't get resolutionOfPriceSignal, using the default value: " + this.resolutionOfPriceSignal);
		}
		
		try {
			this.naturalGasPowerPrice = Double.valueOf(getComConfig().getParameter("naturalGasPowerPrice"));
		}
		catch (Exception e) {
			this.naturalGasPowerPrice = 7.0;
			getGlobalLogger().logWarning("Can't get naturalGasPowerPrice, using the default value: " + this.naturalGasPowerPrice);
		}
		
		try {
			this.activePowerFeedInPV = Double.valueOf(getComConfig().getParameter("activePowerFeedInPV"));
		}
		catch (Exception e) {
			this.activePowerFeedInPV = 10.0;
			getGlobalLogger().logWarning("Can't get activePowerFeedInPV, using the default value: " + this.activePowerFeedInPV);
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
			this.activePowerFeedInCHP = 0.0;
			getGlobalLogger().logWarning("Can't get activePowerAutoConsumptionCHP, using the default value: " + this.activePowerAutoConsumptionCHP);
		}
		
		try {
			this.activePowerAutoConsumptionCHP = Double.valueOf(getComConfig().getParameter("activePowerAutoConsumptionCHP"));
		}
		catch (Exception e) {
			this.activePowerFeedInCHP = 0.0;
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
			this.filePathPriceSignal = getComConfig().getParameter("filePathPriceSignal");
			if (this.filePathPriceSignal == null)
				throw new IllegalArgumentException();
		}
		catch (Exception e) {
			this.filePathPriceSignal = "configfiles/externalSignal/priceDynamic.csv";
			getGlobalLogger().logWarning("Can't get filePathPriceSignal, using the default value: " + this.filePathPriceSignal);
		}
	}
	
	private void readCsvPriceSignal() {
		
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(new File(filePathPriceSignal)));
			String priceSignalLine;
			while ((priceSignalLine = csvReader.readLine()) != null){
				String[] splitLine = priceSignalLine.split(";");
				priceSignalYear.add(new Double(splitLine[0]));
			}
			csvReader.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public void onSystemIsUp() throws OSHException {
		
		readCsvPriceSignal();
		generateNewPriceSignal();
		this.getTimer().registerComponent(this, 1);
		
		lastTimeSignalSent = this.getTimer().getUnixTime();
	}

	/**
	 * Generate PriceSignal
	 */
	private void generateNewPriceSignal() {
		long now = this.getTimer().getUnixTime();
		int relativeTimeFromYearStart = TimeConversion.convertUnixTime2SecondsFromYearStart(now);
		long yearStart = now - relativeTimeFromYearStart;
		
		EnumMap<AncillaryCommodity,PriceSignal> priceSignals = new EnumMap<>(AncillaryCommodity.class);
		
		if (activeAncillaryCommodities.contains(AncillaryCommodity.ACTIVEPOWEREXTERNAL)) {
			int priceSignalFrom = relativeTimeFromYearStart / resolutionOfPriceSignal;
			int priceSignalTo = (relativeTimeFromYearStart + signalPeriod) / resolutionOfPriceSignal;
			PriceSignal priceSignal = new PriceSignal(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
			for (int i = priceSignalFrom; i < priceSignalTo; i++){
				if(priceSignalYear.size() <= i){
					priceSignal.setPrice(yearStart + i * resolutionOfPriceSignal, 0.0);
				}
				else{
					priceSignal.setPrice(yearStart + i * resolutionOfPriceSignal, priceSignalYear.get(i));
				}
			}
			priceSignal.setKnownPriceInterval(now, now + signalPeriod);
			priceSignal.compress();
			priceSignals.put(priceSignal.getCommodity(),priceSignal);
		}
		
		if (activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERFEEDIN)) {
			// PV ActivePower FeedIn		
			PriceSignal newPriceSignalFeedInPV = PriceSignalGenerator.getConstantPriceSignal(
					AncillaryCommodity.PVACTIVEPOWERFEEDIN, 
					now,
					now + signalPeriod, 
					signalPeriod,
					activePowerFeedInPV);
			newPriceSignalFeedInPV.setKnownPriceInterval(now, now + signalPeriod);		
			newPriceSignalFeedInPV.compress();
			priceSignals.put(newPriceSignalFeedInPV.getCommodity(),newPriceSignalFeedInPV);
		}
	
		if (activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERFEEDIN)) {
			// CHP ActivePower FeedIn		
			PriceSignal newPriceSignalFeedInCHP = PriceSignalGenerator.getConstantPriceSignal(
					AncillaryCommodity.CHPACTIVEPOWERFEEDIN, 
					now,
					now + signalPeriod, 
					signalPeriod,
					activePowerFeedInCHP);
			newPriceSignalFeedInCHP.setKnownPriceInterval(now, now + signalPeriod);	
			newPriceSignalFeedInCHP.compress();
			priceSignals.put(newPriceSignalFeedInCHP.getCommodity(),newPriceSignalFeedInCHP);
		}
		
		if (activeAncillaryCommodities.contains(AncillaryCommodity.NATURALGASPOWEREXTERNAL)) {
			// Natural Gas Power Price
			PriceSignal newPriceSignalNaturalGas = PriceSignalGenerator.getConstantPriceSignal(
					AncillaryCommodity.NATURALGASPOWEREXTERNAL, 
					now,
					now + signalPeriod, 
					signalPeriod,
					naturalGasPowerPrice);
			newPriceSignalNaturalGas.setKnownPriceInterval(now, now + signalPeriod);		
			newPriceSignalNaturalGas.compress();
			priceSignals.put(newPriceSignalNaturalGas.getCommodity(),newPriceSignalNaturalGas);
		}
		
		if (activeAncillaryCommodities.contains(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION)) {
			// Natural Gas Power Price
			PriceSignal newPriceSignalPVAutoConsumption = PriceSignalGenerator.getConstantPriceSignal(
					AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, 
					now,
					now + signalPeriod, 
					signalPeriod,
					activePowerAutoConsumptionPV);
			newPriceSignalPVAutoConsumption.setKnownPriceInterval(now, now + signalPeriod);		
			newPriceSignalPVAutoConsumption.compress();
			priceSignals.put(newPriceSignalPVAutoConsumption.getCommodity(),newPriceSignalPVAutoConsumption);
		}		

		if (activeAncillaryCommodities.contains(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION)) {
			// Natural Gas Power Price
			PriceSignal newPriceSignalCHPAutoConsumption = PriceSignalGenerator.getConstantPriceSignal(
					AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, 
					now,
					now + signalPeriod, 
					signalPeriod,
					activePowerAutoConsumptionCHP);
			newPriceSignalCHPAutoConsumption.setKnownPriceInterval(now, now + signalPeriod);		
			newPriceSignalCHPAutoConsumption.compress();
			priceSignals.put(newPriceSignalCHPAutoConsumption.getCommodity(),newPriceSignalCHPAutoConsumption);
		}
		
		//now sending priceSignal	
		EpsComExchange ex = new EpsComExchange(
				this.getDeviceID(), 
				this.getTimer().getUnixTime(), 
				priceSignals);
		this.updateComDataSubscriber(ex);
	}
	

	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		if ((getTimer().getUnixTime() - lastTimeSignalSent) >= newSignalAfterThisPeriod){
			generateNewPriceSignal();
			
			lastTimeSignalSent = getTimer().getUnixTime();
		}
	}

	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		//NOTHING		
	}
	
}
