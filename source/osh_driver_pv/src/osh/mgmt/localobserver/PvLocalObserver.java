package osh.mgmt.localobserver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.details.energy.ElectricCurrentOCDetails;
import osh.datatypes.registry.oc.details.energy.ElectricPowerOCDetails;
import osh.datatypes.registry.oc.details.energy.ElectricVoltageOCDetails;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.PvObserverExchange;
import osh.hal.exchange.PvPredictionExchange;
import osh.mgmt.ipp.PvNonControllableIPP;
import osh.registry.interfaces.IHasState;
import osh.utils.time.TimeConversion;


/**
 * 
 * @author Florian Allerding, Ingo Mauser
 *
 */
public class PvLocalObserver extends LocalObserver implements IHasState {

	private double lastActivePowerLevel = 0.0;
	private double lastReactivePowerLevel = 0.0;
	
	private SparseLoadProfile lastDayProfile;
	private SparseLoadProfile predictedPVProfile;
	
	private int usedDaysForPrediction;
	
	private List<SparseLoadProfile> lastDayProfiles = new ArrayList<SparseLoadProfile>();
	
	private int timeRangeCounter = 0;
	private long timeFromMidnight = Long.MAX_VALUE;
	
	// Configuration variables
	private static final int profileResolutionInSec = 900;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;

	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox	
	 */
	public PvLocalObserver(IOSHOC controllerbox) {
		super(controllerbox);
		lastDayProfile = new SparseLoadProfile();
		
		//start with an empty prediction
		predictedPVProfile = lastDayProfile;
	}

	@Override
	public void onDeviceStateUpdate() {
		
		IHALExchange _oxObj = getObserverDataObject();
		
		if (_oxObj instanceof PvObserverExchange) {
			PvObserverExchange _ox = (PvObserverExchange) _oxObj;

			ElectricCurrentOCDetails _currentDetails = new ElectricCurrentOCDetails(_ox.getDeviceID(), _ox.getTimestamp());
			_currentDetails.setCurrent(_ox.getCurrent());

			ElectricPowerOCDetails _powDetails = new ElectricPowerOCDetails(_ox.getDeviceID(), _ox.getTimestamp());
			_powDetails.setActivePower(_ox.getActivePower());
			_powDetails.setReactivePower(_ox.getReactivePower());

			ElectricVoltageOCDetails _voltageDetails = new ElectricVoltageOCDetails(_ox.getDeviceID(), _ox.getTimestamp());
			_voltageDetails.setVoltage(_ox.getVoltage());

			if (_powDetails != null) {
				if (Math.abs(lastActivePowerLevel -_powDetails.getActivePower()) > 1) {
					this.lastActivePowerLevel = _powDetails.getActivePower();
					this.lastReactivePowerLevel = _powDetails.getReactivePower();

					CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
							getUUID(), 
							getTimer().getUnixTime(),
							DeviceTypes.PVSYSTEM);
					cpse.addPowerState(Commodity.ACTIVEPOWER, lastActivePowerLevel);
					cpse.addPowerState(Commodity.REACTIVEPOWER, lastReactivePowerLevel);

					this.getOCRegistry().setState(
							CommodityPowerStateExchange.class,
							this,
							cpse);
				}
			}

			//refresh time from Midnight
			long lastTimeFromMidnight = timeFromMidnight;
			timeFromMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(this.getTimer().getUnixTime());

			//monitor the load profile
			runPvProfilePredictor(_powDetails);

			//refresh the EApart
			if (lastTimeFromMidnight > timeFromMidnight) {
				//a new day has begun...
				updateEAPart();
			}
		} else if (_oxObj instanceof PvPredictionExchange) {
			PvPredictionExchange _pvPred = (PvPredictionExchange) _oxObj;
			
			usedDaysForPrediction = _pvPred.getPastDaysPrediction();
			lastDayProfiles = _pvPred.getPredicitons();
		} else if (_oxObj instanceof StaticCompressionExchange) {
			StaticCompressionExchange _stat = (StaticCompressionExchange) _oxObj;
			
			this.compressionType = _stat.getCompressionType();
			this.compressionValue = _stat.getCompressionValue();
		}
	}
	
	private void updateEAPart() {		
		long now = getTimer().getUnixTime();
		
		//Prediction is always in relative Time from midnight, we need to extend it and then convert to absolute time
		SparseLoadProfile optimizationProfile = predictedPVProfile.merge(predictedPVProfile, 86400).cloneWithOffset(now);
		
		PvNonControllableIPP ipp = new PvNonControllableIPP(getDeviceID(), getGlobalLogger(), now, optimizationProfile, compressionType, compressionValue);

		this.getOCRegistry().setState(InterdependentProblemPart.class, this, ipp);
	}

	private void runPvProfilePredictor(ElectricPowerOCDetails powerDetails){
		
		if (timeFromMidnight == 0) {
			//hooray a brand new day...let's make a new prediction
			
			if (lastDayProfile.getEndingTimeOfProfile() != 0) {
				while (lastDayProfiles.size() >= usedDaysForPrediction)
					lastDayProfiles.remove(0);
				lastDayProfile.setEndingTimeOfProfile(86400);
				lastDayProfiles.add(lastDayProfile);
			}
			predictedPVProfile = new SparseLoadProfile();
			
			for (SparseLoadProfile sp : lastDayProfiles) {
				predictedPVProfile = predictedPVProfile.merge(sp, 0);
			}
			
			predictedPVProfile.multiplyLoadsWithFactor(1.0 / (lastDayProfiles.size() != 0 ? lastDayProfiles.size() : 1));
			
			predictedPVProfile = predictedPVProfile.getProfileWithoutDuplicateValues();
			
			predictedPVProfile.setEndingTimeOfProfile(86400);
			//create a new profile for the prediction
			lastDayProfile = new SparseLoadProfile();			
		}
		else {		
			if (timeRangeCounter >= profileResolutionInSec) {
				lastDayProfile.setLoad(Commodity.ACTIVEPOWER, timeFromMidnight, powerDetails.getActivePower());
				lastDayProfile.setLoad(Commodity.REACTIVEPOWER, timeFromMidnight, powerDetails.getReactivePower());
				timeRangeCounter = 0;
			}
			else {
				++timeRangeCounter;
			}
		}
	}
	

	@Override
	public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
		return null;
	}
	
	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

}
