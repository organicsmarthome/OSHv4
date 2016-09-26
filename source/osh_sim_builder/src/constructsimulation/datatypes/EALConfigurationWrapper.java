package constructsimulation.datatypes;

import osh.configuration.system.RunningType;

/**
 * 
 * @author Ingo Mauser, Till Schuberth
 *
 */
public class EALConfigurationWrapper {
	
	public RunningType runningType;
	public int numberOfPersons;
	public String h0Filename;	
	
	public boolean simLogger;
	
	public String loggerBusDriverClassName;
	public String loggerBusManagerClassName;
	
	public EALConfigurationWrapper(
			int numberOfPersons,
			String h0Filename,
			boolean simLogger, 
			String loggerBusDriverClassName, 
			String loggerBusManagerClassName
			) {
		
		this.numberOfPersons = numberOfPersons;
		this.h0Filename = h0Filename;
		
		this.simLogger = simLogger;
		this.loggerBusDriverClassName = loggerBusDriverClassName;
		this.loggerBusManagerClassName = loggerBusManagerClassName;
	}
}
