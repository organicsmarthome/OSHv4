package jmetal.operators.mutation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
@SuppressWarnings("rawtypes")
public class BitFlipAutoProbMutation extends BitFlipMutation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3479829596499596362L;
	
	private double autoProbMuatationFactor_ = 1.0;
	
	/**
	   * Valid solution types to apply this operator 
	   */
	  
	private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class,
	      BinaryRealSolutionType.class,
	      IntSolutionType.class) ;

	public BitFlipAutoProbMutation(HashMap<String, Object> parameters, PseudoRandom pseudoRandom) {
		super(parameters, pseudoRandom);
		
	  	if (parameters.get("autoProbMuatationFactor") != null)
	  		autoProbMuatationFactor_ = (Double) parameters.get("autoProbMuatationFactor"); 
	}
	
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
			Configuration.logger_.severe("BitFlipAutoProbMutation.execute: the solution " +
					"is not of the right type. The type should be 'Binary', " +
					"'BinaryReal' or 'Int', but " + solution.getType() + " is obtained");

			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".execute()");
		} // if 

		int nOfBits = solution.getNumberOfBits();
		double prob = mutationProbability_;
		
		if (nOfBits > 1)
			prob = autoProbMuatationFactor_ / (double) solution.getNumberOfBits();
		
		doMutation(prob, solution);
		return solution;
	} // execute
}
