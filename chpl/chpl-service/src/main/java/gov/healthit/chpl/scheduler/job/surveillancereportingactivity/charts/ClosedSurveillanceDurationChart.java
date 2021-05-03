package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.charts;

import java.util.List;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.Statistics;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData.RecordType;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceDataService;

public class ClosedSurveillanceDurationChart {

    public JFreeChart generateChart(List<SurveillanceData> surveillances) {
        JFreeChart chart = ChartFactory.createBarChart(
            "ONC-ACB Closed Surveillance Duration by Measures of Central Tendency",
            "ONC-ACB",
            "Days",
            getData(surveillances),
            PlotOrientation.VERTICAL,
            true, false, false
           );

        return chart;
    }

    private CategoryDataset getData(List<SurveillanceData> surveillances) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<String> acbNames = SurveillanceDataService.getUniqueAcbName(surveillances);

        acbNames.stream()
                .forEach(acbName -> {
                    List<Integer> durationOfClosedSurveillanceValues = SurveillanceDataService.getDataForAcb(surveillances, acbName).stream()
                            .filter(item -> item != null && item.getRecordType().equals(RecordType.UPDATE))
                            .map(item -> item.getDurationOfClosedSurveillance())
                            .collect(Collectors.toList());

                    dataset.addValue(Statistics.getMean(durationOfClosedSurveillanceValues), "Mean", acbName);
                    dataset.addValue(Statistics.getMedian(durationOfClosedSurveillanceValues), "Median", acbName);
                    dataset.addValue(Statistics.getMode(durationOfClosedSurveillanceValues), "Mode", acbName);
                });

        return dataset;
    }
}
