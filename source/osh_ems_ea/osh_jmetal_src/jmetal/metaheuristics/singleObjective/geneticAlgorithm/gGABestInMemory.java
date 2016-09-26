//  gGA.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.metaheuristics.singleObjective.geneticAlgorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Comparator;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.encodings.variable.Real;
import jmetal.util.JMException;
import jmetal.util.comparators.ObjectiveComparator;

/**
 * Class implementing a generational genetic algorithm
 */
public class gGABestInMemory extends Algorithm {

	private static final long serialVersionUID = 1L;
	private boolean showDebugMessages;
	private Variable[] bestInMemory;
	
	private boolean writePopulationsToFile;
	
	/**
	 * Constructor Create a new GGA instance.
	 * 
	 * @param problem Problem to solve.
	 */
	public gGABestInMemory(
			Problem problem, 
			Real[] bestInMemory,
			boolean showDebugMessages,
			boolean writePopulationsToFile) {
		super(problem);
		
		this.bestInMemory = bestInMemory;
		this.showDebugMessages = showDebugMessages;
		
		this.writePopulationsToFile = writePopulationsToFile;
	} // GGA

	/**
	 * Execute the GGA algorithm
	 * 
	 * @throws JMException
	 */
	@SuppressWarnings("resource")
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		
		String folderName = "logs/" + System.currentTimeMillis() / 1000 + "/";
		String outputFileName = folderName + "popLog.csv";
		PrintWriter pw = null;
		
		if (writePopulationsToFile) {
			try {
				File theDir = new File(folderName);

				  // if the directory does not exist, create it
				  if (!theDir.exists()) {
				    theDir.mkdir();  
				  }
				
				pw = new PrintWriter(new File(outputFileName));
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		
		// int generation = 0;
		int populationSize;
		int maxEvaluations;
		int evaluations;

		SolutionSet population;
		SolutionSet offspringPopulation;

		Operator mutationOperator;
		Operator crossoverOperator;
		Operator selectionOperator;

		@SuppressWarnings("rawtypes")
		Comparator comparator;
		comparator = new ObjectiveComparator(0); // Single objective comparator

		// Read the params
		populationSize = ((Integer) this.getInputParameter("populationSize"))
				.intValue();
		maxEvaluations = ((Integer) this.getInputParameter("maxEvaluations"))
				.intValue();

		// Initialize the variables
		population = new SolutionSet(populationSize);
		offspringPopulation = new SolutionSet(populationSize);

		evaluations = 0;

		// Read the operators
		mutationOperator = this.operators_.get("mutation");
		crossoverOperator = this.operators_.get("crossover");
		selectionOperator = this.operators_.get("selection");

		// Create the initial population
		Solution newIndividual;
		Solution bestIndividualInMemory = null;

		bestIndividualInMemory = new Solution(problem_, bestInMemory);
		problem_.evaluate(bestIndividualInMemory);
		evaluations++;
		population.add(bestIndividualInMemory);
		if (pw != null) {
			String s = "";
			for (Variable v : bestIndividualInMemory.getDecisionVariables()) {
				Real r = (Real) v;
				s = s + r.getValue() + ";";
			}
			s = s + bestIndividualInMemory.getObjective(0);
			pw.println(s);
			pw.flush();
		}
		
		for (int i = 0; i < populationSize - 1; i++) {
			newIndividual = new Solution(problem_);
			problem_.evaluate(newIndividual);
			evaluations++;
			population.add(newIndividual);
			if (pw != null) {
				String s = "";
				for (Variable v : newIndividual.getDecisionVariables()) {
					Real r = (Real) v;
					s = s + r.getValue() + ";";
				}
				s = s + newIndividual.getObjective(0);
				pw.println(s);
				pw.flush();
			}
		} // for

		// Sort population
		population.sort(comparator);
		do {
			if ((evaluations % 1) == 0) {
				if (showDebugMessages) {
					// Evaluations; Fitness; Populationsize; Generations;
					// MutationProbability; CrossoverProbability
					System.out.println(evaluations + "; "
							+ population.get(0).getObjective(0) + "; "
							+ population.get(0).getDecisionVariables()[0]
							+ "; "
							+ population.get(0).getDecisionVariables()[1]
							+ "; "
							+ population.get(0).getDecisionVariables()[2]);
				}
			} //

			// Copy the best two individuals to the offspring population
			offspringPopulation.add(new Solution(population.get(0)));
			offspringPopulation.add(new Solution(population.get(1)));

			evaluations += 2; // To get back right number of Evaluations

			// Reproductive cycle
			for (int i = 0; i < ((populationSize / 2) - 1); i++) {
				// Selection
				Solution[] parents = new Solution[2];
				
				Object selected = selectionOperator.execute(population);
				
				if (selected instanceof Solution[]) 
					parents = (Solution[]) selected;
				else if (selected instanceof Solution) {
					parents[0] = (Solution) selected;
					parents[1] = (Solution) selectionOperator.execute(population);
				} else {
					throw new JMException("Selection operator does not return a Solution object");
				}

				// Crossover
				Solution[] offspring = (Solution[]) crossoverOperator
						.execute(parents);

				// <Workaround for 0 bits>
				// int totalNumberOfBits = 0;
				// for (int v = 0; v < parents[0].getDecisionVariables().length;
				// v++) {
				// totalNumberOfBits +=
				// ((Binary)
				// parents[0].getDecisionVariables()[v]).getNumberOfBits();
				// }
				//
				// Solution [] offspring;
				// if (totalNumberOfBits == 0) {
				// offspring = new Solution[2];
				// offspring[0] = parents[0];
				// offspring[1] = parents[1];
				// }
				// else {
				// offspring = (Solution []) crossoverOperator.execute(parents);
				// }

				// Mutation
				mutationOperator.execute(offspring[0]);
				mutationOperator.execute(offspring[1]);

				// Evaluation of the new individual
				problem_.evaluate(offspring[0]);
				problem_.evaluate(offspring[1]);

				evaluations += 2;

				// Replacement: the two new individuals are inserted in the
				// offspring
				// population
				offspringPopulation.add(offspring[0]);
				offspringPopulation.add(offspring[1]);

			} // for

			// The offspring population becomes the new current population
			population.clear();
			for (int i = 0; i < populationSize; i++) {
				population.add(offspringPopulation.get(i));
				if (pw != null) {
					String s = "";
					for (Variable v : offspringPopulation.get(i).getDecisionVariables()) {
						Real r = (Real) v;
						s = s + r.getValue() + ";";
					}
					s = s + offspringPopulation.get(i).getObjective(0);
					pw.println(s);
					pw.flush();
				}
			}
			offspringPopulation.clear();
			population.sort(comparator);
			// generation++;
			// if (showDebugMessages) System.out.println("Generation: " +
			// generation ) ; //for Debugging only
		} while (evaluations < maxEvaluations - 1); // while

		// Return a population with the best individual
		SolutionSet resultPopulation = new SolutionSet(1);
		resultPopulation.add(population.get(0));

		if (pw != null) {
			pw.close();
		}
		
		if (showDebugMessages)
			System.out.println("Evaluations: "
					+ (evaluations + " Fitness: " + population.get(0)
							.getObjective(0)));

		return resultPopulation;
	} // execute

} // gGA