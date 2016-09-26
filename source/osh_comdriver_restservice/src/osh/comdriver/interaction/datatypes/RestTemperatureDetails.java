package osh.comdriver.interaction.datatypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType(name="temperatureDetails")
public class RestTemperatureDetails extends RestStateDetail {
	
	protected double temperature;
	
	protected Map<String,Double> auxiliaryTemperatures;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestTemperatureDetails() {
		this(null, 0);
	}
	
	public RestTemperatureDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}
	
	public double getTemperature() {
		return temperature;
	}
	
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public Map<String, Double> getAuxiliaryTemperatures() {
		return auxiliaryTemperatures;
	}
	
	public void setAuxiliaryTemperatures(
			Map<String, Double> auxiliaryTemperatures) {
		this.auxiliaryTemperatures = auxiliaryTemperatures;
	}
	
	public void setAuxiliaryTemperature( String name, double temperature ) {
		if( auxiliaryTemperatures == null ) {
			auxiliaryTemperatures = new HashMap<>();
		}
		auxiliaryTemperatures.put( name, temperature );
	}
}
