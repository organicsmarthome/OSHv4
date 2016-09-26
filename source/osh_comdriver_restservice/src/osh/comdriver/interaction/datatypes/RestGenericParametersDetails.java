package osh.comdriver.interaction.datatypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType(name="genericParametersDetails")
public class RestGenericParametersDetails extends RestStateDetail {
	protected Map<String,String> map;

	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestGenericParametersDetails() {
		this(null, 0);
	}
	
	public RestGenericParametersDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
		
		map = new HashMap<>();
	}

	public void setParameter( String key, String value ) {
		map.put(key, value);
	}

	public Map<String, String> getMap() {
		return map;
	}
	
	public void setMap(Map<String, String> map) {
		this.map = map;
	}
}
