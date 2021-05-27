package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.charts;

import java.util.List;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.Statistics;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData.RecordType;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceDataService;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "surveillanceActivityReportJobLogger")
public class ClosedSurveillanceByIntervalChart {

    public JFreeChart generateChart(List<SurveillanceData> surveillances, List<CertificationBodyDTO> allAcbs) {
        try {
            LOGGER.info("Starting to build the Closed Surveillance by Interval chart.");

            JFreeChart chart = ChartFactory.createBarChart(
                "ONC-ACB Closed Surveillance Duration by Frequency Intervals",
                "Surveillance Duration",
                "Frequency of Activities",
                getData(surveillances, allAcbs),
                PlotOrientation.VERTICAL,
                true, false, false
               );
            CategoryAxis axis = chart.getCategoryPlot().getDomainAxis();
            axis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
            return chart;
        } finally {
            LOGGER.info("Completed to building the Closed Surveillance by Interval chart.");
        }
    }

    private CategoryDataset getData(List<SurveillanceData> surveillances, List<CertificationBodyDTO> allAcbs) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        allAcbs.stream()
                .forEach(acb -> {
                    List<Integer> durationOfClosedSurveillanceValues = SurveillanceDataService.getDataForAcb(surveillances, acb.getName()).stream()
                            .filter(item -> item != null && item.getRecordType().equals(RecordType.UPDATE))
                            .map(item -> item.getDurationOfClosedSurveillance())
                            .collect(Collectors.toList());

                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, null, 30), acb.getName(), "< 31");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 31, 60), acb.getName(), "31-60");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 61, 90), acb.getName(), "61-90");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 91, 120), acb.getName(), "91-120");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 121, 150), acb.getName(), "121-150");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 151, 180), acb.getName(), "121-180");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 181, 210), acb.getName(), "181-210");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 211, 240), acb.getName(), "211-240");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 241, 270), acb.getName(), "241-270");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 271, 300), acb.getName(), "271-300");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 301, 330), acb.getName(), "301-330");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 331, 360), acb.getName(), "331-360");
                    dataset.addValue(Statistics.getCountInRange(durationOfClosedSurveillanceValues, 361, null), acb.getName(), "> 360");
                });

        return dataset;
    }

}
