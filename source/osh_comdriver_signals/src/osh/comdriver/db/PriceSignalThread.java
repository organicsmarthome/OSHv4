package osh.comdriver.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import osh.comdriver.DBPriceSignalProviderComDriver;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;


/**
 * 
 * @author ???
 *
 */
public class PriceSignalThread extends Thread {
	
	private AncillaryCommodity commodity;
	
	private IGlobalLogger globalLogger;
	private DBPriceSignalProviderComDriver priceSignalProvider;

	private MySqlConnectionHandler database;
	
	public PriceSignalThread(IGlobalLogger globalLogger, DBPriceSignalProviderComDriver priceSignalProvider) {
		super();
		
		this.commodity = AncillaryCommodity.ACTIVEPOWEREXTERNAL;
		
		this.globalLogger = globalLogger;
		this.priceSignalProvider = priceSignalProvider;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			while(true) {
				PriceSignal price = getPriceSignalData();
				PowerLimitSignal limit = getPowerLimitSignalData();
				if( price != null )
					priceSignalProvider.processPriceSignal(price, limit);
				try {
					sleep(120000); // 120 sec.					
				} catch (InterruptedException e) {		
					globalLogger.logError(e);
				}
			}
		} catch (Exception e) {
			globalLogger.logError(e);
		}
	}
	
	public void setUpSQLConnection(String dbHost, String dbPort, String dbName, String dbUser, String dbPasswd) throws ClassNotFoundException {
		database = new MySqlConnectionHandler(dbHost, dbPort, dbName, dbUser, dbPasswd);
		
		globalLogger.logDebug("* establishing SQL connection for priceSignal driver..."); 
	    try {
			database.connect();
			globalLogger.logDebug("* ...SQL connection for priceSignal driver OK"); 		
		} catch (SQLException e) {
			globalLogger.logError("* ...SQL connection for priceSignal driver FAILED", e); 		
		}
	}

	
	
	private PriceSignal getPriceSignalData(){
		Statement statement = null;
		String query = null;
		ResultSet resultSet = null;
		PriceSignal pricesignal = new PriceSignal(commodity);

		// round time to last full hour
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		
		long startTimeForSPS = cal.getTimeInMillis()/1000L;
		long endTimeForSPS = startTimeForSPS + 36 * 3600; //get the sps for the next 36h!
		
		try {
			Connection connection = database.getConnection();
			statement = connection.createStatement();
		} catch ( SQLException e ) {
			globalLogger.logError("SQL connection error", e);
			database.closeConnection();
			return null;
		}
		
		try {
			query = "SELECT * FROM sps WHERE timestamp >= " + startTimeForSPS + " AND timestamp < " + endTimeForSPS;
			statement.execute(query);
			
			resultSet = statement.getResultSet();
			
			while (resultSet.next()) {
				Integer timeStamp = Integer.valueOf(resultSet.getInt("timestamp"));
				Double price = Double.parseDouble(Integer.toString(resultSet.getInt("price")));
				pricesignal.setPrice(timeStamp, price);
			}
			
			pricesignal.setKnownPriceInterval(startTimeForSPS, endTimeForSPS);
			
			statement.close();
		}
		catch (SQLException ex) {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// ignore
				}
			}
			globalLogger.logError("getting DofData failed!", ex);
			database.closeConnection();
		}
		
		return pricesignal;
	}
	
	private PowerLimitSignal getPowerLimitSignalData(){
		Statement statement = null;
		String query = null;
		ResultSet resultSet = null;
		PowerLimitSignal powerlimit = new PowerLimitSignal();

		// round time to last full hour
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		
		long startTimeForEPS = cal.getTimeInMillis()/1000L;
		long endTimeForEPS = startTimeForEPS + 36 * 3600; //get the eps for the next 24h!
		
		Connection connection;
		
		try {
			connection = database.getConnection();
			statement = connection.createStatement();
		} catch ( SQLException e ) {
			globalLogger.logError("SQL connection error", e);
			database.closeConnection();
			return null;
		}
		
		try {
			// last load signal
			
			query = "SELECT * FROM lbs WHERE timestamp <= " + startTimeForEPS + " ORDER BY timestamp DESC LIMIT 1";
			statement.execute(query);
			resultSet = statement.getResultSet();
			
			while (resultSet.next()) {
				long timeStamp = resultSet.getInt("timestamp");
				Double limit = Double.parseDouble(Integer.toString(resultSet.getInt("price")));
				
				powerlimit.setPowerLimit(timeStamp, limit);
			}
			
			statement.close();
			statement = connection.createStatement();

			// future load signals
			
			query = "SELECT * FROM lbs WHERE timestamp > " + startTimeForEPS + " AND timestamp < " + endTimeForEPS;
			statement.execute(query);
			
			resultSet = statement.getResultSet();
			
			while (resultSet.next()) {
				Integer timeStamp = Integer.valueOf(resultSet.getInt("timestamp"));
				Double limit = Double.parseDouble(Integer.toString(resultSet.getInt("price")));
				
				powerlimit.setPowerLimit(timeStamp, limit);
			}
			
			powerlimit.setKnownPowerLimitInterval(startTimeForEPS, endTimeForEPS);
			
			statement.close();
		}
		catch (SQLException ex) {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// ignore
				}
			}
			globalLogger.logError("getting PowerLimitData failed!", ex);
			database.closeConnection();
		}
		
		return powerlimit;
	}
}
