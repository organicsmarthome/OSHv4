package osh.mgmt.ipp;

import java.util.BitSet;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.simulation.batterylogic.SimpleBatteryLogic;
import osh.driver.simulation.batterystorage.SimpleBatteryStorageModel;
import osh.driver.simulation.inverter.SimpleInverterModel;

/**
 * 
 * @author Sebastian Kramer, Jan Mueller
 *
 */
@SuppressWarnings("unused")
public class BatteryStorageNonControllableIPP 
					extends NonControllableIPP<ISolution, IPrediction> {
	
	private static final long serialVersionUID = 1063856793735351671L;
	
	private SimpleInverterModel inverterModel;
	private SimpleBatteryStorageModel  batteryModel;
	private SimpleBatteryLogic batteryLogic;
	
	private final double batteryInitialStateOfCharge;	
	private final double batteryInitialStateOfHealth;
	private final int batteryStandingLoss;
	private final int batteryMinChargingState;
	private final int batteryMaxChargingState;
	private final int batteryMinChargePower;
	private final int batteryMinDischargePower;
	private final int batteryMaxChargePower;
	private final int inverterMinComplexPower;
	private final int inverterMaxComplexPower;
	private final int inverterMaxPower;
	private final int inverterMinPower;
	private final int batteryMaxDischargePower;
	
	private SparseLoadProfile lp = new SparseLoadProfile();


	/**
	 * CONSTRUCTOR
	 */
	public BatteryStorageNonControllableIPP(
			UUID deviceId, 
			IGlobalLogger logger,
			long now,
			double batteryInitialStateOfCharge, 
			double batteryInitialStateOfHealth, 
			int batteryStandingLoss,
			int batteryMinChargingState,
			int batteryMaxChargingState,
			int batteryMinChargePower,
			int batteryMaxChargePower,
			int batteryMinDischargePower,
			int batteryMaxDischargePower,
			int inverterMinComplexPower,
			int inverterMaxComplexPower, 
			int inverterMinPower,
			int inverterMaxPower,
			LoadProfileCompressionTypes compressionType,
			int compressionValue
			) {
		
		super(
				deviceId, 
				logger, 
				false, //does not cause scheduling
				true, //needs ancillary meter state as Input State
				false, //reacts to input states
				false, //is not static
				now, 
				DeviceTypes.BATTERYSTORAGE,
				new Commodity[]{Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
				compressionType,
				compressionValue);
		
		this.batteryInitialStateOfCharge = batteryInitialStateOfCharge;
		this.batteryInitialStateOfHealth = batteryInitialStateOfHealth;
		this.batteryStandingLoss = batteryStandingLoss;
		this.batteryMinChargingState = batteryMinChargingState;
		this.batteryMaxChargingState = batteryMaxChargingState;
		this.batteryMinChargePower = batteryMinChargePower;
		this.batteryMaxChargePower = batteryMaxChargePower;
		this.batteryMinDischargePower = batteryMinDischargePower;
		this.batteryMaxDischargePower = batteryMaxDischargePower;
		this.inverterMinComplexPower = inverterMinComplexPower;
		this.inverterMaxComplexPower = inverterMaxComplexPower;
		this.inverterMinPower = inverterMinPower;
		this.inverterMaxPower = inverterMaxPower;
	}
	
	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected BatteryStorageNonControllableIPP() {
		super();
		batteryInitialStateOfHealth = 0;
		batteryInitialStateOfCharge = 0;
		batteryStandingLoss = 0;
		batteryMinChargingState = 0;
		batteryMaxChargingState = 0;
		batteryMinChargePower = 0;
		batteryMinDischargePower = 0;
		batteryMaxChargePower = 0;
		inverterMinComplexPower = 0;
		inverterMaxComplexPower = 0;
		inverterMaxPower = 0;
		inverterMinPower = 0;
		batteryMaxDischargePower = 0;
	}


	
	@Override
	public void recalculateEncoding(long currentTime, long maxHorizon) {
		this.setReferenceTime(currentTime);
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
		
		this.stepSize = stepSize;
		if (createLoadProfile)
			this.lp = new SparseLoadProfile();
		else
			this.lp = null;
		
		// used for iteration in interdependent calculation
		this.interdependentTime = this.getReferenceTime();
		setOutputStates(null);
		this.interdependentInputStates = null;
		this.ancillaryMeterState = null;
		
	
		this.inverterModel = new SimpleInverterModel(
				inverterMinComplexPower,
				inverterMaxComplexPower, 
				inverterMinPower,
				inverterMaxPower);
		
		this.batteryModel = new SimpleBatteryStorageModel(
				batteryStandingLoss,
				batteryMinChargingState,
				batteryMaxChargingState,
				batteryMinChargePower,
				batteryMaxChargePower,
				batteryMinDischargePower,
				batteryMaxDischargePower,
				batteryInitialStateOfCharge);
	
		this.batteryLogic = new SimpleBatteryLogic();
	}

	@Override
	public void calculateNextStep() {
		
		if (this.interdependentInputStates != null) {
			
			// get information about available power
			int availablePower = 0;
			if (ancillaryMeterState != null) {
				availablePower = (int) ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
			}
			
			// get SOC of Battery
//			double stateOfCharge = this.batteryModel.getStateOfCharge();
			
			// update state
			batteryLogic.doStupidBMS(
					availablePower, 
					batteryModel, 
					inverterModel, 
					stepSize,
					0,
					0,
					0,
					0
					);
			
			// update interdependentOutputStates
			this.internalInterdependentOutputStates.setPower(
					Commodity.ACTIVEPOWER, this.inverterModel.getActivePower());
			this.internalInterdependentOutputStates.setPower(
					Commodity.REACTIVEPOWER, this.inverterModel.getReactivePower());
			setOutputStates(internalInterdependentOutputStates);
			
			if (lp != null) {
				lp.setLoad(Commodity.ACTIVEPOWER, interdependentTime, this.inverterModel.getActivePower());
				lp.setLoad(Commodity.REACTIVEPOWER, interdependentTime, this.inverterModel.getActivePower());
			}
		}
		else {
			getGlobalLogger().logDebug("interdependentInputStates == null");
		}
		
		this.interdependentTime += stepSize;
	}
	
	@Override
	public Schedule getFinalInterdependentSchedule() {
		if (lp != null) {
			if (lp.getEndingTimeOfProfile() > 0) {
				lp.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, 0);
				lp.setLoad(Commodity.REACTIVEPOWER, this.interdependentTime, 0);
			}
			return new Schedule(lp.getCompressedProfile(compressionType, compressionValue, compressionValue), 0, this.getDeviceType().toString());
		} else {
			return new Schedule(new SparseLoadProfile(), 0, this.getDeviceType().toString());
		}
	}	
	
	// ### to string ###
	
	@Override
	public String problemToString() {
		return "[" + getReferenceTime() + "] BatteryStorageNonControllableIPP " 
				+ "activePower="
				+ ( this.inverterModel != null ? this.inverterModel.getActivePower() : "N/A")
				+ " initialStateOfCharge="
				+ batteryInitialStateOfCharge;
	}
}