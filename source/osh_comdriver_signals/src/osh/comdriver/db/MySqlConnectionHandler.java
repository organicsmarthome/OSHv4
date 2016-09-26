package osh.comdriver.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 
 * @author ???
 *
 */
public class MySqlConnectionHandler {
	private String dbHost;
	private String dbPort;
	private String dbUser;
	private String dbPasswd;
	private String dbName;
	private Connection connection = null;
	
	public MySqlConnectionHandler(String dbHost, String dbPort, String dbName, String dbUser,
			String dbPasswd) throws ClassNotFoundException {
		super();
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbUser = dbUser;
		this.dbPasswd = dbPasswd;
		this.dbName = dbName;

		Class.forName( "com.mysql.jdbc.Driver" );
	}

	public void connect() throws SQLException {
		String host_url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
		
		connection = DriverManager.getConnection(host_url, dbUser, dbPasswd);
	}
	
	public void closeConnection() {
		if( connection != null ) {
			try {
				connection.close();
			} catch (SQLException e) {
				// ignore; we tried our best
			}
			connection = null;
		}
	}
	
	public Connection getConnection() throws SQLException {
		if( connection == null || connection.isClosed() )
			connect();
		return connection;
	}
}
