package osh.mgmt.ipp;

import java.util.BitSet;
import java.util.TreeMap;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.thermal.SimpleHotWaterTank;
import osh.esc.LimitedCommodityStateMap;
import osh.mgmt.ipp.watertank.HotWaterTankPrediction;


/**
 * 
 * @author Florian Allerding, Ingo Mauser, Till Schuberth
 *
 */
public class HotWaterTankNonControllableIPP 
					extends NonControllableIPP<ISolution, HotWaterTankPrediction> {
	
	private static final long serialVersionUID = -7474764554202830275L;
	
	private SimpleHotWaterTank waterTank;
	
	private final double initialTemperature;
	private final double tankCapacity;
	private final double tankDiameter;
	private final double ambientTemperature;
	
	private Double firstTemperature = null;
	//6ct per kWh ThermalPower (= gas price per kWh)
	private double punishmentFactorPerWsPowerLost;
	
	private TreeMap<Long, Double> temperatureStates;

	
	/**
	 * CONSTRUCTOR
	 */
	public HotWaterTankNonControllableIPP(
			UUID deviceId, 
			IGlobalLogger logger,
			long now,
			double initialTemperature,
			double tankCapacity,
			double tankDiameter,
			double ambientTemperature,
			double punishmentFactorPerWsLost,
			boolean causeScheduling,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		
		super(
				deviceId, 
				logger, 
				causeScheduling, //does not cause scheduling
				false, //does not need ancillary meter state as Input State
				true, //reacts to input states
				false, //is not static
				now, 
				DeviceTypes.HOTWATERSTORAGE,
				new Commodity[]{
						Commodity.HEATINGHOTWATERPOWER,
						Commodity.DOMESTICHOTWATERPOWER
				},
				compressionType,
				compressionValue);
		
		this.initialTemperature = initialTemperature;
		this.tankCapacity = tankCapacity;
		this.tankDiameter = tankDiameter;
		this.ambientTemperature = ambientTemperature;
		this.punishmentFactorPerWsPowerLost = punishmentFactorPerWsLost;
		
		this.internalInterdependentOutputStates = new LimitedCommodityStateMap(allOutputCommodities);
		this.allInputCommodities = new Commodity[]{Commodity.HEATINGHOTWATERPOWER, Commodity.DOMESTICHOTWATERPOWER};
	}
	
	
	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected HotWaterTankNonControllableIPP() {
		super();
		this.initialTemperature = 0;
		this.tankCapacity = 0;
		this.tankDiameter = 0;
		this.ambientTemperature = 0;
	}

	@Override
	public void recalculateEncoding(long currentTime, long maxHorizon) {
		// get new temperature of tank
		//  better not...new IPP instead
	}

	
	// ### interdependent problem part stuff ###

	@Override
	public void initializeInterdependentCalculation(
			long maxReferenceTime,
			BitSet solution,
			int stepSize,
			boolean createLoadProfile,
			boolean keepPrediction) {
		
		this.interdependentTime = maxReferenceTime;
		
		if (keepPrediction)
			temperatureStates = new TreeMap<Long, Double>();
		else
			temperatureStates = null;
		
		this.stepSize = stepSize;
		this.waterTank = new SimpleHotWaterTank(
				tankCapacity, 
				tankDiameter, 
				initialTemperature, 
				ambientTemperature);
		
		waterTank.reduceByStandingHeatLoss(interdependentTime - this.getTimestamp());
		firstTemperature = waterTank.getCurrentWaterTemperature();
//		this.internalInterdependentOutputStates = new EnumMap<Commodity, RealCommodityState>(Commodity.class);
		this.internalInterdependentOutputStates.clear();
		
		setOutputStates(null);
		this.interdependentInputStates = null;
	}

	@Override
	public void calculateNextStep() {
		
		if (this.interdependentInputStates != null) {

			// update tank according to interdependentInputStates
			double hotWaterPower = 0;
//			RealCommodityState heatWaterState = this.interdependentInputStates.get(Commodity.HEATINGHOTWATERPOWER);
			
//			if (heatWaterState != null) {
//				hotWaterPower -= heatWaterState.getPower();
//
//			}
			
//			RealCommodityState domWaterState = this.interdependentInputStates.get(Commodity.DOMESTICHOTWATERPOWER);
//
//			if (domWaterState != null) {
//				hotWaterPower -= domWaterState.getPower();
//			}
			
			hotWaterPower -= interdependentInputStates.getPower(Commodity.HEATINGHOTWATERPOWER);
			hotWaterPower -= interdependentInputStates.getPower(Commodity.DOMESTICHOTWATERPOWER);

			this.waterTank.addPowerOverTime(hotWaterPower, stepSize, null, null);
		}

		double currentTemp = waterTank.getCurrentWaterTemperature();

		if (temperatureStates != null)
			temperatureStates.put(interdependentTime, currentTemp);

		// update interdependentOutputStates
//		this.interdependentOutputStates.clear();
//		this.internalInterdependentOutputStates.put(
//				Commodity.HEATINGHOTWATERPOWER, 
//				new RealThermalCommodityState(
//						Commodity.HEATINGHOTWATERPOWER, 
//						0.0, 
//						currentTemp, 
//						null));
//		this.internalInterdependentOutputStates.put(
//				Commodity.DOMESTICHOTWATERPOWER, 
//				new RealThermalCommodityState(
//						Commodity.DOMESTICHOTWATERPOWER, 
//						0.0, 
//						currentTemp, 
//						null));
		this.internalInterdependentOutputStates.setTemperature(Commodity.HEATINGHOTWATERPOWER, currentTemp);
		this.internalInterdependentOutputStates.setTemperature(Commodity.DOMESTICHOTWATERPOWER, currentTemp);
		setOutputStates(internalInterdependentOutputStates);


		// reduce be standing loss
		this.waterTank.reduceByStandingHeatLoss(stepSize);


		this.interdependentTime += stepSize;
	}
	
	@Override
	public HotWaterTankPrediction transformToFinalInterdependetPrediction(BitSet solution) {
		return new HotWaterTankPrediction(temperatureStates);
	}
	
	
	@Override
	public Schedule getFinalInterdependentSchedule() {
		//punish for losing power (having a lower temperature then at the start)
		double cervisia = 0;
		double tempDifference = firstTemperature - waterTank.getCurrentWaterTemperature();

		cervisia = 
				-waterTank.calculateEnergyDrawOff(
						firstTemperature, 
						waterTank.getCurrentWaterTemperature())
					* punishmentFactorPerWsPowerLost;
		
		//if tank has higher temperature than at the beginning only give half of the cervizia
		if (tempDifference < 0) {
			cervisia /= 2.0;
		}
		
		return new Schedule(new SparseLoadProfile(), cervisia, this.getDeviceType().toString());
	}
	
	// ### to string ###
	
	@Override
	public String problemToString() {
		return "[" + getReferenceTime() + "] HotWaterTankIPP initialTemperature=" + ((int) (initialTemperature * 10)) / 10.0;
	}
}