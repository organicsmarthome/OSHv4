package osh.core;

import java.util.Random;
import java.util.stream.DoubleStream;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public class OSHRandomGenerator {

	private Random random;

	
	/**
	 * DEFAULT CONSTRUCTOR
	 */
	public OSHRandomGenerator() {
		//see: Seeed ;-)
		this.random = new Random(0xd1ce5bL);
	}
	
	/**
	 * CONSTRUCTOR
	 */
	public OSHRandomGenerator(Random random) {
		this.random = random;
	}

	
	public synchronized int getNextInt(){
		return this.random.nextInt();
	}
	
	public synchronized int getNextInt(int max){
		return this.random.nextInt(max);
	}
	
	/**
	 * random.nextDouble()
	 * @return [0, 1)
	 */
	public synchronized double getNextDouble(){
		return this.random.nextDouble();
	}
	
	public synchronized DoubleStream getDoubleArray(int limit) {
		return this.random.doubles(limit);
	}
	
	public synchronized double[] getDoubleArrayBoundarys(int limit, double minBound, double maxBound) {
		return this.random.doubles(limit, minBound, maxBound).toArray();
	}
	
	public synchronized void getNextBytes(byte[] bytes) {
		this.random.nextBytes(bytes);
	}
	
	/**
	 * 
	 * @param max > 0
	 * @return
	 */
	public synchronized double getNextDouble(double max){
		return max * this.random.nextDouble();
	}
	
	public synchronized float getNextFloat(){
		return this.random.nextFloat();
	}
	
	public synchronized long getNextLong(){
		return this.random.nextLong();
	}
	
	public synchronized boolean getNextBoolean(){
		return this.random.nextBoolean();
	}
	
	public synchronized double getNextGaussian(){
		return this.random.nextGaussian();
	}
	
}
