package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailCertificationBodyStatistic;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.StatisticsMassager;

public abstract class SummaryStatisticsSectionPdf {
    private static final float SECTION_DESCRIPTION_RELATIVE_WIDTH = 6;
    private static final float RECENT_DATE_RELATIVE_WIDTH = 3;
    private static final float PREVIOUS_DATE_RELATIVE_WIDTH = 3;
    private static final float DELTA_RELATIVE_WIDTH = 1;
    private static final float DEFAULT_INDENT = 20;
    private static final Integer NUMBER_OF_INDENTS_ACB_LEVEL_STAT = 3;
    public static final Integer NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT = 1;

    private StatisticsMassager statisticsMassager;

    public SummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO) {
        statisticsMassager = new StatisticsMassager(certificationBodyDAO.findAll());
    }

    public abstract Table generateTable(LocalDate recent, LocalDate previous, EmailStatistics recentEmailStatistics, EmailStatistics previousEmailStatistics);

    public float[] getRelativeColumnWidths() {
        return new float[] {SECTION_DESCRIPTION_RELATIVE_WIDTH, RECENT_DATE_RELATIVE_WIDTH, PREVIOUS_DATE_RELATIVE_WIDTH, DELTA_RELATIVE_WIDTH};
    }


    public Table addTableHeaderRow(Table table, List<String> headers) {
        headers.stream()
                .forEach(text -> {
                    Cell cell = new Cell();
                    cell.setBackgroundColor(SummaryStatisticsPdfDefaults.getTableHeaderDefaultColor());
                    cell.setBorder(new SolidBorder(1));
                    cell.setFont(SummaryStatisticsPdfDefaults.getDefaultTableHeaderFont());
                    cell.setFontSize(SummaryStatisticsPdfDefaults.DEFAULT_FONT_SIZE);
                    cell.add(new Paragraph(text));
                    table.addCell(cell);
                });
        return table;
    }

    public Table addTableRow(Table table, List<String> cells, Integer indentTimes) {
        AtomicInteger index = new AtomicInteger(0);
        cells.stream()
                .forEach(text -> {
                    Cell cell = new Cell();
                    cell.setBorder(new SolidBorder(1));
                    cell.setFont(SummaryStatisticsPdfDefaults.getDefaultFont());
                    cell.setFontSize(SummaryStatisticsPdfDefaults.DEFAULT_FONT_SIZE);
                    if (indentTimes != null && index.get() == 0) {
                        cell.setPaddingLeft(indentTimes * DEFAULT_INDENT);
                    }
                    cell.add(new Paragraph(text));
                    table.addCell(cell);
                    index.getAndIncrement();
                });
        return table;
    }

    public Table addTableRow(Table table, List<String> cells) {
        return addTableRow(table, cells, null);
    }

    public List<String> createDataForRow(String description, Long recentValue, Long previousValue) {
        return Arrays.asList(description,
                recentValue.toString(),
                previousValue.toString(),
                getDelta(recentValue, previousValue));
    }

    public String getDelta(Long recentValue, Long previousValue) {
        if (recentValue == null || previousValue == null) {
            return "N/A";
        } else {
            Long product = recentValue - previousValue;
            return product.toString();
        }
    }

    public Optional<EmailCertificationBodyStatistic> getAccompanyingEmailCertificationBodyStatistic(EmailCertificationBodyStatistic recent, List<EmailCertificationBodyStatistic> previousList) {
        return previousList.stream()
                .filter(previous -> previous.getAcbName().equals(recent.getAcbName()))
                .findAny();
    }

    public Table addAcbRows(Table table, List<EmailCertificationBodyStatistic> recentEmailAcbStats, List<EmailCertificationBodyStatistic> previousEmailAcbStats) {
        recentEmailAcbStats = addMissingAcbsToCollection(recentEmailAcbStats);
        previousEmailAcbStats = addMissingAcbsToCollection(previousEmailAcbStats);

        for (EmailCertificationBodyStatistic recentAcbStat : recentEmailAcbStats) {
            //Find the matching stat in the previous collection
            Optional<EmailCertificationBodyStatistic> previousAcbStat =
                    getAccompanyingEmailCertificationBodyStatistic(recentAcbStat, previousEmailAcbStats);

            if (previousAcbStat.isPresent()) {
                table = addTableRow(table, createDataForRow(recentAcbStat.getAcbName(), recentAcbStat.getCount(), previousAcbStat.get().getCount()), NUMBER_OF_INDENTS_ACB_LEVEL_STAT);
            }
        }
        return table;
    }

    public Table addHeaders(Table table, String sectionName, LocalDate recent, LocalDate previous) {
        List<String> headers = new ArrayList<String>();
        headers.add(sectionName);
        headers.add(recent.format(getDefaultDateFormat()));
        headers.add(previous.format(getDefaultDateFormat()));
        headers.add("Delta");
        addTableHeaderRow(table, headers);
        return table;
    }

    private DateTimeFormatter getDefaultDateFormat() {
        return DateTimeFormatter.ofPattern("LLLL dd, yyyy");
    }

    private List<EmailCertificationBodyStatistic> addMissingAcbsToCollection(List<EmailCertificationBodyStatistic> emailAcbStats) {
        return statisticsMassager.getStatistics(emailAcbStats);
    }
}
