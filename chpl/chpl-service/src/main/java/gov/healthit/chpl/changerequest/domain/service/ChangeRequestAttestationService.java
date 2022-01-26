package gov.healthit.chpl.changerequest.domain.service;

import java.text.DateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmittedResponse;
import gov.healthit.chpl.attestation.domain.DeveloperAttestationSubmission;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.changerequest.dao.ChangeRequestAttestationDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ChangeRequestAttestationService extends ChangeRequestDetailsService<ChangeRequestAttestationSubmission> {
    private ChangeRequestDAO crDAO;
    private ChangeRequestAttestationDAO crAttesttionDAO;
    private AttestationManager attestationManager;
    private Environment env;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    private ObjectMapper mapper;

    @Value("${changeRequest.attestation.approval.subject}")
    private String approvalEmailSubject;

    @Value("${changeRequest.attestation.approval.body}")
    private String approvalEmailBody;

    @Value("${changeRequest.attestation.rejected.subject}")
    private String rejectedEmailSubject;

    @Value("${changeRequest.attestation.rejected.body}")
    private String rejectedEmailBody;

    @Value("${changeRequest.attestation.pendingDeveloperAction.subject}")
    private String pendingDeveloperActionEmailSubject;

    @Value("${changeRequest.attestation.pendingDeveloperAction.body}")
    private String pendingDeveloperActionEmailBody;

    @Autowired
    public ChangeRequestAttestationService(ChangeRequestDAO crDAO, ChangeRequestAttestationDAO crAttesttionDAO,
            UserDeveloperMapDAO userDeveloperMapDAO, AttestationManager attestationManager,
            ChplHtmlEmailBuilder chplHtmlEmailBuilder, Environment env) {
        super(userDeveloperMapDAO);
        this.crDAO = crDAO;
        this.crAttesttionDAO = crAttesttionDAO;
        this.attestationManager = attestationManager;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.env = env;

        this.mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public ChangeRequestAttestationSubmission getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return crAttesttionDAO.getByChangeRequestId(changeRequestId);
    }

    @Override
    @Transactional
    public ChangeRequest create(ChangeRequest cr) {
        try {
            ChangeRequestAttestationSubmission attestation = getDetailsFromHashMap((HashMap<String, Object>) cr.getDetails());

            attestation.setAttestationPeriod(getAttestationPeriod(cr));

            crAttesttionDAO.create(cr, attestation);
            return crDAO.get(cr.getId());
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChangeRequest update(ChangeRequest cr) throws InvalidArgumentsException {
        try {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ChangeRequest execute(ChangeRequest cr) throws EntityRetrievalException, EntityCreationException {
        ChangeRequestAttestationSubmission attestationSubmission = (ChangeRequestAttestationSubmission) cr.getDetails();

        DeveloperAttestationSubmission developerAttestation = DeveloperAttestationSubmission.builder()
                .developer(cr.getDeveloper())
                .period(attestationSubmission.getAttestationPeriod())
                .responses(attestationSubmission.getAttestationResponses().stream()
                        .map(resp -> AttestationSubmittedResponse.builder()
                                .attestation(resp.getAttestation())
                                .response(resp.getResponse())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        attestationManager.saveDeveloperAttestation(developerAttestation);
        return cr;
    }

    @Override
    protected void sendApprovalEmail(ChangeRequest cr) throws EmailNotSentException {
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(approvalEmailSubject)
                .htmlMessage(createAcceptedHtmlMessage(cr))
                .sendEmail();
    }

    @Override
    protected void sendPendingDeveloperActionEmail(ChangeRequest cr) throws EmailNotSentException {
        throw new NotImplementedException("Pending Developer Atcion Email is not implemented.");
    }

    @Override
    protected void sendRejectedEmail(ChangeRequest cr) throws EmailNotSentException {
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(rejectedEmailSubject)
                .htmlMessage(createRejectedHtmlMessage(cr))
                .sendEmail();
    }

    private String createAcceptedHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestation Approved")
                .paragraph("", String.format(approvalEmailBody, df.format(cr.getSubmittedDate()), getApprovalBody(cr)))
                .paragraph("Attestation", toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails()))
                .footer(true)
                .build();
    }

    private String createRejectedHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestation Rejected")
                .paragraph("", String.format(rejectedEmailBody, df.format(cr.getSubmittedDate()), getApprovalBody(cr), cr.getCurrentStatus().getComment()))
                .paragraph("Attestation", toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails()))
                .footer(true)
                .build();
    }

    private String toHtmlString(ChangeRequestAttestationSubmission attestationSubmission) {
        return attestationSubmission.getAttestationResponses().stream()
                .sorted((r1, r2) -> r1.getAttestation().getCondition().getSortOrder().compareTo(r2.getAttestation().getCondition().getSortOrder()))
                .map(resp -> String.format("<strong>%s</strong><br/>%s<br/><li>%s</li><br/><br/>",
                        resp.getAttestation().getCondition().getName(),
                        resp.getAttestation().getDescription(),
                        resp.getResponse().getResponse()))
                .collect(Collectors.joining());
    }

    private ChangeRequestAttestationSubmission getDetailsFromHashMap(HashMap<String, Object> map) {
        return  mapper.convertValue(map, ChangeRequestAttestationSubmission.class);
    }

    private AttestationPeriod getAttestationPeriod(ChangeRequest cr) {
        return attestationManager.getAllPeriods().stream()
                .filter(per -> LocalDate.now().compareTo(per.getSubmissionStart()) >= 0
                        && LocalDate.now().compareTo(per.getSubmissionEnd()) <= 0)
                .findAny()
                .orElse(null);
    }
}
