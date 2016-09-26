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
public class WagoDiGroup {
	@XmlPath("@id")
	private int groupId;

	@XmlPath("input")
	private List<WagoDiData> digitalins;

	public int getGroupId() {
		return groupId;
	}

	public List<WagoDiData> getDigitalIns() {
		return digitalins;
	}

	public byte getByte() {
		Map<Integer, WagoDiData> map = new HashMap<>();
		for (WagoDiData di : digitalins) map.put(di.getId()%10, di);
		byte ret = 0;
		for (int i = 7; i >= 0; i--) {
			ret <<= 1;
			if (map.get(i).getState()) ret |= 1;
		}
		
		return ret;
	}
}
