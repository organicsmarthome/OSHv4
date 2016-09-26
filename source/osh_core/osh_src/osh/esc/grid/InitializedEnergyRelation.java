package osh.esc.grid;

import java.io.Serializable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class InitializedEnergyRelation implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4621503592492456770L;
	
//	private UUID sourceId;
	private int sourceId;
	private ObjectArrayList<InitializedEnergyRelationTarget> targets;
	private InitializedEnergyRelationTarget[] targetsArray;
	
	public InitializedEnergyRelation(int sourceId, ObjectArrayList<InitializedEnergyRelationTarget> targets) {
		this.sourceId = sourceId;
		this.targets = targets;
	}
	
	/** do not use - only for serialisation */
	@Deprecated
	protected InitializedEnergyRelation() {
	}
	
	public void addEnergyTarget(InitializedEnergyRelationTarget target) {
		if (!targets.contains(target)) {
			targets.add(target);
		}
	}
	
	public int getSourceId() {
		return sourceId;
	}
	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}
	
	public void transformToArrayTargets() {
		targetsArray = new InitializedEnergyRelationTarget[targets.size()];
		targetsArray = targets.toArray(targetsArray);
	}
	
	public InitializedEnergyRelationTarget[] getTargets() {
		return targetsArray;
	}
	public void setTarges(InitializedEnergyRelationTarget[] targetsArray) {
		this.targetsArray = targetsArray;
	}

}
