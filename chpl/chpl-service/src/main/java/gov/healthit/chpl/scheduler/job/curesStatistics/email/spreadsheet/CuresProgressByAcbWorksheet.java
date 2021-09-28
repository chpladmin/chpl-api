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
import gov.healthit.chpl.dao.statistics.CuresListingStatisticsByAcbDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.statistics.CuresListingStatisticByAcb;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class CuresProgressByAcbWorksheet {
    private CuresListingStatisticsByAcbDAO curesListingStatisticsByAcbDAO;
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    public CuresProgressByAcbWorksheet(CuresListingStatisticsByAcbDAO curesListingStatisticsByAcbDAO, CertificationBodyDAO certificationBodyDAO) {
        this.curesListingStatisticsByAcbDAO = curesListingStatisticsByAcbDAO;
        this.certificationBodyDAO = certificationBodyDAO;
    }

    public Workbook populate(Workbook workbook) {
        Sheet sheet = workbook.getSheet("Cures Progress By ACB Data");
        List<CuresListingStatisticByAcb> stats = getMostRecentCuresStatistics();
        List<CertificationBody> acbs = getActiveCertificationBodies();

        Row row;
        Integer rowIdx = 1;

        for (CertificationBody acb : acbs) {
            row = sheet.createRow(rowIdx);
            populateRowWithAcbStatistics(workbook, row, getStatisticsForAcb(stats, acb));
            rowIdx++;
        }

        return workbook;
    }

    private Row populateRowWithAcbStatistics(Workbook workbook, Row row, CuresListingStatisticByAcb stat) {
        Cell currentCell = row.createCell(0);

        currentCell.setCellValue(stat.getCertificationBody().getName());

        currentCell = row.createCell(currentCell.getColumnIndex() + 1);
        currentCell.setCellFormula("E" + (row.getRowNum() + 1) + "/C" + (row.getRowNum() + 1));
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
        currentCell.setCellStyle(style);

        currentCell = row.createCell(currentCell.getColumnIndex() + 1);
        currentCell.setCellValue(stat.getCuresListingWithoutCuresCriteriaCount()
                + stat.getCuresListingWithCuresCriteriaCount()
                + stat.getNonCuresListingCount());

        currentCell = row.createCell(currentCell.getColumnIndex() + 1);
        currentCell.setCellValue(stat.getNonCuresListingCount());

        currentCell = row.createCell(currentCell.getColumnIndex() + 1);
        currentCell.setCellValue(stat.getCuresListingWithCuresCriteriaCount()
                + stat.getCuresListingWithoutCuresCriteriaCount());

        currentCell = row.createCell(currentCell.getColumnIndex() + 1);
        currentCell.setCellValue(stat.getCuresListingWithCuresCriteriaCount());

        currentCell = row.createCell(currentCell.getColumnIndex() + 1);
        currentCell.setCellValue(stat.getCuresListingWithoutCuresCriteriaCount());

        return row;
    }

    private CuresListingStatisticByAcb getStatisticsForAcb(List<CuresListingStatisticByAcb> stats, CertificationBody certificationBody) {
        return stats.stream()
                .filter(stat -> stat.getCertificationBody().getId().equals(certificationBody.getId()))
                .findFirst()
                .orElse(null);
    }

    private List<CuresListingStatisticByAcb> getMostRecentCuresStatistics() {
        return curesListingStatisticsByAcbDAO.getStatisticsForDate(curesListingStatisticsByAcbDAO.getDateOfMostRecentStatistics());
    }

    private List<CertificationBody> getActiveCertificationBodies() {
        return certificationBodyDAO.findAllActive().stream()
                .map(dto -> new CertificationBody(dto))
                .sorted((acb1, acb2) -> acb1.getName().compareTo(acb2.getName()))
                .collect(Collectors.toList());
    }

}
