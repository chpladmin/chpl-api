package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class UpdatedCriteriaStatusReportDAO extends BaseDAOImpl {
    public UpdatedCriteriaStatusReport create(UpdatedCriteriaStatusReport ucsr) {
        UpdatedCriteriaStatusReportEntity entity = UpdatedCriteriaStatusReportEntity.builder()
                .certificationCriterionId(ucsr.getCertificationCriterionId())
                .reportDay(LocalDate.now())
                .listingsWithCriterionCount(ucsr.getListingsWithCriterionCount())
                .fullyUpToDateCount(ucsr.getFullyUpToDateCount())
                .functionalitiesTestedUpToDateCount(ucsr.getFunctionalitiesTestedUpToDateCount())
                .standardsUpToDateCount(ucsr.getStandardsUpToDateCount())
                .codeSetsUpToDateCount(ucsr.getCodeSetsUpToDateCount())
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toDomain();
    }

    public List<UpdatedCriteriaStatusReport> getUpdatedCriteriaStatusReportsByDate(LocalDate reportDate) {
        return getUpdatedCriteriaStatusReportEntitiessByDate(reportDate).stream()
                .map(ent -> ent.toDomain())
                .toList();
    }

    public void deleteUpdatedCriteriaStatusReportsByDate(LocalDate reportDate) {
        getUpdatedCriteriaStatusReportEntitiessByDate(reportDate).stream()
                .forEach(ent -> {
                    ent.setDeleted(true);
                    update(ent);
                });
    }

    public LocalDate getMaxReportDate() {
        Session session = getSession();
        Criteria criteria = session.createCriteria(UpdatedCriteriaStatusReportEntity.class);
        ProjectionList projectionList = Projections.projectionList();
        projectionList.add(Projections.max("reportDay"));
        criteria.setProjection(projectionList);
        List<LocalDate> list = criteria.list();
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        } else {
            return null;
        }
    }

    private List<UpdatedCriteriaStatusReportEntity> getUpdatedCriteriaStatusReportEntitiessByDate(LocalDate reportDate) {
        return entityManager
                .createQuery("SELECT ucsr "
                            + "FROM UpdatedCriteriaStatusReportEntity ucsr "
                            + "WHERE (NOT ucsr.deleted = true) "
                            + "AND ucsr.reportDay = :reportDate", UpdatedCriteriaStatusReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }

}
