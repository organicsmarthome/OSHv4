package jmetal.metaheuristics.stoppingRule;

import java.util.Map;

import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.util.Configuration;
import jmetal.util.JMException;

public class DeltaFitnessStoppingRule extends StoppingRule {
	
	private double minDeltaFitnessPerc;
	private int maxGenerationsDeltaFitnessViolated;
	
	private Double lastGenerationBestFitness = null;
	private int generationsDeltaFitnessViolated = 0;
	
	public DeltaFitnessStoppingRule(Map<String , Object> parameters) throws JMException {
		super(parameters);
		
		if (_parameters.get("minDeltaFitnessPerc") != null)
			minDeltaFitnessPerc = (double) parameters.get("minDeltaFitnessPerc");
		else {
			Configuration.logger_.severe("EvaluationsStoppingRule no minDeltaFitnessPerc in parameters.");
			throw new JMException("no minDeltaFitnessPerc in parameters");
		}
		
		if (_parameters.get("maxGenerationsDeltaFitnessViolated") != null)
			maxGenerationsDeltaFitnessViolated = (int) parameters.get("maxGenerationsDeltaFitnessViolated");
		else {
			Configuration.logger_.severe("EvaluationsStoppingRule no maxGenerationsDeltaFitnessViolated in parameters.");
			throw new JMException("no minGenerations in parameters");
		}
	}
	
	/** checks if the optimisation should stop
	 * 
	 * Optimisation will stop if:
	 * 	- delta Fitness change between generations was smaller then required for the required generations
	 * 
	 * It is assumed that the optimisation cannot return worse fitness values for generation n+1 then for generation n 
	 */
	@Override
	public boolean checkIfStop(Problem problem, int generation, SolutionSet currentSortedSolutions) {
		if (lastGenerationBestFitness == null) {
			lastGenerationBestFitness = currentSortedSolutions.get(0).getObjective(0);
			return false;
		}
		double thisGenerationBestFitness = currentSortedSolutions.get(0).getObjective(0);
		
		double deltaFitness = 0;
		
		deltaFitness = Math.abs((Math.abs(thisGenerationBestFitness) - Math.abs(lastGenerationBestFitness)) / Math.abs(lastGenerationBestFitness));
		
		
		if (deltaFitness >= minDeltaFitnessPerc) {
			generationsDeltaFitnessViolated = 0;
			lastGenerationBestFitness = thisGenerationBestFitness;
			return false;
		} else {
			generationsDeltaFitnessViolated++;
			if (generationsDeltaFitnessViolated < maxGenerationsDeltaFitnessViolated) 
				return false;
			else {
				_msg = "Optimisation stopped after violating minDeltaFitness for " + generationsDeltaFitnessViolated + " generations.";
				return true;
			}			
		}		
	}
}
