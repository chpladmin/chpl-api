package gov.healthit.chpl.changerequest.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsFactory;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationFactory;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChangeRequestManager extends SecurityManager {

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Value("${changerequest.status.accepted}")
    private Long acceptedStatus;

    @Value("${changerequest.website}")
    private Long websiteChangeRequestTypeId;

    @Value("${changerequest.developerDetails}")
    private Long developerDetailsChangeRequestTypeId;

    @Value("${changerequest.attestation}")
    private Long attestationChangeRequestTypeId;

    private ChangeRequestDAO changeRequestDAO;
    private ChangeRequestTypeDAO changeRequestTypeDAO;
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;
    private ChangeRequestStatusService crStatusService;
    private ChangeRequestValidationFactory crValidationFactory;
    private ChangeRequestDetailsFactory crDetailsFactory;
    private DeveloperManager devManager;
    private ActivityManager activityManager;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ChangeRequestManager(ChangeRequestDAO changeRequestDAO,
            ChangeRequestTypeDAO changeRequestTypeDAO,
            ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO,
            CertifiedProductDAO certifiedProductDAO, CertificationBodyDAO certificationBodyDAO,
            ChangeRequestStatusService crStatusHelper,
            ChangeRequestValidationFactory crValidationFactory,
            ChangeRequestDetailsFactory crDetailsFactory, DeveloperManager devManager,
            ActivityManager activityManager, ResourcePermissions resourcePermissions, ErrorMessageUtil msgUtil) {
        this.changeRequestDAO = changeRequestDAO;
        this.changeRequestTypeDAO = changeRequestTypeDAO;
        this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
        this.crStatusService = crStatusHelper;
        this.crValidationFactory = crValidationFactory;
        this.crDetailsFactory = crDetailsFactory;
        this.devManager = devManager;
        this.activityManager = activityManager;
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    @Transactional(readOnly = true)
    public Set<KeyValueModel> getChangeRequestTypes() {
        return changeRequestTypeDAO.getChangeRequestTypes().stream()
                .map(crType -> new KeyValueModel(crType.getId(), crType.getName()))
                .collect(Collectors.<KeyValueModel>toSet());
    }

    @Transactional(readOnly = true)
    public Set<KeyValueModel> getChangeRequestStatusTypes() {
        return changeRequestStatusTypeDAO.getChangeRequestStatusTypes().stream()
                .map(crStatusType -> new KeyValueModel(crStatusType.getId(), crStatusType.getName()))
                .collect(Collectors.<KeyValueModel>toSet());
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).CREATE, #parentChangeRequest)")
    public List<ChangeRequest> createChangeRequests(ChangeRequest parentChangeRequest)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException {
        //get developer from db in case passed-in data is not correct
        if (parentChangeRequest.getDeveloper() == null || parentChangeRequest.getDeveloper().getDeveloperId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("changeRequest.developer.required"));
        }
        DeveloperDTO existingDeveloperDto = devManager.getById(parentChangeRequest.getDeveloper().getDeveloperId());
        Developer existingDeveloper = new Developer(existingDeveloperDto);
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
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).GET_ALL, filterObject)")
    public List<ChangeRequest> getAllChangeRequestsForUser() throws EntityRetrievalException {
        return changeRequestDAO.getAll();
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).UPDATE, #cr)")
    public ChangeRequest updateChangeRequest(ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException {
        ChangeRequest crFromDb = getChangeRequest(cr.getId());

        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runUpdateValidations(cr));
        if (cr.getChangeRequestType().getId().equals(websiteChangeRequestTypeId)) {
            validationException.getErrorMessages().addAll(runWebsiteValidations(cr));
        } else if (cr.getChangeRequestType().getId().equals(developerDetailsChangeRequestTypeId)) {
            validationException.getErrorMessages().addAll(runDeveloperDetailsValidations(cr));
        }

        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        ChangeRequest updatedDetails = null, updatedStatus = null;
        // Update the details, if the user is of role developer
        if (resourcePermissions.isUserRoleDeveloperAdmin() && cr.getDetails() != null) {
            updatedDetails =
                    crDetailsFactory.get(crFromDb.getChangeRequestType().getId()).update(cr);
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
            ChangeRequestType websiteChangeRequestType = new ChangeRequestType();
            websiteChangeRequestType.setId(websiteChangeRequestTypeId);
            ChangeRequest websiteChangeRequest = new ChangeRequest();
            websiteChangeRequest.setChangeRequestType(websiteChangeRequestType);
            websiteChangeRequest.setDeveloper(parentChangeRequest.getDeveloper());
            websiteChangeRequest.setSubmittedDate(parentChangeRequest.getSubmittedDate());
            websiteChangeRequest.setDetails(extractWebsiteChangesFromDetails(parentChangeRequest));
            changeRequests.add(websiteChangeRequest);
        } else if (isDeveloperDetailsChangeRequest(parentChangeRequest)) {
            ChangeRequestType devDetailsChangeRequestType = new ChangeRequestType();
            devDetailsChangeRequestType.setId(developerDetailsChangeRequestTypeId);
            ChangeRequest developerDetailsChangeRequest = new ChangeRequest();
            developerDetailsChangeRequest.setChangeRequestType(devDetailsChangeRequestType);
            developerDetailsChangeRequest.setDeveloper(parentChangeRequest.getDeveloper());
            developerDetailsChangeRequest.setSubmittedDate(parentChangeRequest.getSubmittedDate());
            developerDetailsChangeRequest.setDetails(extractDeveloperChangesFromDetails(parentChangeRequest));
            changeRequests.add(developerDetailsChangeRequest);
        } else if (isDeveloperAttestationChangeRequest(parentChangeRequest)) {
            ChangeRequestType attestationChangeRequestType = new ChangeRequestType();
            attestationChangeRequestType.setId(attestationChangeRequestTypeId);
            ChangeRequest attestationChangeRequest = new ChangeRequest();
            attestationChangeRequest.setChangeRequestType(attestationChangeRequestType);
            attestationChangeRequest.setDeveloper(parentChangeRequest.getDeveloper());
            attestationChangeRequest.setSubmittedDate(parentChangeRequest.getSubmittedDate());
            attestationChangeRequest.setDetails(extractAttestationsFromDetails(parentChangeRequest));
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
        // This needs to be able to identify an attestation "details" object.
        // This will probably need to be changed when the attestation object is defined
        HashMap<String, Object> crMap = (HashMap) cr.getDetails();
        return crMap.containsKey("attestation");
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

    private Object extractAttestationsFromDetails(ChangeRequest cr) {
        // This method will probably need to be changed when the attestation object is defined
        HashMap<String, Object> devDetails = new HashMap<String, Object>();
        HashMap<String, Object> crDetails = (HashMap) cr.getDetails();
        if (crDetails.containsKey("attestation")) {
            devDetails.put("attestation", crDetails.get("attestation"));
        }
        return devDetails;
    }

    private ChangeRequest createChangeRequest(ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runCreateValidations(cr));
        if (cr.getChangeRequestType().getId().equals(websiteChangeRequestTypeId)) {
            validationException.getErrorMessages().addAll(runWebsiteValidations(cr));
        } else if (cr.getChangeRequestType().getId().equals(developerDetailsChangeRequestTypeId)) {
            validationException.getErrorMessages().addAll(runDeveloperDetailsValidations(cr));
        }

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

        activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, newCr.getId(), "Change request created", null,
                newCr);
        return newCr;
    }

    private ChangeRequest createBaseChangeRequest(ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequest newCr = changeRequestDAO.create(cr);
        newCr.getStatuses().add(crStatusService.saveInitialStatus(newCr));
        return newCr;
    }

    private List<String> runCreateValidations(ChangeRequest cr) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_TYPE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_IN_PROCESS));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.DEVELOPER_EXISTENCE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.DEVELOPER_ACTIVE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_CREATE));
        return runValidations(rules, cr);
    }

    private List<String> runWebsiteValidations(ChangeRequest cr) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.WEBSITE_VALID));
        return runValidations(rules, cr);
    }

    private List<String> runDeveloperDetailsValidations(ChangeRequest cr) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.SELF_DEVELOPER_VALID));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.ADDRESS_VALID));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CONTACT_VALID));
        return runValidations(rules, cr);
    }

    private List<String> runUpdateValidations(ChangeRequest cr) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_UPDATE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.MULTIPLE_ACBS));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.DEVELOPER_ACTIVE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.STATUS_TYPE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.STATUS_NOT_UPDATABLE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.COMMENT_REQUIRED));
        return runValidations(rules, cr);
    }

    private List<String> runValidations(List<ValidationRule<ChangeRequestValidationContext>> rules, ChangeRequest cr) {
        try {
            List<String> errorMessages = new ArrayList<String>();
            ChangeRequest crFromDb = null;
            if (cr.getId() != null) {
                crFromDb = getChangeRequest(cr.getId());
            }
            ChangeRequestValidationContext context = new ChangeRequestValidationContext(cr, crFromDb);

            for (ValidationRule<ChangeRequestValidationContext> rule : rules) {
                if (rule != null && !rule.isValid(context)) {
                    errorMessages.addAll(rule.getMessages());
                }
            }
            return errorMessages;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
