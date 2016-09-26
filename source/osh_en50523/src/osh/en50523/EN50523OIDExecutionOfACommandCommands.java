package osh.en50523;

/**
 * 
 * Source: DIN EN 50523-1:2010-05, p.42
 * 
 * @author Ingo Mauser, Julian Rothenbacher
 *
 */
public enum EN50523OIDExecutionOfACommandCommands {
	
	//TODO add descriptions
	RESERVED			((byte) 0, ""),
	START				((byte) 1, ""),
	STOP				((byte) 2, ""),
	PAUSE				((byte) 3, ""),
	STARTDEEPFREEZE		((byte) 4, ""),
	STOPDEEPFREEZE		((byte) 5, ""),
	STARTSUPERCOOL		((byte) 6, ""),
	STOPSUPERCOOL		((byte) 7, ""),
	STARTOVERHEAT		((byte) 8, ""),
	STOPOVERHEAT		((byte) 9, ""),
	ACTIVATEGAS			((byte) 10, ""),
	DEACTIVATEGAS		((byte) 11, "");

	
	private byte dinEN50523Command;
	private String descriptionEN;
	
	/**
	 * CONSTRUCTOR
	 * @param EN50523Command
	 * @param descriptionEN
	 */
	//oder mit int und dann eine explizite Typumwandlung in byte...
	private EN50523OIDExecutionOfACommandCommands(byte dinEN50523Command, String descriptionEN) {
		this.dinEN50523Command = dinEN50523Command;
		this.descriptionEN = descriptionEN;
	}
	
	
	public byte getEN50523Command () {
		return dinEN50523Command;
	}
	
	public String getdescriptionEN() {
		return descriptionEN;
	}
	
	//TODO getSignedValue(){}, getUnsignedValue(){}...
	//TODO fromString(String v){}...
	
}
