package osh.datatypes.registry.oc.commands.globalcontroller;

import java.util.UUID;

import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.registry.CommandExchange;


/**
 * 
 * @author Till Schuberth, Ingo Mauser
 *
 * @param <PhenotypeType>
 */
public class EASolutionCommandExchange<PhenotypeType extends ISolution> extends CommandExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4399537651455121419L;
	private PhenotypeType phenotype;
	
	public EASolutionCommandExchange(
			UUID sender, 
			UUID receiver, 
			long timestamp, 
			PhenotypeType phenotype) {
		super(sender, receiver, timestamp);
		
		this.phenotype = phenotype;
	}

	public PhenotypeType getPhenotype() {
		return phenotype;
	}

}
