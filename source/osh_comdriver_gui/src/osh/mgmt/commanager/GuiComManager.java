package osh.mgmt.commanager;

import java.util.Map;
import java.util.UUID;

import osh.cal.ICALExchange;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewerRegistryEnum;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.StateExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.datatypes.registry.oc.state.GUIScheduleDebugExchange;
import osh.datatypes.registry.oc.state.globalobserver.DevicesPowerStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIAncillaryMeterStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIDeviceListStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIHotWaterPredictionStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIScheduleStateExchange;
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
import osh.registry.interfaces.IEventTypeReceiver;


/**
 * 
 * @author Till Schuberth, Ingo Mauser, Jan Mueller
 *
 */
public class GuiComManager extends ComManager implements IEventTypeReceiver {
	
	private Class<? extends StateExchange> stateviewertype = null;
	private StateViewerRegistryEnum stateviewerregistry = StateViewerRegistryEnum.OC;
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param uuid
	 */
	public GuiComManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}

	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();

		// signals from utility or the like
		getOCRegistry().registerStateChangeListener(EpsStateExchange.class, this);
		getOCRegistry().registerStateChangeListener(PlsStateExchange.class, this);
		
		// states to visualize
		getOCRegistry().registerStateChangeListener(GUIScheduleStateExchange.class, this);
		getOCRegistry().registerStateChangeListener(GUIHotWaterPredictionStateExchange.class, this);
		getOCRegistry().registerStateChangeListener(GUIDeviceListStateExchange.class, this);
		getOCRegistry().registerStateChangeListener(GUIAncillaryMeterStateExchange.class, this);
		
		getOCRegistry().registerStateChangeListener(WaterStorageOCSX.class, this);
		getOCRegistry().registerStateChangeListener(BatteryStorageOCSX.class, this);
		getOCRegistry().registerStateChangeListener(DevicesPowerStateExchange.class, this);
		

		
		// schedule to visualize
		getOCRegistry().register(GUIScheduleDebugExchange.class, this);
		
		getTimer().registerComponent(this, 1);
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {

		if (event instanceof StateChangedExchange) {
			StateChangedExchange exsc = (StateChangedExchange) event;
			
			if (exsc.getType().equals(GUIScheduleStateExchange.class)) {
				GUIScheduleStateExchange se = 
						(GUIScheduleStateExchange) getOCRegistry().getState(GUIScheduleStateExchange.class, exsc.getStatefulentity());
				GUIScheduleComExchange gsce = new GUIScheduleComExchange(
						getUUID(), se.getTimestamp(), se.getDebugGetSchedules(), se.getStepSize());
				updateOcDataSubscriber(gsce);
				
			} 
			else if (exsc.getType().equals(GUIDeviceListStateExchange.class)) {
				GUIDeviceListStateExchange se = 
						(GUIDeviceListStateExchange) getOCRegistry().getState(GUIDeviceListStateExchange.class, exsc.getStatefulentity());
				GUIDeviceListComExchange gdlce = new GUIDeviceListComExchange(
						getUUID(), se.getTimestamp(), se.getDeviceList());
				updateOcDataSubscriber(gdlce);
			} 
			else if (exsc.getType().equals(WaterStorageOCSX.class)) {
				WaterStorageOCSX sx = (WaterStorageOCSX) getOCRegistry().getState(WaterStorageOCSX.class, exsc.getStatefulentity());
				GUIWaterStorageComExchange gwsce = new GUIWaterStorageComExchange(
						getUUID(), 
						sx.getTimestamp(), 
						sx.getCurrenttemp(), 
						sx.getMintemp(), 
						sx.getMaxtemp(),
						sx.getDemand(),
						sx.getSupply(),
						sx.getTankId());
				updateOcDataSubscriber(gwsce);
			}
			else if (exsc.getType().equals(BatteryStorageOCSX.class)) {
				BatteryStorageOCSX sx = (BatteryStorageOCSX) getOCRegistry().getState(BatteryStorageOCSX.class, exsc.getStatefulentity());
				GUIBatteryStorageComExchange gbsce = new GUIBatteryStorageComExchange(
						getUUID(), 
						sx.getTimestamp(), 
						sx.getStateOfCharge(), 
						sx.getMinStateOfCharge(), 
						sx.getMaxStateOfCharge(),
						sx.getBatteryId());
				updateOcDataSubscriber(gbsce);
			}
			
			
			else if (exsc.getType().equals(EpsStateExchange.class)) {
				EpsStateExchange eee = getOCRegistry().getState(EpsStateExchange.class, exsc.getStatefulentity());
				GUIEpsComExchange gece = new GUIEpsComExchange(getUUID(), eee.getTimestamp());
				gece.setPriceSignals(eee.getPriceSignals());
				updateOcDataSubscriber(gece);
			}
			else if (exsc.getType().equals(PlsStateExchange.class)) {
				PlsStateExchange pse = getOCRegistry().getState(PlsStateExchange.class, exsc.getStatefulentity());
				GUIPlsComExchange gpce = new GUIPlsComExchange(getUUID(), pse.getTimestamp());
				gpce.setPowerLimitSignals(pse.getPowerLimitSignals());
				updateOcDataSubscriber(gpce);
			}
			else if (exsc.getType().equals(DevicesPowerStateExchange.class)) {
				DevicesPowerStateExchange dpsex = getOCRegistry().getState(DevicesPowerStateExchange.class, exsc.getStatefulentity());
				DevicesPowerComExchange gpce = new DevicesPowerComExchange(getUUID(), dpsex.getTimestamp(), dpsex);
				updateOcDataSubscriber(gpce);
			}
			else if (exsc.getType().equals(GUIHotWaterPredictionStateExchange.class)) {
				GUIHotWaterPredictionStateExchange ghwpse = getOCRegistry().getState(GUIHotWaterPredictionStateExchange.class, exsc.getStatefulentity());
				GUIHotWaterPredictionComExchange ghwpce = new GUIHotWaterPredictionComExchange(
						getUUID(), 
						ghwpse.getTimestamp(), 
						ghwpse.getPredictedTankTemp(), 
						ghwpse.getPredictedHotWaterDemand(),
						ghwpse.getPredictedHotWaterSupply());
				updateOcDataSubscriber(ghwpce);
			} else if (exsc.getType().equals(GUIAncillaryMeterStateExchange.class)) {
				GUIAncillaryMeterStateExchange gamse = getOCRegistry().getState(GUIAncillaryMeterStateExchange.class, exsc.getStatefulentity());
				GUIAncillaryMeterComExchange gamce = new GUIAncillaryMeterComExchange(getUUID(), gamse.getTimestamp(), gamse.getAncillaryMeter());
				updateOcDataSubscriber(gamce);
			}
		} 		
	}

	@Override
	public void onDriverUpdate(ICALExchange exchangeObject) {
		if (exchangeObject instanceof GUIStateSelectedComExchange) {
			GUIStateSelectedComExchange gssce = (GUIStateSelectedComExchange) exchangeObject;
			synchronized (this) {
				stateviewertype = gssce.getSelected();
			}
		} else if (exchangeObject instanceof GUIStateRegistrySelectedComExchange) {
			osh.hal.exchange.GUIStateRegistrySelectedComExchange gssrce = (GUIStateRegistrySelectedComExchange) exchangeObject;
			synchronized (this) {
				stateviewerregistry = gssrce.getSelected();
			}
		}
	}

	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		// TODO build clean sum object (NOT WaterStoragSumDetails...)...maybe somewhere else
		// Best place would be GlobalObserver!

		synchronized (this) {
			if (stateviewerregistry == StateViewerRegistryEnum.OC) {
				Map<UUID, ? extends StateExchange> states = null;
				if (stateviewertype != null) {
					states = getOCRegistry().getStates(stateviewertype);
				}

				updateOcDataSubscriber(
						new GUIStatesComExchange(
								getUUID(), 
								getTimer().getUnixTime(), 
								getOCRegistry().getTypes(), 
								states));
			} else if (stateviewerregistry == StateViewerRegistryEnum.DRIVER) {
				updateOcDataSubscriber(
						new GUIStatesComExchange(
								getUUID(), 
								getTimer().getUnixTime(), 
								stateviewertype));
			}
		}
	}

}
