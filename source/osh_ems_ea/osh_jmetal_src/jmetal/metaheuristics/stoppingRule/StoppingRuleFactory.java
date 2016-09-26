package jmetal.metaheuristics.stoppingRule;

import java.util.HashMap;

import jmetal.util.Configuration;
import jmetal.util.JMException;

public class StoppingRuleFactory {

	 /**
	   * Gets a stopping rule through its name.
	   * @param name Name of the stopping rule
	   * @return The operator
	   */
	  @SuppressWarnings({ "unchecked", "rawtypes" })
	public static StoppingRule getStoppingRule(String name, HashMap parameters) throws JMException {
	    if (name.equalsIgnoreCase("EvaluationsStoppingRule"))
	      return new EvaluationsStoppingRule(parameters);
	    else if (name.equalsIgnoreCase("DeltaFitnessStoppingRule"))
	        return new DeltaFitnessStoppingRule(parameters);
	    else {
	      Configuration.logger_.severe("CrossoverFactory.getCrossoverOperator. " +
	          "Operator '" + name + "' not found ");
	      throw new JMException("Exception in " + name + ".getCrossoverOperator()") ;
	    } // else        
	  } // getCrossoverOperator
}
