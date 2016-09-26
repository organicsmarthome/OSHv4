package osh.old.busdriver.wago.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * wago xml interface
 * 
 * @author tisu
 *
 */
@XmlType
public class WagoDoGroup {
	@XmlPath("@id")
	private int groupId;

	@XmlPath("output")
	private List<WagoDoData> digitalouts;

	public int getGroupId() {
		return groupId;
	}

	public List<WagoDoData> getDigitalOuts() {
		return digitalouts;
	}

	public byte getByte() {
		Map<Integer, WagoDoData> map = new HashMap<>();
		for (WagoDoData do8 : digitalouts) map.put(do8.getId()%10, do8);
		byte ret = 0;
		for (int i = 7; i >= 0; i--) {
			ret <<= 1;
			if (map.get(i).getState()) ret |= 1;
		}
		
		return ret;
	}
}
