package gov.healthit.chpl.changerequest.domain.service;

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
import gov.healthit.chpl.exception.MissingReasonException;
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

    private List<UserDTO> getUsersForDeveloper(final Long developerId) {
        return userDeveloperMapDAO.getByDeveloperId(developerId).stream()
                .map(userDeveloperMap -> userDeveloperMap.getUser())
                .collect(Collectors.<UserDTO> toList());
    }

    @Override
    public ChangeRequest postStatusChangeProcessing(ChangeRequest cr) {
        try {
            if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(pendingDeveloperActionStatus)) {
                sendPendingDeveloperActionEmail(cr);
            } else if (cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(acceptedStatus)) {
                cr = execute(cr);
                sendApprovalEmail(cr);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cr;
    }

    private ChangeRequest execute(final ChangeRequest cr) throws EntityRetrievalException, EntityCreationException {
        ChangeRequestWebsite crWebsite = (ChangeRequestWebsite) cr.getDetails();
        DeveloperDTO dev = developerDAO.getById(cr.getDeveloper().getDeveloperId());
        dev.setWebsite(crWebsite.getWebsite());
        try {
            DeveloperDTO updatedDeveloper = developerManager.update(dev, false);
            cr.setDeveloper(new Developer(updatedDeveloper));
            return cr;
        } catch (JsonProcessingException | MissingReasonException | ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendApprovalEmail(final ChangeRequest cr) throws MessagingException {
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String> toList()))
                .subject(env.getProperty("changeRequest.website.approval.subject"))
                .htmlMessage(String.format(env.getProperty("changeRequest.website.approval.body"),
                        "09/22/2019",
                        "Developer User",
                        "Old Website",
                        cr.getDeveloper().getWebsite(),
                        getApprovalBody(cr)))
                .sendEmail();
    }

    private void sendPendingDeveloperActionEmail(final ChangeRequest cr) throws MessagingException {
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(cr.getDeveloper().getDeveloperId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String> toList()))
                .subject(env.getProperty("changeRequest.website.pendingDeveloperAction.subject"))
                .htmlMessage(String.format(env.getProperty("changeRequest.website.pendingDeveloperAction.body"),
                        cr.getDeveloper().getWebsite()))
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
        } else {
            return "ONC or the CHPL Admin";
        }
    }
}
