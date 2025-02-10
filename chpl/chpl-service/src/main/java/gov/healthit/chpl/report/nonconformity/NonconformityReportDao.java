package gov.healthit.chpl.report.nonconformity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.compliance.surveillance.entity.NonconformityTypeEntity;
import gov.healthit.chpl.compliance.surveillance.entity.SurveillanceNonconformityEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class NonconformityReportDao extends BaseDAOImpl {

    private CertificationCriterionService certificationCriterionService;

    @Autowired
    public NonconformityReportDao(CertificationCriterionService certificationCriterionService) {
        this.certificationCriterionService = certificationCriterionService;
    }

    public List<NonconformityTypeCount> getNonconformityCounts() {
        List<SurveillanceNonconformityEntity> allNonconformities = entityManager.createQuery(
                "SELECT sne "
                + "FROM SurveillanceNonconformityEntity sne "
                + "JOIN FETCH sne.type ncType "
                + "WHERE sne.deleted = false ", SurveillanceNonconformityEntity.class)
                .getResultList();

        List<NonconformityTypeEntity> nonconformityTypes = entityManager.createQuery(
                "FROM NonconformityTypeEntity e ", NonconformityTypeEntity.class)
                .getResultList();

        return nonconformityTypes.stream()
            .map(ncType ->
                NonconformityTypeCount.builder()
                        .count(allNonconformities.stream()
                                .filter(nc -> nc.getType().getId().equals(ncType.getId()))
                                .count())
                        .nonconformityType(ncType.toDomain())
                        .displayOrder(certificationCriterionService.getCertificationResultSortIndex(ncType.getId()))
                        .build())
            .filter(ncCount -> !ncCount.getCount().equals(0L))
            .toList();
    }
}
