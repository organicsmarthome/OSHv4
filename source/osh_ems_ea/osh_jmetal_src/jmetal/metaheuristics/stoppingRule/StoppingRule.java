package jmetal.metaheuristics.stoppingRule;

import java.util.Map;

import jmetal.core.Problem;
import jmetal.core.SolutionSet;

public abstract class StoppingRule {
	
	protected String _msg;
	protected Map<String , Object> _parameters;
	
	public StoppingRule(Map<String , Object> parameters) {
		_parameters = parameters;
	} 
	
	public abstract boolean checkIfStop(Problem problem, int generation, SolutionSet currentSortedSolutions);
	
	public String getMsg() {
		return _msg;
	}
}
