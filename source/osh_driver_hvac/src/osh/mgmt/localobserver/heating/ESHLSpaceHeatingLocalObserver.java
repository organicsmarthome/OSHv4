package osh.mgmt.localobserver.heating;

import java.util.Iterator;
import java.util.Map;

import osh.core.exceptions.OCUnitException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.SpaceHeatingPredictionObserverExchange;
import osh.mgmt.ipp.heating.SpaceHeatingNonControllableIPP;
import osh.mgmt.localobserver.ThermalDemandLocalObserver;

/**
 * 
 * @author Ingo Mauser, Jan Mueller
 *
 */
public class ESHLSpaceHeatingLocalObserver 
					extends ThermalDemandLocalObserver {

	private SparseLoadProfile predictedDemand;
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public ESHLSpaceHeatingLocalObserver(IOSHOC osh) {
		super(osh);
	}

	
	@Override
	public void onDeviceStateUpdate() throws OCUnitException {
		IHALExchange ihex = getObserverDataObject();

		if (ihex instanceof SpaceHeatingPredictionObserverExchange) {
			SpaceHeatingPredictionObserverExchange ox = (SpaceHeatingPredictionObserverExchange) ihex;
			long now = getTimer().getUnixTime();

			// create SparseLoadProfile
			predictedDemand = new SparseLoadProfile();
			Map<Long, Double> map = ox.getPredictedHeatConsumptionMap();

			for (Iterator<Long> it = map.keySet().iterator(); it.hasNext();) {
				now = (long) it.next();
				Double predictedDemadAtTimeStemp = (Double) map.get(now);
				predictedDemand.setLoad(Commodity.HEATINGHOTWATERPOWER, now,
						(int) Math.round(predictedDemadAtTimeStemp));
			}
			predictedDemand.setEndingTimeOfProfile(now);

			// Send new IPP
			SpaceHeatingNonControllableIPP ipp = 
					new SpaceHeatingNonControllableIPP(
							getUUID(),
							getDeviceType(), 
							getGlobalLogger(), 
							now, 
							false, 
							predictedDemand.clone(),
							Commodity.HEATINGHOTWATERPOWER, 
							compressionType, 
							compressionValue);
			getOCRegistry().setState(InterdependentProblemPart.class, this, ipp);
		} 
		else if (ihex instanceof StaticCompressionExchange) {
			StaticCompressionExchange _stat = (StaticCompressionExchange) ihex;
			this.compressionType = _stat.getCompressionType();
			this.compressionValue = _stat.getCompressionValue();
		}
	}

}
