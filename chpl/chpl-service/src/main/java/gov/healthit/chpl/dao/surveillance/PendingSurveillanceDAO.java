package gov.healthit.chpl.dao.surveillance;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.entity.ValidationMessageType;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceRequirementEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceValidationEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;

@Component
public class PendingSurveillanceDAO extends BaseDAOImpl {
    private static String PENDING_SURVEILLANCE_FULL_HQL = "SELECT DISTINCT surv "
            + "FROM PendingSurveillanceEntity surv "
            + "JOIN FETCH surv.certifiedProduct "
            + "LEFT OUTER JOIN FETCH surv.surveilledRequirements reqs "
            + "LEFT OUTER JOIN FETCH reqs.certificationCriterionEntity cce "
            + "LEFT OUTER JOIN FETCH cce.certificationEdition "
            + "LEFT OUTER JOIN FETCH reqs.nonconformities ncs "
            + "LEFT OUTER JOIN FETCH ncs.certificationCriterionEntity cce2 "
            + "LEFT OUTER JOIN FETCH surv.validation ";

    @Transactional(readOnly = true)
    public List<PendingSurveillanceEntity> getAllPendingSurveillance() {
        Query query = entityManager.createQuery(PENDING_SURVEILLANCE_FULL_HQL
                + " WHERE surv.deleted <> true ",
                PendingSurveillanceEntity.class);
        return query.getResultList();
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

        surv.getRequirements().forEach(req -> insertSurveillanceRequirement(req, toInsert.getId()));

        return toInsert.getId();
    }

    private PendingSurveillanceRequirementEntity insertSurveillanceRequirement(SurveillanceRequirement req, Long surveillanceId) {
        PendingSurveillanceRequirementEntity toInsertReq = new PendingSurveillanceRequirementEntity();

        if (req.getResult() != null) {
            toInsertReq.setResult(req.getResult().getName());
        }

        String requirement = req.getRequirementDetailType().getNumber() != null ? req.getRequirementDetailType().getNumber() : req.getRequirementDetailType().getTitle();
        toInsertReq.setRequirementType(req.getRequirementDetailType().getSurveillanceRequirementType().getName());
        toInsertReq.setSurveilledRequirement(requirement);
        toInsertReq.setPendingSurveillanceId(surveillanceId);
        toInsertReq.setLastModifiedUser(AuthUtil.getAuditId());
        toInsertReq.setDeleted(false);

        entityManager.persist(toInsertReq);
        entityManager.flush();

        req.getNonconformities().forEach(nc -> insertSurveillanceNonconformity(nc, toInsertReq.getId()));

        return toInsertReq;
    }

    private PendingSurveillanceNonconformityEntity insertSurveillanceNonconformity(SurveillanceNonconformity nc, Long surveillanceRequirementId) {
        PendingSurveillanceNonconformityEntity toInsertNc = new PendingSurveillanceNonconformityEntity();

        toInsertNc.setCapApproval(nc.getCapApprovalDay());
        toInsertNc.setCapEndDate(nc.getCapEndDay());
        toInsertNc.setCapMustCompleteDate(nc.getCapMustCompleteDay());
        toInsertNc.setCapStart(nc.getCapStartDay());
        toInsertNc.setDateOfDetermination(nc.getDateOfDeterminationDay());
        toInsertNc.setDeveloperExplanation(nc.getDeveloperExplanation());
        toInsertNc.setFindings(nc.getFindings());
        toInsertNc.setPendingSurveillanceRequirementId(surveillanceRequirementId);
        toInsertNc.setResolution(nc.getResolution());
        toInsertNc.setSitesPassed(nc.getSitesPassed());
        toInsertNc.setNonconformityCloseDate(nc.getNonconformityCloseDay());
        toInsertNc.setSummary(nc.getSummary());
        toInsertNc.setTotalSites(nc.getTotalSites());
        toInsertNc.setType(NullSafeEvaluator.eval(() -> nc.getType().getNumber(), nc.getType().getTitle()));
        toInsertNc.setDeleted(false);
        toInsertNc.setLastModifiedUser(AuthUtil.getAuditId());

        entityManager.persist(toInsertNc);
        entityManager.flush();

        return toInsertNc;
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


}
