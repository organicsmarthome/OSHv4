package osh.core.oc;

import java.util.HashMap;
import java.util.Map;

import osh.core.interfaces.IOSHOC;


@Deprecated
public abstract class LocalParameterizedController extends LocalController {

	private Map<String,String> runtimeParameters = new HashMap<String, String>();
	
	public LocalParameterizedController(IOSHOC controllerbox) {
		super(controllerbox);
	}

	/**
	 * Get all runtime parameters for local unit (like dof)
	 * 
	 * @return
	 */
	public Map<String, String> getRuntimeParameters() {
		return runtimeParameters;
	}

	/**
	 * Get one runtime parameter for local unit (like dof)
	 * 
	 * @return
	 */
	public String getRuntimeParameter(String key) {
		return runtimeParameters.get(key);
	}

	/**
	 * Set all runtime parameters for local unit
	 * 
	 * @return
	 */
	public void setRuntimeParameters(Map<String, String> runtimeParameters) {
		this.runtimeParameters = runtimeParameters;
	}

	/**
	 * Set one runtime parameter for local unit
	 * 
	 * @return
	 */
	public void setRuntimeParameters(String key, String value) {
		this.runtimeParameters.put(key, value);
	}
	

}
