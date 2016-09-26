package osh.comdriver.simulation.cruisecontrol.stateviewer;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Till Schuberth
 *
 */
public interface StateViewerListener {
	public void stateViewerRegistryChanged(StateViewerRegistryEnum registry);
	public void stateViewerClassChanged(Class<? extends StateExchange> cls);
}