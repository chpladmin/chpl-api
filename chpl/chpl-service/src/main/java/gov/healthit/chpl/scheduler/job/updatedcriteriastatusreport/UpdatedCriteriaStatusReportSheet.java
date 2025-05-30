package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;
import java.time.LocalDate;
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
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;
import one.util.streamex.StreamEx;

@Log4j2(topic = "updatedCriteriaStatusReportEmailJobLogger")
@Component
public class UpdatedCriteriaStatusReportSheet {
    private static final Integer DATE_ROW_IDX = 0;
    private static final Integer REQUIRES_UPDATE_ROW_IDX = 1;
    private static final Integer LISTING_COUNT_ROW_IDX = 3;

    private static final Integer DESCRIPTIONS_COL_IDX = 0;

    private UpdatedCriterionStatusReportDao updatedCriteriaStatusReportDao;
    private SummaryStatisticsDAO summaryStatisticsDao;
    private List<CertificationStatus> allCertificationStatuses;

    public UpdatedCriteriaStatusReportSheet(UpdatedCriterionStatusReportDao updatedCriteriaStatusReportDao,
            SummaryStatisticsDAO summaryStatisticsDao,
            CertificationStatusDAO certificationStatusDao) {
        this.updatedCriteriaStatusReportDao = updatedCriteriaStatusReportDao;
        this.summaryStatisticsDao = summaryStatisticsDao;
        this.allCertificationStatuses = certificationStatusDao.findAll();
    }

    public void generateSheetForCriteriaOnDates(CertificationCriterion criterion, List<LocalDate> reportDates, Workbook workbook) {
        LOGGER.info("Generating worksheet for " + Util.formatCriteriaNumber(criterion));
        Sheet sheet = addWorksheetForCriteria(criterion, workbook);

        CellUtil.getCell(CellUtil.getRow(DATE_ROW_IDX, sheet), DESCRIPTIONS_COL_IDX).setCellValue(criterion.getNumber() + " Up-to-Date Progress");
        updateChartTitle(sheet, criterion);

        for (int i = UpdatedCriteriaStatusReportWorkbook.TOTAL_NUMBER_OF_MONTHS; i >= 1; --i) {
            LocalDate actualReportDay = reportDates.get(i - 1);
            List<UpdatedCriterionStatusReport> criterionStatusReports = getCriterionStatusReportsForDateAndCriterion(actualReportDay, criterion);
            StatisticsSnapshot statisticsSnapshot = getSummaryStatisticsSnapshotForDate(actualReportDay);

            long totalActiveListingsWithCriterion = calculateActiveListingsWithCriterionCount(statisticsSnapshot, criterion);
            long totalListingsRequiringUpdates = calculateListingsRequiringUpdatesCount(criterionStatusReports);

            CellUtil.getCell(CellUtil.getRow(DATE_ROW_IDX, sheet), i).setCellValue(actualReportDay);
            CellUtil.getCell(CellUtil.getRow(REQUIRES_UPDATE_ROW_IDX, sheet), i).setCellValue(totalListingsRequiringUpdates);
            CellUtil.getCell(CellUtil.getRow(LISTING_COUNT_ROW_IDX, sheet), i).setCellValue(totalActiveListingsWithCriterion);
        }
    }

    private long calculateActiveListingsWithCriterionCount(StatisticsSnapshot statisticsSnapshot, CertificationCriterion criterion) {
        if (statisticsSnapshot == null
                || CollectionUtils.isEmpty(statisticsSnapshot.getAttestedCriterionStatistics())) {
            LOGGER.warn("No attested criterion statistics were found in the statistics snapshot");
            return 0;
        }
        List<Long> activeStatusIds = allCertificationStatuses.stream()
                .filter(certStatus -> CertificationStatusUtil.getActiveStatusNames().contains(certStatus.getName()))
                .map(certStatus -> certStatus.getId())
                .collect(Collectors.toList());

        long countOfListingsInActiveStatusWithCriteria = statisticsSnapshot.getAttestedCriterionStatistics().stream()
            .filter(stat -> stat.getCertificationCriterionId().equals(criterion.getId())
                    && activeStatusIds.contains(stat.getListingStatusId()))
            .map(stat -> stat.getListingIds().size())
            .collect(Collectors.summingInt(Integer::intValue));
        return countOfListingsInActiveStatusWithCriteria;
    }

    private long calculateListingsRequiringUpdatesCount(List<UpdatedCriterionStatusReport> reports) {
        if (CollectionUtils.isEmpty(reports)) {
            LOGGER.warn("No updated criteria status reports were found");
            return 0;
        }

        return StreamEx.of(reports)
                .distinct(UpdatedCriterionStatusReport::getCertifiedProductId)
                .count();
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
}
