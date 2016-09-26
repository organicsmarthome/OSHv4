package osh.core.logging;

public interface IGlobalLogger {

	public abstract boolean isSystemLoggingActive();

	public abstract void setSystemLoggingActive(boolean systemLogingActive);

	/**
	 * create a single file for logging and override it every restart
	 * disable this variable and a logfile with timestamp will be created every restart
	 * @return the createSingleLogfile
	 */
	public abstract boolean isCreateSingleLogfile();

	/**
	 * log system messages in the logfile
	 * @return the logSystemMessages
	 */
	public abstract boolean isLogSystemMessages();

	/**
	 * report the caller class of the log-message while logging 
	 * @return the messageCallerTrace
	 */
	public abstract boolean isMessageCallerTrace();

	/**
	 * print system messages on the console
	 * @return the systemMessagesEnable
	 */
	public abstract boolean isSystemMessagesEnable();

	/**
	 * create a single file for logging and override it every restart
	 * disable this variable and a logfile with timestamp will be created every restart
	 * @param createSingleLogfile the createSingleLogfile to set
	 */
	public abstract void setCreateSingleLogfile(boolean createSingleLogfile);

	public abstract String getLogLevel();

	public abstract void setLogLevel(String logLevel);

	/**
	 * print system messages on the console
	 * @param consoleSystemMessagesEnabled the systemMessagesEnable to set
	 */
	public abstract void setConsoleSystemMessagesEnabled(
			boolean consoleSystemMessagesEnabled);

	/**
	 * log system messages in the logfile
	 * @param logSystemMessagesEnabled the logSystemMessages to set
	 */
	public abstract void setLogSystemMessagesEnabled(
			boolean logSystemMessagesEnabled);

	/**
	 *  report the caller class of the log-message while logging 
	 * @param messageCallerTrace the messageCallerTrace to set
	 */
	public abstract void setMessageCallerTrace(boolean messageCallerTrace);

	public abstract void logError(Object message);

	public abstract void logError(Object message, Throwable throwable);

	public abstract void logWarning(Object message);

	public abstract void logWarning(Object message, Throwable throwable);

	public abstract void logInfo(Object message);

	public abstract void logInfo(Object message, Throwable throwable);

	public abstract void logDebug(Object message);

	public abstract void logDebug(Object message, Throwable throwable);

	/**
	 * System messages can be printed at the console to show the current state of the contollerbox
	 * @param message
	 */
	public abstract void printSystemMessage(Object message);

	/**
	 * System messages which will be printed out but never logged. Messages are only printed out in DEBUG level!
	 * @param message
	 */
	public abstract void printDebugMessage(Object message);

}