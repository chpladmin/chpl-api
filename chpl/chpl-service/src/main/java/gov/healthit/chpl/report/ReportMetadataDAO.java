package gov.healthit.chpl.report;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    public List<ReportMetadata> getReportMetadata(String reportGroup) {
        List<ReportMetadataEntity> entities = getEntities(reportEnvironment, reportGroup);
        if (entities != null) {
            return entities.stream()
                    .map(ReportMetadataEntity::toDomain)
                    .toList();
        }
        return List.of();
    }

    private List<ReportMetadataEntity> getEntities(String environment, String reportGroup) {
        String hql = "SELECT rm "
                + "FROM ReportMetadataEntity rm "
                + "LEFT JOIN FETCH rm.roleMaps "
                + "WHERE rm.environment = :environment "
                + "AND rm.deleted = false ";
        if (!StringUtils.isEmpty(reportGroup)) {
            hql += " AND rm.reportGroup = :reportGroup ";
        }
        Query query = entityManager.createQuery(hql, ReportMetadataEntity.class);
        query.setParameter("environment", environment);
        if (!StringUtils.isEmpty(reportGroup)) {
            query.setParameter("reportGroup", reportGroup);
        }

        List<ReportMetadataEntity> results = query.getResultList();
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results;
    }

}
