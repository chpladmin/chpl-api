package gov.healthit.chpl.changerequest.manager;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestWebsiteDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class ChangeRequestWebsiteHelper {

    private ChangeRequestWebsiteDAO crWebsiteDAO;

    @Autowired
    public ChangeRequestWebsiteHelper(final ChangeRequestWebsiteDAO crWebsiteDAO) {
        this.crWebsiteDAO = crWebsiteDAO;
    }

    public ChangeRequestWebsite getByChangeRequestId(final Long changeRequestId) throws EntityRetrievalException {
        return crWebsiteDAO.getByChangeRequestId(changeRequestId);
    }

    public ChangeRequestWebsite getChangeRequestWebsiteFromHashMap(final HashMap<String, String> map) {
        ChangeRequestWebsite crWebsite = new ChangeRequestWebsite();
        if (map.containsKey("id") && StringUtils.isNumeric(map.get("id"))) {
            crWebsite.setId(new Long(map.get("id")));
        }
        if (map.containsKey("website")) {
            crWebsite.setWebsite(map.get("website"));
        }
        return crWebsite;
    }

    public ChangeRequestWebsite saveChangeRequestWebsite(final ChangeRequest cr,
            final ChangeRequestWebsite crWebsite) {
        try {
            return crWebsiteDAO.create(cr, crWebsite);
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }
}
