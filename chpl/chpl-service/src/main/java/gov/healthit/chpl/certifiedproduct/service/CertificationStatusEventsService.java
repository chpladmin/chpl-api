package gov.healthit.chpl.certifiedproduct.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.comparator.CertificationStatusEventComparator;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CertificationStatusEventsService {
    private CertificationStatusEventDAO certStatusEventDao;
    private CertificationStatusDAO certStatusDao;

    private CertificationStatusEventComparator certStatusEventComparator;

    @Autowired
    public CertificationStatusEventsService(CertificationStatusEventDAO certStatusEventDao,
            CertificationStatusDAO certStatusDao) {
        this.certStatusEventDao = certStatusEventDao;
        this.certStatusDao = certStatusDao;
        this.certStatusEventComparator = new CertificationStatusEventComparator();
    }

    public List<CertificationStatusEvent> getCertificationStatusEvents(Long certifiedProductId) throws EntityRetrievalException {
        return certStatusEventDao.findByCertifiedProductId(certifiedProductId).stream()
                .map(cse -> createCertificationStatusEvent(cse))
                .sorted(certStatusEventComparator)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public CertificationStatusEvent getInitialCertificationEvent(Long listingId) {
        return certStatusEventDao.findInitialCertificationEventForCertifiedProduct(listingId);
    }

    public CertificationStatusEvent getCurrentCertificationStatusEvent(Long certifiedProductId) throws EntityRetrievalException {
        return certStatusEventDao.findByCertifiedProductId(certifiedProductId).stream()
                .map(cse -> createCertificationStatusEvent(cse))
                .sorted((event1, event2) -> Long.compare(event1.getEventDate(), event2.getEventDate()))
                .reduce((a, b) -> b) // get the last item in the list
                .orElse(null);
    }

    private CertificationStatusEvent createCertificationStatusEvent(CertificationStatusEvent certStatusEvent) {
        try {
            return CertificationStatusEvent.builder()
                    .id(certStatusEvent.getId())
                    .eventDate(certStatusEvent.getEventDate())
                    .lastModifiedUser(certStatusEvent.getLastModifiedUser())
                    .lastModifiedDate(certStatusEvent.getLastModifiedDate())
                    .reason(certStatusEvent.getReason())
                    .status(certStatusDao.getById(certStatusEvent.getStatus().getId()))
                    .build();
        } catch (EntityRetrievalException e) {
            LOGGER.error("There was an error retrieving CertificationStatus[" + certStatusEvent.getStatus().getId() + "].");
            return null;
        }
    }
}
