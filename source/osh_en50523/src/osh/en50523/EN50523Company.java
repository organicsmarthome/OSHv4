package osh.en50523;

/**
 * DIN EN 50523:<br>
 * <br>
 * "Byte I ist das i-te übermittelte Byte des Datenfelds. Um das direkte Lesen 
 * des Datenfelds zu erleichtern, wird die Big-Endian-Zuordnung verwendet."<br>
 * <br>
 * "Im Falle der Verwendung von Wortfeldern, die aus zwei Bytes bestehen, 
 * ist das signifikanteste Byte das erste Feld."<br>
 * <br>
 * "Als Format für vorzeichenbehaftete shorteger wird das 2-er-Komplement verwendet."<br>
 * <br>
 * "Der für einen String (Zeichenfolge; Array von Zeichen, das mit einem Null-Zeichen endet) 
 * verwendete Zeichensatz ist ASCII."<br>
 * 
 * @author Ingo Mauser
 *
 */
public enum EN50523Company {
	
	ARCELIK 	((short) 0x4152, "Arcelik"), 		//AR
	BSH 		((short) 0x4253, "BSH"),			//BS
	CANDY 		((short) 0x4341, "Candy"),			//CA
	CLAGE 		((short) 0x434C, "CLAGE"),			//CL
	ELECTROLUX	((short) 0x454C, "Electrolux"),		//EL
	ELCOBRANDT	((short) 0x4542, "ElcoBrandt"),		//EB
	FAGOR 		((short) 0x4641, "Fagor"),			//FA
	LIEBHERR 	((short) 0x4C48, "Liebherr"),		//LH
	GORENJE 	((short) 0x474F, "Gorenje"),		//GO
	INDESIT 	((short) 0x4943, "Indesit Company"),//IC
	MIELE 		((short) 0x4D49, "Miele"),			//MI
	V_ZUG 		((short) 0x565A, "V-ZUG AG"),		//VZ
	WHIRLPOOL 	((short) 0x5748, "Whirlpool"),		//WH
	
	// non-standard:
	SAMSUNG 	((short) 0x5341, "Samsung");		//SA
	
	
	private short companyID;
	private String companyName;
	
	
	private EN50523Company(
			short id, 
			String companyName) {
		this.companyID = id;
		this.companyName = companyName;
	}

	
	public short getCompanyID() {
		return companyID;
	}
	
	public String getCompanyName() {
		return companyName;
	}
	
}
