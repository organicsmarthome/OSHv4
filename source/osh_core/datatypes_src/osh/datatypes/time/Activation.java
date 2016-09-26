package osh.datatypes.time;

import java.io.Serializable;

import osh.datatypes.ea.interfaces.ISolution;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public class Activation implements Cloneable, ISolution, Serializable {
	private static final long serialVersionUID = -3342468910663681537L;
	public long startTime;
	public long duration;
}