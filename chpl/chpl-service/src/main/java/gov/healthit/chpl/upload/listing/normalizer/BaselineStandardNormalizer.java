package gov.healthit.chpl.upload.listing.normalizer;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.standard.BaselineStandardService;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public abstract class BaselineStandardNormalizer implements CertificationResultLevelNormalizer {
    private BaselineStandardService baselineStandardService;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public BaselineStandardNormalizer(BaselineStandardService baselineStandardService,
            ErrorMessageUtil msgUtil) {
        this.baselineStandardService = baselineStandardService;
        this.msgUtil = msgUtil;
    }

    public abstract LocalDate getStandardsCheckDate(CertifiedProductSearchDetails listing);

    @Override
    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null && listing.getCertificationResults().size() > 0) {
            Set<CertificationCriterion> criteriaWithBaselineStandardsAdded = new LinkedHashSet<CertificationCriterion>();

            listing.getCertificationResults().stream()
                .forEach(certResult -> addMissingStandards(listing, certResult, criteriaWithBaselineStandardsAdded));

            if (!CollectionUtils.isEmpty(criteriaWithBaselineStandardsAdded)) {
                listing.addWarningMessage(msgUtil.getMessage("listing.criteria.baselineStandardsAdded",
                        Util.joinListGrammatically(criteriaWithBaselineStandardsAdded.stream()
                                .map(criterion -> Util.formatCriteriaNumber(criterion))
                                .collect(Collectors.toList()), "and")));
            }
        }
    }

    private CertificationResult addMissingStandards(CertifiedProductSearchDetails listing, CertificationResult certResult, Set<CertificationCriterion> criteriaWithBaselineStandardsAdded) {
        List<Standard> validStandardsForCriterionAndListing = baselineStandardService.getBaselineStandardsForCriteriaAndListing(
                listing, certResult.getCriterion(), getStandardsCheckDate(listing));

        validStandardsForCriterionAndListing
                .forEach(std -> {
                    List<Standard> standardsExistingInCertResult = certResult.getStandards().stream()
                            .map(crs -> crs.getStandard())
                            .toList();

                    if (!isStandardInList(std, standardsExistingInCertResult)) {
                        certResult.getStandards().add(CertificationResultStandard.builder()
                                .certificationResultId(certResult.getId())
                                .standard(std)
                                .build());
                        criteriaWithBaselineStandardsAdded.add(certResult.getCriterion());
                    }
                });

        return certResult;
    }

    private Boolean isStandardInList(Standard standard, List<Standard> standards) {
        return standards.stream()
                .filter(std -> standard.getId().equals(std.getId()))
                .findAny()
                .isPresent();
    }
}