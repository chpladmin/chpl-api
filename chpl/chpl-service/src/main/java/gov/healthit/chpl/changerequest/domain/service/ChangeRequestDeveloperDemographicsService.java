package gov.healthit.chpl.changerequest.domain.service;

import java.text.DateFormat;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDeveloperDemographicsDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDemographics;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.DeveloperManager;

@Component
public class ChangeRequestDeveloperDemographicsService extends ChangeRequestDetailsService<ChangeRequestDeveloperDemographics> {

    private ChangeRequestDAO crDAO;
    private ChangeRequestDeveloperDemographicsDAO crDeveloperDemographicsDAO;
    private DeveloperManager developerManager;
    private ActivityManager activityManager;
    private ChplEmailFactory chplEmailFactory;
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Value("${changeRequest.developerDemographics.approval.subject}")
    private String approvalEmailSubject;

    @Value("${changeRequest.developerDemographics.approval.body}")
    private String approvalEmailBody;

    @Value("${changeRequest.developerDemographics.rejected.subject}")
    private String rejectedEmailSubject;

    @Value("${changeRequest.developerDemographics.rejected.body}")
    private String rejectedEmailBody;

    @Value("${changeRequest.developerDemographics.pendingDeveloperAction.subject}")
    private String pendingDeveloperActionEmailSubject;

    @Value("${changeRequest.developerDemographics.pendingDeveloperAction.body}")
    private String pendingDeveloperActionEmailBody;

    @Autowired
    public ChangeRequestDeveloperDemographicsService(ChangeRequestDAO crDAO, ChangeRequestDeveloperDemographicsDAO crDeveloperDemographicsDAO,
            DeveloperManager developerManager, UserDeveloperMapDAO userDeveloperMapDAO,
            ActivityManager activityManager, ChplEmailFactory chplEmailFactory, ChplHtmlEmailBuilder chplHtmlEmailBuilder) {
        super(userDeveloperMapDAO);
        this.crDAO = crDAO;
        this.crDeveloperDemographicsDAO = crDeveloperDemographicsDAO;
        this.developerManager = developerManager;
        this.activityManager = activityManager;
        this.chplEmailFactory = chplEmailFactory;
        this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
    }

    @Override
    public ChangeRequestDeveloperDemographics getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return crDeveloperDemographicsDAO.getByChangeRequestId(changeRequestId);
    }

    @Override
    public ChangeRequest create(ChangeRequest cr) {
        try {
            crDeveloperDemographicsDAO.create(cr, (ChangeRequestDeveloperDemographics) cr.getDetails());
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
            // Convert the map of key/value pairs to a ChangeRequestDeveloperDetails object
            ChangeRequestDeveloperDemographics crDevDetails = (ChangeRequestDeveloperDemographics) cr.getDetails();
            // Use the id from the DB, not the object. Client could have changed the id.
            crDevDetails.setId(((ChangeRequestDeveloperDemographics) crFromDb.getDetails()).getId());
            cr.setDetails(crDevDetails);

            if (!((ChangeRequestDeveloperDemographics) cr.getDetails())
                    .equals((crFromDb.getDetails()))) {
                cr.setDetails(crDeveloperDemographicsDAO.update((ChangeRequestDeveloperDemographics) cr.getDetails()));

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
    protected ChangeRequest execute(ChangeRequest cr)
            throws EntityRetrievalException, EntityCreationException {
        ChangeRequestDeveloperDemographics crDevDetails = (ChangeRequestDeveloperDemographics) cr.getDetails();
        Developer developer = developerManager.getById(cr.getDeveloper().getId());
        if (crDevDetails.getSelfDeveloper() != null) {
            developer.setSelfDeveloper(crDevDetails.getSelfDeveloper());
        }
        developer.setWebsite(crDevDetails.getWebsite());
        if (crDevDetails.getAddress() != null) {
            Address address = developer.getAddress() != null ? developer.getAddress() : new Address();
            address.setLine1(crDevDetails.getAddress().getLine1());
            address.setLine2(crDevDetails.getAddress().getLine2());
            address.setCity(crDevDetails.getAddress().getCity());
            address.setState(crDevDetails.getAddress().getState());
            address.setZipcode(crDevDetails.getAddress().getZipcode());
            address.setCountry(crDevDetails.getAddress().getCountry());
            developer.setAddress(address);
        }
        if (crDevDetails.getContact() != null) {
            PointOfContact contact = developer.getContact() != null ? developer.getContact() : new PointOfContact();
            contact.setFullName(crDevDetails.getContact().getFullName());
            contact.setEmail(crDevDetails.getContact().getEmail());
            contact.setPhoneNumber(crDevDetails.getContact().getPhoneNumber());
            contact.setTitle(crDevDetails.getContact().getTitle());
            developer.setContact(contact);
        }
        try {
            Developer updatedDeveloper = developerManager.update(developer, false);
            cr.setDeveloper(updatedDeveloper);
            return cr;
        } catch (JsonProcessingException | ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void sendApprovalEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(getUsersForDeveloper(cr.getDeveloper().getId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String>toList()))
                .subject(approvalEmailSubject)
                .htmlMessage(createApprovalHtmlMessage(cr))
                .sendEmail();
    }

    private String createApprovalHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Demographics Change Request Approved")
                .paragraph("", String.format(approvalEmailBody,
                        df.format(cr.getSubmittedDate()),
                        formatDeveloperHtml(cr.getDeveloper()),
                        getApprovalBody(cr)))
                .footer(true)
                .build();
    }

    @Override
    protected void sendPendingDeveloperActionEmail(ChangeRequest cr) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipients(getUsersForDeveloper(cr.getDeveloper().getId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String>toList()))
                .subject(pendingDeveloperActionEmailSubject)
                .htmlMessage(createPendingDeveloperActionHtmlMessage(cr))
                .sendEmail();
    }

    private String createPendingDeveloperActionHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Demographics Change Request Pending Developer Action")
                .paragraph("", String.format(pendingDeveloperActionEmailBody,
                        df.format(cr.getSubmittedDate()),
                        formatDetailsHtml((ChangeRequestDeveloperDemographics) cr.getDetails()),
                        getApprovalBody(cr),
                        cr.getCurrentStatus().getComment()))
                .footer(true)
                .build();
    }

    @Override
    protected void sendRejectedEmail(ChangeRequest cr) throws EmailNotSentException {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        chplEmailFactory.emailBuilder()
                .recipients(getUsersForDeveloper(cr.getDeveloper().getId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String>toList()))
                .subject(rejectedEmailSubject)
                .htmlMessage(createRejectedHtmlMessage(cr))
                .sendEmail();
    }

    private String createRejectedHtmlMessage(ChangeRequest cr) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return chplHtmlEmailBuilder.initialize()
                .heading("Developer Demographics Change Request Rejected")
                .paragraph("", String.format(rejectedEmailBody,
                        df.format(cr.getSubmittedDate()),
                        formatDetailsHtml((ChangeRequestDeveloperDemographics) cr.getDetails()),
                        getApprovalBody(cr),
                        cr.getCurrentStatus().getComment()))
                .footer(true)
                .build();
    }

    private String formatDeveloperHtml(Developer dev) {
        StringBuilder devHtml = new StringBuilder("<p>Self-Developer: " + formatSelfDeveloperHtml(dev.getSelfDeveloper()) + "</p>");
        if (dev.getAddress() != null) {
            devHtml.append("<p>Address:<br/>" + formatAddressHtml(dev.getAddress()) + "</p>");
        }
        if (dev.getContact() != null) {
            devHtml.append("<p>Contact:<br/>" + formatContactHtml(dev.getContact()) + "</p>");
        }
        devHtml.append("<p>Website: " + dev.getWebsite() + "</p>");
        return devHtml.toString();
    }

    private String formatSelfDeveloperHtml(Boolean selfDeveloper) {
        String result = "No";
        if (selfDeveloper != null && selfDeveloper.equals(Boolean.TRUE)) {
            result = "Yes";
        }
        return result;
    }
    private String formatAddressHtml(Address addr) {
        String addrHtml = addr.getLine1();
        if (!StringUtils.isEmpty(addr.getLine2())) {
            addrHtml += "<br/>" + addr.getLine2();
        }
        addrHtml += "<br/>" + addr.getCity() + ", " + addr.getState() + " " + addr.getZipcode();
        if (!StringUtils.isEmpty(addr.getCountry())) {
            addrHtml += "<br/>" + addr.getCountry();
        }
        return addrHtml;
    }

    private String formatContactHtml(PointOfContact contact) {
        String contactHtml = "";
        if (!StringUtils.isEmpty(contact.getFullName())) {
            contactHtml += "Name: " + contact.getFullName();
        }
        if (!StringUtils.isEmpty(contact.getEmail())) {
            if (contactHtml.length() > 0) {
                contactHtml += "<br/>";
            }
            contactHtml += "Email: " + contact.getEmail();
        }
        if (!StringUtils.isEmpty(contact.getPhoneNumber())) {
            if (contactHtml.length() > 0) {
                contactHtml += "<br/>";
            }
            contactHtml += "Phone Number: " + contact.getPhoneNumber();
        }
        if (!StringUtils.isEmpty(contact.getTitle())) {
            if (contactHtml.length() > 0) {
                contactHtml += "<br/>";
            }
            contactHtml += "Title: " + contact.getTitle();
        }
        return contactHtml;
    }

    private String formatDetailsHtml(ChangeRequestDeveloperDemographics details) {
        StringBuilder detailsHtml = new StringBuilder("");
        if (details.getSelfDeveloper() != null) {
            detailsHtml.append("<p>Self Developer:<br/>" + formatSelfDeveloperHtml(details.getSelfDeveloper()) + "</p>");
        }
        if (details.getAddress() != null) {
            detailsHtml.append("<p>Address:<br/>" + formatAddressHtml(details.getAddress()) + "</p>");
        }
        if (details.getContact() != null) {
            detailsHtml.append("<p>Contact:<br/>" + formatContactHtml(details.getContact()) + "</p>");
        }
        if (details.getWebsite() != null) {
            detailsHtml.append("<p>Website:<br/>" + details.getWebsite() + "</p>");
        }
        return detailsHtml.toString();
    }
}
