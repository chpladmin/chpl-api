package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.Job;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.SurveillanceUploadResult;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.domain.concept.JobTypeConcept;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
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
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.JobManager;
import gov.healthit.chpl.manager.PendingSurveillanceManager;
import gov.healthit.chpl.manager.SurveillanceUploadManager;
import gov.healthit.chpl.permissions.Permissions;
import gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.validation.surveillance.SurveillanceValidator;

@Component
public class PendingSurveillanceManagerImpl implements PendingSurveillanceManager {
    private static final Logger LOGGER = LogManager.getLogger(PendingSurveillanceManagerImpl.class);
    private static final int SURV_THRESHOLD_DEFAULT = 50;
    private final JobTypeConcept allowedJobType = JobTypeConcept.SURV_UPLOAD;

    private Environment env;
    private Permissions permissions;
    private FileUtils fileUtils;
    private SurveillanceUploadManager survUploadHelper;
    private JobManager jobManager;
    private UserManager userManager;
    private CertifiedProductManager cpManager;
    private SurveillanceValidator survValidator;
    private SurveillanceDAO survDao;
    private UserDAO userDao;
    private ActivityManager activityManager;
    private CertifiedProductDetailsManager cpDetailsManager;
    private SurveillanceValidator validator;
    private UserPermissionDAO userPermissionDAO;
    private CertifiedProductDAO cpDAO;

    @Autowired
    public PendingSurveillanceManagerImpl(final Permissions permissions, final Environment env, final FileUtils fileUtils,
            final SurveillanceUploadManager survUploadManager, final JobManager jobManager, final UserManager userManager,
            final CertifiedProductManager cpManager, final SurveillanceValidator survValidator, final SurveillanceDAO survDao,
            final UserDAO userDao, final ActivityManager activityManager, final CertifiedProductDetailsManager cpDetailsManager,
            final SurveillanceValidator validator, final UserPermissionDAO userPermissionDAO, final CertifiedProductDAO cpDAO) {
        this.env = env;
        this.permissions = permissions;
        this.fileUtils = fileUtils;
        this.survUploadHelper = survUploadManager;
        this.jobManager = jobManager;
        this.userManager = userManager;
        this.cpManager = cpManager;
        this.survValidator = survValidator;
        this.survDao = survDao;
        this.userDao = userDao;
        this.activityManager = activityManager;
        this.cpDetailsManager = cpDetailsManager;
        this.validator = validator;
        this.userPermissionDAO = userPermissionDAO;
        this.cpDAO = cpDAO;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).UPLOAD)")
    public SurveillanceUploadResult uploadPendingSurveillance(final MultipartFile file)
            throws ValidationException, EntityCreationException, EntityRetrievalException {
        if (file.isEmpty()) {
            throw new ValidationException("You cannot upload an empty file!");
        }

        if (!file.getContentType().equalsIgnoreCase("text/csv")
                && !file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            throw new ValidationException("File must be a CSV document.");
        }

        Integer surveillanceThresholdToProcessAsJob = getSurveillanceRecordThreshold();

        //first we need to count how many surveillance records are in the file
        //to know if we handle it normally or as a background job
        String data = fileUtils.readFileAsString(file);

        //This is a container used for 2 different result types...
        SurveillanceUploadResult uploadResult = new SurveillanceUploadResult();

        int numSurveillance = survUploadHelper.countSurveillanceRecords(data);
        if (numSurveillance < surveillanceThresholdToProcessAsJob) {
            uploadResult = processAsFile(file);
        } else { //process as job
            uploadResult = processUploadAsJob(data);
        }
        return uploadResult;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).REJECT, "
            + "#pendingSurveillanceId)")
    public void rejectPendingSurveillance(final Long pendingSurveillanceId)
            throws ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException, EntityCreationException {

        deletePendingSurveillance(pendingSurveillanceId, false);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).GET_ALL)")
    //@PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
    //        + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).GET_ALL, filterObject)")
    public List<Surveillance> getAllPendingSurveillances() {
        List<PendingSurveillanceEntity> pendingResults = survDao.getAllPendingSurveillance();
        List<Surveillance> results = new ArrayList<Surveillance>();
        if (pendingResults != null) {
            for (PendingSurveillanceEntity pr : pendingResults) {
                Surveillance surv = convertToDomain(pr);
                results.add(surv);
            }
        }

        //TODO: There is a problem using @PostFilter - some sort of ACL issue
        //Until that is figured out...
        Iterator<Surveillance> iterator = results.iterator();
        while (iterator.hasNext()) {
            if (!permissions.hasAccess(
                    Permissions.PENDING_SURVEILLANCE,
                    PendingSurveillanceDomainPermissions.GET_ALL,
                    iterator.next())) {
                iterator.remove();
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).CONFIRM, "
            + "#survToInsert)")
    public Surveillance confirmPendingSurveillance(final Surveillance survToInsert)
            throws ValidationException, EntityRetrievalException, UserPermissionRetrievalException,
            SurveillanceAuthorityAccessDeniedException, EntityCreationException, JsonProcessingException {

        if (survToInsert == null || survToInsert.getId() == null) {
            throw new ValidationException("A valid pending surveillance id must be provided.");
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        CertifiedProductSearchDetails beforeCp =
                cpDetailsManager.getCertifiedProductDetails(survToInsert.getCertifiedProduct().getId());

        Long pendingSurvToDelete = survToInsert.getId();
        if (!isPendingSurveillanceAvailableForUpdate(pendingSurvToDelete)) {
            return null;
        }

        survToInsert.getErrorMessages().clear();

        // validate first. this ensures we have all the info filled in
        // that we need to continue
        validate(survToInsert);
        if (survToInsert.getErrorMessages() != null && survToInsert.getErrorMessages().size() > 0) {
            throw new ValidationException(survToInsert.getErrorMessages(), null);
        }

        // insert or update the surveillance
        Long insertedSurv = createSurveillance(survToInsert);
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        if (insertedSurv == null) {
            throw new EntityCreationException("Error creating new surveillance.");
        }

        // delete the pending surveillance item if this one was successfully
        // inserted
        try {
            deletePendingSurveillance(pendingSurvToDelete, true);
        } catch (Exception ex) {
            LOGGER.error("Error deleting pending surveillance with id " + pendingSurvToDelete, ex);
        }

        // if a surveillance was getting replaced, delete it
        try {
            if (!StringUtils.isEmpty(survToInsert.getSurveillanceIdToReplace())) {
                Surveillance survToReplace =
                        getByFriendlyIdAndProduct(
                                survToInsert.getCertifiedProduct().getId(),
                                survToInsert.getSurveillanceIdToReplace());
                deleteSurveillance(survToReplace);
            }
        } catch (Exception ex) {
            LOGGER.error("Deleting surveillance with id " + survToInsert.getSurveillanceIdToReplace()
            + " as part of the replace operation failed", ex);
        }

        CertifiedProductSearchDetails afterCp =
                cpDetailsManager.getCertifiedProductDetails(survToInsert.getCertifiedProduct().getId());

        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, afterCp.getId(),
                "Surveillance upload was confirmed for certified product " + afterCp.getChplProductNumber(),
                beforeCp, afterCp);

        // query the inserted surveillance
        Surveillance result = getSurveillanceById(insertedSurv);
        return result;
    }

    private SurveillanceUploadResult processUploadAsJob(final String data)
            throws EntityCreationException, EntityRetrievalException {
        SurveillanceUploadResult result = new SurveillanceUploadResult();

        //figure out the user
        UserDTO currentUser = null;
        try {
            currentUser = userManager.getById(Util.getCurrentUser().getId());
        } catch (final UserRetrievalException ex) {
            LOGGER.error("Error finding user with ID " + Util.getCurrentUser().getId() + ": " + ex.getMessage());
            result.setJobStatus(SurveillanceUploadResult.UNAUTHORIZED);
            return result;
        }
        if (currentUser == null) {
            LOGGER.error("No user with ID " + Util.getCurrentUser().getId() + " could be found in the system.");
            result.setJobStatus(SurveillanceUploadResult.UNAUTHORIZED);
            return result;
        }

        JobTypeDTO jobType = null;
        List<JobTypeDTO> jobTypes = jobManager.getAllJobTypes();
        for (JobTypeDTO jt : jobTypes) {
            if (jt.getName().equalsIgnoreCase(allowedJobType.getName())) {
                jobType = jt;
            }
        }

        JobDTO toCreate = new JobDTO();
        toCreate.setData(data);
        toCreate.setUser(currentUser);
        toCreate.setJobType(jobType);
        JobDTO insertedJob = jobManager.createJob(toCreate);
        JobDTO createdJob = jobManager.getJobById(insertedJob.getId());

        try {
            boolean isStarted = jobManager.start(createdJob);
            if (!isStarted) {
                result.setJob(new Job(createdJob));
                result.setJobStatus(SurveillanceUploadResult.NOT_STARTED);
                return result;
            } else {
                createdJob = jobManager.getJobById(insertedJob.getId());
            }
        } catch (final EntityRetrievalException ex) {
            LOGGER.error("Could not mark job " + createdJob.getId() + " as started.");
            result.setJob(new Job(createdJob));
            result.setJobStatus(SurveillanceUploadResult.ERROR);
            return result;

        }

        // query the now running job
        result.setJob(new Job(createdJob));
        result.setJobStatus(SurveillanceUploadResult.STARTED);
        return result;
    }

    private SurveillanceUploadResult processAsFile(final MultipartFile file) throws ValidationException {
        SurveillanceUploadResult result = new SurveillanceUploadResult();
        List<Surveillance> uploadedSurveillance = new ArrayList<Surveillance>();
        List<Surveillance> pendingSurvs = survUploadHelper.parseUploadFile(file);

        for (Surveillance surv : pendingSurvs) {
            CertifiedProductDTO owningCp = null;
            try {
                owningCp = cpManager.getById(surv.getCertifiedProduct().getId());
                survValidator.validate(surv, false);
                Long pendingId = createPendingSurveillance(surv);
                Surveillance uploaded = getPendingById(pendingId, false);
                uploadedSurveillance.add(uploaded);
            } catch (final AccessDeniedException denied) {
                LOGGER.error(
                        "User " + Util.getCurrentUser().getSubjectName()
                        + " does not have access to add surveillance"
                        + (owningCp != null
                        ? " to ACB with ID '" + owningCp.getCertificationBodyId() + "'."
                                : "."));
            } catch (Exception ex) {
                LOGGER.error(
                        "Error adding a new pending surveillance. Please make sure all required fields are "
                                + "present.",
                                ex);
            }
        }
        result.setSurveillances(uploadedSurveillance);
        return result;
    }

    private Long createPendingSurveillance(final Surveillance surv) {
        Long insertedId = null;
        try {
            insertedId = survDao.insertPendingSurveillance(surv);
        } catch (Exception ex) {
            LOGGER.error("Error inserting pending surveillance.", ex);
        }
        return insertedId;
    }

    private Surveillance getPendingById(final Long survId, final boolean includeDeleted)
            throws EntityRetrievalException {
        PendingSurveillanceEntity pending = survDao.getPendingSurveillanceById(survId, includeDeleted);
        Surveillance surv = convertToDomain(pending);
        return surv;
    }

    private Surveillance convertToDomain(final PendingSurveillanceEntity pr) {
        Surveillance surv = new Surveillance();
        surv.setId(pr.getId());
        surv.setSurveillanceIdToReplace(pr.getSurvFriendlyIdToReplace());
        surv.setStartDate(pr.getStartDate());
        surv.setEndDate(pr.getEndDate());
        surv.setRandomizedSitesUsed(pr.getNumRandomizedSites());
        surv.setAuthority(userPermissionDAO.findById(pr.getUserPermissionId()).getAuthority());

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

    private void deletePendingSurveillance(final Long pendingSurveillanceId, final boolean isConfirmed)
            throws ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException, EntityCreationException {

        PendingSurveillanceEntity surv = survDao.getPendingSurveillanceById(pendingSurveillanceId, true);
        Surveillance toDelete = getPendingById(pendingSurveillanceId, true);

        if (isPendingSurveillanceAvailableForUpdate(surv)) {
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

    private boolean isPendingSurveillanceAvailableForUpdate(final Long pendingSurvId)
            throws EntityRetrievalException, ObjectMissingValidationException {
        PendingSurveillanceEntity pendingSurv = survDao.getPendingSurveillanceById(pendingSurvId, true);
        return isPendingSurveillanceAvailableForUpdate(pendingSurv);
    }

    private boolean isPendingSurveillanceAvailableForUpdate(final PendingSurveillanceEntity pendingSurv)
            throws EntityRetrievalException, ObjectMissingValidationException {
        if (pendingSurv.getDeleted()) {
            ObjectMissingValidationException alreadyDeletedEx = new ObjectMissingValidationException();
            alreadyDeletedEx.getErrorMessages()
            .add("This pending surveillance has already been confirmed or rejected by another user.");
            alreadyDeletedEx.setObjectId(pendingSurv.getId().toString());
            alreadyDeletedEx.setStartDate(pendingSurv.getStartDate());
            alreadyDeletedEx.setEndDate(pendingSurv.getEndDate());

            try {
                UserDTO lastModifiedUser = userDao.getById(pendingSurv.getLastModifiedUser());
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

    private Integer getSurveillanceRecordThreshold() {
        String surveillanceThresholdToProcessAsJobStr = env.getProperty("surveillanceThresholdToProcessAsJob").trim();
        Integer surveillanceThresholdToProcessAsJob = SURV_THRESHOLD_DEFAULT;
        try {
            surveillanceThresholdToProcessAsJob = Integer.parseInt(surveillanceThresholdToProcessAsJobStr);
        } catch (final NumberFormatException ex) {
            LOGGER.error(
                    "Could not format " + surveillanceThresholdToProcessAsJobStr + " as an integer. Defaulting to"
                            + " 50 instead.");
        }
        return surveillanceThresholdToProcessAsJob;
    }

    private void validate(final Surveillance surveillance) {
        validator.validate(surveillance, false);
    }

    private void checkSurveillanceAuthority(final Surveillance surv) throws SurveillanceAuthorityAccessDeniedException {
        Boolean hasOncAdmin = Util.isUserRoleAdmin();
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

    private Long createSurveillance(final Surveillance surv)
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

    private void updateNullAuthority(final Surveillance surv) {
        Boolean hasOncAdmin = Util.isUserRoleAdmin();
        Boolean hasAcbAdmin = Util.isUserRoleAcbAdmin();
        if (StringUtils.isEmpty(surv.getAuthority())) {
            if (hasOncAdmin) {
                surv.setAuthority(Authority.ROLE_ADMIN);
            } else if (hasAcbAdmin) {
                surv.setAuthority(Authority.ROLE_ACB);
            }
        }
    }

    public Surveillance getByFriendlyIdAndProduct(final Long certifiedProductId, final String survFriendlyId) {
        SurveillanceEntity surv = survDao.getSurveillanceByCertifiedProductAndFriendlyId(certifiedProductId,
                survFriendlyId);
        if (surv == null) {
            throw new EntityNotFoundException("Could not find surveillance for certified product " + certifiedProductId
                    + " with friendly id " + survFriendlyId);
        }
        Surveillance result = convertToDomain(surv);
        validator.validate(result, false);
        return result;
    }

    private Surveillance convertToDomain(final SurveillanceEntity entity) {
        Surveillance surv = new Surveillance();
        surv.setId(entity.getId());
        surv.setFriendlyId(entity.getFriendlyId());
        surv.setStartDate(entity.getStartDate());
        surv.setEndDate(entity.getEndDate());
        surv.setRandomizedSitesUsed(entity.getNumRandomizedSites());
        surv.setAuthority(userPermissionDAO.findById(entity.getUserPermissionId()).getAuthority());
        surv.setLastModifiedDate(entity.getLastModifiedDate());

        if (entity.getCertifiedProduct() != null) {
            CertifiedProductEntity cpEntity = entity.getCertifiedProduct();
            try {
                CertifiedProductDetailsDTO cpDto = cpDAO.getDetailsById(cpEntity.getId());
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

    private void deleteSurveillance(final Surveillance surv)
            throws EntityRetrievalException, SurveillanceAuthorityAccessDeniedException {
        checkSurveillanceAuthority(surv);
        survDao.deleteSurveillance(surv);
    }

    private Surveillance getSurveillanceById(final Long survId) throws EntityRetrievalException {
        SurveillanceEntity surv = survDao.getSurveillanceById(survId);
        Surveillance result = convertToDomain(surv);
        validator.validate(result, false);
        return result;
    }
}

