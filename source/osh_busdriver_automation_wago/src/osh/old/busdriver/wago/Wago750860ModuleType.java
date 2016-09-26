package osh.old.busdriver.wago;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
public enum Wago750860ModuleType {
	CONTROLLER		(0),
	METER			(1),
	SWITCH			(2),
	VIRTUALSWITCH	(3),
	DIGITALINPUT	(4),
	ANALOGINPUT		(5),
	VIRTUALMETER	(6),
	DIGITALOUTPUT   (7);
	
	private final byte value;
	
	Wago750860ModuleType( int value ) {
		this.value = (byte) value;
	}
	
	public byte value() {
		return value;
	}
}
