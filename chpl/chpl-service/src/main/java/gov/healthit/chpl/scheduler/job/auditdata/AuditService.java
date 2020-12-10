package gov.healthit.chpl.scheduler.job.auditdata;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import org.hibernate.engine.spi.SessionImplementor;
import org.postgresql.PGConnection;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

public abstract class AuditService extends BaseDAOImpl {
    private PGConnection pgConnection;
    private String auditDataFilePath;

    public abstract Long getAuditDataCount(Integer month, Integer year);
    public abstract void deleteAuditData(Integer month, Integer year);
    public abstract void archiveDataToFile(Integer month, Integer year, String fileName, boolean includeHeaders)
            throws SQLException;
    public abstract String getProposedFilename(Integer month, Integer year);
    public abstract String getAuditTableNme();

    public AuditService(String auditDataFilePath) {
        this.auditDataFilePath =  auditDataFilePath;
    }

    public String getAuditDataFilePath() {
        return auditDataFilePath;
    }

    @SuppressWarnings({"resource", "rawtypes"})
    public PGConnection getPgConnection() throws SQLException {
        if (pgConnection == null) {
            SessionImplementor sessImpl = (SessionImplementor) getSession();
            Connection conn = sessImpl.getJdbcConnectionAccess().obtainConnection();
            pgConnection = ((DelegatingConnection) conn).getInnermostDelegateInternal().unwrap(PGConnection.class);
        }
        return pgConnection;
    }
}
