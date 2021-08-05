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
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
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

@Component
public class CuresStatisticsChartSpreadsheet {
    private static final Integer EXISTING_CERTIFICATION_COL_IDX = 1;
    private static final Integer NEW_CERTIFICATION_COL_IDX = 2;
    private static final Integer REQUIRES_UPDATE_COL_IDX = 3;
    private static final Integer LISTING_COUNT_COL_IDX = 4;

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
        //criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_12, 12));
        //criteriaToRowMaps.add(new CriteraToRowMap(CertificationCriterionService.Criteria2015.D_13, 13));
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

    private void updateTemplate() throws IOException {
        File template = new File("C:\\CHPL\\files\\Cures_Update.xlsx");
        FileInputStream fis = new FileInputStream(template);
        XSSFWorkbook wb = new XSSFWorkbook(fis);

        Sheet sheet = wb.getSheet("Data");

        Integer currRow = 1;
        Cell currCell = null;

        Row row = sheet.getRow(currRow);
        currCell = row.getCell(1);
        currCell.setCellValue(2);
        currCell = row.getCell(2);
        currCell.setCellValue(100);
        currCell = row.getCell(3);
        currCell.setCellValue(544);
        currCell = row.getCell(4);
        currCell.setCellValue(10);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(2);
        currCell = row.getCell(2);
        currCell.setCellValue(4);
        currCell = row.getCell(3);
        currCell.setCellValue(527);
        currCell = row.getCell(4);
        currCell.setCellValue(6);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(0);
        currCell = row.getCell(2);
        currCell.setCellValue(12);
        currCell = row.getCell(3);
        currCell.setCellValue(433);
        currCell = row.getCell(4);
        currCell.setCellValue(12);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(0);
        currCell = row.getCell(2);
        currCell.setCellValue(2);
        currCell = row.getCell(3);
        currCell.setCellValue(66);
        currCell = row.getCell(4);
        currCell.setCellValue(2);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(0);
        currCell = row.getCell(2);
        currCell.setCellValue(50);
        currCell = row.getCell(3);
        currCell.setCellValue(66);
        currCell = row.getCell(4);
        currCell.setCellValue(2);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(1);
        currCell = row.getCell(2);
        currCell.setCellValue(2);
        currCell = row.getCell(3);
        currCell.setCellValue(523);
        currCell = row.getCell(4);
        currCell.setCellValue(3);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(6);
        currCell = row.getCell(2);
        currCell.setCellValue(11);
        currCell = row.getCell(3);
        currCell.setCellValue(416);
        currCell = row.getCell(4);
        currCell.setCellValue(17);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(15);
        currCell = row.getCell(2);
        currCell.setCellValue(5);
        currCell = row.getCell(3);
        currCell.setCellValue(843);
        currCell = row.getCell(4);
        currCell.setCellValue(70);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(0);
        currCell = row.getCell(2);
        currCell.setCellValue(12);
        currCell = row.getCell(3);
        currCell.setCellValue(852);
        currCell = row.getCell(4);
        currCell.setCellValue(12);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(1);
        currCell = row.getCell(2);
        currCell.setCellValue(6);
        currCell = row.getCell(3);
        currCell.setCellValue(106);
        currCell = row.getCell(4);
        currCell.setCellValue(7);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue("-");
        currCell = row.getCell(2);
        currCell.setCellValue(269);
        currCell = row.getCell(3);
        currCell.setCellValue(645);
        currCell = row.getCell(4);
        currCell.setCellValue(269);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue("-");
        currCell = row.getCell(2);
        currCell.setCellValue(269);
        currCell = row.getCell(3);
        currCell.setCellValue(645);
        currCell = row.getCell(4);
        currCell.setCellValue(269);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(2);
        currCell = row.getCell(2);
        currCell.setCellValue(6);
        currCell = row.getCell(3);
        currCell.setCellValue(498);
        currCell = row.getCell(4);
        currCell.setCellValue(8);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(0);
        currCell = row.getCell(2);
        currCell.setCellValue(1);
        currCell = row.getCell(3);
        currCell.setCellValue(81);
        currCell = row.getCell(4);
        currCell.setCellValue(3);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(2);
        currCell = row.getCell(2);
        currCell.setCellValue(10);
        currCell = row.getCell(3);
        currCell.setCellValue(636);
        currCell = row.getCell(4);
        currCell.setCellValue(10);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(1);
        currCell = row.getCell(2);
        currCell.setCellValue(9);
        currCell = row.getCell(3);
        currCell.setCellValue(525);
        currCell = row.getCell(4);
        currCell.setCellValue(10);

        row = sheet.getRow(currRow++);
        currCell = row.getCell(1);
        currCell.setCellValue(0);
        currCell = row.getCell(2);
        currCell.setCellValue(2);
        currCell = row.getCell(3);
        currCell.setCellValue(533);
        currCell = row.getCell(4);
        currCell.setCellValue(2);


    }

    class CriteraToRowMap {
        private String criteriaKey;
        private Integer rowNumber;

        public CriteraToRowMap(String criteriaKey, Integer rowNumber) {
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
