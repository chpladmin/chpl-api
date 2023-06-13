package gov.healthit.chpl.changerequest.dao;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationSubmissionEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationSubmissionResponseEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.AllowedResponse;
import gov.healthit.chpl.form.FormItem;
import gov.healthit.chpl.form.entity.AllowedResponseEntity;
import gov.healthit.chpl.form.entity.FormItemEntity;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ChangeRequestAttestationDAO extends BaseDAOImpl{

    public ChangeRequestAttestationSubmission create(ChangeRequest cr, ChangeRequestAttestationSubmission changeRequestAttestationSubmission) throws EntityRetrievalException {
        ChangeRequestAttestationSubmissionEntity parent = ChangeRequestAttestationSubmissionEntity.builder()
                .changeRequest(getSession().load(ChangeRequestEntity.class, cr.getId()))
                .attestationPeriod(getAttestationPeriodEntity(changeRequestAttestationSubmission.getAttestationPeriod().getId()))
                .signature(changeRequestAttestationSubmission.getSignature())
                .signatureEmail(changeRequestAttestationSubmission.getSignatureEmail())
                .deleted(false)
                .lastModifiedUser(AuthUtil.getAuditId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .build();

        create(parent);
        return getByChangeRequestId(cr.getId());
    }

    public void addResponsesToChangeRequestAttestationSubmission(ChangeRequestAttestationSubmission changeRequestAttestationSubmission, List<FormItem> formItems) {
        for (FormItem fi : formItems) {
            if (!CollectionUtils.isEmpty(fi.getSubmittedResponses())) {
                for (AllowedResponse resp : fi.getSubmittedResponses()) {
                    ChangeRequestAttestationSubmissionResponseEntity entity = ChangeRequestAttestationSubmissionResponseEntity.builder()
                            .changeRequestAttestationSubmissionId(changeRequestAttestationSubmission.getId())
                            .formItem(FormItemEntity.builder()
                                    .id(fi.getId())
                                    .build())
                            .response(AllowedResponseEntity.builder()
                                    .id(resp.getId())
                                    .build())
                            .responseMessage(resp.getMessage())
                            .creationDate(new Date())
                            .lastModifiedDate(new Date())
                            .lastModifiedUser(AuthUtil.getAuditId())
                            .deleted(false)
                            .build();
                    create(entity);
                }
            }
        }
    }

    public void update(ChangeRequest cr, ChangeRequestAttestationSubmission changeRequestAttestationSubmission) throws EntityRetrievalException {

        ChangeRequestAttestationSubmissionEntity entity = getEntityByChangeRequestId(cr.getId());
        entity.setSignature(changeRequestAttestationSubmission.getSignature());
        entity.setSignatureEmail(changeRequestAttestationSubmission.getSignatureEmail());
        entity.getAttestationPeriod().setId(changeRequestAttestationSubmission.getAttestationPeriod().getId());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);


        List<ChangeRequestAttestationSubmissionResponseEntity> responseEntities = getChangeRequestAttestationSubmissionResponseEntities(entity.getId());

        List<FormItem> items = changeRequestAttestationSubmission.getForm().extractFlatFormItems();
        items.stream()
                .forEach(fi -> {

                    findAddedResponses(fi.getSubmittedResponses(), filterByFormItemId(fi.getId(), responseEntities)).stream()
                            .forEach(resp -> {
                                LOGGER.info("Question: {} Adding response: {}", fi.getQuestion().getId(), resp.getResponse());
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

                    findRemovedResponses(fi.getSubmittedResponses(), filterByFormItemId(fi.getId(), responseEntities)).stream()
                            .forEach(responseEntity -> {
                                LOGGER.info("Question: {} Removing response: {}", fi.getQuestion().getId(), responseEntity.getResponse().getResponse());
                                responseEntity.setDeleted(true);
                                responseEntity.setLastModifiedUser(AuthUtil.getAuditId());
                                update(responseEntity);
                            });
                });
    }

    private List<ChangeRequestAttestationSubmissionResponseEntity> filterByFormItemId(Long formItemId, List<ChangeRequestAttestationSubmissionResponseEntity> responseEntities) {
        return responseEntities.stream()
                .filter(ent -> ent.getFormItem().getId().equals(formItemId))
                .toList();
    }

    private List<AllowedResponse> findAddedResponses(List<AllowedResponse> submittedResponses, List<ChangeRequestAttestationSubmissionResponseEntity> existingResponses) {
        return submittedResponses.stream()
                .filter(sr -> !existingResponses.stream()
                        .filter(er -> er.getResponse().getId().equals(sr.getId()))
                        .findAny()
                        .isPresent())
                .toList();
    }

    private List<ChangeRequestAttestationSubmissionResponseEntity> findRemovedResponses(List<AllowedResponse> submittedResponses, List<ChangeRequestAttestationSubmissionResponseEntity> existingResponses) {
        return existingResponses.stream()
                .filter(er -> !submittedResponses.stream()
                        .filter(sr -> sr.getId().equals(er.getResponse().getId()))
                        .findAny()
                        .isPresent())
                .toList();
    }

    public ChangeRequestAttestationSubmission getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return getEntityByChangeRequestId(changeRequestId).toDomain();
    }

    public Long getIdOfMostRecentAttestationChangeRequest(Long developerId, Long attestationPeriodId) {
        String hql = "SELECT DISTINCT crase "
                + "FROM ChangeRequestAttestationSubmissionEntity crase "
                + "JOIN FETCH crase.changeRequest cr "
                + "JOIN FETCH crase.attestationPeriod per "
                + "LEFT JOIN FETCH per.form "
                + "WHERE (NOT crase.deleted = true) "
                + "AND (NOT cr.deleted = true) "
                + "AND (NOT per.deleted = true) "
                + "AND (cr.developer.id = :developerId) "
                + "AND (per.id = :attestationPeriodId) "
                + "ORDER BY cr.creationDate DESC ";

        List<ChangeRequestAttestationSubmissionEntity> result = entityManager
                .createQuery(hql, ChangeRequestAttestationSubmissionEntity.class)
                .setParameter("developerId", developerId)
                .setParameter("attestationPeriodId", attestationPeriodId)
                .setMaxResults(1)
                .getResultList();

        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return result.get(0).getChangeRequest().getId();
    }


    public List<ChangeRequestAttestationSubmissionResponseEntity> getChangeRequestAttestationSubmissionResponseEntities(Long changeRequestAttestationSubmissionId) {
        String hql = "SELECT DISTINCT crasre "
                + "FROM ChangeRequestAttestationSubmissionResponseEntity crasre "
                + "JOIN FETCH crasre.response resp "
                + "JOIN FETCH crasre.formItem fi "
                + "WHERE (NOT crasre.deleted = true) "
                + "AND (NOT resp.deleted = true) "
                + "AND (NOT fi.deleted = true) "
                + "AND (crasre.changeRequestAttestationSubmissionId = :changeRequestAttestationSubmissionId) ";

        List<ChangeRequestAttestationSubmissionResponseEntity> result = entityManager
                .createQuery(hql, ChangeRequestAttestationSubmissionResponseEntity.class)
                .setParameter("changeRequestAttestationSubmissionId", changeRequestAttestationSubmissionId)
                .getResultList();

        return result;
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
                + "JOIN FETCH crase.attestationPeriod per "
                + "LEFT JOIN FETCH per.form "
                + "WHERE (NOT crase.deleted = true) "
                + "AND (NOT per.deleted = true) "
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
}
