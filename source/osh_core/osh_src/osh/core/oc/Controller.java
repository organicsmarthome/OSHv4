package osh.core.oc;

import osh.OSHComponent;
import osh.core.OCComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSHOC;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.registry.OCRegistry;

/**
 * abstract superclass for all controllers
 * 
 * @author Florian Allerding
 */
public abstract class Controller extends OCComponent implements IRealTimeSubscriber, ILifeCycleListener {
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 */
	public Controller(IOSHOC controllerbox) {
		super(controllerbox);
	}
	
	
	@Override
	public IOSHOC getOSH() {
		return (IOSHOC) super.getOSH();
	}
	
	
	protected OCRegistry getOCRegistry() {
		return getOSH().getOCRegistry();
	}

	@Override
	public void onSystemError() throws OSHException {
		//...in case of use please override
	}

	@Override
	public void onSystemHalt() throws OSHException {
		//...in case of use please override
	}

	@Override
	public void onSystemRunning() throws OSHException {
		//...in case of use please override
	}

	@Override
	public void onSystemIsUp() throws OSHException {
		//...in case of use please override
	}

	@Override
	public void onSystemResume() throws OSHException {
		//...in case of use please override
	}

	@Override
	public void onSystemShutdown() throws OSHException {
		//...in case of use please override
	}

	@Override
	public void onNextTimePeriod() throws OSHException {
		//...in case of use please override
	}

	@Override
	public OSHComponent getSyncObject() {
		return this;
	}
	
}
