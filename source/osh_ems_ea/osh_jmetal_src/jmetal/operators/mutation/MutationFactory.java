package jmetal.operators.mutation;

import java.util.HashMap;

import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import osh.core.OSHRandomGenerator;
@SuppressWarnings("rawtypes")

/**
 * Class implementing a factory for Mutation objects.
 */
public class MutationFactory {
	
	
	public static Mutation getMutationOperator(String name, HashMap parameters, OSHRandomGenerator halRandomGenerator) throws JMException {
		PseudoRandom pseudoRandom = new PseudoRandom(halRandomGenerator);
		return getMutationOperator(name, parameters, pseudoRandom);
	}
	
  /**
   * Gets a crossover operator through its name.
   * @param name of the operator
   * @return the operator
   * @throws JMException 
   */
@SuppressWarnings("unchecked")
public static Mutation getMutationOperator(
		String name, 
		HashMap parameters,
		PseudoRandom pseudoRandom) throws JMException{
	
	if (name.equalsIgnoreCase("BitFlipMutation"))
			return new BitFlipMutation(parameters, pseudoRandom);
	else if (name.equalsIgnoreCase("BitFlipAutoProbMutation"))
		return new BitFlipAutoProbMutation(parameters, pseudoRandom);
	else if (name.equalsIgnoreCase("PolynomialMutation"))
      return new PolynomialMutation(parameters, pseudoRandom);
    else if (name.equalsIgnoreCase("NonUniformMutation"))
      return new NonUniformMutation(parameters, pseudoRandom);
    else if (name.equalsIgnoreCase("SwapMutation"))
      return new SwapMutation(parameters, pseudoRandom);
    else if (name.equalsIgnoreCase("UniformMutation"))
      return new UniformMutation(parameters, pseudoRandom);
    else if (name.equalsIgnoreCase("PPBitFlipMutation"))
      return new PPBitFlipMutation(parameters, pseudoRandom);
    else if (name.equalsIgnoreCase("BlockBitFlipMutation"))
      return new BlockBitFlipMutation(parameters, pseudoRandom);
    else
    {
      Configuration.logger_.severe("Operator '" + name + "' not found ");
      Class cls = java.lang.String.class;
      String name2 = cls.getName() ;    
      throw new JMException("Exception in " + name2 + ".getMutationOperator()") ;
    }        
  } // getMutationOperator
} // MutationFactory
