package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.charts;

import java.util.List;
import java.util.stream.Collectors;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData.RecordType;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceDataService;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "surveillanceActivityReportJobLogger")
public class ClosedSurveillanceDurationByQuartileChart {

    public JFreeChart generateChart(List<SurveillanceData> surveillances) {
        try {
            LOGGER.info("Starting to build the Closed Surveillance Duration by Quartile chart.");
            CategoryAxis xAxis = new CategoryAxis("ONC-ACBs");
            NumberAxis yAxis = new NumberAxis("Surveillance Duration (days)");

            BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
            renderer.setFillBox(false);
            renderer.setMaximumBarWidth(25);
            renderer.setMaxOutlierVisible(false);
            renderer.setMinOutlierVisible(false);
            renderer.setMeanVisible(false);

            CategoryPlot plot = new CategoryPlot(getData(surveillances), xAxis, yAxis, renderer);

            return new JFreeChart("ONC-ACB Closed Surveillance Duration by Quartiles Around the Median", plot);
        } finally {
            LOGGER.info("Completed building the Closed Surveillance Duration by Quartile chart.");
        }
    }

    private BoxAndWhiskerCategoryDataset getData(List<SurveillanceData> surveillances) {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        List<String> acbNames = SurveillanceDataService.getUniqueAcbName(surveillances);

        acbNames.stream()
                .forEach(acbName -> {
                    List<Integer> durationOfClosedSurveillanceValues = SurveillanceDataService.getDataForAcb(surveillances, acbName).stream()
                            .filter(item -> item != null && item.getRecordType().equals(RecordType.UPDATE))
                            .map(item -> item.getDurationOfClosedSurveillance())
                            .collect(Collectors.toList());

                    dataset.add(durationOfClosedSurveillanceValues, "ONC-ACBs", acbName);
                });
        return dataset;
    }
}
