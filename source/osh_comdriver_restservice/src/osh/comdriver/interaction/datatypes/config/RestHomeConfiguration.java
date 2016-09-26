package osh.comdriver.interaction.datatypes.config;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.annotations.PrivateOwned;

@Entity
@Table(name="shconfig")
@XmlRootElement(name="HomeConfiguration")
public class RestHomeConfiguration {
	@Id
	@GeneratedValue
	public Long id;
	
	@OneToMany(cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@PrivateOwned  // deletes child elements, when parent element is deleted
	@XmlElement(name="element")
	public List<RestHomeConfigElement> elements;
	
	public List<RestHomeConfigElement> getElements() {
		if( elements == null ) {
			elements = new ArrayList<>();
		}
		return elements;
	}
	
	public void setElements(List<RestHomeConfigElement> elements) {
		this.elements = elements;
	}
}
