package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class UpdatedCriteriaStatusReportSheet {
    private static final Integer TOTAL_NUMBER_OF_MONTHS = 12;
    private static final Integer MAX_DAYS_TO_CHECK_FOR_DATA = 7;

    private static final Integer DATE_ROW_IDX = 0;
    private static final Integer FULLY_UP_TO_DATE_ROW_IDX = 2;
    private static final Integer CODE_SETS_UP_TP_DATE_ROW_IDX = 3;
    private static final Integer FUNCTIONALITIES_TESTED_UP_TP_DATE_ROW_IDX = 4;
    private static final Integer STANDARDS_UP_TP_DATE_ROW_IDX = 5;
    private static final Integer LISTING_COUNT_ROW_IDX = 6;

    private static final Integer DESCRIPTIONS_COL_IDX = 0;

    private UpdatedCriterionStatusReportDao updatedCriteriaStatusReportDao;
    private SummaryStatisticsDAO summaryStatisticsDao;

    public UpdatedCriteriaStatusReportSheet(UpdatedCriterionStatusReportDao updatedCriteriaStatusReportDao,
            SummaryStatisticsDAO summaryStatisticsDao) {
        this.updatedCriteriaStatusReportDao = updatedCriteriaStatusReportDao;
        this.summaryStatisticsDao = summaryStatisticsDao;
    }

    public void generateSheetForCriteria(CertificationCriterion criterion, Workbook workbook) {
        Sheet sheet = addWorksheetForCriteria(criterion, workbook);

        CellUtil.getCell(CellUtil.getRow(DATE_ROW_IDX, sheet), DESCRIPTIONS_COL_IDX).setCellValue(criterion.getNumber() + " Up-to-Date Progress");
        updateChartTitle(sheet, criterion);

        for (int i = TOTAL_NUMBER_OF_MONTHS; i >= 1; --i) {
            LocalDate actualReportDay = findDateWithSummaryStatisticsAndUpdateCriterionStatusData(LocalDate.now());
            List<UpdatedCriterionStatusReport> criterionStatusReports = getCriterionStatusReportsForDateAndCriterion(actualReportDay, criterion);
            StatisticsSnapshot statisticsSnapshot = getSummaryStatisticsSnapshotForDate(actualReportDay);

            long totalActiveListingsWithCriterion = calculateActiveListingsWithCriterionCount(statisticsSnapshot, criterion);
            long standardsUpToDate = calculateStandardsUpToDateCount(reports);

            CellUtil.getCell(CellUtil.getRow(DATE_ROW_IDX, sheet), i).setCellValue(actualReportDay);
            //TODO calculate these things
//            CellUtil.getCell(CellUtil.getRow(FULLY_UP_TO_DATE_ROW_IDX, sheet), i).setCellValue(report.getFullyUpToDateCount());
//            CellUtil.getCell(CellUtil.getRow(CODE_SETS_UP_TP_DATE_ROW_IDX, sheet), i).setCellValue(report.getCodeSetsUpToDateCount());
//            CellUtil.getCell(CellUtil.getRow(FUNCTIONALITIES_TESTED_UP_TP_DATE_ROW_IDX, sheet), i).setCellValue(report.getFunctionalitiesTestedUpToDateCount());
//            CellUtil.getCell(CellUtil.getRow(STANDARDS_UP_TP_DATE_ROW_IDX, sheet), i).setCellValue(report.getStandardsUpToDateCount());
            //Really TODO how to get total active listings with criteria on each day
//            CellUtil.getCell(CellUtil.getRow(LISTING_COUNT_ROW_IDX, sheet), i).setCellValue(report.getListingsWithCriterionCount());

            actualReportDay = actualReportDay.minusMonths(1);
        }
    }

    private long calculateActiveListingsWithCriterionCount(StatisticsSnapshot statisticsSnapshot, CertificationCriterion criterion) {
        if (CollectionUtils.isEmpty(statisticsSnapshot.getAttestedCriterionStatistics())) {
            return 0;
        }
        statisticsSnapshot.getAttestedCriterionStatistics().stream()
            .filter(stat -> stat.getCertificationCriterionId().equals(criterion.getId())
                    && CertificationStatusUtil.stat.getListingStatusId())
    }

    private long calculateStandardsUpToDate(List<UpdatedCriterionStatusReport> reports, CertificationCriterion criterion) {
        //how many distinct listings have reasons about standards
        return 0;
    }

    private long calculateFunctionalitiesTestedUpToDate(List<UpdatedCriterionStatusReport> reports, CertificationCriterion criterion) {
        //how many distinct listings have reasons about functionalities tested
        return 0;
    }

    private long calculateCodeSetsUpToDate(List<UpdatedCriterionStatusReport> reports, CertificationCriterion criterion) {
        //how many distinct listings have reasons about code sets
        return 0;
    }

    private void updateChartTitle(Sheet sheet, CertificationCriterion criterion) {
        XSSFDrawing drawing = ((XSSFSheet) sheet).getDrawingPatriarch();
        if (drawing != null) {
            List<XSSFChart> charts = drawing.getCharts();
            if (charts != null && charts.size() > 0) {
                charts.get(0).setTitleText(Util.formatCriteriaNumber(criterion) + " Up-to-Date Progress");
            }
        }
    }

    private LocalDate findDateWithSummaryStatisticsAndUpdateCriterionStatusData(LocalDate reportDate) {
        List<UpdatedCriterionStatusReport> criterionStatusReports = null;
        StatisticsSnapshot statisticsSnapshot = null;

        for (Integer offset : getDayOffsetList()) {
            LocalDate possibleReportDate = reportDate.plusDays(offset);
            criterionStatusReports = updatedCriteriaStatusReportDao.getUpdatedCriterionStatusReportsByDay(possibleReportDate);
            statisticsSnapshot = summaryStatisticsDao.getSummaryStatistics(possibleReportDate);
            if (!CollectionUtils.isEmpty(criterionStatusReports) && statisticsSnapshot != null) {
                return possibleReportDate;
            }
        }

        // we don't really ever expect to get to this point - there must be a date with both reports having data
        return reportDate;
    }

    private List<UpdatedCriterionStatusReport> getCriterionStatusReportsForDateAndCriterion(LocalDate reportDate, CertificationCriterion criterion) {
        return updatedCriteriaStatusReportDao.getUpdatedCriterionStatusReportsByDay(reportDate).stream()
                .filter(report -> report.getCertificationCriterion().getId().equals(criterion.getId()))
                .collect(Collectors.toList());
    }

    private StatisticsSnapshot getSummaryStatisticsSnapshotForDate(LocalDate reportDate) {
        return summaryStatisticsDao.getSummaryStatistics(reportDate);
    }

    private Sheet addWorksheetForCriteria(CertificationCriterion criterion, Workbook workbook) {
        Sheet sheet = workbook.cloneSheet(0);
        int num = workbook.getSheetIndex(sheet);
        workbook.setSheetName(num, Util.formatCriteriaNumber(criterion));
        return sheet;
    }

    private List<Integer> getDayOffsetList() {
        //This generates a list in the pattern 0, -1, 1, -2, 2, -3, 3 ....
        List<Integer> dayOffsets = new ArrayList<Integer>();

        for (Integer i = 0; i < MAX_DAYS_TO_CHECK_FOR_DATA; i++) {
            Integer offset = i / 2;
            if (i % 2 == 1) {
                offset = offset * -1;
            }
            dayOffsets.add(offset);
        }
        return dayOffsets;
    }

}
