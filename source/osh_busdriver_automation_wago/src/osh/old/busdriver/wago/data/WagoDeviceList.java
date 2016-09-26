package osh.old.busdriver.wago.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * The wago xml root node
 * 
 * @author Kaibin Bao
 *
 */
@XmlRootElement(name="answer")
public class WagoDeviceList {
	@XmlPath("device[@type='uape']")
	private List<WagoMeterGroup> inputs;
	
	@XmlPath("device[@type='relay']")
	private List<WagoRelayData> relays;
	
	@XmlPath("device[@type='vs']")
	private List<WagoVirtualGroup> vsGroups;
	
	@XmlPath("device[@type='di8']")
	private List<WagoDiGroup> di8Groups;

	@XmlPath("device[@type='do8']")
	private List<WagoDoGroup> do8Groups;

	public List<WagoMeterGroup> getInputs() {
		return inputs;
	}

	public List<WagoRelayData> getRelays() {
		return relays;
	}

	public List<WagoVirtualGroup> getVsGroups() {
		return vsGroups;
	}
	
	public List<WagoDiGroup> getDi8Groups() {
		return di8Groups;
	}
	
	public List<WagoDoGroup> getDo8Groups() {
		return do8Groups;
	}
}
