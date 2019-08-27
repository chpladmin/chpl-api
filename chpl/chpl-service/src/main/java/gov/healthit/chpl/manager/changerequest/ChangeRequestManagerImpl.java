package gov.healthit.chpl.manager.changerequest;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.changerequest.ChangeRequestDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestStatusDAO;
import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatus;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class ChangeRequestManagerImpl extends SecurityManager implements ChangeRequestManager {
    private ChangeRequestDAO changeRequestDAO;
    private ChangeRequestStatusDAO changeRequestStatusDAO;

    @Autowired
    public ChangeRequestManagerImpl(final ChangeRequestDAO changeRequestDAO,
            final ChangeRequestStatusDAO changeRequestStatusDAO) {
        this.changeRequestDAO = changeRequestDAO;
        this.changeRequestStatusDAO = changeRequestStatusDAO;
    }

    @Override
    @Transactional
    public ChangeRequest create(ChangeRequest cr) throws EntityRetrievalException {
        cr = changeRequestDAO.create(cr);
        cr.getStatuses().add(createInitialStatus(cr));

        return cr;
    }

    private ChangeRequestStatus createInitialStatus(ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequestStatusType crStatusType = new ChangeRequestStatusType();
        crStatusType.setId(1l);

        ChangeRequestStatus crStatus = new ChangeRequestStatus();
        crStatus.setCommment("This is a comment for the status.");
        crStatus.setStatusChangeDate(new Date());
        crStatus.setChangeRequestStatusType(crStatusType);

        return changeRequestStatusDAO.create(cr, crStatus);
    }
}
