package osh.busdriver.mielegateway.data;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The XML homebus root node
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
public class MieleDeviceMap {
	@JsonProperty
	private Map<Integer,MieleDeviceHomeBusDataJSON> devices;
	
	public Map<Integer,MieleDeviceHomeBusDataJSON> getDevices() {
		return devices;
	}
	
	public void setDevices(Map<Integer,MieleDeviceHomeBusDataJSON> devices) {
		this.devices = devices;
	}
}
