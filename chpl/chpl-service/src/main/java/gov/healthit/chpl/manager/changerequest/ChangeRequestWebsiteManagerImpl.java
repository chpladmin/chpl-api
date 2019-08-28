package gov.healthit.chpl.manager.changerequest;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.changerequest.ChangeRequestWebsiteDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.domain.changerequest.ChangeRequestType;
import gov.healthit.chpl.domain.changerequest.ChangeRequestWebsite;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class ChangeRequestWebsiteManagerImpl implements ChangeRequestWebsiteManager {
    private static final Long WEBSITE_CHANGE_REQUEST_TYPE = 1L;
    private ChangeRequestBaseManager crBaseManager;
    private ChangeRequestWebsiteDAO crWebsiteDAO;

    @Autowired
    public ChangeRequestWebsiteManagerImpl(final ChangeRequestBaseManager crBaseManager,
            final ChangeRequestWebsiteDAO crWebsiteDAO) {
        this.crBaseManager = crBaseManager;
        this.crWebsiteDAO = crWebsiteDAO;
    }

    @Override
    @Transactional
    public ChangeRequestWebsite create(Developer developer, String website) throws EntityRetrievalException {
        ChangeRequest cr = saveBaseChangeRequest(developer);

        ChangeRequestWebsite crWebsite = new ChangeRequestWebsite();
        crWebsite.setChangeRequest(cr);
        crWebsite.setWebsite(website);
        return crWebsiteDAO.create(crWebsite);
    }

    private ChangeRequest saveBaseChangeRequest(Developer developer) throws EntityRetrievalException {
        ChangeRequest cr = new ChangeRequest();
        ChangeRequestType crType = new ChangeRequestType();
        crType.setId(WEBSITE_CHANGE_REQUEST_TYPE);
        cr.setChangeRequestType(crType);
        cr.setDeveloper(developer);
        return crBaseManager.create(cr);
    }
}
