package gov.healthit.chpl.dao.surveillance;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.entity.ValidationMessageType;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceRequirementEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceValidationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementTypeEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceResultTypeEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;

@Repository("surveillanceDAO")
public class SurveillanceDAO extends BaseDAOImpl {
    private static Logger LOGGER = LogManager.getLogger(SurveillanceDAO.class);
    private static String SURVEILLANCE_FULL_HQL = "SELECT DISTINCT surv "
            + "FROM SurveillanceEntity surv "
            + "JOIN FETCH surv.certifiedProduct " + "JOIN FETCH surv.surveillanceType "
            + "LEFT OUTER JOIN FETCH surv.surveilledRequirements reqs "
            + "LEFT OUTER JOIN FETCH reqs.surveillanceRequirementType "
            + "LEFT OUTER JOIN FETCH reqs.certificationCriterionEntity cce "
            + "LEFT JOIN FETCH cce.certificationEdition "
            + "LEFT OUTER JOIN FETCH reqs.surveillanceResultTypeEntity "
            + "LEFT OUTER JOIN FETCH reqs.nonconformities ncs "
            + "LEFT OUTER JOIN FETCH ncs.certificationCriterionEntity cce2 "
            + "LEFT JOIN FETCH cce2.certificationEdition "
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

    private UserPermissionDAO userPermissionDao;
    private ResourcePermissions resourcePermissions;

    @Autowired
    public SurveillanceDAO(UserPermissionDAO userPermissionDao,
            ResourcePermissions resourcePermissions) {
        this.userPermissionDao = userPermissionDao;
        this.resourcePermissions = resourcePermissions;
    }


    public Long insertSurveillance(Surveillance surv) throws UserPermissionRetrievalException {
        SurveillanceEntity toInsert = new SurveillanceEntity();
        populateSurveillanceEntity(toInsert, surv);
        toInsert.setLastModifiedUser(AuthUtil.getAuditId());
        toInsert.setDeleted(false);
        entityManager.persist(toInsert);
        entityManager.flush();

        for (SurveillanceRequirement req : surv.getRequirements()) {
            SurveillanceRequirementEntity toInsertReq = new SurveillanceRequirementEntity();
            populateSurveillanceRequirementEntity(toInsertReq, req);
            toInsertReq.setSurveillanceId(toInsert.getId());
            toInsertReq.setLastModifiedUser(AuthUtil.getAuditId());
            toInsertReq.setDeleted(false);
            entityManager.persist(toInsertReq);
            entityManager.flush();

            for (SurveillanceNonconformity nc : req.getNonconformities()) {
                SurveillanceNonconformityEntity toInsertNc = new SurveillanceNonconformityEntity();
                populateSurveillanceNonconformityEntity(toInsertNc, nc);
                toInsertNc.setSurveillanceRequirementId(toInsertReq.getId());
                toInsertNc.setDeleted(false);
                toInsertNc.setLastModifiedUser(AuthUtil.getAuditId());

                entityManager.persist(toInsertNc);
                entityManager.flush();
            }
        }
        return toInsert.getId();
    }


    public Long insertNonconformityDocument(Long nonconformityId, SurveillanceNonconformityDocument doc)
            throws EntityRetrievalException {
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
    }


    public Long updateSurveillance(Surveillance newSurv)
            throws EntityRetrievalException, UserPermissionRetrievalException {
        SurveillanceEntity oldSurv = fetchSurveillanceById(newSurv.getId());
        populateSurveillanceEntity(oldSurv, newSurv);
        oldSurv.setLastModifiedUser(AuthUtil.getAuditId());
        oldSurv.setDeleted(false);
        entityManager.merge(oldSurv);
        entityManager.flush();

        // look for reqs that are in the updateSurv but not the newSurv and mark
        // as deleted
        for (SurveillanceRequirementEntity oldReq : oldSurv.getSurveilledRequirements()) {
            boolean isFoundInUpdate = false;
            for (SurveillanceRequirement newReq : newSurv.getRequirements()) {
                if (newReq.getId() != null && newReq.getId().longValue() == oldReq.getId().longValue()) {
                    isFoundInUpdate = true;
                }
            }

            if (!isFoundInUpdate) {
                // delete nonconformities and documents for this requirement
                // first
                for (SurveillanceNonconformityEntity nc : oldReq.getNonconformities()) {
                    if (nc.getDocuments() != null) {
                        for (SurveillanceNonconformityDocumentationEntity ncDoc : nc.getDocuments()) {
                            ncDoc.setLastModifiedUser(AuthUtil.getAuditId());
                            ncDoc.setDeleted(true);
                            entityManager.merge(ncDoc);
                            entityManager.flush();
                        }
                    }
                    nc.setLastModifiedUser(AuthUtil.getAuditId());
                    nc.setDeleted(true);
                    entityManager.merge(nc);
                    entityManager.flush();
                }
                // delete the req
                oldReq.setLastModifiedUser(AuthUtil.getAuditId());
                oldReq.setDeleted(true);
                entityManager.merge(oldReq);
                entityManager.flush();
            }
        }

        // look through the incoming reqs and add or update as necessary
        for (SurveillanceRequirement newReq : newSurv.getRequirements()) {
            if (newReq.getId() != null && newReq.getId().longValue() > 0) {
                // update existing req
                for (SurveillanceRequirementEntity oldReq : oldSurv.getSurveilledRequirements()) {
                    if (oldReq.getId().longValue() == newReq.getId().longValue()) {
                        populateSurveillanceRequirementEntity(oldReq, newReq);
                        oldReq.setLastModifiedUser(AuthUtil.getAuditId());
                        oldReq.setDeleted(false);
                        entityManager.merge(oldReq);
                        entityManager.flush();

                        // look for nonconformites that are in updateReq but not
                        // in newReq and mark as deleted
                        for (SurveillanceNonconformityEntity oldNc : oldReq.getNonconformities()) {
                            boolean isFoundInUpdate = false;
                            for (SurveillanceNonconformity newNc : newReq.getNonconformities()) {
                                if (newNc.getId() != null && newNc.getId().longValue() == oldNc.getId().longValue()) {
                                    isFoundInUpdate = true;
                                }
                            }
                            if (!isFoundInUpdate) {
                                if (oldNc.getDocuments() != null) {
                                    for (SurveillanceNonconformityDocumentationEntity ncDoc : oldNc.getDocuments()) {
                                        ncDoc.setLastModifiedUser(AuthUtil.getAuditId());
                                        ncDoc.setDeleted(true);
                                        entityManager.merge(ncDoc);
                                        entityManager.flush();
                                    }
                                }
                                oldNc.setLastModifiedUser(AuthUtil.getAuditId());
                                oldNc.setDeleted(true);
                                entityManager.merge(oldNc);
                                entityManager.flush();
                            }
                        }

                        // look through newReq nonconformities and add or update
                        // as necessary
                        for (SurveillanceNonconformity newNc : newReq.getNonconformities()) {
                            if (newNc.getId() != null && newNc.getId().longValue() > 0) {
                                // update existing nonconformity
                                for (SurveillanceNonconformityEntity oldNc : oldReq.getNonconformities()) {
                                    if (oldNc.getId().longValue() == newNc.getId().longValue()) {
                                        populateSurveillanceNonconformityEntity(oldNc, newNc);
                                        oldNc.setLastModifiedUser(AuthUtil.getAuditId());
                                        oldNc.setDeleted(false);
                                        entityManager.merge(oldNc);
                                        entityManager.flush();
                                    }
                                }
                            } else {
                                // add new nonconformity
                                SurveillanceNonconformityEntity toInsertNc = new SurveillanceNonconformityEntity();
                                populateSurveillanceNonconformityEntity(toInsertNc, newNc);
                                toInsertNc.setSurveillanceRequirementId(oldReq.getId());
                                toInsertNc.setDeleted(false);
                                toInsertNc.setLastModifiedUser(AuthUtil.getAuditId());
                                entityManager.persist(toInsertNc);
                                entityManager.flush();
                            }
                        }
                    }
                }
            } else {
                // add new req
                SurveillanceRequirementEntity toInsertReq = new SurveillanceRequirementEntity();
                populateSurveillanceRequirementEntity(toInsertReq, newReq);
                toInsertReq.setSurveillanceId(oldSurv.getId());
                toInsertReq.setLastModifiedUser(AuthUtil.getAuditId());
                toInsertReq.setDeleted(false);
                entityManager.persist(toInsertReq);
                entityManager.flush();
                // add new nonconformities
                for (SurveillanceNonconformity nc : newReq.getNonconformities()) {
                    SurveillanceNonconformityEntity toInsertNc = new SurveillanceNonconformityEntity();
                    populateSurveillanceNonconformityEntity(toInsertNc, nc);
                    toInsertNc.setSurveillanceRequirementId(toInsertReq.getId());
                    toInsertNc.setDeleted(false);
                    toInsertNc.setLastModifiedUser(AuthUtil.getAuditId());

                    entityManager.persist(toInsertNc);
                    entityManager.flush();
                }
            }
        }

        return newSurv.getId();
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

    public SurveillanceNonconformityDocumentationEntity getDocumentById(Long documentId)
            throws EntityRetrievalException {
        SurveillanceNonconformityDocumentationEntity doc = entityManager
                .find(SurveillanceNonconformityDocumentationEntity.class, documentId);
        if (doc == null) {
            String msg = msgUtil.getMessage("surveillance.document.notFound");
            throw new EntityRetrievalException(msg);
        }
        return doc;
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

    private Long getSurveillanceAuthority() throws UserPermissionRetrievalException {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return userPermissionDao.getIdFromAuthority(Authority.ROLE_ONC);
        } else if (resourcePermissions.isUserRoleAcbAdmin()) {
            return userPermissionDao.getIdFromAuthority(Authority.ROLE_ACB);
        } else {
            return null;
        }
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
        toInsert.setUserPermissionId(getSurveillanceAuthority());

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
                toInsertNc.setType(nc.getNonconformityType());
                if (nc.getCriterion() != null) {
                    toInsertNc.setCertificationCriterionId(nc.getCriterion().getId());
                }
                toInsertNc.setDeleted(false);
                toInsertNc.setLastModifiedUser(AuthUtil.getAuditId());

                entityManager.persist(toInsertNc);
                entityManager.flush();
            }
        }
        return toInsert.getId();
    }


    public void deleteNonconformityDocument(Long documentId) throws EntityRetrievalException {
        SurveillanceNonconformityDocumentationEntity doc = entityManager
                .find(SurveillanceNonconformityDocumentationEntity.class, documentId);
        if (doc == null) {
            String msg = msgUtil.getMessage("surveillance.document.notFound");
            throw new EntityRetrievalException(msg);
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

    private void populateSurveillanceEntity(SurveillanceEntity to, Surveillance from)
            throws UserPermissionRetrievalException {
        if (from.getCertifiedProduct() != null) {
            to.setCertifiedProductId(from.getCertifiedProduct().getId());
        }
        to.setEndDate(from.getEndDay());
        to.setNumRandomizedSites(from.getRandomizedSitesUsed());
        to.setStartDate(from.getStartDay());
        if (from.getType() != null) {
            to.setSurveillanceTypeId(from.getType().getId());
        }
        to.setUserPermissionId(userPermissionDao.getIdFromAuthority(from.getAuthority()));
    }

    private void populateSurveillanceRequirementEntity(SurveillanceRequirementEntity to,
            SurveillanceRequirement from) {
        if (from.getCriterion() != null) {
            to.setCertificationCriterionId(from.getCriterion().getId());
        } else if (from.getRequirement() != null) {
            to.setSurveilledRequirement(from.getRequirement());
            to.setCertificationCriterionId(null);
        }

        if (from.getType() != null) {
            to.setSurveillanceRequirementTypeId(from.getType().getId());
        }
        if (from.getResult() != null) {
            to.setSurveillanceResultTypeId(from.getResult().getId());
        }
    }

    private void populateSurveillanceNonconformityEntity(SurveillanceNonconformityEntity to,
            SurveillanceNonconformity from) {
        if (from.getCriterion() != null) {
            to.setCertificationCriterionId(from.getCriterion().getId());
        } else if (from.getNonconformityType() != null) {
            to.setType(from.getNonconformityType());
            to.setCertificationCriterionId(null);
        }

        to.setCapApproval(from.getCapApprovalDay());
        to.setCapEndDate(from.getCapEndDay());
        to.setCapMustCompleteDate(from.getCapMustCompleteDay());
        to.setCapStart(from.getCapStartDay());
        to.setDateOfDetermination(from.getDateOfDeterminationDay());
        to.setDeveloperExplanation(from.getDeveloperExplanation());
        to.setFindings(from.getFindings());
        to.setResolution(from.getResolution());
        to.setSitesPassed(from.getSitesPassed());
        to.setNonconformityCloseDate(from.getNonconformityCloseDay());
        to.setSummary(from.getSummary());
        to.setTotalSites(from.getTotalSites());
        to.setType(from.getNonconformityType());
    }
}
