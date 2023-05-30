package gov.healthit.chpl.validation.listing.reviewer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
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
        // Make sure there are no SVAPs for non-2015 listings

        if (!isListing2015Edition(existingListing)) {
            updatedListing.getCertificationResults().stream()
                    .filter(cr -> cr.getSvaps() != null && cr.getSvaps().size() > 0)
                    .forEach(cr -> updatedListing.addBusinessErrorMessage(
                            errorMessageUtil.getMessage("listing.criteria.svap.invalidEdition",
                                    cr.getCriterion().getNumber(), getListingEdition(existingListing))));
        } else {
            validateSvapNoticeUrl(updatedListing);

            List<CertificationResult> certificationResultsWithSvaps = updatedListing.getCertificationResults().stream()
                    .filter(cr -> BooleanUtils.isTrue(cr.isSuccess()) && !CollectionUtils.isEmpty(cr.getSvaps()))
                    .collect(Collectors.toList());

            Map<Long, List<SvapCriteriaMap>> svapCriteriaMap = null;
            try {
                svapCriteriaMap = svapDao.getAllSvapCriteriaMap().stream()
                        .collect(Collectors.groupingBy(scm -> scm.getCriterion().getId()));
            } catch (EntityRetrievalException e) {
                updatedListing.addDataErrorMessage(errorMessageUtil.getMessage("listing.criteria.svap.couldNotValidate"));
                return;
            }

            for (CertificationResult cr : certificationResultsWithSvaps) {
                for (CertificationResultSvap crs : cr.getSvaps()) {
                    populateSvapFields(crs, svapCriteriaMap);
                    if (!isSvapValidForCriteria(crs.getSvapId(), cr.getCriterion().getId(), svapCriteriaMap)) {
                        updatedListing.addBusinessErrorMessage(errorMessageUtil.getMessage("listing.criteria.svap.invalidCriteria",
                                crs.getRegulatoryTextCitation(), cr.getCriterion().getNumber()));
                    }
                    if (isSvapAddedAndMarkedAsReplaced(crs, svapCriteriaMap)) {
                        updatedListing.addWarningMessage(errorMessageUtil.getMessage("listing.criteria.svap.replaced",
                                crs.getRegulatoryTextCitation(), cr.getCriterion().getNumber()));
                    }
                }
            }
        }
    }

    private void populateSvapFields(CertificationResultSvap crs, Map<Long, List<SvapCriteriaMap>> svapCriteriaMap) {
        if (crs.getSvapId() != null) {
            Optional<Svap> svap = getSvap(crs.getSvapId(), svapCriteriaMap);
            if (svap.isPresent()) {
                crs.setRegulatoryTextCitation(svap.get().getRegulatoryTextCitation());
                crs.setApprovedStandardVersion(svap.get().getApprovedStandardVersion());
                crs.setReplaced(svap.get().isReplaced());
            }
        } else if (!StringUtils.isEmpty(crs.getRegulatoryTextCitation())) {
            Optional<Svap> svap = getSvap(crs.getRegulatoryTextCitation(), svapCriteriaMap);
            if (svap.isPresent()) {
                crs.setSvapId(svap.get().getSvapId());
                crs.setApprovedStandardVersion(svap.get().getApprovedStandardVersion());
                crs.setReplaced(svap.get().isReplaced());
            }
        }
    }

    private boolean isSvapAddedAndMarkedAsReplaced(CertificationResultSvap crs, Map<Long, List<SvapCriteriaMap>> svapCriteriaMap) {
        Optional<Svap> svap = getSvap(crs.getSvapId(), svapCriteriaMap);
        return isSvapAdded(crs) && svap.isPresent() && svap.get().isReplaced();
    }

    private void validateSvapNoticeUrl(CertifiedProductSearchDetails listing) {
        if (!StringUtils.isBlank(listing.getSvapNoticeUrl())
                && !validationUtils.isWellFormedUrl(listing.getSvapNoticeUrl())) {
            listing.addBusinessErrorMessage(
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

    private Optional<Svap> getSvap(String regText, Map<Long, List<SvapCriteriaMap>> svapCriteriaMap) {
        return svapCriteriaMap.values().stream()
                .flatMap(List::stream)
                .map(scm -> scm.getSvap())
                .filter(svap -> svap.getRegulatoryTextCitation().equals(regText))
                .findAny();
    }
}
