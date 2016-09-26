/*
 * ============================================================================
 * GNU Lesser General Public License
 * ============================================================================
 *
 * Copyright (C) 2006-2009 Serotonin Software Technologies Inc. http://serotoninsoftware.com
 * @author Matthew Lohbihler
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 */
package osh.driver.bacnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.core.logging.IGlobalLogger;
import osh.driver.BacNetThermalDriver;
import osh.eal.hal.HALRealTimeDriver;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DefaultDeviceEventListener;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetTimeoutException;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;

/**
 * BacNet/IP thermal sensors and A/C control
 * @author Kaibin Bao
 *
 */
public class BacNetDispatcher implements IRealTimeSubscriber {
	
	private LocalDevice bacnetDevice = null;
	
	private IGlobalLogger logger;
	private HALRealTimeDriver timer;
	
	
	static public class BacNetObject {
		public int deviceId;
		public int objectId;
		
		public BacNetObject(int deviceId, int objectId) {
			super();
			this.deviceId = deviceId;
			this.objectId = objectId;
		}
		
		@Override
		public boolean equals(Object obj) {
	        if (this == obj)
	            return true;
	        if( obj == null )
				return false;
	        if (getClass() != obj.getClass())
	            return false;
	        
	        final BacNetObject other = (BacNetObject) obj;
	        
			return ( (this.deviceId == other.deviceId) &&
					 (this.objectId == other.objectId) );
		}
		
		@Override
		public int hashCode() {
			return this.objectId;
		}
		
		@Override
		public String toString() {
			return "[/" + deviceId + "/" + objectId + "]";
		}
	}
	
	private Map<BacNetObject, Double> analogInputStates;
	private Map<BacNetObject, Double> analogValueStates;
	private Set<String> devices;
	
	private final int REDISCOVER_INTERVAL = 60;
	private int rediscover_countdown = REDISCOVER_INTERVAL;
	private boolean STANDALONE = false;
	
	public BacNetDispatcher(HALRealTimeDriver timer, IGlobalLogger logger) {
		this.timer = timer;
		analogInputStates = new HashMap<BacNetDispatcher.BacNetObject, Double>();
		analogValueStates = new HashMap<BacNetDispatcher.BacNetObject, Double>();
		devices = new HashSet<String>();
	}
	
    private class MyDeviceEventListener extends DefaultDeviceEventListener {
        @Override
        public void iAmReceived(RemoteDevice d) {
            System.out.println("IAm received" + d);
        }
    }
	
    public Double getAnalogInputState( BacNetObject oid ) {
    	return analogInputStates.get(oid);
    }

    public Double getAnalogValueState( BacNetObject oid ) {
    	return analogValueStates.get(oid);
    }
    
    public void setAnalogValueState( BacNetObject oid, float value ) throws BACnetException {
        // Get extended information for all remote devices.
        for (RemoteDevice d : bacnetDevice.getRemoteDevices()) {
            //bacnetDevice.getExtendedDeviceInformation(d);

        	if( oid.deviceId == d.getInstanceNumber() ) {
        		bacnetDevice.setPresentValue(d,
        			new ObjectIdentifier(ObjectType.analogValue, oid.objectId),
        			new com.serotonin.bacnet4j.type.primitive.Real(value));
        	}
        }
    }
    
	public void init() throws IOException, OSHException {
        bacnetDevice = new LocalDevice(1984, "255.255.255.255");
        bacnetDevice.getEventHandler().addListener(new MyDeviceEventListener());
        bacnetDevice.initialize();
        if (!STANDALONE) timer.registerComponent(this, 1);
	}
	
	public void addDevice(String host, int port) throws OSHException {
		if( devices.add(host) ) {
			try {
				discover(host, port);
			} catch (UnknownHostException e) {
				devices.remove(host);
				throw new OSHException("can't connect to host " + host, e);
			} catch (BACnetException e) {
				devices.remove(host);
				throw new OSHException("can't connect to host " + host, e);
			}
		}
	}
	
	public void discover(String controllerHostname, int port) throws BACnetException, UnknownHostException {
        InetSocketAddress addr = 
        		new InetSocketAddress(
        				InetAddress.getByName(controllerHostname), port);
        bacnetDevice.sendUnconfirmed(addr, null, new WhoIsRequest());

        // Wait a bit for responses to come in.
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// nop.
		}
	}
	
	@SuppressWarnings("unchecked")
	public void update() throws BACnetException {
        // Get extended information for all remote devices.
        for (RemoteDevice d : bacnetDevice.getRemoteDevices()) {
            //bacnetDevice.getExtendedDeviceInformation(d);

        	// instanceNumber of the current RemoteDevice d
        	int deviceId = d.getInstanceNumber();
        	
        	
        	List<ObjectIdentifier> oids = null;
            try {
                oids = ((SequenceOf<ObjectIdentifier>) bacnetDevice.sendReadPropertyAllowNull(d, d
                        .getObjectIdentifier(), PropertyIdentifier.objectList)).getValues();
            }
            catch (BACnetTimeoutException te) {
            	if (logger != null) {
            		logger.logError("" + te.getMessage());
            	}
            	else {
            		System.out.println("" + te.getMessage());
            	}
            }
            
            if (oids != null) {
            	PropertyReferences refs = new PropertyReferences();
                for (ObjectIdentifier oid : oids) {
                	if( ObjectType.analogInput.equals(oid.getObjectType()) ) {
                		refs.add(oid, PropertyIdentifier.presentValue);
                	} else
                	if( ObjectType.analogValue.equals(oid.getObjectType()) ) {
                		refs.add(oid, PropertyIdentifier.presentValue);
                	}
                }
                
                PropertyValues pvs = bacnetDevice.readProperties(d, refs);
                for (ObjectPropertyReference opr : pvs) {
                	ObjectIdentifier oid = opr.getObjectIdentifier();
                	if( ObjectType.analogInput.equals(oid.getObjectType()) ) {
                		if( PropertyIdentifier.presentValue.equals(opr.getPropertyIdentifier()) ) {
    						Encodable value = pvs.getNoErrorCheck(opr);
    						analogInputStates.put(
    								new BacNetObject(deviceId, oid
    										.getInstanceNumber()), interpretDoubleValue(value));
                		}
                    }
                	if( ObjectType.analogValue.equals(oid.getObjectType()) ) {
                		if( PropertyIdentifier.presentValue.equals(opr.getPropertyIdentifier()) ) {
    						Encodable value = pvs.getNoErrorCheck(opr);
    						analogValueStates.put(
    								new BacNetObject(deviceId, oid
    										.getInstanceNumber()), interpretDoubleValue(value));
                		}
                    }
                }
            }
            
        }
	}
	
	private double interpretDoubleValue( Encodable value ) {
		if( value instanceof com.serotonin.bacnet4j.type.primitive.Double ) {
			return ((com.serotonin.bacnet4j.type.primitive.Double) value).doubleValue();
		}
		if( value instanceof com.serotonin.bacnet4j.type.primitive.Real ) {
			return ((com.serotonin.bacnet4j.type.primitive.Real) value).floatValue();
		}
		if( value instanceof com.serotonin.bacnet4j.type.primitive.SignedInteger ) {
			return ((com.serotonin.bacnet4j.type.primitive.SignedInteger) value).longValue();
		}
		if( value instanceof com.serotonin.bacnet4j.type.primitive.UnsignedInteger ) {
			return ((com.serotonin.bacnet4j.type.primitive.UnsignedInteger) value).longValue();
		}
		if( value instanceof com.serotonin.bacnet4j.type.primitive.Unsigned32 ) {
			return ((com.serotonin.bacnet4j.type.primitive.Unsigned32) value).intValue();
		}
		if( value instanceof com.serotonin.bacnet4j.type.primitive.Unsigned16 ) {
			return ((com.serotonin.bacnet4j.type.primitive.Unsigned16) value).intValue();
		}
		if( value instanceof com.serotonin.bacnet4j.type.primitive.Unsigned8 ) {
			return ((com.serotonin.bacnet4j.type.primitive.Unsigned8) value).intValue();
		}
		
		return Double.NaN;
	}
	
	public void close() {
		bacnetDevice.terminate();
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("AI { ");
		for( Entry<BacNetObject, Double> entry : analogInputStates.entrySet() ) {
			str.append( entry.getKey() + " = " + entry.getValue() );
		}
		str.append(" } \n");
		str.append("AV { ");
		for( Entry<BacNetObject, Double> entry : analogValueStates.entrySet() ) {
			str.append( entry.getKey() + " = " + entry.getValue() + "    " );
		}
		str.append(" } ");
		
		return str.toString();
	}
	
    @SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
    	
    	BacNetDispatcher drv = new BacNetDispatcher(null, null);
    	drv.STANDALONE = true;
    	
    	drv.init();
    	
        // Who is
    	drv.discover("192.168.1.255", 47808);
    	
    	// wie bekomme ich eine Referenz auf den BacNetThermalDriver?
    	BacNetThermalDriver driver = new BacNetThermalDriver(null, null, null);

        // Update a few times
    	for( int i = 0; i < 2; i++ ) {
    		drv.update();
    		System.out.println( drv.toString() );
    		Thread.sleep(1000);
    	}
    	
    	// Hannah
    	List<BacNetDispatcher.BacNetObject> actuatorObjects = driver.getActuatorObjects();
    	for (BacNetObject obj : actuatorObjects) {
    		drv.setAnalogValueState(obj, 21.0f);
    	}
    	
    	// drv.setAnalogValueState(new BacNetObject(3901, 2796223), 24.0f);
    	System.out.println("AnalogValueState is set.");
    	
    	// Update a few times
    	for( int i = 0; i < 10; i++ ) {
    		drv.update();
    		System.out.println( drv.toString() );
    		Thread.sleep(1000);
    	}
    	
        drv.close();
    }
    
	@Override
	public void onNextTimePeriod() throws OSHException {
		try {
			if( rediscover_countdown <= 0 ) {
				for( String dev : devices ) {
					try {
						discover(dev, 47808);
					} catch (UnknownHostException e) {
						throw new OSHException("unknown host: " + dev, e);
					}
				}
				rediscover_countdown = REDISCOVER_INTERVAL;
			}
			rediscover_countdown--;
			update();
		} catch (BACnetException e) {
			throw new OSHException("internal bacnet error", e);
		}
	}

	@Override
	public Object getSyncObject() {
		return this;
	}
}
