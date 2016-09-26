//  PseudoRandom.java
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

package jmetal.util;

import java.util.stream.DoubleStream;

import osh.core.OSHRandomGenerator;


/**
 * Class representing a pseudo-random number generator
 */
public class PseudoRandom {
    
	private OSHRandomGenerator randomGenerator = null;
	
	public PseudoRandom(OSHRandomGenerator randomGenerator) {
		this.randomGenerator = randomGenerator;
		
	}
	
	/** 
	   * Returns a random int value using the Java random generator.
	   * @return A random int value.
	   */
	public int randInt() {
		return randomGenerator.getNextInt();
	} // randInt
	
	/** 
	* Returns a random double value using the PseudoRandom generator.
	* Returns A random double value.
	*/
	public double randDouble() {
		return randomGenerator.getNextDouble();
	//return randomJava.nextDouble();
	} // randDouble
	
	public double nextGaussian() {
		return randomGenerator.getNextGaussian();
	}
	
	/**
	 * Returns an array of random double values in [0,1]
	 * @param limit the size of the array
	 * @return the array of random double values
	 */
	public DoubleStream randDoubleArray(int limit) {
		return randomGenerator.getDoubleArray(limit);
	}
	
	/**
	 * Returns an array of random double values between minBound (inclusive) and maxBound (exclusive)
	 * @param limit the size of the array
	 * @param minBound the minimum boundary for the double values (inclusive)
	 * @param maxBound the maximum boundary for the double values (exclusive)
	 * @return the array of random double values
	 */
	public double[] randDoubleArrayBound(int limit, double minBound, double maxBound) {
		return randomGenerator.getDoubleArrayBoundarys(limit, minBound, maxBound);
	}
	
	/** 
	* Returns a random int value between a minimum bound and maximum bound using
	* the PseudoRandom generator.
	* 
	* Attention: maxBound is expected to be inclusive!!!!!!!!!!!!!!!!!!!!!!!!!!
	* 
	* @param minBound The minimum bound.
	* @param maxBound The maximum bound.
	* Return A pseudo random int value between minBound and maxBound.
	*/
	public int randInt(int minBound, int maxBound) {
	    return minBound + randomGenerator.getNextInt(maxBound-minBound+1);
	} // randInt
	
	/** Returns a random double value between a minimum bound and a maximum bound
	* using the PseudoRandom generator.
	* 
	* Attention: this interface expects maxBound to be inclusive, but java random
	*            generator generates values exclusive maxBound!!!!!!!!!!!!!!!!!!! 
	* 
	* @param minBound The minimum bound.
	* @param maxBound The maximum bound.
	* @return A pseudo random double value between minBound and maxBound
	*/
	public double randDouble(double minBound, double maxBound) {
		double max = maxBound - minBound;
	    return minBound + randomGenerator.getNextDouble(max);
	    //return minBound + (maxBound - minBound)*randomJava.nextDouble();
	} // randDouble
	
	/** Fills the supplied byte array with random values (50% prob.)
	 * 
	 * @param bytes the array to fill
	 */
	public void randNextBytes(byte[] bytes) {
		randomGenerator.getNextBytes(bytes);
	}
	
	
//  /**
//   * generator used to obtain the random values
//   */
//  private static RandomGenerator random = null;    
//  
//  /**
//   * other generator used to obtain the random values
//   */
//  private static java.util.Random randomJava = null;
//             
//  /** 
//   * Constructor.
//   * Creates a new instance of PseudoRandom.
//   */
//  private PseudoRandom() {
//    if (random == null){
//      //this.random = new java.util.Random((long)seed);
//      random = new RandomGenerator();
//      randomJava = new java.util.Random();            
//    }
//  } // PseudoRandom
//    
//  /** 
//   * Returns a random int value using the Java random generator.
//   * @return A random int value.
//   */
//  public static int randInt() {
//    if (random == null) {
//      new PseudoRandom();
//    }
//    return randomJava.nextInt();
//  } // randInt
//    
//  /** 
//   * Returns a random double value using the PseudoRandom generator.
//   * Returns A random double value.
//   */
//  public static double randDouble() {
//    if (random == null) {
//      new PseudoRandom();
//    }
//    return random.rndreal(0.0,1.0);
//    //return randomJava.nextDouble();
//  } // randDouble
//    
//  /** 
//   * Returns a random int value between a minimum bound and maximum bound using
//   * the PseudoRandom generator.
//   * @param minBound The minimum bound.
//   * @param maxBound The maximum bound.
//   * Return A pseudo random int value between minBound and maxBound.
//   */
//  public static int randInt(int minBound, int maxBound) {
//    if (random == null) {
//      new PseudoRandom();
//    }
//    return random.rnd(minBound,maxBound);
//    //return minBound + randomJava.nextInt(maxBound-minBound+1);
//  } // randInt
//    
//  /** Returns a random double value between a minimum bound and a maximum bound
//   * using the PseudoRandom generator.
//   * @param minBound The minimum bound.
//   * @param maxBound The maximum bound.
//   * @return A pseudo random double value between minBound and maxBound
//   */
//  public static double randDouble(double minBound, double maxBound) {
//    if (random == null) {
//      new PseudoRandom();
//    }
//    return random.rndreal(minBound,maxBound);
//    //return minBound + (maxBound - minBound)*randomJava.nextDouble();
//  } // randDouble    
} // PseudoRandom
