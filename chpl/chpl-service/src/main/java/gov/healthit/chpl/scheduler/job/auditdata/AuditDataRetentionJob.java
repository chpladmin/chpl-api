package gov.healthit.chpl.scheduler.job.auditdata;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class AuditDataRetentionJob implements Job {
    private static final Integer MEGABYTE = 1024;
    @Autowired
    private ApiKeyActivityAuditDAO apiKeyActivityAuditDAO;

    @Autowired
    private AuditDataFile auditDataFile;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private Environment env;

    private Integer retentionPolicyInMonths;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("STARTING AuditDataRetentionJob");

        retentionPolicyInMonths = new Integer(env.getProperty("auditDataRetentionPolicyInMonths"));

        // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from within this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    LocalDate targetDate = getStartDate();
                    while (isDateBeforeRetentionPolicy(targetDate)) {
                        LOGGER.info("Processing " + targetDate.toString());
                        if (doesAuditDataExist(targetDate.getMonthValue(), targetDate.getYear())) {
                            archiveData(targetDate.getMonthValue(), targetDate.getYear());
                        }
                        targetDate = targetDate.plusMonths(1);
                    }
                } catch (Exception e) {
                    LOGGER.catching(e);
                    status.setRollbackOnly();
                }
            }
        });

        LOGGER.info("COMPLETED AuditDataRetentionJob");
    }

    private boolean isDateBeforeRetentionPolicy(LocalDate targetDate) {
        LocalDate now = LocalDate.now();
        now = now.minusMonths(retentionPolicyInMonths + 1);
        return now.isAfter(targetDate);
    }

    private void archiveData(Integer month, Integer year) throws SQLException, IOException {
        String fileName = auditDataFile.getProposedFilename(month, year);
        boolean doesArchiveExist = auditDataFile.doesFileAlreadyExist(fileName);
        if (doesArchiveExist) {
            fileName = auditDataFile.getRandomFilename();
            apiKeyActivityAuditDAO.archiveDataToFile(month, year, fileName, false);
            //Append the temporary file to the existing file
            appendFiles(auditDataFile.getProposedFilename(month, year), fileName);
            deleteFile(fileName);
        } else {
            apiKeyActivityAuditDAO.archiveDataToFile(month, year, fileName, true);
        }
        apiKeyActivityAuditDAO.deleteAuditData(month, year);
    }

    private boolean doesAuditDataExist(Integer month, Integer year) {
        Long count = apiKeyActivityAuditDAO.getAuditDataCount(month, year);
        LOGGER.info("Found " + count + " records");
        return  count > 0;
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    private LocalDate getStartDate() {
        return LocalDate.of(2016, Month.APRIL, 1);
    }

    private void appendFiles(String file1, String file2) {
      try (FileOutputStream file1Stream = new FileOutputStream(file1, true);
              FileInputStream file2Stream = new FileInputStream(file2)) {

          byte[] buffer = new byte[MEGABYTE];
          int length;
          //Copy data to another file
          while ((length = file2Stream.read(buffer)) > 0) {
              file1Stream.write(buffer, 0, length);
          }
      } catch (IOException e) {
          LOGGER.catching(e);
      }
    }

    private void deleteFile(String fileName) throws IOException {
        Path fileToDelete = Paths.get(fileName);
        Files.deleteIfExists(fileToDelete);
    }
}
