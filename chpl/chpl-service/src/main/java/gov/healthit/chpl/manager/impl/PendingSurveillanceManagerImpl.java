package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.Job;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.SurveillanceUploadResult;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.domain.concept.JobTypeConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.entity.ValidationMessageType;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceRequirementEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceValidationEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.JobManager;
import gov.healthit.chpl.manager.PendingSurveillanceManager;
import gov.healthit.chpl.manager.SurveillanceUploadManager;
import gov.healthit.chpl.permissions.Permissions;
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
    private SurveillanceUploadManager survUploadManager;
    private JobManager jobManager;
    private UserManager userManager;
    private CertifiedProductManager cpManager;
    private SurveillanceValidator survValidator;
    private SurveillanceDAO survDao;
    private UserDAO userDao;
    private ActivityManager activityManager;
    private CertificationBodyManager acbManager;

    public PendingSurveillanceManagerImpl(Permissions permissions, Environment env, FileUtils fileUtils,
            SurveillanceUploadManager survUploadManager, JobManager jobManager, UserManager userManager,
            CertifiedProductManager cpManager, SurveillanceValidator survValidator, SurveillanceDAO survDao,
            UserDAO userDao, ActivityManager activityManager, CertificationBodyManager acbManager) {
        this.env = env;
        this.permissions = permissions;
        this.fileUtils = fileUtils;
        this.survUploadManager = survUploadManager;
        this.jobManager = jobManager;
        this.userManager = userManager;
        this.cpManager = cpManager;
        this.survValidator = survValidator;
        this.survDao = survDao;
        this.userDao = userDao;
        this.activityManager = activityManager;
        this.acbManager = acbManager;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domain.PendingSurveillanceDomainPermissions).GET_BY_ACB)")
    public SurveillanceUploadResult uploadPendingSurveillance(final MultipartFile file) throws ValidationException, EntityCreationException, EntityRetrievalException {
        if (file.isEmpty()) {
            throw new ValidationException("You cannot upload an empty file!");
        }

        if (!file.getContentType().equalsIgnoreCase("text/csv")
                && !file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            throw new ValidationException("File must be a CSV document.");
        }

        String surveillanceThresholdToProcessAsJobStr = env.getProperty("surveillanceThresholdToProcessAsJob").trim();
        Integer surveillanceThresholdToProcessAsJob = SURV_THRESHOLD_DEFAULT;
        try {
            surveillanceThresholdToProcessAsJob = Integer.parseInt(surveillanceThresholdToProcessAsJobStr);
        } catch (final NumberFormatException ex) {
            LOGGER.error(
                    "Could not format " + surveillanceThresholdToProcessAsJobStr + " as an integer. Defaulting to"
                            + " 50 instead.");
        }

        //first we need to count how many surveillance records are in the file
        //to know if we handle it normally or as a background job
        String data = fileUtils.readFileAsString(file);

        SurveillanceUploadResult uploadResult = new SurveillanceUploadResult();

        int numSurveillance = survUploadManager.countSurveillanceRecords(data);
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
            + "T(gov.healthit.chpl.permissions.domain.PendingSurveillanceDomainPermissions).DELETE, "
            + "#pendingSurveillanceId)")
    public void rejectPendingSurveillance(Long pendingSurveillanceId)
            throws ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException, EntityCreationException {

        deletePendingSurveillance(pendingSurveillanceId, false);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domain.PendingSurveillanceDomainPermissions).GET_ALL)")
    public List<Surveillance> getAllPendingSurveillances() {
        List<CertificationBodyDTO> acbs = acbManager.getAllForUser();
        List<Surveillance> pendingSurvs = new ArrayList<Surveillance>();

        if (acbs != null) {
            for (CertificationBodyDTO acb : acbs) {
                try {
                    List<Surveillance> survsOnAcb = getPendingByAcb(acb.getId());
                    pendingSurvs.addAll(survsOnAcb);
                } catch (final AccessDeniedException denied) {
                    LOGGER.warn("Access denied to pending surveillance for acb " + acb.getName() + " and user "
                            + Util.getUsername());
                }
            }
        }
        return pendingSurvs;
    }

    private SurveillanceUploadResult processUploadAsJob(String data) throws EntityCreationException, EntityRetrievalException {
        SurveillanceUploadResult result = new SurveillanceUploadResult();

        //figure out the user
        UserDTO currentUser = null;
        try {
            currentUser = userManager.getById(Util.getCurrentUser().getId());
        } catch (final UserRetrievalException ex) {
            LOGGER.error("Error finding user with ID " + Util.getCurrentUser().getId() + ": " + ex.getMessage());
            result.setJobStatus(SurveillanceUploadResult.UNAUTHORIZED);;
            return result;
        }
        if (currentUser == null) {
            LOGGER.error("No user with ID " + Util.getCurrentUser().getId() + " could be found in the system.");
            result.setJobStatus(SurveillanceUploadResult.UNAUTHORIZED);;
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
        result.setJobStatus(SurveillanceUploadResult.SUCCESS);
        return result;
    }

    private SurveillanceUploadResult processAsFile(final MultipartFile file) throws ValidationException {
        SurveillanceUploadResult result = new SurveillanceUploadResult();

        //process as normal
        List<Surveillance> uploadedSurveillance = new ArrayList<Surveillance>();
        List<Surveillance> pendingSurvs = survUploadManager.parseUploadFile(file);
        for (Surveillance surv : pendingSurvs) {
            CertifiedProductDTO owningCp = null;
            try {
                owningCp = cpManager.getById(surv.getCertifiedProduct().getId());
                survValidator.validate(surv);
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

    private void deletePendingSurveillance(final Long pendingSurveillanceId, boolean isConfirmed)
            throws ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException, EntityCreationException {
        PendingSurveillanceEntity surv = survDao.getPendingSurveillanceById(pendingSurveillanceId, true);
        CertifiedProductEntity ownerCp = surv.getCertifiedProduct();
        if (ownerCp == null) {
            throw new EntityNotFoundException("Could not find certified product associated with pending surveillance.");
        }

        Surveillance toDelete = getPendingById(pendingSurveillanceId, true);

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

    private boolean isPendingSurveillanceAvailableForUpdate(final Long acbId,
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
























    @Transactional(readOnly = true)
    //@PreAuthorize("hasRole('ROLE_ACB') "
    //        + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domain.PendingSurveillanceDomainPermissions).GET_BY_ACB, #acbId)")
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
}

