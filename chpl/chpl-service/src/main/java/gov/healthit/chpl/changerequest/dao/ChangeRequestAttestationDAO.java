package gov.healthit.chpl.changerequest.dao;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestation;
import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Component
public class ChangeRequestAttestationDAO extends BaseDAOImpl{

    public ChangeRequestAttestation create(ChangeRequest cr, ChangeRequestAttestation crAttestation) throws EntityRetrievalException {
        ChangeRequestAttestationEntity entity = getNewEntity(cr, crAttestation);
        create(entity);
        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }

    public ChangeRequestAttestation getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getEntityByChangeRequestId(changeRequestId));
    }

    public ChangeRequestAttestation update(ChangeRequestAttestation crAttestattion) throws EntityRetrievalException {
        ChangeRequestAttestationEntity entity = getEntity(crAttestattion.getId());
        entity.setAttestation(crAttestattion.getAttestation());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);
        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }

    private ChangeRequestAttestationEntity getNewEntity(ChangeRequest cr, ChangeRequestAttestation crAttestation) {
        ChangeRequestAttestationEntity entity = new ChangeRequestAttestationEntity();
        entity.setChangeRequest(getSession().load(ChangeRequestEntity.class, cr.getId()));
        entity.setAttestation(crAttestation.getAttestation());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        return entity;
    }

    private ChangeRequestAttestationEntity getEntity(Long changeRequestAttestationId) throws EntityRetrievalException {
        String hql = "FROM ChangeRequestAttestationEntity crAttestation "
                + "JOIN FETCH crAttestation.changeRequest "
                + "WHERE (NOT crAttestation.deleted = true) "
                + "AND (crAttestation.id = :changeRequestAttestationId) ";

        List<ChangeRequestAttestationEntity> result = entityManager
                .createQuery(hql, ChangeRequestAttestationEntity.class)
                .setParameter("changeRequestAttestationId", changeRequestAttestationId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request attestation not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request attestation in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private ChangeRequestAttestationEntity getEntityByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        String hql = "FROM ChangeRequestAttestationEntity crAttestation "
                + "JOIN FETCH crAttestation.changeRequest "
                + "WHERE (NOT crAttestation.deleted = true) "
                + "AND (crAttestation.changeRequest.id = :changeRequestId) ";

        List<ChangeRequestAttestationEntity> result = entityManager
                .createQuery(hql, ChangeRequestAttestationEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request attestation not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request attestation in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}
