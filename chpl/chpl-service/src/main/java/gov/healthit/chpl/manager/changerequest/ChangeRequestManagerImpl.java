package gov.healthit.chpl.manager.changerequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestCertificationBodyMapDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestStatusDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.dao.changerequest.ChangeRequestWebsiteDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.domain.changerequest.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatus;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;
import gov.healthit.chpl.domain.changerequest.ChangeRequestWebsite;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;

@Component
public class ChangeRequestManagerImpl extends SecurityManager implements ChangeRequestManager {

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Value("${changerequest.status.cancelledbyrequester}")
    private Long cancelledByRequesterStatus;

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    private ChangeRequestDAO changeRequestDAO;
    private ChangeRequestStatusDAO crStatusDAO;
    private CertifiedProductDAO certifiedProductDAO;
    private CertificationBodyDAO certificationBodyDAO;
    private ChangeRequestCertificationBodyMapDAO crCertificationBodyMapDAO;
    private ChangeRequestWebsiteDAO crWebsiteDAO;
    private ChangeRequestStatusTypeDAO crStatusTypeDAO;
    private ResourcePermissions resourcePermissions;

    @Autowired
    public ChangeRequestManagerImpl(final ChangeRequestDAO changeRequestDAO,
            final ChangeRequestStatusDAO changeRequestStatusDAO, final CertifiedProductDAO certifiedProductDAO,
            final CertificationBodyDAO certificationBodyDAO,
            final ChangeRequestCertificationBodyMapDAO changeRequestCertificationBodyMapDAO,
            final ChangeRequestWebsiteDAO crWebsiteDAO, final ChangeRequestStatusTypeDAO crStatusTypeDAO,
            ResourcePermissions resourcePermissions) {
        this.changeRequestDAO = changeRequestDAO;
        this.crStatusDAO = changeRequestStatusDAO;
        this.certifiedProductDAO = certifiedProductDAO;
        this.certificationBodyDAO = certificationBodyDAO;
        this.crCertificationBodyMapDAO = changeRequestCertificationBodyMapDAO;
        this.crWebsiteDAO = crWebsiteDAO;
        this.crStatusTypeDAO = crStatusTypeDAO;
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    @Transactional
    public ChangeRequest createWebsiteChangeRequest(final ChangeRequest cr) throws EntityRetrievalException {
        // Save the base change request
        ChangeRequest newCr = saveBaseChangeRequest(cr);

        // Save the website change request details
        newCr = saveChangeRequestDetails(newCr, cr.getDetails());

        return getChangeRequest(newCr.getId());
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

    @Override
    @Transactional
    public ChangeRequest updateChangeRequest(final ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequest crFromDb = getChangeRequest(cr.getId());
        updateChangeRequestStatus(crFromDb, cr);
        return getChangeRequest(cr.getId());
    }

    private ChangeRequest saveBaseChangeRequest(final ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequest newCr = changeRequestDAO.create(cr);
        newCr.getStatuses().add(saveInitialStatus(newCr));
        saveCertificationBodies(newCr).stream()
                .map(crAcbMap -> newCr.getCertificationBodies().add(crAcbMap.getCertificationBody()));
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

    private Object getChangeRequestDetails(ChangeRequest cr) throws EntityRetrievalException {
        if (cr.getChangeRequestType().getId() == this.websiteChangeRequestType) {
            return crWebsiteDAO.getByChangeRequestId(cr.getId());
        } else {
            return null;
        }
    }

    private void updateChangeRequestStatus(ChangeRequest crFromDb, ChangeRequest crFromCaller)
            throws EntityRetrievalException {
        // Check for nulls - a Java 8 way to check a chain of objects for null
        Long statusTypeIdFromDB = Optional.ofNullable(crFromDb)
                .map(ChangeRequest::getCurrentStatus)
                .map(ChangeRequestStatus::getChangeRequestStatusType)
                .map(ChangeRequestStatusType::getId)
                .orElse(null);
        Long statusTypeIdFromCaller = Optional.ofNullable(crFromCaller)
                .map(ChangeRequest::getCurrentStatus)
                .map(ChangeRequestStatus::getChangeRequestStatusType)
                .map(ChangeRequestStatusType::getId)
                .orElse(null);

        if (statusTypeIdFromDB != null && statusTypeIdFromCaller != null
                && statusTypeIdFromDB != statusTypeIdFromCaller
                && isStatusChangeValid(statusTypeIdFromDB, statusTypeIdFromCaller)) {
            saveNewStatusForChangeRequest(crFromDb, statusTypeIdFromCaller,
                    crFromCaller.getCurrentStatus().getComment());
        }
    }

    private boolean isStatusChangeValid(final Long previousStatusTypeId, final Long newStatusTypeId) {
        // Does this status type id exist?
        try {
            crStatusTypeDAO.getChangeRequestStatusTypeById(newStatusTypeId);
        } catch (EntityRetrievalException e) {
            return false;
        }

        // Cannot change status is Cancelled
        if (previousStatusTypeId == this.cancelledByRequesterStatus) {
            return false;
        }
        return true;
    }

    private ChangeRequestStatus saveNewStatusForChangeRequest(final ChangeRequest cr, final Long crStatusTypeId,
            final String comment) throws EntityRetrievalException {
        ChangeRequestStatus crStatus = new ChangeRequestStatus();
        ChangeRequestStatusType crStatusType = new ChangeRequestStatusType();
        crStatusType.setId(crStatusTypeId);
        crStatus.setChangeRequestStatusType(crStatusType);
        crStatus.setComment(comment);
        crStatus.setStatusChangeDate(new Date());
        crStatus.setCertificationBody(getCurrentUserCertificationBody());

        return crStatusDAO.create(cr, crStatus);
    }

    private CertificationBody getCurrentUserCertificationBody() {
        List<CertificationBodyDTO> acbs = resourcePermissions.getAllAcbsForCurrentUser();
        if (acbs.size() == 0) {
            return null;
        } else {
            return new CertificationBody(acbs.get(0));
        }
    }

    private ChangeRequest saveChangeRequestDetails(final ChangeRequest cr, final Object details) {
        // Data in the "details" object is unfortunately a hashmap
        if (cr.getChangeRequestType().getId().equals(websiteChangeRequestType)) {
            ChangeRequestWebsite crWebsite = getChangeRequestWebsiteFromHashMap(
                    (HashMap<String, String>) details);
            cr.setDetails(saveChangeRequestWebsite(cr, crWebsite));
        }
        return cr;
    }

    private ChangeRequestWebsite saveChangeRequestWebsite(final ChangeRequest cr,
            final ChangeRequestWebsite crWebsite) {
        try {
            return crWebsiteDAO.create(cr, crWebsite);
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    private ChangeRequestWebsite getChangeRequestWebsiteFromHashMap(final HashMap<String, String> map) {
        ChangeRequestWebsite crWebsite = new ChangeRequestWebsite();
        if (map.containsKey("id") && StringUtils.isNumeric(map.get("id"))) {
            crWebsite.setId(new Long(map.get("id")));
        }
        if (map.containsKey("website")) {
            crWebsite.setWebsite(map.get("website"));
        }
        return crWebsite;
    }
}
