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
import gov.healthit.chpl.domain.surveillance.RequirementGroupType;
import gov.healthit.chpl.domain.surveillance.RequirementType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.entity.surveillance.NonconformityTypeEntity;
import gov.healthit.chpl.entity.surveillance.RequirementGroupTypeEntity;
import gov.healthit.chpl.entity.surveillance.RequirementTypeEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceResultTypeEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.extern.log4j.Log4j2;

@Repository("surveillanceDAO")
@Log4j2
public class SurveillanceDAO extends BaseDAOImpl {
    private static final String SURVEILLANCE_FULL_HQL =
            "SELECT DISTINCT surv "
            + "FROM SurveillanceEntity surv "
            + "JOIN FETCH surv.certifiedProduct "
            + "JOIN FETCH surv.surveillanceType "
            + "LEFT OUTER JOIN FETCH surv.surveilledRequirements reqs "
            + "LEFT OUTER JOIN FETCH reqs.requirementType "
            + "LEFT OUTER JOIN FETCH reqs.surveillanceResultTypeEntity "
            + "LEFT OUTER JOIN FETCH reqs.nonconformities ncs "
            + "LEFT OUTER JOIN FETCH ncs.type nct "
            + "LEFT OUTER JOIN FETCH ncs.documents docs "
            + "WHERE surv.deleted <> true ";

    public Long insertSurveillance(Surveillance surv) throws UserPermissionRetrievalException {
        SurveillanceEntity toInsert = new SurveillanceEntity();
        populateSurveillanceEntity(toInsert, surv);
        toInsert.setLastModifiedUser(AuthUtil.getAuditId());
        toInsert.setDeleted(false);
        entityManager.persist(toInsert);
        entityManager.flush();

        surv.getRequirements().forEach(req -> insertSurveillanceRequirement(req, toInsert.getId()));

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
        nonconformityEntity.setType(NonconformityTypeEntity.builder()
                .id(nonconformity.getType().getId())
                .build());
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
                .surveillanceResultTypeEntity(getSurveillanceResultTypeEntityById(requirement.getResult().getId()))
                .requirementType(requirement.getRequirementType() != null
                        ? getRequirementTypeEntityById(requirement.getRequirementType().getId())
                        : null)
                .requirementTypeOther(requirement.getRequirementTypeOther())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        entityManager.persist(toInsertReq);
        entityManager.flush();

        requirement.getNonconformities().forEach(nc -> insertSurveillanceNonconformity(nc, toInsertReq.getId()));

        return toInsertReq;
    }

    private SurveillanceRequirementEntity updateSurveillanceRequirement(SurveillanceRequirement requirement, SurveillanceRequirementEntity requirementEntity) {
        requirementEntity.setSurveillanceResultTypeEntity(getSurveillanceResultTypeEntityById(requirement.getResult().getId()));
        if (requirement.getRequirementType() != null) {
            requirementEntity.setRequirementType(getRequirementTypeEntityById(requirement.getRequirementType().getId()));
        }
        requirementEntity.setRequirementTypeOther(requirement.getRequirementTypeOther());
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
        if (id == null) {
            return null;
        } else {
            return nonconformities.stream()
                    .filter(req -> NullSafeEvaluator.eval(() -> req.getId(), -1).equals(id))
                    .findAny()
                    .orElse(null);
        }
    }

    private SurveillanceRequirement getSurveillanceRequirement(Long id, Set<SurveillanceRequirement> requirements) {
        if (id == null) {
            return null;
        } else {
            return requirements.stream()
                    .filter(req -> NullSafeEvaluator.eval(() -> req.getId(), -1).equals(id))
                    .findAny()
                    .orElse(null);
        }
    }

    private Boolean isIdInSurveillanceRequirements(Long id, Set<SurveillanceRequirement> requirements) {
        if (id == null) {
            return false;
        } else {
            return requirements.stream()
                    .filter(req -> NullSafeEvaluator.eval(() -> req.getId(), -1).equals(id))
                    .findAny()
                    .isPresent();
        }
    }

    private Boolean isIdInSurveillanceRequirementEntities(Long id, Set<SurveillanceRequirementEntity> requirements) {
        if (id == null) {
            return false;
        } else {
            return requirements.stream()
                    .filter(req -> NullSafeEvaluator.eval(() -> req.getId(), -1).equals(id))
                    .findAny()
                    .isPresent();
        }
    }

    private Boolean isIdInSurveillanceNonconformities(Long id, List<SurveillanceNonconformity> nonconformities) {
        if (id == null) {
            return false;
        } else {
            return nonconformities.stream()
                    .filter(req -> NullSafeEvaluator.eval(() -> req.getId(), -1).equals(id))
                    .findAny()
                    .isPresent();
        }
    }

    private Boolean isIdInSurveillanceNonconformityEntities(Long id, Set<SurveillanceNonconformityEntity> nonconformities) {
        if (id == null) {
            return false;
        } else {
            return nonconformities.stream()
                    .filter(req -> NullSafeEvaluator.eval(() -> req.getId(), -1).equals(id))
                    .findAny()
                    .isPresent();
        }
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


    public List<RequirementGroupType> getAllRequirementGroupTypes() {
        Query query = entityManager.createQuery("from RequirementGroupTypeEntity where deleted <> true",
                RequirementGroupTypeEntity.class);
        List<RequirementGroupTypeEntity> resultEntities = query.getResultList();
        List<RequirementGroupType> results = new ArrayList<RequirementGroupType>();
        for (RequirementGroupTypeEntity resultEntity : resultEntities) {
            RequirementGroupType result = convert(resultEntity);
            results.add(result);
        }
        return results;
    }


    @Cacheable(CacheNames.FIND_SURVEILLANCE_REQ_TYPE)
    public RequirementGroupType findRequirementGroupType(String type) {
        LOGGER.debug("Searching for requirement group type '" + type + "'.");
        if (StringUtils.isEmpty(type)) {
            return null;
        }
        Query query = entityManager.createQuery(
                "from RequirementTypeGroupEntity where UPPER(name) LIKE :name and deleted <> true",
                RequirementGroupTypeEntity.class);
        query.setParameter("name", type.toUpperCase());
        List<RequirementGroupTypeEntity> matches = query.getResultList();

        RequirementGroupTypeEntity resultEntity = null;
        if (matches != null && matches.size() > 0) {
            resultEntity = matches.get(0);
            LOGGER.debug(
                    "Found requirement group type '" + type + "' having id '" + resultEntity.getId() + "'.");
        }

        RequirementGroupType result = convert(resultEntity);
        return result;
    }


    public RequirementGroupType findRequirementGroupType(Long id) {
        if (id == null) {
            return null;
        }
        Query query = entityManager.createQuery(
                "from RequirementGroupTypeEntity where id = :id and deleted <> true",
                RequirementGroupTypeEntity.class);
        query.setParameter("id", id);
        List<RequirementGroupTypeEntity> matches = query.getResultList();

        RequirementGroupTypeEntity resultEntity = null;
        if (matches != null && matches.size() > 0) {
            resultEntity = matches.get(0);
        }

        RequirementGroupType result = convert(resultEntity);
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

    public List<RequirementType> getRequirementTypes() {
        return getRequirementTypeEntities().stream()
                .map(e -> e.toDomain())
                .toList();
    }

    private List<NonconformityTypeEntity> getNonconformityTypeEntities() {
        Query query = entityManager.createQuery(
                "FROM NonconformityTypeEntity e "
                + "LEFT JOIN FETCH e.certificationEdition",
                NonconformityTypeEntity.class);
        return query.getResultList();
    }

    private List<RequirementTypeEntity> getRequirementTypeEntities() {
        Query query = entityManager.createQuery(
                "FROM RequirementTypeEntity e "
                + "LEFT JOIN FETCH e.certificationEdition ce "
                + "LEFT JOIN FETCH e.requirementGroupType rgt",
            RequirementTypeEntity.class);
        return query.getResultList();
    }

    private RequirementTypeEntity getRequirementTypeEntityById(Long requirementTypeId) {
        return getRequirementTypeEntities().stream()
               .filter(rdt -> rdt.getId().equals(requirementTypeId))
               .findAny()
               .orElse(null);
    }

    private List<SurveillanceResultTypeEntity> getSurveillanceResultTypeEntities() {
        Query query = entityManager.createQuery("FROM SurveillanceResultTypeEntity e ", SurveillanceResultTypeEntity.class);
        return query.getResultList();
    }

    private SurveillanceResultTypeEntity getSurveillanceResultTypeEntityById(Long resultTypeId) {
        return getSurveillanceResultTypeEntities().stream()
               .filter(result -> result.getId().equals(resultTypeId))
               .findAny()
               .orElse(null);
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

    private RequirementGroupType convert(RequirementGroupTypeEntity entity) {
        RequirementGroupType result = null;
        if (entity != null) {
            result = new RequirementGroupType();
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
}
