package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.entity.CertificationEditionEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Deprecated
@Repository("certificationEditionDAO")
public class CertificationEditionDAO extends BaseDAOImpl {

    public List<CertificationEdition> findAll() {
        List<CertificationEditionEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public CertificationEdition getById(Long criterionEditionId) throws EntityRetrievalException {
        CertificationEdition result = null;
        CertificationEditionEntity entity = getEntityById(criterionEditionId);

        if (entity != null) {
            result = entity.toDomain();
        }
        return result;
    }

    public List<CertificationEdition> getEditions(List<Long> listingIds) {
        Query query = entityManager.createQuery(
                "SELECT DISTINCT edition "
                        + "FROM CertificationEditionEntity edition, CertifiedProductEntity listing "
                        + "WHERE listing.deleted <> true "
                        + "AND listing.certificationEditionId = edition.id "
                        + "AND listing.id IN (:listingIds) ",
                CertificationEditionEntity.class);
        query.setParameter("listingIds", listingIds);
        List<CertificationEditionEntity> editions = query.getResultList();
        List<CertificationEdition> results = new ArrayList<CertificationEdition>();
        for (CertificationEditionEntity edition : editions) {
            results.add(edition.toDomain());
        }
        return results;
    }

    public CertificationEdition getByYear(String year) {
        CertificationEdition result = null;
        CertificationEditionEntity yearEntity = getEntityByYear(year);
        if (yearEntity != null) {
            result = yearEntity.toDomain();
        }
        return result;
    }

    private List<CertificationEditionEntity> getAllEntities() {
        List<CertificationEditionEntity> result = entityManager
                .createQuery("from CertificationEditionEntity where (NOT deleted = true) ",
                        CertificationEditionEntity.class)
                .getResultList();
        return result;

    }

    private CertificationEditionEntity getEntityById(Long id) throws EntityRetrievalException {
        CertificationEditionEntity entity = null;
        Query query = entityManager.createQuery(
                "from CertificationEditionEntity where (NOT deleted = true) AND (certification_edition_id = :entityid) ",
                CertificationEditionEntity.class);
        query.setParameter("entityid", id);
        List<CertificationEditionEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate criterion edition id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private CertificationEditionEntity getEntityByYear(String year) {
        CertificationEditionEntity entity = null;
        Query query = entityManager.createQuery(
                "from CertificationEditionEntity where (NOT deleted = true) AND (year = :year) ",
                CertificationEditionEntity.class);
        query.setParameter("year", year);
        List<CertificationEditionEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

}
