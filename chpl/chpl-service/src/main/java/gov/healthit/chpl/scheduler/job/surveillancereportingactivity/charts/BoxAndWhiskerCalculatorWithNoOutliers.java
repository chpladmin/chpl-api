package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.util.Args;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.Statistics;

public abstract class BoxAndWhiskerCalculatorWithNoOutliers extends BoxAndWhiskerCalculator {

    private static final Double FORCE_OUTLIERS_VERY_FAR_OUT = 1000d;

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    public static BoxAndWhiskerItem calculateBoxAndWhiskerStatistics(List values, boolean stripNullAndNaNItems) {
        Args.nullNotPermitted(values, "values");

        List vlist;
        if (stripNullAndNaNItems) {
            vlist = new ArrayList(values.size());
            Iterator iterator = values.listIterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj instanceof Number) {
                    Number n = (Number) obj;
                    double v = n.doubleValue();
                    if (!Double.isNaN(v)) {
                        vlist.add(n);
                    }
                }
            }
        } else {
            vlist = values;
        }
        Collections.sort(vlist);

        double mean = Statistics.calculateMean(vlist, false);
        double median = Statistics.calculateMedian(vlist, false);
        double q1 = calculateQ1(vlist);
        double q3 = calculateQ3(vlist);

        double interQuartileRange = q3 - q1;

        double upperOutlierThreshold = q3 + (interQuartileRange * FORCE_OUTLIERS_VERY_FAR_OUT);
        double lowerOutlierThreshold = q1 - (interQuartileRange * FORCE_OUTLIERS_VERY_FAR_OUT);

        double upperFaroutThreshold = q3 + (interQuartileRange * FORCE_OUTLIERS_VERY_FAR_OUT);
        double lowerFaroutThreshold = q1 - (interQuartileRange * FORCE_OUTLIERS_VERY_FAR_OUT);

        double minRegularValue = Double.POSITIVE_INFINITY;
        double maxRegularValue = Double.NEGATIVE_INFINITY;
        double minOutlier = Double.POSITIVE_INFINITY;
        double maxOutlier = Double.NEGATIVE_INFINITY;
        List outliers = new ArrayList();

        Iterator iterator = vlist.iterator();
        while (iterator.hasNext()) {
            Number number = (Number) iterator.next();
            double value = number.doubleValue();
            if (value > upperOutlierThreshold) {
                outliers.add(number);
                if (value > maxOutlier && value <= upperFaroutThreshold) {
                    maxOutlier = value;
                }
            } else if (value < lowerOutlierThreshold) {
                outliers.add(number);
                if (value < minOutlier && value >= lowerFaroutThreshold) {
                    minOutlier = value;
                }
            } else {
                minRegularValue = Math.min(minRegularValue, value);
                maxRegularValue = Math.max(maxRegularValue, value);
            }
            minOutlier = Math.min(minOutlier, minRegularValue);
            maxOutlier = Math.max(maxOutlier, maxRegularValue);
        }

        return new BoxAndWhiskerItem(mean, median, q1, q3, minRegularValue,
                maxRegularValue, minOutlier, maxOutlier, outliers);

    }

}
