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

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class CuresStatisticsChartSpreadsheet {

    private static final Integer USCDI_CRITERIA_SHEET_IDX = 2;
    private static final Integer CURES_UPDATE_CRITERIA_VERT_SHEET_IDX = 3;
    private static final Integer CURES_UPDATE_CRITERIA_HORZ_SHEET_IDX = 4;
    private static final Integer PERCENT_CURES_COL_IDX = 6;

    private String template;
    private CriteriaDataWorksheet criteriaDataWorksheet;
    private UscdiCriteriaByAcbWorksheet uscdiCriteriaByAcbWorksheet;
    private CuresProgressWorksheet curesProgressWorksheet;
    private CuresProgressByAcbWorksheet curesProgressByAcbWorksheet;

    @Autowired
    public CuresStatisticsChartSpreadsheet(CriteriaDataWorksheet criteriaDataWorksheet, UscdiCriteriaByAcbWorksheet uscdiCriteriaByAcbWorksheet,
            CuresProgressWorksheet curesProgressWorksheet, CuresProgressByAcbWorksheet curesProgressByAcbWorksheet,
            @Value("${curesStatisticsChartSpreadsheetTemplate}") String template) {

        this.criteriaDataWorksheet = criteriaDataWorksheet;
        this.uscdiCriteriaByAcbWorksheet = uscdiCriteriaByAcbWorksheet;
        this.curesProgressWorksheet = curesProgressWorksheet;
        this.curesProgressByAcbWorksheet = curesProgressByAcbWorksheet;

        this.template = template;
    }

    public File generateSpreadsheet(LocalDate reportDataDate) throws IOException {
        File newFile = copyTemplateFileToTemporaryFile();
        Workbook workbook = getWorkbook(newFile);

        criteriaDataWorksheet.populate(workbook);
        uscdiCriteriaByAcbWorksheet.populate(workbook);
        curesProgressWorksheet.populate(workbook);
        curesProgressByAcbWorksheet.populate(workbook);

        CopyAndSortWorksheet.copy(workbook.getSheet("Criteria Data"), workbook.getSheet("Criteria Data Sorted"), PERCENT_CURES_COL_IDX, false);

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet currentSheet = workbook.getSheetAt(i);
            XSSFDrawing drawing = (XSSFDrawing) currentSheet.createDrawingPatriarch();
            for (XSSFChart chart : drawing.getCharts()) {
                // This goes into the XML that makes up the chart to set the data in the title.  This has potential
                // to vary from chart to chart, based on formatting.
                chart.getCTChart().getTitle().getTx().getRich().getPArray(1).getRArray(0).setT(reportDate.format(formatter));
            }
        }
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
}
