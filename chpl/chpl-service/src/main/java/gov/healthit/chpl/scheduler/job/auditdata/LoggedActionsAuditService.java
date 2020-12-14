package gov.healthit.chpl.scheduler.job.auditdata;

import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.postgresql.copy.CopyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class LoggedActionsAuditService  extends AuditService {
    private static final String AUDIT_NAME = "audit.logged_actions";

    @Autowired
    public LoggedActionsAuditService(@Value("${auditDataFilePath}") String auditDataFilePath) {
        super(auditDataFilePath);
    }

    @Override
    public Long getAuditDataCount(Integer month, Integer year) {
        Query query = entityManager.createQuery(
                "SELECT count(*) "
                + "FROM LoggedActionEntity a "
                + "WHERE MONTH(a.actionTStamp) = :month "
                + "AND YEAR(a.actionTStamp) = :year");
        query.setParameter("month", month);
        query.setParameter("year", year);

        return (Long) query.getSingleResult();
    }

    @Override
    @Transactional
    public void deleteAuditData(Integer month, Integer year) {
        Query query = entityManager.createQuery(
                "DELETE FROM LoggedActionEntity a "
                + "WHERE MONTH(a.actionTStamp) = :month "
                + "AND YEAR(a.actionTStamp) = :year");
        query.setParameter("month", month);
        query.setParameter("year", year);

        query.executeUpdate();
    }

    @Override
    public void archiveDataToFile(Integer month, Integer year, String fileName, boolean includeHeaders) throws SQLException {
        String copyCmd = "COPY "
                + "(SELECT * "
                + "FROM audit.logged_actions "
                + "WHERE EXTRACT(MONTH FROM action_tstamp) = " + month.toString() + " "
                + "AND EXTRACT(YEAR FROM action_tstamp) = " + year.toString() + ") "
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
        return getAuditDataFilePath() + "logged-actions-" + year.toString() + "-" + month.toString() + ".csv";
    }

    @Override
    public String getAuditTableNme() {
        return AUDIT_NAME;
    }
}
