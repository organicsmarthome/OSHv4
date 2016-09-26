package jmetal.metaheuristics.stoppingRule;

import java.util.Map;

import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.util.Configuration;
import jmetal.util.JMException;

public class EvaluationsStoppingRule extends StoppingRule {
	
	private int populationSize;
	private int maxEvaluations;
	
	public EvaluationsStoppingRule(Map<String , Object> parameters) throws JMException {
		super(parameters);
		
		if (_parameters.get("populationSize") != null)
			populationSize = (int) parameters.get("populationSize");
		else {
			Configuration.logger_.severe("EvaluationsStoppingRule no populationSize in parameters.");
			throw new JMException("no populationSize in parameters");
		}
		
		if (_parameters.get("maxEvaluations") != null)
			maxEvaluations = (int) parameters.get("maxEvaluations");
		else {
			Configuration.logger_.severe("EvaluationsStoppingRule no maxEvaluations in parameters.");
			throw new JMException("no maxEvaluations in parameters");
		}
	}
	
	@Override
	public boolean checkIfStop(Problem problem, int generation, SolutionSet currentSortedSolutions) {
		if ((generation + 1) * populationSize >= maxEvaluations) {
			_msg = "Optimisation stopped after reaching max evaluations: " + ((generation + 1) * populationSize);
			return true;
		}		
		
		return false;
	}
}
