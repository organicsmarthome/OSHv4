package osh.busdriver.mielegateway.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.persistence.oxm.annotations.XmlPath;

@XmlType
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
public class MieleDuration {
	/**
	 * Duration in minutes
	 */
	private int duration;
	
	
	public MieleDuration() {
		super();
	}
	
	public MieleDuration(int duration) {
		super();
		this.duration = duration;
	}

	@XmlPath("text()")
	public void setDuration(String mieleTimeString) {
		this.duration = parseString(mieleTimeString);
	}
	
	/**
	 * Returns MieleTime interpreted as duration in minutes
	 * 
	 * @return
	 */
	public int duration() {
		return duration;
	}
	
	public int hour() {
		return duration / 60;
	}
	
	public int minute() {
		return duration % 60;
	}

	static private int parseString(String mieleTimeString) {
		// strip "h" suffix and spaces at end
		while ( mieleTimeString.endsWith("h") || mieleTimeString.endsWith(" ") ) {
			// strip one character
			mieleTimeString = mieleTimeString.substring(0, mieleTimeString.length()-1);
		}

		String[] timeParts = mieleTimeString.split(":");
		if ( timeParts.length == 2 ) {
			return Integer.valueOf(timeParts[0]) * 60 + Integer.valueOf(timeParts[1]);
		} else {
			return -1;
		}
	}
	
	@Override
	public String toString() {
		if ( duration >= 0 )
			return Integer.toString(duration) + "m";
		else
			return "invalid duration";
	}
}
