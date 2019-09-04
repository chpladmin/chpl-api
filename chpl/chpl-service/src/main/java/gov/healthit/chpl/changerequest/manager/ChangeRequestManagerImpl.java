package gov.healthit.chpl.changerequest.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationFactory;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestManagerImpl extends SecurityManager implements ChangeRequestManager {

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    private ChangeRequestDAO changeRequestDAO;
    private ChangeRequestTypeDAO changeRequestTypeDAO;
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;
    private DeveloperDAO developerDAO;
    private CertifiedProductDAO certifiedProductDAO;
    private CertificationBodyDAO certificationBodyDAO;
    private ChangeRequestCertificationBodyMapHelper crCertificationBodyMapHelper;
    private ChangeRequestStatusHelper crStatusHelper;
    private ChangeRequestWebsiteHelper crWebsiteHelper;
    private ChangeRequestValidationFactory crValidationFactory;

    @Autowired
    public ChangeRequestManagerImpl(final ChangeRequestDAO changeRequestDAO,
            final ChangeRequestTypeDAO changeRequestTypeDAO,
            final ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO, final DeveloperDAO developerDAO,
            final CertifiedProductDAO certifiedProductDAO, final CertificationBodyDAO certificationBodyDAO,
            final ChangeRequestCertificationBodyMapHelper changeRequestCertificationBodyMapHelper,
            final ChangeRequestWebsiteHelper crWebsiteHelper, final ChangeRequestStatusTypeDAO crStatusTypeDAO,
            final ChangeRequestStatusHelper crStatusHelper, final ChangeRequestValidationFactory crValidationFactory) {
        this.changeRequestDAO = changeRequestDAO;
        this.changeRequestTypeDAO = changeRequestTypeDAO;
        this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
        this.developerDAO = developerDAO;
        this.certifiedProductDAO = certifiedProductDAO;
        this.certificationBodyDAO = certificationBodyDAO;
        this.crCertificationBodyMapHelper = changeRequestCertificationBodyMapHelper;
        this.crWebsiteHelper = crWebsiteHelper;
        this.crStatusHelper = crStatusHelper;
        this.crValidationFactory = crValidationFactory;
    }

    @Override
    @Transactional
    public ChangeRequest createChangeRequest(final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException {
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runCreateValidations(cr));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        // Save the base change request
        ChangeRequest newCr = saveBaseChangeRequest(cr);
        // Save the change request details
        newCr = saveChangeRequestDetails(newCr, cr.getDetails());

        return getChangeRequest(newCr.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeRequest getChangeRequest(final Long changeRequestId) throws EntityRetrievalException {
        ChangeRequest cr = new ChangeRequest();
        cr = changeRequestDAO.get(changeRequestId);
        cr.setDetails(getChangeRequestDetails(cr));
        cr.setStatuses(crStatusHelper.getStatuses(cr.getId()));
        cr.setCertificationBodies(
                crCertificationBodyMapHelper.getCertificationBodiesByChangeRequestId(changeRequestId));
        return cr;
    }

    @Override
    @Transactional
    public ChangeRequest updateChangeRequest(final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException {
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runUpdateValidations(cr));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        ChangeRequest crFromDb = getChangeRequest(cr.getId());
        crStatusHelper.updateChangeRequestStatus(crFromDb, cr);
        return getChangeRequest(cr.getId());
    }

    private ChangeRequest saveBaseChangeRequest(final ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequest newCr = changeRequestDAO.create(cr);
        newCr.getStatuses().add(crStatusHelper.saveInitialStatus(newCr));
        saveCertificationBodies(newCr).stream()
                .map(crAcbMap -> newCr.getCertificationBodies().add(crAcbMap.getCertificationBody()));
        return newCr;
    }

    private List<ChangeRequestCertificationBodyMap> saveCertificationBodies(ChangeRequest cr)
            throws EntityRetrievalException {
        return getCertificationBodiesByDeveloper(cr.getDeveloper()).stream()
                .map(result -> crCertificationBodyMapHelper.saveCertificationBody(cr, result))
                .collect(Collectors.<ChangeRequestCertificationBodyMap> toList());
    }

    private List<CertificationBody> getCertificationBodiesByDeveloper(final Developer developer) {
        Map<Long, CertificationBody> acbs = new HashMap<Long, CertificationBody>();

        certifiedProductDAO.findByDeveloperId(developer.getDeveloperId()).stream()
                .filter(result -> !acbs.containsKey(result.getCertificationBodyId()))
                .forEach(result -> {
                    acbs.put(result.getCertificationBodyId(), getCertificationBody(result.getCertificationBodyId()));
                });
        return new ArrayList<CertificationBody>(acbs.values());
    }

    private CertificationBody getCertificationBody(final Long certificationBodyId) {
        try {
            return new CertificationBody(certificationBodyDAO.getById(certificationBodyId));
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getChangeRequestDetails(ChangeRequest cr) throws EntityRetrievalException {
        if (cr.getChangeRequestType().getId() == this.websiteChangeRequestType) {
            return crWebsiteHelper.getByChangeRequestId(cr.getId());
        } else {
            return null;
        }
    }

    private ChangeRequest saveChangeRequestDetails(final ChangeRequest cr, final Object details) {
        // Data in the "details" object is unfortunately a hashmap
        if (cr.getChangeRequestType().getId().equals(websiteChangeRequestType)) {
            ChangeRequestWebsite crWebsite = crWebsiteHelper.getChangeRequestWebsiteFromHashMap(
                    (HashMap<String, String>) details);
            cr.setDetails(crWebsiteHelper.saveChangeRequestWebsite(cr, crWebsite));
        }
        return cr;
    }

    private List<String> runCreateValidations(ChangeRequest cr) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_TYPE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.DEVELOPER));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_CREATE));
        return runValidations(rules, cr);
    }

    private List<String> runUpdateValidations(ChangeRequest cr) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_EXISTANCE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_UPDATE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.STATUS_TYPE));
        return runValidations(rules, cr);
    }

    private List<String> runValidations(List<ValidationRule<ChangeRequestValidationContext>> rules, ChangeRequest cr) {
        List<String> errorMessages = new ArrayList<String>();
        ChangeRequestValidationContext context = new ChangeRequestValidationContext(cr, changeRequestDAO,
                changeRequestTypeDAO, changeRequestStatusTypeDAO, developerDAO);

        for (ValidationRule<ChangeRequestValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }

}
