package gov.healthit.chpl.scheduler.job.listingvalidation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class ListingValidationReportDAO extends BaseDAOImpl {

    public ListingValidationReport create(ListingValidationReport lvr) {
        ListingValidationReportEntity entity = ListingValidationReportEntity.builder()
                .chplProductNumber(lvr.getChplProductNumber())
                .certifiedProductId(lvr.getCertifiedProductId())
                .certificationBodyId(lvr.getCertificationBodyId())
                .product(lvr.getProduct())
                .version(lvr.getVersion())
                .developer(lvr.getDeveloper())
                .certificationBody(lvr.getCertificationBody())
                .certificationStatusName(lvr.getCertificationStatusName())
                .errorMessage(lvr.getErrorMessage())
                .reportDate(lvr.getReportDate())
                .lastModifiedUser(User.SYSTEM_USER_ID)
                .deleted(false)
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return new ListingValidationReport(entity);
    }

    public void deleteAll() {
        entityManager.createQuery("UPDATE ListingValidationReportEntity lvr SET lvr.deleted = true WHERE lvr.deleted = false")
                .executeUpdate();
    }

    public List<ListingValidationReport> getAll() {
        return getAllEntities().stream()
                .map(entity -> new ListingValidationReport(entity))
                .collect(Collectors.toList());
    }

    private List<ListingValidationReportEntity> getAllEntities() {
        return entityManager.createQuery("FROM ListingValidationReportEntity lvr "
                + "WHERE (lvr.deleted = false)", ListingValidationReportEntity.class)
                .getResultList();
    }

    private ListingValidationReportEntity deleteEntity(ListingValidationReportEntity entity) {
        entity.setDeleted(true);
        entity.setLastModifiedUser(User.SYSTEM_USER_ID);
        entityManager.merge(entity);
        entityManager.flush();
        return entity;
    }
}
