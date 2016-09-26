package osh.old.busdriver.wago.data;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * wago xml interface
 * 
 * @author tisu
 *
 */
@XmlType
public class WagoDoData {
	@XmlTransient
	private int groupId;
	
	@XmlPath("@id")
	private int id;
	
	@XmlPath("@state")
	private boolean state;

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getId() {
		return id;
	}

	public boolean getState() {
		return state;
	}
}
