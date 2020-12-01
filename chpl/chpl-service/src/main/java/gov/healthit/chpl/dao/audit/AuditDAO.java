package gov.healthit.chpl.dao.audit;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.Query;

import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import org.hibernate.engine.spi.SessionImplementor;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AuditDAO extends BaseDAOImpl {

    private String auditDataFilePath;

    @Autowired
    public AuditDAO(@Value("${auditDataFilePath}") String auditDataFilePath) {
        this.auditDataFilePath = auditDataFilePath;
    }

    public Integer getApiKeyActivityCount(Integer month, Integer year) {
        Query query = entityManager.createQuery(
                "SELECT a "
                + "FROM ApiKeyActivityEntity a "
                + "WHERE MONTH(a.creationDate) = :month "
                + "AND YEAR(a.creationDate) = : year");
        query.setParameter("month", month);
        query.setParameter("year", year);

        return query.getResultList().size();
    }

    public void archiveDataToFile(Integer month, Integer year, String fileName, boolean includeHeaders) throws SQLException {
        String copyCmd = "COPY "
                + "(SELECT * "
                + "FROM openchpl.api_key_activity "
                + "WHERE EXTRACT(MONTH FROM creation_date) = " + month.toString() + " "
                + "AND EXTRACT(YEAR FROM creation_date) = " + year.toString() + ") "
                + "TO STDOUT DELIMITER ',' CSV "
                + "HEADER";

        LOGGER.info("STARTING");
        CopyManager cm = getPgConnection().getCopyAPI();
        try (FileWriter fw = new FileWriter(new File(auditDataFilePath + "/" + fileName))) {
            LOGGER.info("Got this far");
            cm.copyOut(copyCmd, fw);
            LOGGER.info("And even farther");
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("COMPLETED");
    }

    @SuppressWarnings({"resource", "rawtypes"})
    private PGConnection getPgConnection() throws SQLException {
        SessionImplementor sessImpl = (SessionImplementor) getSession();
        Connection conn = sessImpl.getJdbcConnectionAccess().obtainConnection();

        LOGGER.info(((DelegatingConnection) conn).getInnermostDelegateInternal().getClass().toString());

        return ((DelegatingConnection) conn).getInnermostDelegateInternal().unwrap(PGConnection.class);
    }
}
