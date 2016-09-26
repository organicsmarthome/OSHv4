//  TwoPointsCrossover.java
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

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * This class allows to apply a shuffled crossover operator on binary solutions using two parent
 * solutions. 
 * NOTE: the type of the solutions must be Binary
 */
@SuppressWarnings({"rawtypes"})
public class ShuffledBinaryCrossover extends Crossover {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

  /**
   * Valid solution types to apply this operator 
   */  
  private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class) ;

  private Double crossoverProbability_ = null;

	/**
	 * Constructor
	 * Creates a new instance of the shuffled crossover operator
	 */
	public ShuffledBinaryCrossover(HashMap<String, Object> parameters, PseudoRandom pseudoRandom) {
		super(parameters, pseudoRandom) ;
		
  	if (parameters.get("probability") != null)
  		crossoverProbability_ = (Double) parameters.get("probability") ;
	} // ShuffledBinaryCrossover


	/**
	 * Constructor
	 * @param A properties containing the Operator parameters
	 * Creates a new instance of the n point crossover operator
	 */
	//public ShuffledBinaryCrossover(Properties properties) {
	//	this();
	//}


	/**
	 * Perform the crossover operation
	 * @param probability Crossover probability
	 * @param parent1 The first parent
	 * @param parent2 The second parent
	 * @return Two offspring solutions
	 * @throws JMException 
	 */
	public Solution[] doCrossover(double probability,
			Solution parent1, 
			Solution parent2) throws JMException {
		//TODO: Making the crossover work for multiple decision variables
		
		Solution [] offspring = new Solution[2];

		offspring[0] = new Solution(parent1);
		offspring[1] = new Solution(parent2);

		if (parent1.getType().getClass() == BinarySolutionType.class) {
				if (pseudoRandom.randDouble() < probability) {
					
					
					//Step 1: Generate the permutation array
					int numberOfBits = parent1.getNumberOfBits();
					
					//aborting for 0 bit solutions
					if (numberOfBits == 0) {
						return offspring;
					}					
					
					int[] permutation = new int[numberOfBits];
					
					for (int i = 1; i < numberOfBits; i++) {
						permutation[i] = i;
					}

					//knuth shuffle
				    for (int i = 0; i < numberOfBits; i++) {
				        // choose index uniformly in [i, N-1]
				      	int r = pseudoRandom.randInt(i, numberOfBits);
				        int swap = permutation[r];
				        permutation[r] = permutation[i];
				        permutation[i] = swap;
				    }
					
					//Step 2: Shuffle the parent solution according to the permutation array
					
					BitSet shuffledOffspring1 = new BitSet(numberOfBits);
					BitSet shuffledOffspring2 = new BitSet(numberOfBits);
					
					for (int i = 0; i < numberOfBits; i++) {
						shuffledOffspring1.set(i, ((Binary) offspring[0].getDecisionVariables()[0]).bits_.get(permutation[i]));
						shuffledOffspring2.set(i, ((Binary) offspring[1].getDecisionVariables()[0]).bits_.get(permutation[i]));
					}
					
					//Step 3: Do a single point crossover on the shuffled binarys
					
					int crossoverPoint = pseudoRandom.randInt(0, numberOfBits);					
					
					for (int i = crossoverPoint; i < numberOfBits; i++) {
						boolean swap = shuffledOffspring1.get(i);
						shuffledOffspring1.set(i, shuffledOffspring2.get(i));
						shuffledOffspring2.set(i, swap);				
					}
					
					//Step 4: Unshuffle the offspring
					
					for (int i = 0; i < numberOfBits; i++) {
						((Binary) offspring[0].getDecisionVariables()[0]).bits_.set(permutation[i], shuffledOffspring1.get(i));
						((Binary) offspring[1].getDecisionVariables()[0]).bits_.set(permutation[i], shuffledOffspring2.get(i));
					}
					
				} // if 
			} // if
			else
			{
				Configuration.logger_.severe("ShuffledBinaryCrossover.doCrossover: invalid " +
						"type" + 
						parent1.getDecisionVariables()[0].getVariableType());
				Class cls = java.lang.String.class;
				String name = cls.getName(); 
				throw new JMException("Exception in " + name + ".doCrossover()") ; 
			}

		return offspring;                                                                                      
	} // makeCrossover

	/**
	 * Executes the operation
	 * @param object An object containing an array of two solutions 
	 * @return An object containing an array with the offSprings
	 * @throws JMException 
	 */
	@Override
	@SuppressWarnings({ })
	public Object execute(Object object) throws JMException {
		Solution [] parents = (Solution [])object;

    if (!(VALID_TYPES.contains(parents[0].getType().getClass())  &&
        VALID_TYPES.contains(parents[1].getType().getClass())) ) {

			Configuration.logger_.severe("ShuffledBinaryCrossover.execute: the solutions " +
					"are not of the right type. The type should be 'Binary', but " +
					parents[0].getType() + " and " + 
					parents[1].getType() + " are obtained");
			
			Class cls = java.lang.String.class;
		    String name = cls.getName();
		    throw new JMException("Exception in " + name + ".execute()");
		} // if 

		if (parents.length < 2)
		{
			Configuration.logger_.severe("ShuffledBinaryCrossover.execute: operator needs two " +
			"parents");
			Class cls = java.lang.String.class;
			String name = cls.getName(); 
			throw new JMException("Exception in " + name + ".execute()") ;      
		}

		Solution [] offspring = doCrossover(crossoverProbability_,
				parents[0],
				parents[1]);

		return offspring; 
	} // execute

} // ShuffledBinaryCrossover
