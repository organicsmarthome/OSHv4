package osh.comdriver.interaction.datatypes;

import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType
public class RestConfigurationDetails extends RestStateDetail {

	@XmlType
	public enum ConfigurationStatus {
		@XmlEnumValue("UNCONFIGURED")
		UNCONFIGURED,
		@XmlEnumValue("CONFIGURED")
		CONFIGURED,
		@XmlEnumValue("USED")
		USED,
		@XmlEnumValue("ERROR")
		ERROR
	}
	
	@Enumerated(value=EnumType.STRING) 
	protected ConfigurationStatus configurationStatus;
	
	protected UUID usedBy;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestConfigurationDetails() {
		this(null, 0);
	}

	public RestConfigurationDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	public ConfigurationStatus getConfigurationStatus() {
		return configurationStatus;
	}

	public void setConfigurationStatus(ConfigurationStatus configurationStatus) {
		this.configurationStatus = configurationStatus;
	}

	public UUID getUsedBy() {
		return usedBy;
	}

	public void setUsedBy(UUID usedBy) {
		this.usedBy = usedBy;
	}
}
