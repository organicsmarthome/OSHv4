package osh.datatypes.time;

import java.util.ArrayList;
import java.util.List;

import osh.datatypes.ea.interfaces.ISolution;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ActivationList implements ISolution {

	List<Activation> list;
	
	public ActivationList() {
		list = new ArrayList<>();
	}

	public List<Activation> getList() {
		return list;
	}

	public void setList(List<Activation> list) {
		this.list = list;
	}
	
}
