//  BitFlipMutation.java
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

package jmetal.operators.mutation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * This class implements a bit flip mutation operator.
 * NOTE: the operator is applied to binary or integer solutions, considering the
 * whole solution as a single encodings.variable.
 */
@SuppressWarnings("rawtypes")
public class PPBitFlipMutation extends Mutation {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
   * Valid solution types to apply this operator 
   */
  private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class) ;

  private int[] problemPartBoundarys_ = null;
  
	/**
	 * Constructor
	 * Creates a new instance of the Bit Flip mutation operator
	 */
	public PPBitFlipMutation(HashMap<String, Object> parameters, PseudoRandom pseudoRandom) {
		super(parameters, pseudoRandom) ;
  	if (parameters.get("problemPartBoundarys") != null)
  		problemPartBoundarys_ = (int[]) parameters.get("problemPartBoundarys");  		
	} // BitFlipMutation

	/**
	 * Perform the mutation operation
	 * @param probability Mutation probability
	 * @param solution The solution to mutate
	 * @throws JMException
	 */
	public void doMutation(Solution solution) throws JMException {
		try {
			if ((solution.getType().getClass() == BinarySolutionType.class) && solution.getNumberOfBits() > 0) {
				double adjustedProbability = 1.0 / (double) problemPartBoundarys_[0];
				int problemPartCounter = 0;
				
				for (int i = 0; i < solution.getNumberOfBits(); i++) {
					if (i == problemPartBoundarys_[problemPartCounter]) {						
						problemPartCounter++;
						adjustedProbability = 1.0 
								/ (double)(problemPartBoundarys_[problemPartCounter] 
										- problemPartBoundarys_[problemPartCounter - 1]);
					}
					if (pseudoRandom.randDouble() < adjustedProbability) 
						((Binary) solution.getDecisionVariables()[0]).bits_.flip(i);				
				}

				((Binary) solution.getDecisionVariables()[0]).decode();
			}
		} catch (ClassCastException e1) {
			Configuration.logger_.severe("BitFlipMutation.doMutation: " +
					"ClassCastException error" + e1.getMessage());
			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".doMutation()");
		}
	} // doMutation

	/**
	 * Executes the operation
	 * @param object An object containing a solution to mutate
	 * @return An object containing the mutated solution
	 * @throws JMException 
	 */
	@Override
	public Object execute(Object object) throws JMException {
		Solution solution = (Solution) object;

		if (!VALID_TYPES.contains(solution.getType().getClass())) {
			Configuration.logger_.severe("BitFlipMutation.execute: the solution " +
					"is not of the right type. The type should be 'Binary'"
					+ " but " + solution.getType() + " is obtained");

			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if 

		doMutation(solution);
		return solution;
	} // execute
} // BitFlipMutation
