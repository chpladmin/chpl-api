package gov.healthit.chpl.dao.audit;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.jdbc.Work;
import org.postgresql.copy.CopyManager;
import org.postgresql.jdbc.PgConnection;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AuditDAO extends BaseDAOImpl {

    public void doSomething() {
        LOGGER.info("STARTING");
        getSession().doWork(new Work() {

            @Override
            public void execute(Connection connection) throws SQLException {
                CopyManager cm = new CopyManager(connection.getMetaData().getConnection().unwrap(PgConnection.class));

                try (FileWriter fw = new FileWriter(new File("c:\\chpl\\files\\vendor_auto.csv"))) {
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
}
