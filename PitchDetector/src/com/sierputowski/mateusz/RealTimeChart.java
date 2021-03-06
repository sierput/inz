package com.sierputowski.mateusz;

import java.text.SimpleDateFormat;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;

public class RealTimeChart extends JPanel {

	private final DynamicTimeSeriesCollection dataset;
	private final JFreeChart chart;

	public RealTimeChart(final String title) {
		dataset = new DynamicTimeSeriesCollection(1, 1000, new Second());
		dataset.setTimeBase(new Second(0, 0, 0, 23, 1, 2014));
		dataset.addSeries(new float[1], 0, title);
		chart = ChartFactory.createTimeSeriesChart(title, "Time", title, dataset, true, true, false);
		final XYPlot plot = chart.getXYPlot();
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setFixedAutoRange(10000);
		axis.setDateFormatOverride(new SimpleDateFormat("ss.SS"));
		final ChartPanel chartPanel = new ChartPanel(chart);
		add(chartPanel);
	}

	public void update(float[] value) {
		dataset.advanceTime();
		dataset.appendData(value);
	}

}
