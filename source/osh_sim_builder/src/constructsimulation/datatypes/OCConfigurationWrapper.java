package constructsimulation.datatypes;

import java.util.UUID;

import osh.configuration.oc.GAConfiguration;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class OCConfigurationWrapper {
	
	public long optimizationMainRandomSeed;
	
	public String globalObserverClassName;
	public String globalControllerClassName;
	
	public int epsOptimizationObjective;	
	public int plsOptimizationObjective;
	public int varOptimizationObjective;
	
	public double upperOverlimitFactor;
	public double lowerOverlimitFactor;
	
	public String h0Filename;

	public GAConfiguration gaConfiguration;
	
	public int stepSize;
	
	public UUID hotWaterTankUUID;

	public OCConfigurationWrapper(
			long optimizationMainRandomSeed,
			
			String globalObserverClassName,
			String globalControllerClassName,
			
			int epsOptimizationObjective,
			
			int plsOptimizationObjective,
			int varOptimizationObjective,
			double upperOverlimitFactor,
			double lowerOverlimitFactor,
			
			GAConfiguration gaConfiguration,
			int stepSize,
			UUID hotWaterTankUUID
			) {

		this.optimizationMainRandomSeed = optimizationMainRandomSeed;
		
		this.globalObserverClassName = globalObserverClassName;
		this.globalControllerClassName = globalControllerClassName;
		
		this.epsOptimizationObjective = epsOptimizationObjective;
		
		this.plsOptimizationObjective = plsOptimizationObjective;
		this.varOptimizationObjective = varOptimizationObjective;
		this.upperOverlimitFactor = upperOverlimitFactor;
		this.lowerOverlimitFactor = lowerOverlimitFactor;
		
		
		this.gaConfiguration = gaConfiguration;
		
		this.stepSize = stepSize;
		
		this.hotWaterTankUUID = hotWaterTankUUID;
	}
}
