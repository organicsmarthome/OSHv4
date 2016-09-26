package osh.driver.meter;

import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class BcontrolHeaterDataUpdate {
	
	@XmlElement(name="status")
	private String status;
	
	@XmlElement(name="progress")
	private double progress;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}
	
}
