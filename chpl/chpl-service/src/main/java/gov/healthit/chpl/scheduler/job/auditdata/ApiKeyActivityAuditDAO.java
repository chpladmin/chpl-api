package gov.healthit.chpl.scheduler.job.auditdata;

import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;

import javax.persistence.Query;

import org.postgresql.copy.CopyManager;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ApiKeyActivityAuditDAO extends AuditDAO {

    @Override
    public Long getAuditDataCount(Integer month, Integer year) {
        Query query = entityManager.createQuery(
                "SELECT count(*) "
                + "FROM ApiKeyActivityEntity a "
                //+ "WHERE DAY(a.creationDate) = :day "
                + "WHERE MONTH(a.creationDate) = :month "
                + "AND YEAR(a.creationDate) = : year");
        //query.setParameter("day", targetDate.getDayOfMonth());
        query.setParameter("month", month);
        query.setParameter("year", year);

        return (Long) query.getSingleResult();
    }

    @Override
    public void deleteAuditData(Integer month, Integer year) {
        Query query = entityManager.createQuery(
                "DELETE ApiKeyActivityEntity a "
                //+ "WHERE DAY(a.creationDate) = :day "
                + "WHERE MONTH(a.creationDate) = :month "
                + "AND YEAR(a.creationDate) = : year");
        //query.setParameter("day", targetDate.getDayOfMonth());
        query.setParameter("month", month);
        query.setParameter("year", month);

        query.executeUpdate();
    }

    @Override
    public void archiveDataToFile(Integer month, Integer year, String fileName, boolean includeHeaders) throws SQLException {
        String copyCmd = "COPY "
                + "(SELECT * "
                + "FROM openchpl.api_key_activity "
                //+ "WHERE EXTRACT(DAY FROM creation_date) = " + targetDate.getDayOfMonth() + " "
                + "WHERE EXTRACT(MONTH FROM creation_date) = " + month.toString() + " "
                + "AND EXTRACT(YEAR FROM creation_date) = " + year.toString() + ") "
                + "TO STDOUT DELIMITER ',' CSV "
                + "HEADER";

        LOGGER.info("STARTING");
        CopyManager cm = getPgConnection().getCopyAPI();
        try (FileWriter fw = new FileWriter(new File(fileName))) {
            LOGGER.info("Got this far");
            cm.copyOut(copyCmd, fw);
            LOGGER.info("And even farther");
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("COMPLETED");
    }
}
