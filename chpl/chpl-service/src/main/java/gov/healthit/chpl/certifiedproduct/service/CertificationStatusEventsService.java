package gov.healthit.chpl.certifiedproduct.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Objects;

import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
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
        LocalDate today = LocalDate.now();
        return certStatusEventDao.findByCertifiedProductId(certifiedProductId).stream()
                .map(cse -> createCertificationStatusEvent(cse))
                .filter(cse -> cse.getEventDay().isEqual(today) || cse.getEventDay().isBefore(today))
                .max(new CertificationStatusEventComparator())
                .get();
    }

    public List<CertificationStatusEvent> getAddedCertificationStatusEvents(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        return subtractLists(updatedListing.getCertificationEvents(), existingListing.getCertificationEvents());
    }

    public List<CertificationStatusEvent> getAddedCertificationStatusEventsIgnoringReasonUpdates(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        return subtractListsIgnoringReasonUpdates(updatedListing.getCertificationEvents(), existingListing.getCertificationEvents());
    }

    public List<CertificationStatusEvent> getRemovedCertificationStatusEvents(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        return subtractLists(existingListing.getCertificationEvents(), updatedListing.getCertificationEvents());
    }

    private List<CertificationStatusEvent> subtractLists(List<CertificationStatusEvent> listA, List<CertificationStatusEvent> listB) {
        Predicate<CertificationStatusEvent> notInListB = eventFromA -> !listB.stream()
                .anyMatch(event -> doValuesMatch(eventFromA, event));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private List<CertificationStatusEvent> subtractListsIgnoringReasonUpdates(List<CertificationStatusEvent> listA, List<CertificationStatusEvent> listB) {
        Predicate<CertificationStatusEvent> notInListB = eventFromA -> !listB.stream()
                .anyMatch(event -> doValuesMatchIgnoringReason(eventFromA, event));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private boolean doValuesMatch(CertificationStatusEvent event1, CertificationStatusEvent event2) {
        return ((event1.getStatus() != null && event2.getStatus() != null
                    && StringUtils.equals(event1.getStatus().getName(), event2.getStatus().getName()))
                        || Objects.equal(event1.getCertificationStatusId(), event2.getCertificationStatusId()))
                && (Objects.equal(event1.getEventDay(), event2.getEventDay())
                        || Objects.equal(event1.getEventDate(), event2.getEventDate()))
                && StringUtils.equalsIgnoreCase(event1.getReason(), event2.getReason());
    }

    private boolean doValuesMatchIgnoringReason(CertificationStatusEvent event1, CertificationStatusEvent event2) {
        return ((event1.getStatus() != null && event2.getStatus() != null
                    && StringUtils.equals(event1.getStatus().getName(), event2.getStatus().getName()))
                        || Objects.equal(event1.getCertificationStatusId(), event2.getCertificationStatusId()))
                && (Objects.equal(event1.getEventDay(), event2.getEventDay())
                        || Objects.equal(event1.getEventDate(), event2.getEventDate()));
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
