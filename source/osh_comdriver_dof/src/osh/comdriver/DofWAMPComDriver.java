package osh.comdriver;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.dof.DofWAMPDispatcher;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.dof.DofStateExchange;
import osh.registry.interfaces.IHasState;


/**
 * 
 * @author Sebastian Kramer
 *
 */
public class DofWAMPComDriver extends CALComDriver implements Runnable, IHasState {	
	
	DofWAMPDispatcher dofDispatcher;
	
	private Map<Integer, UUID> mieleUUIDMap = new HashMap<Integer, UUID>();
	private HashMap<UUID, Integer> lastSentValues = new HashMap<UUID, Integer>();

	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 */
	public DofWAMPComDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
		
		//ih: -1609550966		
		mieleUUIDMap.put(-1609550966, UUID.fromString("a010338a-4d49-4d49-0000-5e09c0a80114"));
		//coffee: -1609551312
		mieleUUIDMap.put(-1609551312, UUID.fromString("a0103230-4d49-4d49-0000-5e0ac0a80114"));
		//dw: -1609555510
		mieleUUIDMap.put(-1609555510, UUID.fromString("a01021ca-4d49-4d49-0000-5601c0a80114"));
		//ov: -1609555623
		mieleUUIDMap.put(-1609555623, UUID.fromString("a0102159-4d49-4d49-0000-5e06c0a80114"));
		//td: -1609555628
		mieleUUIDMap.put(-1609555628, UUID.fromString("a0102154-4d49-4d49-0000-5602c0a80114"));
		//wm: -1609555631
		mieleUUIDMap.put(-1609555631, UUID.fromString("a0102151-4d49-4d49-0000-5604c0a80114"));
		
		for (UUID device : mieleUUIDMap.values()) {
			lastSentValues.put(device, Integer.MIN_VALUE);
		}
	}


	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		this.dofDispatcher = new DofWAMPDispatcher(
				getGlobalLogger());
			
		new Thread(this, "push proxy of Miele dof bus driver to WAMP").start();
	}

	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		// NOTHING
	}


	@Override
	public void run() {
		while (true) {
			synchronized (this.dofDispatcher) {
				try { // wait for new data
					this.dofDispatcher.wait();
				} 
				catch (InterruptedException e) {
					getGlobalLogger().logError("should not happen", e);
					break;
				}
				
				long timestamp = getTimer().getUnixTime();
				
				if ( dofDispatcher.getDeviceMap().isEmpty() ) { // an error has occurred
					getGlobalLogger().logError("Device Data of Dof-WAMP-Dispatcher is empy");
				}
				
				for (Entry<Integer, Integer> dof : dofDispatcher.getDeviceMap().entrySet()) {
					// build UUID
					UUID mieleUUID = mieleUUIDMap.get(dof.getKey());
					if (!lastSentValues.get(mieleUUID).equals(dof.getValue())) {
						DofStateExchange dse = new DofStateExchange(mieleUUID, timestamp);
						dse.setDevice1stDegreeOfFreedom(dof.getValue());
						dse.setDevice2ndDegreeOfFreedom(dof.getValue());
						getComRegistry().setStateOfSender(DofStateExchange.class, dse);
						
						lastSentValues.put(mieleUUID, dof.getValue());
					}
				}				
			}
		}		
	}


	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
}
