package gov.healthit.chpl.changerequest.domain.service;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestWebsiteDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.util.EmailBuilder;

@Component
public class ChangeRequestWebsiteService implements ChangeRequestDetailsService<ChangeRequestWebsite> {

    private ChangeRequestDAO crDAO;
    private ChangeRequestWebsiteDAO crWebsiteDAO;
    private DeveloperDAO developerDAO;
    private DeveloperManager developerManager;
    private UserDeveloperMapDAO userDeveloperMapDAO;
    private ActivityManager activityManager;
    private Environment env;

    @Value("${changerequest.status.pendingdeveloperaction}")
    private Long pendingDeveloperActionStatus;

    @Value("${changerequest.status.accepted}")
    private Long acceptedStatus;

    @Value("${changerequest.status.rejected}")
    private Long rejectedStatus;

    @Value("${user.permission.onc}")
    private Long oncPermission;

    @Value("${user.permission.admin}")
    private Long adminPermission;

    @Value("${changeRequest.website.approval.subject}")
    private String approvalEmailSubject;

    @Value("${changeRequest.website.approval.body}")
    private String approvalEmailBody;

    @Value("${changeRequest.website.rejected.subject}")
    private String rejectedEmailSubject;

    @Value("${changeRequest.website.rejected.body}")
    private String rejectedEmailBody;

    @Value("${changeRequest.website.pendingDeveloperAction.subject}")
    private String pendingDeveloperActionEmailSubject;

    @Value("${changeRequest.website.pendingDeveloperAction.body}")
    private String pendingDeveloperActionEmailBody;

    @Autowired
    public ChangeRequestWebsiteService(final ChangeRequestDAO crDAO, final ChangeRequestWebsiteDAO crWebsiteDAO,
            final DeveloperDAO developerDAO, final DeveloperManager developerManager,
            final UserDeveloperMapDAO userDeveloperMapDAO, final ActivityManager activityManager,
            final Environment env) {
        this.crDAO = crDAO;
        this.crWebsiteDAO = crWebsiteDAO;
        this.developerDAO = developerDAO;
        this.developerManager = developerManager;
        this.userDeveloperMapDAO = userDeveloperMapDAO;
        this.activityManager = activityManager;
        this.env = env;
    }

    @Override
    public ChangeRequestWebsite getByChangeRequestId(final Long changeRequestId) throws EntityRetrievalException {
        return crWebsiteDAO.getByChangeRequestId(changeRequestId);
    }

    @Override
    public ChangeRequest create(final ChangeRequest cr) {
        try {
            crWebsiteDAO.create(cr, getDetailsFromHashMap((HashMap<String, Object>) cr.getDetails()));
            return crDAO.get(cr.getId());
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChangeRequest update(final ChangeRequest cr) {
        try {
            // Get the current cr to determine if the website changed
            ChangeRequest crFromDb = crDAO.get(cr.getId());
            // Convert the map of key/value pairs to a ChangeRequestWebsite
            // object
            ChangeRequestWebsite crWebsite = getDetailsFromHashMap((HashMap<String, Object>) cr.getDetails());
            // Use the id from the DB, not the object. Client could have changed
            // the id.
            crWebsite.setId(((ChangeRequestWebsite) crFromDb.getDetails()).getId());
            cr.setDetails(crWebsite);

            if (!((ChangeRequestWebsite) cr.getDetails()).getWebsite()
                    .equals(((ChangeRequestWebsite) crFromDb.getDetails()).getWebsite())) {
                cr.setDetails(crWebsiteDAO.update((ChangeRequestWebsite) cr.getDetails()));

                activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, cr.getId(),
                        "Change request details updated",
                        crFromDb, cr);
            }
            return cr;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChangeRequest postStatusChangeProcessing(ChangeRequest cr) {
        try {
            if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(pendingDeveloperActionStatus)) {
                sendPendingDeveloperActionEmail(cr);
            } else if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(rejectedStatus)) {
                sendRejectedEmail(cr);
            } else if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(acceptedStatus)) {
                cr = execute(cr);
                sendApprovalEmail(cr);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cr;
    }

    private ChangeRequest execute(final ChangeRequest cr)
            throws EntityRetrievalException, EntityCreationException {
        ChangeRequestWebsite crWebsite = (ChangeRequestWebsite) cr.getDetails();
        DeveloperDTO developer = developerDAO.getById(cr.getDeveloper().getDeveloperId());
        developer.setWebsite(crWebsite.getWebsite());
        try {
            DeveloperDTO updatedDeveloper = developerManager.update(developer, false);
            cr.setDeveloper(new Developer(updatedDeveloper));
            return cr;
        } catch (JsonProcessingException | ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendApprovalEmail(final ChangeRequest cr) throws MessagingException {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String> toList()))
                .subject(approvalEmailSubject)
                .htmlMessage(String.format(approvalEmailBody,
                        df.format(cr.getSubmittedDate()),
                        cr.getDeveloper().getWebsite(),
                        getApprovalBody(cr)))
                .sendEmail();
    }

    private void sendPendingDeveloperActionEmail(final ChangeRequest cr) throws MessagingException {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String> toList()))
                .subject(pendingDeveloperActionEmailSubject)
                .htmlMessage(String.format(pendingDeveloperActionEmailBody,
                        df.format(cr.getSubmittedDate()),
                        ((ChangeRequestWebsite) cr.getDetails()).getWebsite(),
                        getApprovalBody(cr),
                        cr.getCurrentStatus().getComment()))
                .sendEmail();
    }

    private void sendRejectedEmail(final ChangeRequest cr) throws MessagingException {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String> toList()))
                .subject(rejectedEmailSubject)
                .htmlMessage(String.format(rejectedEmailBody,
                        df.format(cr.getSubmittedDate()),
                        ((ChangeRequestWebsite) cr.getDetails()).getWebsite(),
                        getApprovalBody(cr),
                        cr.getCurrentStatus().getComment()))
                .sendEmail();
    }

    private ChangeRequestWebsite getDetailsFromHashMap(final HashMap<String, Object> map) {
        ChangeRequestWebsite crWebsite = new ChangeRequestWebsite();
        if (map.containsKey("id") && StringUtils.isNumeric(map.get("id").toString())) {
            crWebsite.setId(new Long(map.get("id").toString()));
        }
        if (map.containsKey("website")) {
            crWebsite.setWebsite(map.get("website").toString());
        }
        return crWebsite;
    }

    private String getApprovalBody(final ChangeRequest cr) {
        if (cr.getCurrentStatus().getCertificationBody() != null) {
            return cr.getCurrentStatus().getCertificationBody().getName();
        } else if (cr.getCurrentStatus().getUserPermission().getId().equals(adminPermission)) {
            return "the CHPL Admin";
        } else if (cr.getCurrentStatus().getUserPermission().getId().equals(oncPermission)) {
            return "ONC";
        } else {
            return "";
        }
    }

    private List<UserDTO> getUsersForDeveloper(final Long developerId) {
        return userDeveloperMapDAO.getByDeveloperId(developerId).stream()
                .map(userDeveloperMap -> userDeveloperMap.getUser())
                .collect(Collectors.<UserDTO> toList());
    }

}
