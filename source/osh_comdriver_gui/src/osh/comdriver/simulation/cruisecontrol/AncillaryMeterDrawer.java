package osh.comdriver.simulation.cruisecontrol;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Stroke;
import java.util.TimeZone;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
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
import osh.datatypes.power.AncillaryCommodityLoadProfile;


/**
 * 
 * @author Till Schuberth
 *
 */
public class AncillaryMeterDrawer extends JPanel {

	private ChartPanel panel;
	private static final Dimension preferredSize = new Dimension(500, 270);
	private static final long serialVersionUID = 1L;

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
	public AncillaryMeterDrawer(
			AncillaryCommodityLoadProfile ancillaryMeter,
			long currentTime) {
		super(new BorderLayout());
		
		panel = (ChartPanel) createDemoPanel(ancillaryMeter, currentTime);
		panel.setPreferredSize(preferredSize);
		this.add(panel, BorderLayout.CENTER);
	}

	/**
	 * CONSTUCTOR
	 */
	public AncillaryMeterDrawer() {
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
			XYDataset dataset1, //meter
			long time) {

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"ancillary meter",  // title
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
		axis1.setAutoRangeIncludesZero(true);
		axis1.setUpperBound(25000);
		axis1.setLowerBound(-25000);
		plot.setRangeAxis(0, axis1);
		
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
		
		// NOW line
		double upperBound = plot.getRangeAxis(0).getUpperBound();
		double lowerBound = plot.getRangeAxis(0).getLowerBound();
		
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
			AncillaryCommodityLoadProfile ancillaryMeter,
			long currentTime) {
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		if (ancillaryMeter != null) {
			
			for (AncillaryCommodity an : AncillaryCommodity.values()) {
				
				XYSeries[] powerProfile = (XYSeries[]) renderSeries(ancillaryMeter, an, currentTime);
				
				for (int j = 0; j < powerProfile.length; j++) {
					dataset.addSeries(powerProfile[j]);
				}				
			}			
		}
		
		return new XYDataset[] {dataset};
	}
	
	private static XYSeries[] renderSeries(AncillaryCommodityLoadProfile ancillaryMeter,
			AncillaryCommodity an, long currentTime) {
		XYSeries[] profileSeries = new XYSeries[1];
		profileSeries[0] = new XYSeries(an.getDescriptionEN());
		
		Long time = ancillaryMeter.getNextLoadChange(an, Long.MIN_VALUE);
		int value = ancillaryMeter.getLoadAt(an, time);
		profileSeries[0].add(time*1000, value);
		time = ancillaryMeter.getNextLoadChange(an, time);
		int lastValue = value;
		
		while (time != null) {
			profileSeries[0].add((time - 1)*1000, lastValue);
			lastValue = ancillaryMeter.getLoadAt(an, time);
			profileSeries[0].add(time*1000, lastValue);
			time = ancillaryMeter.getNextLoadChange(an, time);
		}
		
		profileSeries[0].add((ancillaryMeter.getEndingTimeOfProfile()-1)*1000, ancillaryMeter.getLoadAt(an, ancillaryMeter.getEndingTimeOfProfile() - 1));
		profileSeries[0].add(ancillaryMeter.getEndingTimeOfProfile()*1000, 0);
		
		return profileSeries;
	}
	
	/**
	 * Creates a panel for the demo (used by SuperDemo.java).
	 *
	 * @return A panel.
	 */
	private static ChartPanel createDemoPanel(
			AncillaryCommodityLoadProfile ancillaryMeter,
			long currentTime) {
		ChartPanel panel = new ChartPanel(createStuffForPanel(ancillaryMeter, currentTime));
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
	private static void showMeter(
			AncillaryCommodityLoadProfile ancillaryMeter,
			long currentTime) {
		AncillaryMeterDrawer demo = new AncillaryMeterDrawer(ancillaryMeter, currentTime);
		JFrame frame = new JFrame("AncillaryMeter");
		frame.setContentPane(demo);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}
	
	public void refreshDiagram(
			AncillaryCommodityLoadProfile ancillaryMeter,
			long time) {
		
		JFreeChart chart = createStuffForPanel(ancillaryMeter, time);

		panel.setChart(chart);		
	}
	
	private static JFreeChart createStuffForPanel(
			AncillaryCommodityLoadProfile ancillaryMeter,
			long time) {
		XYDataset[] datasets = createDataset(ancillaryMeter, time);
		JFreeChart chart = createChart(datasets[0], time);
		return chart;
	}
	
	private static JFreeChart createStuffForPanel() {
		return createChart(new XYSeriesCollection(), 0L);
	}
	
	public void updateTime(long timestamp) {
		//
	}
	
}