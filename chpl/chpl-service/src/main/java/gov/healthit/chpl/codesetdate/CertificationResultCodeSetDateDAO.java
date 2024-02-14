package gov.healthit.chpl.codesetdate;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;

@Component
public class CertificationResultCodeSetDateDAO extends BaseDAOImpl {
    public List<CertificationResultCodeSetDate> getCodeSetDateForCertificationResult(Long certificationResultId) {
        List<CertificationResultCodeSetDateEntity> entities = getCodeSetDateForCertification(certificationResultId);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public Long createCodeSetDateMapping(Long certResultId, CertificationResultCodeSetDate codeSetDate)
            throws EntityCreationException {
        try {
            CertificationResultCodeSetDateEntity entity = new CertificationResultCodeSetDateEntity();
            entity.setCertificationResultId(certResultId);
            entity.setCodeSetDateId(codeSetDate.getCodeSetDate().getId());
            create(entity);
            return entity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public CertificationResultCodeSetDate addCodeSetDateMapping(CertificationResultCodeSetDate codeSetDate) throws EntityCreationException {
        CertificationResultCodeSetDateEntity mapping = new CertificationResultCodeSetDateEntity();
        mapping.setCertificationResultId(codeSetDate.getCertificationResultId());
        mapping.setCodeSetDateId(codeSetDate.getCodeSetDate().getId());
        create(mapping);

        return getCertificationResultCodeSetDateById(mapping.getId()).toDomain();
    }

    public void deleteCodeSetDateMapping(Long mappingId) {
        CertificationResultCodeSetDateEntity toDelete = getCertificationResultCodeSetDateById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    public CertificationResultCodeSetDate updateCodeSetDateMapping(Long certificationResultTestToolId, CertificationResultCodeSetDate certResultCodeSetDate) {
        CertificationResultCodeSetDateEntity mapping = getCertificationResultCodeSetDateById(certificationResultTestToolId);

        entityManager.merge(mapping);
        entityManager.flush();

        return getCertificationResultCodeSetDateById(mapping.getId()).toDomain();
    }


    private CertificationResultCodeSetDateEntity getCertificationResultCodeSetDateById(Long id) {
        CertificationResultCodeSetDateEntity entity = null;

        Query query = entityManager.createQuery("SELECT crcsd "
                + "FROM CertificationResultCodeSetDateEntity crcsd "
                + "LEFT OUTER JOIN FETCH crcsd.codeSetDate csd "
                + "WHERE (NOT crcsd.deleted = true) "
                + "AND (crcsd.id = :entityid) ",
                CertificationResultCodeSetDateEntity.class);
        query.setParameter("entityid", id);
        List<CertificationResultCodeSetDateEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultCodeSetDateEntity> getCodeSetDateForCertification(Long certificationResultId) {
        Query query = entityManager.createQuery("SELECT crcsd "
                + "FROM CertificationResultCodeSetDateEntity crcsd "
                + "LEFT OUTER JOIN FETCH crcsd.codeSetDate csd "
                + "WHERE (NOT cr.deleted = true) "
                + "AND (crcsd.certificationResultId = :certificationResultId) ",
                CertificationResultCodeSetDateEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultCodeSetDateEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }

}
