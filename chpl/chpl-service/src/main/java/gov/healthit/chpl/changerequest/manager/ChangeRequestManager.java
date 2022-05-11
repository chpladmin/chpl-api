package gov.healthit.chpl.changerequest.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsFactory;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationService;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChangeRequestManager extends SecurityManager {

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

    @Value("${changerequest.website}")
    private Long websiteChangeRequestTypeId;

    @Value("${changerequest.developerDetails}")
    private Long developerDetailsChangeRequestTypeId;

    @Value("${changerequest.attestation}")
    private Long attestationChangeRequestTypeId;

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
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;
    private ValidationUtils validationUtils;
    private FF4j ff4j;

    @Autowired
    public ChangeRequestManager(ChangeRequestDAO changeRequestDAO,
            ChangeRequestTypeDAO changeRequestTypeDAO,
            ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO,
            CertifiedProductDAO certifiedProductDAO,
            CertificationBodyDAO certificationBodyDAO,
            DeveloperDAO developerDAO,
            ChangeRequestStatusService crStatusHelper,
            ChangeRequestValidationService crValidationService,
            ChangeRequestDetailsFactory crDetailsFactory, DeveloperManager devManager,
            ActivityManager activityManager,
            AttestationManager attestationManager,
            ResourcePermissions resourcePermissions,
            ErrorMessageUtil msgUtil,
            ValidationUtils validationUtils,
            FF4j ff4j) {
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
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
        this.validationUtils = validationUtils;
        this.ff4j = ff4j;
    }

    @Transactional(readOnly = true)
    public Set<KeyValueModel> getChangeRequestTypes() {
        return changeRequestTypeDAO.getChangeRequestTypes().stream()
                .filter(entity -> entity.getName().equals("Developer Attestation Change Request")
                        || (entity.getName().equals("Developer Details Change Request") && ff4j.check(FeatureList.DEMOGRAPHIC_CHANGE_REQUEST))
                        || (entity.getName().equals("Website Change Request") && ff4j.check(FeatureList.DEMOGRAPHIC_CHANGE_REQUEST)))
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
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).CREATE, #parentChangeRequest)")
    public List<ChangeRequest> createChangeRequests(ChangeRequest parentChangeRequest)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException, NotImplementedException {
        //get developer from db in case passed-in data is not correct
        if (parentChangeRequest.getDeveloper() == null || parentChangeRequest.getDeveloper().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("changeRequest.developer.required"));
        }
        Developer existingDeveloper = devManager.getById(parentChangeRequest.getDeveloper().getId());
        parentChangeRequest.setDeveloper(existingDeveloper);

        //make change requests for each type detected - throw error if no changes were made
        List<ChangeRequest> changeRequestsByType = splitByChangeRequestType(parentChangeRequest);
        if (changeRequestsByType == null || changeRequestsByType.size() == 0) {
            throw new InvalidArgumentsException(msgUtil.getMessage("changeRequest.noChanges"));
        }

        List<ChangeRequest> createdCrs = new ArrayList<ChangeRequest>();
        for (ChangeRequest cr : changeRequestsByType) {
            createdCrs.add(createChangeRequest(cr));
        }
        return createdCrs;
    }

    @Transactional(readOnly = true)
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).GET_BY_ID, returnObject)")
    public ChangeRequest getChangeRequest(Long changeRequestId) throws EntityRetrievalException {
        return changeRequestDAO.get(changeRequestId);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).GET_ALL)")
    public List<ChangeRequest> getAllChangeRequestsForUser() throws EntityRetrievalException {
        List<ChangeRequest> results = new ArrayList<ChangeRequest>();
        if (resourcePermissions.isUserRoleAcbAdmin()) {
            results = changeRequestDAO.getAllForAcbs(resourcePermissions.getAllAcbsForCurrentUser().stream()
                    .map(acb -> acb.getId())
                    .toList());
        } else if (resourcePermissions.isUserRoleDeveloperAdmin()) {
            results = changeRequestDAO.getAllForDevelopers(resourcePermissions.getAllDevelopersForCurrentUser().stream()
                    .map(dev -> dev.getId())
                    .toList());
        } else if (resourcePermissions.isUserRoleOnc() || resourcePermissions.isUserRoleAdmin()) {
            results = changeRequestDAO.getAll();
        }
        return results;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).UPDATE, #cr)")
    public ChangeRequest updateChangeRequest(ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, EmailNotSentException {

        ChangeRequest crFromDb = getChangeRequest(cr.getId());

        ChangeRequestValidationContext crValidationContext = getNewValidationContext(cr, crFromDb);
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(crValidationService.validate(crValidationContext));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        ChangeRequest updatedDetails = null, updatedStatus = null;
        // Update the details, if the user is of role developer
        if (resourcePermissions.isUserRoleDeveloperAdmin() && cr.getDetails() != null) {
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

    private List<ChangeRequest> splitByChangeRequestType(ChangeRequest parentChangeRequest) {
        List<ChangeRequest> changeRequests = new ArrayList<ChangeRequest>();
        if (isWebsiteChangeRequest(parentChangeRequest)) {
            if (!ff4j.check(FeatureList.DEMOGRAPHIC_CHANGE_REQUEST)) {
                throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
            }
            ChangeRequestType websiteChangeRequestType = new ChangeRequestType();
            websiteChangeRequestType.setId(websiteChangeRequestTypeId);
            ChangeRequest websiteChangeRequest = new ChangeRequest();
            websiteChangeRequest.setChangeRequestType(websiteChangeRequestType);
            websiteChangeRequest.setDeveloper(parentChangeRequest.getDeveloper());
            websiteChangeRequest.setSubmittedDate(parentChangeRequest.getSubmittedDate());
            websiteChangeRequest.setDetails(extractWebsiteChangesFromDetails(parentChangeRequest));
            changeRequests.add(websiteChangeRequest);
        }
        if (isDeveloperDetailsChangeRequest(parentChangeRequest)) {
            if (!ff4j.check(FeatureList.DEMOGRAPHIC_CHANGE_REQUEST)) {
                throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
            }
            ChangeRequestType devDetailsChangeRequestType = new ChangeRequestType();
            devDetailsChangeRequestType.setId(developerDetailsChangeRequestTypeId);
            ChangeRequest developerDetailsChangeRequest = new ChangeRequest();
            developerDetailsChangeRequest.setChangeRequestType(devDetailsChangeRequestType);
            developerDetailsChangeRequest.setDeveloper(parentChangeRequest.getDeveloper());
            developerDetailsChangeRequest.setSubmittedDate(parentChangeRequest.getSubmittedDate());
            developerDetailsChangeRequest.setDetails(extractDeveloperChangesFromDetails(parentChangeRequest));
            changeRequests.add(developerDetailsChangeRequest);
        }
        if (isDeveloperAttestationChangeRequest(parentChangeRequest)) {
            ChangeRequestType attestationChangeRequestType = new ChangeRequestType();
            attestationChangeRequestType.setId(attestationChangeRequestTypeId);
            ChangeRequest attestationChangeRequest = new ChangeRequest();
            attestationChangeRequest.setChangeRequestType(attestationChangeRequestType);
            attestationChangeRequest.setDeveloper(parentChangeRequest.getDeveloper());
            attestationChangeRequest.setSubmittedDate(parentChangeRequest.getSubmittedDate());
            attestationChangeRequest.setDetails(parentChangeRequest.getDetails());
            changeRequests.add(attestationChangeRequest);
        }
        return changeRequests;
    }

    private boolean isWebsiteChangeRequest(ChangeRequest cr) {
        return isWebsiteChanged(cr);
    }

    private boolean isDeveloperDetailsChangeRequest(ChangeRequest cr) {
        return isSelfDeveloperChanged(cr) || isAddressChanged(cr) || isContactChanged(cr);
    }

    private boolean isDeveloperAttestationChangeRequest(ChangeRequest cr) {
        HashMap<String, Object> crMap = (HashMap) cr.getDetails();
        return crMap.containsKey("attestationResponses");
    }

    private boolean isWebsiteChanged(ChangeRequest cr) {
        Developer existingDeveloper = cr.getDeveloper();
        HashMap<String, Object> crMap = (HashMap) cr.getDetails();

        String crWebsite = null;
        if (crMap.containsKey("website")) {
            crWebsite = crMap.get("website").toString();
            return !StringUtils.equals(crWebsite, existingDeveloper.getWebsite());
        }
        return false;
    }

    private boolean isSelfDeveloperChanged(ChangeRequest cr) {
        Developer existingDeveloper = cr.getDeveloper();
        HashMap<String, Object> crMap = (HashMap) cr.getDetails();

        Boolean crSelfDeveloper = null;
        if (crMap.containsKey("selfDeveloper")) {
            crSelfDeveloper = Boolean.parseBoolean(crMap.get("selfDeveloper").toString());
            return !ObjectUtils.equals(crSelfDeveloper, existingDeveloper.getSelfDeveloper());
        }
        return false;
    }

    private boolean isAddressChanged(ChangeRequest cr) {
        Developer existingDeveloper = cr.getDeveloper();
        HashMap<String, Object> crMap = (HashMap) cr.getDetails();

        if (crMap.containsKey("address")) {
            HashMap<String, Object> addrMap = (HashMap) crMap.get("address");
            Address address = new Address(addrMap);
            return !address.equals(existingDeveloper.getAddress());
        }
        return false;
    }

    private boolean isContactChanged(ChangeRequest cr) {
        Developer existingDeveloper = cr.getDeveloper();
        HashMap<String, Object> crMap = (HashMap) cr.getDetails();

        if (crMap.containsKey("contact")) {
            HashMap<String, Object> contactMap = (HashMap) crMap.get("contact");
            PointOfContact contact = new PointOfContact(contactMap);
            return !contact.equals(existingDeveloper.getContact());
        }
        return false;
    }

    private Object extractWebsiteChangesFromDetails(ChangeRequest cr) {
        HashMap<String, Object> websiteDetails = new HashMap<String, Object>();
        HashMap<String, Object> crDetails = (HashMap) cr.getDetails();
        if (crDetails.containsKey("id")) {
            websiteDetails.put("id", crDetails.get("id"));
        }

        if (crDetails.containsKey("website") && crDetails.get("website") != null) {
            websiteDetails.put("website", crDetails.get("website").toString());
        }
        return websiteDetails;
    }

    private Object extractDeveloperChangesFromDetails(ChangeRequest cr) {
        HashMap<String, Object> devDetails = new HashMap<String, Object>();
        HashMap<String, Object> crDetails = (HashMap) cr.getDetails();
        if (crDetails.containsKey("id")) {
            devDetails.put("id", crDetails.get("id"));
        }

        if (crDetails.containsKey("selfDeveloper")) {
            devDetails.put("selfDeveloper", crDetails.get("selfDeveloper"));
        }
        if (crDetails.containsKey("address")) {
            devDetails.put("address", crDetails.get("address"));
        }
        if (crDetails.containsKey("contact")) {
            devDetails.put("contact", crDetails.get("contact"));
        }
        return devDetails;
    }

    private ChangeRequest createChangeRequest(ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {

        ChangeRequestValidationContext crValidationContext = getNewValidationContext(cr, null);
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(crValidationService.validate(crValidationContext));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        // Save the base change request
        ChangeRequest newCr = createBaseChangeRequest(cr);
        // Carry over the details to the new object, so we have ids necessary
        // for saving any dependent objects
        newCr.setDetails(cr.getDetails());
        // Save the change request details
        newCr = crDetailsFactory.get(newCr.getChangeRequestType().getId()).create(newCr);
        // Get the new change request as it exists in DB
        newCr = getChangeRequest(newCr.getId());

        activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, newCr.getId(), "Change request created", null, newCr);
        return newCr;
    }

    private ChangeRequest createBaseChangeRequest(ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequest newCr = changeRequestDAO.create(cr);
        newCr.getStatuses().add(crStatusService.saveInitialStatus(newCr));
        return newCr;
    }

    private ChangeRequestValidationContext getNewValidationContext(ChangeRequest newChangeRequest, ChangeRequest originalChangeRequest) {
        return new ChangeRequestValidationContext(
                AuthUtil.getCurrentUser(),
                newChangeRequest,
                originalChangeRequest,
                resourcePermissions,
                validationUtils,
                developerDAO,
                changeRequestDAO,
                changeRequestStatusTypeDAO,
                changeRequestTypeDAO,
                attestationManager,
                websiteChangeRequestTypeId,
                developerDetailsChangeRequestTypeId,
                cancelledStatus,
                acceptedStatus,
                rejectedStatus,
                pendingAcbActionStatus,
                pendingDeveloperActionStatus);
    }
}
