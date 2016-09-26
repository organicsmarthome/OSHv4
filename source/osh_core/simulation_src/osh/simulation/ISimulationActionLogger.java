package osh.simulation;

import osh.simulation.screenplay.SubjectAction;

/**
 * Interface for logging Simulation Device actions
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public interface ISimulationActionLogger {
	public void logAction(SubjectAction action);
}
