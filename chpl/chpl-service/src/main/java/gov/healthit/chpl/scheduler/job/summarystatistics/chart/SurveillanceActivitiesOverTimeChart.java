package gov.healthit.chpl.scheduler.job.summarystatistics.chart;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import gov.healthit.chpl.scheduler.job.summarystatistics.chart.domain.SurveillanceActivitiesOverTime;

public class SurveillanceActivitiesOverTimeChart extends SummaryStatisticChart {

    @Override
    public JFreeChart generate(File csv) throws IOException {
        List<SurveillanceActivitiesOverTime> reportData = getDataFromCsv(csv);

        JFreeChart chart = ChartFactory.createTimeSeriesChart("Surveillance Activities Over Time", "Date", "Surveillance Count", createDataSet(reportData));
        chart.getTitle().setFont(getTitleFont());
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);

        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM yyyy"));
        axis.setTickUnit(new DateTickUnit(DateTickUnitType.MONTH, MONTH_INTERVAL));
        axis.setVerticalTickLabels(true);

        return chart;
    }

    private XYDataset createDataSet(List<SurveillanceActivitiesOverTime> data) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeries series1 = new TimeSeries("Total Surveillance Activities");
        TimeSeries series2 = new TimeSeries("Total Open Surveillance Activities");
        TimeSeries series3 = new TimeSeries("Total Closed Surveillance Activities");

        for (SurveillanceActivitiesOverTime item : data) {
            series1.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotalSurveillanceActivities());
            series2.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotalOpenSurveillanceActivities());
            series3.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotalClosedSurveillanceActivities());

        }
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);

        return dataset;
    }

    private List<SurveillanceActivitiesOverTime> getDataFromCsv(File csv) throws IOException {
        List<SurveillanceActivitiesOverTime> reportData = new ArrayList<SurveillanceActivitiesOverTime>();

        try (Reader reader = new FileReader(csv);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);) {
            for (CSVRecord csvRecord : csvParser) {
                //Skip the header record
                if (csvRecord.get(DATE_COLUMN).equalsIgnoreCase("Date")) {
                    continue;
                }
                SurveillanceActivitiesOverTime data = SurveillanceActivitiesOverTime.builder()
                        .date(LocalDate.from(getDateTimeFormatter().parse(csvRecord.get(DATE_COLUMN))))
                        .totalSurveillanceActivities(Long.parseLong(csvRecord.get(SURVEILLANCE_ALL_COLUMN)))
                        .totalOpenSurveillanceActivities(Long.parseLong(csvRecord.get(SURVEILLANCE_OPEN_COLUMN)))
                        .totalClosedSurveillanceActivities(Long.parseLong(csvRecord.get(SURVEILLANCE_CLOSED_COLUMN)))
                        .build();
                reportData.add(data);
            }
        }

        return reportData;
    }
}
