package gov.healthit.chpl.changerequest.manager;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.dao.ChangeRequestWebsiteDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.util.EmailBuilder;

@Component
public class ChangeRequestWebsiteHelper implements ChangeRequestDetailsHelper<ChangeRequestWebsite> {

    private ChangeRequestWebsiteDAO crWebsiteDAO;
    private DeveloperDAO developerDAO;
    private DeveloperManager developerManager;
    private UserDeveloperMapDAO userDeveloperMapDAO;
    private Environment env;

    @Autowired
    public ChangeRequestWebsiteHelper(final ChangeRequestWebsiteDAO crWebsiteDAO, final DeveloperDAO developerDAO,
            final DeveloperManager developerManager, final UserDeveloperMapDAO userDeveloperMapDAO,
            final Environment env) {
        this.crWebsiteDAO = crWebsiteDAO;
        this.developerDAO = developerDAO;
        this.developerManager = developerManager;
        this.userDeveloperMapDAO = userDeveloperMapDAO;
        this.env = env;
    }

    @Override
    public ChangeRequestWebsite getByChangeRequestId(final Long changeRequestId) throws EntityRetrievalException {
        return crWebsiteDAO.getByChangeRequestId(changeRequestId);
    }

    @Override
    public ChangeRequestWebsite getDetailsFromHashMap(final HashMap<String, Object> map) {
        ChangeRequestWebsite crWebsite = new ChangeRequestWebsite();
        if (map.containsKey("id") && StringUtils.isNumeric(map.get("id").toString())) {
            crWebsite.setId(new Long(map.get("id").toString()));
        }
        if (map.containsKey("website")) {
            crWebsite.setWebsite(map.get("website").toString());
        }
        return crWebsite;
    }

    @Override
    public ChangeRequestWebsite create(final ChangeRequest cr, final ChangeRequestWebsite crWebsite) {
        try {
            return crWebsiteDAO.create(cr, crWebsite);
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChangeRequestWebsite update(final ChangeRequest cr, final ChangeRequestWebsite crWebsite) {
        try {
            return crWebsiteDAO.update(crWebsite);
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(final ChangeRequest cr) throws EntityRetrievalException, EntityCreationException {
        ChangeRequestWebsite crWebsite = (ChangeRequestWebsite) cr.getDetails();
        DeveloperDTO dev = developerDAO.getById(cr.getDeveloper().getDeveloperId());
        dev.setWebsite(crWebsite.getWebsite());

        try {
            DeveloperDTO updatedDeveloper = developerManager.update(dev, false);
            sendApprovalEmail(updatedDeveloper);
        } catch (JsonProcessingException | MissingReasonException | ValidationException | MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendApprovalEmail(final DeveloperDTO updatedDeveloper) throws MessagingException {
        new EmailBuilder(env)
                .recipients(getUsersForDeveloper(updatedDeveloper.getId()).stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.<String> toList()))
                .subject(env.getProperty("changeRequest.website.approval.subject"))
                .htmlMessage(String.format(env.getProperty("changeRequest.website.approval.body"),
                        updatedDeveloper.getWebsite()))
                .sendEmail();

    }

    private List<UserDTO> getUsersForDeveloper(final Long developerId) {
        return userDeveloperMapDAO.getByDeveloperId(developerId).stream()
                .map(userDeveloperMap -> userDeveloperMap.getUser())
                .collect(Collectors.<UserDTO> toList());
    }
}
