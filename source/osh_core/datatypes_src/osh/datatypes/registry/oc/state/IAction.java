package osh.datatypes.registry.oc.state;

import java.util.UUID;

/**
 * Interface for prediction
 * @author Florian Allerding, Till Schuberth
 *
 */
public interface IAction {
	public UUID getDeviceId();
	public long getTimestamp();
	public boolean equals(IAction other);
	
//	public IAction createAction(long newTimestamp);
	
	@Override
	public int hashCode();
}