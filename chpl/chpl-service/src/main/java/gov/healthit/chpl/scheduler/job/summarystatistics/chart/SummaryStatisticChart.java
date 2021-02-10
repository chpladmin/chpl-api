package gov.healthit.chpl.scheduler.job.summarystatistics.chart;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.jfree.chart.JFreeChart;

public abstract class SummaryStatisticChart {
    public static final int MONTH_INTERVAL = 4;
    public static final int DATE_COLUMN = 0;
    public static final int DEVELOPER_ALL_COLUMN = 1;
    public static final int DEVELOPER_2014_COLUMN = 2;
    public static final int DEVELOPER_2015_COLUMN = 3;
    public static final int PRODUCT_ALL_COLUMN = 4;
    public static final int PRODUCT_ACTIVE_2014_COLUMN = 5;
    public static final int PRODUCT_ACTIVE_2015_COLUMN = 6;
    public static final int PRODUCT_ACTIVE_ALL_COLUMN = 7;
    public static final int LISTING_ALL_COLUMN = 8;
    public static final int LISTING_2014_COLUMN = 9;
    public static final int LISTING_2015_COLUMN = 10;
    public static final int LISTING_2011_COLUMN = 11;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE LLL dd yyyy");

    public abstract JFreeChart generate(File csv) throws IOException;

    public DateTimeFormatter getDateTimeFormatter() {
        return this.formatter;
    }

    public Font getTitleFont() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, 16);
    }
}
