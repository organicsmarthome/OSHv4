package osh.mgmt.localobserver.dhw;

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
import osh.hal.exchange.prediction.VDI6002WaterDemandPredictionExchange;
import osh.mgmt.ipp.dhw.DomesticHotWaterNonControllableIPP;
import osh.mgmt.localobserver.ThermalDemandLocalObserver;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class VDI6002DomesticHotWaterLocalObserver
					extends ThermalDemandLocalObserver {
	
	private double[] correctionFactorMonth;
	private double[] correctionFactorWeekday;
	private double[][] weekDayHourProbabilities;
	private double avgYearlyDemand;
	
	private SparseLoadProfile predictedDemand;
	
	private int hotWaterPower;
	private long timeFromMidnight = Long.MAX_VALUE; // sic!
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public VDI6002DomesticHotWaterLocalObserver(IOSHOC osh) {		
		super(osh);
	}
	
	
	private void generatePrediction(long timeStamp){
		
		int weekDay = TimeConversion.convertUnixTime2CorrectedWeekdayInt(timeStamp);
		int month = TimeConversion.convertUnixTime2MonthInt(timeStamp);
		long midnight = TimeConversion.getUnixTimeStampCurrentDayMidnight(timeStamp);
		
		predictedDemand = new SparseLoadProfile();
		
		//predict demand for this day
		double dayDemand = (avgYearlyDemand / 365.0) * correctionFactorMonth[month] * correctionFactorWeekday[weekDay] * 1000;
		for (int i = 0; i < 24; i++) {
			predictedDemand.setLoad(Commodity.DOMESTICHOTWATERPOWER, midnight + i * 3600, (int) Math.round(dayDemand * weekDayHourProbabilities[weekDay][i]));
		}
		
		//predict demand for the next day
		long midNightTomorrow = TimeConversion.getStartOfXthDayAfterToday(midnight, 1);
		weekDay = TimeConversion.convertUnixTime2CorrectedWeekdayInt(midNightTomorrow);
		month = TimeConversion.convertUnixTime2MonthInt(midNightTomorrow);
		
		dayDemand = (avgYearlyDemand / 365.0) * correctionFactorMonth[month] * correctionFactorWeekday[weekDay] * 1000;
		for (int i = 0; i < 24; i++) {
			predictedDemand.setLoad(
					Commodity.DOMESTICHOTWATERPOWER, 
					midNightTomorrow + i * 3600, 
					(int) Math.round(dayDemand * weekDayHourProbabilities[weekDay][i]));
		}
		
		predictedDemand.setEndingTimeOfProfile(midNightTomorrow + 86400);
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
					DeviceTypes.DOMESTICHOTWATER);
			cpse.addPowerState(Commodity.DOMESTICHOTWATERPOWER, hotWaterPower);
			this.getOCRegistry().setState(
					CommodityPowerStateExchange.class,
					this,
					cpse);
			
			long lastTimeFromMidnight = this.timeFromMidnight;
			this.timeFromMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
			
//			boolean firstDay = getTimer().getUnixTime() - getTimer().getUnixTimeAtStart() < 86400;
			
//			if (firstDay || lastTimeFromMidnight > this.timeFromMidnight) {
			if (lastTimeFromMidnight > this.timeFromMidnight) {
				generatePrediction(now);
				//a new day has begun...
				sendIPP();
			} 			
				
		} else if (hx instanceof VDI6002WaterDemandPredictionExchange) {
			VDI6002WaterDemandPredictionExchange _pred = (VDI6002WaterDemandPredictionExchange) hx;
			
			this.correctionFactorMonth = _pred.getCorrectionFactorMonth();
			this.correctionFactorWeekday = _pred.getCorrectionFactorWeekday();
			this.weekDayHourProbabilities = _pred.getWeekDayHourProbabilities();
			this.avgYearlyDemand = _pred.getAvgYearlyDemand();

		} else if (hx instanceof StaticCompressionExchange) {
			StaticCompressionExchange _stat = (StaticCompressionExchange) hx;
			this.compressionType = _stat.getCompressionType();
			this.compressionValue = _stat.getCompressionValue();
		}		
	}
	
	private void sendIPP() {
		long now = getTimer().getUnixTime();
		
		DomesticHotWaterNonControllableIPP ipp = 
				new DomesticHotWaterNonControllableIPP(
						getUUID(), 
						getDeviceType(),
						getGlobalLogger(), 
						now, 
						false, 
						predictedDemand.clone(),
						Commodity.DOMESTICHOTWATERPOWER,
						compressionType,
						compressionValue);
		getOCRegistry().setState(
				InterdependentProblemPart.class, this, ipp);
	}

}
