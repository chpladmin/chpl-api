package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.statistics.CuresListingStatisticsDAO;
import gov.healthit.chpl.domain.statistics.CuresListingStatistic;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CuresUpdateProgressWorksheet {
    private CuresListingStatisticsDAO curesListingStatisticsDAO;

    @Autowired
    public CuresUpdateProgressWorksheet(CuresListingStatisticsDAO curesListingStatisticsDAO) {
        this.curesListingStatisticsDAO = curesListingStatisticsDAO;
    }

    public Workbook populate(Workbook workbook) {
        Sheet sheet = workbook.getSheet("Cures Update Progress Data");

        List<CuresListingStatistic> stats = getMostRecentCuresStatistics();
        if (stats != null && stats.size() > 0) {
            CuresListingStatistic stat = stats.get(0);

            Row row = sheet.createRow(1);

            Cell currentCell = row.createCell(0);
            currentCell.setCellValue(stat.getCuresListingWithCuresCriteriaCount() + stat.getCuresListingWithoutCuresCriteriaCount() + stat.getNonCuresListingCount());

            currentCell = row.createCell(currentCell.getColumnIndex() + 1);
            currentCell.setCellValue(stat.getCuresListingWithCuresCriteriaCount() + stat.getCuresListingWithoutCuresCriteriaCount());

            currentCell = row.createCell(currentCell.getColumnIndex() + 1);
            currentCell.setCellValue(stat.getCuresListingWithCuresCriteriaCount());

            currentCell = row.createCell(currentCell.getColumnIndex() + 1);
            currentCell.setCellValue(stat.getCuresListingWithoutCuresCriteriaCount());

            currentCell = row.createCell(currentCell.getColumnIndex() + 1);
            currentCell.setCellValue(stat.getNonCuresListingCount());

            //row = sheet.createRow(row.getRowNum() + 1);

        }

        return workbook;
    }

    private List<CuresListingStatistic> getMostRecentCuresStatistics() {
        return curesListingStatisticsDAO.getStatisticsForDate(curesListingStatisticsDAO.getDateOfMostRecentStatistics());
    }
}
