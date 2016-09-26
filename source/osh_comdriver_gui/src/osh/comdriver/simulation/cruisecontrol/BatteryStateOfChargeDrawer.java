package osh.comdriver.simulation.cruisecontrol;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.jfree.data.xy.XYSeries;

import osh.datatypes.cruisecontrol.GUIBatteryStorageStateExchange;


/**
 * 
 * @author Jan Mueller
 *
 */
@SuppressWarnings("serial")
class BatteryStateOfChargeDrawer extends AbstractDrawer {

	private List<XYSeries> currentSeries = null;
	private long lastentry = 0;
	
	public BatteryStateOfChargeDrawer(UUID id) {
		super("StateOfCharge " + id.toString().substring(0, 6), true);
	}
	
	@Override
	protected String getAxisName() {
		return "StateOfCharge [W/s]";
	}

	@Override
	protected List<XYSeries> getSeries(long begin, long end) {
		return (currentSeries == null ? new LinkedList<XYSeries>() : currentSeries);
	}

	@Override
	protected long getNumberOfEntries() {
		return lastentry;
	}

	public void refreshDiagram(TreeMap<Long, GUIBatteryStorageStateExchange> batteryStorageHistory) {
		List<XYSeries> series = new ArrayList<XYSeries>();
		
		XYSeries minStateOfCharge = new XYSeries("minStateOfCharge");
		XYSeries maxStateOfCharge = new XYSeries("maxStateOfCharge");
		XYSeries stateOfCharge = new XYSeries("StateOfCharge");
		
		for (Entry<Long, GUIBatteryStorageStateExchange> ex : batteryStorageHistory.entrySet()) {
			minStateOfCharge.add(ex.getKey().doubleValue() * 1000, ex.getValue().getMinStateOfCharge());
			maxStateOfCharge.add(ex.getKey().doubleValue() * 1000, ex.getValue().getMaxStateOfCharge());
			stateOfCharge.add(ex.getKey().doubleValue() * 1000, ex.getValue().getStateOfCharge());
		}
		
		series.add(minStateOfCharge);
		series.add(maxStateOfCharge);
		series.add(stateOfCharge);

		this.currentSeries = series;
		if (batteryStorageHistory.size() > 0) {
			this.lastentry = batteryStorageHistory.lastKey();
		}
		super.refreshDiagram();
	}
		
	
}