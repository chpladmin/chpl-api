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

import gov.healthit.chpl.scheduler.job.summarystatistics.chart.domain.NonConformitiesOverTime;

public class SurveillanceNonConformitiesOverTimeChart extends SummaryStatisticChart {

    @Override
    public JFreeChart generate(File csv) throws IOException {
        List<NonConformitiesOverTime> reportData = getDataFromCsv(csv);

        JFreeChart chart = ChartFactory.createTimeSeriesChart("Surveillance Non-Conformities Over Time", "Date", "Non-Conformity Count", createDataSet(reportData));
        chart.getTitle().setFont(getTitleFont());
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);

        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM yyyy"));
        axis.setTickUnit(new DateTickUnit(DateTickUnitType.MONTH, MONTH_INTERVAL));
        axis.setVerticalTickLabels(true);

        return chart;
    }

    private XYDataset createDataSet(List<NonConformitiesOverTime> data) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeries series1 = new TimeSeries("Total Non-Conformities");
        TimeSeries series2 = new TimeSeries("Total Open Non-Conformities");
        TimeSeries series3 = new TimeSeries("Total Closed Non-Conformities");

        for (NonConformitiesOverTime item : data) {
            series1.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotalNonConformityActivities());
            series2.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotalOpenNonConformityActivities());
            series3.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotalClosedNonConformityActivities());

        }
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);

        return dataset;
    }

    private List<NonConformitiesOverTime> getDataFromCsv(File csv) throws IOException {
        List<NonConformitiesOverTime> reportData = new ArrayList<NonConformitiesOverTime>();

        try (Reader reader = new FileReader(csv);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);) {
            for (CSVRecord csvRecord : csvParser) {
                //Skip the header record
                if (csvRecord.get(DATE_COLUMN).equalsIgnoreCase("Date")) {
                    continue;
                }
                NonConformitiesOverTime data = NonConformitiesOverTime.builder()
                        .date(LocalDate.from(getDateTimeFormatter().parse(csvRecord.get(DATE_COLUMN))))
                        .totalNonConformityActivities(Long.parseLong(csvRecord.get(NON_CONFORMITY_ALL_COLUMN)))
                        .totalOpenNonConformityActivities(Long.parseLong(csvRecord.get(NON_CONFORMITY_OPEN_COLUMN)))
                        .totalClosedNonConformityActivities(Long.parseLong(csvRecord.get(NON_CONFORMITY_CLOSED_COLUMN)))
                        .build();
                reportData.add(data);
            }
        }

        return reportData;
    }

}
