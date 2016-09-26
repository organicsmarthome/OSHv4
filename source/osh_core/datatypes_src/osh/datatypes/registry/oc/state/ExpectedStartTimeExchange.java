package osh.datatypes.registry.oc.state;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import osh.datatypes.registry.StateExchange;


@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlRootElement
public class ExpectedStartTimeExchange extends StateExchange {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5083812677185394953L;
	private long expectedStartTime;

	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private ExpectedStartTimeExchange() { this(null, 0); }
	
	public ExpectedStartTimeExchange(UUID sender, long timestamp) {
		super(sender, timestamp);
	}
	
	public ExpectedStartTimeExchange(UUID sender, long timestamp,
			long startTime) {
		super(sender, timestamp);
		setExpectedStartTime(startTime);
	}

	public long getExpectedStartTime() {
		return expectedStartTime;
	}
	
	public void setExpectedStartTime(long expectedStartTime) {
		this.expectedStartTime = expectedStartTime;
	}

	@Override
	public String toString() {
		return "ExpectedStartTimeExchange [expectedStartTime="
				+ expectedStartTime + "]";
	}
}
