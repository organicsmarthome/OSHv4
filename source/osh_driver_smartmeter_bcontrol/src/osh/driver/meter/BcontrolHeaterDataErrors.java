package osh.driver.meter;

import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class BcontrolHeaterDataErrors {
	
	@XmlElement(name="id")
	private int id;
	
	@XmlElement(name="timestamp")
	private double timestamp;
	
	@XmlElement(name="code")
	private int code;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
