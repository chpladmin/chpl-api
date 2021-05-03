package gov.healthit.chpl.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.domain.surveillance.SurveillanceUploadResult;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
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
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;
import gov.healthit.chpl.scheduler.job.SplitDeveloperJob;
import gov.healthit.chpl.scheduler.job.SurveillanceUploadJob;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.validation.surveillance.SurveillanceCreationValidator;
import gov.healthit.chpl.validation.surveillance.SurveillanceUpdateValidator;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class PendingSurveillanceManager extends SecuredManager {
    private FileUtils fileUtils;
    private SurveillanceUploadManager survUploadHelper;
    private SchedulerManager schedulerManager;
    private UserManager userManager;
    private CertifiedProductManager cpManager;
    private SurveillanceDAO survDao;
    private UserDAO userDao;
    private ActivityManager activityManager;
    private CertifiedProductDetailsManager cpDetailsManager;
    private SurveillanceCreationValidator survCreationValidator;
    private SurveillanceUpdateValidator survUpdateValidator;
    private UserPermissionDAO userPermissionDAO;
    private CertifiedProductDAO cpDAO;
    private Integer surveillanceThresholdToProcessAsJob;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public PendingSurveillanceManager(Environment env, FileUtils fileUtils,
            SurveillanceUploadManager survUploadManager, SchedulerManager schedulerManager,
            UserManager userManager, CertifiedProductManager cpManager, SurveillanceDAO survDao, UserDAO userDao,
            ActivityManager activityManager, CertifiedProductDetailsManager cpDetailsManager,
            SurveillanceCreationValidator survCreationValidator,
            @Qualifier("surveillanceUpdateValidator") SurveillanceUpdateValidator survUpdateValidator,
            UserPermissionDAO userPermissionDAO, CertifiedProductDAO cpDAO,
            @Value("${surveillanceThresholdToProcessAsJob}") Integer surveillanceThresholdToProcessAsJob) {
        this.fileUtils = fileUtils;
        this.survUploadHelper = survUploadManager;
        this.schedulerManager = schedulerManager;
        this.userManager = userManager;
        this.cpManager = cpManager;
        this.survDao = survDao;
        this.userDao = userDao;
        this.activityManager = activityManager;
        this.cpDetailsManager = cpDetailsManager;
        this.survCreationValidator = survCreationValidator;
        this.survUpdateValidator = survUpdateValidator;
        this.userPermissionDAO = userPermissionDAO;
        this.cpDAO = cpDAO;
        this.surveillanceThresholdToProcessAsJob = surveillanceThresholdToProcessAsJob;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).UPLOAD)")
    public SurveillanceUploadResult uploadPendingSurveillance(MultipartFile file)
            throws ValidationException, EntityCreationException, EntityRetrievalException,
            IOException, SchedulerException {
        if (file.isEmpty()) {
            throw new ValidationException("You cannot upload an empty file!");
        }

        if (!file.getContentType().equalsIgnoreCase("text/csv")
                && !file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            throw new ValidationException("File must be a CSV document.");
        }

        // first we need to count how many surveillance records are in the file
        // to know if we handle it normally or as a background job
        String data = fileUtils.readFileAsString(file);
        checkFileCanBeReadAndMultipleRowsExist(data);

        // This is a container used for 2 different result types...
        SurveillanceUploadResult uploadResult = new SurveillanceUploadResult();
        int numSurveillance = survUploadHelper.countSurveillanceRecords(data);
        if (numSurveillance < surveillanceThresholdToProcessAsJob) {
            uploadResult = processAsFile(file);
        } else {
            ChplOneTimeTrigger scheduledTrigger = processUploadAsJob(data);
            uploadResult.setTrigger(scheduledTrigger);
        }
        return uploadResult;
    }

    private void checkFileCanBeReadAndMultipleRowsExist(String fileContents) throws IOException, ValidationException  {
        try (BufferedReader reader = new BufferedReader(new StringReader(fileContents));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)) {

            List<CSVRecord> records = parser.getRecords();
            if (records.size() <= 1) {
                String msg = "The file appears to have a header line with no other information. "
                        + "Please make sure there are at least two rows in the CSV file.";
                throw new ValidationException(msg);
            }
        } catch (IOException ex) {
            LOGGER.error("Cannot read file as CSV: " + ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).REJECT, "
            + "#pendingSurveillanceId)")
    public void rejectPendingSurveillance(Long pendingSurveillanceId) throws ObjectMissingValidationException,
            JsonProcessingException, EntityRetrievalException, EntityCreationException {

        PendingSurveillanceEntity entity = survDao.getPendingSurveillanceById(pendingSurveillanceId, true);
        if (entity.getDeleted()) {
            throw createdObjectMissingValidationException(entity);
        }
        deletePendingSurveillance(pendingSurveillanceId, false);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).GET_ALL)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).GET_ALL, filterObject)")
    public List<Surveillance> getAllPendingSurveillances() {
        List<PendingSurveillanceEntity> pendingResults = survDao.getAllPendingSurveillance();
        List<Surveillance> results = new ArrayList<Surveillance>();
        if (pendingResults != null) {
            for (PendingSurveillanceEntity pr : pendingResults) {
                Surveillance surv = convertToDomain(pr);
                results.add(surv);
            }
        }
        return results;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).CONFIRM, "
            + "#survToInsert)")
    public Surveillance confirmPendingSurveillance(Surveillance survToInsert)
            throws ValidationException, EntityRetrievalException, UserPermissionRetrievalException,
            SurveillanceAuthorityAccessDeniedException, EntityCreationException, JsonProcessingException {

        if (survToInsert == null || survToInsert.getId() == null) {
            throw new ValidationException("A valid pending surveillance id must be provided.");
        } else {
            survToInsert.getErrorMessages().clear();
        }

        // the confirmation could be an update to an existing surveillance.
        //if so, find the existing surveillane that's being updated
        Surveillance existingSurveillance = null;
        if (!StringUtils.isEmpty(survToInsert.getSurveillanceIdToReplace())) {
            existingSurveillance = getByFriendlyIdAndListing(
                    survToInsert.getCertifiedProduct().getId(),
                    survToInsert.getSurveillanceIdToReplace());
        }

        CertifiedProductSearchDetails beforeCp = cpDetailsManager
                .getCertifiedProductDetails(survToInsert.getCertifiedProduct().getId());

        Long pendingSurvToDelete = survToInsert.getId();
        if (!isPendingSurveillanceAvailableForUpdate(pendingSurvToDelete)) {
            return null;
        }

        if (existingSurveillance != null) {
            survUpdateValidator.validate(existingSurveillance, survToInsert);
        } else {
            survCreationValidator.validate(survToInsert);
        }
        if (survToInsert.getErrorMessages() != null && survToInsert.getErrorMessages().size() > 0) {
            throw new ValidationException(survToInsert.getErrorMessages(), null);
        }

        Long insertedSurvId = createSurveillance(survToInsert);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        if (insertedSurvId == null) {
            throw new EntityCreationException("Error creating new surveillance.");
        }

        // delete the pending surveillance item if this one was successfully inserted
        try {
            deletePendingSurveillance(pendingSurvToDelete, true);
        } catch (Exception ex) {
            LOGGER.error("Error deleting pending surveillance with id " + pendingSurvToDelete, ex);
        }

        // if a surveillance was getting replaced, delete it
        try {
            if (existingSurveillance != null) {
                deleteSurveillance(existingSurveillance);
            }
        } catch (Exception ex) {
            LOGGER.error("Deleting surveillance with id " + survToInsert.getSurveillanceIdToReplace()
                    + " as part of the replace operation failed", ex);
        }

        CertifiedProductSearchDetails afterCp = cpDetailsManager
                .getCertifiedProductDetails(survToInsert.getCertifiedProduct().getId());

        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, afterCp.getId(),
                "Surveillance upload was confirmed for certified product " + afterCp.getChplProductNumber(), beforeCp,
                afterCp);

        // query the inserted surveillance
        SurveillanceEntity insertedSurv = survDao.getSurveillanceById(insertedSurvId);
        Surveillance result = convertToDomain(insertedSurv);
        return result;
    }

    private ChplOneTimeTrigger processUploadAsJob(String data) throws EntityCreationException,
        EntityRetrievalException, ValidationException, SchedulerException {
        UserDTO jobUser = null;
        try {
            jobUser = userManager.getById(AuthUtil.getCurrentUser().getId());
        } catch (UserRetrievalException ex) {
            LOGGER.error("Could not find user to execute job.");
        }

        ChplOneTimeTrigger uploadSurveillanceTrigger = new ChplOneTimeTrigger();
        ChplJob uploadSurveillanceJob = new ChplJob();
        uploadSurveillanceJob.setName(SurveillanceUploadJob.JOB_NAME);
        uploadSurveillanceJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SurveillanceUploadJob.FILE_CONTENTS_KEY, data);
        jobDataMap.put(SplitDeveloperJob.USER_KEY, jobUser);
        uploadSurveillanceJob.setJobDataMap(jobDataMap);
        uploadSurveillanceTrigger.setJob(uploadSurveillanceJob);
        uploadSurveillanceTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.DELAY_BEFORE_BACKGROUND_JOB_START);
        uploadSurveillanceTrigger = schedulerManager.createBackgroundJobTrigger(uploadSurveillanceTrigger);
        return uploadSurveillanceTrigger;
    }

    private SurveillanceUploadResult processAsFile(MultipartFile file) throws ValidationException {
        SurveillanceUploadResult result = new SurveillanceUploadResult();
        List<Surveillance> uploadedSurveillance = new ArrayList<Surveillance>();
        List<Surveillance> pendingSurvs = survUploadHelper.parseUploadFile(file);

        for (Surveillance surv : pendingSurvs) {
            CertifiedProductDTO owningCp = null;
            try {
                owningCp = cpManager.getById(surv.getCertifiedProduct().getId());
                validateSurveillanceCreation(surv);
                Long pendingId = createPendingSurveillance(surv);
                Surveillance uploaded = getPendingById(pendingId);
                uploadedSurveillance.add(uploaded);
            } catch (AccessDeniedException denied) {
                LOGGER.error("User " + AuthUtil.getCurrentUser().getSubjectName()
                        + " does not have access to add surveillance"
                        + (owningCp != null ? " to ACB with ID '" + owningCp.getCertificationBodyId() + "'." : "."));
            } catch (Exception ex) {
                LOGGER.error("Error adding a new pending surveillance. Please make sure all required fields are "
                        + "present.", ex);
            }
        }
        result.setSurveillances(uploadedSurveillance);
        return result;
    }

    private Long createPendingSurveillance(Surveillance surv) {
        Long insertedId = null;
        try {
            insertedId = survDao.insertPendingSurveillance(surv);
        } catch (Exception ex) {
            LOGGER.error("Error inserting pending surveillance.", ex);
        }
        return insertedId;
    }

    private Surveillance getPendingById(Long survId) throws EntityRetrievalException {
        PendingSurveillanceEntity pending = survDao.getPendingSurveillanceById(survId);
        Surveillance surv = convertToDomain(pending);
        return surv;
    }

    private Surveillance convertToDomain(PendingSurveillanceEntity pr) {
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
                if (preq.getCertificationCriterionEntity() != null) {
                    req.setCriterion(convertToDomain(preq.getCertificationCriterionEntity()));
                }
                SurveillanceResultType result = new SurveillanceResultType();
                result.setName(preq.getResult());
                req.setResult(result);
                SurveillanceRequirementType reqType = new SurveillanceRequirementType();
                reqType.setName(preq.getRequirementType());
                req.setType(reqType);

                CertifiedProduct cp = new CertifiedProduct();
                cp.setId(pr.getCertifiedProductId());
                cp.setChplProductNumber(pr.getCertifiedProductUniqueId());
                cp.setEdition(pr.getCertifiedProduct().getYear());
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
                        if (pnc.getCertificationCriterionEntity() != null) {
                            nc.setCriterion(convertToDomain(pnc.getCertificationCriterionEntity()));
                        }
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

    private void deletePendingSurveillance(Long pendingSurveillanceId, boolean isConfirmed)
            throws ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException,
            EntityCreationException {

        PendingSurveillanceEntity surv = survDao.getPendingSurveillanceById(pendingSurveillanceId);
        Surveillance toDelete = getPendingById(pendingSurveillanceId);

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
            activityManager.addActivity(ActivityConcept.PENDING_SURVEILLANCE, toDelete.getId(),
                    activityMsg.toString(), toDelete, null);
        }

    }

    private boolean isPendingSurveillanceAvailableForUpdate(Long pendingSurvId)
            throws EntityRetrievalException, ObjectMissingValidationException {
        PendingSurveillanceEntity pendingSurv = survDao.getPendingSurveillanceById(pendingSurvId);
        return isPendingSurveillanceAvailableForUpdate(pendingSurv);
    }

    private boolean isPendingSurveillanceAvailableForUpdate(PendingSurveillanceEntity pendingSurv)
            throws EntityRetrievalException, ObjectMissingValidationException {
        if (pendingSurv.getDeleted()) {
            ObjectMissingValidationException alreadyDeletedEx = new ObjectMissingValidationException();
            alreadyDeletedEx.getErrorMessages()
                    .add("This pending surveillance has already been confirmed or rejected by another user.");
            alreadyDeletedEx.setObjectId(pendingSurv.getId().toString());
            alreadyDeletedEx.setStartDate(pendingSurv.getStartDate());
            alreadyDeletedEx.setEndDate(pendingSurv.getEndDate());

            try {
                UserDTO lastModifiedUserDto = userDao.getById(pendingSurv.getLastModifiedUser());
                if (lastModifiedUserDto != null) {
                    User lastModifiedUser = new User(lastModifiedUserDto);
                    alreadyDeletedEx.setUser(lastModifiedUser);
                } else {
                    alreadyDeletedEx.setUser(null);
                }
            } catch (UserRetrievalException ex) {
                alreadyDeletedEx.setUser(null);
            }
            throw alreadyDeletedEx;
        }
        // If pendingSurv were null, we would have gotten an NPE by this point
        // return pendingSurv != null;
        return true;
    }

    private void validateSurveillanceCreation(Surveillance createdSurv) {
        createdSurv.getErrorMessages().clear();
        survCreationValidator.validate(createdSurv);
    }

    private Long createSurveillance(Surveillance surv)
            throws UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
        Long insertedId = null;

        try {
            insertedId = survDao.insertSurveillance(surv);
        } catch (UserPermissionRetrievalException ex) {
            LOGGER.error("Error inserting surveillance.", ex);
            throw ex;
        }

        return insertedId;
    }

    private Surveillance getByFriendlyIdAndListing(Long certifiedProductId, String survFriendlyId) {
        SurveillanceEntity surv = survDao.getSurveillanceByCertifiedProductAndFriendlyId(certifiedProductId,
                survFriendlyId);
        if (surv == null) {
            throw new EntityNotFoundException("Could not find surveillance for certified product " + certifiedProductId
                    + " with friendly id " + survFriendlyId);
        }
        Surveillance result = convertToDomain(surv);
        return result;
    }

    private Surveillance convertToDomain(SurveillanceEntity entity) {
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
            } catch (EntityRetrievalException ex) {
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

    private SurveillanceNonconformityDocument convertToDomain(SurveillanceNonconformityDocumentationEntity entity,
            boolean getContents) {
        SurveillanceNonconformityDocument doc = new SurveillanceNonconformityDocument();
        doc.setId(entity.getId());
        doc.setFileType(entity.getFileType());
        doc.setFileName(entity.getFileName());
        if (getContents) {
            doc.setFileContents(entity.getFileData());
        }
        return doc;
    }

    private CertificationCriterion convertToDomain(CertificationCriterionEntity entity) {
        CertificationCriterion cc = new CertificationCriterion();
        cc.setId(entity.getId());
        cc.setCertificationEditionId(entity.getCertificationEditionId());
        cc.setCertificationEdition(entity.getCertificationEdition().getYear());
        cc.setDescription(entity.getDescription());
        cc.setNumber(entity.getNumber());
        cc.setRemoved(entity.getRemoved());
        cc.setTitle(entity.getTitle());
        return cc;
    }

    private void deleteSurveillance(Surveillance surv)
            throws EntityRetrievalException, SurveillanceAuthorityAccessDeniedException {
        survDao.deleteSurveillance(surv);
    }

    private ObjectMissingValidationException createdObjectMissingValidationException(PendingSurveillanceEntity entity) {
        ObjectMissingValidationException alreadyDeletedEx = new ObjectMissingValidationException();
        alreadyDeletedEx.getErrorMessages()
                .add("This pending surveillance has already been confirmed or rejected by another user.");
        alreadyDeletedEx.setObjectId(entity.getId().toString());
        alreadyDeletedEx.setStartDate(entity.getStartDate());
        alreadyDeletedEx.setEndDate(entity.getEndDate());
        try {
            UserDTO lastModifiedUserDto = userDao.getById(entity.getLastModifiedUser());
            if (lastModifiedUserDto != null) {
                User lastModifiedUser = new User(lastModifiedUserDto);
                alreadyDeletedEx.setUser(lastModifiedUser);
            } else {
                alreadyDeletedEx.setUser(null);
            }
        } catch (UserRetrievalException ex) {
            alreadyDeletedEx.setUser(null);
        }
        return alreadyDeletedEx;
    }
}
