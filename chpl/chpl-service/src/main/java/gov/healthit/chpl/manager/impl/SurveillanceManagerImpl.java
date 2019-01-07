package gov.healthit.chpl.manager.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.ValidationMessageType;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceRequirementEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceValidationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.validation.surveillance.SurveillanceValidator;

@Service
public class SurveillanceManagerImpl implements SurveillanceManager {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceManagerImpl.class);

    @Autowired
    private SurveillanceDAO survDao;
    @Autowired
    private CertifiedProductDAO cpDao;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private SurveillanceValidator validator;
    @Autowired
    private UserPermissionDAO userPermissionDao;

    @Autowired
    private ActivityManager activityManager;
    @Autowired
    private FileUtils fileUtils;
    @Autowired
    private Environment env;

    @Override
    @Transactional(readOnly = true)
    public Surveillance getById(final Long survId) throws EntityRetrievalException {
        SurveillanceEntity surv = survDao.getSurveillanceById(survId);
        Surveillance result = convertToDomain(surv);
        validator.validate(result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Surveillance getByFriendlyIdAndProduct(final Long certifiedProductId, final String survFriendlyId) {
        SurveillanceEntity surv = survDao.getSurveillanceByCertifiedProductAndFriendlyId(certifiedProductId,
                survFriendlyId);
        if (surv == null) {
            throw new EntityNotFoundException("Could not find surveillance for certified product " + certifiedProductId
                    + " with friendly id " + survFriendlyId);
        }
        Surveillance result = convertToDomain(surv);
        validator.validate(result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Surveillance> getByCertifiedProduct(final Long cpId) {
        List<SurveillanceEntity> survResults = survDao.getSurveillanceByCertifiedProductId(cpId);
        List<Surveillance> results = new ArrayList<Surveillance>();
        if (survResults != null) {
            for (SurveillanceEntity survResult : survResults) {
                Surveillance surv = convertToDomain(survResult);
                validator.validate(surv);
                results.add(surv);
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public SurveillanceNonconformityDocument getDocumentById(final Long docId, final boolean getFileContents)
            throws EntityRetrievalException {
        SurveillanceNonconformityDocumentationEntity docEntity = survDao.getDocumentById(docId);

        SurveillanceNonconformityDocument doc = null;
        if (docEntity != null) {
            doc = convertToDomain(docEntity, getFileContents);
        }
        return doc;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public Long createSurveillance(final Long acbId, final Surveillance surv)
            throws UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
        Long insertedId = null;
        checkSurveillanceAuthority(surv);
        updateNullAuthority(surv);

        try {
            insertedId = survDao.insertSurveillance(surv);
        } catch (final UserPermissionRetrievalException ex) {
            LOGGER.error("Error inserting surveillance.", ex);
            throw ex;
        }

        return insertedId;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or " + "(hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public Long addDocumentToNonconformity(final Long acbId, final Long nonconformityId,
            final SurveillanceNonconformityDocument doc)
            throws EntityRetrievalException {
        Long insertedId = null;
        insertedId = survDao.insertNonconformityDocument(nonconformityId, doc);
        return insertedId;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public void updateSurveillance(final Long acbId, final Surveillance surv) throws EntityRetrievalException,
            UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
        SurveillanceEntity dbSurvEntity = new SurveillanceEntity();
        try {
            dbSurvEntity = survDao.getSurveillanceById(surv.getId());
        } catch (final NullPointerException e) {
            LOGGER.debug("Surveillance id is null");
        }
        Surveillance dbSurv = new Surveillance();
        dbSurv.setId(dbSurvEntity.getId());
        UserPermissionDTO upDto = userPermissionDao.findById(dbSurvEntity.getUserPermissionId());
        dbSurv.setAuthority(upDto.getAuthority());
        checkSurveillanceAuthority(dbSurv);
        try {
            survDao.updateSurveillance(surv);
        } catch (final UserPermissionRetrievalException ex) {
            LOGGER.error("Error updating surveillance.", ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public void deleteSurveillance(final Long acbId, final Surveillance surv)
            throws EntityRetrievalException, SurveillanceAuthorityAccessDeniedException {
        checkSurveillanceAuthority(surv);
        survDao.deleteSurveillance(surv);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public void deleteNonconformityDocument(final Long acbId, final Long documentId) throws EntityRetrievalException {
        survDao.deleteNonconformityDocument(documentId);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public List<Surveillance> getPendingByAcb(final Long acbId) {
        List<PendingSurveillanceEntity> pendingResults = survDao.getPendingSurveillanceByAcb(acbId);
        List<Surveillance> results = new ArrayList<Surveillance>();
        if (pendingResults != null) {
            for (PendingSurveillanceEntity pr : pendingResults) {
                Surveillance surv = convertToDomain(pr);
                results.add(surv);
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public Surveillance getPendingById(final Long acbId, final Long survId, final boolean includeDeleted)
            throws EntityRetrievalException {
        PendingSurveillanceEntity pending = survDao.getPendingSurveillanceById(survId, includeDeleted);
        Surveillance surv = convertToDomain(pending);
        return surv;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public Long createPendingSurveillance(final Long acbId, final Surveillance surv) {
        Long insertedId = null;

        try {
            insertedId = survDao.insertPendingSurveillance(surv);
        } catch (Exception ex) {
            LOGGER.error("Error inserting pending surveillance.", ex);
        }

        return insertedId;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public void deletePendingSurveillance(final Long acbId, final Long survId, final boolean isConfirmed)
            throws ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException,
            EntityCreationException {
        PendingSurveillanceEntity surv = survDao.getPendingSurveillanceById(survId, true);
        CertifiedProductEntity ownerCp = surv.getCertifiedProduct();
        if (ownerCp == null) {
            throw new EntityNotFoundException("Could not find certified product associated with pending surveillance.");
        }

        Surveillance toDelete = getPendingById(acbId, survId, true);

        if (isPendingSurveillanceAvailableForUpdate(ownerCp.getCertificationBodyId(), surv)) {
            try {
                survDao.deletePendingSurveillance(toDelete);
            } catch (Exception ex) {
                LOGGER.error("Error marking pending surveillance with id " + toDelete.getId() + " as deleted.", ex);
            }
            StringBuilder activityMsg = new StringBuilder()
                    .append("Pending surveillance " + toDelete.getId() + " has been ");
            if (isConfirmed) {
                activityMsg.append("confirmed.");
            } else {
                activityMsg.append("rejected.");
            }
            activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PENDING_SURVEILLANCE, toDelete.getId(),
                    activityMsg.toString(), toDelete, null);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ACB')")
    public void deletePendingSurveillance(final List<CertificationBodyDTO> userAcbs,
            final Long survId, final boolean isConfirmed)
            throws EntityNotFoundException, AccessDeniedException, ObjectMissingValidationException,
            JsonProcessingException, EntityRetrievalException, EntityCreationException {
        PendingSurveillanceEntity surv = survDao.getPendingSurveillanceById(survId, true);
        if (surv == null) {
            throw new EntityNotFoundException("Could not find pending surveillance with id " + survId);
        }
        CertifiedProductEntity ownerCp = surv.getCertifiedProduct();
        if (ownerCp == null) {
            throw new EntityNotFoundException("Could not find certified product associated with pending surveillance.");
        }
        boolean userHasAcbPermissions = false;
        for (CertificationBodyDTO acb : userAcbs) {
            if (acb.getId() != null && ownerCp.getCertificationBodyId() != null
                    && acb.getId().longValue() == ownerCp.getCertificationBodyId().longValue()) {
                userHasAcbPermissions = true;
            }
        }

        if (!userHasAcbPermissions) {
            throw new AccessDeniedException("Permission denied on ACB " + ownerCp.getCertificationBodyId()
                    + " for user " + Util.getCurrentUser().getSubjectName());
        }

        if (isPendingSurveillanceAvailableForUpdate(ownerCp.getCertificationBodyId(), surv)) {
            Surveillance toDelete = convertToDomain(surv);
            try {
                survDao.deletePendingSurveillance(toDelete);
            } catch (Exception ex) {
                LOGGER.error("Error marking pending surveillance with id " + toDelete.getId() + " as deleted.", ex);
            }

            StringBuilder activityMsg = new StringBuilder()
                    .append("Pending surveillance " + toDelete.getId() + " has been ");
            if (isConfirmed) {
                activityMsg.append("confirmed.");
            } else {
                activityMsg.append("rejected.");
            }
            activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PENDING_SURVEILLANCE, toDelete.getId(),
                    activityMsg.toString(), toDelete, null);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public boolean isPendingSurveillanceAvailableForUpdate(final Long acbId, final Long pendingSurvId)
            throws EntityRetrievalException, ObjectMissingValidationException {
        PendingSurveillanceEntity pendingSurv = survDao.getPendingSurveillanceById(pendingSurvId, true);
        return isPendingSurveillanceAvailableForUpdate(acbId, pendingSurv);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public boolean isPendingSurveillanceAvailableForUpdate(final Long acbId,
            final PendingSurveillanceEntity pendingSurv)
            throws EntityRetrievalException, ObjectMissingValidationException {
        if (pendingSurv.getDeleted().booleanValue()) {
            ObjectMissingValidationException alreadyDeletedEx = new ObjectMissingValidationException();
            alreadyDeletedEx.getErrorMessages()
                    .add("This pending surveillance has already been confirmed or rejected by another user.");
            alreadyDeletedEx.setObjectId(pendingSurv.getId().toString());
            alreadyDeletedEx.setStartDate(pendingSurv.getStartDate());
            alreadyDeletedEx.setEndDate(pendingSurv.getEndDate());

            try {
                UserDTO lastModifiedUser = userDAO.getById(pendingSurv.getLastModifiedUser());
                if (lastModifiedUser != null) {
                    Contact contact = new Contact();
                    contact.setFullName(lastModifiedUser.getFullName());
                    contact.setFriendlyName(lastModifiedUser.getFriendlyName());
                    contact.setEmail(lastModifiedUser.getEmail());
                    contact.setPhoneNumber(lastModifiedUser.getPhoneNumber());
                    contact.setTitle(lastModifiedUser.getTitle());
                    alreadyDeletedEx.setContact(contact);
                } else {
                    alreadyDeletedEx.setContact(null);
                }
            } catch (final UserRetrievalException ex) {
                alreadyDeletedEx.setContact(null);
            }
            throw alreadyDeletedEx;
        }
        //If pendingSurv were null, we would have gotten an NPE by this point
        //return pendingSurv != null;
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public void validate(final Surveillance surveillance) {
        validator.validate(surveillance);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ONC_STAFF')")
    public File getBasicReportDownloadFile() throws IOException {
        return fileUtils.getNewestFileMatchingName("^" + env.getProperty("surveillanceBasicReportName") + "-.+\\.csv$");
    }

    @Override
    public File getAllSurveillanceDownloadFile() throws IOException {
        return fileUtils.getNewestFileMatchingName("^" + env.getProperty("surveillanceAllReportName") + "-.+\\.csv$");
    }

    @Override
    public File getSurveillanceWithNonconformitiesDownloadFile() throws IOException {
        return fileUtils.getNewestFileMatchingName("^" + env.getProperty("surveillanceNonconformitiesReportName")
            + "-.+\\.csv$");
    }

    private Surveillance convertToDomain(final PendingSurveillanceEntity pr) {
        Surveillance surv = new Surveillance();
        surv.setId(pr.getId());
        surv.setSurveillanceIdToReplace(pr.getSurvFriendlyIdToReplace());
        surv.setStartDate(pr.getStartDate());
        surv.setEndDate(pr.getEndDate());
        surv.setRandomizedSitesUsed(pr.getNumRandomizedSites());

        SurveillanceType survType = new SurveillanceType();
        survType.setName(pr.getSurveillanceType());
        surv.setType(survType);

        if (pr.getSurveilledRequirements() != null) {
            for (PendingSurveillanceRequirementEntity preq : pr.getSurveilledRequirements()) {
                SurveillanceRequirement req = new SurveillanceRequirement();
                req.setId(preq.getId());
                req.setRequirement(preq.getSurveilledRequirement());
                SurveillanceResultType result = new SurveillanceResultType();
                result.setName(preq.getResult());
                req.setResult(result);
                SurveillanceRequirementType reqType = new SurveillanceRequirementType();
                reqType.setName(preq.getRequirementType());
                req.setType(reqType);

                CertifiedProduct cp = new CertifiedProduct();
                cp.setId(pr.getCertifiedProductId());
                cp.setChplProductNumber(pr.getCertifiedProductUniqueId());
                surv.setCertifiedProduct(cp);

                if (preq.getNonconformities() != null) {
                    for (PendingSurveillanceNonconformityEntity pnc : preq.getNonconformities()) {
                        SurveillanceNonconformity nc = new SurveillanceNonconformity();
                        nc.setCapApprovalDate(pnc.getCapApproval());
                        nc.setCapEndDate(pnc.getCapEndDate());
                        nc.setCapMustCompleteDate(pnc.getCapMustCompleteDate());
                        nc.setCapStartDate(pnc.getCapStart());
                        nc.setDateOfDetermination(pnc.getDateOfDetermination());
                        nc.setDeveloperExplanation(pnc.getDeveloperExplanation());
                        nc.setFindings(pnc.getFindings());
                        nc.setId(pnc.getId());
                        nc.setNonconformityType(pnc.getType());
                        nc.setResolution(pnc.getResolution());
                        nc.setSitesPassed(pnc.getSitesPassed());
                        nc.setSummary(pnc.getSummary());
                        nc.setTotalSites(pnc.getTotalSites());
                        SurveillanceNonconformityStatus status = new SurveillanceNonconformityStatus();
                        status.setName(pnc.getStatus());
                        nc.setStatus(status);
                        req.getNonconformities().add(nc);
                    }
                }
                surv.getRequirements().add(req);
            }
        }

        if (pr.getValidation() != null && pr.getValidation().size() > 0) {
            for (PendingSurveillanceValidationEntity validation : pr.getValidation()) {
                if (validation.getMessageType() == ValidationMessageType.Error) {
                    surv.getErrorMessages().add(validation.getMessage());
                }
            }
        }
        return surv;
    }

    private SurveillanceNonconformityDocument convertToDomain(final SurveillanceNonconformityDocumentationEntity entity,
            final boolean getContents) {
        SurveillanceNonconformityDocument doc = new SurveillanceNonconformityDocument();
        doc.setId(entity.getId());
        doc.setFileType(entity.getFileType());
        doc.setFileName(entity.getFileName());
        if (getContents) {
            doc.setFileContents(entity.getFileData());
        }
        return doc;
    }

    private Surveillance convertToDomain(final SurveillanceEntity entity) {
        Surveillance surv = new Surveillance();
        surv.setId(entity.getId());
        surv.setFriendlyId(entity.getFriendlyId());
        surv.setStartDate(entity.getStartDate());
        surv.setEndDate(entity.getEndDate());
        surv.setRandomizedSitesUsed(entity.getNumRandomizedSites());
        surv.setAuthority(userPermissionDao.findById(entity.getUserPermissionId()).getAuthority());
        surv.setLastModifiedDate(entity.getLastModifiedDate());

        if (entity.getCertifiedProduct() != null) {
            CertifiedProductEntity cpEntity = entity.getCertifiedProduct();
            try {
                CertifiedProductDetailsDTO cpDto = cpDao.getDetailsById(cpEntity.getId());
                surv.setCertifiedProduct(new CertifiedProduct(cpDto));
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not find details for certified product " + cpEntity.getId());
            }
        } else {
            CertifiedProduct cp = new CertifiedProduct();
            cp.setId(entity.getCertifiedProductId());
            surv.setCertifiedProduct(cp);
        }

        if (entity.getSurveillanceType() != null) {
            SurveillanceType survType = new SurveillanceType();
            survType.setId(entity.getSurveillanceType().getId());
            survType.setName(entity.getSurveillanceType().getName());
            surv.setType(survType);
        } else {
            SurveillanceType survType = new SurveillanceType();
            survType.setId(entity.getSurveillanceTypeId());
            surv.setType(survType);
        }

        if (entity.getSurveilledRequirements() != null) {
            for (SurveillanceRequirementEntity reqEntity : entity.getSurveilledRequirements()) {
                SurveillanceRequirement req = new SurveillanceRequirement();
                req.setId(reqEntity.getId());
                if (reqEntity.getCertificationCriterionEntity() != null) {
                    req.setRequirement(reqEntity.getCertificationCriterionEntity().getNumber());
                } else {
                    req.setRequirement(reqEntity.getSurveilledRequirement());
                }

                if (reqEntity.getSurveillanceResultTypeEntity() != null) {
                    SurveillanceResultType result = new SurveillanceResultType();
                    result.setId(reqEntity.getSurveillanceResultTypeEntity().getId());
                    result.setName(reqEntity.getSurveillanceResultTypeEntity().getName());
                    req.setResult(result);
                } else {
                    SurveillanceResultType result = new SurveillanceResultType();
                    result.setId(reqEntity.getSurveillanceResultTypeId());
                    req.setResult(result);
                }

                if (reqEntity.getSurveillanceRequirementType() != null) {
                    SurveillanceRequirementType result = new SurveillanceRequirementType();
                    result.setId(reqEntity.getSurveillanceRequirementType().getId());
                    result.setName(reqEntity.getSurveillanceRequirementType().getName());
                    req.setType(result);
                } else {
                    SurveillanceRequirementType result = new SurveillanceRequirementType();
                    result.setId(reqEntity.getSurveillanceRequirementTypeId());
                    req.setType(result);
                }

                if (reqEntity.getNonconformities() != null) {
                    for (SurveillanceNonconformityEntity ncEntity : reqEntity.getNonconformities()) {
                        SurveillanceNonconformity nc = new SurveillanceNonconformity();
                        nc.setCapApprovalDate(ncEntity.getCapApproval());
                        nc.setCapEndDate(ncEntity.getCapEndDate());
                        nc.setCapMustCompleteDate(ncEntity.getCapMustCompleteDate());
                        nc.setCapStartDate(ncEntity.getCapStart());
                        nc.setDateOfDetermination(ncEntity.getDateOfDetermination());
                        nc.setDeveloperExplanation(ncEntity.getDeveloperExplanation());
                        nc.setFindings(ncEntity.getFindings());
                        nc.setId(ncEntity.getId());
                        nc.setNonconformityType(ncEntity.getType());
                        nc.setResolution(ncEntity.getResolution());
                        nc.setSitesPassed(ncEntity.getSitesPassed());
                        nc.setSummary(ncEntity.getSummary());
                        nc.setTotalSites(ncEntity.getTotalSites());
                        nc.setLastModifiedDate(ncEntity.getLastModifiedDate());
                        if (ncEntity.getNonconformityStatus() != null) {
                            SurveillanceNonconformityStatus status = new SurveillanceNonconformityStatus();
                            status.setId(ncEntity.getNonconformityStatus().getId());
                            status.setName(ncEntity.getNonconformityStatus().getName());
                            nc.setStatus(status);
                        } else {
                            SurveillanceNonconformityStatus status = new SurveillanceNonconformityStatus();
                            status.setId(ncEntity.getNonconformityStatusId());
                            nc.setStatus(status);
                        }
                        req.getNonconformities().add(nc);

                        if (ncEntity.getDocuments() != null && ncEntity.getDocuments().size() > 0) {
                            for (SurveillanceNonconformityDocumentationEntity docEntity : ncEntity.getDocuments()) {
                                SurveillanceNonconformityDocument doc = convertToDomain(docEntity, false);
                                nc.getDocuments().add(doc);
                            }
                        }
                    }
                }
                surv.getRequirements().add(req);
            }
        }
        return surv;
    }

    private void checkSurveillanceAuthority(final Surveillance surv) throws SurveillanceAuthorityAccessDeniedException {
        Boolean hasOncAdmin = Util.isUserRoleAdmin() || Util.isUserRoleOnc();
        Boolean hasAcbAdmin = Util.isUserRoleAcbAdmin();
        if (StringUtils.isEmpty(surv.getAuthority())) {
            // If user has ROLE_ADMIN and ROLE_ACB
            // return 403
            if (hasOncAdmin && hasAcbAdmin) {
                String errorMsg = "Surveillance cannot be created by user having " + Authority.ROLE_ADMIN + " and "
                        + Authority.ROLE_ACB;
                LOGGER.error(errorMsg);
                throw new SurveillanceAuthorityAccessDeniedException(errorMsg);
            }
        } else {
            // Cannot have surveillance authority as ROLE_ADMIN for user lacking
            // ROLE_ADMIN
            if (surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ADMIN) && !hasOncAdmin) {
                String errorMsg = "User must have authority " + Authority.ROLE_ADMIN;
                LOGGER.error(errorMsg);
                throw new SurveillanceAuthorityAccessDeniedException(errorMsg);
            } else if (surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ACB)) {
                // Cannot have surveillance authority as ACB for user lacking ONC
                // and ACB roles
                if (!hasOncAdmin && !hasAcbAdmin) {
                    String errorMsg = "User must have ONC or ACB roles for a surveillance authority created by ACB";
                    LOGGER.error(errorMsg);
                    throw new SurveillanceAuthorityAccessDeniedException(errorMsg);
                }
            }
        }
    }

    private void updateNullAuthority(final Surveillance surv) {
        Boolean hasOncAdmin = Util.isUserRoleAdmin() || Util.isUserRoleOnc();
        Boolean hasAcbAdmin = Util.isUserRoleAcbAdmin();
        if (StringUtils.isEmpty(surv.getAuthority())) {
            if (hasOncAdmin) {
                surv.setAuthority(Authority.ROLE_ADMIN);
            } else if (hasAcbAdmin) {
                surv.setAuthority(Authority.ROLE_ACB);
            }
        }
    }
}
