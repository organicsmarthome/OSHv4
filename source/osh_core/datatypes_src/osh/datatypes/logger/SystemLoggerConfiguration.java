package osh.datatypes.logger;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class SystemLoggerConfiguration {
	
	// system logger for system messages etc.
	
	private String globalLoggerLogLevel;
	
	private boolean systemLoggingToConsoleActive;
	
	private boolean systemLoggingToFileActive;
	private boolean createSingleLogfile = false;
	
	private boolean systemLoggingActive = true;
	private boolean showMessageCallerTrace = true;

	private String logDirName;
	
	public SystemLoggerConfiguration(
			String globalLoggerLogLevel,
			boolean systemLoggingToConsoleActive,
			boolean systemLoggingToFileActive, 
			boolean createSingleLogfile,
			boolean systemLoggingActive,
			boolean showMessageCallerTrace,
			String logDirName) {
		super();
		
		this.globalLoggerLogLevel = globalLoggerLogLevel;
		
		this.systemLoggingToConsoleActive = systemLoggingToConsoleActive;
		this.systemLoggingToFileActive = systemLoggingToFileActive;
		
		this.createSingleLogfile = createSingleLogfile;
		
		this.systemLoggingActive = systemLoggingActive;
		this.showMessageCallerTrace = showMessageCallerTrace;
		
		this.logDirName = logDirName;
	}

	
	
	public String getGlobalLoggerLogLevel() {
		return globalLoggerLogLevel;
	}

	public boolean isSystemLoggingToConsoleActive() {
		return systemLoggingToConsoleActive;
	}

	public boolean isSystemLoggingToFileActive() {
		return systemLoggingToFileActive;
	}

	public boolean isCreateSingleLogfile() {
		return createSingleLogfile;
	}

	public boolean isShowMessageCallerTrace() {
		return showMessageCallerTrace;
	}

	public boolean isSystemLoggingActive() {
		return systemLoggingActive;
	}

	public String getLogDirName() {
		return logDirName;
	}

}
