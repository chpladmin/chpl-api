package gov.healthit.chpl.scheduler.job.summarystatistics.chart;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DevelopersOverTimeChart {
    private static final int DATE_COLUMN = 0;
    private static final int DEVELOPER_ALL_COLUMN = 1;
    private static final int DEVELOPER_2014_COLUMN = 2;
    private static final int DEVELOPER_2015_COLUMN = 3;
    private static final double LABEL_ANGLE = 1.57;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE LLL dd yyyy");

    public JFreeChart generate(File csv) throws IOException {
        List<DeveloperOverTime> reportData = getDataFromCsv(csv);

        JFreeChart chart = ChartFactory.createTimeSeriesChart("Developers Over Time",
                "Date", "Developer Count", createDataSet(reportData));


        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM yyyy"));
        axis.setTickUnit(new DateTickUnit(DateTickUnitType.MONTH, 4));
        axis.setVerticalTickLabels(true);

        //String dest = "C:/chpl/files/developerChart.jpg";
        //File file = new File(dest);
        //ChartUtils.saveChartAsJPEG(file, chart, 600, 300);
        return chart;
    }

    private XYDataset createDataSet(List<DeveloperOverTime> data) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeries series1 = new TimeSeries("Total Developers");
        TimeSeries series2 = new TimeSeries("Developers with 2014 Listings");
        TimeSeries series3 = new TimeSeries("Developers with 2015 Listings");

        for (DeveloperOverTime item : data) {
            series1.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotalDevelopers());
            series2.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotalDevelopersWith2014Listings());
            series3.add(new Day(java.sql.Date.valueOf(item.getDate())), item.getTotalDevelopersWith2015Listings());

        }
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);

        return dataset;
    }

    private List<DeveloperOverTime> getDataFromCsv(File csv) throws IOException {
        List<DeveloperOverTime> reportData = new ArrayList<DeveloperOverTime>();

        try (Reader reader = new FileReader(csv);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);) {
            for (CSVRecord csvRecord : csvParser) {
                //Skip the header record
                if (csvRecord.get(DATE_COLUMN).equalsIgnoreCase("Date")) {
                    continue;
                }
                DeveloperOverTime data = DeveloperOverTime.builder()
                        .date(LocalDate.from(formatter.parse(csvRecord.get(DATE_COLUMN))))
                        .totalDevelopers(Long.parseLong(csvRecord.get(DEVELOPER_ALL_COLUMN)))
                        .totalDevelopersWith2014Listings(Long.parseLong(csvRecord.get(DEVELOPER_2014_COLUMN)))
                        .totalDevelopersWith2015Listings(Long.parseLong(csvRecord.get(DEVELOPER_2015_COLUMN)))
                        .build();
                reportData.add(data);
            }
        }

        return reportData;
    }
}
