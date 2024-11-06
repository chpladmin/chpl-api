package gov.healthit.chpl.report;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Component
public class ReportMetadataDAO extends BaseDAOImpl {
    private String reportEnvironment;

    @Autowired
    public ReportMetadataDAO(@Value("${report.environment}") String reportEnvironment) {
        this.reportEnvironment = reportEnvironment;
    }

    public ReportMetadata getReportMetadata(String reportKey) {
        ReportMetadataEntity entity = getEntity(reportEnvironment, reportKey);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public List<ReportMetadata> getReportMetadataByReportGroup(String reportGroup) {
        List<ReportMetadataEntity> entities = getEntities(reportEnvironment, reportGroup);
        if (entities != null) {
            return entities.stream()
                    .map(ReportMetadataEntity::toDomain)
                    .toList();
        }
        return List.of();
    }

    private ReportMetadataEntity getEntity(String environment, String reportKey) {
        Query query = entityManager.createQuery("SELECT rm "
                + "FROM ReportMetadataEntity rm "
                + "WHERE rm.environment = :environment "
                + "AND rm.reportKey = :reportKey "
                + "AND rm.deleted = false ",
                ReportMetadataEntity.class);
        query.setParameter("environment", environment);
        query.setParameter("reportKey", reportKey);

        List<ReportMetadataEntity> results = query.getResultList();
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results.get(0);
    }

    private List<ReportMetadataEntity> getEntities(String environment, String reportGroup) {
        Query query = entityManager.createQuery("SELECT rm "
                + "FROM ReportMetadataEntity rm "
                + "WHERE rm.environment = :environment "
                + "AND rm.reportGroup = :reportGroup "
                + "AND rm.deleted = false ",
                ReportMetadataEntity.class);
        query.setParameter("environment", environment);
        query.setParameter("reportGroup", reportGroup);

        List<ReportMetadataEntity> results = query.getResultList();
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results;
    }

}
