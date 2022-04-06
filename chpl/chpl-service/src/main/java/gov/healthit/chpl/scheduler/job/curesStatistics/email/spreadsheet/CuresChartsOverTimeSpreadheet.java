package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class CuresChartsOverTimeSpreadheet {

    private String template;

    @Autowired
    public CuresChartsOverTimeSpreadheet(@Value("${curesChartsOverTimeSpreadsheetTemplate}") String template) {

        this.template = template;
    }

    public File generateSpreadsheet(LocalDate reportDataDate) throws IOException {
        File newFile = copyTemplateFileToTemporaryFile();
        Workbook workbook = getWorkbook(newFile);

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

}

