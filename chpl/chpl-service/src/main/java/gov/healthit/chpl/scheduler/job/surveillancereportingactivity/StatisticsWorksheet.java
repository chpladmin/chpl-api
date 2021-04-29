package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData.RecordType;
import lombok.extern.log4j.Log4j2;
import one.util.streamex.StreamEx;

@Log4j2
public class StatisticsWorksheet {
    private static final Integer STATISTICS_COLUMN_WIDTH = 17 * 256;
    private static final Integer ASSESS_CONFORMITY_COLUMN = 2;
    private static final Integer APPROVE_CAP_COLUMN = 3;
    private static final Integer DURATION_CAP_COLUMN = 4;
    private static final Integer CAP_APPROVAL_TO_SURV_CLOSE_COLUMN = 5;
    private static final Integer CAP_CLOSE_TO_SURV_CLOSE_COLUMN = 6;
    private static final Integer DURATION_SURV_CLOSE_COLUMN = 7;

    private static final Integer MIN_STAT_ROW_OFFSET = 2;
    private static final Integer MAX_STAT_ROW_OFFSET = 3;
    private static final Integer MEAN_STAT_ROW_OFFSET = 4;
    private static final Integer MEDIAN_STAT_ROW_OFFSET = 5;
    private static final Integer MODE_STAT_ROW_OFFSET = 6;
    private static final Integer FREQUENCY_COUNT_HEADER_ROW_OFFSET = 7;
    private static final Integer RANGE_MAX_30_ROW_OFFSET = 8;
    private static final Integer RANGE_MAX_60_ROW_OFFSET = 9;
    private static final Integer RANGE_MAX_90_ROW_OFFSET = 10;
    private static final Integer RANGE_MAX_120_ROW_OFFSET = 11;
    private static final Integer RANGE_MAX_150_ROW_OFFSET = 12;
    private static final Integer RANGE_MAX_180_ROW_OFFSET = 13;
    private static final Integer RANGE_MAX_210_ROW_OFFSET = 14;
    private static final Integer RANGE_MAX_240_ROW_OFFSET = 15;
    private static final Integer RANGE_MAX_270_ROW_OFFSET = 16;
    private static final Integer RANGE_MAX_300_ROW_OFFSET = 17;
    private static final Integer RANGE_MAX_330_ROW_OFFSET = 18;
    private static final Integer RANGE_MAX_360_ROW_OFFSET = 19;
    private static final Integer RANGE_MAX_NONE_ROW_OFFSET = 20;
    private static final Integer RANGE_TOTAL_ROW_OFFSET = 21;


    private Workbook workbook;
    private List<String> mainHeaders = Arrays.asList(
            "Time to Assess Conformity", "Time to Approve CAP", "Duration of CAP", "Time from CAP Approval to Surveillance Close",
            "Time from CAP Close to Surveillance Close", "Duration of Closed Surveillance");

    public StatisticsWorksheet(Workbook workbook) {
        this.workbook = workbook;
    }

    public Workbook generateWorksheet(List<SurveillanceData> surveillances) {
        Sheet sheet = workbook.createSheet("Stats");
        sheet.setDisplayGridlines(false);
        sheet = generateMainHeaders(sheet);

        Integer startingRowForAcb = 1;
        List<String> acbs = getUniqueAcbNames(surveillances);
        for (String acbName : acbs) {
            sheet = generateStatisticsForAcb(sheet, acbName, startingRowForAcb, surveillances);
            startingRowForAcb += 24;
        }
        return workbook;
    }


    @SuppressWarnings("resource")
    private Sheet generateStatisticsForAcb(Sheet sheet, String acbName, Integer startingRow, List<SurveillanceData> surveillances) {
        List<SurveillanceData> surveillancesFilteredByAcb = getDataForAcb(surveillances, acbName);

        Row row = sheet.createRow(startingRow);
        Cell acbNameCell = row.createCell(0);
        acbNameCell.setCellStyle(StatisticsWorksheetStyles.getAcbNameStyle(sheet.getWorkbook()));
        acbNameCell.setCellValue(acbName);

        row = sheet.createRow(startingRow + 1);
        Cell measuresOfCentralTendencyCell = row.createCell(0);
        measuresOfCentralTendencyCell.setCellStyle(StatisticsWorksheetStyles.getSubheaderStyle(sheet.getWorkbook()));
        measuresOfCentralTendencyCell.setCellValue("Measures of Central Tendency (days)");

        generateMinimumRow(sheet.createRow(startingRow + MIN_STAT_ROW_OFFSET), surveillancesFilteredByAcb, false);
        generateMaximumRow(sheet.createRow(startingRow + MAX_STAT_ROW_OFFSET), surveillancesFilteredByAcb, true);
        generateMeanRow(sheet.createRow(startingRow + MEAN_STAT_ROW_OFFSET), surveillancesFilteredByAcb, false);
        generateMedianRow(sheet.createRow(startingRow + MEDIAN_STAT_ROW_OFFSET), surveillancesFilteredByAcb, true);
        generateModeRow(sheet.createRow(startingRow + MODE_STAT_ROW_OFFSET), surveillancesFilteredByAcb, false);

        row = sheet.createRow(startingRow + FREQUENCY_COUNT_HEADER_ROW_OFFSET);
        Cell frequencyTotalsCell = row.createCell(0);
        frequencyTotalsCell.setCellStyle(StatisticsWorksheetStyles.getSubheaderStyle(sheet.getWorkbook()));
        frequencyTotalsCell.setCellValue("Frequency Totals by 30-day Intervals (counts)");

        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_30_ROW_OFFSET), surveillancesFilteredByAcb, null, 30, false);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_60_ROW_OFFSET), surveillancesFilteredByAcb, 31, 60, true);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_90_ROW_OFFSET), surveillancesFilteredByAcb, 61, 90, false);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_120_ROW_OFFSET), surveillancesFilteredByAcb, 91, 120, true);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_150_ROW_OFFSET), surveillancesFilteredByAcb, 121, 150, false);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_180_ROW_OFFSET), surveillancesFilteredByAcb, 151, 180, true);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_210_ROW_OFFSET), surveillancesFilteredByAcb, 181, 210, false);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_240_ROW_OFFSET), surveillancesFilteredByAcb, 211, 240, true);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_270_ROW_OFFSET), surveillancesFilteredByAcb, 241, 270, false);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_300_ROW_OFFSET), surveillancesFilteredByAcb, 271, 300, true);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_330_ROW_OFFSET), surveillancesFilteredByAcb, 301, 330, false);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_360_ROW_OFFSET), surveillancesFilteredByAcb, 331, 360, true);
        generateCountRow(sheet.createRow(startingRow + RANGE_MAX_NONE_ROW_OFFSET), surveillancesFilteredByAcb, 361, null, false);
        generateTotalRow(sheet.createRow(startingRow + RANGE_TOTAL_ROW_OFFSET), surveillancesFilteredByAcb);

        return sheet;
    }

    @SuppressWarnings("resource")
    private Row generateMinimumRow(Row row, List<SurveillanceData> dataForAcb, Boolean useAlternateStyle) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = row.createCell(1);
        cell.setCellValue("Minimum");
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyleForString(wb, useAlternateStyle));

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        return row;
    }

    @SuppressWarnings("resource")
    private Row generateMaximumRow(Row row, List<SurveillanceData> dataForAcb, Boolean useAlternateStyle) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = row.createCell(1);
        cell.setCellValue("Maximum");
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyleForString(wb, useAlternateStyle));

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        return row;
    }

    @SuppressWarnings("resource")
    private Row generateMeanRow(Row row, List<SurveillanceData> dataForAcb, Boolean useAlternateStyle) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = row.createCell(1);
        cell.setCellValue("Mean");
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyleForString(wb, useAlternateStyle));

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        return row;
    }

    @SuppressWarnings("resource")
    private Row generateMedianRow(Row row, List<SurveillanceData> dataForAcb, Boolean useAlternateStyle) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = row.createCell(1);
        cell.setCellValue("Median");
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyleForString(wb, useAlternateStyle));

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        return row;
    }

    @SuppressWarnings("resource")
    private Row generateModeRow(Row row, List<SurveillanceData> dataForAcb, Boolean useAlternateStyle) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = row.createCell(1);
        cell.setCellValue("Mode");
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyleForString(wb, useAlternateStyle));

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        return row;
    }

    @SuppressWarnings("resource")
    private Row generateCountRow(Row row, List<SurveillanceData> dataForAcb, Integer min, Integer max, Boolean useAlternateStyle) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = row.createCell(1);
        cell.setCellValue(getRangeText(min, max));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyleForString(wb, useAlternateStyle));

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList()), min, max));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList()), min, max));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList()), min, max));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList()), min, max));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList()), min, max));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList()), min, max));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb, useAlternateStyle));

        return row;
    }

    @SuppressWarnings("resource")
    private Row generateTotalRow(Row row, List<SurveillanceData> dataForAcb) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = row.createCell(1);
        cell.setCellValue("Total");
        cell.setCellStyle(StatisticsWorksheetStyles.getSubheaderStyle(wb));

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList()), null, null));
        cell.setCellStyle(StatisticsWorksheetStyles.getSubheaderStyle(wb));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList()), null, null));
        cell.setCellStyle(StatisticsWorksheetStyles.getSubheaderStyle(wb));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList()), null, null));
        cell.setCellStyle(StatisticsWorksheetStyles.getSubheaderStyle(wb));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList()), null, null));
        cell.setCellStyle(StatisticsWorksheetStyles.getSubheaderStyle(wb));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList()), null, null));
        cell.setCellStyle(StatisticsWorksheetStyles.getSubheaderStyle(wb));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getCountInRange(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList()), null, null));
        cell.setCellStyle(StatisticsWorksheetStyles.getSubheaderStyle(wb));

        return row;
    }

    @SuppressWarnings("resource")
    private Sheet generateMainHeaders(Sheet sheet) {
        AtomicInteger column = new AtomicInteger(2);
        Row row = sheet.createRow(0);

        mainHeaders.stream()
                .forEach(header -> {
                    Cell cell = row.createCell(column.get());
                    sheet.setColumnWidth(column.get(), STATISTICS_COLUMN_WIDTH);
                    cell.setCellValue(header);
                    cell.setCellStyle(StatisticsWorksheetStyles.getMainHeaderStyle(sheet.getWorkbook()));
                    column.incrementAndGet();
                });
        return sheet;
    }


    private List<SurveillanceData> getDataForAcb(List<SurveillanceData> surveillances, String acbName) {
        List<SurveillanceData> filteredSurveillances = surveillances.stream()
                .filter(surv -> surv.getAcbName().equalsIgnoreCase(acbName))
                .collect(Collectors.toList());
        if (filteredSurveillances != null) {
            return filteredSurveillances;
        } else {
            return new ArrayList<SurveillanceData>();
        }
    }

    private String getRangeText(Integer min, Integer max) {
        if (min == null) {
            Integer endRange = max + 1;
            return " < " + endRange.toString();
        } else if (max == null) {
            return " > " + min.toString();
        } else {
            return min.toString() + "-" + max.toString();
        }
    }

    @SuppressWarnings("resource")
    private List<String> getUniqueAcbNames(List<SurveillanceData> data) {
        return StreamEx.of(data)
                .distinct(SurveillanceData::getAcbName)
                .map(SurveillanceData::getAcbName)
                .sorted()
                .toList();
    }
}
