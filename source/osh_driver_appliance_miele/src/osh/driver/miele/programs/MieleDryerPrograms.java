package osh.driver.miele.programs;

/**
 * 
 * @author Ingo Mauser
 *
 */
public enum MieleDryerPrograms {
	NO_DRYING_STEP (0),
	MACHINE_IRON (1),
	HAND_IRON_TWO_DROPS (2),
	HAND_IRON_ONE_DROP (3),
	NORMAL (4),
	NORMAL_PLUS (5),
	EXTRA_DRY (6);
	
	private MieleDryerPrograms(int id) {
		this.id = id;
	}

	private int id;
	
	public int getId() {
		return id;
	}
}
