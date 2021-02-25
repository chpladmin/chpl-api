package gov.healthit.chpl.validation.listing.reviewer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

@Component("svapReviewer")
public class SvapReviewer implements ComparisonReviewer {
    private SvapDAO svapDao;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public SvapReviewer(SvapDAO svapDao, ValidationUtils validationUtils, ErrorMessageUtil errorMessageUtil) {
        this.svapDao = svapDao;
        this.validationUtils = validationUtils;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        //Make sure there are no SVAPs for non-2015 listings

        if (!isListing2015Edition(existingListing)) {
            updatedListing.getCertificationResults().stream()
            .filter(cr -> cr.getSvaps() != null && cr.getSvaps().size() > 0)
            .forEach(cr -> updatedListing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.criteria.svap.invalidEdition",
                            cr.getNumber(), getListingEdition(existingListing))));
        } else {
            validateSvapNoticeUrl(updatedListing);

            List<CertificationResult> certificationResultsWithSvaps = updatedListing.getCertificationResults().stream()
                    .filter(cr -> cr.isSuccess() && cr.getSvaps() != null && cr.getSvaps().size() > 0)
                    .collect(Collectors.toList());

            Map<Long, List<SvapCriteriaMap>> svapCriteriaMap = null;
            try {
            svapCriteriaMap = svapDao.getAllSvapCriteriaMap().stream()
                    .collect(Collectors.groupingBy(scm -> scm.getCriterion().getId()));
            } catch (EntityRetrievalException e) {
                updatedListing.getErrorMessages().add("Could not validate SVAP");
                return;
            }

            for (CertificationResult cr : certificationResultsWithSvaps) {
                for (CertificationResultSvap crs : cr.getSvaps()) {
                    if (!isSvapValidForCriteria(crs.getSvapId(), cr.getCriterion().getId(), svapCriteriaMap)) {
                        updatedListing.getErrorMessages().add(errorMessageUtil.getMessage("listing.criteria.svap.invalidCriteria",
                                crs.getRegulatoryTextCitation(), cr.getCriterion().getNumber()));
                    }
                    if (isSvapAddedAndMarkedAsReplaced(crs, svapCriteriaMap)) {
                        updatedListing.getWarningMessages().add(errorMessageUtil.getMessage("listing.criteria.svap.replaced",
                                crs.getRegulatoryTextCitation(), cr.getCriterion().getNumber()));
                    }
                }
            }
        }
    }

    private boolean isSvapAddedAndMarkedAsReplaced(CertificationResultSvap crs, Map<Long, List<SvapCriteriaMap>> svapCriteriaMap) {
        return isSvapAdded(crs) && getSvap(crs.getSvapId(), svapCriteriaMap).get().isReplaced();
    }

    private void validateSvapNoticeUrl(CertifiedProductSearchDetails listing) {
        if (!StringUtils.isBlank(listing.getSvapNoticeUrl())
                && !validationUtils.isWellFormedUrl(listing.getSvapNoticeUrl())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.svap.url.invalid"));
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

    private boolean isSvapValidForCriteria(Long svapId, Long criteriaId, Map<Long, List<SvapCriteriaMap>> svapCriteriaMap) {
        if (svapCriteriaMap.containsKey(criteriaId)) {
            return svapCriteriaMap.get(criteriaId).stream()
                    .filter(scm -> scm.getSvap().getSvapId().equals(svapId))
                    .findAny()
                    .isPresent();
        } else {
            return false;
        }
    }

    private boolean isSvapAdded(CertificationResultSvap crSvap) {
        return crSvap.getId() == null;
    }

    private Optional<Svap> getSvap(Long svapId, Map<Long, List<SvapCriteriaMap>> svapCriteriaMap) {
        return svapCriteriaMap.values().stream()
                .flatMap(List::stream)
                .map(scm -> scm.getSvap())
                .filter(svap -> svap.getSvapId().equals(svapId))
                .findAny();
    }
}
