package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.charts;

import java.util.List;

import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

public class BoxAndWhiskerCategoryDatasetNoOutliers extends DefaultBoxAndWhiskerCategoryDataset {
    private static final long serialVersionUID = 1507715308524190123L;

    @Override
    public void add(List list, Comparable rowKey, Comparable columnKey) {
        BoxAndWhiskerItem item = BoxAndWhiskerCalculatorWithNoOutliers.calculateBoxAndWhiskerStatistics(list, true);
        add(item, rowKey, columnKey);
    }

}
