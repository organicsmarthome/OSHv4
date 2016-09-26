package constructsimulation.datatypes;

import java.util.UUID;

/**
 * 
 * @author Ingo Mauser, Till Schuberth
 *
 */
public class OSHConfigurationWrapper {
	
	public int numberOfPersons;
	
	public long mainRandomSeed;
	
	public String logPath;
	
	public UUID meterUUID;
	public UUID hhUUID;
	
	public OSHConfigurationWrapper(
			int numberOfPersons,
			
			long mainRandomSeed,
			
			String logPath,
			
			UUID meterUUID,
			UUID hhUUID
			) { 
		
		this.numberOfPersons = numberOfPersons;
		
		this.mainRandomSeed = mainRandomSeed;

		this.logPath = logPath;
		
		this.meterUUID = meterUUID;
		this.hhUUID = hhUUID;
	}
}
