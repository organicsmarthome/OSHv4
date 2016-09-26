package osh.old.busdriver.wago.data;

import javax.xml.bind.annotation.XmlType;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * wago xml interface
 * 
 * @author Kaibin Bao
 *
 */
@XmlType
public class WagoRelayData {
	@XmlPath("@id")
	private int id;
	
	@XmlPath("output/@state")
	private boolean state;

	public int getId() {
		return id;
	}

	public boolean getState() {
		return state;
	}	
}
