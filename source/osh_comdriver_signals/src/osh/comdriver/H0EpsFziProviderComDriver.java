package osh.comdriver;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.signals.VirtualPriceSignalGenerator;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.hal.exchange.EpsComExchange;
import osh.utils.slp.IH0Profile;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Ingo Mauser
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class H0EpsFziProviderComDriver extends CALComDriver  {
	
	private EnumMap<AncillaryCommodity,PriceSignal> priceSignals = new EnumMap<AncillaryCommodity,PriceSignal>(AncillaryCommodity.class);
	
	private IH0Profile h0Profile;
	private String h0ProfileFileName;
	private String h0ClassName;
	private int currentYear;
	
	/** Time after which a signal is send */
	private int newSignalAfterThisPeriod;
	
	/** Maximum time the signal is available in advance (36h) */
	private int signalPeriod;
	
	private long lastTimeSignalSent = 0L;
	
	/* Minimum time the signal is available in advance (24h) 
	 * atLeast = signalPeriod - newSignalAfterThisPeriod */
//	private int signalAvailableFor;
	
	/** Signal is constant for 15 minutes */
	private int signalConstantPeriod;

	private double activePowerExternalSupplyMin;
	private double activePowerExternalSupplyAvg;
	private double activePowerExternalSupplyMax;
	private double activePowerFeedInPV;
	private double activePowerFeedInCHP;
	private double activePowerAutoConsumptionPV;
	private double activePowerAutoConsumptionCHP;
	private double naturalGasPowerPrice;
	
	
	public H0EpsFziProviderComDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
		
		try {
			this.h0ProfileFileName = getComConfig().getParameter("h0Filename");
			if (this.h0ProfileFileName == null)
				throw new IllegalArgumentException();
		}
		catch (Exception e) {
			this.h0ProfileFileName = "configfiles/h0/H0Profile15MinWinterSummerIntermediate.csv";
			getGlobalLogger().logWarning("Can't get h0Filename, using the default value: " + this.h0ProfileFileName);
		}
		
		try {
			this.h0ClassName = getComConfig().getParameter("h0Classname");
			if (h0ClassName == null)
				throw new IllegalArgumentException();
		}
		catch (Exception e) {
			this.h0ClassName = osh.utils.slp.H0Profile15Minutes.class.getName();
			getGlobalLogger().logWarning("Can't get h0ClassName, using the default value: " + this.h0ClassName);
		}	
		
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
			this.signalConstantPeriod = Integer.valueOf(getComConfig().getParameter("signalConstantPeriod"));
		}
		catch (Exception e) {
			this.signalConstantPeriod = 900; //15 minutes
			getGlobalLogger().logWarning("Can't get signalConstantPeriod, using the default value: " + this.signalConstantPeriod);
		}
		
		try {
			this.activePowerExternalSupplyMin = Double.valueOf(getComConfig().getParameter("activePowerExternalSupplyMin"));
		}
		catch (Exception e) {
			this.activePowerExternalSupplyMin = 5.0;
			getGlobalLogger().logWarning("Can't get activePowerExternalSupplyMin, using the default value: " + this.activePowerExternalSupplyMin);
		}
		
		try {
			this.activePowerExternalSupplyAvg = Double.valueOf(getComConfig().getParameter("activePowerExternalSupplyAvg"));
		}
		catch (Exception e) {
			this.activePowerExternalSupplyAvg = 25.0;
			getGlobalLogger().logWarning("Can't get activePowerExternalSupplyAvg, using the default value: " + this.activePowerExternalSupplyAvg);
		}
		
		try {
			this.activePowerExternalSupplyMax = Double.valueOf(getComConfig().getParameter("activePowerExternalSupplyMax"));
		}
		catch (Exception e) {
			this.activePowerExternalSupplyMax = 45.0;
			getGlobalLogger().logWarning("Can't get activePowerExternalSupplyMax, using the default value: " + this.activePowerExternalSupplyMax);
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
		
		try {
			this.naturalGasPowerPrice = Double.valueOf(getComConfig().getParameter("naturalGasPowerPrice"));
		}
		catch (Exception e) {
			this.naturalGasPowerPrice = 7.5;
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
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		this.currentYear = TimeConversion.convertUnixTime2Year(getTimer().getUnixTime());
		
		try {
			Class h0Class = Class.forName(h0ClassName);
			
			h0Profile = (IH0Profile) h0Class.getConstructor(int.class, String.class, double.class)
				.newInstance(currentYear,
						h0ProfileFileName,
						1000.0);
			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		this.priceSignals = generateNewPriceSignal();
		EpsComExchange ex = new EpsComExchange(
				this.getDeviceID(), 
				getTimer().getUnixTime(), 
				priceSignals);
		this.notifyComManager(ex);
		
		lastTimeSignalSent = getTimer().getUnixTime();
		
		//register update
		getTimer().registerComponent(this, 1);
	}
	
	
	
	@Override
	public void onNextTimePeriod() {
		
		long now = getTimer().getUnixTime();
		
		// generate new PriceSignal and send it
		if ((now - lastTimeSignalSent) >= newSignalAfterThisPeriod) {
			
			int nowIsYear = TimeConversion.convertUnixTime2Year(now);
			if (nowIsYear != this.currentYear) {
				// new years eve...
				this.currentYear = nowIsYear;
				
				// renew H0 Profile
				
				try {
					Class h0Class = Class.forName(h0ClassName);
					this.h0Profile = (IH0Profile) h0Class.getConstructor(int.class, String.class, double.class)
							.newInstance(currentYear,
									h0ProfileFileName,
									1000.0);
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException | NoSuchMethodException
						| SecurityException e) {
					e.printStackTrace();
				}
			}
			
			// EPS
			this.priceSignals = generateNewPriceSignal();
			EpsComExchange ex = new EpsComExchange(
					this.getDeviceID(), 
					now, 
					priceSignals);
			this.notifyComManager(ex);
			
			lastTimeSignalSent = now;
		}
	}
	
	
	private EnumMap<AncillaryCommodity,PriceSignal> generateNewPriceSignal() {
		
		PriceSignal newPriceSignalAutoConsPV = VirtualPriceSignalGenerator.getConstantPriceSignal(
				getTimer().getUnixTime(), 
				getTimer().getUnixTime() + signalPeriod, 
				signalConstantPeriod, 
				activePowerAutoConsumptionPV, 
				AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION);
		
		PriceSignal newPriceSignalAutoConsCHP = VirtualPriceSignalGenerator.getConstantPriceSignal(
				getTimer().getUnixTime(), 
				getTimer().getUnixTime() + signalPeriod, 
				signalConstantPeriod, 
				activePowerAutoConsumptionCHP, 
				AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION);
		
		PriceSignal newPriceSignalExternal = VirtualPriceSignalGenerator.getRandomH0BasedPriceSignal(
				getTimer().getUnixTime(), 
				getTimer().getUnixTime() + signalPeriod, 
				signalConstantPeriod, 
				activePowerExternalSupplyMin, 
				activePowerExternalSupplyAvg, 
				activePowerExternalSupplyMax, 
				h0Profile, 
				getOSH().getRandomGenerator(), 
				false, 
				0, 
				0, 
				AncillaryCommodity.ACTIVEPOWEREXTERNAL);
		
		PriceSignal newPriceSignalFeedInPV = VirtualPriceSignalGenerator.getConstantPriceSignal(
				getTimer().getUnixTime(), 
				getTimer().getUnixTime() + signalPeriod, 
				signalConstantPeriod, 
				activePowerFeedInPV, 
				AncillaryCommodity.PVACTIVEPOWERFEEDIN);
		
		PriceSignal newPriceSignalFeedInCHP = VirtualPriceSignalGenerator.getConstantPriceSignal(
				getTimer().getUnixTime(), 
				getTimer().getUnixTime() + signalPeriod, 
				signalConstantPeriod, 
				activePowerFeedInCHP, 
				AncillaryCommodity.CHPACTIVEPOWERFEEDIN);
		
		PriceSignal newPriceSignalNaturalGas = VirtualPriceSignalGenerator.getConstantPriceSignal(
				getTimer().getUnixTime(), 
				getTimer().getUnixTime() + signalPeriod, 
				signalConstantPeriod, 
				naturalGasPowerPrice, 
				AncillaryCommodity.NATURALGASPOWEREXTERNAL);
		
		EnumMap<AncillaryCommodity,PriceSignal> newPriceSignal = new EnumMap<>(AncillaryCommodity.class);
		newPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newPriceSignalExternal);
		newPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, newPriceSignalAutoConsPV);
		newPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newPriceSignalFeedInPV);
		newPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newPriceSignalFeedInCHP);
		newPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, newPriceSignalAutoConsCHP);
		newPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newPriceSignalNaturalGas);
		
		return newPriceSignal;
	}


	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		//NOTHING
	}

}
