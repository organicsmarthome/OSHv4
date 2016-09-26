package osh.driver.meter;

import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class BcontrolHeaterDataRegisters {
	
	@XmlElement(name="register")
	private String register;
	
	@XmlElement(name="value")
	private double value;

	public String getRegister() {
		return register;
	}

	public void setRegister(String register) {
		this.register = register;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
