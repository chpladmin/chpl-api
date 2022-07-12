package gov.healthit.chpl.attestation.dao;

import java.util.Date;
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
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationSubmissionResponseEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.AllowedResponse;
import gov.healthit.chpl.form.FormItem;
import gov.healthit.chpl.form.entity.AllowedResponseEntity;
import gov.healthit.chpl.form.entity.FormItemEntity;
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

    public List<AttestationSubmission> getDeveloperAttestationSubmissionsByDeveloper(Long developerId) {
        return getAttestationSubmissionEntitiesByDeveloper(developerId).stream()
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

        if (entity.isEmpty()) {
            return createAttestationSubmission(developerId, attestationSubmission);
        } else {
            return updateAttestationSubmission(attestationSubmission);
        }
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
                .lastModifiedUser(AuthUtil.getAuditId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
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
                            .creationDate(new Date())
                            .lastModifiedDate(new Date())
                            .lastModifiedUser(AuthUtil.getAuditId())
                            .deleted(false)
                            .build();
                    create(entity);
                }
            }
        }

        return getAttestationSubmissionEntity(parent.getId()).toDomain();
    }

    private AttestationSubmission updateAttestationSubmission(AttestationSubmission attestationSubmission) throws EntityRetrievalException {
        AttestationSubmissionEntity entity = getAttestationSubmissionEntity(attestationSubmission.getId());
        entity.setSignature(attestationSubmission.getSignature());
        entity.setSignatureEmail(attestationSubmission.getSignatureEmail());
        entity.getAttestationPeriod().setId(attestationSubmission.getAttestationPeriod().getId());
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        List<FormItem> items = attestationSubmission.getForm().extractFlatFormItems();
        items.stream()
                .forEach(fi -> {
                    findAddedResponses(fi.getSubmittedResponses(), entity.getResponses()).stream()
                            .forEach(resp -> {
                                LOGGER.info("Adding response: " + resp.getId());
                                create(ChangeRequestAttestationSubmissionResponseEntity.builder()
                                        .changeRequestAttestationSubmissionId(entity.getId())
                                        .formItem(FormItemEntity.builder()
                                                .id(fi.getId())
                                                .build())
                                        .response(AllowedResponseEntity.builder()
                                            .id(resp.getId())
                                            .build())
                                        .creationDate(new Date())
                                        .lastModifiedDate(new Date())
                                        .lastModifiedUser(AuthUtil.getAuditId())
                                        .deleted(false)
                                        .build());
                            });

                    findRemovedResponses(fi.getSubmittedResponses(), filterByFormItemId(fi.getId(), entity.getResponses())).stream()
                            .forEach(responseEntity -> {
                                LOGGER.info("Removing response: " + responseEntity.getId());
                                responseEntity.setDeleted(true);
                                responseEntity.setLastModifiedUser(AuthUtil.getAuditId());
                                update(responseEntity);
                            });
                });

        update(entity);

        return getAttestationSubmissionEntity(entity.getId()).toDomain();
    }

    private List<AttestationSubmissionResponseEntity> filterByFormItemId(Long formItemId, List<AttestationSubmissionResponseEntity> responseEntities) {
        return responseEntities.stream()
                .filter(ent -> ent.getFormItem().getId().equals(formItemId))
                .toList();
    }

    private List<AllowedResponse> findAddedResponses(List<AllowedResponse> submittedResponses, List<AttestationSubmissionResponseEntity> existingResponses) {
        return submittedResponses.stream()
                .filter(sr -> !existingResponses.stream()
                        .filter(er -> er.getResponse().getId().equals(sr.getId()))
                        .findAny()
                        .isPresent())
                .toList();
    }

    private List<AttestationSubmissionResponseEntity> findRemovedResponses(List<AllowedResponse> submittedResponses, List<AttestationSubmissionResponseEntity> existingResponses) {
        return existingResponses.stream()
                .filter(er -> !submittedResponses.stream()
                        .filter(sr -> sr.getId().equals(er.getResponse().getId()))
                        .findAny()
                        .isPresent())
                .toList();
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
                + "AND (NOT per.deleted = true) "
                + "AND (ase.developerId = :developerId) ";

        List<AttestationSubmissionEntity> result = entityManager
                .createQuery(hql, AttestationSubmissionEntity.class)
                .setParameter("developerId", developerId)
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
                "Data error. Duplicate Attestation 4Submission in database.");
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
