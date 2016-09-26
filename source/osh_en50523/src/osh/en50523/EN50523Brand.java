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
 * "Als Format für vorzeichenbehaftete Integer wird das 2-er-Komplement verwendet."<br>
 * <br>
 * "Der für einen String (Zeichenfolge; Array von Zeichen, das mit einem Null-Zeichen endet) 
 * verwendete Zeichensatz ist ASCII."<br>
 * 
 * @author Ingo Mauser
 *
 */
public enum EN50523Brand {
	
	//Arcelik brands
	ARCELIK				((short) 0x4101, "Arcelik"), 		//A1
	BEKO				((short) 0x4201, "Beko"), 			//B1
	BLOMBERG			((short) 0x4202, "Blomberg"), 		//B2
	ELECTRA_BREGENZ		((short) 0x4501, "Electra Bregenz"),//E1
	ARDEM				((short) 0x4102, "Ardem"), 			//A2
	ALTUS				((short) 0x4103, "Altus"), 			//A3
	DEMRAD				((short) 0x4401, "Demrad"), 		//D1
	
	//BSH brands
	SIEMENS				((short) 0x5301, "Siemens"), 		//S1
	BOSCH				((short) 0x4201, "Bosch"), 			//B1
	BALAY				((short) 0x4203, "Balay"), 			//B3
	CONSTRUCTA			((short) 0x4301, "Constructa"), 	//C1
	CONTINENTAL			((short) 0x4302, "Continental"), 	//C2
	COLDEX				((short) 0x4304, "Coldex"), 		//C4
	CORCHO				((short) 0x4308, "Corcho"), 		//C8
	GAGGENAU			((short) 0x4701, "Gaggenau"), 		//G1
	LYNX				((short) 0x4C04, "Lynx"), 			//L4
	METALFRIO			((short) 0x4D01, "Metalfrio"), 		//M1
	NEFF				((short) 0x4E01, "Neff"), 			//N1
	PITSOS				((short) 0x5001, "Pitsos"), 		//P1
	PROFILO				((short) 0x5003, "Profilo"), 		//P3
	PROTOS				((short) 0x5005, "Protos"), 		//P5
	JUNKER_RUH			((short) 0x5205, "Junker & Ruh"),	//R5
	SUPERSER			((short) 0x5305, "Superser"), 		//S5
	THERMADOR			((short) 0x5401, "Thermador"), 		//T1
	UFESA				((short) 0x5501, "Ufesa"), 			//U1
	
	//Candy brands
	CANDY				((short) 0x4301, "Candy"), 			//C1
	HOOVER				((short) 0x4801, "Hoover"), 		//H1
	ROSIERES			((short) 0x5201, "Rosières"), 		//R1
	IBERNA				((short) 0x4901, "Iberna"), 		//I1
	ZEROWATT			((short) 0x5A01, "Zerowatt"), 		//Z1
	OTSEIN				((short) 0x4F01, "Otsein"), 		//O1
	ZEROWATT_HOOVER		((short) 0x5A02, "Zerowatt Hoover"),//Z2
	OTSEIN_HOOVER		((short) 0x4F02, "Otsein Hoover"), 	//O2
	TRIO				((short) 0x5401, "„trio�?"), 		//T1
	
	//CLAGE brand
	CLAGE				((short) 0x434C, "CLAGE"), //CL
	
	//Electrolux brands
	AEG					((short) 0x4101, "AEG"), 						//A1
	ALLWYN				((short) 0x4102, "Allwyn"), 					//A2
	ARTHUR_MARTIN		((short) 0x4103, "Arthur Martin Electrolux"),	//A3
	CORBERO				((short) 0x4301, "Corberó"), 					//C1
	ELEKTRO_HELIOS		((short) 0x4501, "Elektro Helios"), 			//E1
	ELECTROLUX			((short) 0x4502, "Electrolux"), 				//E2
	FAURE				((short) 0x4601, "Faure"), 						//F1
	FRIGIDAIRE			((short) 0x4602, "Frigidaire"), 				//F2
	HUSQVARNA			((short) 0x4801, "Husqvarna"), 					//H1
	KELVINATOR			((short) 0x4B01, "Kelvinator"), 				//K1
	ELECTROLUX_MAXCLEAN	((short) 0x4D01, "Electrolux Maxclean"), 		//M1
	REX					((short) 0x5201, "Rex"), 						//R1
	ROSENLEW			((short) 0x5202, "Rosenlew"), 					//R2
	SAMUS				((short) 0x5301, "Samus"), 						//S1
	VOSS				((short) 0x5601, "Voss"), 						//V1
	WHITE_WESTINGHOUSE	((short) 0x5701, "White Westinghouse"), 		//W1
	ZANKER_ELECTROLUX	((short) 0x5A01, "Zanker Electrolux"), 			//Z1
	ZANKER				((short) 0x5A02, "Zanker"), 					//Z2
	ZANUSSI				((short) 0x5A03, "Zanussi"), 					//Z3
	ZANUSSI_SAMUS		((short) 0x5A04, "Zanussi-Samus"), 				//Z4
	
	//ElcoBrandt brands
	BRANDT				((short) 0x4252, "Brandt"), 	//BR
	DE_DIETRICH			((short) 0x4444, "DE Dietrich"),//DD
	OCEAN				((short) 0x4F43, "Ocean"), 		//OC
	SAMET				((short) 0x534D, "Samet"), 		//SM
	SAN_GIORGIO			((short) 0x5347, "San Giorgio"),//SG
	SAUTER				((short) 0x5355, "Sauter"), 	//SU
	THOMSON				((short) 0x5448, "Thomson"), 	//TH
	VEDETTE				((short) 0x5645, "Vedette"), 	//VE
	
	//Fagor brands
	FAGOR				((short) 0x4601, "Fagor"), //F1
	ASPES				((short) 0x4101, "Aspes"), //A1
	EDESA				((short) 0x4501, "Edesa"), //E1
	
	//Liebherr brand
	LIEBHERR			((short) 0x4C48, "Liebherr"), //LH
	
	//Gorenje brands
	GORENJE				((short) 0x4747, "Gorenje"), 			//GG
	SIDEX				((short) 0x4753, "Sidex"), 				//GS
	KOERTING			((short) 0x474B, "Körting"), 			//GK
	GALANT				((short) 0x4741, "Galant"), 			//GA
	PACIFIC				((short) 0x4743, "Pacific"), 			//GC
	PACIFIC_BY_GORENJE	((short) 0x4759, "Pacific by Gorenje"), //GY
	GORENJE_PININFARINA	((short) 0x4750, "Gorenje Pininfarina"),//GP

	//Indesit Company brands
	ARISTON				((short) 0x4152, "Ariston"), //AR
	INDESIT				((short) 0x494E, "Indesit"), //IN
	SCHOLTES			((short) 0x5343, "Scholtes"),//SC
	STINOL				((short) 0x5354, "Stinol"),  //ST

	//Miele brands
	MIELE				((short) 0x4D49, "Miele"), //MI

	//V-ZUG brands
	ZUG					((short) 0x5A47, "ZUG"), 	//ZG
	GEHRIG				((short) 0x4747, "Gehrig"),	//GG
	SIBIR				((short) 0x5349, "Sibir"), 	//SI

	//Whirlpool brands
	WHIRLPOOL			((short) 0x5748, "Whirlpool"),	//WH
	BAUKNECHT			((short) 0x424B, "Bauknecht"), 	//BK
	IGNIS				((short) 0x4247, "Ignis"), 		//BG
	LADEN				((short) 0x4C44, "Laden"),		//LD
	
	//non-standard:
	SAMSUNG 			((short) 0x5341, "Samsung");	//SA
	
	
	private short brandID;
	private String brandName;
	
	private EN50523Brand(
			short brandID, 
			String brandName) {
		this.brandID = brandID;
		this.brandName = brandName;
	}

	public short getBrandID() {
		return brandID;
	}

	public String getBrandName() {
		return brandName;
	}
	
	

}
