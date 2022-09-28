package gov.healthit.chpl.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dao.surveillance.PendingSurveillanceDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.surveillance.RequirementDetailType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.domain.surveillance.SurveillanceUploadResult;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.ValidationMessageType;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceRequirementEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceValidationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.scheduler.job.SurveillanceUploadJob;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.util.NullSafeEvaluator;
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
    private PendingSurveillanceDAO pendingSurveillanceDAO;
    private SurveillanceDAO surveillanceDAO;
    private UserDAO userDao;
    private ActivityManager activityManager;
    private CertifiedProductDetailsManager cpDetailsManager;
    private SurveillanceCreationValidator survCreationValidator;
    private SurveillanceUpdateValidator survUpdateValidator;
    private CertifiedProductDAO cpDAO;
    private Integer surveillanceThresholdToProcessAsJob;
    private CertificationCriterionService certificationCriterionService;

    private List<RequirementDetailType> requirementDetailTypes;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public PendingSurveillanceManager(Environment env, FileUtils fileUtils,
            SurveillanceUploadManager survUploadManager, SchedulerManager schedulerManager,
            UserManager userManager, CertifiedProductManager cpManager, PendingSurveillanceDAO pendingSurveillanceDAO,
            SurveillanceDAO surveillanceDAO, UserDAO userDao, ActivityManager activityManager,
            CertifiedProductDetailsManager cpDetailsManager, SurveillanceCreationValidator survCreationValidator,
            @Qualifier("surveillanceUpdateValidator") SurveillanceUpdateValidator survUpdateValidator,
            CertifiedProductDAO cpDAO, @Value("${surveillanceThresholdToProcessAsJob}") Integer surveillanceThresholdToProcessAsJob,
            CertificationCriterionService certificationCriterionService) {
        this.fileUtils = fileUtils;
        this.survUploadHelper = survUploadManager;
        this.schedulerManager = schedulerManager;
        this.userManager = userManager;
        this.cpManager = cpManager;
        this.pendingSurveillanceDAO = pendingSurveillanceDAO;
        this.surveillanceDAO = surveillanceDAO;
        this.userDao = userDao;
        this.activityManager = activityManager;
        this.cpDetailsManager = cpDetailsManager;
        this.survCreationValidator = survCreationValidator;
        this.survUpdateValidator = survUpdateValidator;
        this.cpDAO = cpDAO;
        this.surveillanceThresholdToProcessAsJob = surveillanceThresholdToProcessAsJob;
        this.certificationCriterionService = certificationCriterionService;

        this.requirementDetailTypes = surveillanceDAO.getRequirementDetailTypes();
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

        //TODO OCD-4029
        /*
        PendingSurveillanceEntity entity = survDao.getPendingSurveillanceById(pendingSurveillanceId, true);
        if (entity.getDeleted()) {
            throw createdObjectMissingValidationException(entity);
        }
        deletePendingSurveillance(pendingSurveillanceId, false);
        */
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).GET_ALL)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions).GET_ALL, filterObject)")
    public List<Surveillance> getAllPendingSurveillances() {
        List<PendingSurveillanceEntity> pendingResults = pendingSurveillanceDAO.getAllPendingSurveillance();
        List<Surveillance> results = new ArrayList<Surveillance>();
        if (pendingResults != null) {
            for (PendingSurveillanceEntity pr : pendingResults) {
                //TODO - OCD-4029
                //Surveillance surv = convertToDomain(pr);
                //results.add(surv);
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
            EntityCreationException, JsonProcessingException {

        //TODO OCD-4029
        /*
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
        return insertedSurv.toDomain(cpDAO, certificationCriterionService);
        */
        return null;
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
        jobDataMap.put(SurveillanceUploadJob.USER_KEY, jobUser);
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


    @Transactional
    public Long createPendingSurveillance(Surveillance surv) {
        surv.getErrorMessages().clear();
        survCreationValidator.validate(surv);

        Long insertedId = null;
        try {
            insertedId = pendingSurveillanceDAO.insertPendingSurveillance(surv);
        } catch (Exception ex) {
            LOGGER.error("Error inserting pending surveillance.", ex);
        }
        return insertedId;
    }

    private Surveillance getPendingById(Long survId) throws EntityRetrievalException {
        PendingSurveillanceEntity pending = pendingSurveillanceDAO.getPendingSurveillanceById(survId);
        Surveillance surv = convertToDomain(pending);
        return surv;
    }

    private Surveillance convertToDomain(PendingSurveillanceEntity pr) {
        Surveillance surv = Surveillance.builder()
                .id(pr.getId())
                .surveillanceIdToReplace(pr.getSurvFriendlyIdToReplace())
                .startDay(pr.getStartDate())
                .endDay(pr.getEndDate())
                .randomizedSitesUsed(pr.getNumRandomizedSites())
                .type(SurveillanceType.builder()
                        .name(pr.getSurveillanceType())
                        .build())
                .certifiedProduct(CertifiedProduct.builder()
                        .id(pr.getCertifiedProductId())
                        .chplProductNumber(pr.getCertifiedProductUniqueId())
                        .edition(pr.getCertifiedProduct().getYear())
                        .build())
                .build();

        if (pr.getSurveilledRequirements() != null) {
            for (PendingSurveillanceRequirementEntity preq : pr.getSurveilledRequirements()) {
                SurveillanceRequirement req = SurveillanceRequirement.builder()
                        .id(preq.getId())
                        .requirement(preq.getSurveilledRequirement())
                        .type(getRequirementDetailType(preq.getSurveilledRequirement(), preq.getRequirementType()).getSurveillanceRequirementType())
                        .criterion(preq.getCertificationCriterionEntity() != null ? preq.getCertificationCriterionEntity().toDomain() : null)
                        .requirementDetailType(getRequirementDetailType(preq.getSurveilledRequirement(), preq.getRequirementType()))
                        .result(SurveillanceResultType.builder()
                                .name(preq.getResult())
                                .build())
                        .build();

                if (preq.getNonconformities() != null) {
                    for (PendingSurveillanceNonconformityEntity pnc : preq.getNonconformities()) {
                        SurveillanceNonconformity nc = SurveillanceNonconformity.builder()
                                .capApprovalDay(pnc.getCapApproval())
                                .capEndDay(pnc.getCapEndDate())
                                .capMustCompleteDay(pnc.getCapMustCompleteDate())
                                .capStartDay(pnc.getCapStart())
                                .dateOfDeterminationDay(pnc.getDateOfDetermination())
                                .developerExplanation(pnc.getDeveloperExplanation())
                                .findings(pnc.getFindings())
                                .id(pnc.getId())
                                .nonconformityType(pnc.getType())
                                .criterion(pnc.getCertificationCriterionEntity() != null ? pnc.getCertificationCriterionEntity().toDomain() : null)
                                .type(pnc.getNcType().toDomain())
                                .resolution(pnc.getResolution())
                                .sitesPassed(pnc.getSitesPassed())
                                .summary(pnc.getSummary())
                                .totalSites(pnc.getTotalSites())
                                .nonconformityCloseDay(pnc.getNonconformityCloseDate())
                                .nonconformityStatus(pnc.getNonconformityCloseDate() == null ? "Open" : "Closed")
                                .build();
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

    private RequirementDetailType getRequirementDetailType(String requirement, String requirementType) {
        return requirementDetailTypes.stream()
                .filter(detailType -> (NullSafeEvaluator.eval(() -> detailType.getNumber(), "") .equals(requirement)
                                        || NullSafeEvaluator.eval(() -> detailType.getTitle(), "") .equals(requirement))
                                        && detailType.getSurveillanceRequirementType().getName().equals(requirementType))
                .findAny()
                .orElse(null);
    }

    private void deletePendingSurveillance(Long pendingSurveillanceId, boolean isConfirmed)
            throws ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException,
            EntityCreationException {

        PendingSurveillanceEntity surv = pendingSurveillanceDAO.getPendingSurveillanceById(pendingSurveillanceId);
        Surveillance toDelete = getPendingById(pendingSurveillanceId);

        if (isPendingSurveillanceAvailableForUpdate(surv)) {
            try {
                pendingSurveillanceDAO.deletePendingSurveillance(toDelete);
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
        PendingSurveillanceEntity pendingSurv = pendingSurveillanceDAO.getPendingSurveillanceById(pendingSurvId);
        return isPendingSurveillanceAvailableForUpdate(pendingSurv);
    }

    private boolean isPendingSurveillanceAvailableForUpdate(PendingSurveillanceEntity pendingSurv)
            throws EntityRetrievalException, ObjectMissingValidationException {
        if (pendingSurv.getDeleted()) {
            ObjectMissingValidationException alreadyDeletedEx = new ObjectMissingValidationException();
            alreadyDeletedEx.getErrorMessages()
                    .add("This pending surveillance has already been confirmed or rejected by another user.");
            alreadyDeletedEx.setObjectId(pendingSurv.getId().toString());
            alreadyDeletedEx.setStartDate(new Date(DateUtil.toEpochMillis(pendingSurv.getStartDate())));
            alreadyDeletedEx.setEndDate(new Date(DateUtil.toEpochMillisEndOfDay(pendingSurv.getEndDate())));

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

    private Long createSurveillance(Surveillance surv) throws UserPermissionRetrievalException {
        Long insertedId = null;

        try {
            insertedId = surveillanceDAO.insertSurveillance(surv);
        } catch (UserPermissionRetrievalException ex) {
            LOGGER.error("Error inserting surveillance.", ex);
            throw ex;
        }

        return insertedId;
    }

    private Surveillance getByFriendlyIdAndListing(Long certifiedProductId, String survFriendlyId) {
        SurveillanceEntity surv = surveillanceDAO.getSurveillanceByCertifiedProductAndFriendlyId(certifiedProductId,
                survFriendlyId);
        if (surv == null) {
            throw new EntityNotFoundException("Could not find surveillance for certified product " + certifiedProductId
                    + " with friendly id " + survFriendlyId);
        }
        return surv.toDomain(cpDAO, certificationCriterionService);
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

    private void deleteSurveillance(Surveillance surv) throws EntityRetrievalException {
        surveillanceDAO.deleteSurveillance(surv);
    }

    private ObjectMissingValidationException createdObjectMissingValidationException(PendingSurveillanceEntity entity) {
        ObjectMissingValidationException alreadyDeletedEx = new ObjectMissingValidationException();
        alreadyDeletedEx.getErrorMessages()
                .add("This pending surveillance has already been confirmed or rejected by another user.");
        alreadyDeletedEx.setObjectId(entity.getId().toString());
        alreadyDeletedEx.setStartDate(entity.getStartDate() == null ? null : new Date(DateUtil.toEpochMillis(entity.getStartDate())));
        alreadyDeletedEx.setEndDate(entity.getEndDate() == null ? null : new Date(DateUtil.toEpochMillisEndOfDay(entity.getEndDate())));
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
