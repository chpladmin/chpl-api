package gov.healthit.chpl.changerequest.manager;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.dao.ChangeRequestWebsiteDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DeveloperManager;

@Component
public class ChangeRequestWebsiteHelper implements ChangeRequestDetailsHelper<ChangeRequestWebsite> {

    private ChangeRequestWebsiteDAO crWebsiteDAO;
    private DeveloperDAO developerDAO;
    private DeveloperManager developerManager;

    @Autowired
    public ChangeRequestWebsiteHelper(final ChangeRequestWebsiteDAO crWebsiteDAO, final DeveloperDAO developerDAO,
            final DeveloperManager developerManager) {
        this.crWebsiteDAO = crWebsiteDAO;
        this.developerDAO = developerDAO;
        this.developerManager = developerManager;
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
            developerManager.update(dev, false);
        } catch (JsonProcessingException | MissingReasonException | ValidationException e) {
            throw new RuntimeException(e);
        }
    }
}
