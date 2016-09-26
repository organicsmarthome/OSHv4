package osh.core.interfaces;

/**
 * This interface is for managing components of the framework, like registry or
 * gui. 
 *
 */
public interface IRealtimeSimulationManagementListener extends IRealTimeSubscriber {

	public void onAfterTimePeriod();
	
	public void onBeforeNextTimePeriod();
	
}
