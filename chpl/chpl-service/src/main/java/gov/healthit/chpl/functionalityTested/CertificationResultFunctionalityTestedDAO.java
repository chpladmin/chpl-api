package gov.healthit.chpl.functionalityTested;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.util.AuthUtil;

@Component
public class CertificationResultFunctionalityTestedDAO extends BaseDAOImpl {

    public List<CertificationResultFunctionalityTested> getFunctionalitiesTestedForCertificationResult(
            Long certificationResultId) {
        List<CertificationResultFunctionalityTestedEntity> entities = getFunctionalitiesTestedForCertification(certificationResultId);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public Long createFunctionalityTestedMapping(Long certResultId, CertificationResultFunctionalityTested functionalityTested)
            throws EntityCreationException {
        try {
            CertificationResultFunctionalityTestedEntity entity = new CertificationResultFunctionalityTestedEntity();
            entity.setCertificationResultId(certResultId);
            entity.setFunctionalityTestedId(functionalityTested.getFunctionalityTested().getId());
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            create(entity);
            return entity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public CertificationResultFunctionalityTested addFunctionalityTestedMapping(CertificationResultFunctionalityTested functionalityTested) throws EntityCreationException {
        CertificationResultFunctionalityTestedEntity mapping = new CertificationResultFunctionalityTestedEntity();
        mapping.setCertificationResultId(functionalityTested.getCertificationResultId());
        mapping.setFunctionalityTestedId(functionalityTested.getFunctionalityTested().getId());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        create(mapping);

        return getCertificationResultFunctionalityTestedById(mapping.getId()).toDomain();
    }

    public void deleteFunctionalityTestedMapping(Long mappingId) {
        CertificationResultFunctionalityTestedEntity toDelete = getCertificationResultFunctionalityTestedById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    public CertificationResultFunctionalityTested updateFunctionalityTestedMapping(Long certificationResultTestToolId, CertificationResultFunctionalityTested certResultFunctionalityTested) {
        CertificationResultFunctionalityTestedEntity mapping = getCertificationResultFunctionalityTestedById(certificationResultTestToolId);

        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.merge(mapping);
        entityManager.flush();

        return getCertificationResultFunctionalityTestedById(mapping.getId()).toDomain();
    }


    private CertificationResultFunctionalityTestedEntity getCertificationResultFunctionalityTestedById(Long id) {
        CertificationResultFunctionalityTestedEntity entity = null;

        Query query = entityManager.createQuery("SELECT crft "
                + "FROM CertificationResultFunctionalityTestedEntity crft "
                + "LEFT OUTER JOIN FETCH crft.functionalityTested ft "
                + "WHERE (NOT crft.deleted = true) "
                + "AND (crft.id = :entityid) ",
                CertificationResultFunctionalityTestedEntity.class);
        query.setParameter("entityid", id);
        List<CertificationResultFunctionalityTestedEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultFunctionalityTestedEntity> getFunctionalitiesTestedForCertification(
            Long certificationResultId) {
        Query query = entityManager.createQuery("SELECT crft "
                + "FROM CertificationResultFunctionalityTestedEntity crft "
                + "LEFT OUTER JOIN FETCH crft.functionalityTested ft "
                + "WHERE (NOT crft.deleted = true) "
                + "AND (crft.certificationResultId = :certificationResultId) ",
                CertificationResultFunctionalityTestedEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultFunctionalityTestedEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }
}
