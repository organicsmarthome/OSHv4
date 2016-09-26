package osh.comdriver;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.dof.DofDBThread;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;
import osh.hal.exchange.DofComExchange;
import osh.hal.exchange.ScheduledApplianceUIexchange;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public class DatabaseDofProviderComDriver extends CALComDriver {
	
	HashMap<UUID, Integer> device1stDegreeOfFreedom = new HashMap<UUID, Integer>();
	HashMap<UUID, Integer> device2ndDegreeOfFreedom = new HashMap<>();

	private DofDBThread dbThread;
	private final OSHParameterCollection config;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 */
	public DatabaseDofProviderComDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);

		this.config = driverConfig;
	}


	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		//processDummyDofInformation();
		String hostname = config.getParameter("dofdbhost");
		if( hostname == null )
			throw new OSHException("missing config argument dofdbhost");
		
		String port = config.getParameter("dofdbport");
		if( port == null )
			throw new OSHException("missing config argument dofdbport");

		String dbname = config.getParameter("dofdbname");
		if( dbname == null )
			throw new OSHException("missing config argument dofdbname");

		String dbloginname = config.getParameter("dofdbloginname");
		if( dbloginname == null )
			throw new OSHException("missing config argument dofdbloginname");

		String dbpassword = config.getParameter("dofdbloginpwd");
		if( dbpassword == null )
			throw new OSHException("missing config argument dofdbloginpwd");

		dbThread = new DofDBThread(this.getGlobalLogger(), this,
				hostname,
				port,
				dbname,
				dbloginname,
				dbpassword );
		
		try {
			Class.forName( "com.mysql.jdbc.Driver" );
			dbThread.setUpSQLConnection();
		} catch (SQLException e) {
			getGlobalLogger().logError(e);
		} catch (ClassNotFoundException e) {
			getGlobalLogger().logError(e);
		}
			
		dbThread.start();
	}
	
	public ArrayList<ExpectedStartTimeExchange> triggerComManager() {
		// TODO: If you want to see the planned appliances in the EMP then
		// add here some fancy code...

		ScheduledApplianceUIexchange applianceUIexchange = new ScheduledApplianceUIexchange(
				getDeviceID(), getTimer().getUnixTime());
		return applianceUIexchange.getCurrentApplianceSchedules();
	}
	
	
	public void processDofInformation(HashMap<UUID, Integer> appliance1stDof, HashMap<UUID, Integer> appliance2ndDof){
		
		for (Entry<UUID,Integer> e : appliance1stDof.entrySet()) {
			this.device1stDegreeOfFreedom.put(e.getKey(), e.getValue());
		}
		for (Entry<UUID,Integer> e : appliance2ndDof.entrySet()) {
			this.device2ndDegreeOfFreedom.put(e.getKey(), e.getValue());
		}
		
		DofComExchange dce = new DofComExchange(getDeviceID(), getTimer().getUnixTime());
		dce.setDevice1stDegreeOfFreedom(device1stDegreeOfFreedom);
		dce.setDevice2ndDegreeOfFreedom(device2ndDegreeOfFreedom);
		
		this.notifyComManager(dce);
	}

	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		// NOTHING
	}
}
