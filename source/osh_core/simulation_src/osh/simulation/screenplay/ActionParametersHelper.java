package osh.simulation.screenplay;

import java.util.List;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ActionParametersHelper {

	public static String getValueForParameterOfParameters(
			PerformAction action, 
			String parametersName, 
			String parameterName) {
		String s = null;
		
		List<ActionParameters> parametersList = action.getActionParameterCollection();
		for (ActionParameters aps : parametersList) {
			if (aps.getParametersName().equals(parametersName)) {
				for (ActionParameter ap : aps.getParameter()) {
					if (ap.getName().equals(parameterName)) {
						s= ap.getValue();
					}
				}
			}
		}
		
		return s;
	}
	
}
