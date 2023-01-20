package gov.healthit.chpl.scheduler.job.ics.reviewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.manager.SurveillanceManager;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "icsErrorsReportCreatorJobLogger")
@Component
public class MissingIcsSurveillanceReviewer extends IcsErrorsReviewer {
    private static final int MAX_INHERITED_GENERATIONS_WITHOUT_SURVEILLANCE = 3;
    private static final String ICS_REQUIREMENT_TYPE = "Inherited Certified Status";
    private SurveillanceManager survManager;
    private ListingGraphDAO listingGraphDao;
    private String errorMessage;

    @Autowired
    public MissingIcsSurveillanceReviewer(SurveillanceManager survManager,
            ListingGraphDAO listingGraphDao,
            @Value("${ics.missingSurveillanceError}") String errorMessage) {
        this.survManager = survManager;
        this.listingGraphDao = listingGraphDao;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getIcsError(CertifiedProductSearchDetails listing) {
        //it's an error if none of the Listing's "self", "parent",
        //"grandparent", "great-grandparent" has surveillance of requirement type "Inherited Certified Status"
        if (hasIcs(listing)) {
            int generationsWithoutIcsSurveillanceCount =
                    getGenerationsWithoutIcsSurveillanceCount(Stream.of(listing.getId()).toList(),
                    Boolean.FALSE, 0);
            if (generationsWithoutIcsSurveillanceCount > MAX_INHERITED_GENERATIONS_WITHOUT_SURVEILLANCE) {
                return errorMessage;
            }
        }
        return null;
    }

    private Integer getGenerationsWithoutIcsSurveillanceCount(List<Long> listingIds, Boolean foundIcsSurveillance, Integer generationsWithoutIcsSurveillanceCount) {
        Iterator<Long> listingIdIter = listingIds.iterator();
        while (!foundIcsSurveillance && listingIdIter.hasNext()) {
            Long listingId = listingIdIter.next();
            List<CertifiedProductDTO> parents = getParents(listingId);
            removeSelfReferencingListings(parents, listingId);
            if (CollectionUtils.isEmpty(parents)) {
                return generationsWithoutIcsSurveillanceCount;
            }

            List<Surveillance> surveillances = new ArrayList<Surveillance>();
            parents.stream()
                .map(parent -> survManager.getByCertifiedProduct(parent.getId()))
                .filter(survList -> !CollectionUtils.isEmpty(survList))
                .forEach(survList -> surveillances.addAll(survList));
            boolean hasIcsSurveillance = isAnySurveillanceForIcs(surveillances);
            if (!hasIcsSurveillance) {
                LOGGER.debug("\tNo ICS Surveillance found for listing(s): " + parents.stream().map(parent -> parent.getId() + "").collect(Collectors.joining(", ")));
                return getGenerationsWithoutIcsSurveillanceCount(parents.stream().map(parent -> parent.getId()).toList(),
                        foundIcsSurveillance,
                        ++generationsWithoutIcsSurveillanceCount);
            } else {
                LOGGER.debug("\tFound ICS Surveillance for listing(s): " + parents.stream().map(parent -> parent.getId() + "").collect(Collectors.joining(", ")));
                foundIcsSurveillance = true;
            }
        }
        return generationsWithoutIcsSurveillanceCount;
    }

    private List<CertifiedProductDTO> getParents(Long listingId) {
        return listingGraphDao.getParents(listingId);
    }

    private void removeSelfReferencingListings(List<CertifiedProductDTO> listings, Long listingId) {
        if (listings != null) {
            Iterator<CertifiedProductDTO> listingIter = listings.iterator();
            while (listingIter.hasNext()) {
                if (listingIter.next().getId().equals(listingId)) {
                    listingIter.remove();
                }
            }
        }
    }

    private boolean isAnySurveillanceForIcs(List<Surveillance> surveillances) {
        return surveillances.stream()
                .flatMap(surv -> surv.getRequirements().stream())
                .filter(req -> !StringUtils.isEmpty(req.getRequirementTypeOther()))
                .filter(req -> req.getRequirementTypeOther().equals(ICS_REQUIREMENT_TYPE))
                .count() > 0;

    }
}