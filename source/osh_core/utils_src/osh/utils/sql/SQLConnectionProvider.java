package osh.utils.sql;

import java.net.ConnectException;
import java.sql.Connection;

import com.jcraft.jsch.JSchException;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class SQLConnectionProvider {
	
	private static SingleSQLConnectionProvider connection;
	
	private static final SingleSQLConnectionProvider[] providers = {
		//if you want to consistently log to the AIFB-MySQL-Server please provide a user and PW for the SSH-Tunnel
		new SingleSQLAndSSHConnectionProvider(
				"server.tld", //database server 
				3306, // database port
				"database_scheme", //database scheme 
				"user", //SQL-User
				"pw", //SQL-PW
				null, //SSH-User
				null, //SSH-PW
				"ssh_server.tld", //SSH-Server
				22, //SSH-Port
				54053, //initial local port for the SSH-Tunnel
				false, //use key file for authentication
				null  //keyfile path
				), 
	};	

	private static void initSQLConnection () throws ConnectException {
		
		for (int idx = 0; idx < providers.length; idx++) {		
			try {
				providers[idx].getConnection();
				connection = providers[idx];
				return;
				
			} catch (ConnectException | JSchException e) {
				continue;
			}
		};		
		throw new ConnectException("No database reachable");		
	}
	

	private static void initSQLConnection (int indexOfPreferredConn) throws ConnectException {
		
		if (indexOfPreferredConn > -1 && indexOfPreferredConn < providers.length) {
			try {
				providers[indexOfPreferredConn].getConnection();
				connection = providers[indexOfPreferredConn];
				return;

			} catch (ConnectException | JSchException e) {
			}
		}
		
		initSQLConnection();
	}
	
	/** tries to get a connection from the array of possible connections
	 * 
	 * @return a connection to a mysql-server
	 * @throws ConnectException if no connection is available
	 */
	public static Connection getConnection() throws ConnectException, JSchException {
		if (connection == null)
			initSQLConnection();
		
		return connection.getConnection();
	}
	
	/** tries to get a connection but tests a preferred connection (selected by index) first
	 * 
	 * @param indexOfPreferredConn index of the preferred connection
	 * @return a connection to a mysql-server (if the preferred connection is available it will be returned, otherwise the first successful connection)
	 * @throws ConnectException if no connection is available
	 */
	public static Connection getConnection(int indexOfPreferredConn) throws ConnectException, JSchException {
		initSQLConnection(indexOfPreferredConn);
		
		return connection.getConnection();
	}
	
	public static void closeConnection() {
		if (connection != null) {
			connection.closeConnection();
			connection = null;
		}
	}
}
