package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

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
        sheet = generateStatisticsForAcb(sheet, "Drummond Group", 1, surveillances);
        sheet = generateStatisticsForAcb(sheet, "ICSA Labs", 25, surveillances);
        return workbook;
    }


    @SuppressWarnings("resource")
    private Sheet generateStatisticsForAcb(Sheet sheet, String acbName, Integer startingRow, List<SurveillanceData> surveillances) {
        Row row = sheet.createRow(startingRow);
        Cell acbNameCell = row.createCell(0);
        acbNameCell.setCellStyle(StatisticsWorksheetStyles.getAcbNameStyle(sheet.getWorkbook()));
        acbNameCell.setCellValue(acbName);

        row = sheet.createRow(startingRow + 1);
        Cell measuresOfCentralTendencyCell = row.createCell(0);
        measuresOfCentralTendencyCell.setCellStyle(StatisticsWorksheetStyles.getSubheaderStyle(sheet.getWorkbook()));
        measuresOfCentralTendencyCell.setCellValue("Measures of Central Tendency (days)");

        generateMinimumRow(sheet.createRow(startingRow + MIN_STAT_ROW_OFFSET), getDataForAcb(surveillances, acbName));
        generateMaximumRow(sheet.createRow(startingRow + MAX_STAT_ROW_OFFSET), getDataForAcb(surveillances, acbName));
        generateMeanRow(sheet.createRow(startingRow + MEAN_STAT_ROW_OFFSET), getDataForAcb(surveillances, acbName));
        generateMedianRow(sheet.createRow(startingRow + MEDIAN_STAT_ROW_OFFSET), getDataForAcb(surveillances, acbName));
        generateModeRow(sheet.createRow(startingRow + MODE_STAT_ROW_OFFSET), getDataForAcb(surveillances, acbName));

        return sheet;
    }

    @SuppressWarnings("resource")
    private Row generateMinimumRow(Row row, List<SurveillanceData> dataForAcb) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = null;
        row.createCell(1).setCellValue("Minimum");

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMinimum(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        return row;
    }

    @SuppressWarnings("resource")
    private Row generateMaximumRow(Row row, List<SurveillanceData> dataForAcb) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = null;
        row.createCell(1).setCellValue("Maximum");

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMaximum(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        return row;
    }

    @SuppressWarnings("resource")
    private Row generateMeanRow(Row row, List<SurveillanceData> dataForAcb) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = null;
        row.createCell(1).setCellValue("Mean");

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMean(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        return row;
    }

    @SuppressWarnings("resource")
    private Row generateMedianRow(Row row, List<SurveillanceData> dataForAcb) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = null;
        row.createCell(1).setCellValue("Median");

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMedian(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        return row;
    }

    @SuppressWarnings("resource")
    private Row generateModeRow(Row row, List<SurveillanceData> dataForAcb) {
        Workbook wb = row.getSheet().getWorkbook();
        Cell cell = null;
        row.createCell(1).setCellValue("Median");

        cell = row.createCell(ASSESS_CONFORMITY_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .filter(item -> item.getRecordType().equals(RecordType.UPDATE))
                .map(item -> item.getTimeToAssessConformity())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(APPROVE_CAP_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .map(item -> item.getTimeToApproveCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(DURATION_CAP_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .map(item -> item.getDurationOfCap())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(CAP_APPROVAL_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .map(item -> item.getTimeFromCapApprovalToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(CAP_CLOSE_TO_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .map(item -> item.getTimeFromCapCloseToSurveillanceClose())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

        cell = row.createCell(DURATION_SURV_CLOSE_COLUMN);
        cell.setCellValue(Statistics.getMode(dataForAcb.stream()
                .map(item -> item.getDurationOfClosedSurveillance())
                .collect(Collectors.toList())));
        cell.setCellStyle(StatisticsWorksheetStyles.getDefaultStatStyle(wb));

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
        return surveillances.stream()
                .filter(surv -> surv.getAcbName().equalsIgnoreCase(acbName))
                .collect(Collectors.toList());
    }
}
