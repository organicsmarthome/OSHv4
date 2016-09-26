package osh.en50523;

/**
 * [DIN EN 50523-2 p.13]<br>
 * Extended with IDs for appliances not found in DIN EN 50523<br>
 * shortID should be in range 00 to 31 (5 bits out of 8)
 * 
 * @author Ingo Mauser
 *
 */
public enum EN50523DeviceType {

	COMBINATIONAPPLIANCE	("CB", EN50523Cluster.HOUSEHOLD, EN50523Category.COMMON, (byte) 0x00, "combination appliance", "Kombigerät"),
	
	AIRCONDITION			("AC", EN50523Cluster.HOUSEHOLD, EN50523Category.VENTILATION, (byte) 0x03, "", "Klimaanlage"),
	
	DISHWASHER 				("DW", EN50523Cluster.HOUSEHOLD, EN50523Category.WET, (byte) 0x01, "dishwasher", "Geschirrspülmaschine"),
	DRYER 					("TD", EN50523Cluster.HOUSEHOLD, EN50523Category.WET, (byte) 0x02, "dryer", "Wäschetrockner"),
	WASHERDRYER 			("WD", EN50523Cluster.HOUSEHOLD, EN50523Category.WET, (byte) 0x03, "washerdryer", "Waschtrockner"),
	WASHINGMACHINE 			("WM", EN50523Cluster.HOUSEHOLD, EN50523Category.WET, (byte) 0x04, "washing machine", "Waschmaschine"),
	
	GASOVEN 				("GO", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x01, "gas oven", "Gasbackofen"),
	GASSTOVE 				("GT", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x02, "gas stove", "Gasherd"),
	ELECTRICCOOKTOP 		("HB", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x03, "cooktop (electrical)", "Elektrische Kochmulde"),
	COOKERHOOD 				("HD", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x04, "cooker hood", "Dunstabzugshaube"),
	MICROWAVE				("MV", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x05, "microwave", "Mikrowellenherd"),
	ELECTRICOVEN 			("OV", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x06, "oven (electrical)", "Elektrischer Backofen"),
	ELECTRICSTOVE 			("RG", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x07, "stove (electrical)", "Elektrischer Küchenherd"),
	STEAMER 				("ST", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x08, "steamer", "Dampfgarer"),
	INDUCTIONCOOKTOP 		("IH", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x09, "cooktop (induction)", "Induktionsherd"),
	
	FREEZERREFRIGERATOR		("FR", EN50523Cluster.HOUSEHOLD, EN50523Category.COLD, (byte) 0x01, "refrigerator with freezer", "Kühlschrank mit Gefrierabteil"),
	FREEZER					("FZ", EN50523Cluster.HOUSEHOLD, EN50523Category.COLD, (byte) 0x02, "freezer", "Gefrierschrank bzw. -truhe"),
	REFRIGERATOR			("RE", EN50523Cluster.HOUSEHOLD, EN50523Category.COLD, (byte) 0x03, "refrigerator", "Kühlschrank"),
	WINECOOLER				("WC", EN50523Cluster.HOUSEHOLD, EN50523Category.COLD, (byte) 0x04, "wine cooler", "Weinkühlschrank"),
	
	WATERHEATERINSTANTANEOUS("WHI", EN50523Cluster.HOUSEHOLD, EN50523Category.HEAT, (byte) 0x01, "water heater (instantaneous)", "Durchlauferhitzer"),
	WATERHEATERSTORAGE		("WHS", EN50523Cluster.HOUSEHOLD, EN50523Category.HEAT, (byte) 0x02, "water heater (storage)", "Heißwasserspeicher"),
	
	// EXTENDED (not found in DIN EN 50523)
	// found in Miele Gateway:
	COFFEESYSTEM 			("CM", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x0A, "coffee maker", "Kaffeevollautomat"),
	// self-assigned
	GASCOOKTOP 				("GH", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x21, "gas cooktop", "Gaskochmulde"),
	BREADMAKER 				("BM", EN50523Cluster.HOUSEHOLD, EN50523Category.HOT, (byte) 0x22, "bread maker", "Brotbackautomat"),
	GATEWAY 				("GW", EN50523Cluster.HOUSEHOLD, EN50523Category.COMMON, (byte) 0x31, "gateway", "Gateway");
	
	private String productName;
	private EN50523Cluster cluster;
	private EN50523Category category;
	private byte shortID;
	private String descriptionEN;
	private String descriptionDE;
	
	private short productTypeID;

	
	/**
	 * CONSTRUCTOR
	 * @param productName
	 * @param cluster
	 * @param category
	 * @param shortID
	 * @param descriptionEN
	 * @param descriptionDE
	 */
	private EN50523DeviceType(String productName, EN50523Cluster cluster,
			EN50523Category category, byte shortID, String descriptionEN,
			String descriptionDE) {
		this.productName = productName;
		this.cluster = cluster;
		this.category = category;
		this.shortID = shortID;
		this.descriptionEN = descriptionEN;
		this.descriptionDE = descriptionDE;
		
		this.productTypeID = (short) (
				(((short)cluster.getClusterID()&0xffff) << 8) |
				(((short)category.getCategoryID()&0xffff) << 11) |
				(((short)shortID&0xffff) << 0));
	}

	
	public String getProductName() {
		return productName;
	}

	public EN50523Cluster getCluster() {
		return cluster;
	}

	public EN50523Category getCategory() {
		return category;
	}

	@Deprecated
	public byte getShortID() {
		return shortID;
	}

	public String getDescriptionEN() {
		return descriptionEN;
	}

	public String getDescriptionDE() {
		return descriptionDE;
	}

	public short getProductTypeID() {
		return productTypeID;
	}
	
}
