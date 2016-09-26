package osh.driver.meter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import osh.core.logging.IGlobalLogger;
import osh.driver.BcontrolSmartMeterDriver;
import osh.eal.hal.HALRealTimeDriver;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class BcontrolConnectorThread implements Runnable {
	
	private final String logInPage = "/mum-webservice/0/start.php";
	private final String meteringPage = "/mum-webservice/consumption.php?meter_id=0/";
	private final String heaterPagePart1 = "/unieq/cmsd/sensor/";
	private final String heaterPagePart2 = ".json?idtype=uuid";
	
	private IGlobalLogger logger;
	private HALRealTimeDriver timer;
	private BcontrolSmartMeterDriver meterDriver;
	
	private String meterURL;
	private int meterNumber; //  mitte = 14243021
	
//	private UUID heaterUUID = UUID.fromString("c1493681-5cfb-45e9-a55c-e65b8aa6ae0d");
	private UUID heaterUUID;
	
	private boolean isConnected = false;
	private boolean isHeaterConnected = false;
	private boolean isRunning = true;
	
	private String password = "pw";

	
	/**
	 * CONSTRUCTOR
	 * */
	public BcontrolConnectorThread(
			IGlobalLogger logger, 
			HALRealTimeDriver timer,
			BcontrolSmartMeterDriver meterDriver,
			String meterURL,
			int meterNumber,
			UUID heaterUUID) {
		
		this.logger = logger;
		this.timer = timer;
		this.meterDriver = meterDriver;
		this.meterURL = meterURL;
		this.meterNumber = meterNumber;
		this.heaterUUID = heaterUUID;
	}

	@Override
	public void run() {
		
		try {
			Thread.sleep(5000);
		} 
    	catch (InterruptedException e) {
			e.printStackTrace();
			logger.logDebug(e);
		}
		
		// set cookie manager
//		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		CookieManager manager = new CookieManager();
	    manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
	    CookieHandler.setDefault(manager);
	    
	    if (heaterUUID != null) {
	    	// works only with one Heater...
	    	MyAuthenticator myAuth = new MyAuthenticator(); 
			Authenticator.setDefault(myAuth);
	    }
	    
	    long lastPull = timer.getUnixTime();
	    
	    while(isRunning) {
	    	
	    	// check whether meter is connected
	    	if (!isConnected) {
	    		// connect to meter
	    		String meterConnectURL = getOpenConnectionURLString();
	    		String reply = "";
	    		try {
					reply = openConnectionToMeter(meterConnectURL);
				} 
	    		catch (Exception e) {
					e.printStackTrace();
				}
	    		if (reply == null || reply.contains("{\"authentication\":false}")) {
	    			isConnected = false;
	    			isHeaterConnected = false;
	    		}
	    		else {
	    			isConnected = true;
	    		}
	    	}
	    	
	    	
	    	if (isConnected) {
	    		// read data
	    		{
		    		// read meter data
			    	String meterMeteringURL = getReadMeterDataURLString();
			    	String meterDataString = "";
			    	try {
						meterDataString = readMeterData(meterMeteringURL);
						// check whether connection/authentication failed
						if (meterDataString == null || meterDataString.contains("{\"authentication\":false}")) {
				    		isConnected = false;
				    		isHeaterConnected = false;
				    		logger.logDebug("FAILED meterDataString=" + meterDataString);
				    	}
						else {
//							logger.logDebug("SUCCESS meterDataString=" + meterDataString);
							meterDriver.receiveMeterMessageFromMeter(meterDataString);
						}
					} 
			    	catch (Exception e) {
						e.printStackTrace();
						isConnected = false;
						isHeaterConnected = false;
					}
			    	
			    	if (isConnected && meterDataString == null) {
			    		isConnected = false;
			    		isHeaterConnected = false;
			    		logger.logDebug("was connected and meterDataString == null");
			    	}
	    		}
		    	
		    	// read heater data
		    	if (heaterUUID != null) {
		    		String meterHeaterURL = getReadHeaterDataURLString();
			    	String heaterDataString = "";
			    	try {
			    		if (!isHeaterConnected) {
			    			openConnectionToHeaterReading(meterHeaterURL);
			    		}
			    		
			    		heaterDataString = readHeaterData(meterHeaterURL);
						// check whether connection/authentication failed
			    		/*
			    		 {
						   "status": "failed"
						 }
						*/
			    		
						if (heaterDataString == null || heaterDataString.contains("\"status\": \"failed\"")) {
				    		isHeaterConnected = false;
				    		logger.logDebug("FAILED heaterDataString=" + heaterDataString);
				    	}
						else {
							isHeaterConnected = true;
//							logger.logDebug("SUCCESS heaterDataString=" + heaterDataString);
							try {
								meterDriver.receiveHeaterMessageFromMeter(heaterDataString);
							}
							catch (IOException e) {
								logger.logWarning(e.getStackTrace(), e);
								isHeaterConnected = false;
							}
							catch (Exception e) {
								logger.logWarning(e.getStackTrace(), e);
								isHeaterConnected = false;
							}
						}
					} 
			    	catch (Exception e) {
						e.printStackTrace();
						isHeaterConnected = false;
					}
			    	
			    	if (isHeaterConnected && heaterDataString == null) {
			    		isHeaterConnected = false;
			    		logger.logDebug("was connected and meterDataString == null");
			    	}
		    	}
		    	
		    	
	    	}
	    	
	    	lastPull = timer.getUnixTime();
	    	
	    	try {
	    		while (lastPull >= timer.getUnixTime()) {
	    			Thread.sleep(10);
	    		}
			} 
	    	catch (InterruptedException e) {
				e.printStackTrace();
				logger.logDebug(e);
			}
	    }
		
	}
	
	
	private String openConnectionToMeter(String meterConnectURL) throws Exception {
		// open up connection to get cookie (PHPSESSID)a
		URLConnection mycon = new URL(meterConnectURL).openConnection();
	    mycon.getContent();
	    
	    String query = getOpenConnectionBody(meterNumber);
	    mycon = new URL(meterConnectURL).openConnection();
	    mycon.setDoOutput(true);
	    mycon.setRequestProperty("Accept-Language", "de-DE,de;q=0.8,de-DE;q=0.6,en;q=0.4");
	    mycon.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	    mycon.setRequestProperty("Accept-Charset", "utf-8");
	    mycon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
	    OutputStream output = null;
	    output = mycon.getOutputStream();
	    output.write(query.getBytes("utf-8"));
	    output.close();
	    mycon.getContent();
	    
	    // read the output from the server
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
	    		mycon.getInputStream()));
	    
	    StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}
		
		mycon.getInputStream().close();
		
		return stringBuilder.toString();
	}
	
	class MyAuthenticator extends Authenticator {

        public PasswordAuthentication getPasswordAuthentication () {
        	System.out.println(">> Authenticator Called for " + getRequestingScheme());
        	System.out.println(">> with URL " + getRequestingURL());
        	System.out.println(">> " + meterNumber + ":" + password);
//        	@SuppressWarnings("restriction")
//			String encPW = new sun.misc.BASE64Encoder().encode((password).getBytes());
            return new PasswordAuthentication ("" + meterNumber, password.toCharArray());
        }
    }
	
	private void openConnectionToHeaterReading(String url) throws Exception {
		
		/// 1 -> Server redirected too many  times (20)
//		URLConnection connection = new URL(url).openConnection();
//		connection.setRequestProperty("Authorization", "Basic " + (new sun.misc.BASE64Encoder().encode((meterNumber+":"+password).getBytes())));
////		connection.setRequestProperty("Authorization", "Basic " + meterNumber+":"+password);
//		connection.connect();
		
		/// 2 -> Server redirected too many  times (20)
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		
		/// 3
//		String encoded = (String) Base64.encode("" + meterNumber + ":" + "pw");
//		connection.setRequestProperty("Authorization", "Basic "+encoded);
		
		/// 4
//		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//		connection.setRequestMethod("GET");
//		connection.connect();
		
		/// 5
//		String userPassword = username + ":" + password;
//		String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
//		URLConnection uc = url.openConnection();
//		uc.setRequestProperty("Authorization", "Basic " + encoding);
//		uc.connect();
		
		try {
			connection.getContent();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
//		connection = (HttpURLConnection) new URL(url).openConnection();
		try {
//			connection.getContent();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("#########");
	}
	
	private String readMeterData(String meterMeteringURL) throws Exception {
		URLConnection mycon = new URL(meterMeteringURL).openConnection();
	    mycon.setDoOutput(true);
	    
//	    String query = getBody(meterNumber);
	    mycon.setRequestProperty("Accept-Language", "de-DE,de;q=0.8,de-DE;q=0.6,en;q=0.4");
	    mycon.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	    mycon.setRequestProperty("Accept-Charset", "utf-8");
	    mycon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
	    
	    OutputStream output = mycon.getOutputStream();
//	    output.write(query.getBytes("utf-8"));
	    output.close();
	    mycon.getContent();
	    
	    // read the output from the server
	    BufferedReader reader = new BufferedReader(
	    		new InputStreamReader(
	    				mycon.getInputStream()));
	    
	    StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}
		
		mycon.getInputStream().close();
		
		return stringBuilder.toString();
	}
	
	private String readHeaterData(String meterHeaterURL) throws Exception {
		URLConnection mycon = new URL(meterHeaterURL).openConnection();
		((HttpURLConnection) mycon).setRequestMethod("GET");
//	    mycon.setDoOutput(true);
	    
	    mycon.setRequestProperty("Accept-Language", "de-DE,de,ro;q=0.8,de-DE;q=0.6,en;q=0.4");
	    mycon.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	    mycon.setRequestProperty("Accept-Charset", "utf-8");
	    mycon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
	    
//	    OutputStream output = mycon.getOutputStream();
//	    output.close();
	    mycon.getContent();
	    
	    // read the output from the server
	    BufferedReader reader = new BufferedReader(
	    		new InputStreamReader(
	    				mycon.getInputStream()));
	    
	    StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}
		
		mycon.getInputStream().close();
		
		return stringBuilder.toString();
	}
	
	
	private String getOpenConnectionURLString() {
		String myUrl = this.meterURL + this.logInPage;
		return myUrl;
	}
	
	private String getOpenConnectionBody(int meterNumber) {
		String body = "login=" + meterNumber
				+ "&password=" + password
				+ "&language=de_DE"
				+ "&submit=Anmelden"
				+ "&datetime=" + getCurrentDateAsString();
		return body;
	}
	
	private String getReadMeterDataURLString() {
		String returnValue = this.meterURL + this.meteringPage;
		return returnValue;
	}
	
	private String getReadHeaterDataURLString() {
		String returnValue = this.meterURL + this.heaterPagePart1 + this.heaterUUID + this.heaterPagePart2;
		return returnValue;
	}
	
	private String getCurrentDateAsString() {
		Calendar c = new GregorianCalendar();
		String date = "" + c.get(Calendar.YEAR) 
				+ ( (c.get(Calendar.MONTH) + 1) < 10 ? "0" + (c.get(Calendar.MONTH) + 1) : (c.get(Calendar.MONTH) + 1) )
				+ ( c.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + c.get(Calendar.DAY_OF_MONTH) : c.get(Calendar.DAY_OF_MONTH))
				+ ( c.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + c.get(Calendar.HOUR_OF_DAY) : c.get(Calendar.HOUR_OF_DAY) )
				+ ( c.get(Calendar.MINUTE) < 10 ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE) )
				+ ( c.get(Calendar.SECOND) < 10 ? "0" + c.get(Calendar.SECOND) : c.get(Calendar.SECOND) );
		return date;
	}

	public void shutdown() {
		 this.isRunning = false;
	}

	
}
