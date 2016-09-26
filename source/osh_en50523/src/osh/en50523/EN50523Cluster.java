package osh.en50523;

/**
 * 3-bit HEX as BYTE (0 to 7)
 * @author Ingo Mauser
 *
 */
public enum EN50523Cluster {
	
	ALL			((byte) 0x3, "all clusters", "allen Clustern gemeinsam"),
	HOUSEHOLD	((byte) 0x6, "household cluster", "Haushaltscluster");
	
	private byte clusterID;
	private String descriptionEN;
	private String descriptionDE;
	
	
	private EN50523Cluster(byte clusterID, String descriptionEN, String descriptionDE) {
		this.clusterID = clusterID;
		this.descriptionEN = descriptionEN;
		this.descriptionDE = descriptionDE;
	}


	public byte getClusterID() {
		return clusterID;
	}


	public String getDescriptionEN() {
		return descriptionEN;
	}


	public String getDescriptionDE() {
		return descriptionDE;
	}
	
	
	
}
