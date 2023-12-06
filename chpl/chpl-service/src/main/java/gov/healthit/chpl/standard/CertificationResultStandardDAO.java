package gov.healthit.chpl.standard;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.util.AuthUtil;

@Component
public class CertificationResultStandardDAO extends BaseDAOImpl {
    public List<CertificationResultStandard> getStandardsForCertificationResult(Long certificationResultId) {
        List<CertificationResultStandardEntity> entities = getFunctionalitiesTestedForCertification(certificationResultId);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public Long createStandardMapping(Long certResultId, CertificationResultStandard standard)
            throws EntityCreationException {
        try {
            CertificationResultStandardEntity entity = new CertificationResultStandardEntity();
            entity.setCertificationResultId(certResultId);
            entity.setStandardId(standard.getStandard().getId());
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            create(entity);
            return entity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public CertificationResultStandard addStandardMapping(CertificationResultStandard standard) throws EntityCreationException {
        CertificationResultStandardEntity mapping = new CertificationResultStandardEntity();
        mapping.setCertificationResultId(standard.getCertificationResultId());
        mapping.setStandardId(standard.getStandard().getId());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        create(mapping);

        return getCertificationResultStandardById(mapping.getId()).toDomain();
    }

    public void deleteStandardMapping(Long mappingId) {
        CertificationResultStandardEntity toDelete = getCertificationResultStandardById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    public CertificationResultStandard updateStandardMapping(Long certificationResultTestToolId, CertificationResultStandard certResultStandard) {
        CertificationResultStandardEntity mapping = getCertificationResultStandardById(certificationResultTestToolId);

        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.merge(mapping);
        entityManager.flush();

        return getCertificationResultStandardById(mapping.getId()).toDomain();
    }


    private CertificationResultStandardEntity getCertificationResultStandardById(Long id) {
        CertificationResultStandardEntity entity = null;

        Query query = entityManager.createQuery("SELECT crs "
                + "FROM CertificationResultStandardEntity crs "
                + "LEFT OUTER JOIN FETCH crs.standard s "
                + "WHERE (NOT crs.deleted = true) "
                + "AND (crs.id = :entityid) ",
                CertificationResultStandardEntity.class);
        query.setParameter("entityid", id);
        List<CertificationResultStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultStandardEntity> getFunctionalitiesTestedForCertification(
            Long certificationResultId) {
        Query query = entityManager.createQuery("SELECT crs "
                + "FROM CertificationResultStandardEntity crs "
                + "LEFT OUTER JOIN FETCH crs.standard ft "
                + "WHERE (NOT crs.deleted = true) "
                + "AND (crs.certificationResultId = :certificationResultId) ",
                CertificationResultStandardEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultStandardEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }

}
