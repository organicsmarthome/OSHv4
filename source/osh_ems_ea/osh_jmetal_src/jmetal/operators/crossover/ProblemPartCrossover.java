//  SinglePointCrossover.java
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

package jmetal.operators.crossover;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class allows to apply a Single Point crossover operator using two parent
 * solutions.
 */
public class ProblemPartCrossover extends Crossover {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

  /**
   * Valid solution types to apply this operator 
   */
  @SuppressWarnings({ "rawtypes" })
  private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class);

  private Double crossoverProbability_ = null;
  
  private int[] problemPartBoundarys_ = null;

  /**
   * Constructor
   * Creates a new instance of the problem part crossover operator
   */
  public ProblemPartCrossover(HashMap<String, Object> parameters, PseudoRandom pseudoRandom) {
  	super(parameters, pseudoRandom) ;
  	if (parameters.get("probability") != null)
  		crossoverProbability_ = (Double) parameters.get("probability") ;
  	if (parameters.get("problemPartBoundarys") != null)
  		problemPartBoundarys_ = (int[]) parameters.get("problemPartBoundarys") ;  
  } // ProblemPartCrossover


  /**
   * Perform the crossover operation.
   * @param probability Crossover probability
   * @param parent1 The first parent
   * @param parent2 The second parent   
   * @return An array containing the two offsprings
   * @throws JMException
   */
  @SuppressWarnings("rawtypes")
  public Solution[] doCrossover(double probability,	  
          Solution parent1,
          Solution parent2) throws JMException {
	  
//	TODO: Making the crossover work multiple decision variables
	  
    Solution[] offspring = new Solution[2];
    offspring[0] = new Solution(parent1);
    offspring[1] = new Solution(parent2);
    try {
    	
      if (pseudoRandom.randDouble() < probability) {
    	  
    	//aborting for 0 bit solutions
		if (parent1.getNumberOfBits() == 0) {
			return offspring;
		}
    	  
    	  // if no problem part boundary's are provided or if the problem consists of only a single problem part do a SinglePointCrossover
    	  if (problemPartBoundarys_== null || problemPartBoundarys_.length < 2) {
    		  int startswap = pseudoRandom.randInt(0, parent1.getNumberOfBits());
              for (int i = startswap; i <= parent1.getNumberOfBits(); i++) {
                boolean swap = ((Binary) offspring[0].getDecisionVariables()[0]).bits_.get(i);
                ((Binary) offspring[0].getDecisionVariables()[0]).bits_.set(i, ((Binary) offspring[1].getDecisionVariables()[0]).bits_.get(i));
                ((Binary) offspring[1].getDecisionVariables()[0]).bits_.set(i, swap);
              }
    	  } else {
    		  
    		  //otherwise do the crossover
        	  
        	  int startswap, endswap;
    			
    		  //we only need the swap at every second cutting point
        	  for (int i = 1; i < problemPartBoundarys_.length; i+= 2) {
    			startswap = problemPartBoundarys_[i - 1];
    			endswap = problemPartBoundarys_[i];
    			
    			for (int j = startswap; j < endswap; j++) {
    				boolean swap = ((Binary) offspring[0].getDecisionVariables()[0]).bits_.get(j);
          			((Binary) offspring[0].getDecisionVariables()[0]).bits_.set(j, ((Binary) offspring[1].getDecisionVariables()[0]).bits_.get(j));
          			((Binary) offspring[1].getDecisionVariables()[0]).bits_.set(j, swap);
    			}
    		 }
    	  }                 
      }
    } catch (ClassCastException e1) {
      Configuration.logger_.severe("ProblemPartCrossover.doCrossover: Cannot perfom " +
              "ProblemPartCrossover");
      Class cls = java.lang.String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".doCrossover()");
    }
    return offspring;
  } // doCrossover

  /**
   * Executes the operation
   * @param object An object containing an array of two solutions
   * @return An object containing an array with the offSprings
   * @throws JMException
   */
  @SuppressWarnings("rawtypes")
  @Override
  public Object execute(Object object) throws JMException {
    Solution[] parents = (Solution[]) object;

    if (parents.length < 2) {
        Configuration.logger_.severe("ProblemPartCrossover.execute: operator " +
                "needs two parents");
        Class cls = java.lang.String.class;
        String name = cls.getName();
        throw new JMException("Exception in " + name + ".execute()");
    }
    
    if (!(VALID_TYPES.contains(parents[0].getType().getClass())  &&
        VALID_TYPES.contains(parents[1].getType().getClass())) ) {

      Configuration.logger_.severe("ProblemPartCrossover.execute: the solutions " +
              "are not of the right type. The type should be 'Binary' but " +
              parents[0].getType() + " and " +
              parents[1].getType() + " are obtained");

      Class cls = java.lang.String.class;
      String name = cls.getName();
      throw new JMException("Exception in " + name + ".execute()");
    } // if
    
    Solution[] offSpring;
    offSpring = doCrossover(crossoverProbability_,
            parents[0],
            parents[1]);

    //-> Update the offSpring solutions
    for (int i = 0; i < offSpring.length; i++) {
      offSpring[i].setCrowdingDistance(0.0);
      offSpring[i].setRank(0);
    }
    return offSpring;
  } // execute
} // SinglePointCrossover
