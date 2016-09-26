package osh.driver.simulation;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.driver.simulation.batterylogic.SimpleBatteryLogic;
import osh.driver.simulation.batterystorage.SimpleBatteryStorageModel;
import osh.driver.simulation.inverter.SimpleInverterModel;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.BatteryStorageOX;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;

/**
 * 
 * @author Jan Mueller
 *
 */
public class NonControllableBatterySimulationDriver extends DeviceSimulationDriver {

	//	// Battery parameters

	private final int STEP_SIZE = 1;

	private SimpleInverterModel inverterModel;
	private SimpleBatteryStorageModel batteryModel;
	private SimpleBatteryLogic batteryLogic;

	private int initialStateOfCharge = 0;

	private int batteryMinChargingState;
	private int batteryMaxChargingState;
	private int batteryMinDischargePower;
	private int batteryMaxDischargePower;
	private int batteryMinChargePower;
	private int batteryMaxChargePower;
	private int standingLoss = 0;

	private int inverterMinPower;
	private int inverterMaxPower;
	private int inverterMinComplexPower;
	private int inverterMaxComplexPower;

	private long newIppAfter;
	private int triggerIppIfDeltaSoCBigger;	

	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 * @throws HALException 
	 */
	public NonControllableBatterySimulationDriver(IOSH controllerbox, UUID deviceID,
			OSHParameterCollection driverConfig)
					throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig);

		// Battery parameters
		this.batteryMinChargingState = Integer.parseInt(driverConfig.getParameter("minChargingState")) ;
		this.batteryMaxChargingState = Integer.parseInt(driverConfig.getParameter("maxChargingState"));
		this.batteryMinDischargePower = Integer.parseInt(driverConfig.getParameter("minDischargingPower"));
		this.batteryMaxDischargePower = Integer.parseInt(driverConfig.getParameter("maxDischargingPower"));
		this.batteryMinChargePower = Integer.parseInt(driverConfig.getParameter("minChargingPower"));
		this.batteryMaxChargePower = Integer.parseInt(driverConfig.getParameter("maxChargingPower"));

		//FIXME (also in standing loss calculation)
		this.standingLoss = 0;

		//Inverter parameters
		this.inverterMinPower = Integer.parseInt(driverConfig.getParameter("minInverterPower"));
		this.inverterMaxPower = Integer.parseInt(driverConfig.getParameter("maxInverterPower"));
		this.inverterMinComplexPower = inverterMinPower;
		this.inverterMaxComplexPower = inverterMaxPower;

		try {
			this.newIppAfter = Long.valueOf(getDriverConfig().getParameter("newIppAfter"));
		}
		catch (Exception e) {
			this.newIppAfter = 1 * 3600; // 1 hour
			getGlobalLogger().logWarning("Can't get newIppAfter, using the default value: " + this.newIppAfter);
		}

		try {
			this.triggerIppIfDeltaSoCBigger = Integer.valueOf(getDriverConfig().getParameter("triggerIppIfDeltaSoCBigger"));
		}
		catch (Exception e) {
			this.triggerIppIfDeltaSoCBigger = 100;
			getGlobalLogger().logWarning("Can't get triggerIppIfDeltaSoCBigger, using the default value: " + this.triggerIppIfDeltaSoCBigger);
		}

		this.initialStateOfCharge = 0;

		this.batteryModel = new SimpleBatteryStorageModel(
				standingLoss,
				batteryMinChargingState, 
				batteryMaxChargingState, 
				batteryMinChargePower,
				batteryMaxChargePower,
				batteryMinDischargePower, 
				batteryMaxDischargePower, 
				initialStateOfCharge
				);

		this.inverterModel = new SimpleInverterModel(
				inverterMinComplexPower, 
				inverterMaxComplexPower, 
				inverterMinPower,
				inverterMaxPower
				);

		this.batteryLogic = new SimpleBatteryLogic();
	}

	@Override
	public void onNextTimeTick() {
		long now = getTimer().getUnixTime();

		int currentPowerAtGridConnection = 0;
		if (ancillaryMeterState != null) {
			currentPowerAtGridConnection = (int) ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
		}

		batteryLogic.doStupidBMS(
				currentPowerAtGridConnection,
				batteryModel,
				inverterModel,
				STEP_SIZE,
				0, //int OptimizedmaxChargePower,
				0, //int OptimizedminChargePower,
				0, //int OptimizedmaxDisChargePower,
				0  //int OptimizedminDisChargePower
				);


		// set state of driver
		setPower(Commodity.ACTIVEPOWER, inverterModel.getActivePower());
		setPower(Commodity.REACTIVEPOWER,  inverterModel.getReactivePower());

		// send OC
		BatteryStorageOX ox = new BatteryStorageOX(
				getDeviceID(), 
				now,
				inverterModel.getActivePower(),
				inverterModel.getReactivePower(),
				batteryModel.getStateOfCharge(),
				batteryModel.getStateOfHealth(),
				standingLoss,
				batteryMinChargingState,
				batteryMaxChargingState,
				batteryMinChargePower,
				batteryMaxChargePower,
				batteryMinDischargePower,
				batteryMaxDischargePower,
				inverterMinComplexPower,
				inverterMaxComplexPower,
				inverterMinPower,
				inverterMaxPower,
				0,
				newIppAfter,
				triggerIppIfDeltaSoCBigger,
				compressionType,
				compressionValue);

		this.notifyObserver(ox);
	}

	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}

}
