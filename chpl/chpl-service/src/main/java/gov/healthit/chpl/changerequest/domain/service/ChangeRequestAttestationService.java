package gov.healthit.chpl.changerequest.domain.service;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.jfree.data.time.DateRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmittedResponse;
import gov.healthit.chpl.attestation.domain.DeveloperAttestationSubmission;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.changerequest.dao.ChangeRequestAttestationDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ChangeRequestAttestationService extends ChangeRequestDetailsService<ChangeRequestAttestationSubmission> {
    private static final Integer MAX_PAGE_SIZE = 100;

    private ChangeRequestDAO crDAO;
    private ChangeRequestAttestationDAO crAttesttionDAO;
    private ChplEmailFactory chplEmailFactory;
    private AttestationManager attestationManager;
    private AttestationPeriodService attestationPeriodService;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private UserDAO userDAO;
    private DeveloperDAO developerDao;
    private ActivityManager activityManager;
    private ListingSearchService listingSearchService;
    private CertificationBodyDAO certificationBodyDAO;

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

    @Value("${changeRequest.attestation.updated.subject}")
    private String updatedEmailSubject;

    @Value("${changeRequest.attestation.updated.body}")
    private String updatedEmailBody;

    @Value("${changeRequest.attestation.withdrawn.subject}")
    private String withdrawnEmailSubject;

    @Value("${changeRequest.attestation.withdrawn.body}")
    private String withdrawnEmailBody;

    @Value("${changerequest.status.cancelledbyrequester}")
    private Long cancelledStatus;

    @Autowired
    public ChangeRequestAttestationService(ChangeRequestDAO crDAO, ChangeRequestAttestationDAO crAttesttionDAO,
            UserDeveloperMapDAO userDeveloperMapDAO, AttestationManager attestationManager,
            AttestationPeriodService attestationPeriodService, ChplEmailFactory chplEmailFactory,
            ChplHtmlEmailBuilder chplHtmlEmailBuilder, UserDAO userDAO, DeveloperDAO developerDao,
            ActivityManager activityManager, ListingSearchService listingSearchService,
            CertificationBodyDAO certificationBodyDAO) {

        super(userDeveloperMapDAO);
        this.crDAO = crDAO;
        this.crAttesttionDAO = crAttesttionDAO;
        this.attestationManager = attestationManager;
        this.attestationPeriodService = attestationPeriodService;
        this.chplEmailFactory = chplEmailFactory;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.userDAO = userDAO;
        this.developerDao = developerDao;
        this.activityManager = activityManager;
        this.listingSearchService = listingSearchService;
        this.certificationBodyDAO = certificationBodyDAO;
    }

    private List<String> activeStatuses = Stream.of(CertificationStatusType.Active.getName(),
            CertificationStatusType.SuspendedByAcb.getName(),
            CertificationStatusType.SuspendedByOnc.getName())
            .collect(Collectors.toList());

    @Override
    @Transactional
    public ChangeRequestAttestationSubmission getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return crAttesttionDAO.getByChangeRequestId(changeRequestId);
    }

    @Override
    @Transactional
    public ChangeRequest create(ChangeRequest cr) {
        try {
            ChangeRequestAttestationSubmission attestation = (ChangeRequestAttestationSubmission) cr.getDetails();
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
            ChangeRequest crFromDb = crDAO.get(cr.getId());
            ChangeRequestAttestationSubmission attestation = (ChangeRequestAttestationSubmission) cr.getDetails();

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
                cr.setDetails(crAttesttionDAO.update((ChangeRequestAttestationSubmission) cr.getDetails()));

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
        Developer beforeDeveloper = developerDao.getById(cr.getDeveloper().getId());

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
        attestationManager.deleteAttestationPeriodDeveloperExceptions(
                developerAttestation.getDeveloper().getId(),
                developerAttestation.getPeriod().getId());

        Developer updatedDeveloper = developerDao.getById(cr.getDeveloper().getId());
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
        try {
            return getListingDataForDeveloper(cr.getDeveloper()).stream()
                .filter(listing -> isListingActiveDuringPeriod(listing, attestationPeriodService.getMostRecentPastAttestationPeriod()))
                .map(listing -> listing.getCertificationBody())
                .collect(Collectors.toSet()).stream()
                .map(pair -> getCertificationBody(pair.getId()))
                .toList();
        } catch (ValidationException e) {
            LOGGER.error("Could not identify Certification Body for Developer with id: {}", cr.getDeveloper().getId());
            return null;
        }
    }

    private CertificationBody getCertificationBody(Long acbId) {
        try {
            return new CertificationBody(certificationBodyDAO.getById(acbId));
        } catch (Exception e) {
            LOGGER.error("Could not identify Certification Body with id: {}", acbId);
            return null;
        }
    }

    private List<ListingSearchResult> getListingDataForDeveloper(Developer developer) throws ValidationException {
        SearchRequest request = SearchRequest.builder()
                .certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet()))
                .developer(developer.getName())
                .pageSize(MAX_PAGE_SIZE)
                .build();
        return listingSearchService.findListings(request).getResults();
    }

    private Boolean isListingActiveDuringPeriod(ListingSearchResult listing, AttestationPeriod period) {
        List<CertificationStatusEvent> statusEvents = listing.getStatusEvents().stream()
                .map(statusEventSearchResult ->  CertificationStatusEvent.builder()
                        .status(CertificationStatus.builder()
                                .name(statusEventSearchResult.getStatus().getName())
                                .build())
                        .eventDate(toDate(statusEventSearchResult.getStatusStart()).getTime())
                        .build())
                .sorted(Comparator.comparing(CertificationStatusEvent::getEventDate))
                .toList();

        return isListingActiveDuringAttestationPeriod(statusEvents, period);
    }

    private boolean isListingActiveDuringAttestationPeriod(List<CertificationStatusEvent> statusEvents, AttestationPeriod period) {
        List<DateRange> activeDateRanges = getDateRangesWithActiveStatus(statusEvents);
        return activeDateRanges.stream()
            .filter(activeDates -> toDate(period.getPeriodStart()).getTime() <= activeDates.getUpperMillis()
                    && toDate(period.getPeriodEnd()).getTime() >= activeDates.getLowerMillis())
            .findAny().isPresent();
    }

    private Date toDate(LocalDate localDate) {
        return  DateUtil.toDate(localDate);
    }

    private List<DateRange> getDateRangesWithActiveStatus(List<CertificationStatusEvent> listingStatusEvents) {
        //Assumes statuses are sorted
        return IntStream.range(0, listingStatusEvents.size())
            .filter(i -> listingStatusEvents.get(i) != null && listingStatusEvents.get(i).getStatus() != null
                && !StringUtils.isEmpty(listingStatusEvents.get(i).getStatus().getName()))
            .filter(i -> activeStatuses.contains(listingStatusEvents.get(i).getStatus().getName()))
            .mapToObj(i -> new DateRange(new Date(listingStatusEvents.get(i).getEventDate()),
                    i < (listingStatusEvents.size() - 1) ? new Date(listingStatusEvents.get(i + 1).getEventDate())
                            //Math.max here to handle the case where status is a future date
                            : new Date(Math.max(System.currentTimeMillis(), listingStatusEvents.get(i).getEventDate()))))
            .collect(Collectors.toList());
    }











    private void sendWithdrawnDetailsEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(getUsersForDeveloper(cr.getDeveloper().getId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(withdrawnEmailSubject)
                .htmlMessage(withdrawnUpdatedHtmlMessage(cr))
                .sendEmail();
    }

    private void sendUpdatedDetailsEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(getUsersForDeveloper(cr.getDeveloper().getId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(updatedEmailSubject)
                .htmlMessage(createUpdatedHtmlMessage(cr))
                .sendEmail();
    }

    private void sendSubmittedEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(getUsersForDeveloper(cr.getDeveloper().getId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(submittedEmailSubject)
                .htmlMessage(createSubmittedHtmlMessage(cr))
                .sendEmail();
    }

    @Override
    protected void sendApprovalEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(getUsersForDeveloper(cr.getDeveloper().getId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(approvalEmailSubject)
                .htmlMessage(createAcceptedHtmlMessage(cr))
                .sendEmail();
    }

    @Override
    protected void sendPendingDeveloperActionEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(getUsersForDeveloper(cr.getDeveloper().getId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(pendingDeveloperActionEmailSubject)
                .htmlMessage(createPendingDeveloperActionHtmlMessage(cr))
                .sendEmail();
    }

    @Override
    protected void sendRejectedEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(getUsersForDeveloper(cr.getDeveloper().getId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(rejectedEmailSubject)
                .htmlMessage(createRejectedHtmlMessage(cr))
                .sendEmail();
    }

    private String withdrawnUpdatedHtmlMessage(ChangeRequest cr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, YYYY");
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestations Withdrawn")
                .paragraph("", String.format(withdrawnEmailBody,
                        cr.getDeveloper().getName(),
                        formatter.format(DateUtil.toLocalDate(cr.getSubmittedDate().getTime())),
                        AuthUtil.getUsername()))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(true)
                .build();
    }

    private String createUpdatedHtmlMessage(ChangeRequest cr) {
        ChangeRequestAttestationSubmission details = (ChangeRequestAttestationSubmission) cr.getDetails();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, YYYY");
        String period = formatter.format(details.getAttestationPeriod().getPeriodStart()) + " - " + formatter.format(details.getAttestationPeriod().getPeriodEnd());
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestations Submitted")
                .paragraph("", String.format(updatedEmailBody, cr.getDeveloper().getName(), period))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(true)
                .build();
    }

    private String createSubmittedHtmlMessage(ChangeRequest cr) {
        ChangeRequestAttestationSubmission details = (ChangeRequestAttestationSubmission) cr.getDetails();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, YYYY");
        String period = formatter.format(details.getAttestationPeriod().getPeriodStart()) + " - " + formatter.format(details.getAttestationPeriod().getPeriodEnd());
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestations Submitted")
                .paragraph("", String.format(submittedEmailBody, cr.getDeveloper().getName(), period))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(true)
                .build();
    }

    private String createAcceptedHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestations Accepted")
                .paragraph("", String.format(approvalEmailBody, df.format(cr.getSubmittedDate()), getApprovalBody(cr)))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(true)
                .build();
    }

    private String createPendingDeveloperActionHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Action Required")
                .paragraph("", String.format(pendingDeveloperActionEmailBody, df.format(cr.getSubmittedDate()), getApprovalBody(cr), cr.getCurrentStatus().getComment()))
                .paragraph("Attestation Responses submitted for " + cr.getDeveloper().getName(), toHtmlString((ChangeRequestAttestationSubmission) cr.getDetails(), chplHtmlEmailBuilder))
                .footer(true)
                .build();
    }

    private String createRejectedHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Attestations Rejected")
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
        return !((ChangeRequestAttestationSubmission) updatedCr.getDetails()).equals((originalCr.getDetails()));
    }
}
