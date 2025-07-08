package gov.healthit.chpl.changerequest.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
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
import gov.healthit.chpl.changerequest.dao.ChangeRequestListingUrlDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestUpdateRequest;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsFactory;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationService;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
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
    private static final String SERVICE_BASE_URL_LIST_TYPE = "Service Base URL List";

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

    @Value("${changerequest.developerDemographics}")
    private Long developerDemographicsChangeRequestTypeId;

    @Value("${changerequest.attestation}")
    private Long attestationChangeRequestTypeId;

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
    private ChangeRequestListingUrlDAO changeRequestListingUrlDAO;

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
            FF4j ff4j,
            ChangeRequestListingUrlDAO changeRequestListingUrlDAO) {
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
        this.changeRequestListingUrlDAO = changeRequestListingUrlDAO;
    }

    @Transactional(readOnly = true)
    public List<ChangeRequestType> getChangeRequestTypes() {
        return changeRequestTypeDAO.getChangeRequestTypes().stream()
                .filter(entity -> entity.getName().equals(ChangeRequestType.ATTESTATION_TYPE)
                        || (entity.getName().equals(ChangeRequestType.DEMOGRAPHICS_TYPE)
                                && ff4j.check(FeatureList.DEMOGRAPHIC_CHANGE_REQUEST))
                        || (entity.getName().equals(ChangeRequestType.LISTING_URL_TYPE)
                                && ff4j.check(FeatureList.SERVICE_BASE_URL_LIST_CHANGE_REQUEST))
                        )
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChangeRequestStatusType> getChangeRequestStatusTypes() {
        return changeRequestStatusTypeDAO.getChangeRequestStatusTypes();
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).CREATE, #changeRequest)")
    @CacheEvict(cacheNames = CacheNames.COLLECTIONS_DEVELOPERS)
    public ChangeRequest createChangeRequest(ChangeRequest changeRequest)
            throws InvalidArgumentsException, EntityRetrievalException, ValidationException, ActivityException {

        changeRequest.setDeveloper(getDeveloperFromDb(changeRequest));
        changeRequest.setChangeRequestType(getChangeRequestType(changeRequest));
        changeRequest = updateChangeRequestWithCastedDetails(changeRequest);
        if (!ff4j.check(FeatureList.SERVICE_BASE_URL_LIST_CHANGE_REQUEST)
                && changeRequest.getDetails() != null
                && changeRequest.getDetails() instanceof ChangeRequestListingUrl
                && ((ChangeRequestListingUrl) changeRequest.getDetails()).getChangeRequestListingUrlType().getName().equals(SERVICE_BASE_URL_LIST_TYPE)) {
            throw new InvalidArgumentsException(msgUtil.getMessage("changeRequest.listingUrl.serviceBaseUrlList.featureDisabled"));
        }
        ChangeRequest cr = saveChangeRequest(changeRequest);
        if (cr == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("changeRequest.noChanges"));
        }
        return cr;
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
            JsonProcessingException, InvalidArgumentsException, EmailNotSentException {

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

        ChangeRequest updatedDetails = null, updatedStatus = null;
        // Update the details, if the user is of role developer
        if (resourcePermissionsFactory.get().isUserRoleDeveloperAdmin()
                && cr.getDetails() != null
                && ChangeRequestStatusService.doesCurrentStatusExist(cr)
                && !cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(cancelledStatus)) {
            updatedDetails = crDetailsFactory.get(crFromDb.getChangeRequestType().getId()).update(cr);
        }

        // Update the status
        if (ChangeRequestStatusService.doesCurrentStatusExist(cr)) {
            updatedStatus = crStatusService.updateChangeRequestStatus(cr);
        }

        if (updatedDetails == null && updatedStatus == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("changeRequest.noChanges"));
        }

        ChangeRequest newCr = getChangeRequest(cr.getId());
        return newCr;
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

    private ChangeRequestType getChangeRequestType(ChangeRequest parentChangeRequest) throws EntityRetrievalException {
        if (isDeveloperDemogrpahicChangeRequest(parentChangeRequest)) {
            return changeRequestTypeDAO.getChangeRequestTypeById(developerDemographicsChangeRequestTypeId);
        } else if (isDeveloperAttestationChangeRequest(parentChangeRequest)) {
            return changeRequestTypeDAO.getChangeRequestTypeById(attestationChangeRequestTypeId);
        } else if (isServiceBaseUrlListChangeRequest(parentChangeRequest)) {
            return changeRequestTypeDAO.getChangeRequestTypeByName(ChangeRequestType.LISTING_URL_TYPE);
        }

        return null;
    }

    private boolean isDeveloperDemogrpahicChangeRequest(ChangeRequest cr) {
        HashMap<String, Object> crMap = (HashMap) cr.getDetails();
        return crMap.containsKey("developerId")
                || (ObjectUtils.allNotNull(cr, cr.getChangeRequestType())
                && cr.getChangeRequestType().isDemographics());
    }

    private boolean isDeveloperAttestationChangeRequest(ChangeRequest cr) {
        HashMap<String, Object> crMap = (HashMap) cr.getDetails();
        return crMap.containsKey("form");
    }

    private boolean isServiceBaseUrlListChangeRequest(ChangeRequest cr) {
        HashMap<String, Object> crMap = (HashMap) cr.getDetails();
        try {
            Integer listingUrlTypeId = changeRequestListingUrlDAO.getChangeRequestListingUrlType(SERVICE_BASE_URL_LIST_TYPE).getId().intValue();
            return crMap.containsKey("changeRequestListingUrlType")
                    &&  ((Map) crMap.get("changeRequestListingUrlType")).get("id").equals(listingUrlTypeId);
        } catch (Exception e) {
            return false;
        }
    }

    private ChangeRequest saveChangeRequest(ChangeRequest cr) throws ValidationException, EntityRetrievalException, ActivityException {

        ChangeRequestValidationContext crValidationContext = getNewValidationContext(cr, null);
        ValidationException validationException = new ValidationException(
                crValidationService.getErrorMessages(crValidationContext).stream()
                        .filter(msg -> msg != null && !msg.isEmpty())
                        .toList());
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        ChangeRequest newCr = createBaseChangeRequest(cr);
        newCr.setDetails(cr.getDetails());
        newCr = crDetailsFactory.get(newCr.getChangeRequestType().getId()).create(newCr);
        if (newCr != null) {
            newCr = getChangeRequest(newCr.getId());

            activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, newCr.getId(), "Change request created", null, newCr);
            return newCr;
        }
        return null;
    }

    private ChangeRequest createBaseChangeRequest(ChangeRequest cr) throws EntityRetrievalException {
        cr.setCertificationBodies(crDetailsFactory.get(cr.getChangeRequestType().getId()).getAssociatedCertificationBodies(cr));

        ChangeRequest newCr = changeRequestDAO.create(cr);
        newCr.getStatuses().add(crStatusService.saveInitialStatus(newCr));

        return newCr;
    }

    private ChangeRequest updateChangeRequestWithCastedDetails(ChangeRequest cr) {
        if (isDeveloperDemogrpahicChangeRequest(cr)) {
            cr.setDetails(mapper.convertValue(cr.getDetails(), ChangeRequestDeveloperDemographics.class));
        } else if (isDeveloperAttestationChangeRequest(cr)) {
            cr.setDetails(mapper.convertValue(cr.getDetails(), ChangeRequestAttestationSubmission.class));
        } else if (isServiceBaseUrlListChangeRequest(cr)) {
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
                developerDemographicsChangeRequestTypeId,
                attestationChangeRequestTypeId,
                cancelledStatus,
                acceptedStatus,
                rejectedStatus,
                pendingAcbActionStatus,
                pendingDeveloperActionStatus);
    }
}
