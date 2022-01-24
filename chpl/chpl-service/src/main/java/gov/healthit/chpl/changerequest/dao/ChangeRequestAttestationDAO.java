package gov.healthit.chpl.changerequest.dao;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationResponse;
import gov.healthit.chpl.attestation.entity.AttestationAnswerEntity;
import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.attestation.entity.AttestationQuestionEntity;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestation;
import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationResponseEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ChangeRequestAttestationDAO extends BaseDAOImpl{

    public ChangeRequestAttestation create(ChangeRequest cr, ChangeRequestAttestation crAttestation) throws EntityRetrievalException {
        ChangeRequestAttestationEntity parent = ChangeRequestAttestationEntity.builder()
                .changeRequest(getSession().load(ChangeRequestEntity.class, cr.getId()))
                .period(getAttestationPeriodEntity(crAttestation.getAttestationPeriod().getId()))
                .deleted(false)
                .lastModifiedUser(AuthUtil.getAuditId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .build();

        create(parent);

        crAttestation.getResponses().stream()
                .forEach(resp -> createAttestationResponse(resp, parent.getId()));
        return ChangeRequestConverter.convert(getEntity(parent.getId()));
    }


    private ChangeRequestAttestationResponseEntity createAttestationResponse(AttestationResponse response, Long changeRequestAttestationId) {
        try {
            ChangeRequestAttestationResponseEntity entity = getChangeRequestAttestationResponseEntity(response, changeRequestAttestationId);
            create(entity);
            return entity;
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            throw new RuntimeException(e);
        }
    }

    public ChangeRequestAttestation getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return ChangeRequestConverter.convert(getEntityByChangeRequestId(changeRequestId));
    }

    private ChangeRequestAttestationEntity getEntity(Long changeRequestAttestationId) throws EntityRetrievalException {
        String hql = "SELECT DISTINCT crae "
                + "FROM ChangeRequestAttestationEntity crae "
                + "JOIN FETCH crae.changeRequest cr "
                + "JOIN FETCH crae.responses resp "
                + "JOIN FETCH resp.question ques "
                + "JOIN FETCH resp.answer ans "
                + "WHERE (NOT crae.deleted = true) "
                + "AND (NOT cr.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (crae.id = :changeRequestAttestationId) ";

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
        String hql = "SELECT DISTINCT crae "
                + "FROM ChangeRequestAttestationEntity crae "
                + "JOIN FETCH crae.changeRequest cr "
                + "JOIN FETCH crae.responses resp "
                + "JOIN FETCH resp.question ques "
                + "JOIN FETCH resp.answer ans "
                + "WHERE (NOT crae.deleted = true) "
                + "AND (NOT cr.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (cr.id = :changeRequestId) ";

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

    private ChangeRequestAttestationResponseEntity getChangeRequestAttestationResponseEntity(
            AttestationResponse response, Long changeRequestAttestationId) throws EntityRetrievalException {
        return ChangeRequestAttestationResponseEntity.builder()
                .changeRequestAttestationId(changeRequestAttestationId)
                .answer(getAttestationAnswerEntity(response.getAnswer().getId()))
                .question(getAttestationQuestionEntity(response.getQuestion().getId()))
                .deleted(false)
                .lastModifiedUser(AuthUtil.getAuditId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .build();
    }

    private List<AttestationPeriodEntity> getAllPeriodEntities() {
        List<AttestationPeriodEntity> result = entityManager.createQuery(
                "FROM AttestationPeriodEntity ape "
                + "WHERE (NOT ape.deleted = true)",
                AttestationPeriodEntity.class).getResultList();
        return result;
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

    private AttestationQuestionEntity getAttestationQuestionEntity(Long id) throws EntityRetrievalException {
        List<AttestationQuestionEntity> result = entityManager.createQuery(
                "FROM AttestationQuestionEntity aqe "
                + "WHERE (NOT aqe.deleted = true) "
                + "AND aqe.id = :attestationQuestionId", AttestationQuestionEntity.class)
                .setParameter("attestationQuestionId", id)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Attestation Question not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate Attestation Question in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private AttestationAnswerEntity getAttestationAnswerEntity(Long id) throws EntityRetrievalException {
        List<AttestationAnswerEntity> result = entityManager.createQuery(
                "FROM AttestationAnswerEntity aae "
                + "WHERE (NOT aae.deleted = true) "
                + "AND aae.id = :attestationAnswerId", AttestationAnswerEntity.class)
                .setParameter("attestationAnswerId", id)
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
