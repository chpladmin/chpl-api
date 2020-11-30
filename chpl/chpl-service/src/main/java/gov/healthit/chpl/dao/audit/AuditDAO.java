package gov.healthit.chpl.dao.audit;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.postgresql.copy.CopyManager;
import org.postgresql.jdbc.PgConnection;
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

    @SuppressWarnings("resource")
    public void doSomething() throws SQLException {
        LOGGER.info("STARTING");


        CopyManager cm = new CopyManager(getPgConnection());
        try (FileWriter fw = new FileWriter(new File(auditDataFilePath + "vendor_auto.csv"))) {
            LOGGER.info("Got this far");
            cm.copyOut("COPY (SELECT * from openchpl.vendor) TO STDOUT DELIMITER ',' CSV HEADER", fw);
            LOGGER.info("And even farther");
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("COMPLETED");
    }

    @SuppressWarnings({
            "resource", "rawtypes"
    })
    private PgConnection getPgConnection() throws SQLException {
        Session session = getSession();
        SessionImplementor sessImpl = (SessionImplementor) session;
        Connection conn = null;
        conn = sessImpl.getJdbcConnectionAccess().obtainConnection();

        return (org.postgresql.jdbc.PgConnection)
                    ((DelegatingConnection) conn).getInnermostDelegateInternal();
    }
}
