package osh.mgmt.globalcontroller.jmetal;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.encodings.variable.Binary;
import jmetal.metaheuristics.singleObjective.evolutionStrategy.ElitistES;
import jmetal.operators.mutation.MutationFactory;
import osh.core.OSHRandomGenerator;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OCEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.esc.EnergyManagementProblem;

/**
 * 
 * @author Till Schuberth, Ingo Mauser
 *
 */
public class JMetalSolver extends Optimizer {
	
	protected OSHRandomGenerator randomGenerator;
	
	protected IGlobalLogger logger;
	
	protected boolean showDebugMessages;
	
	protected int[][] bitPositions;
	
	protected final int STEP_SIZE;
	
	protected Comparator<Solution> fitnessComparator = new Comparator<Solution>() {
		@Override
		public int compare(Solution o1, Solution o2) {
			double v1 = o1.getObjective(0), v2 = o2.getObjective(0);
			if (v1 < v2) return -1;
			else if (v1 > v2) return +1;
			else return 0;
		}
	};
	
	
	/**
	 * CONSTRUCTOR
	 * @param globalLogger
	 * @param randomGenerator
	 * @param showDebugMessages
	 */
	public JMetalSolver(
			IGlobalLogger globalLogger,
			OSHRandomGenerator randomGenerator, 
			boolean showDebugMessages,
			int STEP_SIZE) {
		this.logger = globalLogger;
		this.randomGenerator = randomGenerator;
		this.showDebugMessages = showDebugMessages;
		this.STEP_SIZE = STEP_SIZE;
	}
	
	
	public SolutionWithFitness getSolution(
			List<InterdependentProblemPart<?, ?>> problemparts,
			OCEnergySimulationCore ocESC,
			int[][] bitPositions,
			EnumMap<AncillaryCommodity,PriceSignal> priceSignals,
			EnumMap<AncillaryCommodity,PowerLimitSignal> powerLimitSignals, 
			long ignoreLoadProfileBefore,
			IFitness fitnessFunction) throws Exception {
		
		this.bitPositions = bitPositions;
		SolutionWithFitness result = getSolutionAndFitness(
				problemparts,
				ocESC,
				priceSignals,
				powerLimitSignals,
				ignoreLoadProfileBefore, 
				fitnessFunction);
		
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SolutionWithFitness getSolutionAndFitness(
			List<InterdependentProblemPart<?, ?>> problemparts,
			OCEnergySimulationCore ocESC,
			EnumMap<AncillaryCommodity,PriceSignal> priceSignals,
			EnumMap<AncillaryCommodity,PowerLimitSignal> powerLimitSignals, 
			long ignoreLoadProfileBefore,
			IFitness fitnessFunction) throws Exception {
		
		int mu = 1; // Requirement: lambda must be divisible by mu
	    int lambda = 10; // Population size
	    
	    int evaluations = 40 * lambda; // Generations = evaluations / lambda
		
		int numberOfBits = 0;
		for (InterdependentProblemPart<?, ?> i : problemparts) {
			numberOfBits = numberOfBits + i.getBitCount();
		}
		
		// DECLARATION
	    Problem problem;			// The problem to solve
	    Algorithm algorithm;		// The algorithm to use
	    Operator mutation;			// Mutation operator
	            
		HashMap parameters;			// Operator parameters

	    // calculate ignoreLoadProfileAfter (Optimizaion Horizon)
		long ignoreLoadProfileAfter = ignoreLoadProfileBefore;
		for (InterdependentProblemPart<?, ?> ex : problemparts) {
			if (ex instanceof ControllableIPP<?, ?>) {
				ignoreLoadProfileAfter = Math.max(((ControllableIPP<?, ?>) ex).getOptimizationHorizon(), ignoreLoadProfileAfter);
			}
		}
	    
		// INITIALIZATION
		problem = new EnergyManagementProblem(
				problemparts,
				ocESC,
				bitPositions, 
				priceSignals, 
				powerLimitSignals, 
				ignoreLoadProfileBefore, 
				ignoreLoadProfileAfter, 
				randomGenerator, 
				this.logger, 
				fitnessFunction,
				STEP_SIZE);
		
	    // SHORT CUT IFF NOTHING HAS TO BE OPTIMIZED
		if (numberOfBits == 0) {
			Solution solution = new Solution(problem);
			problem.evaluate(solution);

			SolutionWithFitness result = new SolutionWithFitness(new BitSet(), Collections.<BitSet>emptyList(), solution.getFitness());
			
 			//better be sure
 			((EnergyManagementProblem) problem).finalizeGrids();
 			
			return result;
		}
	    
	    algorithm = new ElitistES(problem, mu, lambda, showDebugMessages);
	    //algorithm = new NonElitistES(problem, mu, lambda);
	    
	    /* Algorithm parameters */
	    algorithm.setInputParameter("maxEvaluations", evaluations);
	    
	    /* Mutation and Crossover for Real codification */
	    parameters = new HashMap() ;
	    parameters.put("probability", 1.0/30) ;
	    mutation = MutationFactory.getMutationOperator(
	    		"BitFlipMutation", 
	    		parameters,
	    		this.randomGenerator);                    
	    
	    algorithm.addOperator("mutation", mutation);
	 
	    /* Execute the Algorithm */
	    SolutionSet population = algorithm.execute();

		Binary s = (Binary) population.best(fitnessComparator).getDecisionVariables()[0];
		
		int bitpos = 0;
		ArrayList<BitSet> resultBitSet = new ArrayList<BitSet>();
		for (InterdependentProblemPart<?, ?> part : problemparts) {
			resultBitSet.add(s.bits_.get(bitpos, bitpos + part.getBitCount()));
			bitpos += part.getBitCount();
		}
		if (bitpos < s.bits_.length()) {
			throw new NullPointerException("Confilct: Solution has more bits then needed for IPP");
		}
		
		double returnFitness = population.best(fitnessComparator).getObjective(0);
		
		SolutionWithFitness result = new SolutionWithFitness(s.bits_, resultBitSet, returnFitness);
		
		//better be sure
		((EnergyManagementProblem) problem).finalizeGrids();
		
		return result;
	}
	
}

