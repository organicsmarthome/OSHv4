package osh.datatypes.cruisecontrol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * 
 * @author Till Schuberth
 *
 * @param <Value>
 */
public class OptimizedDataStorage<Value extends OptimizedDataStorage.EqualData<? super Value>> {

	public interface EqualData<T> {
		public boolean equalData(T o);
	}
	
	private TreeMap<Long, Value> storage = new TreeMap<Long, Value>();
	
	public TreeMap<Long, Value> getMap() {
		return storage;
	}
	
	public void add(long timestamp, Value value) {
		storage.put(timestamp, value);
		
		// check if this data point is the same as the two before 
		//  and throw out the middle one if possible
		if (storage.size() >= 3) {
			List<Entry<Long, Value>> lastEntries = new ArrayList<Entry<Long, Value>>(3);
			Iterator<Entry<Long, Value>> it = storage.descendingMap().entrySet().iterator();
			for (int i = 0; i < 3; i++) lastEntries.add(i, it.next());
			
			if (lastEntries.get(0).getValue().equalData(lastEntries.get(1).getValue()) && lastEntries.get(1).getValue().equalData(lastEntries.get(2).getValue())) {
				storage.remove(lastEntries.get(1).getKey());
			}
		}
	}
	
}
