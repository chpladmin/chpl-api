package gov.healthit.chpl.report;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Component
public class ReportMetadataDAO extends BaseDAOImpl {
    private static final String RWT_PRE_HTI_5_CHART = "Real World Testing";
    private static final String RWT_RESULTS_CHART = "Real World Testing Results";
    private static final String FLAG_BASED_RWT_CHARTS_REPORT_GROUP = "charts";
    private String reportEnvironment;
    private FF4j ff4j;

    @Autowired
    public ReportMetadataDAO(@Value("${report.environment}") String reportEnvironment,
            FF4j ff4j) {
        this.reportEnvironment = reportEnvironment;
        this.ff4j = ff4j;
    }

    public List<ReportMetadata> getReportMetadata(String reportGroup) {
        List<ReportMetadataEntity> entities = getEntities(reportEnvironment, reportGroup);
        if (entities != null) {
            return  entities.stream()
                    .filter(entity -> isReportVisibleBasedOnFlag(entity))
                    .map(ReportMetadataEntity::toDomain)
                    .toList();
        }
        return List.of();
    }

    private boolean isReportVisibleBasedOnFlag(ReportMetadataEntity report) {
        if (!ff4j.check(FeatureList.HTI_5_ERD)) {
            return !report.getReportGroup().equals(FLAG_BASED_RWT_CHARTS_REPORT_GROUP)
                    || !report.getTitle().equals(RWT_RESULTS_CHART);
        }
        return !report.getReportGroup().equals(FLAG_BASED_RWT_CHARTS_REPORT_GROUP)
                || !report.getTitle().equals(RWT_PRE_HTI_5_CHART);
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
