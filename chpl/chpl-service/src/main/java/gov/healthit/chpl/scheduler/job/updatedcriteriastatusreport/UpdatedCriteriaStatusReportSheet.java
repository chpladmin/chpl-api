package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;

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

    private UpdatedCriteriaStatusReportDAO updatedCriteriaStatusReportDAO;

    public UpdatedCriteriaStatusReportSheet(UpdatedCriteriaStatusReportDAO updatedCriteriaStatusReportDAO) {
        this.updatedCriteriaStatusReportDAO = updatedCriteriaStatusReportDAO;
    }

    public void generateSheetForCriteria(CertificationCriterion criterion, Workbook workbook) {
        Sheet sheet = addWorksheetForCriteria(criterion, workbook);
        LocalDate reportDate = updatedCriteriaStatusReportDAO.getMaxReportDate();

        for (int i = TOTAL_NUMBER_OF_MONTHS; i >= 1; --i) {
            UpdatedCriteriaStatusReport report = getDataFromOnOrAroundDate(reportDate, criterion);

            CellUtil.getCell(CellUtil.getRow(DATE_ROW_IDX, sheet), i).setCellValue(report.getReportDay());
            CellUtil.getCell(CellUtil.getRow(FULLY_UP_TO_DATE_ROW_IDX, sheet), i).setCellValue(report.getFullyUpToDateCount());
            CellUtil.getCell(CellUtil.getRow(CODE_SETS_UP_TP_DATE_ROW_IDX, sheet), i).setCellValue(report.getCodeSetsUpToDateCount());
            CellUtil.getCell(CellUtil.getRow(FUNCTIONALITIES_TESTED_UP_TP_DATE_ROW_IDX, sheet), i).setCellValue(report.getFunctionalitiesTestedUpToDateCount());
            CellUtil.getCell(CellUtil.getRow(STANDARDS_UP_TP_DATE_ROW_IDX, sheet), i).setCellValue(report.getStandardsUpToDateCount());
            CellUtil.getCell(CellUtil.getRow(LISTING_COUNT_ROW_IDX, sheet), i).setCellValue(report.getListingsWithCriterionCount());

            reportDate = reportDate.minusMonths(1);
        }
    }

    private UpdatedCriteriaStatusReport getDataFromOnOrAroundDate(LocalDate reportDate, CertificationCriterion criterion) {
        UpdatedCriteriaStatusReport report = null;

        for (Integer offset : getDayOffsetList()) {
            report = getReportDateForDateAndCriterion(reportDate.plusDays(offset), criterion);
            if (report != null) {
                return report;
            }
        }

        return UpdatedCriteriaStatusReport.builder()
                .reportDay(reportDate)
                .listingsWithCriterionCount(0)
                .codeSetsUpToDateCount(0)
                .functionalitiesTestedUpToDateCount(0)
                .standardsUpToDateCount(0)
                .fullyUpToDateCount(0)
                .build();
    }

    private UpdatedCriteriaStatusReport getReportDateForDateAndCriterion(LocalDate reportDate, CertificationCriterion criterion) {
        return updatedCriteriaStatusReportDAO.getUpdatedCriteriaStatusReportsByDate(reportDate).stream()
                .filter(report -> report.getCertificationCriterionId().equals(criterion.getId()))
                .findAny()
                .orElse(UpdatedCriteriaStatusReport.builder()
                        .reportDay(reportDate)
                        .listingsWithCriterionCount(0)
                        .codeSetsUpToDateCount(0)
                        .functionalitiesTestedUpToDateCount(0)
                        .standardsUpToDateCount(0)
                        .fullyUpToDateCount(0)
                        .build());
    }

    private Sheet addWorksheetForCriteria(CertificationCriterion criterion, Workbook workbook) {
        Sheet sheet = workbook.cloneSheet(0);
        int num = workbook.getSheetIndex(sheet);
        workbook.setSheetName(num, criterion.getNumber());
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
