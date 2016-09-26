package osh.comdriver.interaction.datatypes;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Kaibin Bao
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType
public class RestStateDetail {
	
	protected UUID sender;
	private long timestamp;

	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestStateDetail() {
		this(null, 0);
	}
	
	public RestStateDetail(UUID sender, long timestamp) {
		super();
		this.sender = sender;
		this.timestamp = timestamp;
	}

	@XmlTransient
	public UUID getSender() {
		return sender;
	}
	
	public void setSender(UUID sender) {
		this.sender = sender;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RestStateDetail other = (RestStateDetail) obj;
		if (sender == null) {
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}
	
	
}
