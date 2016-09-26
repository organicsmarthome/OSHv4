package osh.comdriver.simulation;

import java.util.Map;
import java.util.UUID;

import osh.OSH;
import osh.cal.ICALExchange;
import osh.comdriver.simulation.cruisecontrol.GuiDataCollector;
import osh.comdriver.simulation.cruisecontrol.GuiMain;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewerListener;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewerRegistryEnum;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.StateExchange;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.hal.exchange.DevicesPowerComExchange;
import osh.hal.exchange.GUIAncillaryMeterComExchange;
import osh.hal.exchange.GUIBatteryStorageComExchange;
import osh.hal.exchange.GUIDeviceListComExchange;
import osh.hal.exchange.GUIEpsComExchange;
import osh.hal.exchange.GUIHotWaterPredictionComExchange;
import osh.hal.exchange.GUIPlsComExchange;
import osh.hal.exchange.GUIScheduleComExchange;
import osh.hal.exchange.GUIStateRegistrySelectedComExchange;
import osh.hal.exchange.GUIStateSelectedComExchange;
import osh.hal.exchange.GUIStatesComExchange;
import osh.hal.exchange.GUIWaterStorageComExchange;
import osh.simulation.SimulationComDriver;
import osh.simulation.exception.SimulationSubjectException;


/**
 * 
 * @author Till Schuberth, Ingo Mauser, Jan Mueller
 *
 */
public class GuiComDriver extends SimulationComDriver implements StateViewerListener {

	private GuiMain driver;
	private GuiDataCollector collector;
	
	boolean saveGraph = false;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 */
	public GuiComDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) throws SimulationSubjectException {
		super(controllerbox, deviceID, driverConfig);
		
		driver = new GuiMain(!getOSH().isSimulation());
		driver.registerListener(this);
		collector = new GuiDataCollector(driver, saveGraph);
	}

	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 1);
	}
	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();		
		driver.updateTime(getTimer().getUnixTime());
	}
	
	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		
		if (exchangeObject instanceof GUIScheduleComExchange) {
			GUIScheduleComExchange exgs = (GUIScheduleComExchange) exchangeObject;
			collector.updateGlobalSchedule(exgs.getSchedules(), exchangeObject.getTimestamp());
		} 
		else if (exchangeObject instanceof GUIDeviceListComExchange) {
			GUIDeviceListComExchange exgdl = (GUIDeviceListComExchange) exchangeObject;
			collector.updateEADeviceList(exgdl.getDeviceList());
		}
		else if (exchangeObject instanceof GUIStatesComExchange) {
			GUIStatesComExchange exgse = (GUIStatesComExchange) exchangeObject;
			if (exgse.isOcMode()) {
				collector.updateStateView(exgse.getTypes(), exgse.getStates());
			} else {
				Map<UUID, ? extends StateExchange> states = null;
				if (exgse.getDriverstatetype() != null) {
					states = ((OSH) getOSH()).getDriverRegistry().getStates(exgse.getDriverstatetype());
				}

				collector.updateStateView(((OSH) getOSH()).getDriverRegistry().getTypes(), states);
			}
		}
		else if (exchangeObject instanceof GUIEpsComExchange) {
			GUIEpsComExchange gece = (GUIEpsComExchange) exchangeObject;
			collector.updateGlobalSchedule(gece.getPriceSignals(), gece.getTimestamp());
		}
		else if (exchangeObject instanceof GUIPlsComExchange) {
			GUIPlsComExchange gpce = (GUIPlsComExchange) exchangeObject;
			collector.updateGlobalSchedule(gpce.getTimestamp(), gpce.getPowerLimitSignals());
		}
		else if (exchangeObject instanceof GUIWaterStorageComExchange) {
			GUIWaterStorageComExchange gwsce = (GUIWaterStorageComExchange) exchangeObject;
			WaterStorageOCSX gwsse = new WaterStorageOCSX(
					gwsce.getDeviceID(), 
					gwsce.getTimestamp(), 
					gwsce.getCurrenttemp(), 
					gwsce.getMintemp(), 
					gwsce.getMaxtemp(),
					gwsce.getDemand(),
					gwsce.getSupply(),
					gwsce.getTankId());
			collector.updateWaterStorageData(gwsse);
		}
		else if (exchangeObject instanceof GUIHotWaterPredictionComExchange) {
			GUIHotWaterPredictionComExchange ghwpce = (GUIHotWaterPredictionComExchange) exchangeObject;
			collector.updateWaterPredictionData(
					ghwpce.getPredictedTankTemp(), ghwpce.getPredictedHotWaterDemand(), ghwpce.getPredictedHotWaterSupply(), ghwpce.getTimestamp());
		}
		else if (exchangeObject instanceof GUIAncillaryMeterComExchange) {
			GUIAncillaryMeterComExchange gamce = (GUIAncillaryMeterComExchange) exchangeObject;
			
			collector.updateAncillaryMeter(gamce.getAncillaryMeter(), gamce.getTimestamp());
		}		
		else if (exchangeObject instanceof GUIBatteryStorageComExchange) {
			GUIBatteryStorageComExchange gbsce = (GUIBatteryStorageComExchange) exchangeObject;
			BatteryStorageOCSX gbsse = new BatteryStorageOCSX(
					gbsce.getDeviceID(), 
					gbsce.getTimestamp(), 
					gbsce.getCurrentStateOfCharge(), 
					gbsce.getMinStateOfCharge(), 
					gbsce.getMaxStateOfCharge(),
					gbsce.getBatteryId());
			collector.updateBatteryStorageData(gbsse);
		}		
		else if (exchangeObject instanceof DevicesPowerComExchange) {
			DevicesPowerComExchange dpsex = (DevicesPowerComExchange) exchangeObject;
			collector.updatePowerStates(dpsex.getTimestamp(), dpsex.getPowerStates());
		}
		else {
			getGlobalLogger().logError("unknown exchange data type: " + exchangeObject.getClass().getName());
			return;
		}
			
	}

	@Override
	public void stateViewerClassChanged(Class<? extends StateExchange> cls) {
		notifyComManager(
				new GUIStateSelectedComExchange(
						getDeviceID(), 
						getTimer().getUnixTime(), 
						cls));
	}


	@Override
	public void stateViewerRegistryChanged(StateViewerRegistryEnum registry) {
		notifyComManager(
				new GUIStateRegistrySelectedComExchange(
						getDeviceID(), 
						getTimer().getUnixTime(), 
						registry));
	}
}
