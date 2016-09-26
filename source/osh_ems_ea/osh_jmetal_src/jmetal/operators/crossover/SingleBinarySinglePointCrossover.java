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
 * This class allows to apply a n points crossover operator using two parent
 * solutions. 
 * NOTE: the type of the solutions must be Binary
 */
@SuppressWarnings({"rawtypes"})
public class SingleBinarySinglePointCrossover extends Crossover {

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
	 * Creates a new instance of the n point crossover operator
	 */
	public SingleBinarySinglePointCrossover(HashMap<String, Object> parameters, PseudoRandom pseudoRandom) {
		super(parameters, pseudoRandom) ;
		
  	if (parameters.get("probability") != null)
  		crossoverProbability_ = (Double) parameters.get("probability") ;
	} // SingleBinarySinglePointCrossover

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

		Solution [] offspring = new Solution[2];

		offspring[0] = new Solution(parent1);
		offspring[1] = new Solution(parent2);

		if (parent1.getType().getClass() == BinarySolutionType.class) {
			if (pseudoRandom.randDouble() < probability) {

				int numberOfBits = ((Binary) parent1.getDecisionVariables()[0]).getNumberOfBits();

				//aborting for 0 bit solutions
				if (numberOfBits == 0) {
					return offspring;
				}
				
				//Generate the random cutting point
				int crossoverPoint = pseudoRandom.randInt(0, numberOfBits - 1);

				//edge cases
				if (crossoverPoint == numberOfBits)
					return offspring;
				if (crossoverPoint == 0) {
					Solution tmp = offspring[0];
					offspring[0] = offspring[1];
					offspring[1] = tmp;
					return offspring;
				}					
				
				//using bitset operations is faster then just setting bits one after another
				BitSet tmp1 = new BitSet();
				tmp1.or(((Binary) offspring[0].getDecisionVariables()[0]).bits_);
				tmp1.clear(0, crossoverPoint);
				BitSet tmp2 = new BitSet();
				tmp2.or(((Binary) offspring[1].getDecisionVariables()[0]).bits_);
				tmp2.clear(0, crossoverPoint);
				
				((Binary) offspring[0].getDecisionVariables()[0]).bits_.clear(crossoverPoint, numberOfBits);
				((Binary) offspring[0].getDecisionVariables()[0]).bits_.or(tmp2);
				
				((Binary) offspring[1].getDecisionVariables()[0]).bits_.clear(crossoverPoint, numberOfBits);
				((Binary) offspring[1].getDecisionVariables()[0]).bits_.or(tmp1);				
			} // if 
		} // if
		else
		{
			Configuration.logger_.severe("SingleBinarySinglePointCrossover.doCrossover: invalid " +
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

			Configuration.logger_.severe("SingleBinarySinglePointCrossover.execute: the solutions " +
					"are not of the right type. The type should be 'Binary', but " +
					parents[0].getType() + " and " + 
					parents[1].getType() + " are obtained");

			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if 

		if (parents.length < 2)
		{
			Configuration.logger_.severe("SingleBinarySinglePointCrossover.execute: operator needs two " +
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

}
