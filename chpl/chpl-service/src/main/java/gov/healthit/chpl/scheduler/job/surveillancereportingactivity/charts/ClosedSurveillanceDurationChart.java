package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.charts;

import java.util.List;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.Statistics;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData.RecordType;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceDataService;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "surveillanceActivityReportJobLogger")
public class ClosedSurveillanceDurationChart {

    public JFreeChart generateChart(List<SurveillanceData> surveillances, List<CertificationBody> allAcbs) {
       try {
           LOGGER.info("Starting to build the Closed Surveillance Duration chart.");
            JFreeChart chart = ChartFactory.createBarChart(
                "ONC-ACB Closed Surveillance Duration by Measures of Central Tendency",
                "ONC-ACB",
                "Days",
                getData(surveillances, allAcbs),
                PlotOrientation.VERTICAL,
                true, false, false
               );

            return chart;
       } finally {
           LOGGER.info("Completed to building the Closed Surveillance Duration chart.");
       }
    }

    private CategoryDataset getData(List<SurveillanceData> surveillances, List<CertificationBody> allAcbs) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        allAcbs.stream()
                .forEach(acb -> {
                    List<Integer> durationOfClosedSurveillanceValues = SurveillanceDataService.getDataForAcb(surveillances, acb.getName()).stream()
                            .filter(item -> item != null && item.getRecordType().equals(RecordType.UPDATE))
                            .map(item -> item.getDurationOfClosedSurveillance())
                            .collect(Collectors.toList());

                    dataset.addValue(Statistics.getMean(durationOfClosedSurveillanceValues), "Mean", acb.getName());
                    dataset.addValue(Statistics.getMedian(durationOfClosedSurveillanceValues), "Median", acb.getName());
                    dataset.addValue(Statistics.getMode(durationOfClosedSurveillanceValues), "Mode", acb.getName());
                });

        return dataset;
    }
}
