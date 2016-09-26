package osh.old.busdriver.wago;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import osh.core.logging.IGlobalLogger;
import osh.old.busdriver.wago.data.WagoDeviceList;


/**
 * This class handles a connection to the wago controller. It starts its own
 * thread in the contstructor named "TCPUDPConnectionHandler for xxxx". It opens
 * a pair of connections, one tcp connection to receive the current xml documents
 * from the controller, and one udp "connection" to send commands.
 * The xml documents are parsed by a separate parser, which must register itself
 * via <code>registerParser</code>. Commands are sent by instances of subclasses
 * of <code>CommandGenerator</code>.
 * 
 * @author Kaibin Bao, Till Schuberth
 *
 */
public class TCPUDPConnectionHandler implements Runnable {
	private InetAddress controllerAddr;
	
	// Port of Wago 750-860 Protocol is 9155
	private final int controllerPort = 9155;
	private Socket dataSocket;
	private DatagramSocket cmdSocket;
	
	private byte commandCounter = (byte)1;
	private Object commandCounterLock = new Object();
	
	private boolean shutdown = false;
	
	private DocumentParsedListener listener;
	private ConnectionStatusListener connectionListener;
	
	private IGlobalLogger logger;
	private Charset ascii = Charset.forName("ASCII");
	
	private int parseErrorcntr;
	private Date lastParseErrorcntrReset = new Date();
	private int reconnectWait;
	private Date lastIOException = new Date();

	/* *****************
	 * inner classes
	 */
	
	public interface DocumentParsedListener {
		public void documentParsedEvent(WagoDeviceList devicelist);
	}
	
	public interface ConnectionStatusListener {
		public void connectionEvent(boolean isConnected);
	}
	
	/**
	 * split tcp stream into xml documents (separated by a NUL-character)
	 * This input stream reads from the source until a NUL-character appears.
	 * The NUL-character will not be copied to the outgoing stream.
	 * 
	 * @author userx
	 *
	 */
	private static class TokenizingInputStream extends InputStream {

		private InputStream stream;
		private boolean hasMore = true, endOfToken = false;
		
		public TokenizingInputStream(InputStream stream) {
			this.stream = new BufferedInputStream(stream);
		}

		public boolean hasNext() {
			return hasMore;
		}
		
		public void next() {
			endOfToken = false;
		}
		
		@Override
		public int available() throws IOException {
			return stream.available();
		}
		
		@Override
		public void close() throws IOException {
			skip();
			
			if( !hasMore ) {
				stream.close();
			}
		}

		public void reallyClose() throws IOException {
			stream.close();
		}
		
		/**
		 * Skip one message
		 * @throws IOException
		 */
		public void skip() throws IOException {
			while (read() >= 0);
		}
		
		@Override
		public int read() throws IOException {
			if (endOfToken) return -1;
			
			int ret = stream.read();
			if (ret == 0) {
				endOfToken = true;
				return -1;
			} else
			if( ret == -1 ) {
				hasMore = false;
				return -1;
			}
			else {
				return ret;
			}
		}
	}
	
	/**
	 * abstract super class for a command sent to the wago controller (e. g. for
	 * a digital output module)
	 *
	 */
	public static abstract class CommandGenerator {
		protected void sendCommand(String device, String moduleid, String functionid, String command, TCPUDPConnectionHandler instance) throws SmartPlugException {
			instance.sendCommand(device + "-" + moduleid + "-" + functionid + "=" + command);
		}
		@Override
		public abstract boolean equals(Object o);
		public abstract void sendCommand() throws SmartPlugException;
		
		/**
		 * @return true if the target is the same as in o, but not necessarily
		 * the command
		 */
		public abstract boolean equalsTarget(Object o);
		
		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}
	
	
	/**
	 * CONSTRUCTOR
	 * @param logger
	 * @param address ip address of the wago controller
	 * @throws SmartPlugException
	 */
	public TCPUDPConnectionHandler(IGlobalLogger logger, InetAddress address) throws SmartPlugException {
		this.logger = logger;

		try {
			this.controllerAddr = address;
			cmdSocket = new DatagramSocket();
			cmdSocket.setSoTimeout(3000);
		} catch (SocketException e) {
			throw new SmartPlugException("could not initialize datagram socket", e);
		}

		// let the thread initialize the data socket
		/*
		try {
			initDataSocket();
		} catch (IOException e1) {
			logger.logError("IOException in wago connection handler; trying to continue...", e1);
		}
		*/
		
		try {
			sendUdpPacket("RE", (byte) 0);
		} catch (IOException e) {
			logger.logWarning("wago controller reset failed (host: " + controllerAddr.toString() + ")", e);
		}
		
		new Thread(this, "TCPUDPConnectionHandler for " + controllerAddr.toString()).start();
	}
	
	
	/* ###########
	 * # methods #
	 * ########### */
	
	private void sendUdpPacket(String data, byte session) throws IOException {
		sendUdpPacket(data.getBytes(ascii), session);
	}
	
	/**
	 * send out udp command packet
	 * @param data
	 * @param session session byte to be used for this command (1 command = 1 session)
	 * @throws IOException
	 */
	private synchronized void sendUdpPacket(byte[] data, byte session) throws IOException {
		if (cmdSocket == null) throw new IOException("socket still uninitialized");
		
		DatagramPacket _p = new DatagramPacket(data, data.length);
		_p.setAddress(controllerAddr);
		_p.setPort(controllerPort);
		DatagramPacket _r = new DatagramPacket(new byte[100], 100);
		_r.setAddress(controllerAddr);
		_r.setPort(controllerPort);

		for (int i = 0; i < 3; i++) {
			cmdSocket.send(_p);
			try {
				cmdSocket.receive(_r);
			} catch (SocketTimeoutException e) {
				continue;
			}
			
			byte[] ack = Arrays.copyOf("ACK".getBytes(ascii), 4);
			ack[3] = session;
			
			if (!Arrays.equals(Arrays.copyOf(_r.getData(), _r.getLength()), ack)) break; //pretty inefficient, but what the hell...
			
			return;
		}
		
		String resp = new String(_r.getData(), ascii);
		throw new IOException("No answer from controller. Answer: \"" + resp + "\"");
	}
	
	private byte getSessionId() {
		byte retval;
		
		synchronized (commandCounterLock) {
			retval = commandCounter;

			commandCounter++;
			if( commandCounter == 0 )
				commandCounter = 1;

		}
		
		return retval;
	}
	
	/**
	 * send command to wago controller ("COxblahblah")
	 */
	private void sendCommand(String cmd) throws SmartPlugException {
		byte[] command;

		//generate session byte for this command
		byte session = getSessionId();
		
		byte[] baCmd = cmd.getBytes(ascii);
		command = new byte[baCmd.length + 3];
		command[0] = 'C';
		command[1] = 'O';
		command[2] = session;
		for (int i = 0; i < baCmd.length; i++) command[i + 3] = baCmd[i];
		
		
		try {
			sendUdpPacket(command, session);
		} catch (IOException e) {
			throw new SmartPlugException("could not send smart plug command", e);
		}
	}
	
	private void initDataSocket() throws IOException {
		dataSocket = new Socket(controllerAddr, controllerPort);
		dataSocket.setSoTimeout(5000);
	}
	
	@Override
	public void run() {
		JAXBContext context;
		
		try {
			context = JAXBContext.newInstance(WagoDeviceList.class);
		} catch (JAXBException e1) {
			logger.logError("unable to initialize XML marshaller", e1);
			return;
		}
        
		TokenizingInputStream is = null;
		OutputStream os = null;
		
		while (!shutdown) {
			try {
				//check connection
				if( dataSocket == null ) {
					initDataSocket();
				}
				if( !dataSocket.isConnected() || dataSocket.isClosed()) {
					try {
						// closes also the input-/ outputstream
						dataSocket.close();
					} catch (IOException e) {}
					
					logger.logWarning("controller " + controllerAddr.toString() + " is not connected");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
					initDataSocket();
				}

				//is is a single xml document
				is = new TokenizingInputStream( dataSocket.getInputStream() );
				os = dataSocket.getOutputStream();

				if( connectionListener != null ) connectionListener.connectionEvent(true);
				
				//parse document with all registered parsers
				while( is.hasNext() && !shutdown) {
					while( is.available() == 0 ) {
						try {
							Thread.sleep(700);
						} catch (InterruptedException e) {} // ignore
						// send data request if enough time has passed
						os.write(42);
						os.flush();
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {} // ignore
					}

					// Process the XML
					try {
						Unmarshaller unmarshaller = context.createUnmarshaller();
						WagoDeviceList deviceList = (WagoDeviceList) unmarshaller.unmarshal(is);
						getListener().documentParsedEvent(deviceList);

						if( connectionListener != null ) connectionListener.connectionEvent(true);
					} catch (JAXBException e) {
						handleParseException(e);
						logger.logError("failed to unmarshall wago homebus xml", e);
						//deviceList.setDevices(Collections.<MieleDeviceHomeBusData>emptyList()); // set empty list
						
						if( connectionListener != null ) connectionListener.connectionEvent(false);
				    } finally {
						is.skip();
						is.next();
				    }
				}
			} catch (IOException e1) {
				if( e1 instanceof NoRouteToHostException ) {
					logger.logWarning("It's dead, Jim: No route to " + controllerAddr);
				} else {
					logger.logError("IOException in wago connection handler; trying to continue...", e1);
				}
				long diff = new Date().getTime() - lastIOException.getTime();
				if (diff < 0 || diff > 300000) {
					reconnectWait = 0;
				} else {
					if (reconnectWait <= 0) reconnectWait = 1;
					reconnectWait *= 2;
					if (reconnectWait > 180) reconnectWait = 180;
				}
				lastIOException = new Date();

				try { // just for safety
					if( is != null )
						is.reallyClose();
					if( os != null )
						os.close();
				} catch ( IOException e ) {};
			} catch (Exception e) {
				//prevent thread from dying
				logger.logError("fatal error, FIXME:", e);
			}

			if( connectionListener != null ) connectionListener.connectionEvent(false);
			
			try {
				Thread.sleep(reconnectWait * 1000);
			} catch (InterruptedException e) {}
		}
		
		try {
			dataSocket.close();
		} catch (IOException e) {}
	}
	
	private void handleParseException(Exception e) {
		long diff = new Date().getTime() - lastParseErrorcntrReset.getTime();
		if (diff < 0 || diff > 30000L) {
			parseErrorcntr = 0;
			lastParseErrorcntrReset = new Date();
		}
		parseErrorcntr++;
		
		//if there are more than three parse exceptions in 30 seconds, log them
		if (parseErrorcntr >= 3) {
			logger.logError("parse error; trying to continue...", e);
		}
	}
		
	/* ***************************
	 * simple methods
	 */
	
	public DocumentParsedListener getListener() {
		return listener;
	}

	public void setListener(DocumentParsedListener listener) {
		this.listener = listener;
	}
	
	public ConnectionStatusListener getConnectionListener() {
		return connectionListener;
	}
	
	public void setConnectionListener(
			ConnectionStatusListener connectionListener) {
		this.connectionListener = connectionListener;
	}

	public void shutdown() {
		shutdown = true;
	}
}
