package osh.driver.chp.model;

import java.io.Serializable;

import osh.utils.physics.ComplexPowerUtil;

/**
 * 
 * @author Sebastian Kramer, Ingo Mauser
 *
 */
public class GenericChpModel implements Serializable {

	private static final long serialVersionUID = -8465930221284802329L;
	
	private static final double timeForFullActivePower = 300.0;
	private static final double timeForFullThermalPower = 600.0;	
	private static final double timeForFullThermalShutdown = 300.0;
	
	private double typicalActivePower;
	private double typicalReactivePower;
	private double typicalThermalPower;
	private int typicalGasPower;
	private double cosPhi;
	private boolean isOn;
	
//	private boolean printDebug = false;
	
	private int actualActivePower;
	private int actualReactivePower;
	private int actualThermalPower;
	private int actualGasPower;
	
	private int avgActualActivePower;
	private int avgActualReactivePower;
	private int avgActualThermalPower;
	private int avgActualGasPower;
	
	private double lastThermalPower;
	
	private long runningSince = -1;
	private long stoppedSince = -1;
	
	/**
	 * CONSTRUCTOR for cloning
	 */
	protected GenericChpModel() {
		// NOTHING
	}
	
	/**
	 * CONSTRUCTOR
	 */
	public GenericChpModel(
			double typicalActivePower, 
			double typicalReactivePower, 
			double typicalThermalPower, 
			double typicalGasPower,
			double cosPhi, 
			boolean isOn, 
			int lastThermalPower, 
			Long runningSince, 
			Long stoppedSince) {
		this.typicalActivePower = typicalActivePower;
		this.typicalReactivePower = typicalReactivePower;
		this.typicalThermalPower = typicalThermalPower;
		this.typicalGasPower = (int) Math.round(typicalGasPower);
		this.cosPhi = cosPhi;
		this.isOn = isOn;
		this.lastThermalPower = lastThermalPower;
		this.runningSince = runningSince != null ? runningSince : Long.MAX_VALUE;
		this.stoppedSince = stoppedSince != null ? stoppedSince : Long.MAX_VALUE;
	}

	public void setRunning(boolean isRunning, long timeStamp) {
		if (isRunning != this.isOn) {
//			System.out.println("[" + timeStamp + "] Change State to: " + isRunning);
			this.isOn = isRunning;
			if (isRunning) {
				runningSince = timeStamp;
			} else {				
				stoppedSince = timeStamp;
				/*
				 * if chp is turned off we have to update the lastThermalPower,
				 * otherwise if the chp was turned off at T, it will be the
				 * thermal power the chp was outputting at T-1 because it is
				 * only updated when power values are questioned
				 */
				double runTime = timeStamp - runningSince;
				if (runTime < timeForFullThermalPower)
					lastThermalPower = (int) Math.round((runTime / timeForFullThermalPower) * typicalThermalPower);
				else
					lastThermalPower = typicalThermalPower;
			}
		}
	}
	
//	public void setPrintDebug() {
//		this.printDebug = true;
//	}
	
	public void calcPower(long timeStamp) {
		if (isOn) {
			double runTime = timeStamp - runningSince;
			actualActivePower = (int) Math.round(typicalActivePower);
			actualReactivePower = (int) Math.round(typicalReactivePower);
			actualThermalPower = (int) Math.round(typicalThermalPower);
			actualGasPower = (int) Math.round(typicalGasPower);
//			String prString = "Startup, time=" + timeStamp + ", runTime=" + runTime;
			if (runTime < timeForFullActivePower) {
				actualActivePower = (int) Math.round((0.9 + (0.1 * runTime / timeForFullActivePower)) * this.typicalActivePower);
				try {
					actualReactivePower = (int) Math.round(ComplexPowerUtil.convertActiveToReactivePower(actualActivePower, cosPhi, true));
				} catch (Exception e) {
					e.printStackTrace();
				}
//				prString += ", power: act=" + actualActivePower + " react=" + actualReactivePower;
			}
			if (runTime < timeForFullThermalPower) {
				actualThermalPower = (int) Math.round((runTime / timeForFullThermalPower) * this.typicalThermalPower);
//				prString += ", therm=" + actualThermalPower;
			}
//			if (prString.length() > 40 && printDebug)
//				System.out.println(prString);
			this.lastThermalPower = actualThermalPower;
		} else {
			double stopTime = timeStamp - stoppedSince;
			actualActivePower = 0;
			actualReactivePower = 0;
			actualThermalPower = 0;
			actualGasPower = 0;
			
			double timeCorrected = (lastThermalPower / typicalThermalPower) * timeForFullThermalShutdown;
			
			if(stopTime < timeCorrected) {
				actualThermalPower = (int) Math.round(((timeCorrected - stopTime) / timeCorrected) * this.lastThermalPower);
//				if (printDebug)
//					System.out.println("Stop, time=" + timeStamp + ", stopTime=" + stopTime + ", therm=" + actualThermalPower);
			}			
		}		
	}
	
	public void calcPowerAvg(long start, long end) {
		double timeSpan = end - start;
		if (isOn) {
			double runTimeStart = start - runningSince;
			double runTimeEnd = end - runningSince;

//			actualGasPower = typicalGasPower;
			avgActualGasPower = typicalGasPower;
			if (runTimeStart < timeForFullActivePower) {
				double startActivePowerPerc = (runTimeStart / timeForFullActivePower);	
				
				if (runTimeEnd <= timeForFullActivePower) {	
					double endActivePowerPerc = (runTimeEnd / timeForFullActivePower);
					avgActualActivePower = (int) Math.round(((((startActivePowerPerc + endActivePowerPerc) / 2.0) * 0.1) + 0.9) * typicalActivePower);
				} else {					
					double timeNotOnFullPower = (timeForFullActivePower - runTimeStart);
					avgActualActivePower = (int) Math.round(
								(((((1.0 + (startActivePowerPerc * 0.1 + 0.9)) / 2.0) * timeNotOnFullPower) 
										+ (timeSpan - timeNotOnFullPower)) / timeSpan) * typicalActivePower);
				}
//				actualActivePower = (int) Math.round(startActivePower);
				
				try {
//					actualReactivePower = (int) Math.round(ComplexPowerUtil.convertActiveToReactivePower(actualActivePower, cosPhi, true));
					avgActualReactivePower = (int) Math.round(ComplexPowerUtil.convertActiveToReactivePower(avgActualActivePower, cosPhi, true));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
//				actualActivePower = (int) Math.round(typicalActivePower);
				avgActualActivePower = (int) Math.round(typicalActivePower);
//				actualReactivePower = (int) Math.round(typicalReactivePower);
				avgActualReactivePower =  (int) Math.round(typicalReactivePower);
			}
			
			if (runTimeStart < timeForFullThermalPower) {
				double startThermalPowerPerc = (runTimeStart / timeForFullThermalPower);	
				
				if (runTimeEnd <= timeForFullThermalPower) {	
					double endThermalPowerPerc  = (runTimeEnd / timeForFullThermalPower);
					avgActualThermalPower = (int) Math.round(((startThermalPowerPerc + endThermalPowerPerc) / 2.0) * typicalThermalPower);
				} else {					
					double timeNotOnFullPower = (timeForFullThermalPower - runTimeStart);
					avgActualThermalPower = (int) Math.round(
								(((((1.0 + startThermalPowerPerc) / 2.0) * timeNotOnFullPower) 
										+ (timeSpan - timeNotOnFullPower)) / timeSpan) * typicalThermalPower);
				}
			} else {
//				actualThermalPower = (int) Math.round(typicalThermalPower);
				avgActualThermalPower = (int) Math.round(typicalThermalPower);
			}
			
			lastThermalPower = avgActualThermalPower;
		} else {
			double stopTimeStart = start - stoppedSince;
			double stopTimeEnd = end - stoppedSince;
//			actualActivePower = 0;
//			actualReactivePower = 0;
//			actualThermalPower = 0;
//			actualGasPower = 0;
			avgActualActivePower = 0;
			avgActualReactivePower = 0;
			avgActualGasPower = 0;
			
			double timeCorrected = (lastThermalPower / typicalThermalPower) * timeForFullThermalShutdown;
			
			if (stopTimeStart < timeCorrected) {
				double startThermalPower = ((timeCorrected - stopTimeStart) / timeCorrected) * this.lastThermalPower;
				
				if (stopTimeEnd < timeCorrected) {
					double endThermalPower = ((timeCorrected - stopTimeEnd) / timeCorrected) * this.lastThermalPower;
					avgActualThermalPower = (int) Math.round((startThermalPower + endThermalPower) / 2.0);		
				} else {					
					double timeOnPower = (long) (timeCorrected - stopTimeStart);
					avgActualThermalPower = (int) Math.round(((startThermalPower * timeOnPower) / 2.0) / timeSpan);				
				}
				
//				actualThermalPower = (int) Math.round(startThermalPower);
			} else {
				avgActualThermalPower = 0;
			}
		}
	}
	
	public long getRunningForAtTimestamp(long timestamp) {
		if (isOn) {
			return timestamp - runningSince;
		} else {
			return 0;
		}
	}
	
	public int getActivePower() {
		return actualActivePower;
	}	
	public int getReactivePower() {
		return actualReactivePower;
	}
	public int getThermalPower() {
		return actualThermalPower;
	}
	public int getGasPower() {
		return actualGasPower;
	}
	
	public Long getRunningSince() {
		return runningSince;
	}

	public Long getStoppedSince() {
		return stoppedSince;
	}

	public int getAvgActualActivePower() {
		return avgActualActivePower;
	}

	public int getAvgActualReactivePower() {
		return avgActualReactivePower;
	}

	public int getAvgActualThermalPower() {
		return avgActualThermalPower;
	}

	public int getAvgActualGasPower() {
		return avgActualGasPower;
	}

	public GenericChpModel clone() {
		GenericChpModel clone = new GenericChpModel();
		clone.typicalActivePower = this.typicalActivePower;
		clone.typicalReactivePower = this.typicalReactivePower;
		clone.typicalThermalPower = this.typicalThermalPower;
		clone.typicalGasPower = this.typicalGasPower;
		clone.cosPhi = this.cosPhi;
		clone.isOn = this.isOn;
		clone.actualActivePower = this.actualActivePower;
		clone.actualReactivePower = this.actualReactivePower;
		clone.actualThermalPower = this.actualThermalPower;
		clone.actualGasPower = this.actualGasPower;
		clone.avgActualActivePower = this.avgActualActivePower;
		clone.avgActualReactivePower = this.avgActualReactivePower;
		clone.avgActualThermalPower = this.avgActualThermalPower;
		clone.avgActualGasPower = this.avgActualGasPower;
		clone.lastThermalPower = this.lastThermalPower;
		clone.runningSince = this.runningSince;
		clone.stoppedSince = this.stoppedSince;
		return clone;	
	}

}
