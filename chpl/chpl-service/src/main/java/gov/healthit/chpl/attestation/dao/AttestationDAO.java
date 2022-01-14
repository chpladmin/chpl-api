package gov.healthit.chpl.attestation.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.attestation.domain.AttestationCategory;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationResponse;
import gov.healthit.chpl.attestation.entity.AttestationAnswerEntity;
import gov.healthit.chpl.attestation.entity.AttestationCategoryEntity;
import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.attestation.entity.AttestationQuestionEntity;
import gov.healthit.chpl.attestation.entity.DeveloperAttestationEntity;
import gov.healthit.chpl.attestation.entity.DeveloperAttestationResponseEntity;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestation;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.developer.DeveloperEntity;

@Repository
public class AttestationDAO extends BaseDAOImpl{

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

    public void create(ChangeRequestAttestation attestation, Long developerId) {
        DeveloperAttestationEntity entity = DeveloperAttestationEntity.builder()
                .developer(DeveloperEntity.builder()
                        .id(developerId)
                        .build())
                .period(AttestationPeriodEntity.builder()
                        .id(attestation.getAttestationPeriod().getId())
                        .build())
                .responses(attestation.getResponses().stream()
                        .map(resp -> getDeveloperAttestationResponseEntity(resp, -1L))
                        .collect(Collectors.toSet()))
                .build();

        create(entity);

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
}
