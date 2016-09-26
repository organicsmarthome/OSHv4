package osh.utils.time;

public enum Months {
	JAN("Januar"),
	FEB("Februar"),
	MAR("März"),
	APR("April"),
	MAY("Mai"),
	JUN("Juni"),
	JUL("Juli"),
	AUG("August"),
	SEP("September"),
	OCT("Oktober"),
	NOV("November"),
	DEC("Dezember");
	
	private String name;
	
	private Months(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
