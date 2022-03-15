package gov.healthit.chpl.changerequest.dao;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationSubmittedResponse;
import gov.healthit.chpl.attestation.entity.AttestationEntity;
import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.attestation.entity.AttestationValidResponseEntity;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationResponseEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationSubmissionEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ChangeRequestAttestationDAO extends BaseDAOImpl{

    public ChangeRequestAttestationSubmission create(ChangeRequest cr, ChangeRequestAttestationSubmission changeRequestAttestationSubmission) throws EntityRetrievalException {
        ChangeRequestAttestationSubmissionEntity parent = ChangeRequestAttestationSubmissionEntity.builder()
                .changeRequest(getSession().load(ChangeRequestEntity.class, cr.getId()))
                .period(getAttestationPeriodEntity(changeRequestAttestationSubmission.getAttestationPeriod().getId()))
                .signature(changeRequestAttestationSubmission.getSignature())
                .signatureEmail(changeRequestAttestationSubmission.getSignatureEmail())
                .deleted(false)
                .lastModifiedUser(AuthUtil.getAuditId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .build();

        create(parent);

        changeRequestAttestationSubmission.getAttestationResponses().stream()
                .forEach(resp -> createAttestationResponse(resp, parent.getId()));
        return ChangeRequestConverter.convert(getEntity(parent.getId()));
    }

    public ChangeRequestAttestationSubmission update(ChangeRequestAttestationSubmission changeRequestAttestationSubmission) throws EntityRetrievalException {
        ChangeRequestAttestationSubmissionEntity entity = getEntity(changeRequestAttestationSubmission.getId());
        entity.setSignature(changeRequestAttestationSubmission.getSignature());
        entity.setSignatureEmail(changeRequestAttestationSubmission.getSignatureEmail());
        entity.getPeriod().setId(changeRequestAttestationSubmission.getAttestationPeriod().getId());
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        entity.getResponses().stream()
                .forEach(response -> {
                    AttestationSubmittedResponse submittedResponse = changeRequestAttestationSubmission.getAttestationResponses().stream()
                            .filter(inResponse -> inResponse.getId().equals(response.getId()))
                            .findAny()
                            .get();

                    response.getValidResponse().setId(submittedResponse.getResponse().getId());
                    response.setLastModifiedUser(AuthUtil.getAuditId());
                    update(response);
                });

        update(entity);

        return ChangeRequestConverter.convert(getEntity(entity.getId()));
    }


    private ChangeRequestAttestationResponseEntity createAttestationResponse(AttestationSubmittedResponse response, Long changeRequestAttestationSubmissionId) {
        try {
            ChangeRequestAttestationResponseEntity entity = getChangeRequestAttestationResponseEntity(response, changeRequestAttestationSubmissionId);
            create(entity);
            return entity;
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            throw new RuntimeException(e);
        }
    }

    public ChangeRequestAttestationSubmission getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getEntityByChangeRequestId(changeRequestId));
    }

    private ChangeRequestAttestationSubmissionEntity getEntity(Long changeRequestSubmissionAttestationId) throws EntityRetrievalException {
        String hql = "SELECT DISTINCT crase "
                + "FROM ChangeRequestAttestationSubmissionEntity crase "
                + "JOIN FETCH crase.changeRequest cr "
                + "JOIN FETCH crase.responses resp "
                + "JOIN FETCH resp.attestation att "
                + "JOIN FETCH att.condition cond "
                + "JOIN FETCH resp.validResponse vr "
                + "WHERE (NOT crase.deleted = true) "
                + "AND (NOT cr.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (crase.id = :changeRequestAttestationSubmissionId) ";

        List<ChangeRequestAttestationSubmissionEntity> result = entityManager
                .createQuery(hql, ChangeRequestAttestationSubmissionEntity.class)
                .setParameter("changeRequestAttestationSubmissionId", changeRequestSubmissionAttestationId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request attestation submission not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request attestation submission in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private ChangeRequestAttestationSubmissionEntity getEntityByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        String hql = "SELECT DISTINCT crase "
                + "FROM ChangeRequestAttestationSubmissionEntity crase "
                + "JOIN FETCH crase.changeRequest cr "
                + "JOIN FETCH crase.responses resp "
                + "JOIN FETCH resp.attestation att "
                + "JOIN FETCH att.condition cond "
                + "JOIN FETCH resp.validResponse vr "
                + "WHERE (NOT crase.deleted = true) "
                + "AND (NOT cr.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (cr.id = :changeRequestId) ";

        List<ChangeRequestAttestationSubmissionEntity> result = entityManager
                .createQuery(hql, ChangeRequestAttestationSubmissionEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Change request attestation submission not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate change request attestation submission in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private ChangeRequestAttestationResponseEntity getChangeRequestAttestationResponseEntity(
            AttestationSubmittedResponse response, Long changeRequestAttestatioSubmissionId) throws EntityRetrievalException {
        return ChangeRequestAttestationResponseEntity.builder()
                .changeRequestAttestationSubmissionId(changeRequestAttestatioSubmissionId)
                .validResponse(getAttestationValidResponseEntity(response.getResponse().getId()))
                .attestation(getAttestationEntity(response.getAttestation().getId()))
                .deleted(false)
                .lastModifiedUser(AuthUtil.getAuditId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .build();
    }

    private AttestationPeriodEntity getAttestationPeriodEntity(Long id) throws EntityRetrievalException {
        List<AttestationPeriodEntity> result = entityManager.createQuery(
                "FROM AttestationPeriodEntity ape "
                + "WHERE (NOT ape.deleted = true) "
                + "AND ape.id = :attestationPeriodId", AttestationPeriodEntity.class)
                .setParameter("attestationPeriodId", id)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Attestation Period not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate Attestation Period in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private AttestationEntity getAttestationEntity(Long id) throws EntityRetrievalException {
        List<AttestationEntity> result = entityManager.createQuery(
                "FROM AttestationEntity ae "
                + "WHERE (NOT ae.deleted = true) "
                + "AND ae.id = :attestationId", AttestationEntity.class)
                .setParameter("attestationId", id)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Attestation not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate Attestation in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private AttestationValidResponseEntity getAttestationValidResponseEntity(Long id) throws EntityRetrievalException {
        List<AttestationValidResponseEntity> result = entityManager.createQuery(
                "FROM AttestationValidResponseEntity vr "
                + "WHERE (NOT vr.deleted = true) "
                + "AND vr.id = :attestationValidResponseId", AttestationValidResponseEntity.class)
                .setParameter("attestationValidResponseId", id)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Attestation Answernot found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate Attestation Answer in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

}
