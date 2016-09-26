package constructMultiple;

public class PVConfiguration {
	

	public String pvComplexPowerMax = "10000";
	public String pvCosPhiMax = "-0.8";
	public int pvNominalPower = 2000;
	public boolean usePVRealESHL = false;
	public boolean usePVRealHOLL = true;
	
	public PVConfiguration(String pvComplexPowerMax, String pvCosPhiMax,
			int pvNominalPower, boolean usePVRealESHL, boolean usePVRealHOLL) {
		this.pvComplexPowerMax = pvComplexPowerMax;
		this.pvCosPhiMax = pvCosPhiMax;
		this.pvNominalPower = pvNominalPower;
		this.usePVRealESHL = usePVRealESHL;
		this.usePVRealHOLL = usePVRealHOLL;
	}
	
	public String toShortName() {
		if (!usePVRealESHL && !usePVRealHOLL)
			return "npv";
		String name = "pv";
		name += usePVRealESHL ? "ESHL" : "HOLL";
		name += pvNominalPower;
		return name;				
	}

}
