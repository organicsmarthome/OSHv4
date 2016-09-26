package osh.configuration;

import java.util.HashMap;
import java.util.List;

import osh.configuration.system.ConfigurationParameter;


/**
 * internal representation for name-value-pairs used by the OSH
 * 
 *@author Florian Allerding
 */
public class OSHParameterCollection {
	
	private HashMap<String, String> parameterCollection;
	
	public OSHParameterCollection() {
		this.parameterCollection = new HashMap<String, String>();
	}
	
	public String getParameter(String name){	
		String _retStr = parameterCollection.get(name);
		return _retStr;
	}
	
	public void setParameter(String name, String value){
		
		parameterCollection.put(name, value);
	}
	
	public void loadCollection(List<ConfigurationParameter> configParam){

		for(int i = 0; i < configParam.size(); i++){
			parameterCollection.put(configParam.get(i).getParameterName(), configParam.get(i).getParameterValue());
		}
		
	}

}