package gov.healthit.chpl.attestation.dao;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.attestation.domain.Attestation;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.attestation.domain.AttestationSubmittedResponse;
import gov.healthit.chpl.attestation.domain.DeveloperAttestationSubmission;
import gov.healthit.chpl.attestation.entity.AttestationEntity;
import gov.healthit.chpl.attestation.entity.AttestationPeriodDeveloperExceptionEntity;
import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.attestation.entity.AttestationValidResponseEntity;
import gov.healthit.chpl.attestation.entity.DeveloperAttestationResponseEntity;
import gov.healthit.chpl.attestation.entity.DeveloperAttestationSubmissionEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class AttestationDAO extends BaseDAOImpl{

    public List<AttestationPeriod> getAllPeriods() {
        return getAllPeriodEntities().stream()
                .map(ent -> new AttestationPeriod(ent))
                .collect(Collectors.toList());
    }

    public List<Attestation> getAttestationForm() {
        return getAttestationFormEntities().stream()
                .map(ent -> new Attestation(ent))
                .collect(Collectors.toList());
    }

    public DeveloperAttestationSubmission getDeveloperAttestationSubmission(Long developerAttestationSubmissionId) throws EntityRetrievalException {
        return new DeveloperAttestationSubmission(getDeveloperAttestationSubmissionEntity(developerAttestationSubmissionId));
    }

    public List<DeveloperAttestationSubmission> getDeveloperAttestationSubmissionsByDeveloper(Long developerId) {
        return getDeveloperAttestationSubmissionEntitiesByDeveloper(developerId).stream()
                .map(ent -> new DeveloperAttestationSubmission(ent))
                .collect(Collectors.toList());
    }

    public List<DeveloperAttestationSubmission> getDeveloperAttestationSubmissionsByDeveloperAndPeriod(Long developerId, Long periodId) {
        return getDeveloperAttestationSubmissionEntitiesByDeveloperAndPeriod(developerId, periodId).stream()
                .map(ent -> new DeveloperAttestationSubmission(ent))
                .collect(Collectors.toList());
    }

    public DeveloperAttestationSubmission createDeveloperAttestationSubmission(DeveloperAttestationSubmission developerAttestationSubmission) throws EntityRetrievalException {
        DeveloperAttestationSubmissionEntity entity = DeveloperAttestationSubmissionEntity.builder()
                .developer(DeveloperEntity.builder()
                        .id(developerAttestationSubmission.getDeveloper().getDeveloperId())
                        .build())
                .period(AttestationPeriodEntity.builder()
                        .id(developerAttestationSubmission.getPeriod().getId())
                        .build())
                .signature(developerAttestationSubmission.getSignature())
                .signatureEmail(developerAttestationSubmission.getSignatureEmail())
                .deleted(false)
                .lastModifiedUser(AuthUtil.getAuditId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .build();

        create(entity);

        developerAttestationSubmission.getResponses().stream()
                .forEach(resp -> createDeveloperAttestationResponse(resp, entity.getId()));

        return new DeveloperAttestationSubmission(getDeveloperAttestationSubmissionEntity(entity.getId()));
    }

    public void deleteDeveloperAttestationSubmission(Long developerAttestationId) throws EntityRetrievalException {
        DeveloperAttestationSubmissionEntity attestation = getDeveloperAttestationSubmissionEntity(developerAttestationId);

        attestation.setDeleted(true);
        update(attestation);

        attestation.getResponses().stream()
                .forEach(resp -> {
                    resp.setDeleted(true);
                    update(resp);
                });
    }

    public List<AttestationPeriodDeveloperException> getAttestationPeriodDeveloperExceptions(Long developerId, Long attestationPeriodId) {
        return getAttestationPeriodDeveloperExceptionEntities(developerId, attestationPeriodId).stream()
                .map(entity -> new AttestationPeriodDeveloperException(entity))
                .collect(Collectors.toList());
    }

    public void deleteAttestationPeriodDeveloperExceptions(Long developerId, Long attestationPeriodId) {
        getAttestationPeriodDeveloperExceptionEntities(developerId, attestationPeriodId).stream()
                .forEach(entity -> {
                    entity.setDeleted(true);
                    entity.setLastModifiedUser(AuthUtil.getAuditId());
                    update(entity);
                });
    }

    public AttestationPeriodDeveloperException createAttestationPeriodDeveloperException(AttestationPeriodDeveloperException adpe) throws EntityRetrievalException {
        AttestationPeriodDeveloperExceptionEntity entity = AttestationPeriodDeveloperExceptionEntity.builder()
                .developer(DeveloperEntity.builder()
                        .id(adpe.getDeveloper().getId())
                        .build())
                .period(AttestationPeriodEntity.builder()
                        .id(adpe.getPeriod().getId())
                        .build())
                .exceptionEnd(adpe.getExceptionEnd())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        create(entity);

        return new AttestationPeriodDeveloperException(getAttestationPeriodDeveloperExceptionEntity(entity.getId()));
    }

    private DeveloperAttestationResponseEntity createDeveloperAttestationResponse(AttestationSubmittedResponse response, Long developerAttestationId) {
        try {
            DeveloperAttestationResponseEntity entity = DeveloperAttestationResponseEntity.builder()
                    .developerAttestationSubmissionId(developerAttestationId)
                    .validResponse(getAttestationValidResponseEntity(response.getResponse().getId()))
                    .attestation(getAttestationEntity(response.getAttestation().getId()))
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

    private DeveloperAttestationResponseEntity getDeveloperAttestationResponseEntity(AttestationSubmittedResponse response) {
        return DeveloperAttestationResponseEntity.builder()
                .validResponse(AttestationValidResponseEntity.builder()
                        .id(response.getResponse().getId())
                        .build())
                .attestation(AttestationEntity.builder()
                        .id(response.getAttestation().getId())
                        .build())
                .lastModifiedUser(AuthUtil.getAuditId())
                .build();
    }

    public List<AttestationPeriodEntity> getAllPeriodEntities() {
        List<AttestationPeriodEntity> result = entityManager.createQuery(
                "FROM AttestationPeriodEntity ape "
                + "WHERE (NOT ape.deleted = true)",
                AttestationPeriodEntity.class).getResultList();
        return result;
    }


    private List<AttestationEntity> getAttestationFormEntities() {
        List<AttestationEntity> result = entityManager.createQuery(
                "SELECT DISTINCT ae "
                + "FROM AttestationEntity ae "
                + "JOIN FETCH ae.condition c "
                + "JOIN FETCH ae.validResponses vr "
                + "WHERE (NOT ae.deleted = true) "
                + "AND (NOT c.deleted = true )"
                + "AND (NOT vr.deleted = true) ",
                AttestationEntity.class)
                .getResultList();
        return result;
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
                    "Data error. Duplicate Question in database.");
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
                    "Data error. Attestation Valid Response not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate Attestation Valid Response in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private DeveloperAttestationSubmissionEntity getDeveloperAttestationSubmissionEntity(Long developerAttestationSubmissionId) throws EntityRetrievalException {
        String hql = "SELECT DISTINCT dase "
                + "FROM DeveloperAttestationSubmissionEntity dase "
                + "JOIN FETCH dase.developer d "
                + "JOIN FETCH dase.responses resp "
                + "JOIN FETCH dase.period per "
                + "WHERE (NOT dase.deleted = true) "
                + "AND (NOT d.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (NOT per.deleted = true) "
                + "AND (dase.id = :developerAttestationSubmissionId) ";

        List<DeveloperAttestationSubmissionEntity> result = entityManager
                .createQuery(hql, DeveloperAttestationSubmissionEntity.class)
                .setParameter("developerAttestationSubmissionId", developerAttestationSubmissionId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Developer submission attestation not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate developer submission attestation in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private List<DeveloperAttestationSubmissionEntity> getDeveloperAttestationSubmissionEntitiesByDeveloperAndPeriod(Long developerId, Long periodId) {
        String hql = "SELECT DISTINCT dase "
                + "FROM DeveloperAttestationSubmissionEntity dase "
                + "JOIN FETCH dase.developer d "
                + "JOIN FETCH dase.responses resp "
                + "JOIN FETCH dase.period per "
                + "WHERE (NOT dase.deleted = true) "
                + "AND (NOT d.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (NOT per.deleted = true) "
                + "AND (d.id = :developerId) "
                + "AND (per.id = :periodId) ";

        List<DeveloperAttestationSubmissionEntity> result = entityManager
                .createQuery(hql, DeveloperAttestationSubmissionEntity.class)
                .setParameter("developerId", developerId)
                .setParameter("periodId", periodId)
                .getResultList();

        return result;
    }

    private List<DeveloperAttestationSubmissionEntity> getDeveloperAttestationSubmissionEntitiesByDeveloper(Long developerId) {
        String hql = "FROM "
                + "JOIN FETCH DeveloperAttestationSubmissionEntity dase "
                + "JOIN FETCH dase.developer d "
                + "JOIN FETCH dase.responses resp "
                + "JOIN FETCH dase.period per "
                + "WHERE (NOT dase.deleted = true) "
                + "AND (NOT d.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (NOT per.deleted = true) "
                + "AND (d.id = :developerId) ";

        List<DeveloperAttestationSubmissionEntity> result = entityManager
                .createQuery(hql, DeveloperAttestationSubmissionEntity.class)
                .setParameter("developerId", developerId)
                .getResultList();

        return result;
    }

    private List<AttestationPeriodDeveloperExceptionEntity> getAttestationPeriodDeveloperExceptionEntities(Long developerId, Long attestationPeriodId) {
        String hql = "SELECT apde "
                + "FROM AttestationPeriodDeveloperExceptionEntity apde "
                + "JOIN FETCH apde.developer d "
                + "JOIN FETCH apde.period p "
                + "WHERE d.id = :developerId "
                + "AND p.id = :attestationPeriodId "
                + "AND apde.deleted = false ";

        List<AttestationPeriodDeveloperExceptionEntity> result = entityManager
                .createQuery(hql, AttestationPeriodDeveloperExceptionEntity.class)
                .setParameter("developerId", developerId)
                .setParameter("attestationPeriodId", attestationPeriodId)
                .getResultList();

        return result;
    }

    private AttestationPeriodDeveloperExceptionEntity getAttestationPeriodDeveloperExceptionEntity(Long id) throws EntityRetrievalException {
        String hql = "SELECT apde "
                + "FROM AttestationPeriodDeveloperExceptionEntity apde "
                + "JOIN FETCH apde.developer d "
                + "JOIN FETCH apde.period p "
                + "WHERE apde.id = :id "
                + "AND apde.deleted = false ";

        List<AttestationPeriodDeveloperExceptionEntity> result = entityManager
                .createQuery(hql, AttestationPeriodDeveloperExceptionEntity.class)
                .setParameter("id", id)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Attestation Period Developer Exception not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                    "Data error. Duplicate Attestation Period Developer Exception in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);

    }

}
