package gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class CuresSpreadsheet {
    protected File copyTemplateFileToTemporaryFile(String template, String destinationBaseFileName) throws IOException {
        try (InputStream srcInputStream = getTemplateAsStream(template)) {
            File tempFile = File.createTempFile(destinationBaseFileName + "_", ".xlsx");
            Files.copy(srcInputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }

    private InputStream getTemplateAsStream(String template) {
        return getClass().getClassLoader().getResourceAsStream(template);
    }

    protected Workbook getWorkbook(File newFile) throws IOException {
        FileInputStream fis = new FileInputStream(newFile);
        return new XSSFWorkbook(fis);
    }

    protected File writeFileToDisk(Workbook workbook, File saveFile) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(saveFile)) {
            workbook.write(outputStream);
            workbook.close();
        }
        return saveFile;
    }

}
