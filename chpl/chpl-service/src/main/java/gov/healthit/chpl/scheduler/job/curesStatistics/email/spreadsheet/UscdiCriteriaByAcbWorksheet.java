package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.statistics.CuresCriteriaStatisticsByAcbDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.statistics.CuresCriteriaStatisticsByAcb;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class UscdiCriteriaByAcbWorksheet {
    private CuresCriteriaStatisticsByAcbDAO curesStatisticsByAcbDAO;
    private CertificationCriterionService certificationCriterionService;
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    public UscdiCriteriaByAcbWorksheet(CuresCriteriaStatisticsByAcbDAO curesStatisticsByAcbDAO, CertificationCriterionService certificationCriterionService,
            CertificationBodyDAO certificat5ionBodyDAO) {

        this.curesStatisticsByAcbDAO = curesStatisticsByAcbDAO;
        this.certificationCriterionService = certificationCriterionService;
        this.certificationBodyDAO = certificat5ionBodyDAO;
    }

    public Workbook populate(Workbook workbook) {
        LOGGER.info("Populating sheet Listing Data By ONC-ACB Data");
        Sheet sheet = workbook.getSheet("Listing Data By ONC-ACB");

        List<CuresCriteriaStatisticsByAcb> stats = getMostRecentCuresStatistics();

        Integer currentRowIdx = 1;
        Row currentRow;
        String currentCriteria = "";

        for (CertificationCriterion criterion : getUscdiCriteria()) {
            for (CertificationBody acb : getActiveAcbs()) {
                CuresCriteriaStatisticsByAcb stat = getCuresStatisticByAcbAndCriteria(stats, acb, criterion);
                currentRow = sheet.createRow(currentRowIdx);
                Cell cell = currentRow.createCell(0);
                if (!currentCriteria.equals(stat.getCuresCriterion().getNumber())) {
                    cell.setCellValue(stat.getCuresCriterion().getNumber());
                } else {
                    cell.setCellValue("");
                }

                cell = currentRow.createCell(cell.getColumnIndex() + 1);
                cell.setCellValue(stat.getCertificationBody().getName());

                cell = currentRow.createCell(cell.getColumnIndex() + 1);
                cell.setCellFormula("E" + (currentRowIdx + 1) + "/F" + (currentRowIdx + 1));
                CellStyle style = workbook.createCellStyle();
                style.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
                cell.setCellStyle(style);

                cell = currentRow.createCell(cell.getColumnIndex() + 1);
                cell.setCellValue(stat.getCriteriaNeedingUpgradeCount());

                cell = currentRow.createCell(cell.getColumnIndex() + 1);
                cell.setCellValue(stat.getCuresCriterionCreatedCount() + stat.getOriginalCriterionUpgradedCount());

                cell = currentRow.createCell(cell.getColumnIndex() + 1);
                cell.setCellFormula("D" + (currentRowIdx + 1) + "+E" + (currentRowIdx + 1));

                currentCriteria = stat.getCuresCriterion().getNumber();
                ++currentRowIdx;
            }
        }
        return workbook;
    }


    private List<CuresCriteriaStatisticsByAcb> getMostRecentCuresStatistics() {
        return curesStatisticsByAcbDAO.getStatisticsForDate(curesStatisticsByAcbDAO.getDateOfMostRecentStatistics());
    }

    private CuresCriteriaStatisticsByAcb getCuresStatisticByAcbAndCriteria(List<CuresCriteriaStatisticsByAcb> stats, CertificationBody acb, CertificationCriterion criterion) {
        return stats.stream()
                .filter(stat -> stat.getCertificationBody().getId().equals(acb.getId())
                        && stat.getCuresCriterion().getId().equals(criterion.getId()))
                .findFirst()
                .orElse(CuresCriteriaStatisticsByAcb.builder()
                        .certificationBody(acb)
                        .curesCriterion(criterion)
                        .curesCriterionCreatedCount(0L)
                        .originalCriterionUpgradedCount(0L)
                        .criteriaNeedingUpgradeCount(0L)
                        .build());
    }

    private List<CertificationBody> getActiveAcbs() {
        return certificationBodyDAO.findAllActive().stream()
                .map(dto -> new CertificationBody(dto))
                .sorted((dto1, dto2) -> dto1.getName().compareTo(dto2.getName()))
                .collect(Collectors.toList());
    }

    private List<CertificationCriterion> getUscdiCriteria() {
        return certificationCriterionService.getUscdiCriteria().stream()
                .sorted((c1, c2) -> certificationCriterionService.sortCriteria(new CertificationCriterionDTO(c1), new CertificationCriterionDTO(c2)))
                .collect(Collectors.toList());
    }
}
