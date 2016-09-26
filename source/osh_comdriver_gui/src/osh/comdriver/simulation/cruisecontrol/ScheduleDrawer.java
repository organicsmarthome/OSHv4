package osh.comdriver.simulation.cruisecontrol;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Stroke;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.ILoadProfile;
import osh.datatypes.power.SparseLoadProfile;


/**
 * 
 * @author Till Schuberth
 *
 */
public class ScheduleDrawer extends JPanel {

	private ChartPanel panel;
	private static final Dimension preferredSize = new Dimension(500, 270);
	private static final long serialVersionUID = 1L;
	
	private int counter = 0;

	{
		// set a theme using the new shadow generator feature available in
		// 1.0.14 - for backwards compatibility it is not enabled by default
		ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
	}

	/**
	 * A demonstration application showing how to create a simple time series
	 * chart. This example uses monthly data.
	 *
	 * @param title  the frame title.
	 */
	public ScheduleDrawer(
			List<Schedule> schedules, 
			EnumMap<AncillaryCommodity,PriceSignal> ps, 
			EnumMap<AncillaryCommodity,PowerLimitSignal> pls, 
			long currentTime) {
		super(new BorderLayout());
		
		panel = (ChartPanel) createDemoPanel(schedules, ps, pls, currentTime);
		panel.setPreferredSize(preferredSize);
		this.add(panel, BorderLayout.CENTER);
	}

	/**
	 * CONSTUCTOR
	 */
	public ScheduleDrawer() {
		super(new BorderLayout());
		
		panel = (ChartPanel) createDemoPanel();
		panel.setPreferredSize(preferredSize);
		this.add(panel, BorderLayout.CENTER);
	}

	
	/**
	 * Creates a chart.
	 *
	 * @param dataset1  a dataset.
	 * @return A chart.
	 */
	private static JFreeChart createChart(
			XYDataset dataset1, //power
			XYDataset dataset2, //costs
			XYDataset dataset3,
			long time) {

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"schedule",  // title
				"time",      // x-axis label
				"power",     // y-axis label
				dataset1,    // data
				true,        // create legend?
				true,        // generate tooltips?
				false        // generate URLs?
				);
		
		chart.setBackgroundPaint(Color.white);

		XYPlot plot = (XYPlot) chart.getPlot();        
		
		NumberAxis axis1 = new NumberAxis("power");
		NumberAxis axis2 = new NumberAxis("costs");
		axis1.setAutoRangeIncludesZero(true);
		axis1.setUpperBound(6000);
		axis1.setLowerBound(-6000);
		axis2.setAutoRangeIncludesZero(true);
		axis2.setUpperBound(50);
		axis2.setLowerBound(-10);
		plot.setRangeAxis(0, axis1);
		plot.setRangeAxis(1, axis2);
		
		plot.setDataset(1, dataset2);
		plot.mapDatasetToRangeAxis(1, 1);
		
		plot.setDataset(2, dataset3);
		plot.mapDatasetToRangeAxis(2, 0);
		
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		//TODO: SHADOWS OFF
		
		final StandardXYItemRenderer r1 = new StandardXYItemRenderer();
		final StandardXYItemRenderer r2 = new StandardXYItemRenderer();
		final StandardXYItemRenderer r3 = new StandardXYItemRenderer();
		final StandardXYItemRenderer r4 = new StandardXYItemRenderer();
		plot.setRenderer(0, r1);
		plot.setRenderer(1, r2);
		plot.setRenderer(2, r3);
		plot.setRenderer(3, r4);
		
		int numberOfSeries = 0;
		numberOfSeries += dataset1.getSeriesCount();
		numberOfSeries += dataset2.getSeriesCount();
		numberOfSeries += dataset3.getSeriesCount();
		
		Color[] color = new Color[numberOfSeries];
		
		for (int i = 0; i < numberOfSeries / 2; i++) {
			color[i] = Color.getHSBColor(i * 1.0f / (numberOfSeries / 2), 1.0f, 1.0f);
		}
		
		int i = 0;
		
		for (int j = 0; j < dataset1.getSeriesCount() / 2; j++) {
			float[] rgbcolor = Color.RGBtoHSB(
					color[i].getRed(),
					color[i].getGreen(),
					color[i].getBlue(),
					null);
			plot.getRendererForDataset(dataset1).setSeriesPaint(2 * j, Color.getHSBColor(rgbcolor[0], 1.0f, 1.0f));
			plot.getRendererForDataset(dataset1).setSeriesPaint(2 * j + 1, Color.getHSBColor(rgbcolor[0], 1.0f, 0.6f));
			i++;
		}
		for (int j = 0; j < dataset2.getSeriesCount() / 2; j++) {
			float[] rgbcolor = Color.RGBtoHSB(
					color[i].getRed(),
					color[i].getGreen(),
					color[i].getBlue(),
					null);
			plot.getRendererForDataset(dataset2).setSeriesPaint(2 * j, Color.getHSBColor(rgbcolor[0], 1.0f, 1.0f));
			plot.getRendererForDataset(dataset2).setSeriesPaint(2 * j + 1, Color.getHSBColor(rgbcolor[0], 1.0f, 0.6f));
			i++;
		}
		for (int j = 0; j < dataset3.getSeriesCount() / 2; j++) {
			float[] rgbcolor = Color.RGBtoHSB(
					color[i].getRed(),
					color[i].getGreen(),
					color[i].getBlue(),
					null);
			plot.getRendererForDataset(dataset3).setSeriesPaint(2 * j, Color.getHSBColor(rgbcolor[0], 1.0f, 1.0f));
			plot.getRendererForDataset(dataset3).setSeriesPaint(2 * j + 1, Color.getHSBColor(rgbcolor[0], 1.0f, 0.6f));
			i++;
		}
		
		// NOW line
		double upperBound = plot.getRangeAxis(1).getUpperBound();
		double lowerBound = plot.getRangeAxis(1).getLowerBound();
		
		XYSeries nowLine = new XYSeries("now");
		nowLine.add(time*1000, lowerBound);
		nowLine.add(time*1000, upperBound);
		XYSeriesCollection nowColl = new XYSeriesCollection(); //power axis
		nowColl.addSeries(nowLine);
		XYDataset nowSet = nowColl;
		
		plot.setDataset(3, nowSet);
		plot.mapDatasetToRangeAxis(3, 1);
		
		plot.getRendererForDataset(nowSet).setSeriesPaint(0, Color.DARK_GRAY);
		plot.getRendererForDataset(nowSet).setSeriesStroke(
				0, 
				(Stroke) new BasicStroke(
						2.0f, 
						BasicStroke.CAP_ROUND, 
						BasicStroke.JOIN_ROUND,
						1.0f, 
						new float[] {6.0f, 6.0f}, 
						0.0f));
		
		plot.setDomainAxis(new DateAxis());
		((DateAxis) plot.getDomainAxis()).setTimeZone(TimeZone.getTimeZone("GMT"));
		plot.getDomainAxis().setAutoRange(false);
		
		long begin = (time / 86400) * 86400 * 1000; //beginning of day
		long end = begin + 86400 * 2 * 1000;
		
		plot.getDomainAxis().setRange(begin, end);
		
		return chart;
		
	}
	
	/**
	 * Creates a dataset, consisting of two series of monthly data.
	 *
	 * @return The dataset.
	 */
	private static XYDataset[] createDataset(
			List<Schedule> schedules, 
			EnumMap<AncillaryCommodity,PriceSignal> ps, 
			EnumMap<AncillaryCommodity,PowerLimitSignal> pls,
			long currentTime) {
		
		XYSeriesCollection dataset1 = new XYSeriesCollection(); //power axis
		
		if (pls != null) {
			for (Entry<AncillaryCommodity,PowerLimitSignal> e : pls.entrySet()) {
				long time = (long) ((currentTime / 86400.0) * 86400);
				
				PowerLimitSignal currentPls = e.getValue();
				
				XYSeries upperLimit = new XYSeries(e.getKey().getCommodity() + "_upper_limit");
				XYSeries lowerLimit = new XYSeries(e.getKey().getCommodity() + "_lower_limit");
				
				while (currentPls != null && currentPls.getNextPowerLimitChange(time) != null) {
					upperLimit.add(time*1000, currentPls.getPowerUpperLimit(time));
					lowerLimit.add(time*1000, currentPls.getPowerLowerLimit(time));
					
					time = currentPls.getNextPowerLimitChange(time);
					
					upperLimit.add((time-1)*1000, currentPls.getPowerUpperLimit(time - 1));
					lowerLimit.add((time-1)*1000, currentPls.getPowerLowerLimit(time - 1));
				}
				
				dataset1.addSeries(upperLimit);
				dataset1.addSeries(lowerLimit);
			}
		}
		
		XYSeriesCollection dataset2 = new XYSeriesCollection(); //costs axis
		
		if (ps != null) {
			for (Entry<AncillaryCommodity,PriceSignal> e : ps.entrySet()) {
				long time = (long) ((currentTime / 86400.0) * 86400);
				
				PriceSignal currentPs = e.getValue();
				XYSeries currentxySeries = new XYSeries(e.getKey().getCommodity() + "_price");
				
				while (currentPs != null && currentPs.getNextPriceChange(time) != null) {
					currentxySeries.add(time*1000, currentPs.getPrice(time));
					time = currentPs.getNextPriceChange(time);
					currentxySeries.add((time-1)*1000, currentPs.getPrice(time - 1));
				}
				
				dataset2.addSeries(currentxySeries);
			}
		}
		
		XYSeriesCollection dataset3 = new XYSeriesCollection();
		
		if (schedules != null) {
//			int cntr = 1;
			SparseLoadProfile powerSum = new SparseLoadProfile();
			
			if (schedules != null) {
				for (Schedule i : schedules) {
					
					XYSeries[] powerProfile = (XYSeries[]) renderSeries(i.getProfile(), i.getScheduleName(), currentTime);
					
					for (int j = 0; j < powerProfile.length; j++) {
						dataset3.addSeries(powerProfile[j]);
					}
					
					powerSum = (SparseLoadProfile) powerSum.merge(i.getProfile(), 0);
				}
			}
			
			XYSeries[] powerProfileSum = (XYSeries[]) renderSeries(powerSum, "sum", currentTime);
			
			for (int j = 0; j < powerProfileSum.length; j++) {
				dataset3.addSeries(powerProfileSum[j]);
			}
		}
		
		return new XYDataset[] {dataset1, dataset2, dataset3};
	}
	
	private static XYSeries[] renderSeries(ILoadProfile<Commodity> i, String name, long currentTime) {
		XYSeries[] profileSeries = new XYSeries[2];
		profileSeries[0] = new XYSeries(name + "_P");
		profileSeries[1] = new XYSeries(name + "_Q");
		
		long time = (long) ((currentTime / 86400.0) * 86400);
		while (i.getNextLoadChange(Commodity.ACTIVEPOWER, time) != null) {
			profileSeries[0].add(time*1000, i.getLoadAt(Commodity.ACTIVEPOWER, time));
			time = i.getNextLoadChange(Commodity.ACTIVEPOWER, time);
			profileSeries[0].add((time-1)*1000, i.getLoadAt(Commodity.ACTIVEPOWER, time - 1));
		}
		if (time < i.getEndingTimeOfProfile() - 1) {
			profileSeries[0].add(time*1000, i.getLoadAt(Commodity.ACTIVEPOWER, time));
		}
		profileSeries[0].add((i.getEndingTimeOfProfile()-1)*1000, i.getLoadAt(Commodity.ACTIVEPOWER, i.getEndingTimeOfProfile() - 1));
		profileSeries[0].add(i.getEndingTimeOfProfile()*1000, 0);
		
		time = (long) ((currentTime / 86400.0) * 86400);
		while (i.getNextLoadChange(Commodity.REACTIVEPOWER, time) != null) {
			profileSeries[1].add(time*1000, i.getLoadAt(Commodity.REACTIVEPOWER, time));
			time = i.getNextLoadChange(Commodity.REACTIVEPOWER, time);
			profileSeries[1].add((time-1)*1000, i.getLoadAt(Commodity.REACTIVEPOWER, time - 1));
		}
		if (time < i.getEndingTimeOfProfile() - 1) {
			profileSeries[1].add(time*1000, i.getLoadAt(Commodity.REACTIVEPOWER, time));
		}
		profileSeries[1].add((i.getEndingTimeOfProfile()-1)*1000, i.getLoadAt(Commodity.REACTIVEPOWER, i.getEndingTimeOfProfile() - 1));
		profileSeries[1].add(i.getEndingTimeOfProfile()*1000, 0);
		
		return profileSeries;
	}
	
	/**
	 * Creates a panel for the demo (used by SuperDemo.java).
	 *
	 * @return A panel.
	 */
	private static ChartPanel createDemoPanel(
			List<Schedule> schedules, 
			EnumMap<AncillaryCommodity,PriceSignal> ps, 
			EnumMap<AncillaryCommodity,PowerLimitSignal> pls, 
			long currentTime) {
		ChartPanel panel = new ChartPanel(createStuffForPanel(schedules, ps, pls, currentTime));
		panel.setFillZoomRectangle(true);
		panel.setMouseWheelEnabled(true);
		return panel;
	}
	
	private static ChartPanel createDemoPanel() {
		ChartPanel panel = new ChartPanel(createStuffForPanel());
		panel.setFillZoomRectangle(true);
		panel.setMouseWheelEnabled(true);
		return panel;
	}
	
	@SuppressWarnings("unused")
	private static void showSchedule(
			List<Schedule> schedules, 
			EnumMap<AncillaryCommodity,PriceSignal> ps, 
			EnumMap<AncillaryCommodity,PowerLimitSignal> pls,
			long currentTime) {
		ScheduleDrawer demo = new ScheduleDrawer(schedules, ps, pls, currentTime);
		JFrame frame = new JFrame("Schedule");
		frame.setContentPane(demo);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}
	
	public static void showScheduleAndWait(
			List<Schedule> schedules, 
			EnumMap<AncillaryCommodity,PriceSignal> ps, 
			EnumMap<AncillaryCommodity,PowerLimitSignal> pls, 
			long currentTime) {
		
		final Object sync = new Object();
		
		ScheduleDrawer demo = new ScheduleDrawer(schedules, ps, pls, currentTime);
		JFrame frame = new JFrame("Schedule");
		frame.setContentPane(demo);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
		
		synchronized (sync) {
			try {
				sync.wait();
			} catch (InterruptedException e) {}
		}
		
		frame.dispose();
	}
	
	public void refreshDiagram(
			List<Schedule> schedules, 
			EnumMap<AncillaryCommodity,PriceSignal> ps, 
			EnumMap<AncillaryCommodity,PowerLimitSignal> pls,
			long time,
			boolean saveGraph) {
		
		JFreeChart chart = createStuffForPanel(schedules, ps, pls, time);
		
		if (!saveGraph) {
			panel.setChart(chart);
		}
		else {
			BufferedOutputStream out;
			try {
				out = new BufferedOutputStream(
						new FileOutputStream("logs/graphic_" + counter + ".png"));
				ChartUtilities.writeChartAsPNG(out, chart, 1024, 768);
				out.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			counter++;
		}
		
	}
	
	private static JFreeChart createStuffForPanel(
			List<Schedule> schedules, 
			EnumMap<AncillaryCommodity,PriceSignal> ps, 
			EnumMap<AncillaryCommodity,PowerLimitSignal> pls,
			long time) {
		XYDataset[] datasets = createDataset(schedules, ps, pls, time);
		JFreeChart chart = createChart(datasets[0], datasets[1], datasets[2], time);
		return chart;
	}
	
	private static JFreeChart createStuffForPanel() {
		return createChart(new XYSeriesCollection(), new XYSeriesCollection(), new XYSeriesCollection(), 0L);
	}
	
	public void updateTime(long timestamp) {
		//
	}
	
}