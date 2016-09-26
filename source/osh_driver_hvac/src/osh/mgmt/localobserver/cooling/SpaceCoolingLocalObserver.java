package osh.mgmt.localobserver.cooling;

import java.util.ArrayList;
import java.util.Map;

import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OCUnitException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.SpaceCoolingObserverExchange;
import osh.mgmt.ipp.ChilledWaterDemandNonControllableIPP;
import osh.mgmt.localobserver.ThermalDemandLocalObserver;

/**
 * 
 * @author Ingo Mauser, Julian Feder
 *
 */
public class SpaceCoolingLocalObserver
					extends ThermalDemandLocalObserver {
	
	private ArrayList<ChillerCalendarDate> dates;
	private Map<Long, Double> temperaturePrediction;
	private int coldWaterPower;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;

	
	/**
	 * CONSTRUCTOR
	 */
	public SpaceCoolingLocalObserver(IOSHOC osh) {
		super(osh);
	}

	
	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		IHALExchange hx = getObserverDataObject();
		
		if (hx instanceof SpaceCoolingObserverExchange) {

			SpaceCoolingObserverExchange ox = (SpaceCoolingObserverExchange) hx;
			dates = ox.getDates();
			temperaturePrediction = ox.getTemperaturePrediction();
			coldWaterPower = ox.getColdWaterPower();

			ChilledWaterDemandNonControllableIPP ipp = 
					new ChilledWaterDemandNonControllableIPP(
							getDeviceID(), 
							getGlobalLogger(), 
							getTimer().getUnixTime(), 
							false, 
							dates, 
							temperaturePrediction,
							compressionType,
							compressionValue);
			getOCRegistry().setState(
					InterdependentProblemPart.class, this, ipp);

			// set current power state
			CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
					getDeviceID(), 
					getTimer().getUnixTime(),
					DeviceTypes.SPACECOOLING);
			cpse.addPowerState(Commodity.COLDWATERPOWER, coldWaterPower);
			this.getOCRegistry().setState(
					CommodityPowerStateExchange.class,
					this,
					cpse);

		} else if (hx instanceof StaticCompressionExchange) {

			StaticCompressionExchange _stat = (StaticCompressionExchange) hx;
			this.compressionType = _stat.getCompressionType();
			this.compressionValue = _stat.getCompressionValue();
		}
	}

}
