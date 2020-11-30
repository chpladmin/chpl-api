package gov.healthit.chpl.dao.audit;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.hibernate.jdbc.Work;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
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

    public void doSomething() {
        LOGGER.info("STARTING");
        getSession().doWork(new Work() {

            @SuppressWarnings("resource")
            @Override
            public void execute(Connection connection) throws SQLException {
                CopyManager cm = new CopyManager(getJNDIConnection());

                try (FileWriter fw = new FileWriter(new File(auditDataFilePath + "\\vendor_auto.csv"))) {
                    LOGGER.info("Got this far");
                    cm.copyOut("COPY (SELECT * from openchpl.vendor) TO STDOUT DELIMITER ',' CSV HEADER", fw);
                    LOGGER.info("And even farther");
                } catch (Exception e) {
                    LOGGER.catching(e);
                }

            }
        });
        LOGGER.info("COMPLETED");
    }

    private BaseConnection getJNDIConnection() {
        String DATASOURCE_CONTEXT = "java:comp/env/jdbc/openchpl";
        BaseConnection result = null;
        try {
            Context initialContext = new InitialContext();
            // cast is necessary
            DataSource datasource = (DataSource) initialContext.lookup(DATASOURCE_CONTEXT);
            if (datasource != null) {
                result = datasource.getConnection().unwrap(PgConnection.class);
            } else {
                LOGGER.error("Failed to lookup datasource.");
            }
        } catch (NamingException ex) {
            LOGGER.catching(ex);
        } catch (SQLException ex) {
            LOGGER.catching(ex);
        }
        return result;
    }
}
