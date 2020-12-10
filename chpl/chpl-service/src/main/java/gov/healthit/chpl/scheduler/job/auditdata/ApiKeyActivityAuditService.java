package gov.healthit.chpl.scheduler.job.auditdata;

import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;

import javax.persistence.Query;

import org.postgresql.copy.CopyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ApiKeyActivityAuditService extends AuditService {
    private static final String AUDIT_NAME = "openchpl.api_key_activity";

    @Autowired
    public ApiKeyActivityAuditService(@Value("${auditDataFilePath}") String auditDataFilePath) {
        super(auditDataFilePath);
    }

    @Override
    public Long getAuditDataCount(Integer month, Integer year) {
        Query query = entityManager.createQuery(
                "SELECT count(*) "
                + "FROM ApiKeyActivityEntity a "
                + "WHERE MONTH(a.creationDate) = :month "
                + "AND YEAR(a.creationDate) = :year");
        query.setParameter("month", month);
        query.setParameter("year", year);

        return (Long) query.getSingleResult();
    }

    @Override
    public void deleteAuditData(Integer month, Integer year) {
        Query query = entityManager.createQuery(
                "DELETE FROM ApiKeyActivityEntity a "
                + "WHERE MONTH(a.creationDate) = :month "
                + "AND YEAR(a.creationDate) = :year");
        query.setParameter("month", month);
        query.setParameter("year", month);

        query.executeUpdate();
    }

    @Override
    public void archiveDataToFile(Integer month, Integer year, String fileName, boolean includeHeaders) throws SQLException {
        String copyCmd = "COPY "
                + "(SELECT * "
                + "FROM openchpl.api_key_activity "
                + "WHERE EXTRACT(MONTH FROM creation_date) = " + month.toString() + " "
                + "AND EXTRACT(YEAR FROM creation_date) = " + year.toString() + ") "
                + "TO STDOUT DELIMITER ',' CSV ";
        if (includeHeaders) {
               copyCmd += "HEADER";
        }

        CopyManager cm = getPgConnection().getCopyAPI();
        try (FileWriter fw = new FileWriter(new File(fileName))) {
            cm.copyOut(copyCmd, fw);
        } catch (Exception e) {
            LOGGER.catching(e);
        }
    }

    @Override
    public String getProposedFilename(Integer month, Integer year) {
        return getAuditDataFilePath() + "api-key-activity-" + year.toString() + "-" + month.toString() + ".csv";
    }

    @Override
    public String getAuditTableNme() {
        return AUDIT_NAME;
    }
}
