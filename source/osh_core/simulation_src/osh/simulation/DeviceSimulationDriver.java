package osh.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.exception.EnergySimulationException;
import osh.simulation.energy.IDeviceEnergySubject;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.ScreenplayType;
import osh.simulation.screenplay.SubjectAction;

/**
 * Superclass for simulation subjects like simulated appliances or SmartMeters or ....
 * This class inherits from HALDeviceDriver. 
 * This is necessary for the capability of an integration into the OSH's HAL 
 * 
 * @author Florian Allerding, Kaibin Bao, Sebastian Kramer, Ingo Mauser, Till Schuberth
 * 
 */
public abstract class DeviceSimulationDriver 
						extends HALDeviceDriver 
						implements ISimulationSubject, IDeviceEnergySubject {

	// INNER CLASSES

	private Comparator<SubjectAction> actionComparator = new Comparator<SubjectAction>() {
		@Override
		public int compare(SubjectAction arg0, SubjectAction arg1) {
			return (int) ((arg0.getTick() - arg1.getTick()));
		}
	};
	
	// VARIABLES

	private BuildingSimulationEngine simulationEngine;
	private ISimulationActionLogger simulationActionLogger;
	
	private SortedSet<SubjectAction> actions;
	
	// ### ESC STUFFF ###
	
	protected LimitedCommodityStateMap commodityInputStates;
	protected AncillaryMeterState ancillaryMeterState;
	
	
	// ### Static values ###
	
	/** Compression type of any load profiles */
	protected LoadProfileCompressionTypes compressionType;
	protected int compressionValue;
	
	/** List of Commodities used by this device */
	protected final ArrayList<Commodity> usedCommodities;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public DeviceSimulationDriver(
			IOSH osh, 
			UUID deviceID,
			OSHParameterCollection driverConfig) 
			throws HALException {
		super(osh, deviceID, driverConfig);
		
		// get Commodities used by this device
		{
			String commoditiesArray = driverConfig.getParameter("usedcommodities");
			if (commoditiesArray != null) {
				usedCommodities = Commodity.parseCommodityArray(commoditiesArray);
			}
			else {
				throw new HALException("Used Commodities are missing!");
			}
		}
		
		this.actions = new TreeSet<SubjectAction>(actionComparator);
		
		for (Commodity c : Commodity.values()) {
			this.setPower(c, 0);
		}
		
		// if DeviceClassification.APPLIANCE (but info at this point not yet available!)
		// all conditions after first && should NOT be necessary (but remain for safety reasons)
		if (driverConfig.getParameter("screenplaytype") != null) {
			
			ScreenplayType screenplayType = ScreenplayType.fromValue(driverConfig.getParameter("screenplaytype"));
			
			if (screenplayType == ScreenplayType.STATIC) {
				// screenplay is loaded from file...
			}
			else if (screenplayType == ScreenplayType.DYNAMIC) {
				// NOTHING here...
			}
			else {
				throw new RuntimeException("value \"screenplayType\" for variable \"screenplaytype\": unknown value!");
			}
		}
		else {
			throw new RuntimeException("variable \"screenplaytype\" : missing!");
		}
		
		try {
			this.compressionType = LoadProfileCompressionTypes.valueOf(getDriverConfig().getParameter("compressionType"));
		}
		catch (Exception e) {
			this.compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
			getGlobalLogger().logWarning("Can't get compressionType, using the default value: " + this.compressionType);
		}

		try {
			this.compressionValue = Integer.valueOf(getDriverConfig().getParameter("compressionValue"));
		}
		catch (Exception e) {
			this.compressionValue = 100;
			getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.compressionValue);
		}
		
	}
	
	@Override
	public void onSystemIsUp() {
		StaticCompressionExchange _stat = new StaticCompressionExchange(getDeviceID(), getTimer().getUnixTime(), 
				compressionType, compressionValue);

		this.notifyObserver(_stat);
	}
	
	
	@Override
	public void onSimulationIsUp() throws SimulationSubjectException {
		//NOTHING
	}
	
	@Override
	public void onSimulationPreTickHook() {
		//NOTHING
	}
	
	@Override
	public void onSimulationPostTickHook() {
		//NOTHING
	}
	
	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest) throws HALException {
		//NOTHING
	}
	

	/**
	 * delete all actions from the list 
	 */
	@Override
	public void flushActions(){
		actions.clear();
	}
	
	/**
	 * gets all actions for a subject
	 * @return
	 */
	@Override
	public Collection<SubjectAction> getActions() {
		return actions;
	}
	
	/**
	 * Get another subject depending on this subject perhaps to tell him to do something...
	 * @param SubjectID
	 */
	@Override
	public ISimulationSubject getAppendingSubject(UUID SubjectID){
		ISimulationSubject _simSubject = null;
		
		//ask the simulation engine...
		_simSubject = this.simulationEngine.getSimulationSubjectByID(SubjectID);
		
		return _simSubject;
	}

	/**
	 * is invoked on every (new) time tick to announce the subject
	 */
//	@Override
	public abstract void onNextTimeTick();
		
	/**
	 * @param nextAction
	 * is invoked when the subject has to do the action "nextAction"
	 */
	@Override
	public abstract void performNextAction(SubjectAction nextAction);
	
	/**
	 * Sets an action for this simulation subject
	 * @param actions
	 */
	@Override
	public void setAction(SubjectAction action) {
		actions.add(action);
	}
	
	
	protected BuildingSimulationEngine getSimulationEngine() {
		return this.simulationEngine;
	}
	
	@Override
	public void setSimulationEngine(BuildingSimulationEngine simulationEngine){
		this.simulationEngine = simulationEngine;
	}
	
	
	protected ISimulationActionLogger getSimulationActionLogger() {
		return simulationActionLogger;
	}

	@Override
	public void setSimulationActionLogger(ISimulationActionLogger simulationLogger) {
		this.simulationActionLogger = simulationLogger;
	}
	
	
	@Override
	public void triggerSubject() {
		//invoke for announcement...
		onNextTimeTick();
		
		long currentTimeTick = getOSH().getTimer().getUnixTime();
		
		while ( actions.size() > 0 && actions.first().getTick() <= currentTimeTick ) {
			// delete earlier entries (tbd)
			if ( actions.first().getTick() < currentTimeTick ) {
				actions.remove(actions.first());
				continue;
			}
			
			// perform next action
			SubjectAction action = actions.first();
			if (simulationActionLogger != null) {
				simulationActionLogger.logAction(action);
			}
			performNextAction(action);
			
			// remove action entry
			try {
				actions.remove(actions.first());
			} 
			catch (Exception ex) {
				throw new RuntimeException("actions name: " + actions.getClass().getCanonicalName(), ex);
			}
		}
	}

	
	// ### ESC STUFF ###
	
	@Override
	public LimitedCommodityStateMap getCommodityOutputStates() 
			throws EnergySimulationException {
		LimitedCommodityStateMap map = new LimitedCommodityStateMap(usedCommodities);
		for (Commodity c : usedCommodities) {
			map.setPower(c, getPower(c));
		}
		return map;
	}


	@Override
	public void setCommodityInputStates(
			LimitedCommodityStateMap inputStates,
			AncillaryMeterState ancillaryMeterState)
					throws EnergySimulationException {
		this.commodityInputStates = inputStates;
		this.ancillaryMeterState = ancillaryMeterState;
	}
	
}
