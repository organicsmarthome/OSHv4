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
@Table(name="element")
//@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
//@XmlType(name="element")
//@XmlRootElement(name="element")
public class RestHomeConfigElement {
	@Id
	//@GeneratedValue
	@XmlAttribute(name="id")
	public Long id;
	
	@XmlAttribute(name="type")
	public String type;
	
	@XmlAttribute(name="x")
	public int x;
	
	@XmlAttribute(name="y")
	public int y;
	
	@XmlAttribute(name="w")
	public int w;
	
	@XmlAttribute(name="h")
	public int h;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	public int getW() {
		return w;
	}
	public void setW(int w) {
		this.w = w;
	}
	
	public int getH() {
		return h;
	}
	public void setH(int h) {
		this.h = h;
	}
}
