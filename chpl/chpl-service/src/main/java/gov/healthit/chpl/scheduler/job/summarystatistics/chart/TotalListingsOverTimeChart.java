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

import gov.healthit.chpl.scheduler.job.summarystatistics.chart.domain.ListingsOverTime;

public class TotalListingsOverTimeChart extends SummaryStatisticChart {

    @Override
    public JFreeChart generate(File csv) throws IOException {
        List<ListingsOverTime> reportData = getDataFromCsv(csv);

        JFreeChart chart = ChartFactory.createTimeSeriesChart("Total Listings Over Time", "Date", "Listing Count", createDataSet(reportData));
        chart.getTitle().setFont(getTitleFont());
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);

        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM yyyy"));
        axis.setTickUnit(new DateTickUnit(DateTickUnitType.MONTH, MONTH_INTERVAL));
        axis.setVerticalTickLabels(true);

        return chart;
    }

    private XYDataset createDataSet(List<ListingsOverTime> data) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeries series1 = new TimeSeries("Total Listings");
        TimeSeries series2 = new TimeSeries("Total 2011 Listings");
        TimeSeries series3 = new TimeSeries("Total 2014 Listings");
        TimeSeries series4 = new TimeSeries("Total 2015 Listings");

        for (ListingsOverTime item : data) {
            series1.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotalListings());
            series2.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotal2011Listings());
            series3.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotal2014Listings());
            series4.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotal2015Listings());
        }
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);
        dataset.addSeries(series4);

        return dataset;
    }

    private List<ListingsOverTime> getDataFromCsv(File csv) throws IOException {
        List<ListingsOverTime> reportData = new ArrayList<ListingsOverTime>();

        try (Reader reader = new FileReader(csv);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);) {
            for (CSVRecord csvRecord : csvParser) {
                //Skip the header record
                if (csvRecord.get(DATE_COLUMN).equalsIgnoreCase("Date")) {
                    continue;
                }
                ListingsOverTime data = ListingsOverTime.builder()
                        .date(LocalDate.from(getDateTimeFormatter().parse(csvRecord.get(DATE_COLUMN))))
                        .totalListings(Long.parseLong(csvRecord.get(LISTING_ALL_COLUMN)))
                        .total2011Listings(Long.parseLong(csvRecord.get(LISTING_2011_COLUMN)))
                        .total2014Listings(Long.parseLong(csvRecord.get(LISTING_2014_COLUMN)))
                        .total2015Listings(Long.parseLong(csvRecord.get(LISTING_2015_COLUMN)))
                        .build();
                reportData.add(data);
            }
        }

        return reportData;
    }

}

