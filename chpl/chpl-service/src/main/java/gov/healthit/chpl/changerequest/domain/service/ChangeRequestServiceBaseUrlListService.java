package gov.healthit.chpl.changerequest.domain.service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
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
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.activity.ActivityConcept;
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
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChangeRequestServiceBaseUrlListService extends ChangeRequestDetailsService<ChangeRequestListingUrl> {
    private ChangeRequestDAO crDAO;
    private ChangeRequestListingUrlDAO crListingUrlDAO;
    private CertifiedProductManager certifiedProductManager;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private CertificationCriterionService certificationCriterionService;
    private ActivityManager activityManager;
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO;
    private ChplEmailFactory chplEmailFactory;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Value("${changeRequest.serviceBaseUrlList.approval.subject}")
    private String approvalEmailSubject;

    @Value("${changeRequest.serviceBaseUrlList.approval.body}")
    private String approvalEmailBody;

    @Value("${changeRequest.serviceBaseUrlList.rejected.subject}")
    private String rejectedEmailSubject;

    @Value("${changeRequest.serviceBaseUrlList.rejected.body}")
    private String rejectedEmailBody;

    @Value("${changeRequest.serviceBaseUrlList.pendingDeveloperAction.subject}")
    private String pendingDeveloperActionEmailSubject;

    @Value("${changeRequest.serviceBaseUrlList.pendingDeveloperAction.body}")
    private String pendingDeveloperActionEmailBody;

    @Value("${changeRequest.serviceBaseUrlList.cancelled.subject}")
    private String cancelledEmailSubject;

    @Value("${changeRequest.serviceBaseUrlList.cancelled.body}")
    private String cancelledEmailBody;

    @Autowired
    public ChangeRequestServiceBaseUrlListService(ChangeRequestDAO crDAO,
            ChangeRequestListingUrlDAO crListingUrlDAO,
            CertifiedProductManager certifiedProductManager,
            CertifiedProductDetailsManager certifiedProductDetailsManager,
            CertificationCriterionService certificationCriterionService,
            ActivityManager activityManager,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO,
            ChplEmailFactory chplEmailFactory,
            ChplHtmlEmailBuilder chplHtmlEmailBuilder,
            ResourcePermissionsFactory resourcePermissionsFactory) {
        super();
        this.crDAO = crDAO;
        this.crListingUrlDAO = crListingUrlDAO;
        this.certifiedProductManager = certifiedProductManager;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.certificationCriterionService = certificationCriterionService;
        this.activityManager = activityManager;
        this.developerCertificationBodyMapDAO = developerCertificationBodyMapDAO;
        this.chplEmailFactory = chplEmailFactory;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    @Override
    public ChangeRequestListingUrl getByChangeRequestId(Long changeRequestId, Long developerId) throws EntityRetrievalException {
        return crListingUrlDAO.getByChangeRequestId(changeRequestId);
    }

    @Override
    public ChangeRequest create(ChangeRequest cr) {
        try {
            crListingUrlDAO.create(cr, (ChangeRequestListingUrl) cr.getDetails());
            return crDAO.get(cr.getId());
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChangeRequest update(ChangeRequest cr) throws InvalidArgumentsException {
        try {
            // Get the current cr to determine if the developer details changed
            ChangeRequest crFromDb = crDAO.get(cr.getId());
            // Convert the map of key/value pairs to a ChangeRequestListingUrl object
            ChangeRequestListingUrl crListingUrl = (ChangeRequestListingUrl) cr.getDetails();
            // Use the id from the DB, not the object. Client could have changed the id.
            crListingUrl.setId(((ChangeRequestListingUrl) crFromDb.getDetails()).getId());
            cr.setDetails(crListingUrl);

            if (!((ChangeRequestListingUrl) cr.getDetails()).equals((crFromDb.getDetails()))) {
                cr.setDetails(crListingUrlDAO.update((ChangeRequestListingUrl) cr.getDetails()));

                activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, cr.getId(),
                        "Change request details updated",
                        crFromDb, cr);
            } else {
                return null;
            }
            return cr;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CertificationBody> getAssociatedCertificationBodies(ChangeRequest cr) {
        return developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(cr.getDeveloper().getId());
    }

    @Override
    @ListingStoreRemove(removeBy = RemoveBy.LISTING_ID, id = "#cr.listingId")
    protected ChangeRequest execute(ChangeRequest cr)
            throws EntityRetrievalException, EntityCreationException {
        ChangeRequestListingUrl crListingUrl = (ChangeRequestListingUrl) cr.getDetails();
        try {
            CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetails(crListingUrl.getListingId());

            listing.getCertificationResults().stream()
                    .filter(crResult -> crResult.getCriterion().getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_10)))
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
                        ((ChangeRequestListingUrl) cr.getDetails()).getUrl(),getApprovalBody(cr),
                        cr.getCurrentStatus().getComment()))
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
                        ((ChangeRequestListingUrl) cr.getDetails()).getUrl(),getApprovalBody(cr),
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

    private String getChplProductNumber(ChangeRequest cr) {
        String chplProductNumber = "";
        if (cr.getDetails() != null && ((ChangeRequestListingUrl) cr.getDetails()).getListingId() != null) {
            try {
                CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetails(((ChangeRequestListingUrl) cr.getDetails()).getListingId());
                chplProductNumber = listing.getChplProductNumber();
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not locate listing with id {}", ((ChangeRequestListingUrl) cr.getDetails()).getListingId(), e);
            }
        }
        return chplProductNumber;
    }
}
