package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

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
public class UpdatedListingStatusReportDAO extends BaseDAOImpl {
    public UpdatedListingStatusReport create(UpdatedListingStatusReport ulsr) {
        UpdatedListingStatusReportEntity entity = UpdatedListingStatusReportEntity.builder()
                .certifiedProductId(ulsr.getCertifiedProductId())
                .reportDay(LocalDate.now())
                .criteriaRequireUpdateCount(ulsr.getCriteriaRequireUpdateCount())
                .daysUpdatedEarly(ulsr.getDaysUpdatedEarly())
                .chplProductNumber(ulsr.getChplProductNumber())
                .product(ulsr.getProduct())
                .version(ulsr.getVersion())
                .developer(ulsr.getDeveloper())
                .certificationBody(ulsr.getCertificationBody())
                .certificationStatus(ulsr.getCertificationStatus())
                .developerId(ulsr.getDeveloperId())
                .certificationBodyId(ulsr.getCertificationBodyId())
                .certificationStatusId(ulsr.getCertificationStatusId())
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toDomain();
    }

    public List<UpdatedListingStatusReport> getUpdatedListingStatusReportsByDate(LocalDate reportDate) {
        return getUpdatedListingStatusReportEntitiessByDate(reportDate).stream()
                .map(ent -> ent.toDomain())
                .toList();
    }

    public void deleteUpdatedListingStatusReportsByDate(LocalDate reportDate) {
        getUpdatedListingStatusReportEntitiessByDate(reportDate).stream()
                .forEach(ent -> {
                    ent.setDeleted(true);
                    update(ent);
                });
    }

    public LocalDate getMaxReportDate() {
        Session session = getSession();
        Criteria criteria = session.createCriteria(UpdatedListingStatusReportEntity.class);
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

    private List<UpdatedListingStatusReportEntity> getUpdatedListingStatusReportEntitiessByDate(LocalDate reportDate) {
        return entityManager
                .createQuery("SELECT ulsr "
                            + "FROM UpdatedListingStatusReportEntity ulsr "
                            + "WHERE (NOT ulsr.deleted = true) "
                            + "AND ulsr.reportDay = :reportDate", UpdatedListingStatusReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }

}
