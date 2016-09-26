package osh.hal.exchange;

import java.util.UUID;

import osh.datatypes.registry.StateExchange;
import osh.eal.hal.exchange.HALControllerExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class HttpRestInteractionComManagerExchange 
				extends HALControllerExchange {
	
	private StateExchange stateExchange;

	/**
	 * 
	 * @param deviceID
	 * @param timestamp
	 */
	public HttpRestInteractionComManagerExchange(UUID deviceID, Long timestamp, StateExchange stateExchange) {
		super(deviceID, timestamp);
		this.stateExchange = stateExchange;
	}

	public StateExchange getStateExchange() {
		return stateExchange;
	}
	
	public void setStateExchange(StateExchange stateExchange) {
		this.stateExchange = stateExchange;
	}
}
