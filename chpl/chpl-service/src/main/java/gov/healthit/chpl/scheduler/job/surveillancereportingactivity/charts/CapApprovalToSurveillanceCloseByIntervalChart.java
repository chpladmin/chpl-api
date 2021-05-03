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

import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.Statistics;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceDataService;

public class CapApprovalToSurveillanceCloseByIntervalChart {
    public JFreeChart generateChart(List<SurveillanceData> surveillances) {
        JFreeChart chart = ChartFactory.createBarChart(
            "ONC-ACB Time from CAP Approval to Surveillance Close by Frequency Intervals",
            "Surveillance Duration",
            "Frequency of Activities",
            getData(surveillances),
            PlotOrientation.VERTICAL,
            true, false, false
           );
        CategoryAxis axis = chart.getCategoryPlot().getDomainAxis();
        axis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        return chart;
    }

    private CategoryDataset getData(List<SurveillanceData> surveillances) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<String> acbNames = SurveillanceDataService.getUniqueAcbName(surveillances);

        acbNames.stream()
                .forEach(acbName -> {
                    List<Integer> timeFromCapApprovalToSurveillanceCloseValues = SurveillanceDataService.getDataForAcb(surveillances, acbName).stream()
                            .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                            .collect(Collectors.toList());

                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, null, 30), acbName, "< 31");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 31, 60), acbName, "31-60");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 61, 90), acbName, "61-90");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 91, 120), acbName, "91-120");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 121, 150), acbName, "121-150");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 151, 180), acbName, "121-180");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 181, 210), acbName, "181-210");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 211, 240), acbName, "211-240");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 241, 270), acbName, "241-270");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 271, 300), acbName, "271-300");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 301, 330), acbName, "301-330");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 331, 360), acbName, "331-360");
                    dataset.addValue(Statistics.getCountInRange(timeFromCapApprovalToSurveillanceCloseValues, 361, null), acbName, "> 360");
                });

        return dataset;
    }

}
