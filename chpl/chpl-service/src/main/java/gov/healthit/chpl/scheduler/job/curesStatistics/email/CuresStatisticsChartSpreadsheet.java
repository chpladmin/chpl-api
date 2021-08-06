package gov.healthit.chpl.scheduler.job.curesStatistics.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.statistics.CuresCriterionChartStatistic;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CuresStatisticsChartSpreadsheet {
    private static final Integer EXISTING_CERTIFICATION_COL_IDX = 1;
    private static final Integer NEW_CERTIFICATION_COL_IDX = 2;
    private static final Integer REQUIRES_UPDATE_COL_IDX = 3;
    private static final Integer LISTING_COUNT_COL_IDX = 4;
    private static final Integer PERCENT_CURES_COL_IDX = 6;

    @Value("${curesStatisticsChartSpreadsheetTemplate}")
    private String template;

    @Value("${downloadFolderPath}")
    private String downloadPath;

    private CertificationCriterionService criterionService;

    private List<CriteraToRowMap> criteriaToRowMaps = new ArrayList<CuresStatisticsChartSpreadsheet.CriteraToRowMap>();

    @Autowired
    public CuresStatisticsChartSpreadsheet(CertificationCriterionService criterionService) {
        this.criterionService = criterionService;

        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_1_CURES, 1));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_2_CURES, 2));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_3_CURES, 3));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_7_CURES, 4));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_8_CURES, 5));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_9_CURES, 6));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_10, 7));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.C_3_CURES, 8));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_2_CURES, 9));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_3_CURES, 10));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_10_CURES, 11));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_12, 12));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_13, 13));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.E_1_CURES, 14));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.F_5_CURES, 15));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_6_CURES, 16));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_9_CURES, 17));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_10, 18));
    }

    public File generateSpreadsheet(Map<CertificationCriterionDTO, CuresCriterionChartStatistic> dataMap) throws IOException {
        File newFile = copyFileToTemporaryFile(getTemplate());
        Workbook workbook = getWorkbook(newFile);

        populateDataSheet(dataMap, workbook);

        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

        return writeFileToDisk(workbook, newFile);
    }

    private File writeFileToDisk(Workbook workbook, File saveFile) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(saveFile)) {
            workbook.write(outputStream);
            workbook.close();
        }
        return saveFile;
    }

    private void populateDataSheet(Map<CertificationCriterionDTO, CuresCriterionChartStatistic> dataMap, Workbook workbook) {
        Sheet sheet = getDataSheet(workbook);

        criteriaToRowMaps.stream()
                .forEach(map ->
                        writeDataForCuresCriterionChartStatistic(
                                getCuresCriterionChartStatisticByCriterion(
                                        dataMap, criterionService.get(map.getCriteriaKey())),
                                        sheet.getRow(map.getRowNumber())));

        cloneSheetAndReorder(sheet);
    }

    private void cloneSheetAndReorder(Sheet origSheet) {
        Workbook workbook = origSheet.getWorkbook();

        Sheet clonedSheet = workbook.cloneSheet(workbook.getSheetIndex(origSheet));

        List<Row> clonedRows = getRows(clonedSheet, false);
        Collections.sort(clonedRows, (a, b) -> -1 * Double.compare(a.getCell(PERCENT_CURES_COL_IDX).getNumericCellValue(), b.getCell(PERCENT_CURES_COL_IDX).getNumericCellValue()));

        writeRowsToSheet(clonedRows, getDataSortedSheet(workbook));

        workbook.removeSheetAt(workbook.getSheetIndex(clonedSheet));
    }

    private void writeRowsToSheet(List<Row> rows, Sheet sheet) {
        Integer currRowIdx = 1;
        for (Row currRow : rows) {
            copyRow(currRow, sheet.createRow(currRowIdx));
            currRowIdx++;
        }
    }

    private List<Row> getRows(Sheet sheet, Boolean includeHeaderRow) {
        List<Row> clonedRows = new ArrayList<Row>();
        Iterator<Row> rowIterator = sheet.iterator();

        if (!includeHeaderRow && rowIterator.hasNext()) {
            rowIterator.next();
        }
        while (rowIterator.hasNext()) {
            Row clonedRow = rowIterator.next();
            clonedRows.add(clonedRow);
        }
        return clonedRows;
    }

    private Row copyRow(Row originalRow, Row newRow) {
        short minColIx = originalRow.getFirstCellNum();
        short maxColIx = originalRow.getLastCellNum();
        for (short colIx = minColIx; colIx < maxColIx; colIx++) {
          Cell originalCell = originalRow.getCell(colIx);
          Cell newCell = newRow.createCell(colIx);

          CellStyle newCellStyle = originalRow.getSheet().getWorkbook().createCellStyle();
          newCellStyle.cloneStyleFrom(originalCell.getCellStyle());
          newCell.setCellStyle(newCellStyle);

          switch (originalCell.getCellType()) {
              case STRING:
                  newCell.setCellValue(originalCell.getStringCellValue());
                  break;
              case BOOLEAN:
                  newCell.setCellValue(originalCell.getBooleanCellValue());
                  break;
              case ERROR:
                  newCell.setCellErrorValue(originalCell.getErrorCellValue());
                  break;
              case FORMULA:
                  newCell.setCellValue(originalCell.getNumericCellValue());
                  break;
              case NUMERIC:
                  newCell.setCellValue(originalCell.getNumericCellValue());
                  break;
              case _NONE:
                  break;
              default:
                  break;
          }
        }
        return newRow;
    }

    private void writeDataForCuresCriterionChartStatistic(CuresCriterionChartStatistic data, Row row) {
        row.getCell(EXISTING_CERTIFICATION_COL_IDX).setCellValue(data.getExistingCertificationCount());
        row.getCell(NEW_CERTIFICATION_COL_IDX).setCellValue(data.getNewCertificationCount());
        row.getCell(REQUIRES_UPDATE_COL_IDX).setCellValue(data.getRequiresUpdateCount());
        row.getCell(LISTING_COUNT_COL_IDX).setCellValue(data.getListingCount());
    }


    private CuresCriterionChartStatistic getCuresCriterionChartStatisticByCriterion(Map<CertificationCriterionDTO, CuresCriterionChartStatistic> dataMap, CertificationCriterion criterion) {
        return dataMap.entrySet().stream()
                .filter(entry -> entry.getKey().getId().equals(criterion.getId()))
                .findFirst()
                .get()
                .getValue();
    }

    private Sheet getDataSheet(Workbook workbook) {
        return workbook.getSheet("Data");
    }

    private Sheet getDataSortedSheet(Workbook workbook) {
        return workbook.getSheet("Data Sorted");
    }

    private Workbook getWorkbook(File newFile) throws IOException {
        FileInputStream fis = new FileInputStream(newFile);
        return new XSSFWorkbook(fis);
    }

    private File copyFileToTemporaryFile(File src) throws IOException {
        File tempFile = File.createTempFile("CuresStatisticsCharts_", ".xlsx");
        Path copied = Paths.get(tempFile.getPath());
        Path originalPath = Paths.get(src.getPath());
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    private File getTemplate() {
        return new File(downloadPath + "/" + template);
    }

    class CriteraToRowMap {
        private String criteriaKey;
        private Integer rowNumber;

        CriteraToRowMap(String criteriaKey, Integer rowNumber) {
            this.criteriaKey = criteriaKey;
            this.rowNumber = rowNumber;
        }

        public String getCriteriaKey() {
            return criteriaKey;
        }

        public void setCriteriaKey(String criteriaKey) {
            this.criteriaKey = criteriaKey;
        }

        public Integer getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(Integer rowNumber) {
            this.rowNumber = rowNumber;
        }
    }
}
