package osh.esc.grid;

import java.io.Serializable;

import osh.esc.grid.carrier.ConnectionType;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class EnergyRelation<T extends ConnectionType> implements Serializable {
	
	/** Serial ID */
	private static final long serialVersionUID = -6270348787737984480L;
	
	private T activeToPassiveConnection;
	private T passiveToActiveConnection;
	
	private EnergySourceSink activeEntity;
	private EnergySourceSink passiveEntity;
	
	/**
	 * CONSTRUCTOR
	 */
	public EnergyRelation(
			EnergySourceSink activeEntity,
			EnergySourceSink passiveEntity,
			T activeToPassiveConnection, 
			T passiveToActiveConnection) {
		super();
		this.activeEntity = activeEntity;
		this.passiveEntity = passiveEntity;
		this.activeToPassiveConnection = activeToPassiveConnection;
		this.passiveToActiveConnection = passiveToActiveConnection;
	}
	
	public T getActiveToPassive() {
		return activeToPassiveConnection;
	}
	public T getPassiveToActive() {
		return passiveToActiveConnection;
	}
	
	public EnergySourceSink getActiveEntity() {
		return activeEntity;
	}
	public EnergySourceSink getPassiveEntity() {
		return passiveEntity;
	}
	
	@Override
	public String toString() {
		return "[active] " + activeEntity.getDeviceUuid() + " <--> [passive] " + passiveEntity.getDeviceUuid();
	}
	
}
