package osh.en50523;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * DIN EN 50523 Device State as BYTE (unsigned)<br>
 * enhanced with proprietary state: UNKNOWN (value 99) and <br>
 * value from Energy@home spec: ENABLED_REMOTE_AND_ENERGY_CONTROL (value 1)
 * @author Kaibin Bao, Ingo Mauser
 *
 */
@XmlType
public enum EN50523DeviceStateRemoteControl {
	
	@XmlEnumValue("99")
	UNKNOWN 							(99, "Remote control unknown"),
	
	@XmlEnumValue("0")
	DISABLED 							(0, "Remote control disbled"),
	
	@XmlEnumValue("1")
	ENABLED_REMOTE_AND_ENERGY_CONTROL	(1, "Remote control disbled"), // Energy@home spec
	
	@XmlEnumValue("7")
	TEMPORARILY_DISABLED				(7, "Remote control temporarily disabled"),
	
	@XmlEnumValue("15")
	ENABLED_REMOTE_CONTROL 				(15, "Remote control enabled");
	
	
	private int value;
	private String description;
	
	
	/**
	 * CONSTRUCTOR
	 * @param valueString
	 * @param value
	 * @param description
	 */
	private EN50523DeviceStateRemoteControl(int value, String description) {
		this.value = value;
		this.description = description;
	}

	
	public static EN50523DeviceStateRemoteControl fromInt(int v) {
        for (EN50523DeviceStateRemoteControl c: EN50523DeviceStateRemoteControl.values()) {
            if (c.getValue() == v) {
                return c;
            }
        }
        throw new IllegalArgumentException(v + "");
    }
	
	public int getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}
    
}
