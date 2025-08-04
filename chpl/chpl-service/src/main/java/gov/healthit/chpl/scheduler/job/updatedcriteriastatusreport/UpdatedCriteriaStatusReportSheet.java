package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateReport;
import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateReportService;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "updatedCriteriaStatusReportEmailJobLogger")
@Component
public class UpdatedCriteriaStatusReportSheet {
    private static final Integer DATE_ROW_IDX = 0;
    private static final Integer REQUIRES_UPDATE_ROW_IDX = 1;
    private static final Integer LISTING_COUNT_ROW_IDX = 3;

    private static final Integer DESCRIPTIONS_COL_IDX = 0;

    private CriteriaUpToDateReportService reportService;

    public UpdatedCriteriaStatusReportSheet(CriteriaUpToDateReportService reportService) {
        this.reportService = reportService;
    }

    public void generateSheetForCriteriaOnDates(CertificationCriterion criterion, List<LocalDate> reportDates, Workbook workbook) {
        LOGGER.info("Generating worksheet for " + Util.formatCriteriaNumber(criterion));
        Sheet sheet = addWorksheetForCriteria(criterion, workbook);

        CellUtil.getCell(CellUtil.getRow(DATE_ROW_IDX, sheet), DESCRIPTIONS_COL_IDX).setCellValue(criterion.getNumber() + " Up-to-Date Progress");
        updateChartTitle(sheet, criterion);

        for (int i = UpdatedCriteriaStatusReportWorkbook.TOTAL_NUMBER_OF_MONTHS; i >= 1; --i) {
            LocalDate actualReportDay = reportDates.get(i - 1);
            List<CriteriaUpToDateReport> reports = reportService.getAllCriteriaUpToDateReports(actualReportDay);

            long totalActiveListingsWithCriterion = calculateActiveListingsWithCriterionCount(reports, criterion);
            long totalListingsRequiringUpdates = calculateListingsRequiringUpdatesCount(reports, criterion);

            CellUtil.getCell(CellUtil.getRow(DATE_ROW_IDX, sheet), i).setCellValue(actualReportDay);
            CellUtil.getCell(CellUtil.getRow(REQUIRES_UPDATE_ROW_IDX, sheet), i).setCellValue(totalListingsRequiringUpdates);
            CellUtil.getCell(CellUtil.getRow(LISTING_COUNT_ROW_IDX, sheet), i).setCellValue(totalActiveListingsWithCriterion);
        }
    }

    private long calculateActiveListingsWithCriterionCount(List<CriteriaUpToDateReport> reports, CertificationCriterion criterion) {
        CriteriaUpToDateReport reportForCriterion = reports.stream()
                .filter(report -> report.getCriterion().getId().equals(criterion.getId()))
                .findAny().orElse(null);
        if (reportForCriterion == null) {
            LOGGER.error("Unable to calculate active listings with criterion " + Util.formatCriteriaNumber(criterion));
            return 0;
        }
        return reportForCriterion.getActiveListingsAttestingToCriterionCount();
    }

    private long calculateListingsRequiringUpdatesCount(List<CriteriaUpToDateReport> reports, CertificationCriterion criterion) {
        CriteriaUpToDateReport reportForCriterion = reports.stream()
                .filter(report -> report.getCriterion().getId().equals(criterion.getId()))
                .findAny().orElse(null);

        if (reportForCriterion == null) {
            LOGGER.error("Unable to calculate listings requiring update count for criterion " + Util.formatCriteriaNumber(criterion));
            return 0;
        }

        return reportForCriterion.getActiveListingsAttestingToCriterionCount() - reportForCriterion.getActiveListingsUpToDateOnCriterionCount();
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

    private Sheet addWorksheetForCriteria(CertificationCriterion criterion, Workbook workbook) {
        Sheet sheet = workbook.cloneSheet(0);
        int num = workbook.getSheetIndex(sheet);
        workbook.setSheetName(num, Util.formatCriteriaNumber(criterion));
        return sheet;
    }
}
