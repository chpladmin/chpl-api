package gov.healthit.chpl.scheduler.job.ics.reviewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.compliance.surveillance.SurveillanceManager;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "icsErrorsReportCreatorJobLogger")
@Component
public class MissingIcsSurveillanceReviewer extends IcsErrorsReviewer {
    private static final int MAX_INHERITED_GENERATIONS_WITHOUT_SURVEILLANCE = 3;
    private static final String ICS_REQUIREMENT_TYPE = "Inherited Certified Status";
    private SurveillanceManager survManager;
    private CertifiedProductDAO cpDao;
    private ListingGraphDAO listingGraphDao;
    private String missingSurveillanceErrorMessage;
    private String missingSurveillanceAndAcbChangeErrorMessage;

    @Autowired
    public MissingIcsSurveillanceReviewer(SurveillanceManager survManager,
            CertifiedProductDAO cpDao,
            ListingGraphDAO listingGraphDao,
            @Value("${ics.missingSurveillanceError}") String missingSurveillanceErrorMessage,
            @Value("${ics.missingSurveillanceAndChangedAcbsError}") String missingSurveillanceAndAcbChangeErrorMessage) {
        this.survManager = survManager;
        this.cpDao = cpDao;
        this.listingGraphDao = listingGraphDao;
        this.missingSurveillanceErrorMessage = missingSurveillanceErrorMessage;
        this.missingSurveillanceAndAcbChangeErrorMessage = missingSurveillanceAndAcbChangeErrorMessage;
    }

    @Override
    public String getIcsError(CertifiedProductSearchDetails listing) {
        //it's an error if none of the Listing's "self", "parent",
        //"grandparent", "great-grandparent" has surveillance of requirement type "Inherited Certified Status"
        if (hasIcs(listing)) {
            InheritanceCount generationsWithoutIcsSurveillanceCount =
                    getGenerationsWithoutIcsSurveillanceCount(Stream.of(listing.getId()).collect(Collectors.toList()),
                            new ArrayList<Long>(),
                            Boolean.FALSE,
                            InheritanceCount.builder().hasAcbChangeDuringInheritanceOverLimit(false).numTimesInherited(0).build());
            if (generationsWithoutIcsSurveillanceCount.getNumTimesInherited() > MAX_INHERITED_GENERATIONS_WITHOUT_SURVEILLANCE
                    && !generationsWithoutIcsSurveillanceCount.isHasAcbChangeDuringInheritanceOverLimit()) {
                return missingSurveillanceErrorMessage;
            } else if (generationsWithoutIcsSurveillanceCount.getNumTimesInherited() > MAX_INHERITED_GENERATIONS_WITHOUT_SURVEILLANCE
                    && generationsWithoutIcsSurveillanceCount.isHasAcbChangeDuringInheritanceOverLimit()) {
                return missingSurveillanceAndAcbChangeErrorMessage;
            }
        }
        return null;
    }

    private InheritanceCount getGenerationsWithoutIcsSurveillanceCount(List<Long> listingIds, List<Long> checkedListingIds, Boolean foundIcsSurveillance, InheritanceCount generationsWithoutIcsSurveillanceCount) {
        Iterator<Long> listingIdIter = listingIds.iterator();
        while (!foundIcsSurveillance && listingIdIter.hasNext()) {
            Long listingId = listingIdIter.next();
            if (checkedListingIds.contains(listingId)) {
                continue;
            }

            checkedListingIds.add(listingId);
            CertifiedProductDTO currListing = getListing(listingId);
            List<Surveillance> surveillances = survManager.getByCertifiedProduct(listingId);
            boolean hasIcsSurveillance = isAnySurveillanceForIcs(surveillances);
            if (!hasIcsSurveillance) {
                LOGGER.debug("\tNo ICS Surveillance found for listing: " + listingId);
                List<CertifiedProductDTO> parents = getParents(listingId);
                if (CollectionUtils.isEmpty(parents)) {
                    return generationsWithoutIcsSurveillanceCount;
                } else {
                    generationsWithoutIcsSurveillanceCount.setNumTimesInherited(generationsWithoutIcsSurveillanceCount.getNumTimesInherited() + 1);
                    List<Long> parentAcbIds = parents.stream().map(parent -> parent.getCertificationBodyId()).collect(Collectors.toList());
                    if (!parentAcbIds.stream().filter(acbId -> !acbId.equals(currListing.getCertificationBodyId())).toList().isEmpty()) {
                        generationsWithoutIcsSurveillanceCount.setHasAcbChangeDuringInheritanceOverLimit(true);
                    }
                    return getGenerationsWithoutIcsSurveillanceCount(parents.stream().map(parent -> parent.getId()).toList(),
                        checkedListingIds,
                        foundIcsSurveillance,
                        generationsWithoutIcsSurveillanceCount);
                }
            } else {
                LOGGER.debug("\tFound ICS Surveillance for listing: " + listingId);
                foundIcsSurveillance = true;
            }
        }
        return generationsWithoutIcsSurveillanceCount;
    }

    private List<CertifiedProductDTO> getParents(Long listingId) {
        return listingGraphDao.getParents(listingId);
    }

    private CertifiedProductDTO getListing(Long id) {
        try {
            return cpDao.getById(id);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Unable to look up listing with id " + id, ex);
            return null;
        }
    }

    private boolean isAnySurveillanceForIcs(List<Surveillance> surveillances) {
        if (CollectionUtils.isEmpty(surveillances)) {
            return false;
        }
        return surveillances.stream()
                .flatMap(surv -> surv.getRequirements().stream())
                .filter(req -> isRequirementTypeIcs(req) || isRequirementGroupTypeIcs(req))
                .count() > 0;

    }

    private boolean isRequirementTypeIcs(SurveillanceRequirement req) {
        return req.getRequirementType() != null
                && Strings.CS.equals(req.getRequirementType().getTitle(), ICS_REQUIREMENT_TYPE);
    }

    private boolean isRequirementGroupTypeIcs(SurveillanceRequirement req) {
        return req.getRequirementType() != null
                && req.getRequirementType().getRequirementGroupType() != null
                && Strings.CS.equals(req.getRequirementType().getRequirementGroupType().getName(), ICS_REQUIREMENT_TYPE);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static final class InheritanceCount {
        private Integer numTimesInherited;
        private boolean hasAcbChangeDuringInheritanceOverLimit;
    }
}
