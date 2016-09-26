package osh.busdriver.mielegateway.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * The XML homebus root node
 * 
 * @author Kaibin Bao
 *
 */
@XmlRootElement(name="DEVICES")
public class MieleDeviceList {
	@XmlPath("device")
	private List<MieleDeviceHomeBusDataREST> devices;
	
	public List<MieleDeviceHomeBusDataREST> getDevices() {
		return devices;
	}
	
	public void setDevices(List<MieleDeviceHomeBusDataREST> devices) {
		this.devices = devices;
	}
}
