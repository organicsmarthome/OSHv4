package osh.mgmt.localobserver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.BaseloadObserverExchange;
import osh.hal.exchange.BaseloadPredictionExchange;
import osh.mgmt.localobserver.baseload.ipp.BaseloadIPP;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;
import osh.utils.time.TimeConversion;


/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class BaseloadLocalObserver 
				extends LocalObserver 
				implements IHasState, IEventTypeReceiver {
	
	private final int profileResolutionInSec = 900;
	
	private float weightForOtherWeekday;
	private float weightForSameWeekday;
	private int usedDaysForPrediction;
	
	private List<SparseLoadProfile> lastDayProfiles = new ArrayList<SparseLoadProfile>();
	
	private SparseLoadProfile lastDayProfile;
	private SparseLoadProfile predictedBaseloadProfile;
	
	private long timeFromMidnight = Long.MAX_VALUE;
	private int timeRangeCounter;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	/**
	 * CONSTRUCTOR
	 */
	public BaseloadLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
		
		lastDayProfile = new SparseLoadProfile();
		predictedBaseloadProfile = lastDayProfile;
	}
	
	private void monitorBaseloadProfile(int activeBaseload, int reactiveBaseload){
		
		if (this.timeFromMidnight == 0) {
			// a brand new day...let's make a new prediction
			
			if (lastDayProfile.getEndingTimeOfProfile() != 0) {
				while (lastDayProfiles.size() >= usedDaysForPrediction)
					lastDayProfiles.remove(0);
				lastDayProfile.setEndingTimeOfProfile(86400);
				lastDayProfiles.add(lastDayProfile);
			}
			predictedBaseloadProfile = new SparseLoadProfile();
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
			predictedLoadToday.setEndingTimeOfProfile(86400);
			predictedLoadToday.multiplyLoadsWithFactor(1 / weightSumToday);
			
			//Prediction for tomorrow
			for (int i = 0; i < lastDayProfiles.size() - 1; i++) {
				double weight = ((i + 2) % 7 == 0) ? weightForSameWeekday : weightForOtherWeekday;	
				SparseLoadProfile weightedProfile = lastDayProfiles.get(i).clone();
				weightedProfile.multiplyLoadsWithFactor(weight);
				predictedLoadTomorow = predictedLoadTomorow.merge(weightedProfile, 0);
				weightSumTomorrow += weight;
			}
			predictedLoadTomorow.setEndingTimeOfProfile(86400);
			predictedLoadTomorow.multiplyLoadsWithFactor(1 / weightSumTomorrow);

			predictedBaseloadProfile = predictedLoadToday.merge(
					predictedLoadTomorow, 86400).getProfileWithoutDuplicateValues();
			
			//create a new profile for the prediction
			lastDayProfile = new SparseLoadProfile();	
		}
		else {
			if (timeFromMidnight == 86400 - 1) {
				lastDayProfile.setLoad(
						Commodity.ACTIVEPOWER, 
						timeFromMidnight, 
						activeBaseload);
				lastDayProfile.setLoad(
						Commodity.REACTIVEPOWER, 
						timeFromMidnight, 
						reactiveBaseload);
			} 
			else if (timeRangeCounter >= profileResolutionInSec) {
				lastDayProfile.setLoad(
						Commodity.ACTIVEPOWER, 
						timeFromMidnight, 
						activeBaseload);
				lastDayProfile.setLoad(
						Commodity.REACTIVEPOWER, 
						timeFromMidnight, 
						reactiveBaseload);
				timeRangeCounter = 1;
			}
			else {
				timeRangeCounter++;
			}
		}
	}

	@Override
	public void onDeviceStateUpdate() {
		
		IHALExchange _oxObj = getObserverDataObject();
		
		if (_oxObj instanceof BaseloadObserverExchange) {
			BaseloadObserverExchange _ox = (BaseloadObserverExchange) _oxObj;

			CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
					getUUID(), 
					getTimer().getUnixTime(),
					DeviceTypes.OTHER);
			
			for (Commodity c : _ox.getCommodities()) {
				int power = _ox.getPower(c);
				cpse.addPowerState(c, power);
			}
			
			this.getOCRegistry().setState(
					CommodityPowerStateExchange.class,
					this,
					cpse);
			
			long lastTimeFromMidnight = this.timeFromMidnight;
			this.timeFromMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(this.getTimer().getUnixTime());
			
			//monitor the baseload
			this.monitorBaseloadProfile(_ox.getActivePower(), _ox.getReactivePower());		
			
			if (lastTimeFromMidnight > this.timeFromMidnight) {
				//a new day has begun...
				updateIPP();
			}
		} else if (_oxObj instanceof BaseloadPredictionExchange) {
			BaseloadPredictionExchange _pred = (BaseloadPredictionExchange) _oxObj;
			
			lastDayProfiles = _pred.getPredicitons();
			this.usedDaysForPrediction = _pred.getUsedDaysForPrediction();
			this.weightForOtherWeekday = _pred.getWeightForOtherWeekday();
			this.weightForSameWeekday = _pred.getWeightForSameWeekday();
			
		} else if (_oxObj instanceof StaticCompressionExchange) {
			StaticCompressionExchange _stat = (StaticCompressionExchange) _oxObj;
			this.compressionType = _stat.getCompressionType();
			this.compressionValue = _stat.getCompressionValue();
		}
	}

	
	private void updateIPP() {
		long now = getTimer().getUnixTime();
		
		BaseloadIPP ipp = new BaseloadIPP(
				getDeviceID(), 
				getGlobalLogger(), 
				now, 
				false, 
				DeviceTypes.BASELOAD, 
				now, 
				predictedBaseloadProfile.cloneWithOffset(now),
				compressionType,
				compressionValue);

		this.getOCRegistry().setState(InterdependentProblemPart.class, this, ipp);
	}


	@Override
	public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
		return null;
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}


	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		//NOTHING
	}
	
}
