package osh.comdriver.simulation.cruisecontrol;

import java.awt.BorderLayout;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.swing.JFrame;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.location.CContentAreaCenterLocation;
import bibliothek.gui.dock.common.location.TreeLocationRoot;
import bibliothek.gui.dock.common.theme.ThemeMap;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewer;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewerListener;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.cruisecontrol.GUIBatteryStorageStateExchange;
import osh.datatypes.cruisecontrol.PowerSum;
import osh.datatypes.ea.Schedule;
import osh.datatypes.gui.DeviceTableEntry;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Till Schuberth, Ingo Mauser, Jan Mueller
 *
 */
public class GuiMain {

	private JFrame rootframe;
	private CControl control;
	private CContentAreaCenterLocation normalLocation;
	
	private CruiseControl cruisecontrol;
	private ScheduleDrawer scheduledrawer;
	private AncillaryMeterDrawer ancillaryMeterDrawer = null;
	private Map<UUID, WaterTemperatureDrawer> waterdrawer = new HashMap<>();
	private Map<UUID, BatteryStateOfChargeDrawer> batterydrawer = new HashMap<>();
	private WaterPredictionDrawer waterPredictionDrawer;
	private PowerDrawer powersumsdrawer;
	private DeviceTable devicetable;
	private StateViewer stateviewer;
	
	private final boolean ismultithread;
	/**
	 * CONSTRUCTOR
	 */
	public GuiMain(boolean ismultithread) {
		this.ismultithread = ismultithread;
		
		this.rootframe = new JFrame("OSH Simulation GUI");
		this.rootframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.control = new CControl( this.rootframe );
		this.rootframe.setLayout(new BorderLayout());
		this.rootframe.add( control.getContentArea(), BorderLayout.CENTER );
        control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		
		this.scheduledrawer = createScheduledrawer();		
//		this.waterdrawer = createWaterdrawer();
		this.powersumsdrawer = createPowerSumsdrawer();
		this.devicetable = createDevicetable();
		this.stateviewer = createStateViewer();
		//cruisecontrol must be the last one to be in front of the others
		this.cruisecontrol = createCruisecontrol();
		
		SingleCDockable scheduledock = new DefaultSingleCDockable("scheduledrawer", "Schedule", this.scheduledrawer);		
//		SingleCDockable waterdock = new DefaultSingleCDockable("waterdrawer", this.waterdrawer.getName(), this.waterdrawer);
		SingleCDockable powersumsdock = new DefaultSingleCDockable("powersumsdrawer", this.powersumsdrawer.getName(), this.powersumsdrawer);
		SingleCDockable devicetabledock = new DefaultSingleCDockable("devicetable", "Device Table", this.devicetable);
		SingleCDockable stateviewerdock = new DefaultSingleCDockable("stateviewer", "Registry State Viewer", this.stateviewer);
		SingleCDockable cruisecontroldock = new DefaultSingleCDockable("cruisecontrol", "OSH Simulation GUI", this.cruisecontrol);
		
		control.addDockable( scheduledock );		
//		control.addDockable( waterdock );
		control.addDockable( powersumsdock );
		control.addDockable( devicetabledock );
		control.addDockable( stateviewerdock );
		control.addDockable( cruisecontroldock );
		
		normalLocation = CLocation.base().normal();
		
		scheduledock.setLocation(normalLocation);
//		waterdock.setLocation(normal.stack());
        powersumsdock.setLocation(normalLocation.stack());
        
        TreeLocationRoot south = CLocation.base().normalSouth(0.4);
        
        devicetabledock.setLocation(south);
        stateviewerdock.setLocation(south.stack());

        TreeLocationRoot north = CLocation.base().normalNorth(0.1);

        cruisecontroldock.setLocation(north);
        
		scheduledock.setVisible(true);		
//		waterdock.setVisible(true);
		powersumsdock.setVisible(true);
		devicetabledock.setVisible(true);
		stateviewerdock.setVisible(true);
		cruisecontroldock.setVisible(true);

		this.rootframe.pack();
		this.rootframe.setBounds( 50, 50, 1000, 700 );
		this.rootframe.setVisible(true);
	}
	
	ScheduleDrawer getScheduledrawer() {
		return scheduledrawer;
	}
	
	AncillaryMeterDrawer getAncillaryMeterDrawer() {
		return ancillaryMeterDrawer;
	}
	
//	WaterTemperatureDrawer getWaterdrawer(UUID id) {
//		return waterdrawer.get(id);
//	}

	private PowerDrawer getPowerSumsDrawer() {
		return powersumsdrawer;
	}
	
	CruiseControl getCruisecontrol() {
		return cruisecontrol;
	}
	
	DeviceTable getDevicetable() {
		return devicetable;
	}
	StateViewer getStateViewer() {
		return stateviewer;
	}
	
	private static ScheduleDrawer createScheduledrawer() {
		ScheduleDrawer drawer = new ScheduleDrawer();
    	return drawer;
	}
	private static AncillaryMeterDrawer createAncillaryMeterDrawer() {
		AncillaryMeterDrawer drawer = new AncillaryMeterDrawer();
    	return drawer;
	}
	private static WaterTemperatureDrawer createWaterdrawer(UUID id) {
		WaterTemperatureDrawer drawer = new WaterTemperatureDrawer(id);
    	return drawer;
	}
	private static BatteryStateOfChargeDrawer createBatteryDrawer(UUID id) {
		BatteryStateOfChargeDrawer drawer = new BatteryStateOfChargeDrawer(id);
    	return drawer;
	}
	private PowerDrawer createPowerSumsdrawer() {
		PowerDrawer drawer = new PowerDrawer();
    	return drawer;
	}

	private CruiseControl createCruisecontrol() {
		CruiseControl showcontrol = new CruiseControl(!ismultithread);
    	return showcontrol;
	}
	private static DeviceTable createDevicetable() {
		DeviceTable devlist = new DeviceTable();
		return devlist;
	}
	private static StateViewer createStateViewer() {
		return new StateViewer();
	}
	
	public void waitForUserIfRequested() {
		if (cruisecontrol.isWait()) {
			cruisecontrol.waitForGo();
		}
	}

	public void updateTime(long timestamp) {
		cruisecontrol.updateTime(timestamp);
	}
	
	public void refreshDiagram(
			List<Schedule> schedules, 
			EnumMap<AncillaryCommodity,PriceSignal> ps, 
			EnumMap<AncillaryCommodity,PowerLimitSignal> pls,
			long time,
			boolean saveGraph) {

		if (getCruisecontrol().isUpdate()) {
			getScheduledrawer().refreshDiagram(
					schedules, 
					ps, 
					pls,
					time,
					saveGraph);
		}
	}
	
	public void refreshMeter(
			AncillaryCommodityLoadProfile ancillaryMeter,
			long time) {
		
		if (ancillaryMeterDrawer == null) {
			this.ancillaryMeterDrawer = createAncillaryMeterDrawer();			
			SingleCDockable ancillarydock = new DefaultSingleCDockable("ancillaryMeterDrawer", "AncillaryMeter", this.ancillaryMeterDrawer);
			control.addDockable(ancillarydock);
			ancillarydock.setLocation(normalLocation.stack());
			ancillarydock.setVisible(true);
		}
		getAncillaryMeterDrawer().refreshDiagram(
				ancillaryMeter,
				time);		
	}
	
	public void refreshDeviceTable(Set<DeviceTableEntry> entries) {
		getDevicetable().refreshDeviceTable(entries);
	}
	
	public void refreshStateTable(Set<Class<? extends StateExchange>> types, Map<UUID, ? extends StateExchange> states) {
		getStateViewer().showTypes(types);
		getStateViewer().showStates(states);
	}
	
	public void refreshWaterDiagram(HashMap<UUID,TreeMap<Long, Double>> tankTemps, HashMap<UUID,TreeMap<Long, Double>> hotWaterDemands, 
			HashMap<UUID,TreeMap<Long, Double>> hotWaterSupplys) {
		if (!tankTemps.isEmpty()) {
			for (UUID key : tankTemps.keySet()) {
				if (tankTemps.get(key).size() > 0) {
					if (!waterdrawer.containsKey(key)) {
						WaterTemperatureDrawer drawer = createWaterdrawer(key);
						waterdrawer.put(key, drawer);
						SingleCDockable waterdock = new DefaultSingleCDockable("waterdrawer" + key, drawer.getName(), drawer);
						control.addDockable( waterdock );
						waterdock.setLocation(normalLocation.stack());
						waterdock.setVisible(true);
					}
					waterdrawer.get(key).refreshDiagram(tankTemps.get(key), hotWaterDemands.get(key), hotWaterSupplys.get(key));
				}
			}
		}
	}
	
	public void refreshWaterPredictionDiagram(TreeMap<Long, Double> predictedTankTemp,
			TreeMap<Long, Double> predictedHotWaterDemand,
			TreeMap<Long, Double> predictedHotWaterSupply) {
		if (!predictedTankTemp.isEmpty()) {
			if (waterPredictionDrawer == null) {
				waterPredictionDrawer = new WaterPredictionDrawer();
				SingleCDockable waterdock = new DefaultSingleCDockable("waterPredictionDrawer", 
						waterPredictionDrawer.getName(), waterPredictionDrawer);
				control.addDockable( waterdock );
				waterdock.setLocation(normalLocation.stack());
				waterdock.setVisible(true);
			}
			waterPredictionDrawer.refreshDiagram(predictedTankTemp, predictedHotWaterDemand, predictedHotWaterSupply);
		}
	}

	public void refreshBatteryDiagram(HashMap<UUID,TreeMap<Long, GUIBatteryStorageStateExchange>> optimizedStorageHistories) {
		if (!optimizedStorageHistories.isEmpty()) {
			for (Entry<UUID,TreeMap<Long, GUIBatteryStorageStateExchange>> e : optimizedStorageHistories.entrySet()) {
				if (e.getValue().size() > 0) {
					if (!batterydrawer.containsKey(e.getKey())) {
						BatteryStateOfChargeDrawer drawer =  createBatteryDrawer(e.getKey());
						batterydrawer.put(e.getKey(), drawer);
						SingleCDockable batterydock = new DefaultSingleCDockable("batterydrawer" + e.getKey(), drawer.getName(), drawer);
						control.addDockable( batterydock );
						batterydock.setLocation(normalLocation.stack());
						batterydock.setVisible(true);
					}
					batterydrawer.get(e.getKey()).refreshDiagram(e.getValue());
				}
			}
		}
	}
	
	
	
	public void refreshPowerSumDiagram(long now, EnumMap<Commodity, TreeMap<Long, PowerSum>> commodityPowerSum) {
		if (commodityPowerSum.size() > 0) {
			getPowerSumsDrawer().refreshDiagram(now, commodityPowerSum);
		}
	}

	public void registerListener(StateViewerListener l) {
		stateviewer.registerListener(l);
	}
	
}
