package osh.mgmt.commanager;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;

import osh.core.bus.BusManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.StateExchange;
import osh.datatypes.registry.commands.SwitchCommandExchange;
import osh.datatypes.registry.commands.SwitchRequest;
import osh.datatypes.registry.driver.details.energy.ElectricPowerDriverDetails;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;
import osh.datatypes.registry.oc.state.MieleDofStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.hal.exchange.HttpRestInteractionComManagerExchange;
import osh.registry.interfaces.IEventTypeReceiver;


/**
 * get stuff from O/C-registry
 * @author Kaibin Bao
 *
 */
public class HttpRestInteractionBusManager extends BusManager implements IEventTypeReceiver {

	public HttpRestInteractionBusManager(
			IOSHOC controllerbox,
			UUID uuid) {
		super(controllerbox, uuid);
	}
	
	public boolean setSwitchDetails(UUID element, SwitchRequest sd) {
		SwitchCommandExchange swcmd = new SwitchCommandExchange(
				getUUID(), 
				element, 
				getTimer().getUnixTime(), 
				sd.getTurnOn());
		getOCRegistry().sendCommand(SwitchCommandExchange.class, swcmd);
		
		return true;
	}
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		ArrayList<Class<? extends StateExchange>> stateTypesPushedToDriver = new ArrayList<>();
		stateTypesPushedToDriver.add(ElectricPowerDriverDetails.class);
		stateTypesPushedToDriver.add(CommodityPowerStateExchange.class);
		stateTypesPushedToDriver.add(ExpectedStartTimeExchange.class);
		
		initializeStatePushToDriver( stateTypesPushedToDriver );
	}

	// push states to com driver { 
	
	private void initializeStatePushToDriver(ArrayList<Class<? extends StateExchange>> stateTypesPushedToDriver) throws OSHException {
		// register to future state changes
		for( Class<? extends StateExchange> type : stateTypesPushedToDriver ) {
			getOCRegistry().registerStateChangeListener(type, this);
		}

		// push current states to driver
		for( Class<? extends StateExchange> type : stateTypesPushedToDriver ) {
			for( Entry<UUID, ? extends StateExchange> ent : getOCRegistry().getStates(type).entrySet() ) {
				HttpRestInteractionComManagerExchange toDriverExchange = new HttpRestInteractionComManagerExchange(
						getUUID(), getTimer().getUnixTime(), ent.getValue() );
				
				updateOcDataSubscriber(toDriverExchange);
			}
		}
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		if( event instanceof StateChangedExchange) {
			UUID uuid = ((StateChangedExchange) event).getStatefulentity();
			Class<? extends StateExchange> typeOfObj = ((StateChangedExchange) event).getType();
			StateExchange sx = getOCRegistry().getState(typeOfObj, uuid);
			
			HttpRestInteractionComManagerExchange toDriverExchange = new HttpRestInteractionComManagerExchange(
					getUUID(), 
					getTimer().getUnixTime(), 
					sx );
			
			updateOcDataSubscriber(toDriverExchange);
		}
	}

	// } 

	public MieleDofStateExchange getDof(UUID uuid) {
		return getOCRegistry().getState(MieleDofStateExchange.class, uuid);
	}

	@Override
	public void onDriverUpdate(IHALExchange exchangeObject) {
		//NOTHING
	}

}
