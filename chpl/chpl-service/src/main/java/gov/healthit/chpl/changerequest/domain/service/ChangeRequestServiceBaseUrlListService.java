package gov.healthit.chpl.changerequest.domain.service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestListingUrlDAO;
import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.CertifiedProductUpdateException;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChangeRequestServiceBaseUrlListService extends ChangeRequestListingUrlService {
    private ChangeRequestDAO crDAO;
    private ChangeRequestListingUrlDAO crListingUrlDAO;
    private CertifiedProductManager certifiedProductManager;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private CertificationCriterionService certificationCriterionService;
    private ChplEmailFactory chplEmailFactory;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.submission.subject}")
    private String submissionEmailSubject;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.submission.body}")
    private String submissionEmailBody;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.approval.subject}")
    private String approvalEmailSubject;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.approval.body}")
    private String approvalEmailBody;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.rejected.subject}")
    private String rejectedEmailSubject;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.rejected.body}")
    private String rejectedEmailBody;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.pendingDeveloperAction.subject}")
    private String pendingDeveloperActionEmailSubject;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.pendingDeveloperAction.body}")
    private String pendingDeveloperActionEmailBody;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.updatedDetails.subject}")
    private String updatedDetailsEmailSubject;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.updatedDetails.body}")
    private String updatedDetailsEmailBody;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.cancelled.subject}")
    private String cancelledEmailSubject;

    @Value("${changeRequest.listingUrl.serviceBaseUrlList.cancelled.body}")
    private String cancelledEmailBody;

    @Autowired
    public ChangeRequestServiceBaseUrlListService(ChangeRequestDAO crDAO,
            ChangeRequestListingUrlDAO crListingUrlDAO,
            CertifiedProductManager certifiedProductManager,
            CertifiedProductDetailsManager certifiedProductDetailsManager,
            CertificationCriterionService certificationCriterionService,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO,
            ChplEmailFactory chplEmailFactory,
            ChplHtmlEmailBuilder chplHtmlEmailBuilder,
            ResourcePermissionsFactory resourcePermissionsFactory) {
        super(crDAO, crListingUrlDAO, certifiedProductDetailsManager, developerCertificationBodyMapDAO);
        this.crDAO = crDAO;
        this.crListingUrlDAO = crListingUrlDAO;
        this.certifiedProductManager = certifiedProductManager;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.certificationCriterionService = certificationCriterionService;
        this.chplEmailFactory = chplEmailFactory;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    @Override
    public Long create(Long changeRequestId, Object changeRequestDetails) {
        try {
            ChangeRequestListingUrl details = (ChangeRequestListingUrl) changeRequestDetails;
            // If CR details match the values from the existing listing, just return
            if (getAffectedUrl(certifiedProductDetailsManager.getCertifiedProductDetails(details.getListing().getId())).equals(details.getUrl())) {
                return null;
            }

            Long newCrId = crListingUrlDAO.create(changeRequestId, details);

            try {
                ChangeRequest changeRequestWithDetails = crDAO.get(changeRequestId);
                sendSubmittedEmail(changeRequestWithDetails);
            } catch (EmailNotSentException ex) {
                LOGGER.error("Email about Service Base URL List Change Request was not sent for change request " + changeRequestId, ex);
            }

            return newCrId;
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @ListingStoreRemove(removeBy = RemoveBy.LISTING_ID, id = "#cr.details.listingId")
    protected ChangeRequest execute(ChangeRequest cr) throws EntityRetrievalException, EntityCreationException {
        ChangeRequestListingUrl crListingUrl = (ChangeRequestListingUrl) cr.getDetails();
        try {
            CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetails(crListingUrl.getListing().getId());

            listing.getCertificationResults().stream()
                    .filter(crResult -> crResult.getCriterion().getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_10).getId()))
                    .forEach(crResult -> crResult.setServiceBaseUrlList(crListingUrl.getUrl()));

            ListingUpdateRequest listingUpdateRequest = ListingUpdateRequest.builder()
                    .listing(listing)
                    .acknowledgeBusinessErrors(true)
                    .acknowledgeWarnings(true)
                    .build();

            certifiedProductManager.update(listingUpdateRequest);
            return cr;
        } catch (MissingReasonException | InvalidArgumentsException | IOException | CertifiedProductUpdateException | ValidationException | ActivityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void sendSubmittedEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(resourcePermissionsFactory.get().getAllUsersOnDeveloper(cr.getDeveloper()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String>toList()))
                .subject(submissionEmailSubject)
                .htmlMessage(createSubmissionHtmlMessage(cr))
                .sendEmail();
    }

    private String createSubmissionHtmlMessage(ChangeRequest cr) {
        ChangeRequestListingUrl details = (ChangeRequestListingUrl) cr.getDetails();

        return chplHtmlEmailBuilder.initialize()
                .heading("Service Base URL List Change Request Submitted")
                .paragraph("", String.format(submissionEmailBody,
                        details.getUrl(),
                        getChplProductNumber(cr)))
                .footer(PublicFooter.class)
                .build();
    }

    @Override
    protected void sendApprovalEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(resourcePermissionsFactory.get().getAllUsersOnDeveloper(cr.getDeveloper()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String>toList()))
                .subject(approvalEmailSubject)
                .htmlMessage(createApprovalHtmlMessage(cr))
                .sendEmail();
    }

    private String createApprovalHtmlMessage(ChangeRequest cr) {
        return chplHtmlEmailBuilder.initialize()
                .heading("Service Base URL List Change Request Approved")
                .paragraph("", String.format(approvalEmailBody,
                        cr.getSubmittedDateTime().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                        getChplProductNumber(cr),
                        ((ChangeRequestListingUrl) cr.getDetails()).getUrl(),
                        getApprovalBody(cr)))
                .footer(PublicFooter.class)
                .build();
    }

    @Override
    protected void sendPendingDeveloperActionEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(resourcePermissionsFactory.get().getAllUsersOnDeveloper(cr.getDeveloper()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String>toList()))
                .subject(pendingDeveloperActionEmailSubject)
                .htmlMessage(createPendingDeveloperActionHtmlMessage(cr))
                .sendEmail();
    }

    private String createPendingDeveloperActionHtmlMessage(ChangeRequest cr) {
        return chplHtmlEmailBuilder.initialize()
                .heading("Service Base URL List Change Request Pending Developer Action")
                .paragraph("", String.format(pendingDeveloperActionEmailBody,
                        cr.getSubmittedDateTime().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                        getChplProductNumber(cr),
                        ((ChangeRequestListingUrl) cr.getDetails()).getUrl(), getApprovalBody(cr),
                        cr.getCurrentStatus().getComment()))
                .footer(PublicFooter.class)
                .build();
    }

    @Override
    protected void sendUpdatedDetailsEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
            .recipients(resourcePermissionsFactory.get().getAllUsersOnDeveloper(cr.getDeveloper()).stream()
                    .map(user -> user.getEmail())
                    .collect(Collectors.<String>toList()))
            .subject(updatedDetailsEmailSubject)
            .htmlMessage(createUpdatedDetailsHtmlMessage(cr))
            .sendEmail();
    }

    private String createUpdatedDetailsHtmlMessage(ChangeRequest cr) {
        return chplHtmlEmailBuilder.initialize()
                .heading("Service Base URL List Change Request Details Updated")
                .paragraph("", String.format(updatedDetailsEmailBody,
                        ((ChangeRequestListingUrl) cr.getDetails()).getUrl(),
                        getChplProductNumber(cr)))
                .footer(PublicFooter.class)
                .build();
    }

    @Override
    protected void sendRejectedEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(resourcePermissionsFactory.get().getAllUsersOnDeveloper(cr.getDeveloper()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String>toList()))
                .subject(rejectedEmailSubject)
                .htmlMessage(createRejectedHtmlMessage(cr))
                .sendEmail();
    }

    private String createRejectedHtmlMessage(ChangeRequest cr) {
        return chplHtmlEmailBuilder.initialize()
                .heading("Service Base URL List Change Request Rejected")
                .paragraph("", String.format(rejectedEmailBody,
                        cr.getSubmittedDateTime().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                        getChplProductNumber(cr),
                        ((ChangeRequestListingUrl) cr.getDetails()).getUrl(), getApprovalBody(cr),
                        cr.getCurrentStatus().getComment()))
                .footer(PublicFooter.class)
                .build();
    }

    @Override
    protected void sendCancelledEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(resourcePermissionsFactory.get().getAllUsersOnDeveloper(cr.getDeveloper()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String>toList()))
                .subject(cancelledEmailSubject)
                .htmlMessage(createCancelledHtmlMessage(cr))
                .sendEmail();
    }

    private String createCancelledHtmlMessage(ChangeRequest cr) {
        return chplHtmlEmailBuilder.initialize()
                .heading("Service Base URL List Change Request Cancelled")
                .paragraph("", String.format(cancelledEmailBody,
                        cr.getDeveloper().getName(),
                        cr.getSubmittedDateTime().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                        getChplProductNumber(cr),
                        ((ChangeRequestListingUrl) cr.getDetails()).getUrl(),
                        AuthUtil.getUsername()))
                .footer(PublicFooter.class)
                .build();
    }

    @Override
    protected String getAffectedUrl(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null) {
            return listing.getCertificationResults().stream()
                    .filter(crResult -> crResult.getCriterion().getId().equals(
                            certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_10).getId()))
                    .map(crResult -> crResult.getServiceBaseUrlList())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
