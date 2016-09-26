package osh.datatypes.commodity;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * 
 * @author Ingo Mauser
 *
 */
public enum Commodity {
	
	@XmlEnumValue("activepower")
	ACTIVEPOWER				("activepower", "Wirkleistung", "active power", "W"),	// 
	
	@XmlEnumValue("reactivepower")
	REACTIVEPOWER			("reactivepower", "Blindleistung", "reactive power", "var"),	// 
	
	@XmlEnumValue("naturalgaspower")
	NATURALGASPOWER			("naturalgaspower", "Erdgas", "natural gas", "W"),	// 

	@XmlEnumValue("liquidgaspower")
	LIQUIDGASPOWER			("liquidgaspower", "Fl�ssiggas", "liquid gas", "W"),	// 
	
	@XmlEnumValue("heatinghotwaterpower")
	HEATINGHOTWATERPOWER	("heatinghotwaterpower", "Warmwasser (Heizung)", "hot water", "W"),	// 
	
	@XmlEnumValue("domestichotwaterpower")
	DOMESTICHOTWATERPOWER	("domestichotwaterpower", "Warmwasser (Trinkwasser)", "domestic hot water", "W"),	// 
	
	@XmlEnumValue("coldwaterpower")
	COLDWATERPOWER			("coldwaterpower", "Kaltwasser", "cold water", "W");	// 
	
	private final String commodity;
	
	private final String descriptionDE;
	
	private final String descriptionEN;
	
	private final String unit;
	
	
	/**
	 * CONSTRUCTOR
	 * @param state byte (meaning is UNSIGNED byte (0 - 255))
	 */
	Commodity(String commodity, String descriptionDE, String descriptionEN, String unit) {
		this.commodity = commodity;
		this.descriptionDE = descriptionDE;
		this.descriptionEN = descriptionEN;
		this.unit = unit;
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
	
	
	public static Commodity fromString(String v) {
        for (Commodity c: Commodity.values()) {
            if (c.commodity.equalsIgnoreCase(v)) {
                return c;
            }
        }
        
        throw new IllegalArgumentException(v);
    }
	
	
	public static ArrayList<Commodity> parseCommodityArray( String str ) throws IllegalArgumentException {
		while ( str.startsWith("[") )
			str = str.substring(1);
		
		while ( str.endsWith("]") )
			str = str.substring(0, str.length()-1);
		
		StringTokenizer strtok = new StringTokenizer(str, ",");
		ArrayList<Commodity> commodityList = new ArrayList<Commodity>();
		
		while ( strtok.hasMoreElements() ) {
			Commodity uuid = Commodity.fromString( strtok.nextToken() );
			commodityList.add(uuid);
		}
		return commodityList;
	}
	
}
