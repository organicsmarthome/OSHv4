package osh.mgmt.localcontroller;

import java.util.UUID;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.driver.chp.model.GenericChpModel;
import osh.hal.exchange.ChpControllerExchange;
import osh.mgmt.ipp.DachsChpNonControllableIPP;
import osh.mgmt.mox.DachsChpMOX;
import osh.registry.interfaces.IHasState;
import osh.utils.physics.ComplexPowerUtil;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class NonControllableDachsChpLocalController 
				extends LocalController
				implements IHasState {
	
	private long lastTimeIppSent = Long.MIN_VALUE;
	private boolean lastSentState;
	

	private long newIPPAfter;
	private double currentHotWaterStorageMinTemp;
	private double currentHotWaterStorageMaxTemp;
	private double fixedCostPerStart;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	private long runningSince;
	private long stoppedSince;
	private int lastThermalPower;

	/**
	 * CONSTRUCTOR
	 */
	public NonControllableDachsChpLocalController(IOSHOC controllerbox) {
		super(controllerbox);
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		getTimer().registerComponent(this, 1);
	}
	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		
		
		// get new MOX
		DachsChpMOX mox = (DachsChpMOX) getDataFromLocalObserver();
		
		long now = getTimer().getUnixTime();
		this.newIPPAfter = mox.getNewIPPAfter();
		this.currentHotWaterStorageMinTemp = mox.getCurrentHotWaterStorageMinTemp();
		this.currentHotWaterStorageMaxTemp = mox.getCurrentHotWaterStorageMaxTemp();
		this.fixedCostPerStart = mox.getFixedCostPerStart();
		
		this.compressionType = mox.getCompressionType();
		this.compressionValue = mox.getCompressionValue();
		
		if (mox.isRunning())
			lastThermalPower = mox.getThermalPower();
		
		if ((now > lastTimeIppSent + newIPPAfter) || mox.isRunning() != lastSentState) {
			int typicalActivePower = mox.getTypicalActivePower();
			int typicalReactivePower = mox.getTypicalReactivePower();
			int typicalThermalpower = mox.getTypicalThermalPower();
			int typicalGasPower = mox.getTypicalGasPower();
			boolean isRunning = mox.isRunning();
			
			// new IPP
			boolean toBeScheduled = false;
			DachsChpNonControllableIPP sipp = null;
			try {
				sipp = new DachsChpNonControllableIPP(
						getDeviceID(), 
						getGlobalLogger(), 
						getTimer().getUnixTime(), 
						toBeScheduled,
						mox.getMinRuntime(),
						new GenericChpModel(
								typicalActivePower, 
								typicalReactivePower, 
								typicalThermalpower, 
								typicalGasPower, 
								ComplexPowerUtil.convertActiveAndReactivePowerToCosPhi(typicalActivePower, typicalReactivePower),
								isRunning, 
								lastThermalPower, 
								runningSince, 
								stoppedSince),
						isRunning,
						currentHotWaterStorageMinTemp,
						currentHotWaterStorageMaxTemp,
						mox.getWaterTemperature(), 
						fixedCostPerStart,
						compressionType,
						compressionValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
			getOCRegistry().setState(
					InterdependentProblemPart.class, this, sipp);
			
			lastTimeIppSent = now;
			lastSentState = isRunning;
		}
		
		double currentWaterTemp = mox.getWaterTemperature(); // get it...
		
		ChpControllerExchange cx = null;
		if (currentWaterTemp <= currentHotWaterStorageMinTemp) {
			//starting
			runningSince = now;
			cx = new ChpControllerExchange(
					getDeviceID(), 
					now, 
					false, 
					false, 
					true,
					15 * 60);
		}
		else if (currentWaterTemp >= currentHotWaterStorageMaxTemp) {
			stoppedSince = now;
			cx = new ChpControllerExchange(
					getDeviceID(), 
					getTimer().getUnixTime(), 
					true,
					false, 
					false, 
					0);
		}
		if (cx != null) {
			this.updateOcDataSubscriber(cx);
		}
	}
	
	
	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

}
