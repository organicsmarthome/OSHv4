package constructMultiple;

import osh.datatypes.power.LoadProfileCompressionTypes;

public class CompressionConfiguration {

	/**
	 * @param compressionType
	 * @param compressionValue
	 */
	public CompressionConfiguration(LoadProfileCompressionTypes compressionType, int compressionValue) {
		super();
		this.compressionType = compressionType;
		this.compressionValue = compressionValue;
	}
	public LoadProfileCompressionTypes compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
	public int compressionValue = 100;
	
	public String toShortName() {
		if (compressionType == LoadProfileCompressionTypes.DISCONTINUITIES)
			return "disc-" + compressionValue + "W";
		else
			return "slots-" + compressionValue + "s";
	}
	
}
