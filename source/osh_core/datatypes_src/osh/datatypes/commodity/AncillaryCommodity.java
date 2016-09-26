package osh.datatypes.commodity;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * 
 * @author Ingo Mauser
 *
 */
public enum AncillaryCommodity {
	
	@XmlEnumValue("activepowerexternal")
	ACTIVEPOWEREXTERNAL		("activepowerexternal", "Wirkleistung Bezug", "active power consumption", "W"),	// 
	
	@XmlEnumValue("reactivepower")
	REACTIVEPOWEREXTERNAL	("reactivepowerexternal", "Blindleistung Bezug", "reactive power consumption", "W"),	// 
	
	@XmlEnumValue("pvactivepowerfeedin")
	PVACTIVEPOWERFEEDIN		("pvactivepowerfeedin", "Wirkleistung Rückspeisung von PV", "active power feedin from PV", "W"),	// 
	
	@XmlEnumValue("pvactivepowerautoconsumption")
	PVACTIVEPOWERAUTOCONSUMPTION ("pvactivepowerautoconsumption", "Wirkleistung Eigenstromnutzung von PV", "active power auto consumption from PV", "W"),	//
	
	@XmlEnumValue("chpactivepowerfeedin")
	CHPACTIVEPOWERFEEDIN		("chpactivepowerfeedin", "Wirkleistung Rückspeisung von CHP", "active power feedin from CHP", "W"),	// 
	
	@XmlEnumValue("chpactivepowerautoconsumption")
	CHPACTIVEPOWERAUTOCONSUMPTION ("chpactivepowerautoconsumption", "Wirkleistung Eigenstromnutzung von CHP", "active power auto consumption from CHP", "W"),	//
	
	@XmlEnumValue("batteryactivepowerfeedin")
	BATTERYACTIVEPOWERFEEDIN		("batteryactivepowerfeedin", "Wirkleistung Rückspeisung von Batterie", "active power feedin from battery", "W"),	// 
	
	@XmlEnumValue("batteryactivepowerautoconsumption")
	BATTERYACTIVEPOWERAUTOCONSUMPTION ("batteryactivepowerautoconsumption", "Wirkleistung Eigenstromnutzung von Batterie", "active power auto consumption from battery", "W"),	//
	
	@XmlEnumValue("batteryactivepowerconsumption")
	BATTERYACTIVEPOWERCONSUMPTION ("batteryactivepowerconsumption", "Wirkleistung Eigenstromverbrauch von Batterie", "active power consumption from battery", "W"),	//
	
	@XmlEnumValue("naturalgaspowerexternal")
	NATURALGASPOWEREXTERNAL	("naturalgaspowerexternal", "Leistung Erdgas", "natural gas power consumption", "W");	// 
	
	private final String commodity;
	
	private final String descriptionDE;
	
	private final String descriptionEN;
	
	private final String unit;
	
	/**
	 * CONSTRUCTOR
	 * @param state byte (meaning is UNSIGNED byte (0 - 255))
	 */
	AncillaryCommodity(String commodity, String descriptionDE, String descriptionEN, String unit) {
		this.commodity = commodity;
		this.descriptionDE = descriptionDE;
		this.descriptionEN = descriptionEN;
		this.unit = unit;
	}
	
	
	public static AncillaryCommodity fromString(String v) {
		for (AncillaryCommodity c: AncillaryCommodity.values()) {
			if (c.commodity.equalsIgnoreCase(v)) {
				return c;
			}
		}
		
		throw new IllegalArgumentException(v);
	}

	@Override
	public String toString() {
		return this.commodity;
	}
	
	public String getCommodity() {
		return commodity;
	}

	public String getDescriptionDE() {
		return descriptionDE;
	}

	public String getDescriptionEN() {
		return descriptionEN;
	}
	
	
	public String getUnit() {
		return unit;
	}
}
