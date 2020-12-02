package gov.healthit.chpl.scheduler.job.auditdata;

import java.io.File;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuditDataFile {
    private String auditDataFilePath;

    @Autowired
    public AuditDataFile(@Value("${auditDataFilePath}") String auditDataFilePath) {
        this.auditDataFilePath = auditDataFilePath;
    }

    public String getProposedFilename(Integer month, Integer year) {
        return auditDataFilePath + "api-key-activity-" + year.toString() + "-" + month.toString() + ".csv";
    }

    public String getRandomFilename() {
        return auditDataFilePath + UUID.randomUUID().toString() + ".csv";
    }

    public boolean doesFileAlreadyExist(String fileName) {
        File check = new File(fileName);
        return check.exists();
    }
}
