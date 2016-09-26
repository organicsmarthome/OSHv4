package osh.old.busdriver.wago;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;
import osh.old.busdriver.wago.TCPUDPConnectionHandler.ConnectionStatusListener;
import osh.old.busdriver.wago.TCPUDPConnectionHandler.DocumentParsedListener;
import osh.old.busdriver.wago.data.WagoDeviceList;
import osh.old.busdriver.wago.data.WagoDiData;
import osh.old.busdriver.wago.data.WagoDiGroup;
import osh.old.busdriver.wago.data.WagoDoData;
import osh.old.busdriver.wago.data.WagoDoGroup;
import osh.old.busdriver.wago.data.WagoMeterGroup;
import osh.old.busdriver.wago.data.WagoPowerMeter;
import osh.old.busdriver.wago.data.WagoRelayData;
import osh.old.busdriver.wago.data.WagoVirtualGroup;
import osh.old.busdriver.wago.data.WagoVirtualSwitch;
import osh.old.busdriver.wago.parser.SwitchCommand;


/**
 * This class organizes all necessary steps to receive data from a Wago controller.
 * @author Kaibin Bao, Till Schuberth, Florian Allerding, Ingo Mauser
 */
@Deprecated
public class WagoTCPUDPDispatcher {
	private TCPUDPConnectionHandler connhandler = null;
	
	public HashMap<Integer, WagoRelayData> switchData = new HashMap<>();
	public HashMap<Integer, WagoPowerMeter> powerData = new HashMap<>();
	public HashMap<Integer, WagoVirtualSwitch> vsdata = new HashMap<>();
	public HashMap<Integer, WagoVirtualGroup> vsgdata = new HashMap<>();
	public HashMap<Integer, WagoDiData> didata = new HashMap<>();
	public HashMap<Integer, WagoDiGroup> digdata = new HashMap<>();
	public HashMap<Integer, WagoDoData> dodata = new HashMap<>();
	public HashMap<Integer, WagoDoGroup> dogdata = new HashMap<>();
	
	public boolean connected = false;
	
	private Set<UpdateListener> updatelisteners = new HashSet<UpdateListener>();
		
	public interface UpdateListener {
		public void wagoUpdateEvent();
	}
	
	public WagoTCPUDPDispatcher(IGlobalLogger logger, InetAddress address) throws OSHException {
		try {
			connhandler = new TCPUDPConnectionHandler(logger, address);
			connhandler.setConnectionListener(new ConnectionStatusListener() {
				@Override
				public void connectionEvent(boolean isConnected) {
					connected = isConnected;
					
					notifyUpdateListeners();
				}
			});
			connhandler.setListener(new DocumentParsedListener() {
				@Override
				public void documentParsedEvent(WagoDeviceList devicelist) {
					// convert power data
					if( devicelist.getInputs() != null ) {
						for( WagoMeterGroup meterGroup : devicelist.getInputs() ) {
							int gid = meterGroup.getGroupId();
							for( WagoPowerMeter meter : meterGroup.getMeters() ) {
								meter.setGroupId(gid);
								int uid = gid * 10 + meter.getMeterId();
								powerData.put(uid, meter);
							}
						}
					}

					// convert switch data
					if( devicelist.getRelays() != null ) {
						for( WagoRelayData relay : devicelist.getRelays() ) {
							int uid = relay.getId();
							switchData.put(uid, relay);
						}
					}

					// convert vs data
					if( devicelist.getVsGroups() != null ) {
						for( WagoVirtualGroup vsGroup : devicelist.getVsGroups() ) {
							int gid = vsGroup.getGroupId();
							for( WagoVirtualSwitch vs : vsGroup.getVswitches() ) {
								vs.setGroupId(gid);
								int uid = gid * 10 + vs.getId();
								vsdata.put(uid, vs);
							}
							vsgdata.put(gid, vsGroup);
						}
					}
					
					// convert digital in data
					if ( devicelist.getDi8Groups() != null ) {
						for( WagoDiGroup diGroup : devicelist.getDi8Groups() ) {
							int gid = diGroup.getGroupId();
							for( WagoDiData di : diGroup.getDigitalIns() ) {
								di.setGroupId(gid);
								int uid = gid * 10 + di.getId();
								didata.put(uid, di);
							}
							digdata.put(gid, diGroup);
						}
					}

					// convert digital out data
					if ( devicelist.getDo8Groups() != null ) {
						for( WagoDoGroup doGroup : devicelist.getDo8Groups() ) {
							int gid = doGroup.getGroupId();
							for( WagoDoData do8 : doGroup.getDigitalOuts() ) {
								do8.setGroupId(gid);
								int uid = gid * 10 + do8.getId();
								dodata.put(uid, do8);
							}
							dogdata.put(gid, doGroup);
						}
					}

					notifyUpdateListeners();
				}
			});
		} catch (SmartPlugException e) {
			throw new OSHException(e);
		}
	}
	
	public boolean isConnected() {
		return connected;
	}

	public Collection<WagoPowerMeter> getPowerData () {
		synchronized (powerData) {
			return powerData.values();
		}
	}
	
	public Collection<WagoRelayData> getSwitchData() {
		synchronized (switchData) {
			return switchData.values();
		}
	}
	
	public Collection<WagoDiData> getDigitalInData() {
		synchronized (didata) {
			return didata.values();
		}
	}
	
	public Collection<WagoDiGroup> getDigitalInGroup() {
		synchronized (digdata) {
			return digdata.values();
		}
	}

	public Collection<WagoDoData> getDigitalOutData() {
		synchronized (dodata) {
			return dodata.values();
		}
	}
	
	public Collection<WagoDoGroup> getDigitalOutGroup() {
		synchronized (dogdata) {
			return dogdata.values();
		}
	}

	public Collection<WagoVirtualSwitch> getVirtualSwitchData() {
		synchronized (vsdata) {
			return vsdata.values();
		}
	}

	public Collection<WagoVirtualGroup> getVirtualSwitchGroupData() {
		synchronized (vsdata) {
			return vsgdata.values();
		}
	}

	public void setSwitch(int id, boolean state) throws SmartPlugException {
		SwitchCommand.Command cmd;
		if (state) cmd = SwitchCommand.Command.CMD_ON; else cmd = SwitchCommand.Command.CMD_OFF;
		
		new SwitchCommand("relay", id, cmd, connhandler).sendCommand();
	}
	
	public void setVirtualSwitch(int moduleid, int portid, boolean state) throws SmartPlugException {
		SwitchCommand.Command cmd;
		if (state) cmd = SwitchCommand.Command.CMD_ON; else cmd = SwitchCommand.Command.CMD_OFF;
		
		new SwitchCommand("vs", moduleid, portid, cmd, connhandler).sendCommand();
	}

	public void setDigitalOutput(int moduleid, int portid, boolean state) throws SmartPlugException {
		SwitchCommand.Command cmd;
		if (state) cmd = SwitchCommand.Command.CMD_ON; else cmd = SwitchCommand.Command.CMD_OFF;
		
		new SwitchCommand("do8", moduleid, portid, cmd, connhandler).sendCommand();
	}

	public void registerUpdateListener(UpdateListener l) {
		updatelisteners.add(l);
	}

	public void unregisterUpdateListener(UpdateListener l) {
		updatelisteners.remove(l);
	}
	
	protected void notifyUpdateListeners() {
		for (UpdateListener l : updatelisteners) {
			l.wagoUpdateEvent();
		}
	}

}