package osh.mgmt.globalcontroller.jmetal;

import java.util.BitSet;
import java.util.List;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class SolutionWithFitness {

	BitSet fullSet;
	List<BitSet> bitSet;
	double fitness;
	
	public SolutionWithFitness(BitSet fullSet, List<BitSet> bitSet, double fitness) {
		this.fullSet = fullSet;
		this.bitSet = bitSet;
		this.fitness = fitness;
	}

	
	public BitSet getFullSet() {
		return fullSet;
	}
	
	public List<BitSet> getBitSet() {
		return bitSet;
	}

	public double getFitness() {
		return fitness;
	}
	
	
}
