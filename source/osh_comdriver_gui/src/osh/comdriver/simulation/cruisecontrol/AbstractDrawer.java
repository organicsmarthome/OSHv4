package osh.comdriver.simulation.cruisecontrol;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.TimeZone;

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

/**
 * 
 * @author Till Schuberth
 *
 */
abstract class AbstractDrawer extends JPanel {

	private ChartPanel panel;
	private static final Dimension preferredSize = new Dimension(500, 270);
	private static final long serialVersionUID = 1L;
	
	private String name;
	private boolean showPast;
	private int showDays = 2;
	
	{
		// set a theme using the new shadow generator feature available in
		// 1.0.14 - for backwards compatibility it is not enabled by default
		ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
	}

	
	/**
	 * CONSTRUCTOR
	 */
	public AbstractDrawer(String name, boolean showPast) {
		super(new BorderLayout());
		this.name = name;
		this.showPast = showPast;
		panel = (ChartPanel) createDemoPanel();
		panel.setPreferredSize(preferredSize);
		add(panel, BorderLayout.CENTER);
		panel.setVisible(true);
	}

	public String getName() {
		return name;
	}
	
	/**
	 * Creates a chart.
	 *
	 * @param dataset1  a dataset.
	 *
	 * @return A chart.
	 */
	private JFreeChart createChart(XYDataset dataset, long lastentry) {

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				name,  // title
				"time",      // x-axis label
				"temperature",     // y-axis label
				dataset,     // data
				true,        // create legend?
				true,               // generate tooltips?
				false               // generate URLs?
				);
		
		chart.setBackgroundPaint(Color.white);

		XYPlot plot = (XYPlot) chart.getPlot();        
		
		NumberAxis axis1 = new NumberAxis(getAxisName());
		axis1.setAutoRangeIncludesZero(isIncludeZero());
		plot.setRangeAxis(0, axis1);
		
		plot.setDataset(0, dataset);
		plot.mapDatasetToRangeAxis(1, 0);
		
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		//TODO: SHADOWS OFF
		
		final StandardXYItemRenderer r1 = new StandardXYItemRenderer();
		plot.setRenderer(0, r1);
		r1.setSeriesPaint(0, Color.BLUE);
		r1.setSeriesPaint(1, Color.RED);
		r1.setSeriesPaint(2, Color.GREEN);
		r1.setSeriesPaint(3, Color.BLACK);
		r1.setSeriesPaint(4, Color.ORANGE);
		
		//plot.setDomainAxis(new NumberAxis("time"));
		plot.setDomainAxis(new DateAxis());
		((DateAxis) plot.getDomainAxis()).setTimeZone(TimeZone.getTimeZone("GMT"));
		plot.getDomainAxis().setAutoRange(false);
		
		long begin = getRangeBegin(lastentry);
		long end = getRangeEnd(lastentry);
		
		plot.getDomainAxis().setRange(begin, end);
		
		return chart;
	}
	
	protected boolean isIncludeZero() {
		return false;
	}
	
	protected abstract String getAxisName();
	
	protected long getRangeBegin(long lastentry) {
		int daysintopast = 0;
		if (showPast) daysintopast = getShowDays() - 1;
		
		long ret = (lastentry / 86400 - daysintopast) * 86400 * 1000;
		if (ret < 0) ret = 0;
		
		return ret;
	}
	
	protected long getRangeEnd(long lastentry) {
		int daysintofuture = 1;
		if (!showPast) daysintofuture = getShowDays() + 1;
		
		return (lastentry / 86400 + daysintofuture) * 86400 * 1000;
	}

	protected abstract List<XYSeries> getSeries(long begin, long end);
	protected abstract long getNumberOfEntries();
	
	private XYDataset createDataset() {				
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		for (XYSeries s : getSeries(getRangeBegin(getNumberOfEntries()), getRangeEnd(getNumberOfEntries()))) {
			dataset.addSeries(s);
		}

		return dataset;
	}
		
	private ChartPanel createDemoPanel() {
		ChartPanel panel = new ChartPanel(createStuffForPanel(true));
		panel.setFillZoomRectangle(true);
		panel.setMouseWheelEnabled(true);
		return panel;
	}
	
	public void refreshDiagram() {
		JFreeChart chart = createStuffForPanel(false);
		panel.setChart(chart);
		
	}
	
	private JFreeChart createStuffForPanel(boolean empty) {
		if (empty) {
			return createChart(new XYSeriesCollection(), 0);
		} 
		else {
			XYDataset dataset = createDataset();
			JFreeChart chart = createChart(dataset, getNumberOfEntries());
			return chart;
		}
	}
	
	public int getShowDays() {
		return showDays;
	}

	public void setShowDays(int showDays) {
		this.showDays = showDays;
		refreshDiagram();
	}

}