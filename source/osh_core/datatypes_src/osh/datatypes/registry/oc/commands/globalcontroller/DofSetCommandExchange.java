package osh.datatypes.registry.oc.commands.globalcontroller;

import java.util.UUID;

import osh.datatypes.registry.CommandExchange;


/**
 * 
 * @author Till Schuberth, Ingo Mauser, Kaibin Bao
 *
 */
public class DofSetCommandExchange extends CommandExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3381723233858020252L;
	private Integer firstDof;
	private Integer secondDoF;
	
	public DofSetCommandExchange(
			UUID sender, 
			UUID receiver, 
			Long timestamp, 
			Integer firstDof,
			Integer secondDoF) {
		super(sender, receiver, timestamp);
		
		if (firstDof != null) {
			this.firstDof = firstDof;
		}
		if (secondDoF != null) {
			this.secondDoF = secondDoF;
		}
		
		if (firstDof != null && firstDof < 0 ) throw new IllegalArgumentException("First Dof is less than zero");
		if (secondDoF != null && secondDoF < 0 ) throw new IllegalArgumentException("Second Dof is less than zero");
	}

	public Integer getDof() {
		return firstDof;
	}
	
	public Integer getSecondDof() {
		return secondDoF;
	}
}
