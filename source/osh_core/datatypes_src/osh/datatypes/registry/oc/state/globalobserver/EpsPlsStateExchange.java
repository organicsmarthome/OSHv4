package osh.datatypes.registry.oc.state.globalobserver;

import java.util.EnumMap;
import java.util.UUID;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Florian Allerding, Ingo Mauser
 *
 */
public class EpsPlsStateExchange extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5984069610430579990L;
	
	private EnumMap<AncillaryCommodity,PriceSignal> ps;
	private EnumMap<AncillaryCommodity,PowerLimitSignal> pwrLimit;
	
	private final int epsOptimizationObjective;
	private final int plsOptimizationObjective;
	private final int varOptimizationObjective;
	private final double plsUpperOverlimitFactor;
	private final double plsLowerOverlimitFactor;
	
	private final boolean epsPlsChanged;
	
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 * @param ps
	 * @param pwrLimit
	 */
	public EpsPlsStateExchange(
			UUID sender, 
			long timestamp,
			EnumMap<AncillaryCommodity,PriceSignal> ps,
			EnumMap<AncillaryCommodity,PowerLimitSignal> pwrLimit,
			int epsOptimizationObjective,
			int plsOptimizationObjective,
			int varOptimizationObjective,
			double plsUpperOverlimitFactor,
			double plsLowerOverlimitFactor,
			boolean epsPlsChanged) {
		super(sender, timestamp);
		
		this.ps = ps;
		this.pwrLimit = pwrLimit;
		this.epsOptimizationObjective = epsOptimizationObjective;
		this.plsOptimizationObjective = plsOptimizationObjective;
		this.varOptimizationObjective = varOptimizationObjective;
		this.plsUpperOverlimitFactor = plsUpperOverlimitFactor;
		this.plsLowerOverlimitFactor = plsLowerOverlimitFactor;
		this.epsPlsChanged = epsPlsChanged;
	}

	
	public EnumMap<AncillaryCommodity,PriceSignal> getPs() {
		return ps;
	}

	public EnumMap<AncillaryCommodity,PowerLimitSignal> getPwrLimit() {
		return pwrLimit;
	}
	
	public int getEpsOptimizationObjective() {
		return epsOptimizationObjective;
	}


	public int getPlsOptimizationObjective() {
		return plsOptimizationObjective;
	}


	public int getVarOptimizationObjective() {
		return varOptimizationObjective;
	}


	public double getPlsUpperOverlimitFactor() {
		return plsUpperOverlimitFactor;
	}
	
	public double getPlsLowerOverlimitFactor() {
		return plsLowerOverlimitFactor;
	}


	public boolean isEpsPlsChanged() {
		return epsPlsChanged;
	}


	@Override
	public EpsPlsStateExchange clone() {
		
		// TODO cloning
//		EnumMap<AncillaryCommodity,PriceSignal> newPs = new HashMap<>();
//		for (Entry<AncillaryCommodity,PriceSignal> e : ps.entrySet()) {
//			newPs.put(e.getKey(), e.getValue().clone());
//		}
//		
//		EnumMap<AncillaryCommodity,PowerLimitSignal> newPls = new HashMap<>();
//		for (Entry<AncillaryCommodity,PowerLimitSignal> e : pwrLimit.entrySet()) {
//			newPls.put(e.getKey(), e.getValue().clone());
//		}
		
		EpsPlsStateExchange copy = 
				new EpsPlsStateExchange(
						getSender(),
						getTimestamp(),
						ps,
						pwrLimit,
						epsOptimizationObjective,
						plsOptimizationObjective,
						varOptimizationObjective,
						plsUpperOverlimitFactor,
						plsLowerOverlimitFactor,
						epsPlsChanged);
		return copy;
	}

}
