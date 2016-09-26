package osh.simulation;

import java.util.HashSet;
import java.util.Set;

import osh.eal.hal.HALRealTimeDriver;
import osh.eal.hal.exceptions.HALException;
import osh.registry.DriverRegistry;
import osh.registry.ComRegistry;
import osh.registry.OCRegistry;
import osh.registry.Registry;
import osh.simulation.exception.SimulationEngineException;

/**
 * 
 * @author Florian Allerding, Till Schuberth, Ingo Mauser
 *
 */
public abstract class SimulationEngine {
	
	//LOGGING
	protected SimulationResults oshSimulationResults;
	
	//TIMER
	protected HALRealTimeDriver timerdriver;
	
	//COMMUNICATION
	protected ComRegistry comRegistry;
	protected OCRegistry ocRegistry;
	protected DriverRegistry driverRegistry;
	
	private long currentSimulationTick = -1;
	private long simulationDuration;

	private Set<SimulationEngine> subengines = new HashSet<SimulationEngine>();
	
	
	// ## IMPORTANT GETTERS AND SETTERS ##

	// # TIMER DRIVER #
	public void assignTimerDriver(HALRealTimeDriver timerDriver) {
		this.timerdriver = timerDriver;
	}

	// # REGISTRIES #
	// EXTERNAL REGISTRY
	public void assignComRegistry(ComRegistry externalRegistry){
		this.comRegistry = externalRegistry;
	}
	// OC REGISTRY
	public void assignOCRegistry(OCRegistry ocRegistry){
		this.ocRegistry = ocRegistry;
	}
	// DRIVER REGISTRY
	public void assignDriverRegistry(DriverRegistry driverRegistry){
		this.driverRegistry = driverRegistry;
	}
	
	
	// # SUB SIMULATION ENGINES #
	// add engine that depends on this engine
	public void addSubSimulationEngine(SimulationEngine simengine) {
		subengines.add(simengine);
	}
	// remove engine that depends on this engine
	public void removeSubSimulationEngine(SimulationEngine simengine) {
		subengines.remove(simengine);
	}
	
	// ## LOGIC ##
	
	
	/**
	 * will call every simulation Engine that the simulation setup is complete.
	 * So every ISimulationSubject will be notified, too
	 * @throws SimulationEngineException 
	 */
	public void notifySimulationIsUp() throws SimulationEngineException{
		// notify main engine that simulation is up
		this.notifyLocalEngineOnSimulationIsUp();
		
		// notify sub engines that simulation is up
		for(SimulationEngine simulationEngine: subengines){
			simulationEngine.notifySimulationIsUp();
		}
	}
	
	abstract protected void notifyLocalEngineOnSimulationIsUp() throws SimulationEngineException;
 
	/**
	 * 
	 * @param currentTick
	 * @param doSimulation if true, do a full simulation with a call to
	 * 			simulateNextTimeTick, if false, only update timerdriver and empty all
	 * 			queues.
	 * @throws SimulationEngineException
	 */
	private void internalSimulateNextTimeTick(long currentTick, boolean doSimulation) throws SimulationEngineException {
		//update realtimeDriver
		//doNextSimulationTickTimer:
		try {
			if (this.timerdriver != null) this.timerdriver.updateTimer(currentTick);
		} 
		catch (HALException e) {
			throw new SimulationEngineException(e);
		}

		//do the simulation in subengines
		if (doSimulation) {
			simulateNextTimeTick(currentTick);
			for (SimulationEngine e : subengines) {
				e.triggerEngine();
			}
		}

		//empty all queues
		boolean queueswereempty;
		do {
			queueswereempty = true;
			queueswereempty &= doSimulateNextTimeTickQueues(currentTick); // &= is AND
			for (SimulationEngine e : subengines) {
				queueswereempty &= e.doSimulateNextTimeTickQueues(currentTick);
			}
		} while (!queueswereempty);
	}
	
	
	/**
	 * this function is to be called from inside internalSimulateNextTimeTick.
	 * 
	 * @param currentTick
	 * @throws SimulationEngineException 
	 */
	private boolean doSimulateNextTimeTickQueues(long currentTick) throws SimulationEngineException {
		boolean allqueueswereempty = true;
		
		allqueueswereempty &= processRegistry(this.comRegistry);
		allqueueswereempty &= processRegistry(this.ocRegistry);
		allqueueswereempty &= processRegistry(this.driverRegistry);
		
		return allqueueswereempty;
	}

	private boolean processRegistry(Registry registry) throws SimulationEngineException {
		// empty all event/state queues
		if (registry != null) {
			registry.flushAllQueues();
		}
		else {
			System.out.println("ERROR: No Registry available!");
			System.exit(0); // shutdown
			return true;
		}
		
		return registry.areAllQueuesEmpty();
	}
	
	/**
	 * Simulate next time tick
	 * @throws SimulationEngineException
	 */
	protected abstract void simulateNextTimeTick(long currentTick) throws SimulationEngineException;

	/**
	 * start the simulation based on an external clock This function is
	 * deprecated, because I can't see that the function does what it should do.
	 * If I'm wrong, feel free to delete the tag.
	 * 
	 * IMA: Simulation with external clock (timerDriver), e.g. combination of
	 * real house and simulated houses
	 * 
	 * TODO: simplify implementation.
	 * 
	 * @throws SimulationEngineException
	 */
	@Deprecated
	public void runSimulationByExternalClock(int startTime) throws SimulationEngineException {
		internalSimulateNextTimeTick(startTime, false);
	}
	
	/**
	 * Trigger the simulation by an external clock to simulate the next step
	 * @throws SimulationEngineException
	 */
	public void triggerEngine() throws SimulationEngineException {
		//next tick
		++this.currentSimulationTick;

		//simulate it
		internalSimulateNextTimeTick(currentSimulationTick, true);
	}
	
	public void setSimulationDuration(int duration) {
		simulationDuration = duration;
	}
	
	public SimulationResults triggerEngineWithResult() throws SimulationEngineException {
		//next tick
		++this.currentSimulationTick;

		//simulate it
		internalSimulateNextTimeTick(this.currentSimulationTick, true);
		
		return oshSimulationResults;
	}
	
	public SimulationResults runSimulationForTick(int tick) throws SimulationEngineException {
		internalSimulateNextTimeTick(tick, true);
		return oshSimulationResults;
	}
	
	/**
	 * start the simulation with a given numberOfTicks based on the internal clock
	 * @param numberOfTicks
	 * @throws SimulationEngineException
	 */
	public SimulationResults runSimulation(long numberOfTicks) throws SimulationEngineException {
		simulationDuration = numberOfTicks;
		for (int currentTick = 0; currentTick < numberOfTicks; currentTick++ ) {
			internalSimulateNextTimeTick(currentTick, true);
		}
		return oshSimulationResults;
	}

	/**
	 * Reset the simulation timer to zero. Necessary when you want to run several simulations
	 * beginning at the same start time '0'
	 * @throws SimulationEngineException 
	 */
	public void resetSimulationTimer() throws SimulationEngineException {
		internalSimulateNextTimeTick(0, false);
	}
	
	/**
	 * You can set a specific start time for the simulation.
	 * Normally you don't need that
	 * @param startTimeTick
	 * @throws SimulationEngineException 
	 */
	public void setSimulationTimerTo(int startTimeTick) throws SimulationEngineException{
		internalSimulateNextTimeTick(startTimeTick, false);
	}
	
	/**
	 * Required for appliances (to know whether it is the last day)
	 * @return
	 */
	public long getSimulationDuration() {
		return simulationDuration;
	}

}
