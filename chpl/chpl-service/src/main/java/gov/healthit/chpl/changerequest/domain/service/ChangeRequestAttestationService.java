package gov.healthit.chpl.changerequest.domain.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.manager.AttestationCertificationBodyService;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.changerequest.dao.ChangeRequestAttestationDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.service.email.AttestationEmails;
import gov.healthit.chpl.changerequest.entity.ChangeRequestAttestationSubmissionResponseEntity;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.form.FormItem;
import gov.healthit.chpl.form.FormService;
import gov.healthit.chpl.form.SectionHeading;
import gov.healthit.chpl.form.validation.FormValidator;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ChangeRequestAttestationService extends ChangeRequestDetailsService<ChangeRequestAttestationSubmission> {
    private static final Integer MAX_PAGE_SIZE = 100;

    private ChangeRequestDAO crDAO;
    private ChangeRequestAttestationDAO crAttestationDAO;
    private AttestationManager attestationManager;
    private AttestationPeriodService attestationPeriodService;
    private UserDAO userDAO;
    private DeveloperDAO developerDAO;
    private ActivityManager activityManager;
    private AttestationCertificationBodyService attestationCertificationBodyService;
    private FormValidator formValidator;
    private FormService formService;
    private AttestationEmails attestationEmails;

    @Value("${changerequest.status.cancelledbyrequester}")
    private Long cancelledStatus;

    @Autowired
    public ChangeRequestAttestationService(ChangeRequestDAO crDAO, ChangeRequestAttestationDAO crAttestationDAO,
            UserDeveloperMapDAO userDeveloperMapDAO, AttestationManager attestationManager,
            AttestationPeriodService attestationPeriodService, UserDAO userDAO, DeveloperDAO developerDAO,
            ActivityManager activityManager, FormService formService, FormValidator formValidator,
            AttestationEmails attestationEmails, AttestationCertificationBodyService atttesAttestationCertificationBodyService) {
        super(userDeveloperMapDAO);
        this.crDAO = crDAO;
        this.crAttestationDAO = crAttestationDAO;
        this.attestationManager = attestationManager;
        this.attestationPeriodService = attestationPeriodService;
        this.userDAO = userDAO;
        this.developerDAO = developerDAO;
        this.activityManager = activityManager;
        this.attestationCertificationBodyService = atttesAttestationCertificationBodyService;
        this.formService = formService;
        this.formValidator = formValidator;
        this.attestationEmails = attestationEmails;
    }

    @Override
    @Transactional
    public ChangeRequestAttestationSubmission getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        ChangeRequestAttestationSubmission cras = crAttestationDAO.getByChangeRequestId(changeRequestId);
        cras.setForm(getPopulatedForm(cras));
        return cras;
    }

    @Override
    @Transactional
    public ChangeRequest create(ChangeRequest cr) {
        try {
            ChangeRequestAttestationSubmission attestation = (ChangeRequestAttestationSubmission) cr.getDetails();
            attestation.setSignatureEmail(getUserById(AuthUtil.getCurrentUser().getId()).getEmail());
            attestation.setAttestationPeriod(getAttestationPeriod(cr));
            ChangeRequestAttestationSubmission createdAttestation = crAttestationDAO.create(cr, attestation);

            List<FormItem> rolledUpFormItems = attestation.getForm().extractFlatFormItems();
            crAttestationDAO.addResponsesToChangeRequestAttestationSubmission(createdAttestation, rolledUpFormItems);

            ChangeRequest newCr = crDAO.get(cr.getId());

            try {
                attestationEmails.getSubmittedEmail().send(newCr);
            } catch (EmailNotSentException e) {
                LOGGER.error(e);
            }

            return newCr;
        } catch (EntityRetrievalException | UserRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChangeRequest update(ChangeRequest cr) throws InvalidArgumentsException {
        try {
            ChangeRequest crFromDb = crDAO.get(cr.getId());
            ChangeRequestAttestationSubmission attestation = (ChangeRequestAttestationSubmission) cr.getDetails();
            attestation.setForm(formValidator.removePhantomAndDuplicateResponses(attestation.getForm()));

            // Use the id from the DB, not the object. Client could have changed the id.
            attestation.setId(((ChangeRequestAttestationSubmission) crFromDb.getDetails()).getId());
            cr.setDetails(attestation);

            //The Attestation period is not editable - use from the existing details
            ((ChangeRequestAttestationSubmission) cr.getDetails()).setAttestationPeriod(
                    ((ChangeRequestAttestationSubmission) crFromDb.getDetails()).getAttestationPeriod());

            //Get email that based on current user
            ((ChangeRequestAttestationSubmission) cr.getDetails()).setSignatureEmail(getUserById(AuthUtil.getCurrentUser().getId()).getEmail());

            if (isNewCurrentStatusCancelledByRequestor(cr, crFromDb)) {
                sendWithdrawnDetailsEmail(cr);
                activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, cr.getId(),
                        "Change request cancelled by requestor",
                        crFromDb, cr);
            } else if (haveDetailsBeenUpdated(cr, crFromDb)) {

                crAttestationDAO.update(cr, (ChangeRequestAttestationSubmission) cr.getDetails());
                cr.setDetails(getByChangeRequestId(cr.getId()));

                activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, cr.getId(),
                        "Change request details updated",
                        crFromDb, cr);

                sendUpdatedDetailsEmail(cr);
            } else {
                return null;
            }
            return cr;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @ListingStoreRemove(removeBy = RemoveBy.DEVELOPER_ID, id = "#cr.developer.id")
    protected ChangeRequest execute(ChangeRequest cr) throws EntityRetrievalException, EntityCreationException {
        Developer beforeDeveloper = developerDAO.getById(cr.getDeveloper().getId());
        ChangeRequestAttestationSubmission changeRequestAttestationSubmission = (ChangeRequestAttestationSubmission) cr.getDetails();

        AttestationSubmission developerAttestation = AttestationSubmission.builder()
                .developerId(cr.getDeveloper().getId())
                .attestationPeriod(changeRequestAttestationSubmission.getAttestationPeriod())
                .signature(changeRequestAttestationSubmission.getSignature())
                .signatureEmail(changeRequestAttestationSubmission.getSignatureEmail())
                .form(changeRequestAttestationSubmission.getForm())
                .build();

        attestationManager.saveDeveloperAttestation(cr.getDeveloper().getId(), developerAttestation);
        attestationManager.deleteAttestationPeriodDeveloperExceptions(cr.getDeveloper().getId(), developerAttestation.getAttestationPeriod().getId());

        Developer updatedDeveloper = developerDAO.getById(cr.getDeveloper().getId());
        try {
            activityManager.addActivity(ActivityConcept.DEVELOPER, updatedDeveloper.getId(),
                "Developer attestation created.", beforeDeveloper, updatedDeveloper);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Error writing activity about attestation submission approval.", ex);
        }
        return cr;
    }

    @Override
    public List<CertificationBody> getAssociatedCertificationBodies(ChangeRequest cr) {
        return attestationCertificationBodyService.getAssociatedCertificationBodies(
                cr.getDeveloper().getId(), attestationPeriodService.getSubmittableAttestationPeriod(cr.getDeveloper().getId()).getId());
    }

    private void sendWithdrawnDetailsEmail(ChangeRequest cr) throws EmailNotSentException {
        attestationEmails.getWithdrawnEmail().send(cr);
    }

    private void sendUpdatedDetailsEmail(ChangeRequest cr) throws EmailNotSentException {
        attestationEmails.getUpdatedEmail().send(cr);
    }

    @Override
    protected void sendApprovalEmail(ChangeRequest cr) throws EmailNotSentException {
        attestationEmails.getAcceptedEmail().send(cr);
    }

    @Override
    protected void sendPendingDeveloperActionEmail(ChangeRequest cr) throws EmailNotSentException {
        attestationEmails.getPendingDeveloperActionEmail().send(cr);
    }

    @Override
    protected void sendRejectedEmail(ChangeRequest cr) throws EmailNotSentException {
        attestationEmails.getRejectedEmail().send(cr);
    }

    private AttestationPeriod getAttestationPeriod(ChangeRequest cr) {
        return attestationPeriodService.getSubmittableAttestationPeriod(cr.getDeveloper().getId());
    }

    private UserDTO getUserById(Long userId) throws UserRetrievalException {
        return userDAO.getById(userId);
    }

    private Boolean isNewCurrentStatusCancelledByRequestor(ChangeRequest updatedCr, ChangeRequest originalCr) {
        return updatedCr.getCurrentStatus().getChangeRequestStatusType().getId().equals(cancelledStatus)
                && !originalCr.getCurrentStatus().getChangeRequestStatusType().getId().equals(cancelledStatus);
    }

    private Boolean haveDetailsBeenUpdated(ChangeRequest updatedCr, ChangeRequest originalCr) {
        ChangeRequestAttestationSubmission updated = (ChangeRequestAttestationSubmission) updatedCr.getDetails();
        ChangeRequestAttestationSubmission orig = (ChangeRequestAttestationSubmission) originalCr.getDetails();
        return !orig.isEqual(updated);
    }

    private Form getPopulatedForm(ChangeRequestAttestationSubmission submission) {
        try {
            List<ChangeRequestAttestationSubmissionResponseEntity> submittedResponses =
                    crAttestationDAO.getChangeRequestAttestationSubmissionResponseEntities(submission.getId());

            Form form = formService.getForm(submission.getAttestationPeriod().getForm().getId());
            for (SectionHeading heading : form.getSectionHeadings()) {
                heading.setFormItems(populateFormItemsWithSubmittedResponses(heading.getFormItems(), submittedResponses));
            }

            return form;
        } catch (EntityRetrievalException e) {
            return null;
        }
    }

    private List<FormItem> populateFormItemsWithSubmittedResponses(List<FormItem> formItems, List<ChangeRequestAttestationSubmissionResponseEntity> submittedResponses) {
        for (FormItem fi : formItems) {
            fi.setSubmittedResponses(submittedResponses.stream()
                    .filter(sr -> sr.getFormItem().getId().equals(fi.getId()))
                    .map(sr -> sr.getResponse().toDomain())
                    .toList());

            fi.setChildFormItems(populateFormItemsWithSubmittedResponses(fi.getChildFormItems(), submittedResponses));
        }
        return formItems;
    }

}
