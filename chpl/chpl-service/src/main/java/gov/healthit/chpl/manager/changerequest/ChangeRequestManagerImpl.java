package gov.healthit.chpl.manager.changerequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestCertificationBodyMapDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestStatusDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestWebsiteDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.domain.changerequest.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatus;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;
import gov.healthit.chpl.domain.changerequest.ChangeRequestType;
import gov.healthit.chpl.domain.changerequest.ChangeRequestWebsite;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class ChangeRequestManagerImpl extends SecurityManager implements ChangeRequestManager {

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    private ChangeRequestDAO changeRequestDAO;
    private ChangeRequestStatusDAO crStatusDAO;
    private CertifiedProductDAO certifiedProductDAO;
    private CertificationBodyDAO certificationBodyDAO;
    private ChangeRequestCertificationBodyMapDAO crCertificationBodyMapDAO;
    private ChangeRequestWebsiteDAO crWebsiteDAO;

    @Autowired
    public ChangeRequestManagerImpl(final ChangeRequestDAO changeRequestDAO,
            final ChangeRequestStatusDAO changeRequestStatusDAO, final CertifiedProductDAO certifiedProductDAO,
            final CertificationBodyDAO certificationBodyDAO,
            final ChangeRequestCertificationBodyMapDAO changeRequestCertificationBodyMapDAO,
            final ChangeRequestWebsiteDAO crWebsiteDAO) {
        this.changeRequestDAO = changeRequestDAO;
        this.crStatusDAO = changeRequestStatusDAO;
        this.certifiedProductDAO = certifiedProductDAO;
        this.certificationBodyDAO = certificationBodyDAO;
        this.crCertificationBodyMapDAO = changeRequestCertificationBodyMapDAO;
        this.crWebsiteDAO = crWebsiteDAO;
    }

    @Override
    @Transactional
    public ChangeRequest createWebsiteChangeRequest(final Developer developer, final String website)
            throws EntityRetrievalException {

        // Save the base change request
        ChangeRequest cr = new ChangeRequest();
        cr.setDeveloper(developer);
        ChangeRequestType crType = new ChangeRequestType();
        crType.setId(this.websiteChangeRequestType);
        cr.setChangeRequestType(crType);
        cr = saveBaseChangeRequest(cr);

        // Save the website change request details
        ChangeRequestWebsite crWebsite = saveWebsite(cr, website);
        cr.setDetails(crWebsite);

        return getChangeRequest(cr.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeRequest getChangeRequest(final Long changeRequestId) throws EntityRetrievalException {
        ChangeRequest cr = new ChangeRequest();
        cr = changeRequestDAO.get(changeRequestId);
        cr.setDetails(getChangeRequestDetails(cr));
        cr.setStatuses(crStatusDAO.getByChangeRequestId(changeRequestId));
        cr.setCertificationBodies(
                crCertificationBodyMapDAO.getByChangeRequestId(changeRequestId).stream()
                        .map(result -> result.getCertificationBody())
                        .collect(Collectors.<CertificationBody> toList()));
        return cr;
    }

    private ChangeRequest saveBaseChangeRequest(final ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequest newCr = changeRequestDAO.create(cr);
        newCr.getStatuses().add(saveInitialStatus(newCr));
        saveCertificationBodies(newCr).stream()
                .forEach(crAcbMap -> newCr.getCertificationBodies().add(crAcbMap.getCertificationBody()));
        return newCr;
    }

    private ChangeRequestStatus saveInitialStatus(ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequestStatusType crStatusType = new ChangeRequestStatusType();
        crStatusType.setId(this.pendingAcbActionStatus);

        ChangeRequestStatus crStatus = new ChangeRequestStatus();
        crStatus.setStatusChangeDate(new Date());
        crStatus.setChangeRequestStatusType(crStatusType);

        return crStatusDAO.create(cr, crStatus);
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
            return crCertificationBodyMapDAO.create(map);
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

    private ChangeRequestWebsite saveWebsite(ChangeRequest cr, String website) {
        ChangeRequestWebsite crWebsite = new ChangeRequestWebsite();
        crWebsite.setWebsite(website);
        try {
            return crWebsiteDAO.create(cr, crWebsite);
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getChangeRequestDetails(ChangeRequest cr) throws EntityRetrievalException {

        if (cr.getChangeRequestType().getId() == this.websiteChangeRequestType) {
            return crWebsiteDAO.getByChangeRequestId(cr.getId());
        } else {
            return null;
        }
    }
}
