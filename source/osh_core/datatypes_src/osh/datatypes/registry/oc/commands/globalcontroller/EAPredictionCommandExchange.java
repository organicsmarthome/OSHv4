package osh.datatypes.registry.oc.commands.globalcontroller;

import java.util.UUID;

import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.registry.CommandExchange;


/**
 * 
 * @author Till Schuberth, Ingo Mauser
 *
 * @param <PhenotypeType>
 */
public class EAPredictionCommandExchange<PredictionType extends IPrediction> extends CommandExchange {


	/**
	 * 
	 */
	private static final long serialVersionUID = -8765943738508084657L;
	private PredictionType prediction;
	
	public EAPredictionCommandExchange(
			UUID sender, 
			UUID receiver, 
			long timestamp, 
			PredictionType prediction) {
		super(sender, receiver, timestamp);
		
		this.prediction = prediction;
	}

	public PredictionType getPrediction() {
		return prediction;
	}

}
