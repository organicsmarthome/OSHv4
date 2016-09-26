package constructsimulation.datatypes;

import osh.simulation.screenplay.ScreenplayType;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ScreenplayWrapper {
	
	public ScreenplayType screenplayType;
	
	public String iDeviceScreenplayDirectory;
	
	public long spsDuration;
	public int chosenPriceCurve;
	
	public ScreenplayWrapper(
			ScreenplayType screenplayType,
			String iDeviceScreenplayDirectory,
			long spsDuration,
			int chosenPriceCurve) {

		this.screenplayType = screenplayType;
		
		this.iDeviceScreenplayDirectory = iDeviceScreenplayDirectory;
		
		this.spsDuration = spsDuration;
		this.chosenPriceCurve = chosenPriceCurve;
	}

}
