package osh.hal.exchange;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import osh.cal.CALComExchange;
import osh.datatypes.ea.Schedule;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class GUIScheduleComExchange extends CALComExchange {
	
	private List<Schedule> schedules;	
	private int stepSize;
	
	/**
	 * CONSTRUCTOR
	 */
	public GUIScheduleComExchange(
			UUID deviceID, 
			Long timestamp,
			List<Schedule> schedules,
			int stepSize) {
		super(deviceID, timestamp);
		
		this.schedules = new ArrayList<>();
		this.stepSize = stepSize;
		
		synchronized (schedules) {
			for (int i = 0; i < schedules.size(); i++) {
				Schedule s = schedules.get(i);
				Schedule clonedS = s.clone();
				this.schedules.add(clonedS);
			}
		}
	}


	public List<Schedule> getSchedules() {
		return schedules;
	}

	public int getStepSize() {
		return stepSize;
	}

}
