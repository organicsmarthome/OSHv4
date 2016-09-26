package osh.mgmt.localobserver.heating;

import java.util.ArrayList;
import java.util.List;

import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OCUnitException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.HotWaterDemandObserverExchange;
import osh.hal.exchange.prediction.WaterDemandPredictionExchange;
import osh.mgmt.ipp.HotWaterDemandNonControllableIPP;
import osh.mgmt.localobserver.ThermalDemandLocalObserver;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class SpaceHeatingLocalObserver
					extends ThermalDemandLocalObserver {
	
	private float weightForOtherWeekday;
	private float weightForSameWeekday;
	private int pastDaysPrediction;
	
	private List<SparseLoadProfile> lastDayProfiles = new ArrayList<SparseLoadProfile>();
	
	private SparseLoadProfile lastDayProfile;
	private SparseLoadProfile predictedWaterDemand;
	
	private int hotWaterPower;
	private long timeFromMidnight;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;

	
	/**
	 * CONSTRUCTOR
	 */
	public SpaceHeatingLocalObserver(IOSHOC osh) {
		super(osh);
		//no profile, yet
		lastDayProfile = new SparseLoadProfile();
		//start with an empty prediction
		predictedWaterDemand = lastDayProfile;
	}
	
	
	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		IHALExchange hx = getObserverDataObject();
		
		if (hx instanceof HotWaterDemandObserverExchange) {
			HotWaterDemandObserverExchange ox = (HotWaterDemandObserverExchange) hx;
			hotWaterPower = ox.getHotWaterPower();
			
			long now = getTimer().getUnixTime();	
			
			// set current power state
			CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
					getDeviceID(), 
					now,
					DeviceTypes.SPACEHEATING);
			cpse.addPowerState(Commodity.HEATINGHOTWATERPOWER, hotWaterPower);
			this.getOCRegistry().setState(
					CommodityPowerStateExchange.class,
					this,
					cpse);
			
			long lastTimeFromMidnight = this.timeFromMidnight;
			this.timeFromMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
			
			
			monitorLoad();
			
			boolean firstDay = getTimer().getUnixTime() - getTimer().getUnixTimeAtStart() < 86400;
			
			if (firstDay || lastTimeFromMidnight > this.timeFromMidnight) {
				//a new day has begun...
				sendIPP();
			}
			if (lastTimeFromMidnight <= this.timeFromMidnight && now % 3600 == 0) {
				double predVal =  (double) predictedWaterDemand.getLoadAt(Commodity.HEATINGHOTWATERPOWER, timeFromMidnight);
				
				if ((predVal != 0 && ((double) hotWaterPower / predVal  > 1.25 
						|| (double) hotWaterPower / predVal < 0.75))
						|| (predVal == 0 && hotWaterPower != 0)) {
					//only using the actual value for the next hour, restore the predicted value if there is no other value set in t+3600
					int oldVal = predictedWaterDemand.getLoadAt(Commodity.HEATINGHOTWATERPOWER, timeFromMidnight);
					Long nextLoadChange = predictedWaterDemand.getNextLoadChange(Commodity.HEATINGHOTWATERPOWER, timeFromMidnight);
					
					if ((nextLoadChange != null && nextLoadChange > timeFromMidnight + 3600) 
							|| (nextLoadChange == null && predictedWaterDemand.getEndingTimeOfProfile() > timeFromMidnight + 3600)) {
						predictedWaterDemand.setLoad(Commodity.HEATINGHOTWATERPOWER, timeFromMidnight + 3600, oldVal);
					}
						
					predictedWaterDemand.setLoad(Commodity.HEATINGHOTWATERPOWER, timeFromMidnight, hotWaterPower);
					sendIPP();
				}				
			}			
		} 
		else if (hx instanceof WaterDemandPredictionExchange) {
			WaterDemandPredictionExchange _pred = (WaterDemandPredictionExchange) hx;
			
			lastDayProfiles = _pred.getPredicitons();
			this.pastDaysPrediction = _pred.getPastDaysPrediction();
			this.weightForOtherWeekday = _pred.getWeightForOtherWeekday();
			this.weightForSameWeekday = _pred.getWeightForSameWeekday();
		} 
		else if (hx instanceof StaticCompressionExchange) {
			StaticCompressionExchange _stat = (StaticCompressionExchange) hx;
			this.compressionType = _stat.getCompressionType();
			this.compressionValue = _stat.getCompressionValue();
		}
	}
	
	
	private void monitorLoad(){
		
		if (timeFromMidnight == 0) {
			// a brand new day...let's make a new prediction
			
			if (lastDayProfile.getEndingTimeOfProfile() != 0) {
				while (lastDayProfiles.size() >= pastDaysPrediction)
					lastDayProfiles.remove(0);				
				lastDayProfile.setEndingTimeOfProfile(86400);
				lastDayProfiles.add(lastDayProfile);
			}
			predictedWaterDemand = new SparseLoadProfile();
			SparseLoadProfile predictedLoadToday = new SparseLoadProfile();
			SparseLoadProfile predictedLoadTomorow = new SparseLoadProfile();
			
			double weightSumToday = 0.0;
			double weightSumTomorrow = 0.0;
			
			//Prediction for today
			for (int i = 0; i < lastDayProfiles.size(); i++) {
				double weight = ((i + 1) % 7 == 0) ? weightForSameWeekday : weightForOtherWeekday;
				SparseLoadProfile weightedProfile = lastDayProfiles.get(i).clone();
				weightedProfile.multiplyLoadsWithFactor(weight);
				predictedLoadToday = predictedLoadToday.merge(weightedProfile, 0);
				weightSumToday += weight;
			}
			predictedLoadToday.multiplyLoadsWithFactor(1.0 / weightSumToday);
			predictedLoadToday.setEndingTimeOfProfile(86400);
			
			//Prediction for tomorrow
			for (int i = 0; i < lastDayProfiles.size() - 1; i++) {
				double weight = ((i + 2) % 7 == 0) ? weightForSameWeekday : weightForOtherWeekday;	
				SparseLoadProfile weightedProfile = lastDayProfiles.get(i).clone();
				weightedProfile.multiplyLoadsWithFactor(weight);
				predictedLoadTomorow = predictedLoadTomorow.merge(weightedProfile, 0);
				weightSumTomorrow += weight;
			}
			predictedLoadTomorow.multiplyLoadsWithFactor(1.0 / weightSumTomorrow);
			predictedLoadTomorow.setEndingTimeOfProfile(86400);
			
			predictedWaterDemand = predictedLoadToday.merge(predictedLoadTomorow, 86400).getProfileWithoutDuplicateValues();
			
			//create a new profile for the prediction
			lastDayProfile = new SparseLoadProfile();
			
			lastDayProfile.setLoad(
					Commodity.HEATINGHOTWATERPOWER, 
					0, 
					hotWaterPower);
		}
		else {
			lastDayProfile.setLoad(
					Commodity.HEATINGHOTWATERPOWER, 
					timeFromMidnight, 
					hotWaterPower);
		}
	}

	
	private void sendIPP() {
		long now = getTimer().getUnixTime();
		long secondsSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
		long startOfDay = now - secondsSinceMidnight;
		
		HotWaterDemandNonControllableIPP ipp = 
				new HotWaterDemandNonControllableIPP(
						getUUID(), 
						getDeviceType(),
						getGlobalLogger(), 
						now, 
						false, 
						predictedWaterDemand.cloneWithOffset(startOfDay),
						Commodity.HEATINGHOTWATERPOWER,
						compressionType,
						compressionValue);
		getOCRegistry().setState(
				InterdependentProblemPart.class, this, ipp);
	}
	
}
