package osh.comdriver;

import java.util.EnumMap;
import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.db.PriceSignalThread;
import osh.comdriver.signals.PriceSignalGenerator;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.details.utility.CurrentPriceSignalLogDetails;
import osh.hal.exchange.EpsComExchange;
import osh.utils.time.TimeConversion;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public class DBPriceSignalProviderComDriver extends CALComDriver {
	
	private EnumMap<AncillaryCommodity,PriceSignal> currentPriceSignal = new EnumMap<AncillaryCommodity, PriceSignal>(AncillaryCommodity.class);
	
	private PriceSignalThread priceSignalThread;
	
	private double reactivePowerPrice = 1.0;
	private double pvFeedInPrice = 12.0;
	private double chpFeedInPrice = 8.0;
	private double naturalGasPrice = 9.0;
	
	/** Maximum time the signal is available in advance (36h) */
	private int signalPeriod = 36 * 3600;
	/** Minimum time the signal is available in advance (24h) */
	private int signalAvailableFor = 24 * 3600;
	/** Signal is constant for 15 minutes */
	private int signalConstantPeriod = 15 * 60;
	
	
	
	private String spsDBHost = "db";
	private String spsDBPort = "3306";
	private String spsDBName = "database";
	private String spsDBLoginName = "user";
	private String spsDBLoginPwd = "pw";

	
	public DBPriceSignalProviderComDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
		
//		this.spsDBHost = driverConfig.getParameter("spsdbhost");
//		this.spsDBPort = driverConfig.getParameter("spsdbport");
//		this.spsDBName = driverConfig.getParameter("spsdbname");
//		this.spsDBLoginName = driverConfig.getParameter("spsdbloginname");
//		this.spsDBLoginPwd = driverConfig.getParameter("spsdbloginpwd");
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		//processPriceSignal();
		priceSignalThread = new PriceSignalThread(getGlobalLogger(), this);
		try {
			priceSignalThread.setUpSQLConnection(
					spsDBHost,
					spsDBPort,
					spsDBName,
					spsDBLoginName,
					spsDBLoginPwd);
		} catch (ClassNotFoundException e) {
			getGlobalLogger().logError("unable to set up SQL connection for price signal provider", e);
			return;
		}
		priceSignalThread.start();	
	}

	/**
	 * communicate the price to the global observer
	 * @param pricesignal
	 * @param powerlimit
	 */
	public void processPriceSignal(PriceSignal pricesignal, PowerLimitSignal powerlimit){
		
		long now = getTimer().getUnixTime();
		
//		HashMap<VirtualCommodity,PriceSignal> map = new HashMap<>();
//		map.put(VirtualCommodity.ACTIVEPOWEREXTERNAL, pricesignal);
//		EpsComExchange epsComEx = new EpsComExchange(getDeviceID(), now, map);
//		this.notifyComManager(epsComEx);
		
		// save as current state
		CurrentPriceSignalLogDetails priceSignalDetails = new CurrentPriceSignalLogDetails(getDeviceID(), now);
		priceSignalDetails.setCommodity(pricesignal.getCommodity());
		priceSignalDetails.getPricePerUnit(pricesignal.getPrice(now));
		
		this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, pricesignal);
		
		{
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.REACTIVEPOWEREXTERNAL, reactivePowerPrice);
			this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
		}
		{
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.NATURALGASPOWEREXTERNAL, naturalGasPrice);
			this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
		}
		{
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERFEEDIN, pvFeedInPrice);
			this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
		}
		{
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, chpFeedInPrice);
			this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
		}
		
		// EPS
		EpsComExchange ex = new EpsComExchange(
				this.getDeviceID(), 
				now, 
				currentPriceSignal);
		this.notifyComManager(ex);
		
	}
	
	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		//NOTHING
	}

	private PriceSignal generatePriceSignal(AncillaryCommodity commodity, double price) {
		PriceSignal priceSignal = null;
		
		
		if ( currentPriceSignal == null ) {
			// initial price signal
			long now = getTimer().getUnixTime();
//			long diff = now % 3600;
//			if (diff < 60) {
//				now = now - diff - 3600;
//			}
//			else {
//				now = now - diff;
//			}
			
			long timeSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
			long timeTillEndOfDay = 86400 - timeSinceMidnight;
			long additionalTime = signalAvailableFor;
			
			priceSignal = PriceSignalGenerator.getConstantPriceSignal(
					commodity, 
					now - 100, 
					now + timeTillEndOfDay + additionalTime, 
					signalConstantPeriod, 
					price);
					
		}
		else {
			// generate every 12 hours
			long now = getTimer().getUnixTime();
			
			priceSignal = PriceSignalGenerator.getConstantPriceSignal(
					commodity, 
					now - 100, 
					now + signalPeriod, 
					signalConstantPeriod, 
					price);
		}
		
		return priceSignal;
	}
}
