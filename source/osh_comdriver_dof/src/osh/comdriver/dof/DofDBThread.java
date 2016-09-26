package osh.comdriver.dof;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import osh.comdriver.DatabaseDofProviderComDriver;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;


/**
 * 
 * @author ???
 *
 */
public class DofDBThread extends Thread {

	private static Connection dofSQLconnection;

	private IGlobalLogger globalLogger;
	private HashMap<UUID, Integer> appliance1stDof;
	private HashMap<UUID, Integer> appliance2ndDof;
	private DatabaseDofProviderComDriver userInteractionProvider;
	
	private String dbHost;
	private String dbPort;
	private String dbName;
	private String dbUser;
	private String dbPasswd;
	
	/**
	 * CONSTRUCTOR
	 * @param globalLogger
	 * @param userInteractionProvider
	 */
	public DofDBThread(
			IGlobalLogger globalLogger,
			DatabaseDofProviderComDriver userInteractionProvider,
			String dbHost, String dbPort, String dbName,
			String dbUser, String dbPasswd) {
		super();
		
		this.globalLogger = globalLogger;
		this.appliance1stDof = new HashMap<>();
		this.appliance2ndDof = new HashMap<>();
		this.userInteractionProvider = userInteractionProvider;

		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbName = dbName;
		this.dbUser = dbUser;
		this.dbPasswd = dbPasswd;
	}

	/**
	 * @return the dofSQLconnection
	 */
	public Connection getDofSQLconnection() {
		return dofSQLconnection;
	}

	/**
	 * 
	 * @param dbHost
	 * @param dbPort
	 * @param dbName
	 * @param dbUser
	 * @param dbPasswd
	 * @throws SQLException
	 */
	public void setUpSQLConnection() throws SQLException {

		globalLogger
				.logDebug("* establishing SQL connection for DoF driver...");

		// set db adress DB
		String host_url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/"
				+ dbName;

		dofSQLconnection = DriverManager.getConnection(host_url, dbUser,
				dbPasswd);

		globalLogger.logDebug("* ...SQL connection for DoF driver OK");

	}

	@Override
	public void run() {
		super.run();

		while (true) {

			try {
				getDofData();
				this.userInteractionProvider.processDofInformation(
						appliance1stDof, 
						appliance2ndDof);
				this.renewApplianceScheduleTable();
			} 
			catch (Exception e) {
				globalLogger.logError(
						"transfering user interaction data faild", e);
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
			} // ignore
		}
	}

	private void renewApplianceScheduleTable() {
		Statement statement = null;
		String query = null;
		try {
			// get the connection
			statement = dofSQLconnection.createStatement();
			// set the autocommit to false
			dofSQLconnection.setAutoCommit(false);
			// first delete old data
			query = "DELETE FROM `" + "appliance_schedule" + "`";
			statement.execute(query);

			ArrayList<ExpectedStartTimeExchange> applianceSchedule = this.userInteractionProvider
					.triggerComManager();

			if (applianceSchedule != null) {

				for (int i = 0; i < applianceSchedule.size(); i++) {

					String devId = applianceSchedule.get(i).getSender()
							.toString();
					int startTime = (int) applianceSchedule.get(i)
							.getExpectedStartTime();

					query = "INSERT INTO `" + "appliance_schedule" + "`"
							+ "(`uuid`, `scheduled_at`, `scheduled_to`) "
							+ "VALUES (" + "'" + devId + "', " + "'"
							+ (System.currentTimeMillis() / 1000L) + "', "
							+ "'" + startTime + "'" + ")";
					statement.execute(query);
				}
				// commit the query
				dofSQLconnection.commit();

				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}

				}
			}

		} catch (Exception ex) {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			globalLogger.logError("update of the DOF table failed", ex);
			try {
				dofSQLconnection.close();
				getDofSQLconnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} finally {
			// accept now the auto commit again
			try {
				dofSQLconnection.setAutoCommit(true);
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void getDofData() {
		Statement statement = null;
		String query = null;
		ResultSet resultSet = null;

		try {
			statement = dofSQLconnection.createStatement();

			query = "SELECT * FROM appliance_dof";
			statement.execute(query);
			// dofSQLconnection.commit();

			resultSet = statement.getResultSet();

			while (resultSet.next()) {

				UUID appId = UUID.fromString(resultSet.getString("uuid"));
				Integer app1stDof = Integer.valueOf(resultSet.getInt("firstdof"));
				Integer app2ndDof = Integer.valueOf(resultSet.getInt("seconddof"));

				appliance1stDof.put(appId, app1stDof);
				appliance2ndDof.put(appId, app2ndDof);
			}
			statement.close();
		} catch (Exception ex) {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				dofSQLconnection.close();
				getDofSQLconnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			globalLogger.logError("getting DofData failed!", ex);
		}
	}

}
