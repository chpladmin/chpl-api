package gov.healthit.chpl.manager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceReportingActivityJob;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.validation.surveillance.SurveillanceCreationValidator;
import gov.healthit.chpl.validation.surveillance.SurveillanceReadValidator;
import gov.healthit.chpl.validation.surveillance.SurveillanceUpdateValidator;

@Service
public class SurveillanceManager extends SecuredManager {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceManager.class);

    private SurveillanceDAO survDao;
    private CertifiedProductDAO cpDao;
    private CertifiedProductDetailsManager cpDetailsManager;
    private ActivityManager activityManager;
    private SchedulerManager schedulerManager;
    private SurveillanceReadValidator survReadValidator;
    private SurveillanceUpdateValidator survUpdateValidator;
    private SurveillanceCreationValidator survCreationValidator;
    private FileUtils fileUtils;
    private Environment env;
    private UserDAO userDAO;
    private CertificationCriterionService certificationCriterionService;
    private String schemaBasicSurveillanceName;

    @SuppressWarnings("checkstyle:parameterNumber")
    @Autowired
    public SurveillanceManager(SurveillanceDAO survDao, CertifiedProductDAO cpDao,
            @Lazy CertifiedProductDetailsManager cpDetailsManager, ActivityManager activityManager,
            SchedulerManager schedulerManager, SurveillanceReadValidator survReadValidator,
            SurveillanceCreationValidator survCreationValidator,
            SurveillanceUpdateValidator survUpdateValidator,
            FileUtils fileUtils, Environment env, ResourcePermissions resourcePermissions,
            UserDAO userDAO, CertificationCriterionService certificationCriterionService,
            @Value("${schemaBasicSurveillanceName}") String schemaBasicSurveillanceName) {
        this.survDao = survDao;
        this.cpDao = cpDao;
        this.cpDetailsManager = cpDetailsManager;
        this.activityManager = activityManager;
        this.schedulerManager = schedulerManager;
        this.survUpdateValidator = survUpdateValidator;
        this.survCreationValidator = survCreationValidator;
        this.survReadValidator = survReadValidator;
        this.fileUtils = fileUtils;
        this.env = env;
        this.userDAO = userDAO;
        this.certificationCriterionService = certificationCriterionService;
        this.schemaBasicSurveillanceName = schemaBasicSurveillanceName;
    }

    @Transactional(readOnly = true)
    public Surveillance getById(final Long survId) throws EntityRetrievalException {
        Surveillance result = survDao.getSurveillanceById(survId).toDomain(cpDao, certificationCriterionService);
        survReadValidator.validate(result);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Surveillance> getByCertifiedProduct(final Long cpId) {
        List<Surveillance> surveillances = survDao.getSurveillanceByCertifiedProductId(cpId).stream()
                .map(survEntity -> survEntity.toDomain(cpDao, certificationCriterionService))
                .toList();
        surveillances.forEach(surv -> survReadValidator.validate(surv));
        return surveillances;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).CREATE, #survToInsert)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.LISTING_ID, id = "#survToInsert.certifiedProduct.id")
    public Long createSurveillance(Surveillance survToInsert)
            throws UserPermissionRetrievalException, EntityRetrievalException, JsonProcessingException, EntityCreationException,
            ValidationException {
        CertifiedProductSearchDetails beforeListing = cpDetailsManager
                .getCertifiedProductDetails(survToInsert.getCertifiedProduct().getId());

        validateSurveillanceCreation(survToInsert);
        if (survToInsert.getErrorMessages() != null && survToInsert.getErrorMessages().size() > 0) {
            throw new ValidationException(survToInsert.getErrorMessages(), null);
        }

        Long insertedId = null;
        try {
            insertedId = survDao.insertSurveillance(survToInsert);
        } catch (final UserPermissionRetrievalException ex) {
            LOGGER.error("Error inserting surveillance.", ex);
            throw ex;
        }
        logSurveillanceCreationActivity(beforeListing);
        return insertedId;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).UPDATE, #survToUpdate)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH,
            CacheNames.COMPLAINTS
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.LISTING_ID, id = "#survToUpdate.certifiedProduct.id")
    public void updateSurveillance(final Surveillance survToUpdate) throws EntityRetrievalException,
            EntityCreationException, JsonProcessingException, ValidationException {
        CertifiedProductSearchDetails beforeListing = cpDetailsManager
                .getCertifiedProductDetails(survToUpdate.getCertifiedProduct().getId());

        Optional<Surveillance> beforeSurv = beforeListing.getSurveillance().stream()
            .filter(surv -> surv.getId().equals(survToUpdate.getId()))
            .findFirst();
        validateSurveillanceUpdate(beforeSurv.isPresent() ? beforeSurv.get() : null, survToUpdate);
        if (survToUpdate.getErrorMessages() != null && survToUpdate.getErrorMessages().size() > 0) {
            throw new ValidationException(survToUpdate.getErrorMessages(), null);
        }

        if (beforeSurv.isPresent() && !beforeSurv.get().matches(survToUpdate)) {
            survDao.updateSurveillance(survToUpdate);
            logSurveillanceUpdateActivity(beforeListing);
        }
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).DELETE, #survToDelete)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH, CacheNames.COMPLAINTS
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.LISTING_ID, id = "#survToDelete.certifiedProduct.id")
    public void deleteSurveillance(Surveillance survToDelete, String reason)
            throws InvalidArgumentsException, EntityRetrievalException, EntityCreationException, JsonProcessingException {
        if (survToDelete == null) {
            throw new InvalidArgumentsException("Cannot find surveillance with id " + survToDelete.getId() + " to delete.");
        }

        CertifiedProductSearchDetails beforeCp = cpDetailsManager
                .getCertifiedProductDetails(survToDelete.getCertifiedProduct().getId());

        survDao.deleteSurveillance(survToDelete);

        CertifiedProductSearchDetails afterCp = cpDetailsManager
                .getCertifiedProductDetailsNoCache(survToDelete.getCertifiedProduct().getId());
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, afterCp.getId(),
                "Surveillance was delete from certified product " + afterCp.getChplProductNumber(),
                beforeCp, afterCp, reason);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).ACTIVITY_REPORT)")
    public ChplOneTimeTrigger submitActivityReportRequest(LocalDate start, LocalDate end) throws ValidationException, UserRetrievalException {
        UserDTO user = userDAO.getById(AuthUtil.getCurrentUser().getId());

        ChplOneTimeTrigger surveillanceActivityReportTrigger = new ChplOneTimeTrigger();
        ChplJob surveillanceActivityReportJob = new ChplJob();
        surveillanceActivityReportJob.setName(SurveillanceReportingActivityJob.JOB_NAME);
        surveillanceActivityReportJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        //our current jackson library does not allow java serliaziation of LocalDate objects
        //so here we convert them back to Strings (and the job will convert them back to LocalDates)
        jobDataMap.put(SurveillanceReportingActivityJob.START_DATE_KEY, start.format(SurveillanceReportingActivityJob.JOB_DATA_DATE_FORMATTER));
        jobDataMap.put(SurveillanceReportingActivityJob.END_DATE_KEY, end.format(SurveillanceReportingActivityJob.JOB_DATA_DATE_FORMATTER));
        jobDataMap.put(SurveillanceReportingActivityJob.USER_EMAIL, user.getEmail());
        surveillanceActivityReportJob.setJobDataMap(jobDataMap);
        surveillanceActivityReportTrigger.setJob(surveillanceActivityReportJob);
        surveillanceActivityReportTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.DELAY_BEFORE_BACKGROUND_JOB_START);
        try {
            return schedulerManager.createBackgroundJobTrigger(surveillanceActivityReportTrigger);
        } catch (SchedulerException e) {
            LOGGER.error("Could not schedule 'surveillanceActivityReportTrigger'.");
            LOGGER.catching(e);
            return null;
        }
    }

    private void validateSurveillanceUpdate(Surveillance existingSurv, Surveillance updatedSurv) {
        updatedSurv.getErrorMessages().clear();
        survUpdateValidator.validate(existingSurv, updatedSurv);
    }

    private void validateSurveillanceCreation(Surveillance createdSurv) {
        createdSurv.getErrorMessages().clear();
        survCreationValidator.validate(createdSurv);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).BASIC_REPORT)")
    public File getBasicReportDownloadFile() throws IOException {
        return fileUtils.getNewestFileMatchingName("^" + env.getProperty("surveillanceBasicReportName") + "-.+\\.csv$");
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).BASIC_REPORT)")
    public File getBasicReportDownloadDefinitionFile() throws IOException {
        return fileUtils.getDownloadFile(schemaBasicSurveillanceName);
    }

    public File getAllSurveillanceDownloadFile() throws IOException {
        return fileUtils.getNewestFileMatchingName("^" + env.getProperty("surveillanceAllReportName") + "-.+\\.csv$");
    }

    public File getSurveillanceWithNonconformitiesDownloadFile() throws IOException {
        return fileUtils.getNewestFileMatchingName(
                "^" + env.getProperty("surveillanceNonconformitiesReportName") + "-.+\\.csv$");
    }

    private void logSurveillanceUpdateActivity(CertifiedProductSearchDetails existingListing)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
        CertifiedProductSearchDetails changedListing = cpDetailsManager.getCertifiedProductDetailsNoCache(existingListing.getId());
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, existingListing.getId(),
                "Surveillance was updated on certified product " + changedListing.getChplProductNumber(),
                existingListing, changedListing);
    }

    private void logSurveillanceCreationActivity(CertifiedProductSearchDetails existingListing)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
        CertifiedProductSearchDetails changedListing = cpDetailsManager.getCertifiedProductDetailsNoCache(existingListing.getId());
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, existingListing.getId(),
                "Surveillance was added to certified product " + changedListing.getChplProductNumber(),
                existingListing, changedListing);
    }
}
