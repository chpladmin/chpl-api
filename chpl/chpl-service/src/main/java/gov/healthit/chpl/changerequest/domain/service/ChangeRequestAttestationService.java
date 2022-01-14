package gov.healthit.chpl.changerequest.domain.service;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestAttestationDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestation;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.DeveloperManager;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ChangeRequestAttestationService extends ChangeRequestDetailsService<ChangeRequestAttestation> {
    private ChangeRequestDAO crDAO;
    private ChangeRequestAttestationDAO crAttesttionDAO;
    private DeveloperManager developerManager;
    private ActivityManager activityManager;
    private AttestationDAO attestationDAO;
    private Environment env;

    private ObjectMapper mapper;

    @Value("${changeRequest.attestation.approval.subject}")
    private String approvalEmailSubject;

    @Value("${changeRequest.attestation.approval.body}")
    private String approvalEmailBody;

    @Value("${changeRequest.attestation.rejected.subject}")
    private String rejectedEmailSubject;

    @Value("${changeRequest.attestatiion.rejected.body}")
    private String rejectedEmailBody;

    @Value("${changeRequest.attestatiion.pendingDeveloperAction.subject}")
    private String pendingDeveloperActionEmailSubject;

    @Value("${changeRequest.attestatiion.pendingDeveloperAction.body}")
    private String pendingDeveloperActionEmailBody;

    @Autowired
    public ChangeRequestAttestationService(ChangeRequestDAO crDAO, ChangeRequestAttestationDAO crAttesttionDAO,
            DeveloperManager developerManager, UserDeveloperMapDAO userDeveloperMapDAO,
            ActivityManager activityManager, AttestationDAO attestationDAO, Environment env) {
        super(userDeveloperMapDAO);
        this.crDAO = crDAO;
        this.crAttesttionDAO = crAttesttionDAO;
        this.developerManager = developerManager;
        this.activityManager = activityManager;
        this.attestationDAO = attestationDAO;
        this.env = env;

        this.mapper = new ObjectMapper();
    }

    @Override
    public ChangeRequestAttestation getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return crAttesttionDAO.getByChangeRequestId(changeRequestId);
    }

    @Override
    @Transactional
    public ChangeRequest create(ChangeRequest cr) {
        try {
            crAttesttionDAO.create(cr, getDetailsFromHashMap((HashMap<String, Object>) cr.getDetails()));
            return crDAO.get(cr.getId());
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChangeRequest update(ChangeRequest cr) throws InvalidArgumentsException {
        try {
            /*
            // Get the current cr to determine if the attestation changed
            ChangeRequest crFromDb = crDAO.get(cr.getId());

            // Convert the map of key/value pairs to a ChangeRequestWebsite object
            ChangeRequestAttestation crAttestation = getDetailsFromHashMap((HashMap<String, Object>) cr.getDetails());

            // Use the id from the DB, not the object. Client could have changed the id.
            crAttestation.setId(((ChangeRequestAttestation) crFromDb.getDetails()).getId());
            cr.setDetails(crAttestation);

            if (!((ChangeRequestAttestation) cr.getDetails()).getAttestation().equals(((ChangeRequestAttestation) crFromDb.getDetails()).getAttestation())) {
                cr.setDetails(crAttesttionDAO.update((ChangeRequestAttestation) cr.getDetails()));

                activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, cr.getId(),
                        "Change request details updated",
                        crFromDb, cr);
            } else {
                return null;
            }
            return cr;
            */
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ChangeRequest execute(ChangeRequest cr) throws EntityRetrievalException, EntityCreationException {
        attestationDAO.create(getDetailsFromHashMap((HashMap) cr.getDetails()), cr.getDeveloper().getDeveloperId());
        // This will need to be implemented once we know what do when an attestation is approved
        LOGGER.info("Attestation Change Request has been executed.");
        return cr;
    }

    @Override
    protected void sendApprovalEmail(ChangeRequest cr) throws EmailNotSentException {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(approvalEmailSubject)
                .htmlMessage(String.format(approvalEmailBody,
                        df.format(cr.getSubmittedDate()),
                        cr.getDeveloper().getWebsite(),
                        getApprovalBody(cr)))
                .sendEmail();
    }

    @Override
    protected void sendPendingDeveloperActionEmail(ChangeRequest cr) throws EmailNotSentException {
//        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
//        new EmailBuilder(env)
//                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
//                        .map(user -> user.getEmail())
//                        .collect(Collectors.toList()))
//                .subject(pendingDeveloperActionEmailSubject)
//                .htmlMessage(String.format(pendingDeveloperActionEmailBody,
//                        df.format(cr.getSubmittedDate()),
//                        ((ChangeRequestAttestation) cr.getDetails()).getAttestation(),
//                        getApprovalBody(cr),
//                        cr.getCurrentStatus().getComment()))
//                .sendEmail();
    }

    @Override
    protected void sendRejectedEmail(ChangeRequest cr) throws EmailNotSentException {
//        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
//        new EmailBuilder(env)
//                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
//                        .map(user -> user.getEmail())
//                        .collect(Collectors.toList()))
//                .subject(rejectedEmailSubject)
//                .htmlMessage(String.format(rejectedEmailBody,
//                        df.format(cr.getSubmittedDate()),
//                        ((ChangeRequestAttestation) cr.getDetails()).getAttestation(),
//                        getApprovalBody(cr),
//                        cr.getCurrentStatus().getComment()))
//                .sendEmail();
    }

    private ChangeRequestAttestation getDetailsFromHashMap(HashMap<String, Object> map) {
        return  mapper.convertValue(map, ChangeRequestAttestation.class);
    }
}
