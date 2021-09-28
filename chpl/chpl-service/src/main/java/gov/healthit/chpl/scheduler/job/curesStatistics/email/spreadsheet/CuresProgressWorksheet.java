package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.statistics.CuresListingStatisticsByAcbDAO;
import gov.healthit.chpl.domain.statistics.CuresListingStatisticByAcb;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class CuresProgressWorksheet {
    private CuresListingStatisticsByAcbDAO curesListingStatisticsByAcbDAO;

    @Autowired
    public CuresProgressWorksheet(CuresListingStatisticsByAcbDAO curesListingStatisticsByAcbDAO) {
        this.curesListingStatisticsByAcbDAO = curesListingStatisticsByAcbDAO;
    }

    public Workbook populate(Workbook workbook) {
        Sheet sheet = workbook.getSheet("Cures Progress Data");

        List<CuresListingStatisticByAcb> stats = getMostRecentCuresStatistics();

        Row row = sheet.createRow(1);

        Cell currentCell = row.createCell(0);
        currentCell.setCellValue(getCountSummed(stats, CuresListingStatisticByAcb::getCuresListingWithoutCuresCriteriaCount)
                + getCountSummed(stats, CuresListingStatisticByAcb::getCuresListingWithCuresCriteriaCount)
                + getCountSummed(stats, CuresListingStatisticByAcb::getNonCuresListingCount));

        currentCell = row.createCell(currentCell.getColumnIndex() + 1);
        currentCell.setCellValue(getCountSummed(stats, CuresListingStatisticByAcb::getCuresListingWithCuresCriteriaCount)
                + getCountSummed(stats, CuresListingStatisticByAcb::getCuresListingWithoutCuresCriteriaCount));

        currentCell = row.createCell(currentCell.getColumnIndex() + 1);
        currentCell.setCellValue(getCountSummed(stats, CuresListingStatisticByAcb::getCuresListingWithCuresCriteriaCount));

        currentCell = row.createCell(currentCell.getColumnIndex() + 1);
        currentCell.setCellValue(getCountSummed(stats, CuresListingStatisticByAcb::getCuresListingWithoutCuresCriteriaCount));

        currentCell = row.createCell(currentCell.getColumnIndex() + 1);
        currentCell.setCellValue(getCountSummed(stats, CuresListingStatisticByAcb::getNonCuresListingCount));

        return workbook;
    }

    private Long getCountSummed(List<CuresListingStatisticByAcb> stats, ToLongFunction<? super CuresListingStatisticByAcb> mapper) {
        return stats.stream()
                .collect(Collectors.summingLong(mapper));
    }

    private List<CuresListingStatisticByAcb> getMostRecentCuresStatistics() {
        return curesListingStatisticsByAcbDAO.getStatisticsForDate(curesListingStatisticsByAcbDAO.getDateOfMostRecentStatistics());
    }
}
