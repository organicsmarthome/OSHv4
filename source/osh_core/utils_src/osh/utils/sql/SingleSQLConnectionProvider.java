package osh.utils.sql;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.jcraft.jsch.JSchException;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class SingleSQLConnectionProvider {
	
	protected Connection conn;
	
	protected final String databaseServerName;
	protected final int databaseServerPort;
	protected final String databaseServerScheme;

	protected final String databaseUser;
	protected final String databasePW;
	
	protected final String trustStorePath;
	protected final String trustStorePW;
	
	protected boolean useSSL = false;
	
	//test statement for mysql that does not depend on tables
	protected static final String testStatement = "SELECT 1";
	
	public SingleSQLConnectionProvider(String databaseServerName, int databaseServerPort, String databaseServerScheme, String databaseUser,
			String databasePW) {
		super();
		this.databaseServerName = databaseServerName;
		this.databaseServerPort = databaseServerPort;
		this.databaseServerScheme = databaseServerScheme;
		this.databaseUser = databaseUser;
		this.databasePW = databasePW;
		this.trustStorePath = null;
		this.trustStorePW = null;
	}
	
	public SingleSQLConnectionProvider(String databaseServerName, int databaseServerPort, String databaseServerScheme, String databaseUser,
			String databasePW, boolean useSSL, String trustStorePath, String trustStorePW) {
		super();
		this.databaseServerName = databaseServerName;
		this.databaseServerPort = databaseServerPort;
		this.databaseServerScheme = databaseServerScheme;
		this.databaseUser = databaseUser;
		this.databasePW = databasePW;
		this.useSSL = useSSL;
		this.trustStorePath = trustStorePath;
		this.trustStorePW = trustStorePW;
	}
	
	protected void initConnection() throws ConnectException {
		try {
			String url = "jdbc:mysql://" + databaseServerName + ":" + databaseServerPort + "/" + databaseServerScheme;
			
			if (useSSL) {
				url += "?useSSL=true&requireSSL=true";
				System.setProperty("javax.net.ssl.trustStore",trustStorePath);
				System.setProperty("javax.net.ssl.trustStorePassword",trustStorePW);
//				System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
				System.setProperty("https.protocols", "TLSv1.2");
//				System.setProperty("javax.net.debug","all");
			}
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection(url, databaseUser,
					databasePW);


			Statement stmt = conn.createStatement();
			stmt.execute(testStatement);
			stmt.close();
			
			this.conn = conn;
		} catch (ClassNotFoundException | SQLException e) {
			throw new ConnectException("database not reachable");
		}
	}
	
	public Connection getConnection() throws ConnectException, JSchException {		
		if (conn == null)
			initConnection();
		
		return conn;
	}
	
	public void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		conn = null;
	}
}
