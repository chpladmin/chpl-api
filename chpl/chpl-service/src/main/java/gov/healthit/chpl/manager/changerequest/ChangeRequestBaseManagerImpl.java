package gov.healthit.chpl.manager.changerequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestCertificationBodyMapDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestStatusDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.domain.changerequest.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatus;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class ChangeRequestBaseManagerImpl extends SecurityManager implements ChangeRequestBaseManager {
    private final static Long INITIAL_STATUS = 1L;

    private ChangeRequestDAO changeRequestDAO;
    private ChangeRequestStatusDAO changeRequestStatusDAO;
    private CertifiedProductDAO certifiedProductDAO;
    private CertificationBodyDAO certificationBodyDAO;
    private ChangeRequestCertificationBodyMapDAO changeRequestCertificationBodyMapDAO;

    @Autowired
    public ChangeRequestBaseManagerImpl(final ChangeRequestDAO changeRequestDAO,
            final ChangeRequestStatusDAO changeRequestStatusDAO, final CertifiedProductDAO certifiedProductDAO,
            final CertificationBodyDAO certificationBodyDAO,
            final ChangeRequestCertificationBodyMapDAO changeRequestCertificationBodyMapDAO) {
        this.changeRequestDAO = changeRequestDAO;
        this.changeRequestStatusDAO = changeRequestStatusDAO;
        this.certifiedProductDAO = certifiedProductDAO;
        this.certificationBodyDAO = certificationBodyDAO;
        this.changeRequestCertificationBodyMapDAO = changeRequestCertificationBodyMapDAO;
    }

    @Override
    @Transactional
    public ChangeRequest create(final ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequest newCr = changeRequestDAO.create(cr);
        newCr.getStatuses().add(saveInitialStatus(newCr));
        saveCertificationBodies(newCr).stream()
                .forEach(crAcbMap -> newCr.getCertificationBodies().add(crAcbMap.getCertificationBody()));
        return newCr;
    }

    private ChangeRequestStatus saveInitialStatus(ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequestStatusType crStatusType = new ChangeRequestStatusType();
        crStatusType.setId(INITIAL_STATUS);

        ChangeRequestStatus crStatus = new ChangeRequestStatus();
        crStatus.setStatusChangeDate(new Date());
        crStatus.setChangeRequestStatusType(crStatusType);

        return changeRequestStatusDAO.create(cr, crStatus);
    }

    private List<ChangeRequestCertificationBodyMap> saveCertificationBodies(ChangeRequest cr)
            throws EntityRetrievalException {
        return getCertificationBodiesByDeveloper(cr.getDeveloper()).stream()
                .map(result -> saveCertificationBody(cr, result))
                .collect(Collectors.<ChangeRequestCertificationBodyMap> toList());
    }

    private ChangeRequestCertificationBodyMap saveCertificationBody(ChangeRequest cr, CertificationBody acb) {
        ChangeRequestCertificationBodyMap map = new ChangeRequestCertificationBodyMap();
        map.setCertificationBody(acb);
        map.setChangeRequest(cr);
        try {
            return changeRequestCertificationBodyMapDAO.create(map);
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    private List<CertificationBody> getCertificationBodiesByDeveloper(final Developer developer) {
        Map<Long, CertificationBody> acbs = new HashMap<Long, CertificationBody>();

        certifiedProductDAO.findByDeveloperId(developer.getDeveloperId()).stream()
                .filter(result -> !acbs.containsKey(result.getCertificationBodyId()))
                .forEach(result -> {
                    acbs.put(result.getCertificationBodyId(), getCertificationBody(result.getCertificationBodyId()));
                });
        return new ArrayList<CertificationBody>(acbs.values());
    }

    private CertificationBody getCertificationBody(final Long certificationBodyId) {
        try {
            return new CertificationBody(certificationBodyDAO.getById(certificationBodyId));
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }
}
