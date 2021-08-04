package gov.healthit.chpl.changerequest.domain.service;

import java.io.IOException;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDeveloperDetailsDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestDeveloperDetails;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.DeveloperManager;

@Component
public class ChangeRequestDeveloperDetailsService extends ChangeRequestDetailsService<ChangeRequestDeveloperDetails> {

    private ChangeRequestDAO crDAO;
    private ChangeRequestDeveloperDetailsDAO crDeveloperDetailsDao;
    private DeveloperManager developerManager;
    private ActivityManager activityManager;
    private Environment env;

    @Value("${changeRequest.developerDetails.approval.subject}")
    private String approvalEmailSubject;

    @Value("${changeRequest.developerDetails.approval.body}")
    private String approvalEmailBody;

    @Value("${changeRequest.developerDetails.rejected.subject}")
    private String rejectedEmailSubject;

    @Value("${changeRequest.developerDetails.rejected.body}")
    private String rejectedEmailBody;

    @Value("${changeRequest.developerDetails.pendingDeveloperAction.subject}")
    private String pendingDeveloperActionEmailSubject;

    @Value("${changeRequest.developerDetails.pendingDeveloperAction.body}")
    private String pendingDeveloperActionEmailBody;

    @Autowired
    public ChangeRequestDeveloperDetailsService(ChangeRequestDAO crDAO, ChangeRequestDeveloperDetailsDAO crDeveloperDetailsDao,
            DeveloperManager developerManager, UserDeveloperMapDAO userDeveloperMapDAO,
            ActivityManager activityManager, Environment env) {
        super(userDeveloperMapDAO);
        this.crDAO = crDAO;
        this.crDeveloperDetailsDao = crDeveloperDetailsDao;
        this.developerManager = developerManager;
        this.activityManager = activityManager;
        this.env = env;
    }

    @Override
    public ChangeRequestDeveloperDetails getByChangeRequestId(Long changeRequestId) throws EntityRetrievalException {
        return crDeveloperDetailsDao.getByChangeRequestId(changeRequestId);
    }

    @Override
    public ChangeRequest create(ChangeRequest cr) {
        try {
            crDeveloperDetailsDao.create(cr, getDetailsFromHashMap((HashMap<String, Object>) cr.getDetails()));
            return crDAO.get(cr.getId());
        } catch (IOException | EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChangeRequest update(ChangeRequest cr) throws InvalidArgumentsException {
        try {
            // Get the current cr to determine if the developer details changed
            ChangeRequest crFromDb = crDAO.get(cr.getId());
            // Convert the map of key/value pairs to a ChangeRequestDeveloperDetails
            // object
            ChangeRequestDeveloperDetails crDevDetails = getDetailsFromHashMap((HashMap<String, Object>) cr.getDetails());
            // Use the id from the DB, not the object. Client could have changed
            // the id.
            crDevDetails.setId(((ChangeRequestDeveloperDetails) crFromDb.getDetails()).getId());
            cr.setDetails(crDevDetails);

            if (!((ChangeRequestDeveloperDetails) cr.getDetails())
                    .equals(((ChangeRequestDeveloperDetails) crFromDb.getDetails()))) {
                cr.setDetails(crDeveloperDetailsDao.update((ChangeRequestDeveloperDetails) cr.getDetails()));

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
        ChangeRequestDeveloperDetails crDevDetails = (ChangeRequestDeveloperDetails) cr.getDetails();
        DeveloperDTO developer = developerManager.getById(cr.getDeveloper().getDeveloperId());
        if (crDevDetails.getSelfDeveloper() != null) {
            developer.setSelfDeveloper(crDevDetails.getSelfDeveloper());
        }
        if (crDevDetails.getAddress() != null) {
            AddressDTO address = developer.getAddress() != null ? developer.getAddress() : new AddressDTO();
            address.setStreetLineOne(crDevDetails.getAddress().getLine1());
            address.setStreetLineTwo(crDevDetails.getAddress().getLine2());
            address.setCity(crDevDetails.getAddress().getCity());
            address.setState(crDevDetails.getAddress().getState());
            address.setZipcode(crDevDetails.getAddress().getZipcode());
            address.setCountry(crDevDetails.getAddress().getCountry());
            developer.setAddress(address);
        }
        if (crDevDetails.getContact() != null) {
            ContactDTO contact = developer.getContact() != null ? developer.getContact() : new ContactDTO();
            contact.setFullName(crDevDetails.getContact().getFullName());
            contact.setEmail(crDevDetails.getContact().getEmail());
            contact.setPhoneNumber(crDevDetails.getContact().getPhoneNumber());
            contact.setTitle(crDevDetails.getContact().getTitle());
            developer.setContact(contact);
        }
        try {
            DeveloperDTO updatedDeveloper = developerManager.update(developer, false);
            cr.setDeveloper(new Developer(updatedDeveloper));
            return cr;
        } catch (JsonProcessingException | ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void sendApprovalEmail(ChangeRequest cr) throws MessagingException {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String>toList()))
                .subject(approvalEmailSubject)
                .htmlMessage(String.format(approvalEmailBody,
                        df.format(cr.getSubmittedDate()),
                        formatDeveloperHtml(cr.getDeveloper()),
                        getApprovalBody(cr)))
                .sendEmail();
    }

    @Override
    protected void sendPendingDeveloperActionEmail(ChangeRequest cr) throws MessagingException {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String>toList()))
                .subject(pendingDeveloperActionEmailSubject)
                .htmlMessage(String.format(pendingDeveloperActionEmailBody,
                        df.format(cr.getSubmittedDate()),
                        formatDetailsHtml((ChangeRequestDeveloperDetails) cr.getDetails()),
                        getApprovalBody(cr),
                        cr.getCurrentStatus().getComment()))
                .sendEmail();
    }

    @Override
    protected void sendRejectedEmail(ChangeRequest cr) throws MessagingException {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String>toList()))
                .subject(rejectedEmailSubject)
                .htmlMessage(String.format(rejectedEmailBody,
                        df.format(cr.getSubmittedDate()),
                        formatDetailsHtml((ChangeRequestDeveloperDetails) cr.getDetails()),
                        getApprovalBody(cr),
                        cr.getCurrentStatus().getComment()))
                .sendEmail();
    }

    private ChangeRequestDeveloperDetails getDetailsFromHashMap(HashMap<String, Object> map)
        throws IOException {
        ChangeRequestDeveloperDetails crDevDetails = new ChangeRequestDeveloperDetails();
        if (map.containsKey("id") && StringUtils.isNumeric(map.get("id").toString())) {
            crDevDetails.setId(new Long(map.get("id").toString()));
        }
        if (map.containsKey("selfDeveloper") && map.get("selfDeveloper") != null) {
            crDevDetails.setSelfDeveloper(BooleanUtils.toBooleanObject(map.get("selfDeveloper").toString()));
        }
        if (map.containsKey("address") && map.get("address") != null) {
            Address address = new Address((HashMap<String, Object>) map.get("address"));
            crDevDetails.setAddress(address);
        }
        if (map.containsKey("contact") && map.get("contact") != null) {
            PointOfContact contact = new PointOfContact((HashMap<String, Object>) map.get("contact"));
            crDevDetails.setContact(contact);
        }
        return crDevDetails;
    }

    private String formatDeveloperHtml(Developer dev) {
        String devHtml = "<p>Self-Developer: " + formatSelfDeveloperHtml(dev.getSelfDeveloper()) + "</p>";
        if (dev.getAddress() != null) {
            devHtml += "<p>Address:<br/>" + formatAddressHtml(dev.getAddress()) + "</p>";
        }
        if (dev.getContact() != null) {
            devHtml += "<p>Contact:<br/>" + formatContactHtml(dev.getContact()) + "</p>";
        }
        return devHtml;
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

    private String formatDetailsHtml(ChangeRequestDeveloperDetails details) {
        String detailsHtml = "";
        if (details.getSelfDeveloper() != null) {
            detailsHtml += "<p>Self-Developer: " + formatSelfDeveloperHtml(details.getSelfDeveloper()) + "</p>";
        }
        if (details.getAddress() != null) {
            detailsHtml += "<p>Address:<br/>" + formatAddressHtml(details.getAddress()) + "</p>";
        }
        if (details.getContact() != null) {
            detailsHtml += "<p>Contact:<br/>" + formatContactHtml(details.getContact()) + "</p>";
        }
        return detailsHtml;
    }
}
