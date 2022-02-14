package gov.healthit.chpl.changerequest.domain.service;

import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

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
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ChangeRequestAttestationService extends ChangeRequestDetailsService<ChangeRequestAttestationSubmission> {
    private ChangeRequestDAO crDAO;
    private ChangeRequestAttestationDAO crAttesttionDAO;
    private AttestationManager attestationManager;
    private Environment env;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private UserDAO userDAO;

    private ObjectMapper mapper;

    @Value("${changeRequest.attestation.submitted.subject}")
    private String submittedEmailSubject;

    @Value("${changeRequest.attestation.submitted.body}")
    private String submittedEmailBody;

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
            ChplHtmlEmailBuilder chplHtmlEmailBuilder,  UserDAO userDAO, Environment env) {
        super(userDeveloperMapDAO);
        this.crDAO = crDAO;
        this.crAttesttionDAO = crAttesttionDAO;
        this.attestationManager = attestationManager;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.env = env;
        this.userDAO = userDAO;

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

            attestation.setSignatureEmail(getUserById(AuthUtil.getCurrentUser().getId()).getEmail());

            attestation.setAttestationPeriod(getAttestationPeriod(cr));

            crAttesttionDAO.create(cr, attestation);
            ChangeRequest newCr = crDAO.get(cr.getId());

            try {
                sendSubmittedEmail(newCr);
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
                .signature(attestationSubmission.getSignature())
                .signatureEmail(attestationSubmission.getSignatureEmail())
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

    private void sendSubmittedEmail(ChangeRequest cr) throws EmailNotSentException {
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(submittedEmailSubject)
                .htmlMessage(createSubmittedHtmlMessage(cr))
                .sendEmail();
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
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(pendingDeveloperActionEmailSubject)
                .htmlMessage(createPendingDeveloperActionHtmlMessage(cr))
                .sendEmail();
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

    private String createSubmittedHtmlMessage(ChangeRequest cr) {
        ChangeRequestAttestationSubmission details = (ChangeRequestAttestationSubmission) cr.getDetails();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, YYYY");
        String period = formatter.format(details.getAttestationPeriod().getPeriodStart()) + " - " + formatter.format(details.getAttestationPeriod().getPeriodEnd());
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestation Submitted")
                .paragraph("", String.format(submittedEmailBody, cr.getDeveloper().getName(), period))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(true)
                .build();
    }

    private String createAcceptedHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestation Accepted")
                .paragraph("", String.format(approvalEmailBody, df.format(cr.getSubmittedDate()), getApprovalBody(cr)))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(true)
                .build();
    }

    private String createPendingDeveloperActionHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestation Requires Developer Action")
                .paragraph("", String.format(pendingDeveloperActionEmailBody, df.format(cr.getSubmittedDate()), getApprovalBody(cr), cr.getCurrentStatus().getComment()))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(true)
                .build();
    }

    private String createRejectedHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestation Rejected")
                .paragraph("", String.format(rejectedEmailBody, df.format(cr.getSubmittedDate()), getApprovalBody(cr), cr.getCurrentStatus().getComment()))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(true)
                .build();
    }

    private String toHtmlString(ChangeRequestAttestationSubmission attestationSubmission, ChplHtmlEmailBuilder htmlBuilder) {
        List<String> headings = Arrays.asList("Condition", "Attestation", "Response");

        List<List<String>> rows = attestationSubmission.getAttestationResponses().stream()
                .sorted((r1, r2) -> r1.getAttestation().getSortOrder().compareTo(r2.getAttestation().getSortOrder()))
                .map(resp -> Arrays.asList(
                        resp.getAttestation().getCondition().getName(),
                        convertPsuedoMarkdownToHtmlLink(resp.getAttestation().getDescription()),
                        resp.getResponse().getResponse()))
                .collect(Collectors.toList());

        return htmlBuilder.getTableHtml(headings, rows, "");
    }

    private String convertPsuedoMarkdownToHtmlLink(String toConvert) {
        String regex = "^(.*)\\[(.*)\\]\\((.*)\\)(.*)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(toConvert);
        String converted = "";

        if (matcher.find() && matcher.groupCount() == 4) {
            converted = matcher.group(1) + "<a href=" + matcher.group(3) + ">" + matcher.group(2) + "</a>" + matcher.group(4);
        } else {
          converted = toConvert;
        }
        return converted;
    }

    private ChangeRequestAttestationSubmission getDetailsFromHashMap(HashMap<String, Object> map) {
        return  mapper.convertValue(map, ChangeRequestAttestationSubmission.class);
    }

    private AttestationPeriod getAttestationPeriod(ChangeRequest cr) {
        return attestationManager.getMostRecentOrCurrentAttestationPeriodForDeveloperWrtExceptions(cr.getDeveloper().getDeveloperId());
    }

    private UserDTO getUserById(Long userId) throws UserRetrievalException {
        return userDAO.getById(userId);
    }
}
