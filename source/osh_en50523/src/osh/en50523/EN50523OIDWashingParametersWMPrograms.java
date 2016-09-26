package osh.en50523;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * DIN EN 50523 Parameters for washing machine (according to Miele Washing Machine W8985 WPS ProgramIDs)
 * 
 * @author Julian Rothenbacher, Ingo Mauser
 *
 */
@XmlType
public enum EN50523OIDWashingParametersWMPrograms {

	/**
	 * Device:	Washing Machine<br>
	 * Program:	no program selected<br>
	 * DE: 		Kein Programm ausgewaehlt<br>
	 * Meaning:	No washing program has been selected<br>
	 */
	@XmlEnumValue("0")
	NOPROGRAMSELECTED	( 0, "KEIN PROGRAMM AUSGEWAEHLT"),
	
	
	/**
	 * Device:	Washing Machine<br>
	 * Program:	Cottons<br>
	 * DE: 		Baumwolle<br>
	 * Meaning:	Cottons washing program has been selected<br>
	 */
	@XmlEnumValue("2")
	COTTONS				( 2, "BAUMWOLLE"),
	
	/**
	 * Device:	Washing Machine<br>
	 * Program:	Minimum Iron<br>
	 * DE: 		Pflegeleicht<br>
	 * Meaning:	Minimum Iron washing program has been selected<br>
	 */
	@XmlEnumValue("3")
	MINIMUMIRON			( 3, "PFLEGELEICHT"),
	
	/**
	 * Device:	Washing Machine<br>
	 * Program:	Delicates<br>
	 * DE: 		Synthetic<br>
	 * Meaning:	Delicates washing program has been selected<br>
	 */
	@XmlEnumValue("4")
	DELICATES			( 4, "SYNTHETIC"),
	
	/**
	 * Device:	Washing Machine<br>
	 * Program:	Quick wash<br>
	 * DE: 		Kurzwaschgang<br>
	 * Meaning:	Quick wash washing program has been selected<br>
	 */
	@XmlEnumValue("7")
	QUICKWASH			( 7, "KURZWASCHGANG"),
	
	/**
	 * Device:	Washing Machine<br>
	 * Program:	Woolens<br>
	 * DE: 		Wolle<br>
	 * Meaning:	Woolens washing program has been selected<br>
	 */
	@XmlEnumValue("8")
	WOOLLENS			( 8, "WOLLE"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	Silks<br>
	 * DE: 		Seide<br>
	 * Meaning:	Silks washing program has been selected<br>
	 */
	@XmlEnumValue("9")
	SILKS				( 9, "SEIDE"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	Starch<br>
	 * DE: 		Staerken<br>
	 * Meaning:	Starch washing program has been selected<br>
	 */
	@XmlEnumValue("17")
	STARCH				( 17, "STAERKEN"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	Seperate rinse<br>
	 * DE: 		Extraspuelen<br>
	 * Meaning:	Seperate rinse washing program has been selected<br>
	 */
	@XmlEnumValue("18")
	SEPERATERINSE		( 18, "EXTRASPUELEN"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	Drain<br>
	 * DE: 		Pumpen<br>
	 * Meaning:	Drain washing program has been selected<br>
	 */
	@XmlEnumValue("19")
	DRAIN				( 19, "PUMPEN"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	Spin/Drain<br>
	 * DE: 		Pumpen/Schleudern<br>
	 * Meaning:	Spin/Drain washing program has been selected<br>
	 */
	@XmlEnumValue("21")
	SPINDRAIN			( 21, "PUMPENSCHLEUDERN"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	Curtains<br>
	 * DE: 		Gardinen<br>
	 * Meaning:	Curtains washing program has been selected<br>
	 */
	@XmlEnumValue("22")
	CURTAINS			( 22, "GARDINEN"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	Shirts<br>
	 * DE: 		Oberhemden<br>
	 * Meaning:	Shirts washing program has been selected<br>
	 */
	@XmlEnumValue("23")
	SHIRTS				( 23, "OBERHEMDEN"),
		
	/**
	 * Device:	Washing Machine<br>
	 * Program:	Denim<br>
	 * DE: 		Jeans<br>
	 * Meaning:	Denim washing program has been selected<br>
	 */
	@XmlEnumValue("24")
	DENIM				( 24, "JEANS"),
	
	/**
	 * Device:	Washing Machine<br>
	 * Program:	Proofing<br>
	 * DE: 		Impraegnieren<br>
	 * Meaning:	Proofing washing program has been selected<br>
	 */
	@XmlEnumValue("27")
	PROOFING			( 27, "IMPRAEGNIEREN"),
	
	/**
	 * Device:	Washing Machine<br>
	 * Program:	Sports wear<br>
	 * DE: 		Sportwaesche<br>
	 * Meaning:	Sports wear washing program has been selected<br>
	 */
	@XmlEnumValue("29")	
	SPORTSWEAR			( 29, "SPORTWAESCHE"),
	
	/**
	 * Device:	Washing Machine<br>
	 * Program:	Automatic<br>
	 * DE: 		Automatic<br>
	 * Meaning:	Automatic washing program has been selected<br>
	 */
	@XmlEnumValue("31")
	AUTOMATIC			( 31, "AUTOMATIC"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	Outer wear<br>
	 * DE: 		Outdoor<br>
	 * Meaning:	Outer wear washing program has been selected<br>
	 */
	@XmlEnumValue("37")
	OUTERWEAR			( 37, "OUTDOOR"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	Pillows<br>
	 * DE: 		Kopfkissen<br>
	 * Meaning:	Pillows washing program has been selected<br>
	 */
	@XmlEnumValue("39")
	PILLOWS				( 39, "KOPFKISSEN"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	Express<br>
	 * DE: 		Express<br>
	 * Meaning:	Express washing program has been selected<br>
	 */
	@XmlEnumValue("49")
	EXPRESS				( 49, "EXPRESS"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	Dark Garments<br>
	 * DE: 		Dunkle Waesche<br>
	 * Meaning:	Dark Garments washing program has been selected<br>
	 */
	@XmlEnumValue("50")
	DARKGARMENTS		( 50, "DUNKLEWAESCHE"),

	/**
	 * Device:	Washing Machine<br>
	 * Program:	First wash<br>
	 * DE: 		Neue Textilien<br>
	 * Meaning:	First wash washing program has been selected<br>
	 */
	@XmlEnumValue("53")
	FIRSTWASH			( 53, "NEUETEXTILIEN");
	
	
	@XmlElement
	private final int programID;
	@XmlAttribute
	private final String descriptionDE;
	
	EN50523OIDWashingParametersWMPrograms (int programId, String descriptionDE) {
		this.programID =  programId;
		this.descriptionDE = descriptionDE;
				
	}
	
	
	
	/**
	 * @return -128 to 127
	 */
	public byte getByteValue() {
		return (byte) programID;
	}

	
    public static EN50523OIDWashingParametersWMPrograms fromString(String v) {
    	// 0 to 127
        for (EN50523OIDWashingParametersWMPrograms c: EN50523OIDWashingParametersWMPrograms.values()) {
            if (Integer.toString(c.programID).equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    
	public int getProgramID() {
		return programID;
	}

	public String getDescriptionDE() {
		return descriptionDE;
	}
	
	
	
	
	
	
	
	
}
