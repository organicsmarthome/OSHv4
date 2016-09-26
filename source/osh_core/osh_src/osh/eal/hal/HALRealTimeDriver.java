package osh.eal.hal;

import java.util.TimeZone;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.core.logging.IGlobalLogger;
import osh.core.threads.InvokerThreadRegistry;
import osh.eal.hal.exceptions.HALException;

/**
 * class for accessing the real-time clock in the lab scenario 
 * and for accessing the ticks in the simulation...
 * Note: Time base is Unix-time ! ! ! 
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 * @category smart-home ControllerBox HAL
 */
public class HALRealTimeDriver implements Runnable {
	
	private long timeTick;
	private long unixTimeAtStart;
	private long timeIncrement;
	private boolean isSimulation;
	boolean runningVirtual;
	private IGlobalLogger globalLogger;
	private InvokerThreadRegistry registeredComponents;
	private TimeZone hostTimeZone;
	
	/**
	 * CONSTRUCTOR for testing purposes
	 * @param globalLogger
	 * @param isSimulation
	 * @param timeIncrement
	 * @param forcedStartTime
	 */
	public HALRealTimeDriver(
			IGlobalLogger globalLogger,
			boolean isSimulation,
			long timeIncrement, 
			long forcedStartTime){
		this(globalLogger, TimeZone.getDefault(), isSimulation, false, timeIncrement, forcedStartTime);
	}
	
	/**
	 * CONSTRUCTOR for normal usage
	 * @param globalLogger
	 * @param isSimulation
	 * @param runningVirtual
	 * @param timeIncrement
	 * @param forcedStartTime
	 */
	public HALRealTimeDriver(
			IGlobalLogger globalLogger,
			TimeZone hostTimeZone,
			boolean isSimulation,
			boolean runningVirtual,
			long timeIncrement, 
			long forcedStartTime){

		this.hostTimeZone = hostTimeZone;
		this.globalLogger = globalLogger;
		this.timeIncrement = timeIncrement;
		this.isSimulation = isSimulation;
		this.runningVirtual = runningVirtual;
		
		if (!isSimulation) {
			new Thread(this, "RealTimeDriver").start();
			this.unixTimeAtStart = System.currentTimeMillis() / 1000L;
		}
		else {
			this.unixTimeAtStart = forcedStartTime;
		}
	}
	
	/**
	 * you can register a component on the realtime-driver. So with the given frequency a component 
	 * will be announced that the given time period is over. You can also unregister a component (method: unregisterComponent(IRealTimeObserver iRealTimeObserver) )  
	 * @param realTimeSubscriber
	 * @param refreshFrequency (>=1)
	 * @throws OSHException 
	 */
	public void registerComponent(IRealTimeSubscriber iRealTimeListener, long refreshFrequency) throws OSHException {
		registerComponent(iRealTimeListener, refreshFrequency, Thread.NORM_PRIORITY);
	}

	public void registerComponent(IRealTimeSubscriber iRealTimeListener, long refreshFrequency, int priority) throws OSHException {
		globalLogger.logDebug("registerComponent:" + iRealTimeListener + " refreshFrequency:" + refreshFrequency);
		registeredComponents.addRealtimeSubscriber(iRealTimeListener, refreshFrequency, priority);
	}

	public void unregisterComponent(IRealTimeSubscriber iRealTimeListener) {
		globalLogger.logDebug("unregisterComponent:" + iRealTimeListener);
		registeredComponents.removeRealtimeSubscriber(iRealTimeListener);
	}
	
/*	public void unregisterComponent(IRealTimeListener iRealTimeObserver){
		HALrealTimeComponentRegister removeCandidate = null;
		for (HALrealTimeComponentRegister _component:registedComponents){
			if (_component.iRealTimeObserver == iRealTimeObserver){
				removeCandidate = _component;
			}
		}
		if (removeCandidate != null) {
			removeCandidate.thread.should_run = false;
			registedComponents.remove(removeCandidate);
		}
	}*/
	
	public TimeZone getHostTimeZone() {
		return hostTimeZone;
	}
	
	public long getUnixTime(){
		long unixTime;
		
		if (!isSimulation) {
			unixTime = System.currentTimeMillis() / 1000L;	
		}
		else {
			unixTime = (long)(this.timeTick * timeIncrement + this.unixTimeAtStart);
		}
		
		return unixTime;
	}
	
	/**
	 * get the configured time increment of the time base. (In the real-smart-home scenario it's usually 1)
	 * @return
	 */
	public long getTimeIncrement() {
		return timeIncrement;
	}

	public long getUnixTimeAtStart() {
		return unixTimeAtStart;
	}

	public boolean isSimulation() {
		return isSimulation;
	}
	
	/**
	 * duration the cb is running
	 */
	public long getRuntime(){
		return this.timeTick;
	}
	
	/**
	 * time the cb started / time since the cb is running
	 */
	public long runningSince() {
		return getUnixTimeAtStart();
	}

	public void setSimulation(boolean isSimulation) {
		this.isSimulation = isSimulation;
	}

	public void setThreadRegistry(InvokerThreadRegistry threadRegistry) {
		this.registeredComponents = threadRegistry;
	}
	
	//for simulation update the time from the simulation engine
	public void updateTimer(long simulationTick) throws HALException {
		if (simulationTick % 3600 == 0) {
			this.globalLogger.logDebug("updateTimer(" + simulationTick + ")");
		}
		this.timeTick = simulationTick;
		this.registeredComponents.triggerInvokers();
	}
	
	/**
	 * You should N E V E R invoke this method by yourself ! ! ! !
	 * only the simulation engine should do this!
	 */
	public void resetTimer(){
		this.unixTimeAtStart = System.currentTimeMillis() / 1000L;
		this.timeTick = 0;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				++timeTick;
				try {
					registeredComponents.triggerInvokers();
				} catch (Exception e) {
					// Catch all ... Should never happen
					globalLogger.logError("Should never ever ever happen!", e);
					e.printStackTrace();
				}
			}
			catch (InterruptedException ex){
				globalLogger.logError("Should never ever ever happen!", ex);
				ex.printStackTrace();
			}
		}
	}

	public void startTimerProcessingThreads() {
		registeredComponents.startThreads();
	}
	
}
