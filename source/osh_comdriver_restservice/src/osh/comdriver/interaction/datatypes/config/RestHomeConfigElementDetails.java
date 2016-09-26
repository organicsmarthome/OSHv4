package osh.comdriver.interaction.datatypes.config;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * 
 * @author Birger Becker
 *
 */

@Entity
@Table(name="elementDetails")
public class RestHomeConfigElementDetails {
	@Id
	@XmlAttribute(name="id")
	public Long id;
	
	@XmlAttribute(name="name")
	public String name;
	
	@XmlAttribute(name="bus")
	public String bus;
	
	@XmlAttribute(name="sensor")
	public String sensor;
	
	@XmlAttribute(name="location")
	public String location;
	
	@XmlAttribute(name="icon")
	public String icon;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBus() {
		return bus;
	}
	public void setBus(String bus) {
		this.bus = bus;
	}
	public String getSensor() {
		return sensor;
	}
	public void setSensor(String sensor) {
		this.sensor = sensor;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
}
