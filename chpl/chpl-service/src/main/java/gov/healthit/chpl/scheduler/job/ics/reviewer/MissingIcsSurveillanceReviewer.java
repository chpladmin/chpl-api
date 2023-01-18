package gov.healthit.chpl.scheduler.job.ics.reviewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
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
            LOGGER.debug("\tListing " + listing.getId() + " has ICS.");
            List<CertifiedProductDTO> parents = getParents(listing.getId());
            if (!CollectionUtils.isEmpty(parents)) {
                LOGGER.debug("\tListing " + listing.getId() + " has parents.");
                List<CertifiedProductDTO> grandparents = getGrandparents(
                        parents.stream().map(parent -> parent.getId()).toList());
                if (!CollectionUtils.isEmpty(grandparents)) {
                    LOGGER.debug("\tListing " + listing.getId() + " has grandparents.");
                    List<CertifiedProductDTO> greatGrandparents = getGreatGrandparents(
                            grandparents.stream().map(grandparent -> grandparent.getId()).toList());
                    if (!CollectionUtils.isEmpty(greatGrandparents)) {
                        LOGGER.debug("\tListing " + listing.getId() + " has great-grandparents.");
                        List<Long> familyMemberIds = new ArrayList<Long>();
                        familyMemberIds.addAll(parents.stream().map(parent -> parent.getId()).toList());
                        familyMemberIds.addAll(grandparents.stream().map(grandparent -> grandparent.getId()).toList());
                        familyMemberIds.addAll(greatGrandparents.stream().map(greatGrandparent -> greatGrandparent.getId()).toList());
                        LOGGER.debug("\tChecking all family members of " + listing.getId() + " for ICS Surveillance Type. Family members are: "
                                + familyMemberIds.stream().map(id -> id.toString()).collect(Collectors.joining(", ")));
                        Iterator<Long> familyMemberIter = familyMemberIds.iterator();
                        boolean foundIcsSurveillance = false;
                        while (familyMemberIter.hasNext() && !foundIcsSurveillance) {
                            List<Surveillance> familyMemberSurveillances = survManager.getByCertifiedProduct(familyMemberIter.next());
                            if (isAnySurveillanceForIcs(familyMemberSurveillances)) {
                                LOGGER.info("\tICS Surveillance exists for family members of listing " + listing.getId());
                                foundIcsSurveillance = true;
                            }
                        }
                        if (!foundIcsSurveillance) {
                            LOGGER.info("\tICS Surveillance does not exist for family members of listing " + listing.getId());
                            return errorMessage;
                        }
                    }
                }
            }
        }
        return null;
    }

    private List<CertifiedProductDTO> getParents(Long listingId) {
        return listingGraphDao.getParents(listingId);
    }

    private List<CertifiedProductDTO> getGrandparents(List<Long> parentListingIds) {
        return parentListingIds.stream()
                .flatMap(parentId -> listingGraphDao.getParents(parentId).stream())
                .collect(Collectors.toList());
    }

    private List<CertifiedProductDTO> getGreatGrandparents(List<Long> grandparentListingIds) {
        return grandparentListingIds.stream()
                .flatMap(grandparentListingId -> listingGraphDao.getParents(grandparentListingId).stream())
                .collect(Collectors.toList());
    }

    private boolean isAnySurveillanceForIcs(List<Surveillance> surveillances) {
        return surveillances.stream()
                .flatMap(surv -> surv.getRequirements().stream())
                .filter(req -> req.getRequirementType().getTitle().equals(ICS_REQUIREMENT_TYPE))
                .count() > 0;

    }
}
