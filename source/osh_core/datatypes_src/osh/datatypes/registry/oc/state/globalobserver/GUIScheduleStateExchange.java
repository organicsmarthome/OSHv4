package osh.datatypes.registry.oc.state.globalobserver;

import java.util.List;
import java.util.UUID;

import osh.datatypes.ea.Schedule;
import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Florian Allerding, Ingo Mauser
 *
 */
public class GUIScheduleStateExchange extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9037948484261116427L;
	private List<Schedule> schedules;
	private int stepSize;
	
	public GUIScheduleStateExchange(
			UUID sender, 
			long timestamp, 
			List<Schedule> schedules,
			int stepSize) {
		super(sender, timestamp);
		this.schedules = schedules;
		this.stepSize = stepSize;
	}

	public List<Schedule> getDebugGetSchedules() {
		return schedules;
	}

	public int getStepSize() {
		return stepSize;
	}

	@Override
	public GUIScheduleStateExchange clone() {
		GUIScheduleStateExchange copy = (GUIScheduleStateExchange) super.clone();
		//TODO: do proper cloning
		return copy;
	}

}
