package osh.comdriver;

import java.util.HashMap;
import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.hal.exchange.DofComExchange;


/**
 * 
 * @author Till Schuberth, Ingo Mauser
 *
 */
public class DummyDoFProviderComDriver extends CALComDriver {
	
	HashMap<UUID, Integer> device1stDegreeOfFreedom;
	HashMap<UUID, Integer> device2ndDegreeOfFreedom;

	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 */
	public DummyDoFProviderComDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);

		this.device1stDegreeOfFreedom = new HashMap<>();
		this.device2ndDegreeOfFreedom = new HashMap<>();
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		processDofInformation();
	}
	

	private void processDofInformation() {
		
		DofComExchange dce = new DofComExchange(getDeviceID(), getTimer().getUnixTime());
		
		UUID[] deviceIds = {
				UUID.fromString("e2ef0d13-61b3-4188-b32a-1570dcbab4d1"), //INDUCTIONCOOKTOP
				UUID.fromString("de61f462-cda2-4941-8402-f93a1f1b3e57"), //COFFEESYSTEM
				UUID.fromString("ab9519db-7a14-4e43-ac3a-ade723802194"), //DISHWASHER
				UUID.fromString("cef732b1-04ba-49e1-8189-818468a0d98e"), //ELECTRICSTOVE
				UUID.fromString("1468cc8a-dfdc-418a-8df8-96ba8c146156"), //DRYER
				UUID.fromString("e7b3f13d-fdf6-4663-848a-222303d734b8")}; //WASHINGMACHINE
		
		
		int[] appliance1stDoF = 	  
			{	0,
				0,
				57600,
				0,
				57600,
				57600};
		
		int[] appliance2ndDoF = 	  
			{	0,
				0,
				1800,
				0,
				1800,
				1800};
		
		for (int i = 0; i < deviceIds.length; i++) {
			this.device1stDegreeOfFreedom.put(deviceIds[i], appliance1stDoF[i]);
			this.device2ndDegreeOfFreedom.put(deviceIds[i], appliance2ndDoF[i]);
		}
		
		this.notifyComManager(dce);
	}
	
	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		// NOTHING
	}

}
