package osh.esc.grid;

import java.io.Serializable;
import java.util.UUID;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class EnergySourceSink extends EnergyDevice implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5283939620436042291L;
	private UUID deviceUuid;
	
	// additional information...?
	
	/**
	 * CONSTRUCTOR
	 * @param deviceUuid
	 */
	public EnergySourceSink(UUID deviceUuid) {
		this.deviceUuid = deviceUuid;
	}
	
	
	public UUID getDeviceUuid() {
		return deviceUuid;
	}
	
	@Override
	public String toString() {
		return deviceUuid.toString();
	}


	@Override
	public int hashCode() {
		return deviceUuid == null ? 0 : deviceUuid.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnergySourceSink other = (EnergySourceSink) obj;
		if (deviceUuid == null) {
			if (other.deviceUuid != null)
				return false;
		} else if (!deviceUuid.equals(other.deviceUuid))
			return false;
		return true;
	}
}
