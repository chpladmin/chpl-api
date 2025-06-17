package gov.healthit.chpl.changerequest.manager;

import java.util.Set;
import java.util.stream.Collectors;

import org.ff4j.FF4j;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.attestation.service.AttestationResponseValidationService;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestUpdateRequest;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsFactory;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsService;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationService;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.form.validation.FormValidator;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.scheduler.job.changerequest.ChangeRequestReportEmailJob;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChangeRequestManager {
    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Value("${changerequest.status.pendingdeveloperaction}")
    private Long pendingDeveloperActionStatus;

    @Value("${changerequest.status.accepted}")
    private Long acceptedStatus;

    @Value("${changerequest.status.cancelledbyrequester}")
    private Long cancelledStatus;

    @Value("${changerequest.status.rejected}")
    private Long rejectedStatus;

    private SchedulerManager schedulerManager;
    private ChangeRequestDAO changeRequestDAO;
    private ChangeRequestTypeDAO changeRequestTypeDAO;
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;
    private DeveloperDAO developerDAO;
    private ChangeRequestStatusService crStatusService;
    private ChangeRequestValidationService crValidationService;
    private ChangeRequestDetailsFactory crDetailsFactory;
    private DeveloperManager devManager;
    private ActivityManager activityManager;
    private AttestationManager attestationManager;
    private AttestationResponseValidationService attestationResponseValidationService;
    private AttestationPeriodService attestationPeriodService;
    private ListingSearchService listingSearchService;
    private ResourcePermissionsFactory resourcePermissionsFactory;
    private ErrorMessageUtil msgUtil;
    private ValidationUtils validationUtils;
    private FormValidator formValidator;

    private FF4j ff4j;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ChangeRequestManager(SchedulerManager schedulerManager,
            ChangeRequestDAO changeRequestDAO,
            ChangeRequestTypeDAO changeRequestTypeDAO,
            ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO,
            CertifiedProductDAO certifiedProductDAO,
            CertificationBodyDAO certificationBodyDAO,
            DeveloperDAO developerDAO,
            ChangeRequestStatusService crStatusHelper,
            ChangeRequestValidationService crValidationService,
            ChangeRequestDetailsFactory crDetailsFactory,
            DeveloperManager devManager,
            ActivityManager activityManager,
            AttestationManager attestationManager,
            AttestationResponseValidationService attestationResponseValidationService,
            AttestationPeriodService attestationPeriodService,
            ListingSearchService listingSearchService,
            ResourcePermissionsFactory resourcePermissionsFactory,
            ErrorMessageUtil msgUtil,
            ValidationUtils validationUtils,
            FormValidator formValidator,
            FF4j ff4j) {
        this.schedulerManager = schedulerManager;
        this.changeRequestDAO = changeRequestDAO;
        this.changeRequestTypeDAO = changeRequestTypeDAO;
        this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
        this.developerDAO = developerDAO;
        this.crStatusService = crStatusHelper;
        this.crValidationService = crValidationService;
        this.crDetailsFactory = crDetailsFactory;
        this.devManager = devManager;
        this.activityManager = activityManager;
        this.attestationManager = attestationManager;
        this.attestationResponseValidationService = attestationResponseValidationService;
        this.attestationPeriodService = attestationPeriodService;
        this.listingSearchService = listingSearchService;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
        this.msgUtil = msgUtil;
        this.validationUtils = validationUtils;
        this.formValidator = formValidator;
        this.ff4j = ff4j;
    }

    @Transactional(readOnly = true)
    public Set<KeyValueModel> getChangeRequestTypes() {
        return changeRequestTypeDAO.getChangeRequestTypes().stream()
                .filter(type -> type.getName().equals(ChangeRequestType.ATTESTATION_TYPE)
                        || (type.getName().equals(ChangeRequestType.DEMOGRAPHICS_TYPE)
                                && ff4j.check(FeatureList.DEMOGRAPHIC_CHANGE_REQUEST))
                        || (type.getName().equals(ChangeRequestType.SBUL_TYPE)
                                && ff4j.check(FeatureList.SERVICE_BASE_URL_LIST_CHANGE_REQUEST))
                        || (type.getName().equals(ChangeRequestType.RWT_PLANS_TYPE)
                                && ff4j.check(FeatureList.RWT_CHANGE_REQUEST))
                        || (type.getName().equals(ChangeRequestType.RWT_RESULTS_TYPE)
                                && ff4j.check(FeatureList.RWT_CHANGE_REQUEST))
                        )
                .map(crType -> new KeyValueModel(crType.getId(), crType.getName()))
                .collect(Collectors.<KeyValueModel>toSet());
    }

    @Transactional(readOnly = true)
    public Set<KeyValueModel> getChangeRequestStatusTypes() {
        return changeRequestStatusTypeDAO.getChangeRequestStatusTypes().stream()
                .map(crStatusType -> new KeyValueModel(crStatusType.getId(), crStatusType.getName()))
                .collect(Collectors.toSet());
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).CREATE, #changeRequest)")
    @CacheEvict(cacheNames = CacheNames.COLLECTIONS_DEVELOPERS)
    public ChangeRequest createChangeRequest(ChangeRequest changeRequest)
            throws InvalidArgumentsException, EntityRetrievalException, ValidationException, ActivityException {

        changeRequest.setDeveloper(getDeveloperFromDb(changeRequest));
        changeRequest.setChangeRequestType(getChangeRequestTypeFromDb(changeRequest));
        changeRequest = updateChangeRequestWithCastedDetails(changeRequest);
        if (!ff4j.check(FeatureList.SERVICE_BASE_URL_LIST_CHANGE_REQUEST)
                && changeRequest.getChangeRequestType().isSbul()) {
            throw new InvalidArgumentsException(msgUtil.getMessage("changeRequest.listingUrl.serviceBaseUrlList.featureDisabled"));
        } else if (!ff4j.check(FeatureList.RWT_CHANGE_REQUEST)
                && isRwtChangeRequestType(changeRequest.getChangeRequestType())) {
            throw new InvalidArgumentsException(msgUtil.getMessage("changeRequest.listingUrl.rwtUrl.featureDisabled"));
        }
        Long newCrId = saveChangeRequest(changeRequest);
        ChangeRequest newCr = null;
        if (newCrId == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("changeRequest.noChanges"));
        } else {
            newCr = getChangeRequest(newCrId);
            activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, newCr.getId(), "Change request created", null, newCr);
        }
        return newCr;
    }

    private boolean isRwtChangeRequestType(ChangeRequestType changeRequestType) {
        return changeRequestType.isRwtPlans() || changeRequestType.isRwtResults();
    }

    @Transactional(readOnly = true)
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).GET_BY_ID, returnObject)")
    public ChangeRequest getChangeRequest(Long changeRequestId) throws EntityRetrievalException {
        return changeRequestDAO.get(changeRequestId);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).UPDATE, #crUpdateRequest)")
    @CacheEvict(cacheNames = CacheNames.COLLECTIONS_DEVELOPERS)
    public ChangeRequest updateChangeRequest(ChangeRequestUpdateRequest crUpdateRequest)
            throws EntityRetrievalException, ValidationException, EntityCreationException,
            JsonProcessingException, ActivityException, InvalidArgumentsException, EmailNotSentException {

        ChangeRequest cr = updateChangeRequestWithCastedDetails(crUpdateRequest.getChangeRequest());

        ChangeRequest crFromDb = getChangeRequest(cr.getId());

        ChangeRequestValidationContext crValidationContext = getNewValidationContext(cr, crFromDb);

        ValidationException validationException = new ValidationException(
                crValidationService.getErrorMessages(crValidationContext),
                crValidationService.getWarningMessages(crValidationContext));
        if ((validationException.getErrorMessages() != null && !validationException.getErrorMessages().isEmpty())
                || (!crUpdateRequest.isAcknowledgeWarnings()
                        && validationException.getWarningMessages() != null
                        && !validationException.getWarningMessages().isEmpty())) {
            throw validationException;
        }

        // Update the details, if the user is of role developer
        if (resourcePermissionsFactory.get().isUserRoleDeveloperAdmin()
                && cr.getDetails() != null
                && ChangeRequestStatusService.doesCurrentStatusExist(cr)
                && !cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(cancelledStatus)) {
            crDetailsFactory.get(crFromDb.getChangeRequestType().getId()).update(cr);
        }

        // Update the status
        if (ChangeRequestStatusService.doesCurrentStatusExist(cr)) {
            crStatusService.updateChangeRequestStatus(cr);
        }

        ChangeRequest updatedCr = getChangeRequest(cr.getId());
        activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, cr.getId(),
                "Change request details updated",
                crFromDb, updatedCr);
        return updatedCr;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).SEARCH)")
    public ChplOneTimeTrigger triggerChangeRequestsReport(ChangeRequestSearchRequest searchRequest)
            throws SchedulerException, ValidationException {

        ChplOneTimeTrigger changeRequestsReportTrigger = new ChplOneTimeTrigger();
        ChplJob changeRequestsReportJob = new ChplJob();
        changeRequestsReportJob.setName(ChangeRequestReportEmailJob.JOB_NAME);
        changeRequestsReportJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(ChangeRequestReportEmailJob.USER_KEY, AuthUtil.getCurrentUser());
        jobDataMap.put(ChangeRequestReportEmailJob.SEARCH_REQUEST, searchRequest);
        changeRequestsReportJob.setJobDataMap(jobDataMap);
        changeRequestsReportTrigger.setJob(changeRequestsReportJob);
        changeRequestsReportTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);
        changeRequestsReportTrigger = schedulerManager.createBackgroundJobTrigger(changeRequestsReportTrigger);
        return changeRequestsReportTrigger;
    }

    private Developer getDeveloperFromDb(ChangeRequest changeRequest) throws InvalidArgumentsException, EntityRetrievalException {
        if (changeRequest.getDeveloper() == null || changeRequest.getDeveloper().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("changeRequest.developer.required"));
        }
        return devManager.getById(changeRequest.getDeveloper().getId());
    }

    private ChangeRequestType getChangeRequestTypeFromDb(ChangeRequest changeRequest) throws EntityRetrievalException {
        if (changeRequest.getChangeRequestType().isDemographics()) {
            return changeRequestTypeDAO.getChangeRequestTypeByName(ChangeRequestType.DEMOGRAPHICS_TYPE);
        } else if (changeRequest.getChangeRequestType().isAttestation()) {
            return changeRequestTypeDAO.getChangeRequestTypeByName(ChangeRequestType.ATTESTATION_TYPE);
        } else if (changeRequest.getChangeRequestType().isSbul()) {
            return changeRequestTypeDAO.getChangeRequestTypeByName(ChangeRequestType.SBUL_TYPE);
        } else if (changeRequest.getChangeRequestType().isRwtPlans()) {
            return changeRequestTypeDAO.getChangeRequestTypeByName(ChangeRequestType.RWT_PLANS_TYPE);
        } else if (changeRequest.getChangeRequestType().isRwtResults()) {
            return changeRequestTypeDAO.getChangeRequestTypeByName(ChangeRequestType.RWT_RESULTS_TYPE);
        }

        return null;
    }

    private Long saveChangeRequest(ChangeRequest cr) throws ValidationException, EntityRetrievalException, ActivityException {
        ChangeRequestValidationContext crValidationContext = getNewValidationContext(cr, null);
        ValidationException validationException = new ValidationException(
                crValidationService.getErrorMessages(crValidationContext).stream()
                        .filter(msg -> msg != null && !msg.isEmpty())
                        .toList());
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        Long newCrId = createBaseChangeRequest(cr);
        Long crDetailsId = createChangeRequestDetails(newCrId, cr.getChangeRequestType().getId(), cr.getDetails());
        return newCrId;
    }

    private Long createBaseChangeRequest(ChangeRequest cr) throws EntityRetrievalException {
        cr.setCertificationBodies(crDetailsFactory.get(cr.getChangeRequestType().getId()).getAssociatedCertificationBodies(cr));
        Long newCrId = changeRequestDAO.create(cr);
        crStatusService.saveInitialStatus(newCrId);
        return newCrId;
    }

    private Long createChangeRequestDetails(Long changeRequestId, Long changeRequestTypeId, Object changeRequestDetails) {
        ChangeRequestDetailsService<?> crDetailsService = crDetailsFactory.get(changeRequestTypeId);
        return crDetailsService.create(changeRequestId, changeRequestDetails);
    }

    private ChangeRequest updateChangeRequestWithCastedDetails(ChangeRequest cr) {
        if (cr.getChangeRequestType().isDemographics()) {
            cr.setDetails(mapper.convertValue(cr.getDetails(), ChangeRequestDeveloperDemographics.class));
        } else if (cr.getChangeRequestType().isAttestation()) {
            cr.setDetails(mapper.convertValue(cr.getDetails(), ChangeRequestAttestationSubmission.class));
        } else if (cr.getChangeRequestType().isListingUrl()) {
            cr.setDetails(mapper.convertValue(cr.getDetails(), ChangeRequestListingUrl.class));
            ((ChangeRequestListingUrl) cr.getDetails()).setUrl(((ChangeRequestListingUrl) cr.getDetails()).getUrl().trim());
        }
        return cr;
    }

    private ChangeRequestValidationContext getNewValidationContext(ChangeRequest newChangeRequest, ChangeRequest originalChangeRequest) {
        return new ChangeRequestValidationContext(
                AuthUtil.getCurrentUser(),
                newChangeRequest,
                originalChangeRequest,
                formValidator,
                attestationResponseValidationService,
                attestationPeriodService,
                listingSearchService,
                resourcePermissionsFactory,
                validationUtils,
                developerDAO,
                changeRequestDAO,
                changeRequestStatusTypeDAO,
                changeRequestTypeDAO,
                attestationManager,
                cancelledStatus,
                acceptedStatus,
                rejectedStatus,
                pendingAcbActionStatus,
                pendingDeveloperActionStatus);
    }
}
