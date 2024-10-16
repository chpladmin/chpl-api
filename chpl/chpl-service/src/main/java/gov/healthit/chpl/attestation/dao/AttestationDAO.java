package gov.healthit.chpl.attestation.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.entity.AttestationPeriodDeveloperExceptionEntity;
import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.attestation.entity.AttestationSubmissionEntity;
import gov.healthit.chpl.attestation.entity.AttestationSubmissionResponseEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.AllowedResponse;
import gov.healthit.chpl.form.FormItem;
import gov.healthit.chpl.form.entity.AllowedResponseEntity;
import gov.healthit.chpl.form.entity.FormItemEntity;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class AttestationDAO extends BaseDAOImpl{

    public List<AttestationPeriod> getAllPeriods() {
        return getAllPeriodEntities().stream()
                .map(ent -> new AttestationPeriod(ent))
                .collect(Collectors.toList());
    }

    public List<AttestationSubmission> getAttestationSubmissionsByDeveloper(Long developerId) {
        return getAttestationSubmissionEntitiesByDeveloper(developerId).stream()
                .map(ent -> ent.toDomain())
                .collect(Collectors.toList());
    }

    public List<AttestationSubmission> getAttestationSubmissionsByDeveloperAndPeriod(Long developerId, Long periodId) {
        return getAttestationSubmissionEntitiesByDeveloperAndPeriod(developerId, periodId).stream()
                .map(ent -> ent.toDomain())
                .collect(Collectors.toList());
    }

    public List<AttestationSubmissionResponseEntity> getAttestationSubmissionResponseEntities(Long attestationSubmissionId) {
        String hql = "SELECT DISTINCT asre "
                + "FROM AttestationSubmissionResponseEntity asre "
                + "JOIN FETCH asre.response resp "
                + "JOIN FETCH asre.formItem fi "
                + "WHERE (NOT asre.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (NOT fi.deleted = true) "
                + "AND (asre.attestationSubmissionId = :attestationSubmissionId) ";

        List<AttestationSubmissionResponseEntity> result = entityManager
                .createQuery(hql, AttestationSubmissionResponseEntity.class)
                .setParameter("attestationSubmissionId", attestationSubmissionId)
                .getResultList();

        return result;
    }

    public AttestationSubmission saveAttestationSubmssion(Long developerId, AttestationSubmission attestationSubmission) throws EntityRetrievalException {
        Optional<AttestationSubmissionEntity> entity = getAttestationSubmissionEntitiesByDeveloper(developerId).stream()
                .filter(ent -> ent.getAttestationPeriod().getId().equals(attestationSubmission.getAttestationPeriod().getId()))
                .findAny();

        if (entity.isPresent()) {
            deleteAttestationSubmission(entity.get().getId());
        }
        return createAttestationSubmission(developerId, attestationSubmission);
    }

    private void deleteAttestationSubmission(Long attestationSubmissionId) throws EntityRetrievalException {
        AttestationSubmissionEntity entity = getAttestationSubmissionEntity(attestationSubmissionId);

        entity.setDeleted(true);
        entity.getResponses().forEach(resp -> resp.setDeleted(true));
        update(entity);
    }

    private AttestationSubmission createAttestationSubmission(Long developerId, AttestationSubmission attestationSubmission) throws EntityRetrievalException {
        AttestationSubmissionEntity parent = AttestationSubmissionEntity.builder()
                .developerId(developerId)
                .attestationPeriod(getAllPeriodEntities().stream()
                        .filter(ent -> ent.getId().equals(attestationSubmission.getAttestationPeriod().getId()))
                        .findAny()
                        .get())
                .signature(attestationSubmission.getSignature())
                .signatureEmail(attestationSubmission.getSignatureEmail())
                .deleted(false)
                .build();

        create(parent);

        for (FormItem fi : attestationSubmission.getForm().extractFlatFormItems()) {
            if (!CollectionUtils.isEmpty(fi.getSubmittedResponses())) {
                for (AllowedResponse resp : fi.getSubmittedResponses()) {
                    AttestationSubmissionResponseEntity entity = AttestationSubmissionResponseEntity.builder()
                            .attestationSubmissionId(parent.getId())
                            .formItem(FormItemEntity.builder()
                                    .id(fi.getId())
                                    .build())
                            .response(AllowedResponseEntity.builder()
                                    .id(resp.getId())
                                    .build())
                            .deleted(false)
                            .build();
                    create(entity);
                }
            }
        }

        return getAttestationSubmissionEntity(parent.getId()).toDomain();
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
                .deleted(false)
                .build();

        create(entity);

        return new AttestationPeriodDeveloperException(getAttestationPeriodDeveloperExceptionEntity(entity.getId()));
    }

    public AttestationPeriodDeveloperException getCurrentAttestationPeriodDeveloperException(Long developerId) {
        AttestationPeriodDeveloperExceptionEntity entity = getCurrentAttestationPeriodDeveloperExceptionEntity(developerId);
        if (entity != null) {
            return new AttestationPeriodDeveloperException(entity);
        } else {
            return null;
        }
    }

    public List<AttestationPeriodEntity> getAllPeriodEntities() {
        List<AttestationPeriodEntity> result = entityManager.createQuery(
                "FROM AttestationPeriodEntity ape "
                + "LEFT JOIN FETCH ape.form "
                + "WHERE (NOT ape.deleted = true)",
                AttestationPeriodEntity.class).getResultList();
        return result;
    }

    private List<AttestationSubmissionEntity> getAttestationSubmissionEntitiesByDeveloper(Long developerId) {
        String hql = "SELECT DISTINCT ase "
                + "FROM AttestationSubmissionEntity ase "
                + "JOIN FETCH ase.attestationPeriod per "
                + "LEFT JOIN FETCH per.form "
                + "LEFT JOIN FETCH ase.responses resp "
                + "LEFT JOIN FETCH resp.formItem fi "
                + "LEFT JOIN FETCH fi.question "
                + "LEFT JOIN FETCH resp.response "
                + "WHERE (NOT ase.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (NOT per.deleted = true) "
                + "AND (ase.developerId = :developerId) ";

        List<AttestationSubmissionEntity> result = entityManager
                .createQuery(hql, AttestationSubmissionEntity.class)
                .setParameter("developerId", developerId)
                .getResultList();

        return result;
    }

    private List<AttestationSubmissionEntity> getAttestationSubmissionEntitiesByDeveloperAndPeriod(Long developerId, Long periodId) {
        String hql = "SELECT DISTINCT ase "
                + "FROM AttestationSubmissionEntity ase "
                + "JOIN FETCH ase.attestationPeriod per "
                + "LEFT JOIN FETCH per.form "
                + "LEFT JOIN FETCH ase.responses resp "
                + "LEFT JOIN FETCH resp.formItem fi "
                + "LEFT JOIN FETCH fi.question "
                + "LEFT JOIN FETCH resp.response "
                + "WHERE (NOT ase.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (NOT per.deleted = true) "
                + "AND (ase.developerId = :developerId) "
                + "AND (per.id = :periodId)";

        List<AttestationSubmissionEntity> result = entityManager
                .createQuery(hql, AttestationSubmissionEntity.class)
                .setParameter("developerId", developerId)
                .setParameter("periodId", periodId)
                .getResultList();

        return result;
    }

    private AttestationSubmissionEntity getAttestationSubmissionEntity(Long attestationSubmissionId) throws EntityRetrievalException {
        String hql = "SELECT DISTINCT ase "
                + "FROM AttestationSubmissionEntity ase "
                + "JOIN FETCH ase.attestationPeriod per "
                + "LEFT JOIN FETCH per.form "
                + "LEFT JOIN FETCH ase.responses resp "
                + "LEFT JOIN FETCH resp.formItem fi "
                + "LEFT JOIN FETCH fi.question "
                + "LEFT JOIN FETCH resp.response "
                + "WHERE (NOT ase.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (NOT per.deleted = true) "
                + "AND (ase.id = :attestationSubmissionId) ";

        List<AttestationSubmissionEntity> result = entityManager
                .createQuery(hql, AttestationSubmissionEntity.class)
                .setParameter("attestationSubmissionId", attestationSubmissionId)
                .getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(
                    "Data error. Attestation Submission not found in database.");
        } else if (result.size() > 1) {
            throw new EntityRetrievalException(
                "Data error. Duplicate Attestation Submission in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
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

    private AttestationPeriodDeveloperExceptionEntity getCurrentAttestationPeriodDeveloperExceptionEntity(Long developerId) {
        String hql = "SELECT apde "
                + "FROM AttestationPeriodDeveloperExceptionEntity apde "
                + "JOIN FETCH apde.developer d "
                + "JOIN FETCH apde.period p "
                + "WHERE d.id = :developerId "
                + "AND apde.deleted = false ";

        List<AttestationPeriodDeveloperExceptionEntity> result = entityManager
                .createQuery(hql, AttestationPeriodDeveloperExceptionEntity.class)
                .setParameter("developerId", developerId)
                .getResultList();

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}
