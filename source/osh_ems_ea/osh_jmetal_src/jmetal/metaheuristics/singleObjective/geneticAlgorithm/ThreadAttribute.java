package jmetal.metaheuristics.singleObjective.geneticAlgorithm;


/** provides a thread-local ID
 * 
 * @author Sebastian Kramer
 */
public class ThreadAttribute {
	
	private static final ThreadLocal<Integer> id = new ThreadLocal<Integer>();
	
	public static Integer getLocalId() {
		return id.get();
	}
	
	public static void setLocalId(Integer newId) {
		id.set(newId);
	}

}
