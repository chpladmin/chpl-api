package gov.healthit.chpl.validation.listing.reviewer;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardCriteriaMap;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.standard.StandardGroupService;
import gov.healthit.chpl.standard.StandardGroupValidation;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class BaselineStandardReviewer extends StandardGroupValidation {
    private StandardGroupService standardGroupService;
    private StandardDAO standardDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public BaselineStandardReviewer(StandardGroupService standardGroupService, StandardDAO standardDao,
            ErrorMessageUtil msgUtil) {
        super(standardGroupService, msgUtil);

        this.standardGroupService = standardGroupService;
        this.standardDao = standardDao;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
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

    private CertificationResult addMissingStandards(CertifiedProductSearchDetails listing,
            CertificationResult certResult,
            Set<CertificationCriterion> criteriaWithBaselineStandardsAdded) {
        List<Standard> validStandardsForCriterionAndListing = getValidStandardsForCriteriaAndListing(certResult.getCriterion(), listing.getCertificationDay());

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

    private List<Standard> getValidStandardsForCriteriaAndListing(CertificationCriterion criterion, LocalDate certificationDate) {
        try {
            List<StandardCriteriaMap> maps = standardDao.getAllStandardCriteriaMap();
            Map<String, List<Standard>> standardGroups = standardGroupService.getGroupedStandardsForCriteria(criterion, certificationDate);

            maps.removeIf(map -> !map.getCriterion().getId().equals(criterion.getId()));
            return maps.stream()
                    .filter(map -> !isStandardInAGroup(standardGroups, map.getStandard())
                            && DateUtil.isDateBetweenInclusive(Pair.of(map.getStandard().getRequiredDay(), map.getStandard().getEndDay()), certificationDate))
                    .map(map -> map.getStandard())
                    .toList();
        } catch (EntityRetrievalException e) {
            LOGGER.info("Error retrieving Standards for Criterion");
            throw new RuntimeException(e);
        }
    }

    private Boolean isStandardInAGroup(Map<String, List<Standard>> standardGroups, Standard standard) {

        var x = standardGroups.entrySet().stream()
            .flatMap(mapEntry -> mapEntry.getValue().stream())
            .filter(std -> std.getId().equals(standard.getId()))
            .findAny()
            .isPresent();
        return x;
    }
}
