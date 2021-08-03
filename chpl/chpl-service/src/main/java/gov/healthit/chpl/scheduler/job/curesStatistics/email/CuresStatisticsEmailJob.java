package gov.healthit.chpl.scheduler.job.curesStatistics.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.util.EmailBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class CuresStatisticsEmailJob  extends QuartzJob {
    @Autowired
    private CriterionListingStatisticsCsvCreator criterionListingStatisticsCsvCreator;

    @Autowired
    private OriginalCriterionUpgradedStatisticsCsvCreator originalCriterionUpgradedStatisticsCsvCreator;

    @Autowired
    private CuresCriterionUpgradedWithoutOriginalStatisticsCsvCreator curesCriterionUpgradedWithoutOriginalStatisticsCsvCreator;

    @Autowired
    private ListingCriterionForCuresAchievementStatisticsCsvCreator listingCriterionForCuresAchievementStatisticsCsvCreator;

    @Autowired
    private ListingCuresStatusStatisticsHtmlCreator listingCuresStatusStatisticsHtmlCreator;

    @Autowired
    private PrivacyAndSecurityListingStatisticsHtmlCreator privacyAndSecurityListingStatisticsHtmlCreator;

    @Autowired
    private Environment env;

    @Autowired
    private CuresStatisticsSpreadsheet curesStatisticsSpreadhseet;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("*****Cures Reporting Email Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        List<File> csvAttachments = new ArrayList<File>();
        try {
            File statisticsCsv = criterionListingStatisticsCsvCreator.createCsvFile();
            if (statisticsCsv != null) {
                csvAttachments.add(statisticsCsv);
            }
        } catch (IOException ex) {
            LOGGER.error("Error creating statistics", ex);
        }
        try {
            File statisticsCsv = originalCriterionUpgradedStatisticsCsvCreator.createCsvFile();
            if (statisticsCsv != null) {
                csvAttachments.add(statisticsCsv);
            }
        } catch (IOException ex) {
            LOGGER.error("Error creating statistics", ex);
        }
        try {
            File statisticsCsv = curesCriterionUpgradedWithoutOriginalStatisticsCsvCreator.createCsvFile();
            if (statisticsCsv != null) {
                csvAttachments.add(statisticsCsv);
            }
        } catch (IOException ex) {
            LOGGER.error("Error creating statistics", ex);
        }
        try {
            File statisticsCsv = listingCriterionForCuresAchievementStatisticsCsvCreator.createCsvFile();
            if (statisticsCsv != null) {
                csvAttachments.add(statisticsCsv);
            }
        } catch (IOException ex) {
            LOGGER.error("Error creating statistics", ex);
        }

        try {
            sendEmail(context, csvAttachments);
        } catch (MessagingException ex) {
            LOGGER.error("Error sending email!", ex);
        }

        try {
            updateTemplate();
        } catch (Exception e) {
            LOGGER.catching(e);
        }

        try {
            curesStatisticsSpreadhseet.getCuresCriterionChartStatistics();
        } catch (Exception e) {
            LOGGER.catching(e);
        }

        LOGGER.info("*****Cures Reporting Email Job is complete.*****");
    }

    private String createEmailBody() {
        String emailBody = "<h2>Cures Upgrade Statistics</h4><br/>";
        emailBody += listingCuresStatusStatisticsHtmlCreator.createEmailBody();
        emailBody += privacyAndSecurityListingStatisticsHtmlCreator.createEmailBody();
        return emailBody;
    }

    private void sendEmail(JobExecutionContext context, List<File> attachments) throws MessagingException {
        String emailAddress = context.getMergedJobDataMap().getString(JOB_DATA_KEY_EMAIL);
        LOGGER.info("Sending email to: " + emailAddress);
        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(emailAddress)
                .subject(env.getProperty("curesStatisticsReport.subject"))
                .htmlMessage(createEmailBody())
                .fileAttachments(attachments)
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + emailAddress);
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

        XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);

        fis.close();

        Date timestamp = new Date();
        File outputFile = new File("C:\\CHPL\\files\\Cures_Update_" + timestamp.getTime() + ".xlsx");
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        wb.write(outputStream);
        wb.close();
        outputStream.close();

    }


}
