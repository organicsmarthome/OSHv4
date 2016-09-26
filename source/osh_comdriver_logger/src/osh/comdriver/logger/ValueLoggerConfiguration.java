package osh.comdriver.logger;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ValueLoggerConfiguration {
	
	// value logger for power values etc.
	//  console
	private Boolean valueLoggingToConsoleActive;
	private Integer valueLoggingToConsoleResolution;
	//  file
	private Boolean valueLoggingToFileActive;
	private Integer valueLoggingToFileResolution;
	//  database
	private Boolean valueLoggingToDatabaseActive;
	private Integer valueLoggingToDatabaseResolution;
	//  RRD database
	private Boolean valueLoggingToRrdDatabaseActive;
	private Integer valueLoggingToRrdDatabaseResolution;
	
	
	public ValueLoggerConfiguration(Boolean valueLoggingToConsoleActive,
			Integer valueLoggingToConsoleResolution,
			Boolean valueLoggingToFileActive,
			Integer valueLoggingToFileResolution,
			Boolean valueLoggingToDatabaseActive,
			Integer valueLoggingToDatabaseResolution,
			Boolean valueLoggingToRrdDatabaseActive,
			Integer valueLoggingToRrdDatabaseResolution) {
		super();
		
		this.valueLoggingToConsoleActive = valueLoggingToConsoleActive;
		this.valueLoggingToConsoleResolution = valueLoggingToConsoleResolution;
		
		this.valueLoggingToFileActive = valueLoggingToFileActive;
		this.valueLoggingToFileResolution = valueLoggingToFileResolution;
		
		this.valueLoggingToDatabaseActive = valueLoggingToDatabaseActive;
		this.valueLoggingToDatabaseResolution = valueLoggingToDatabaseResolution;
		
		this.valueLoggingToRrdDatabaseActive = valueLoggingToRrdDatabaseActive;
		this.valueLoggingToRrdDatabaseResolution = valueLoggingToRrdDatabaseResolution;
	}
	
	
	
	public Boolean getIsValueLoggingToConsoleActive() {
		return valueLoggingToConsoleActive;
	}
	
	public void setIsValueLoggingToConsoleActive(Boolean isValueLoggingToConsoleActive) {
		this.valueLoggingToConsoleActive = isValueLoggingToConsoleActive;
	}
	
	public Boolean getIsValueLoggingToFileActive() {
		return valueLoggingToFileActive;
	}
	
	public void setIsValueLoggingToFileActive(Boolean isValueLoggingToFileActive) {
		this.valueLoggingToFileActive = isValueLoggingToFileActive;
	}
	
	public Integer getValueLoggingToFileResolution() {
		return valueLoggingToFileResolution;
	}
	
	public void setValueLoggingToFileResolution(Integer valueLoggingToFileResolution) {
		this.valueLoggingToFileResolution = valueLoggingToFileResolution;
	}
	
	public Boolean getIsValueLoggingToDatabaseActive() {
		return valueLoggingToDatabaseActive;
	}
	
	public void setIsValueLoggingToDatabaseActive(Boolean isValueLoggingToDatabaseActive) {
		this.valueLoggingToDatabaseActive = isValueLoggingToDatabaseActive;
	}
	
	public Integer getValueLoggingToDatabaseResolution() {
		return valueLoggingToDatabaseResolution;
	}
	
	public void setValueLoggingToDatabaseResolution(Integer valueLoggingToDatabaseResolution) {
		this.valueLoggingToDatabaseResolution = valueLoggingToDatabaseResolution;
	}
	
	// Console
	
	public Integer getValueLoggingToConsoleResolution() {
		return valueLoggingToConsoleResolution;
	}
	
	public void setValueLoggingToConsoleResolution(
			Integer valueLoggingToConsoleResolution) {
		this.valueLoggingToConsoleResolution = valueLoggingToConsoleResolution;
	}

	// RRD Database
	
	public Boolean getValueLoggingToRrdDatabaseActive() {
		return valueLoggingToRrdDatabaseActive;
	}

	public Integer getValueLoggingToRrdDatabaseResolution() {
		return valueLoggingToRrdDatabaseResolution;
	}

	public void setValueLoggingToRrdDatabaseResolution(
			Integer valueLoggingToRrdDatabaseResolution) {
		this.valueLoggingToRrdDatabaseResolution = valueLoggingToRrdDatabaseResolution;
	}
	
}
