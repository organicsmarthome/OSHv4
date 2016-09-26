package osh.simulation;

import java.util.Collection;
import java.util.UUID;

import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;

/**
 *@author Florian Allerding, Ingo Mauser
 */
public interface ISimulationSubject {
	
	/**
	 * is invoked when the complete simulation environment has been set up 
	 */
	public void onSimulationIsUp() throws SimulationSubjectException;
	
	/**
	 * is invoked before every simulation tick by the simulation engine
	 */
	public void onSimulationPreTickHook() throws SimulationSubjectException;

	/**
	 * is invoked by the SimulationEngine on every time tick to synchronize the subjects<br>
	 * 1. trigger onNextTimeTick()<br>
	 * 2. do action handling
	 */
	public void triggerSubject() throws SimulationSubjectException;
	
	/**
	 * is invoked after every simulation tick by the simulation engine
	 */
	public void onSimulationPostTickHook() throws SimulationSubjectException;
	
	
	// ### ACTION related ###
	
	/**
	 * delete all actions from the list 
	 */
	public void flushActions();
	
	/**
	 * Sets an action for this simulation subject
	 * @param actions
	 */
	public void setAction(SubjectAction action);
	
	/**
	 * gets all actions for a subject
	 * @return
	 */
	public Collection<SubjectAction> getActions();
	
	/**
	 * @param nextAction
	 * is invoked when the subject has to do the action "nextAction"
	 */
	public void performNextAction(SubjectAction nextAction);
	
	
	// ### GETTERS and SETTERS ###
	
	public void setSimulationEngine(BuildingSimulationEngine simulationEngine);

	public UUID getDeviceID();
	
	public ISimulationSubject getAppendingSubject(UUID SubjectID);

	public void setSimulationActionLogger(ISimulationActionLogger simulationLogger);
	
}
