package osh.comdriver.simulation.cruisecontrol;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.jfree.data.xy.XYSeries;


/**
 * 
 * @author Till Schuberth, Ingo Mauser, Sebastian Kramer
 *
 */
@SuppressWarnings("serial")
class WaterTemperatureDrawer extends Abstract2AxisDrawer {

	private List<XYSeries> currentSeries1 = null;
	private List<XYSeries> currentSeries2 = null;
	private long lastentry = 0;
	
	public WaterTemperatureDrawer(UUID id) {
		super("Watertemperature " + id.toString().substring(0, 6), true);
	}
	
	@Override
	protected String getAxisName() {
		return "temperature";
	}
	
	@Override
	protected String getAxisName2() {
		return "watt";
	}

	@Override
	protected List<XYSeries> getSeries1(long begin, long end) {
		return (currentSeries1 == null ? new LinkedList<XYSeries>() : currentSeries1);
	}
	
	@Override
	protected List<XYSeries> getSeries2(long begin, long end) {
		return (currentSeries2 == null ? new LinkedList<XYSeries>() : currentSeries2);
	}

	@Override
	protected long getNumberOfEntries() {
		return lastentry;
	}

	public void refreshDiagram(TreeMap<Long, Double> tankTemperature,
			TreeMap<Long, Double> waterDemand,
			TreeMap<Long, Double> waterSupply) {
		List<XYSeries> series1 = new ArrayList<XYSeries>();
		List<XYSeries> series2 = new ArrayList<XYSeries>();
		
		XYSeries minTemp = new XYSeries("minTemp");
		XYSeries maxTemp = new XYSeries("maxTemp");
		XYSeries temp = new XYSeries("temp");
		
		XYSeries demand = new XYSeries("hotWaterDemand");
		XYSeries supply = new XYSeries("hotWaterSupply");
		
		for (Entry<Long, Double> ex : tankTemperature.entrySet()) {
			minTemp.add(ex.getKey().doubleValue() * 1000, 60);
			maxTemp.add(ex.getKey().doubleValue() * 1000, 80);
			temp.add(ex.getKey().doubleValue() * 1000, ex.getValue());
		}
		
		processDemandSupply(waterDemand, demand);
		processDemandSupply(waterSupply, supply);
		
		series1.add(minTemp);
		series1.add(maxTemp);
		series1.add(temp);
		
		series2.add(supply);
		series2.add(demand);
		

		this.currentSeries1 = series1;
		this.currentSeries2 = series2;
		if (tankTemperature.size() > 0) {
			this.lastentry = tankTemperature.lastKey();
		}
		super.refreshDiagram();
	}
	
	private void processDemandSupply(TreeMap<Long, Double> water, XYSeries toWrite) {
		
		for (Entry<Long, Double> en : water.entrySet()) {
//			double valueCorr = (en.getValue() / 1000) + 70;
			toWrite.add(en.getKey().doubleValue() * 1000, en.getValue());
		}
		
	}
		
	
}