package gov.healthit.chpl.codeset;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;

@Component
public class CertificationResultCodeSetDAO extends BaseDAOImpl {
    public List<CertificationResultCodeSet> getCodeSetsForCertificationResult(Long certificationResultId) {
        List<CertificationResultCodeSetEntity> entities = getCodeSetForCertificationResult(certificationResultId);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public Long createCodeSetMapping(Long certResultId, CertificationResultCodeSet codeSet)
            throws EntityCreationException {
        try {
            CertificationResultCodeSetEntity entity = new CertificationResultCodeSetEntity();
            entity.setCertificationResultId(certResultId);
            entity.setCodeSetId(codeSet.getCodeSet().getId());
            create(entity);
            return entity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public CertificationResultCodeSet addCodeSetMapping(CertificationResultCodeSet codeSet) throws EntityCreationException {
        CertificationResultCodeSetEntity mapping = new CertificationResultCodeSetEntity();
        mapping.setCertificationResultId(codeSet.getCertificationResultId());
        mapping.setCodeSetId(codeSet.getCodeSet().getId());
        create(mapping);

        return getCertificationResultCodeSetById(mapping.getId()).toDomain();
    }

    public void deleteCodeSetMapping(Long mappingId) {
        CertificationResultCodeSetEntity toDelete = getCertificationResultCodeSetById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    public CertificationResultCodeSet updateCodeSetMapping(Long certificationResultTestToolId, CertificationResultCodeSet certResultCodeSet) {
        CertificationResultCodeSetEntity mapping = getCertificationResultCodeSetById(certificationResultTestToolId);

        entityManager.merge(mapping);
        entityManager.flush();

        return getCertificationResultCodeSetById(mapping.getId()).toDomain();
    }


    private CertificationResultCodeSetEntity getCertificationResultCodeSetById(Long id) {
        CertificationResultCodeSetEntity entity = null;

        Query query = entityManager.createQuery("SELECT crcs "
                + "FROM CertificationResultCodeSetEntity crcs "
                + "LEFT OUTER JOIN FETCH crcs.codeSet csd "
                + "WHERE (NOT crcs.deleted = true) "
                + "AND (crcs.id = :entityid) ",
                CertificationResultCodeSetEntity.class);
        query.setParameter("entityid", id);
        List<CertificationResultCodeSetEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultCodeSetEntity> getCodeSetForCertificationResult(Long certificationResultId) {
        Query query = entityManager.createQuery("SELECT crcs "
                + "FROM CertificationResultCodeSetEntity crcs "
                + "LEFT OUTER JOIN FETCH crcs.codeSet csd "
                + "WHERE (NOT crcs.deleted = true) "
                + "AND (crcs.certificationResultId = :certificationResultId) ",
                CertificationResultCodeSetEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultCodeSetEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }

}
