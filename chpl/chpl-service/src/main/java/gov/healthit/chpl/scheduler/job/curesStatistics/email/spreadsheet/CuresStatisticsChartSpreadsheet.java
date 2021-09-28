package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
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

    private static final Integer B_1_CURES_ROW_IDX = 1;
    private static final Integer B_2_CURES_ROW_IDX = 2;
    private static final Integer B_3_CURES_ROW_IDX = 3;
    private static final Integer B_7_CURES_ROW_IDX = 4;
    private static final Integer B_8_CURES_ROW_IDX = 5;
    private static final Integer B_9_CURES_ROW_IDX = 6;
    private static final Integer B_10_ROW_IDX = 7;
    private static final Integer C_3_CURES_ROW_IDX = 8;
    private static final Integer D_2_CURES_ROW_IDX = 9;
    private static final Integer D_3_CURES_ROW_IDX = 10;
    private static final Integer D_10_CURES_ROW_IDX = 11;
    private static final Integer D_12_ROW_IDX = 12;
    private static final Integer D_13_ROW_IDX = 13;
    private static final Integer E_1_CURES_ROW_IDX = 14;
    private static final Integer F_5_CURES_ROW_IDX = 15;
    private static final Integer G_6_CURES_ROW_IDX = 16;
    private static final Integer G_9_CURES_ROW_IDX = 17;
    private static final Integer G_10_ROW_IDX = 18;

    private static final Integer USCDI_CRITERIA_SHEET_IDX = 2;
    private static final Integer CURES_UPDATE_CRITERIA_VERT_SHEET_IDX = 3;
    private static final Integer CURES_UPDATE_CRITERIA_HORZ_SHEET_IDX = 4;

    @Value("${curesStatisticsChartSpreadsheetTemplate}")
    private String template;

    @Value("${downloadFolderPath}")
    private String downloadPath;

    private CertificationCriterionService criterionService;
    private UscdiCriteriaByAcbWorksheet uscdiCriteriaByAcbWorksheet;
    private CuresProgressWorksheet curesProgressWorksheet;
    private CuresProgressByAcbWorksheet curesProgressByAcbWorksheet;

    private List<CriteraToRowMap> criteriaToRowMaps = new ArrayList<CuresStatisticsChartSpreadsheet.CriteraToRowMap>();

    @Autowired
    public CuresStatisticsChartSpreadsheet(CertificationCriterionService criterionService, UscdiCriteriaByAcbWorksheet uscdiCriteriaByAcbWorksheet,
            CuresProgressWorksheet curesProgressWorksheet, CuresProgressByAcbWorksheet curesProgressByAcbWorksheet) {

        this.criterionService = criterionService;
        this.uscdiCriteriaByAcbWorksheet = uscdiCriteriaByAcbWorksheet;
        this.curesProgressWorksheet = curesProgressWorksheet;
        this.curesProgressByAcbWorksheet = curesProgressByAcbWorksheet;

        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_1_CURES, B_1_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_2_CURES, B_2_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_3_CURES, B_3_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_7_CURES, B_7_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_8_CURES, B_8_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_9_CURES, B_9_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.B_10, B_10_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.C_3_CURES, C_3_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_2_CURES, D_2_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_3_CURES, D_3_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_10_CURES, D_10_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_12, D_12_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_13, D_13_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.E_1_CURES, E_1_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.F_5_CURES, F_5_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_6_CURES, G_6_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_9_CURES, G_9_CURES_ROW_IDX));
        criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.G_10, G_10_ROW_IDX));
    }

    public File generateSpreadsheet(Map<CertificationCriterionDTO, CuresCriterionChartStatistic> dataMap, LocalDate reportDataDate) throws IOException {
        File newFile = copyTemplateFileToTemporaryFile();
        Workbook workbook = getWorkbook(newFile);

        populateDataSheet(dataMap, workbook);

        uscdiCriteriaByAcbWorksheet.populate(workbook);
        curesProgressWorksheet.populate(workbook);
        curesProgressByAcbWorksheet.populate(workbook);

        updateChartTitles(workbook, reportDataDate);

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

    private void updateChartTitles(Workbook workbook, LocalDate reportDate) {
        List<Sheet> allChartSheets = new ArrayList<Sheet>();
        allChartSheets.add(workbook.getSheetAt(USCDI_CRITERIA_SHEET_IDX));
        allChartSheets.add(workbook.getSheetAt(CURES_UPDATE_CRITERIA_VERT_SHEET_IDX));
        allChartSheets.add(workbook.getSheetAt(CURES_UPDATE_CRITERIA_HORZ_SHEET_IDX));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (Sheet currentSheet : allChartSheets) {
            XSSFDrawing drawing = (XSSFDrawing) currentSheet.createDrawingPatriarch();
            for (XSSFChart chart : drawing.getCharts()) {
                // This goes into the XML that makes up the chart to set the data in the title.  This has potential
                // to vary from chart to chart, based on formatting.
                chart.getCTChart().getTitle().getTx().getRich().getPArray(1).getRArray(0).setT(reportDate.format(formatter));
            }
        }
    }

    private void populateDataSheet(Map<CertificationCriterionDTO, CuresCriterionChartStatistic> dataMap, Workbook workbook) {
        Sheet sheet = getDataSheet(workbook);

        criteriaToRowMaps.stream()
                .forEach(map ->
                        writeDataForCuresCriterionChartStatistic(
                                getCuresCriterionChartStatisticByCriterion(
                                        dataMap, criterionService.get(map.getCriteriaKey())), sheet.getRow(map.getRowNumber())));

        Sheet sortedDataSheet = getDataSortedSheet(workbook);
        CopyAndSortWorksheet.copy(sheet, sortedDataSheet, PERCENT_CURES_COL_IDX, false);
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
        return workbook.getSheet("Criteria Data");
    }

    private Sheet getDataSortedSheet(Workbook workbook) {
        return workbook.getSheet("Criteria Data Sorted");
    }

    private Workbook getWorkbook(File newFile) throws IOException {
        FileInputStream fis = new FileInputStream(newFile);
        return new XSSFWorkbook(fis);
    }

    private File copyTemplateFileToTemporaryFile() throws IOException {
        try (InputStream srcInputStream = getTemplateAsStream()) {
            File tempFile = File.createTempFile("CuresStatisticsCharts_", ".xlsx");
            Files.copy(srcInputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }

    private InputStream getTemplateAsStream() {
        return getClass().getClassLoader().getResourceAsStream(template);
    }

    static class CopyAndSortWorksheet {
        public static Sheet copy(Sheet origSheet, Sheet newSheet, Integer sortColumnIndex, Boolean includeHeaders) {
            Workbook workbook = origSheet.getWorkbook();
            Sheet clonedSheet = workbook.cloneSheet(workbook.getSheetIndex(origSheet));
            List<Row> clonedRows = getRows(clonedSheet, includeHeaders);

            if (sortColumnIndex != null) {
                Collections.sort(clonedRows, (a, b) -> -1 * Double.compare(a.getCell(sortColumnIndex).getNumericCellValue(), b.getCell(sortColumnIndex).getNumericCellValue()));
            }

            writeRowsToSheet(clonedRows, newSheet);
            workbook.removeSheetAt(workbook.getSheetIndex(clonedSheet));
            return newSheet;
        }

        private static List<Row> getRows(Sheet sheet, Boolean includeHeaderRow) {
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

        private static void writeRowsToSheet(List<Row> rows, Sheet sheet) {
            Integer currRowIdx = 1;
            for (Row currRow : rows) {
                copyRow(currRow, sheet.createRow(currRowIdx));
                currRowIdx++;
            }
        }

        private static Row copyRow(Row originalRow, Row newRow) {
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
                        newCell.setCellFormula(originalCell.getCellFormula());
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
