package gov.healthit.chpl.changerequest.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsFactory;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestWebsiteService;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationFactory;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestManagerImpl extends SecurityManager implements ChangeRequestManager {

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Value("${changerequest.status.accepted}")
    private Long acceptedStatus;

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    private ChangeRequestDAO changeRequestDAO;
    private ChangeRequestTypeDAO changeRequestTypeDAO;
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;
    private DeveloperDAO developerDAO;
    private ChangeRequestStatusService crStatusService;
    private ChangeRequestValidationFactory crValidationFactory;
    private ChangeRequestDetailsFactory crDetailsFactory;
    private ActivityManager activityManager;

    @Autowired
    public ChangeRequestManagerImpl(final ChangeRequestDAO changeRequestDAO,
            final ChangeRequestTypeDAO changeRequestTypeDAO,
            final ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO, final DeveloperDAO developerDAO,
            final CertifiedProductDAO certifiedProductDAO, final CertificationBodyDAO certificationBodyDAO,
            final ChangeRequestStatusTypeDAO crStatusTypeDAO, final ChangeRequestStatusService crStatusHelper,
            final ChangeRequestValidationFactory crValidationFactory, final ChangeRequestWebsiteService crWebsiteHelper,
            final ChangeRequestDetailsFactory crDetailsFactory, final ActivityManager activityManager) {
        this.changeRequestDAO = changeRequestDAO;
        this.changeRequestTypeDAO = changeRequestTypeDAO;
        this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
        this.developerDAO = developerDAO;
        this.crStatusService = crStatusHelper;
        this.crValidationFactory = crValidationFactory;
        this.crDetailsFactory = crDetailsFactory;
        this.activityManager = activityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<KeyValueModel> getChangeRequestTypes() {
        return changeRequestTypeDAO.getChangeRequestTypes().stream()
                .map(crType -> new KeyValueModel(crType.getId(), crType.getName()))
                .collect(Collectors.<KeyValueModel> toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<KeyValueModel> getChangeRequestStatusTypes() {
        return changeRequestStatusTypeDAO.getChangeRequestStatusTypes().stream()
                .map(crStatusType -> new KeyValueModel(crStatusType.getId(), crStatusType.getName()))
                .collect(Collectors.<KeyValueModel> toSet());
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).CREATE, #cr)")
    public ChangeRequest createChangeRequest(final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runCreateValidations(cr));
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

    @Override
    @Transactional(readOnly = true)
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).GET_BY_ID, returnObject)")
    public ChangeRequest getChangeRequest(final Long changeRequestId) throws EntityRetrievalException {
        ChangeRequest cr = new ChangeRequest();
        return changeRequestDAO.get(changeRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).GET_ALL, filterObject)")
    public List<ChangeRequest> getAllChangeRequestsForUser() throws EntityRetrievalException {
        return changeRequestDAO.getAll();
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).UPDATE, #cr)")
    public ChangeRequest updateChangeRequest(final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, EntityCreationException, JsonProcessingException {
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runUpdateValidations(cr));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        // Update the details
        crDetailsFactory.get(cr.getChangeRequestType().getId()).update(cr);
        // Update the status
        crStatusService.updateChangeRequestStatus(cr);

        ChangeRequest newCr = getChangeRequest(cr.getId());
        return newCr;
    }

    private ChangeRequest createBaseChangeRequest(final ChangeRequest cr) throws EntityRetrievalException {
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
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_CREATE));
        return runValidations(rules, cr);
    }

    private List<String> runUpdateValidations(ChangeRequest cr) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_EXISTENCE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_UPDATE));
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
            ChangeRequestValidationContext context = new ChangeRequestValidationContext(cr, crFromDb, changeRequestDAO,
                    changeRequestTypeDAO, changeRequestStatusTypeDAO, developerDAO);

            for (ValidationRule<ChangeRequestValidationContext> rule : rules) {
                if (!rule.isValid(context)) {
                    errorMessages.addAll(rule.getMessages());
                }
            }
            return errorMessages;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
