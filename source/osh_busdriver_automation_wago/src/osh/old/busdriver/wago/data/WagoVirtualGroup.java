package osh.old.busdriver.wago.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * wago xml interface
 * 
 * @author Kaibin Bao
 *
 */
@XmlType
public class WagoVirtualGroup {
	@XmlPath("@id")
	private int groupId;

	@XmlPath("output")
	private List<WagoVirtualSwitch> vswitches;

	public int getGroupId() {
		return groupId;
	}

	public List<WagoVirtualSwitch> getVswitches() {
		return vswitches;
	}

	public byte getByte() {
		Map<Integer, WagoVirtualSwitch> map = new HashMap<>();
		for (WagoVirtualSwitch vs : vswitches) map.put(vs.getId()%10, vs);
		byte ret = 0;
		for (int i = 7; i >= 0; i--) {
			ret <<= 1;
			if (map.get(i).getState()) ret |= 1;
		}
		
		return ret;
	}
}
