package osh.en50523;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Probably no DIN EN 50523 standardized parameters!
 * Parameters for Tumble Dryer (according to Miele Tumble Dryer T8687 C)
 * 
 * @author Julian Rothenbacher, Ingo Mauser
 *
 */
@XmlType
public enum EN50523OIDWashingParametersTDPrograms {

	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	no program selected<br>
	 * DE: 		Kein Programm ausgewaehlt<br>
	 * Meaning:	No drying program has been selected<br>
	 */
	@XmlEnumValue("0")
	NOPROGRAMSELECTED				((byte) 0, "KEIN PROGRAMM AUSGEWAEHLT"),
	
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Cottons<br>
	 * DE: 		Baumwolle<br>
	 * Meaning:	Cottons drying program has been selected<br>
	 */
	@XmlEnumValue("1")
	COTTONS				((byte) 1, "BAUMWOLLE"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Minimum Iron<br>
	 * DE: 		Pflegeleicht<br>
	 * Meaning:	Minimum Iron drying program has been selected<br>
	 */
	@XmlEnumValue("2")
	MINIMUMIRON				((byte) 2, "PFLEGELEICHT"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Woolens Handcare<br>
	 * DE: 		Finish Wolle<br>
	 * Meaning:	Woolens Handcare drying program has been selected<br>
	 */
	@XmlEnumValue("3")
	WOOLLENSHANDCARE				((byte) 3, "WOLLEFINISH"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Smoothing<br>
	 * DE: 		Glaetten<br>
	 * Meaning:	Smoothing drying program has been selected<br>
	 */
	@XmlEnumValue("4")
	SMOOTHING				((byte) 4, "GLAETTEN"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Cool Air<br>
	 * DE: 		Lueften kalt<br>
	 * Meaning:	Cool Air drying program has been selected<br>
	 */
	@XmlEnumValue("5")
	COOLAIR				((byte) 5, "LUEFTENKALT"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Warm Air<br>
	 * DE: 		<br>
	 * Meaning:	Warm Air drying program has been selected<br>
	 */
	@XmlEnumValue("6")
	WARMAIR				((byte) 6, "LUEFTENWARM"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Denim<br>
	 * DE: 		Jeans<br>
	 * Meaning:	Denim drying program has been selected<br>
	 */
	@XmlEnumValue("7")
	DENIM				((byte) 7, "JEANS"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Shirts<br>
	 * DE: 		Oberhemden<br>
	 * Meaning:	Shirts drying program has been selected<br>
	 */
	@XmlEnumValue("8")
	SHIRTS				((byte) 8, "OBERHEMDEN"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Outer wear<br>
	 * DE: 		Outdoor<br>
	 * Meaning:	Outer wear drying program has been selected<br>
	 */
	@XmlEnumValue("9")
	OUTERWEAR				((byte) 9, "OUTDOOR"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Automatic<br>
	 * DE: 		Automatic<br>
	 * Meaning:	Automatic drying program has been selected<br>
	 */
	@XmlEnumValue("10")
	AUTOMATIC				((byte) 10, "AUTOMATIC"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Proofing<br>
	 * DE: 		Impraegnieren<br>
	 * Meaning:	Proofing drying program has been selected<br>
	 */
	@XmlEnumValue("11")
	PROOFING				((byte) 11, "IMPRAEGNIEREN"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Standard Pillows<br>
	 * DE: 		Kopfkissen<br>
	 * Meaning:	Standard Pillows drying program has been selected<br>
	 */
	@XmlEnumValue("13")
	STANDARDPILLOWS				((byte) 13, "KOPFKISSEN"),

	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Delicates<br>
	 * DE: 		Synthetic<br>
	 * Meaning:	Delicates drying program has been selected<br>
	 */
	@XmlEnumValue("14")
	DELICATES				((byte) 14, "SYNTHETIC"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Sports wear<br>
	 * DE: 		Sportwaesche<br>
	 * Meaning:	Sports wear drying program has been selected<br>
	 */
	@XmlEnumValue("16")	
	SPORTSWEAR				((byte) 16, "SPORTWAESCHE"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Silk handcare<br>
	 * DE: 		Finish Seide<br>
	 * Meaning:	Silk handcare drying program has been selected<br>
	 */
	@XmlEnumValue("17")
	SILKHANDCARE				((byte) 17, "SEIDEFINISH"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Large Pillows<br>
	 * DE: 		Kopfkissen groß<br>
	 * Meaning:	Large Pillows drying program has been selected<br>
	 */
	@XmlEnumValue("18")
	LARGEPILLOWS				((byte) 18, "KOPFKISSENGROSS"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Cotton hygiene<br>
	 * DE: 		Baumwolle Hygiene<br>
	 * Meaning:	Cotton hygiene drying program has been selected<br>
	 */
	@XmlEnumValue("19")
	COTTONHYGIENE				((byte) 19, "BAUMWOLLEHYGIENE"),

	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Minimum iron hygiene<br>
	 * DE: 		Pflegeleicht Hygiene<br>
	 * Meaning:	Minimum iron hygiene drying program has been selected<br>
	 */
	@XmlEnumValue("20")
	MINIMUMIRONHYGIENE				((byte) 20, "PFLEGELEICHTHYGIENE"),
	
	/**
	 * Device:	Tumble Dryer<br>
	 * Program:	Timed drying hygiene<br>
	 * DE: 		Lueften Hygiene<br>
	 * Meaning:	Timed drying hygiene drying program has been selected<br>
	 */
	@XmlEnumValue("21")
	TIMEDDRYINGHYGIENE				((byte) 21, "LUEFTENHYGIENE");
	
	
	
	
	
	
	/**
	 * Unsigned Byte
	 */
	@XmlElement
	private final byte programID;
	@XmlAttribute
	private final String descriptionDE;
	
	EN50523OIDWashingParametersTDPrograms (int programId, String descriptionDE) {
		this.programID = (byte) programId;
		this.descriptionDE = descriptionDE;
				
	}
	
	/**
	 * 
	 * @return -128 to 127
	 */
	public byte getSignedValue() {
		return programID;
	}
	
	/**
	 * 
	 * @return 0 to 255
	 */
	public int getUnsignedValue() {
		return programID & 0xFF;
	}
	
    public static EN50523OIDWashingParametersTDPrograms fromString(String v) {
    	// 0 to 127
        for (EN50523OIDWashingParametersTDPrograms c: EN50523OIDWashingParametersTDPrograms.values()) {
            if (Byte.toString(c.programID).equals(v)) {
                return c;
            }
        }
        // 128 to 255
        for (EN50523OIDWashingParametersTDPrograms c: EN50523OIDWashingParametersTDPrograms.values()) {
            if (Integer.toString(c.programID & 0xFF).equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    
	public byte getProgramID() {
		return programID;
	}

	public String getDescriptionDE() {
		return descriptionDE;
	}
	
	
	
	
	
	
	
	
}
