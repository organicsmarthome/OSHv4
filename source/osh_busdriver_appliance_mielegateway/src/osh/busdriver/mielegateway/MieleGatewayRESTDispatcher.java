package osh.busdriver.mielegateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import osh.busdriver.mielegateway.data.MieleApplianceRawDataREST;
import osh.busdriver.mielegateway.data.MieleDeviceHomeBusDataREST;
import osh.busdriver.mielegateway.data.MieleDeviceList;
import osh.core.logging.IGlobalLogger;

/**
 * Handling the connection to one miele gateway
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
@SuppressWarnings("deprecation")
public class MieleGatewayRESTDispatcher implements Runnable {
	
	private IGlobalLogger logger;
	
	private HttpClient httpclient;
	private HttpContext httpcontext;
	final private CredentialsProvider httpCredsProvider;
	String homebusUrl;

	// (Miele) UID (sic!) -> device state map
	private HashMap<Integer, MieleDeviceHomeBusDataREST> deviceData;
	
	
	/**
	 * CONSTRUCTOR
	 * @param logger
	 * @param address
	 * @throws MalformedURLException
	 */
	public MieleGatewayRESTDispatcher(
			String gatewayHostAndPort, 
			String username, 
			String password, 
			IGlobalLogger logger) {
		super();
		
		this.homebusUrl = "http://" + gatewayHostAndPort + "/homebus/?language=en";
		
		this.httpCredsProvider = new BasicCredentialsProvider();
		this.httpCredsProvider.setCredentials(
			    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), 
			    new UsernamePasswordCredentials(username, password));
		this.httpcontext = new BasicHttpContext();
		this.httpcontext.setAttribute(ClientContext.CREDS_PROVIDER, httpCredsProvider);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
		        new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
		
		this.httpclient = new DefaultHttpClient(cm);
		this.httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1); // Default to HTTP 1.1 (connection persistence)
		this.httpclient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		this.httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 1000);
		this.httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000);
		
		this.logger = logger;
		
		//this.parser = new MieleGatewayParser("http://" + device + "/homebus");
		this.deviceData = new HashMap<Integer, MieleDeviceHomeBusDataREST>();
		
		new Thread(this, "MieleGatewayDispatcher for " + gatewayHostAndPort).start();
	}
	
	/**
	 * Collect information about Miele device and provide it 
	 * (to MieleGatewayDriver)
	 * 
	 * @param id Miele UID (sic!)
	 * @return
	 */
	public MieleDeviceHomeBusDataREST getDeviceData(Integer id) {
		MieleDeviceHomeBusDataREST dev;
		synchronized (this) {
			dev = deviceData.get(id);
		}
		return dev;
	}
	
	/**
	 * Collect all information about Miele devices and provide it 
	 * (to MieleGatewayDriver)
	 * 
	 * @return
	 */
	public Collection<MieleDeviceHomeBusDataREST> getDeviceData() {
		ArrayList<MieleDeviceHomeBusDataREST> devices = new ArrayList<MieleDeviceHomeBusDataREST>();
		synchronized (this) {
			devices.addAll(deviceData.values());
		}
		return devices;
	}
	
	public void sendCommand(String url) {
		HttpGet httpget = new HttpGet(url);
		try {
			HttpResponse response = httpclient.execute(httpget, httpcontext);				
			if( response.getStatusLine().getStatusCode() != 200 ) {
				logger.logWarning("miele@home bus driver: error sending command " + url);
			}
			EntityUtils.consume(response.getEntity());
		} catch (IOException e1) {
			httpget.abort();
			logger.logWarning("miele@home bus driver: error sending command " + url, e1);
		}
	}
	
	@Override
	public void run() {
		MieleDeviceList deviceList = new MieleDeviceList();
		JAXBContext context;

		// initialize empty device list
		deviceList.setDevices(Collections.<MieleDeviceHomeBusDataREST>emptyList());
		
		try {
			context = JAXBContext.newInstance(MieleDeviceList.class);
		} catch (JAXBException e1) {
			logger.logError("unable to initialize XML marshaller", e1);
			return;
		}
		
		while (true) {
			// fetch device list
			try {
				HttpGet httpget = new HttpGet(homebusUrl);
				HttpResponse response = httpclient.execute(httpget, httpcontext);
				HttpEntity entity = response.getEntity();
				
				if (entity != null) {
				    InputStream instream = entity.getContent();
				    
				    //DEBUG
//				    String s = convertStreamToString(instream);
//				    System.out.println(s);
				    
					// Process the XML
					try {
						 // Create the EclipseLink JAXB (MOXy) Unmarshaller
			            Map<String, Object> jaxbProperties = new HashMap<String, Object>(2);
//			            jaxbProperties.put(JAXBContextProperties.MEDIA_TYPE, "application/xml");
//			            jaxbProperties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
			            JAXBContext jc = JAXBContext.newInstance(new Class[] {MieleDeviceList.class}, 
			                jaxbProperties);
			            Unmarshaller unmarshaller = jc.createUnmarshaller();
			            
//						Unmarshaller unmarshaller = context.createUnmarshaller();
						deviceList = (MieleDeviceList) unmarshaller.unmarshal(instream);
						
						//DEBUG
//						StringReader reader = new StringReader("s");
//						deviceList = (MieleDeviceList) unmarshaller.unmarshal(reader);
						//DEBUG END
						
						if (logger == null) {
							System.out.println(deviceList);
						}
					} catch (JAXBException e) {
						if (logger != null) {
							logger.logError("failed to unmarshall miele homebus xml", e);
						}
						else {
							e.printStackTrace();
						}
						deviceList.setDevices(Collections.<MieleDeviceHomeBusDataREST>emptyList()); // set empty list
				    } finally {
				        instream.close();
				        if ( deviceList == null || deviceList.getDevices() == null || deviceList.getDevices().size() == 0 ) {
				        	deviceList = new MieleDeviceList();
				        	if (logger != null) {
								logger.logWarning("no miele devices in list");
							}
							else {
								System.out.println("no miele devices in list");
							}
				        }
				        if ( deviceList.getDevices() == null ) {
				        	deviceList.setDevices(Collections.<MieleDeviceHomeBusDataREST>emptyList()); // set empty list
				        }
				    }
				}
			} catch (IOException e1) {
				deviceList.setDevices(Collections.<MieleDeviceHomeBusDataREST>emptyList()); // set empty list
				if (logger != null) {
					logger.logWarning("miele@home bus driver: failed to fetch device list; " + e1.getMessage());
					logger.logInfo("miele@home bus driver: failed to fetch device list", e1);
				}
				else {
					e1.printStackTrace();
				}
			}
			
			// fetch device details
			for ( MieleDeviceHomeBusDataREST dev : deviceList.getDevices() ) {
				try {
					HttpGet httpget = new HttpGet(dev.getDetailsUrl());
					HttpResponse response = httpclient.execute(httpget, httpcontext);
					HttpEntity entity = response.getEntity();
					if (entity != null) {
					    InputStream instream = entity.getContent();
					    
						// Process the XML
						try {
							Unmarshaller unmarshaller = context.createUnmarshaller();
							MieleApplianceRawDataREST deviceDetails = (MieleApplianceRawDataREST) unmarshaller.unmarshal(instream);
							dev.setDeviceDetails(deviceDetails);
						} catch (JAXBException e) {
							logger.logError("failed to unmarshall miele homebus detail xml", e);
					    } finally {
					        instream.close();
					    }
					}
				} catch (IOException e2) {
					// ignore
				}
			}
			
			// store device state
			synchronized (this) {
				deviceData.clear();
				for ( MieleDeviceHomeBusDataREST dev : deviceList.getDevices() ) {
					deviceData.put(dev.getUid(), dev);
				}
				this.notifyAll();
			}
			
			// wait a second till next state fetch
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e3) {
				logger.logError("sleep interrupted - miele@home bus driver dies right now...");
				break;
			}
		}
	}
	
	public String convertStreamToString(InputStream is) throws IOException {
        // To convert the InputStream to String we use the
        // Reader.read(char[] buffer) method. We iterate until the
        // Reader return -1 which means there's no more data to
        // read. We use the StringWriter class to produce the string.
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        }
        return "";
    }
}
