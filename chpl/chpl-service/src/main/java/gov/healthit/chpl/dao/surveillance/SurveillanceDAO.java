package gov.healthit.chpl.dao.surveillance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.surveillance.RequirementDetailType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.entity.surveillance.NonconformityTypeEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceRequirementEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceValidationEntity;
import gov.healthit.chpl.entity.surveillance.RequirementDetailTypeEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementTypeEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceResultTypeEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Repository("surveillanceDAO")
@Log4j2
public class SurveillanceDAO extends BaseDAOImpl {
    private static String SURVEILLANCE_FULL_HQL =
            "SELECT DISTINCT surv "
            + "FROM SurveillanceEntity surv "
            + "JOIN FETCH surv.certifiedProduct "
            + "JOIN FETCH surv.surveillanceType "
            + "LEFT OUTER JOIN FETCH surv.surveilledRequirements reqs "
            + "LEFT OUTER JOIN FETCH reqs.requirementDetailType "
            + "LEFT OUTER JOIN FETCH reqs.surveillanceResultTypeEntity "
            + "LEFT OUTER JOIN FETCH reqs.nonconformities ncs "
            + "LEFT OUTER JOIN FETCH ncs.type nct "
            //+ "LEFT JOIN FETCH nct.certificationEdition "
            + "LEFT OUTER JOIN FETCH ncs.documents docs "
            + "WHERE surv.deleted <> true ";

    private static String PENDING_SURVEILLANCE_FULL_HQL = "SELECT DISTINCT surv "
            + "FROM PendingSurveillanceEntity surv "
            + "JOIN FETCH surv.certifiedProduct "
            + "LEFT OUTER JOIN FETCH surv.surveilledRequirements reqs "
            + "LEFT OUTER JOIN FETCH reqs.certificationCriterionEntity cce "
            + "LEFT OUTER JOIN FETCH cce.certificationEdition "
            + "LEFT OUTER JOIN FETCH reqs.nonconformities ncs "
            + "LEFT OUTER JOIN FETCH ncs.certificationCriterionEntity cce2 "
            + "LEFT OUTER JOIN FETCH cce2.certificationEdition "
            + "LEFT OUTER JOIN FETCH surv.validation ";

    public Long insertSurveillance(Surveillance surv) throws UserPermissionRetrievalException {
        SurveillanceEntity toInsert = new SurveillanceEntity();
        populateSurveillanceEntity(toInsert, surv);
        toInsert.setLastModifiedUser(AuthUtil.getAuditId());
        toInsert.setDeleted(false);
        entityManager.persist(toInsert);
        entityManager.flush();

        surv.getRequirements().forEach(req -> {
            SurveillanceRequirementEntity newRequirement = insertSurveillanceRequirement(req, toInsert.getId());
            req.getNonconformities().forEach(nc -> {
                insertSurveillanceNonconformity(nc, newRequirement.getId());
            });
        });
        return toInsert.getId();
    }

    private SurveillanceNonconformityEntity insertSurveillanceNonconformity(SurveillanceNonconformity nonconformity, Long surveillanceRequirementId) {
        SurveillanceNonconformityEntity toInsertNc  = SurveillanceNonconformityEntity.builder()
                .surveillanceRequirementId(surveillanceRequirementId)
                .type(NonconformityTypeEntity.builder()
                        .id(nonconformity.getType().getId())
                        .number(nonconformity.getType().getNumber())
                        .title(nonconformity.getType().getTitle())
                        .removed(nonconformity.getType().getRemoved())
                        .build())
                .capApproval(nonconformity.getCapApprovalDay())
                .capEndDate(nonconformity.getCapEndDay())
                .capMustCompleteDate(nonconformity.getCapMustCompleteDay())
                .capStart(nonconformity.getCapStartDay())
                .dateOfDetermination(nonconformity.getDateOfDeterminationDay())
                .developerExplanation(nonconformity.getDeveloperExplanation())
                .findings(nonconformity.getFindings())
                .resolution(nonconformity.getResolution())
                .sitesPassed(nonconformity.getSitesPassed())
                .nonconformityCloseDate(nonconformity.getNonconformityCloseDay())
                .summary(nonconformity.getSummary())
                .totalSites(nonconformity.getTotalSites())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        entityManager.persist(toInsertNc);
        entityManager.flush();

        return toInsertNc;
    }

    private SurveillanceNonconformityEntity updateSurveillanceNonconformity(SurveillanceNonconformity nonconformity, SurveillanceNonconformityEntity nonconformityEntity) {
        nonconformityEntity.getType().setId(nonconformity.getType().getId());
        nonconformityEntity.setCapApproval(nonconformity.getCapApprovalDay());
        nonconformityEntity.setCapEndDate(nonconformity.getCapEndDay());
        nonconformityEntity.setCapMustCompleteDate(nonconformity.getCapMustCompleteDay());
        nonconformityEntity.setCapStart(nonconformity.getCapStartDay());
        nonconformityEntity.setDateOfDetermination(nonconformity.getDateOfDeterminationDay());
        nonconformityEntity.setDeveloperExplanation(nonconformity.getDeveloperExplanation());
        nonconformityEntity.setFindings(nonconformity.getFindings());
        nonconformityEntity.setResolution(nonconformity.getResolution());
        nonconformityEntity.setSitesPassed(nonconformity.getSitesPassed());
        nonconformityEntity.setNonconformityCloseDate(nonconformity.getNonconformityCloseDay());
        nonconformityEntity.setSummary(nonconformity.getSummary());
        nonconformityEntity.setTotalSites(nonconformity.getTotalSites());
        nonconformityEntity.setLastModifiedUser(AuthUtil.getAuditId());

        entityManager.merge(nonconformityEntity);
        entityManager.flush();

        //Add documents
        nonconformity.getDocuments().stream()
                .filter(doc -> !isIdInDocumentEntities(doc.getId(), nonconformityEntity.getDocuments()))
                .forEach(nc -> insertNonconformityDocument(nonconformityEntity.getId(), nc));
        //Delete documents
        nonconformityEntity.getDocuments().stream()
                .filter(doc -> !isIdInDocuments(doc.getId(), nonconformity.getDocuments()))
                .forEach(doc -> deleteNonconformityDocument(doc.getId()));

        return nonconformityEntity;
    }

    private SurveillanceNonconformityEntity deleteSurveillanceNonconformity(SurveillanceNonconformityEntity nonconformityEntity) {
        nonconformityEntity.setDeleted(true);
        nonconformityEntity.setLastModifiedUser(AuthUtil.getAuditId());

        entityManager.merge(nonconformityEntity);
        entityManager.flush();

        nonconformityEntity.getDocuments().forEach(doc -> deleteNonconformityDocument(doc.getId()));
        return nonconformityEntity;
    }

    private SurveillanceRequirementEntity insertSurveillanceRequirement(SurveillanceRequirement requirement, Long surveillanceId) {
        SurveillanceRequirementEntity toInsertReq = SurveillanceRequirementEntity.builder()
                .surveillanceId(surveillanceId)
                .surveillanceResultTypeEntity(SurveillanceResultTypeEntity.builder()
                        .id(requirement.getResult().getId())
                        .build())
                .requirementDetailType(RequirementDetailTypeEntity.builder()
                        .id(requirement.getRequirementDetailType().getId())
                        .build())
                .requirementDetailOther(requirement.getRequirementDetailOther())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        entityManager.persist(toInsertReq);
        entityManager.flush();

        requirement.getNonconformities().forEach(nc -> insertSurveillanceNonconformity(nc, toInsertReq.getId()));

        return toInsertReq;
    }

    private SurveillanceRequirementEntity updateSurveillanceRequirement(SurveillanceRequirement requirement, SurveillanceRequirementEntity requirementEntity) {
        requirementEntity.getSurveillanceResultTypeEntity().setId(requirement.getResult().getId());
        requirementEntity.getRequirementDetailType().setId(requirement.getRequirementDetailType().getId());
        requirementEntity.setRequirementDetailOther(requirement.getRequirementDetailOther());
        requirementEntity.setLastModifiedUser(AuthUtil.getAuditId());

        entityManager.merge(requirementEntity);
        entityManager.flush();

        //Add Nonconformity
        requirement.getNonconformities().stream()
                .filter(nc -> !isIdInSurveillanceNonconformityEntities(nc.getId(), requirementEntity.getNonconformities()))
                .forEach(nc -> insertSurveillanceNonconformity(nc, requirementEntity.getId()));
        //Delete Nonconformity
        requirementEntity.getNonconformities().stream()
                .filter(entity -> !isIdInSurveillanceNonconformities(entity.getId(), requirement.getNonconformities()))
                .forEach(nc -> deleteSurveillanceNonconformity(nc));
        //Update Nonconformity
        requirementEntity.getNonconformities().stream()
                .filter(entity -> isIdInSurveillanceNonconformities(entity.getId(), requirement.getNonconformities()))
                .forEach(nc -> updateSurveillanceNonconformity(getSurveillanceNonconformity(nc.getId(), requirement.getNonconformities()), nc));

        return requirementEntity;
    }

    private SurveillanceRequirementEntity deleteSurveillanceRequirement(SurveillanceRequirementEntity requirementEntity) {
        requirementEntity.setDeleted(true);
        requirementEntity.setLastModifiedUser(AuthUtil.getAuditId());

        entityManager.merge(requirementEntity);
        entityManager.flush();

        requirementEntity.getNonconformities().forEach(nc -> deleteSurveillanceNonconformity(nc));

        return requirementEntity;
    }

    @Deprecated
    public Long insertNonconformityDocument(Long nonconformityId, SurveillanceNonconformityDocument doc) {
        try {
            SurveillanceNonconformityEntity nc = entityManager.find(SurveillanceNonconformityEntity.class, nonconformityId);
            if (nc == null) {
                String msg = msgUtil.getMessage("surveillance.nonconformity.notFound");
                throw new EntityRetrievalException(msg);
            }
            SurveillanceNonconformityDocumentationEntity docEntity = new SurveillanceNonconformityDocumentationEntity();
            docEntity.setNonconformityId(nonconformityId);
            docEntity.setFileData(doc.getFileContents());
            docEntity.setFileType(doc.getFileType());
            docEntity.setFileName(doc.getFileName());
            docEntity.setDeleted(false);
            docEntity.setLastModifiedUser(AuthUtil.getAuditId());

            entityManager.persist(docEntity);
            entityManager.flush();

            return docEntity.getId();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error adding Document to Nonconformity: {}", nonconformityId);
            return null;
        }
    }

    private SurveillanceNonconformity getSurveillanceNonconformity(Long id, List<SurveillanceNonconformity> nonconformities) {
        return nonconformities.stream()
                .filter(nc -> nc.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    private SurveillanceRequirement getSurveillanceRequirement(Long id, Set<SurveillanceRequirement> requirements) {
        return requirements.stream()
                .filter(nc -> nc.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    private Boolean isIdInSurveillanceRequirements(Long id, Set<SurveillanceRequirement> requirements) {
        return requirements.stream()
                .filter(req -> req.getId().equals(id))
                .findAny()
                .isPresent();
    }

    private Boolean isIdInSurveillanceRequirementEntities(Long id, Set<SurveillanceRequirementEntity> requirements) {
        return requirements.stream()
                .filter(req -> req.getId().equals(id))
                .findAny()
                .isPresent();
    }

    private Boolean isIdInSurveillanceNonconformities(Long id, List<SurveillanceNonconformity> nonconformities) {
        return nonconformities.stream()
                .filter(nc -> nc.getId().equals(id))
                .findAny()
                .isPresent();
    }

    private Boolean isIdInSurveillanceNonconformityEntities(Long id, Set<SurveillanceNonconformityEntity> nonconformities) {
        return nonconformities.stream()
                .filter(nc -> nc.getId().equals(id))
                .findAny()
                .isPresent();
    }

    private Boolean isIdInDocuments(Long id, List<SurveillanceNonconformityDocument> documents) {
        return documents.stream()
                .filter(nc -> nc.getId().equals(id))
                .findAny()
                .isPresent();
    }

    private Boolean isIdInDocumentEntities(Long id, Set<SurveillanceNonconformityDocumentationEntity> documents) {
        return documents.stream()
                .filter(nc -> nc.getId().equals(id))
                .findAny()
                .isPresent();
    }

    public Long updateSurveillance(Surveillance updatedSurveillance) throws EntityRetrievalException {
        SurveillanceEntity originalSurveillance = fetchSurveillanceById(updatedSurveillance.getId());
        populateSurveillanceEntity(originalSurveillance, updatedSurveillance);
        originalSurveillance.setLastModifiedUser(AuthUtil.getAuditId());
        originalSurveillance.setDeleted(false);
        entityManager.merge(originalSurveillance);
        entityManager.flush();

        // Add requirements
        updatedSurveillance.getRequirements().stream()
                .filter(originalRequirement -> !isIdInSurveillanceRequirementEntities(originalRequirement.getId(), originalSurveillance.getSurveilledRequirements()))
                .forEach(originalRequirement -> insertSurveillanceRequirement(originalRequirement, originalSurveillance.getId()));
        // Delete requirements
        originalSurveillance.getSurveilledRequirements().stream()
                .filter(originalRequirement -> !isIdInSurveillanceRequirements(originalRequirement.getId(), updatedSurveillance.getRequirements()))
                .forEach(originalRequirement -> deleteSurveillanceRequirement(originalRequirement));
        // Update requirements
        originalSurveillance.getSurveilledRequirements().stream()
            .filter(originalRequirement -> isIdInSurveillanceRequirements(originalRequirement.getId(), updatedSurveillance.getRequirements()))
            .forEach(originalRequirement -> updateSurveillanceRequirement(
                    getSurveillanceRequirement(originalRequirement.getId(), updatedSurveillance.getRequirements()), originalRequirement));

        return updatedSurveillance.getId();
    }


    public SurveillanceEntity getSurveillanceByCertifiedProductAndFriendlyId(Long certifiedProductId,
            String survFriendlyId) {
        Query query = entityManager.createQuery(
                "from SurveillanceEntity surv " + "where surv.friendlyId = :friendlyId "
                        + "and surv.certifiedProductId = :cpId " + "and surv.deleted <> true",
                SurveillanceEntity.class);
        query.setParameter("friendlyId", survFriendlyId);
        query.setParameter("cpId", certifiedProductId);
        List<SurveillanceEntity> matches = query.getResultList();

        if (matches != null && matches.size() > 0) {
            return matches.get(0);
        }
        return null;
    }


    public SurveillanceEntity getSurveillanceById(Long id) throws EntityRetrievalException {
        SurveillanceEntity result = fetchSurveillanceById(id);
        return result;
    }

    public SurveillanceEntity getSurveillanceByNonconformityId(Long nonconformityId)
            throws EntityRetrievalException {
        entityManager.clear();
        Query query = entityManager.createQuery(SURVEILLANCE_FULL_HQL
                + "AND ncs.id = :entityid",
                SurveillanceEntity.class);
        query.setParameter("entityid", nonconformityId);

        List<SurveillanceEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            String msg = msgUtil.getMessage("surveillance.notFound");
            throw new EntityRetrievalException(msg);
        } else {
            return results.get(0);
        }
    }

    public SurveillanceEntity getSurveillanceByDocumentId(Long documentId)
            throws EntityRetrievalException {
        entityManager.clear();
        Query query = entityManager.createQuery(SURVEILLANCE_FULL_HQL
                + "AND docs.id = :entityid",
                SurveillanceEntity.class);
        query.setParameter("entityid", documentId);

        List<SurveillanceEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            String msg = msgUtil.getMessage("surveillance.notFound");
            throw new EntityRetrievalException(msg);
        } else {
            return results.get(0);
        }
    }

    @Deprecated
    public SurveillanceNonconformityDocumentationEntity getDocumentById(Long documentId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from SurveillanceNonconformityDocumentationEntity doc " + "where doc.id = :id "
                        + "and doc.deleted <> true",
                        SurveillanceNonconformityDocumentationEntity.class);
        query.setParameter("id", documentId);
        List<SurveillanceNonconformityDocumentationEntity> matches = query.getResultList();

        if (matches != null && matches.size() > 0) {
            return matches.get(0);
        }
        String msg = msgUtil.getMessage("surveillance.document.notFound");
        throw new EntityRetrievalException(msg);
    }

    public List<SurveillanceEntity> getSurveillanceByCertifiedProductId(Long id) {
            entityManager.clear();
            Query query = entityManager.createQuery(SURVEILLANCE_FULL_HQL
                    + "AND surv.certifiedProductId = :cpId",
                    SurveillanceEntity.class);
            query.setParameter("cpId", id);

            List<SurveillanceEntity> results = query.getResultList();
            return results;
    }

    /*
    //TODO - OCD-4029
    public Long insertPendingSurveillance(Surveillance surv) throws UserPermissionRetrievalException {
        PendingSurveillanceEntity toInsert = new PendingSurveillanceEntity();
        toInsert.setSurvFriendlyIdToReplace(surv.getSurveillanceIdToReplace());
        if (surv.getCertifiedProduct() != null) {
            toInsert.setCertifiedProductId(surv.getCertifiedProduct().getId());
            toInsert.setCertifiedProductUniqueId(surv.getCertifiedProduct().getChplProductNumber());
        }
        toInsert.setNumRandomizedSites(surv.getRandomizedSitesUsed());
        toInsert.setEndDate(surv.getEndDay());
        toInsert.setStartDate(surv.getStartDay());
        if (surv.getType() != null) {
            toInsert.setSurveillanceType(surv.getType().getName());
        }
        toInsert.setLastModifiedUser(AuthUtil.getAuditId());
        toInsert.setDeleted(false);

        entityManager.persist(toInsert);
        entityManager.flush();

        for (String errorMessage : surv.getErrorMessages()) {
            PendingSurveillanceValidationEntity valEntity = new PendingSurveillanceValidationEntity();
            valEntity.setMessageType(ValidationMessageType.Error);
            valEntity.setPendingSurveillanceId(toInsert.getId());
            valEntity.setMessage(errorMessage);
            valEntity.setDeleted(false);
            valEntity.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(valEntity);
            entityManager.flush();
        }

        for (SurveillanceRequirement req : surv.getRequirements()) {
            PendingSurveillanceRequirementEntity toInsertReq = new PendingSurveillanceRequirementEntity();
            if (req.getResult() != null) {
                toInsertReq.setResult(req.getResult().getName());
            }
            if (req.getType() != null) {
                toInsertReq.setRequirementType(req.getType().getName());
            }
            if (req.getCriterion() != null) {
                toInsertReq.setCertificationCriterionId(req.getCriterion().getId());
            }
            toInsertReq.setSurveilledRequirement(req.getRequirement());
            toInsertReq.setPendingSurveillanceId(toInsert.getId());
            toInsertReq.setLastModifiedUser(AuthUtil.getAuditId());
            toInsertReq.setDeleted(false);

            entityManager.persist(toInsertReq);
            entityManager.flush();

            for (SurveillanceNonconformity nc : req.getNonconformities()) {
                PendingSurveillanceNonconformityEntity toInsertNc = new PendingSurveillanceNonconformityEntity();

                toInsertNc.setCapApproval(nc.getCapApprovalDay());
                toInsertNc.setCapEndDate(nc.getCapEndDay());
                toInsertNc.setCapMustCompleteDate(nc.getCapMustCompleteDay());
                toInsertNc.setCapStart(nc.getCapStartDay());
                toInsertNc.setDateOfDetermination(nc.getDateOfDeterminationDay());
                toInsertNc.setDeveloperExplanation(nc.getDeveloperExplanation());
                toInsertNc.setFindings(nc.getFindings());
                toInsertNc.setPendingSurveillanceRequirementId(toInsertReq.getId());
                toInsertNc.setResolution(nc.getResolution());
                toInsertNc.setSitesPassed(nc.getSitesPassed());
                toInsertNc.setNonconformityCloseDate(nc.getNonconformityCloseDay());
                toInsertNc.setSummary(nc.getSummary());
                toInsertNc.setTotalSites(nc.getTotalSites());
                //.setType(nc.getNonconformityType());
                //if (nc.getCriterion() != null) {
                //    toInsertNc.setCertificationCriterionId(nc.getCriterion().getId());
                //}
                toInsertNc.setDeleted(false);
                toInsertNc.setLastModifiedUser(AuthUtil.getAuditId());

                entityManager.persist(toInsertNc);
                entityManager.flush();
            }
        }
        return toInsert.getId();
    }

    */

    @Deprecated
    public void deleteNonconformityDocument(Long documentId) {

        SurveillanceNonconformityDocumentationEntity doc = entityManager
                .find(SurveillanceNonconformityDocumentationEntity.class, documentId);
        if (doc == null) {
            LOGGER.error(msgUtil.getMessage("surveillance.document.notFound"));
            return;
        }
        doc.setDeleted(true);
        doc.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.merge(doc);
        entityManager.flush();
    }


    public void deleteSurveillance(Surveillance surv) throws EntityRetrievalException {
        LOGGER.debug("Looking for surveillance with id " + surv.getId() + " to delete.");
        SurveillanceEntity toDelete = fetchSurveillanceById(surv.getId());
        if (toDelete.getSurveilledRequirements() != null) {
            for (SurveillanceRequirementEntity reqToDelete : toDelete.getSurveilledRequirements()) {
                if (reqToDelete.getNonconformities() != null) {
                    for (SurveillanceNonconformityEntity ncToDelete : reqToDelete.getNonconformities()) {
                        if (ncToDelete.getDocuments() != null) {
                            for (SurveillanceNonconformityDocumentationEntity docToDelete : ncToDelete.getDocuments()) {
                                docToDelete.setDeleted(true);
                                docToDelete.setLastModifiedUser(AuthUtil.getAuditId());
                                entityManager.merge(docToDelete);
                                entityManager.flush();
                            }
                        }
                        ncToDelete.setDeleted(true);
                        ncToDelete.setLastModifiedUser(AuthUtil.getAuditId());
                        entityManager.merge(ncToDelete);
                        entityManager.flush();
                    }
                }
                reqToDelete.setDeleted(true);
                reqToDelete.setLastModifiedUser(AuthUtil.getAuditId());
                entityManager.merge(reqToDelete);
                entityManager.flush();
            }
        }
        toDelete.setDeleted(true);
        toDelete.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.merge(toDelete);
        entityManager.flush();
    }


    public void deletePendingSurveillance(Surveillance surv) throws EntityRetrievalException {
        PendingSurveillanceEntity toDelete = fetchPendingSurveillanceById(surv.getId(), false);

        if (toDelete.getValidation() != null) {
            for (PendingSurveillanceValidationEntity val : toDelete.getValidation()) {
                val.setDeleted(true);
                val.setLastModifiedUser(AuthUtil.getAuditId());
                entityManager.merge(val);
                entityManager.flush();
            }
        }

        if (toDelete.getSurveilledRequirements() != null) {
            for (PendingSurveillanceRequirementEntity reqToDelete : toDelete.getSurveilledRequirements()) {
                if (reqToDelete.getNonconformities() != null) {
                    for (PendingSurveillanceNonconformityEntity ncToDelete : reqToDelete.getNonconformities()) {
                        ncToDelete.setDeleted(true);
                        ncToDelete.setLastModifiedUser(AuthUtil.getAuditId());
                        entityManager.merge(ncToDelete);
                        entityManager.flush();
                    }
                }
                reqToDelete.setDeleted(true);
                reqToDelete.setLastModifiedUser(AuthUtil.getAuditId());
                entityManager.merge(reqToDelete);
                entityManager.flush();
            }
        }
        toDelete.setDeleted(true);
        toDelete.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.merge(toDelete);
        entityManager.flush();
    }


    public PendingSurveillanceEntity getPendingSurveillanceById(Long id) throws EntityRetrievalException {
        PendingSurveillanceEntity entity = fetchPendingSurveillanceById(id, false);
        return entity;
    }


    public PendingSurveillanceEntity getPendingSurveillanceById(Long id, Boolean includeDeleted)
            throws EntityRetrievalException {
        PendingSurveillanceEntity entity = fetchPendingSurveillanceById(id, includeDeleted);
        return entity;
    }


    public List<PendingSurveillanceEntity> getPendingSurveillanceByAcb(Long acbId) {
        List<PendingSurveillanceEntity> results = fetchPendingSurveillanceByAcbId(acbId);
        return results;
    }


    public List<SurveillanceType> getAllSurveillanceTypes() {
        Query query = entityManager.createQuery("from SurveillanceTypeEntity where deleted <> true",
                SurveillanceTypeEntity.class);
        List<SurveillanceTypeEntity> resultEntities = query.getResultList();
        List<SurveillanceType> results = new ArrayList<SurveillanceType>();
        for (SurveillanceTypeEntity resultEntity : resultEntities) {
            SurveillanceType result = convert(resultEntity);
            results.add(result);
        }
        return results;
    }


    public SurveillanceType findSurveillanceType(String type) {
        LOGGER.debug("Searchig for surveillance type '" + type + "'.");
        if (StringUtils.isEmpty(type)) {
            return null;
        }
        Query query = entityManager.createQuery(
                "from SurveillanceTypeEntity where UPPER(name) LIKE :name and deleted <> true",
                SurveillanceTypeEntity.class);
        query.setParameter("name", type.toUpperCase());
        List<SurveillanceTypeEntity> matches = query.getResultList();

        SurveillanceTypeEntity resultEntity = null;
        if (matches != null && matches.size() > 0) {
            resultEntity = matches.get(0);
            LOGGER.debug("Found surveillance type '" + type + "' having id '" + resultEntity.getId() + "'.");
        }

        SurveillanceType result = convert(resultEntity);
        return result;
    }


    public SurveillanceType findSurveillanceType(Long id) {
        LOGGER.debug("Searchig for surveillance type with id '" + id + "'.");
        if (id == null) {
            return null;
        }
        Query query = entityManager.createQuery("from SurveillanceTypeEntity where id = :id and deleted <> true",
                SurveillanceTypeEntity.class);
        query.setParameter("id", id);
        List<SurveillanceTypeEntity> matches = query.getResultList();

        SurveillanceTypeEntity resultEntity = null;
        if (matches != null && matches.size() > 0) {
            resultEntity = matches.get(0);
        }

        SurveillanceType result = convert(resultEntity);
        return result;
    }


    @Transactional(readOnly = true)
    public List<SurveillanceEntity> getAllSurveillance() {
        Query query = entityManager.createQuery("from SurveillanceEntity where deleted <> true",
                SurveillanceEntity.class);
        return query.getResultList();
    }


    @Transactional(readOnly = true)
    public List<SurveillanceNonconformityEntity> getAllSurveillanceNonConformities() {
        Query query = entityManager.createQuery("from SurveillanceNonconformityEntity where deleted <> true",
                SurveillanceNonconformityEntity.class);
        return query.getResultList();
    }


    public List<SurveillanceRequirementType> getAllSurveillanceRequirementTypes() {
        Query query = entityManager.createQuery("from SurveillanceRequirementTypeEntity where deleted <> true",
                SurveillanceRequirementTypeEntity.class);
        List<SurveillanceRequirementTypeEntity> resultEntities = query.getResultList();
        List<SurveillanceRequirementType> results = new ArrayList<SurveillanceRequirementType>();
        for (SurveillanceRequirementTypeEntity resultEntity : resultEntities) {
            SurveillanceRequirementType result = convert(resultEntity);
            results.add(result);
        }
        return results;
    }


    @Cacheable(CacheNames.FIND_SURVEILLANCE_REQ_TYPE)
    public SurveillanceRequirementType findSurveillanceRequirementType(String type) {
        LOGGER.debug("Searching for surveillance requirement type '" + type + "'.");
        if (StringUtils.isEmpty(type)) {
            return null;
        }
        Query query = entityManager.createQuery(
                "from SurveillanceRequirementTypeEntity where UPPER(name) LIKE :name and deleted <> true",
                SurveillanceRequirementTypeEntity.class);
        query.setParameter("name", type.toUpperCase());
        List<SurveillanceRequirementTypeEntity> matches = query.getResultList();

        SurveillanceRequirementTypeEntity resultEntity = null;
        if (matches != null && matches.size() > 0) {
            resultEntity = matches.get(0);
            LOGGER.debug(
                    "Found surveillance requirement type '" + type + "' having id '" + resultEntity.getId() + "'.");
        }

        SurveillanceRequirementType result = convert(resultEntity);
        return result;
    }


    public SurveillanceRequirementType findSurveillanceRequirementType(Long id) {
        LOGGER.debug("Searching for surveillance requirement type by id '" + id + "'.");
        if (id == null) {
            return null;
        }
        Query query = entityManager.createQuery(
                "from SurveillanceRequirementTypeEntity where id = :id and deleted <> true",
                SurveillanceRequirementTypeEntity.class);
        query.setParameter("id", id);
        List<SurveillanceRequirementTypeEntity> matches = query.getResultList();

        SurveillanceRequirementTypeEntity resultEntity = null;
        if (matches != null && matches.size() > 0) {
            resultEntity = matches.get(0);
        }

        SurveillanceRequirementType result = convert(resultEntity);
        return result;
    }


    public List<SurveillanceResultType> getAllSurveillanceResultTypes() {
        Query query = entityManager.createQuery("from SurveillanceResultTypeEntity where deleted <> true",
                SurveillanceResultTypeEntity.class);
        List<SurveillanceResultTypeEntity> resultEntities = query.getResultList();
        List<SurveillanceResultType> results = new ArrayList<SurveillanceResultType>();
        for (SurveillanceResultTypeEntity resultEntity : resultEntities) {
            SurveillanceResultType result = convert(resultEntity);
            results.add(result);
        }
        return results;
    }


    @Cacheable(CacheNames.FIND_SURVEILLANCE_RESULT_TYPE)
    public SurveillanceResultType findSurveillanceResultType(String type) {
        LOGGER.debug("Searching for surveillance result type '" + type + "'.");
        if (StringUtils.isEmpty(type)) {
            return null;
        }
        Query query = entityManager.createQuery(
                "from SurveillanceResultTypeEntity where UPPER(name) LIKE :name and deleted <> true",
                SurveillanceResultTypeEntity.class);
        query.setParameter("name", type.toUpperCase());
        List<SurveillanceResultTypeEntity> matches = query.getResultList();

        SurveillanceResultTypeEntity resultEntity = null;
        if (matches != null && matches.size() > 0) {
            resultEntity = matches.get(0);
            LOGGER.debug("Found surveillance result type '" + type + "' having id '" + resultEntity.getId() + "'.");
        }

        SurveillanceResultType result = convert(resultEntity);
        return result;
    }


    public SurveillanceResultType findSurveillanceResultType(Long id) {
        LOGGER.debug("Searching for surveillance result type by id '" + id + "'.");
        if (id == null) {
            return null;
        }
        Query query = entityManager.createQuery("from SurveillanceResultTypeEntity where id = :id and deleted <> true",
                SurveillanceResultTypeEntity.class);
        query.setParameter("id", id);
        List<SurveillanceResultTypeEntity> matches = query.getResultList();

        SurveillanceResultTypeEntity resultEntity = null;
        if (matches != null && matches.size() > 0) {
            resultEntity = matches.get(0);
        }

        SurveillanceResultType result = convert(resultEntity);
        return result;
    }



    @Transactional(readOnly = true)
    public List<PendingSurveillanceEntity> getAllPendingSurveillance() {
        Query query = entityManager.createQuery(PENDING_SURVEILLANCE_FULL_HQL
                + " WHERE surv.deleted <> true ",
                PendingSurveillanceEntity.class);
        return query.getResultList();
    }

    private SurveillanceEntity fetchSurveillanceById(Long id) throws EntityRetrievalException {
        entityManager.clear();
        Query query = entityManager.createQuery(SURVEILLANCE_FULL_HQL
                + "AND surv.id = :entityid",
                SurveillanceEntity.class);
        query.setParameter("entityid", id);

        List<SurveillanceEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            String msg = msgUtil.getMessage("surveillance.notFound");
            throw new EntityRetrievalException(msg);
        } else {
            return results.get(0);
        }
    }

    public List<NonconformityType> getNonconformityTypes() {
        return getNonconformityTypeEntities().stream()
                .map(e -> e.toDomain())
                .toList();
    }

    public List<RequirementDetailType> getRequirementDetailTypes() {
        return getRequirementDetailTypeEntities().stream()
                .map(e -> e.toDomain())
                .toList();
    }

    private PendingSurveillanceEntity fetchPendingSurveillanceById(Long id, Boolean includeDeleted)
            throws EntityRetrievalException {
        PendingSurveillanceEntity entity = null;
        String hql = PENDING_SURVEILLANCE_FULL_HQL
                + " WHERE surv.id = :entityid ";
        if (!includeDeleted) {
            hql = hql + " AND surv.deleted <> true ";
        }

        entityManager.clear();
        Query query = entityManager.createQuery(hql, PendingSurveillanceEntity.class);
        query.setParameter("entityid", id);

        List<PendingSurveillanceEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            String msg = msgUtil.getMessage("surveillance.pending.notFound");
            throw new EntityRetrievalException(msg);
        } else {
            entity = results.get(0);
        }
        return entity;
    }

    private List<PendingSurveillanceEntity> fetchPendingSurveillanceByAcbId(Long acbId) {
        entityManager.clear();
        Query query = entityManager.createQuery(PENDING_SURVEILLANCE_FULL_HQL
                + " WHERE surv.deleted <> true "
                + " AND cp.certificationBodyId = :acbId",
                PendingSurveillanceEntity.class);
        query.setParameter("acbId", acbId);

        List<PendingSurveillanceEntity> results = query.getResultList();
        return results;
    }

    private List<NonconformityTypeEntity> getNonconformityTypeEntities() {
        Query query = entityManager.createQuery(
                "FROM NonconformityTypeEntity e "
                + "LEFT JOIN FETCH e.certificationEdition",
                NonconformityTypeEntity.class);
        return query.getResultList();
    }

    private List<RequirementDetailTypeEntity> getRequirementDetailTypeEntities() {
        Query query = entityManager.createQuery(
                "FROM RequirementDetailTypeEntity e "
                + "LEFT JOIN FETCH e.certificationEdition ce "
                + "LEFT JOIN FETCH e.surveillanceRequirementType srt",
            RequirementDetailTypeEntity.class);
        return query.getResultList();
    }

    private SurveillanceType convert(SurveillanceTypeEntity entity) {
        SurveillanceType result = null;
        if (entity != null) {
            result = new SurveillanceType();
            result.setId(entity.getId());
            result.setName(entity.getName());
        }
        return result;
    }

    private SurveillanceRequirementType convert(SurveillanceRequirementTypeEntity entity) {
        SurveillanceRequirementType result = null;
        if (entity != null) {
            result = new SurveillanceRequirementType();
            result.setId(entity.getId());
            result.setName(entity.getName());
        }
        return result;
    }

    private SurveillanceResultType convert(SurveillanceResultTypeEntity entity) {
        SurveillanceResultType result = null;
        if (entity != null) {
            result = new SurveillanceResultType();
            result.setId(entity.getId());
            result.setName(entity.getName());
        }
        return result;
    }

    private void populateSurveillanceEntity(SurveillanceEntity to, Surveillance from) {
        if (from.getCertifiedProduct() != null) {
            to.setCertifiedProductId(from.getCertifiedProduct().getId());
        }
        to.setEndDate(from.getEndDay());
        to.setNumRandomizedSites(from.getRandomizedSitesUsed());
        to.setStartDate(from.getStartDay());
        if (from.getType() != null) {
            to.setSurveillanceTypeId(from.getType().getId());
        }
    }

//    private void populateSurveillanceRequirementEntity(SurveillanceRequirementEntity to,
//            SurveillanceRequirement from) {
//        if (from.getCriterion() != null) {
//            to.setCertificationCriterionId(from.getCriterion().getId());
//        } else if (from.getRequirement() != null) {
//            to.setSurveilledRequirement(from.getRequirement());
//            to.setCertificationCriterionId(null);
//        }
//
//        if (from.getType() != null) {
//            to.setSurveillanceRequirementTypeId(from.getType().getId());
//        }
//        if (from.getResult() != null) {
//            to.setSurveillanceResultTypeId(from.getResult().getId());
//        }
//    }

//    private void populateSurveillanceNonconformityEntity(SurveillanceNonconformityEntity to,
//            SurveillanceNonconformity from) {
//        to.setType(NonconformityTypeEntity.builder()
//                .id(from.getType().getId())
//                .number(from.getType().getNumber())
//                .title(from.getType().getTitle())
//                .removed(from.getType().getRemoved())
//                .build());
//        to.setCapApproval(from.getCapApprovalDay());
//        to.setCapEndDate(from.getCapEndDay());
//        to.setCapMustCompleteDate(from.getCapMustCompleteDay());
//        to.setCapStart(from.getCapStartDay());
//        to.setDateOfDetermination(from.getDateOfDeterminationDay());
//        to.setDeveloperExplanation(from.getDeveloperExplanation());
//        to.setFindings(from.getFindings());
//        to.setResolution(from.getResolution());
//        to.setSitesPassed(from.getSitesPassed());
//        to.setNonconformityCloseDate(from.getNonconformityCloseDay());
//        to.setSummary(from.getSummary());
//        to.setTotalSites(from.getTotalSites());
//    }
}
