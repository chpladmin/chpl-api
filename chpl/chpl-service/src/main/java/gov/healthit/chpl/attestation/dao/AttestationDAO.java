package gov.healthit.chpl.attestation.dao;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.attestation.domain.AttestationCategory;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationResponse;
import gov.healthit.chpl.attestation.domain.DeveloperAttestation;
import gov.healthit.chpl.attestation.entity.AttestationAnswerEntity;
import gov.healthit.chpl.attestation.entity.AttestationCategoryEntity;
import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.attestation.entity.AttestationQuestionEntity;
import gov.healthit.chpl.attestation.entity.DeveloperAttestationEntity;
import gov.healthit.chpl.attestation.entity.DeveloperAttestationResponseEntity;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class AttestationDAO extends BaseDAOImpl{
    private DeveloperDAO developerDAO;

    @Autowired
    public AttestationDAO(DeveloperDAO developerDAO) {
        this.developerDAO = developerDAO;
    }

    public List<AttestationPeriod> getAllPeriods() {
        return getAllPeriodEntities().stream()
                .map(ent -> new AttestationPeriod(ent))
                .collect(Collectors.toList());
    }

    public List<AttestationCategory> getAttestationForm() {
        return getAttestationFormEntities().stream()
                .map(ent -> new AttestationCategory(ent))
                .collect(Collectors.toList());
    }

    public DeveloperAttestation create(DeveloperAttestation attestation) throws EntityRetrievalException {
        DeveloperAttestationEntity entity = DeveloperAttestationEntity.builder()
                .developer(DeveloperEntity.builder()
                        .id(attestation.getDeveloper().getDeveloperId())
                        .build())
                .period(AttestationPeriodEntity.builder()
                        .id(attestation.getPeriod().getId())
                        .build())
                .deleted(false)
                .lastModifiedUser(AuthUtil.getAuditId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .build();

        create(entity);

        attestation.getResponses().stream()
                .forEach(resp -> createAttestationResponse(resp, entity.getId()));

        return new DeveloperAttestation(getDeveloperAttestationEntity(entity.getId()));
    }

    private DeveloperAttestationResponseEntity createAttestationResponse(AttestationResponse response, Long developerAttestationId) {
        try {
            DeveloperAttestationResponseEntity entity = DeveloperAttestationResponseEntity.builder()
                    .developerAttestationId(developerAttestationId)
                    .answer(getAttestationAnswerEntity(response.getAnswer().getId()))
                    .question(getAttestationQuestionEntity(response.getQuestion().getId()))
                    .deleted(false)
                    .lastModifiedUser(AuthUtil.getAuditId())
                    .creationDate(new Date())
                    .lastModifiedDate(new Date())
                    .build();
            create(entity);
            return entity;
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            throw new RuntimeException(e);
        }
    }

    private DeveloperAttestationResponseEntity getDeveloperAttestationResponseEntity(AttestationResponse response, Long lastModifiedUserId) {
        return DeveloperAttestationResponseEntity.builder()
                .answer(AttestationAnswerEntity.builder()
                        .id(response.getAnswer().getId())
                        .build())
                .question(AttestationQuestionEntity.builder()
                        .id(response.getQuestion().getId())
                        .build())
                .lastModifiedUser(lastModifiedUserId)
                .build();
    }

    private List<AttestationPeriodEntity> getAllPeriodEntities() {
        List<AttestationPeriodEntity> result = entityManager.createQuery(
                "FROM AttestationPeriodEntity ape "
                + "WHERE (NOT ape.deleted = true)",
                AttestationPeriodEntity.class).getResultList();
        return result;
    }


    private List<AttestationCategoryEntity> getAttestationFormEntities() {
        List<AttestationCategoryEntity> result = entityManager.createQuery(
                "SELECT DISTINCT ace "
                + "FROM AttestationCategoryEntity ace "
                + "JOIN FETCH ace.questions aqe "
                + "JOIN FETCH aqe.answers aae "
                + "WHERE (NOT ace.deleted = true) "
                + "AND (NOT aqe.deleted = true )"
                + "AND (NOT aae.deleted = true) ",
                AttestationCategoryEntity.class)
                .getResultList();
        return result;
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

    private DeveloperAttestationEntity getDeveloperAttestationEntity(Long developerAttestationId) throws EntityRetrievalException {
        String hql = "SELECT DISTINCT dae "
                + "FROM DeveloperAttestationEntity dae "
                + "JOIN FETCH dae.developer d "
                + "JOIN FETCH dae.responses resp "
                + "JOIN FETCH dae.period per "
                + "WHERE (NOT dae.deleted = true) "
                + "AND (NOT d.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (NOT per.deleted = true) "
                + "AND (dae.id = :developerAttestationId) ";

        List<DeveloperAttestationEntity> result = entityManager
                .createQuery(hql, DeveloperAttestationEntity.class)
                .setParameter("developerAttestationId", developerAttestationId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Developer attestation not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate developer attestation in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

}
