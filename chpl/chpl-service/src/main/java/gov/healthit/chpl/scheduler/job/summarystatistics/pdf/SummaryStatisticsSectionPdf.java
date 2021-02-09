package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailCertificationBodyStatistic;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;

public abstract class SummaryStatisticsSectionPdf {
    private static final float SECTION_DESCRIPTION = 6;
    private static final float RECENT_DATE = 3;
    private static final float PREVIOUS_DATE = 3;
    private static final float DELTA = 1;
    private static final float DEFAULT_INDENT = 20;

    private List<CertificationBodyDTO> acbs;

    public SummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO) {
        this.acbs = certificationBodyDAO.findAll();
    }

    public abstract Table generateTable(LocalDate recent, LocalDate previous, EmailStatistics recentEmailStatistics, EmailStatistics previousEmailStatistics);

    public float[] getRelativeColumnWidths() {
        return new float[] {SECTION_DESCRIPTION, RECENT_DATE, PREVIOUS_DATE, DELTA};
    }


    public Table addTableHeaderRow(Table table, List<String> headers) {
        headers.stream()
                .forEach(text -> {
                    Cell cell = new Cell();
                    Color backgroundColor = new DeviceRgb(225,238,217);
                    cell.setBackgroundColor(backgroundColor);
                    cell.setBorder(new SolidBorder(1));
                    cell.setFont(SummaryStatisticsPDFDefaults.getDefaultFont());
                    cell.setFontSize(SummaryStatisticsPDFDefaults.DEFAULT_FONT_SIZE);
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
                    cell.setFont(SummaryStatisticsPDFDefaults.getDefaultFont());
                    cell.setFontSize(SummaryStatisticsPDFDefaults.DEFAULT_FONT_SIZE);
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

        List<EmailCertificationBodyStatistic> orderedRecentAcbStats = recentEmailAcbStats.stream()
                        .sorted(Comparator.comparing(EmailCertificationBodyStatistic::getAcbName))
                        .collect(Collectors.toList());

        for (EmailCertificationBodyStatistic recentAcbStat : orderedRecentAcbStats) {
            //Find the matching stat in the previous collection
            Optional<EmailCertificationBodyStatistic> previousAcbStat =
                    getAccompanyingEmailCertificationBodyStatistic(recentAcbStat, previousEmailAcbStats);

            if (previousAcbStat.isPresent()) {
                table = addTableRow(table, createDataForRow(recentAcbStat.getAcbName(), recentAcbStat.getCount(), previousAcbStat.get().getCount()), 3);
            }
        }
        return table;
    }

    private List<EmailCertificationBodyStatistic> addMissingAcbsToCollection(List<EmailCertificationBodyStatistic> emailAcbStats) {
        for (CertificationBodyDTO acb : acbs) {
            Optional<EmailCertificationBodyStatistic> emailAcbStat = emailAcbStats.stream()
                    .filter(item -> item.getAcbName().equals(acb.getName()))
                    .findAny();
            if (emailAcbStat.isEmpty()) {
                emailAcbStats.add(EmailCertificationBodyStatistic.builder()
                        .acbName(acb.getName())
                        .count(0L)
                        .build());
            }
        }
        return emailAcbStats;
    }
}
