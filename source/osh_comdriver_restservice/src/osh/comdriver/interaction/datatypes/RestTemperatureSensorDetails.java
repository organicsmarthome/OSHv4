package osh.comdriver.interaction.datatypes;

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
@XmlType(name="temperatureSensorDetails")
public class RestTemperatureSensorDetails extends RestStateDetail {

	protected double temperature;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestTemperatureSensorDetails() {
		this(null, 0);
	}
	
	public RestTemperatureSensorDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	
	
}
