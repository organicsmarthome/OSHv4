package osh.datatypes.registry.oc.state.globalobserver;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.StateExchange;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GUIAncillaryMeterStateExchange extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9104434585663750101L;
	private AncillaryCommodityLoadProfile ancillaryMeter;
	
	@Deprecated
	public GUIAncillaryMeterStateExchange() {
		super(null, 0L);
	}
	
	public GUIAncillaryMeterStateExchange(UUID sender, long timestamp, AncillaryCommodityLoadProfile ancillaryMeter) {
		super(sender, timestamp);
		this.ancillaryMeter = ancillaryMeter;
	}

	public AncillaryCommodityLoadProfile getAncillaryMeter() {
		return ancillaryMeter;
	}

}
