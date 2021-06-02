package gov.healthit.chpl.validation.listing.reviewer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("optionalStandardReviewer")
public class OptionalStandardReviewer implements Reviewer {
    private OptionalStandardDAO optionalStandardDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public OptionalStandardReviewer(OptionalStandardDAO optionalStandardDAO, ErrorMessageUtil errorMessageUtil) {
        this.optionalStandardDAO = optionalStandardDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        //Make sure there are no Optional Standards for non-2015 listings

        if (!isListing2015Edition(listing)) {
            listing.getCertificationResults().stream()
            .filter(cr -> cr.getOptionalStandards() != null && cr.getOptionalStandards().size() > 0)
            .forEach(cr -> listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.criteria.optionalStandard.invalidEdition",
                            cr.getNumber(), getListingEdition(listing))));
        } else {
            List<CertificationResult> certificationResultsWithOptionalStandards = listing.getCertificationResults().stream()
                    .filter(cr -> cr.isSuccess() && cr.getOptionalStandards() != null && cr.getOptionalStandards().size() > 0)
                    .collect(Collectors.toList());

            Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap = null;
            try {
            optionalStandardCriteriaMap = optionalStandardDAO.getAllOptionalStandardCriteriaMap().stream()
                    .collect(Collectors.groupingBy(scm -> scm.getCriterion().getId()));
            } catch (EntityRetrievalException e) {
                listing.getErrorMessages().add("Could not validate Optional Standard");
                return;
            }

            for (CertificationResult cr : certificationResultsWithOptionalStandards) {
                for (CertificationResultOptionalStandard cros : cr.getOptionalStandards()) {
                    if (!isOptionalStandardValidForCriteria(cros.getOptionalStandard().getId(), cr.getCriterion().getId(), optionalStandardCriteriaMap)) {
                        listing.getErrorMessages().add(errorMessageUtil.getMessage("listing.criteria.optionalStandard.invalidCriteria",
                                cros.getOptionalStandard().getOptionalStandard(), cr.getCriterion().getNumber()));
                    }
                }
            }
        }
    }

    private boolean isListing2015Edition(CertifiedProductSearchDetails listing) {
        return getListingEdition(listing).equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
    }

    private String getListingEdition(CertifiedProductSearchDetails listing) {
        return listing.getCertificationEdition().containsKey(CertifiedProductSearchDetails.EDITION_NAME_KEY)
                ? listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString()
                        : "";
    }

    private boolean isOptionalStandardValidForCriteria(Long osId, Long criteriaId, Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap) {
        if (optionalStandardCriteriaMap.containsKey(criteriaId)) {
            return optionalStandardCriteriaMap.get(criteriaId).stream()
                    .filter(oscm -> oscm.getOptionalStandard().getId().equals(osId))
                    .findAny()
                    .isPresent();
        } else {
            return false;
        }
    }
}
