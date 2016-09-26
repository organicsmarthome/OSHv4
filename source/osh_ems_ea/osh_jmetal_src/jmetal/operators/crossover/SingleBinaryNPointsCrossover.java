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
import java.util.HashSet;
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
public class SingleBinaryNPointsCrossover extends Crossover {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Valid solution types to apply this operator 
	 */  
	private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class) ;

	private Double crossoverProbability_ = null;

	private Integer points_ = null;

	/**
	 * Constructor
	 * Creates a new instance of the n point crossover operator
	 */
	public SingleBinaryNPointsCrossover(HashMap<String, Object> parameters, PseudoRandom pseudoRandom) {
		super(parameters, pseudoRandom) ;

		if (parameters.get("probability") != null)
			crossoverProbability_ = (Double) parameters.get("probability") ;
		if (parameters.get("points") != null)
			points_ = (Integer) parameters.get("points");
	} // SingleBinaryNPointsCrossover


	/**
	 * Constructor
	 * @param A properties containing the Operator parameters
	 * Creates a new instance of the n point crossover operator
	 */
	//public SingleBinaryNPointsCrossover(Properties properties) {
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
			int points,
			Solution parent1, 
			Solution parent2) throws JMException {
		//TODO: Making the crossover work for multiple decision variables

		Solution [] offspring = new Solution[2];

		offspring[0] = new Solution(parent1);
		offspring[1] = new Solution(parent2);

		if (parent1.getType().getClass() == BinarySolutionType.class) {
			if (pseudoRandom.randDouble() < probability) {		
				

				int numberOfBits = parent1.getNumberOfBits();

				//aborting for 0 bit solutions
				if (numberOfBits == 0) {
					return offspring;
				}					
				//adjusting for small bitstrings
				if (numberOfBits < points) {
					points = numberOfBits;
				}

				//Step 1: Generate the random cutting points and sort them
				HashSet<Integer> xoPoints = new HashSet<Integer>(points);
				Integer[] crossoverPoints = new Integer[points + 1];					

				while(xoPoints.size() != points) {
					while(!xoPoints.add(pseudoRandom.randInt(0, numberOfBits - 1)));
				}
				
				//adding start and finish for easier iterating
				//if start and/or end are selected as crossoverPoints then this will effectively reduce the number of xo-points 
				xoPoints.add(0);
				xoPoints.add(numberOfBits);

				crossoverPoints = xoPoints.toArray(crossoverPoints);
				
				Arrays.sort(crossoverPoints);
				
				//Step 2: Do the crossover
				//using bitset operations is faster then just setting bits one after another
				BitSet tmp1 = new BitSet();
				tmp1.or(((Binary) offspring[0].getDecisionVariables()[0]).bits_);
				BitSet tmp2 = new BitSet();
				tmp2.or(((Binary) offspring[1].getDecisionVariables()[0]).bits_);
				
				for (int i = 1; i < crossoverPoints.length; i++) {
					if (i % 2 == 0) {
						((Binary) offspring[0].getDecisionVariables()[0]).bits_.clear(crossoverPoints[i - 1], crossoverPoints[i]);
						((Binary) offspring[1].getDecisionVariables()[0]).bits_.clear(crossoverPoints[i - 1], crossoverPoints[i]);
					} else {
						tmp1.clear(crossoverPoints[i - 1], crossoverPoints[i]);
						tmp2.clear(crossoverPoints[i - 1], crossoverPoints[i]);
					}
				}
				((Binary) offspring[0].getDecisionVariables()[0]).bits_.or(tmp2);
				((Binary) offspring[1].getDecisionVariables()[0]).bits_.or(tmp1);				
				
			} // if 
		} // if
		else
		{
			Configuration.logger_.severe("SingleBinaryNPointsCrossover.doCrossover: invalid " +
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

			Configuration.logger_.severe("SingleBinaryNPointsCrossover.execute: the solutions " +
					"are not of the right type. The type should be 'Binary', but " +
					parents[0].getType() + " and " + 
					parents[1].getType() + " are obtained");

			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if 

		if (parents.length < 2)
		{
			Configuration.logger_.severe("SingleBinaryNPointsCrossover.execute: operator needs two " +
					"parents");
			Class cls = java.lang.String.class;
			String name = cls.getName(); 
			throw new JMException("Exception in " + name + ".execute()") ;      
		}

		if (points_ == null || points_ < 1)
		{
			Configuration.logger_.severe("SingleBinaryNPointsCrossover.execute: number of cutting points cannot be negative or zero");
			Class cls = java.lang.String.class;
			String name = cls.getName(); 
			throw new JMException("Exception in " + name + ".execute()") ;      
		}

		Solution [] offspring = doCrossover(crossoverProbability_,
				points_,
				parents[0],
				parents[1]);

		return offspring; 
	} // execute

} // SingleBinaryNPointsCrossover
