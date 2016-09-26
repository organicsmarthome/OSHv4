package osh.comdriver.simulation.cruisecontrol.stateviewer;

import java.util.Comparator;

/**
 * 
 * @author Till Schuberth
 *
 */
class ClassNameComparator implements Comparator<Class<?>> {

	@Override
	public int compare(Class<?> o1, Class<?> o2) {
		return o1.getName().compareTo(o2.getName());
	}
	
}