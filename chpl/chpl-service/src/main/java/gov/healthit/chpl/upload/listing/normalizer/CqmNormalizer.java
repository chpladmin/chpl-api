package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CqmNormalizer {
    private static final String CMS_ID_BEGIN = "CMS";

    private CQMCriterionDAO cqmDao;
    private CertificationCriterionService criterionService;

    private List<CQMCriterion> allCqmsWithVersions;

    @Autowired
    public CqmNormalizer(CQMCriterionDAO cqmDao, CertificationCriterionService criterionService) {
        this.cqmDao = cqmDao;
        this.criterionService = criterionService;
    }

    @PostConstruct
    private void initializeAllCqmsWithVersions() {
        allCqmsWithVersions = cqmDao.findAll();
        allCqmsWithVersions = allCqmsWithVersions.stream()
                .filter(cqm -> !StringUtils.isEmpty(cqm.getCmsId()) && cqm.getCmsId().startsWith(CMS_ID_BEGIN))
                .collect(Collectors.toList());
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getCqmResults() != null && listing.getCqmResults().size() > 0) {
            listing.getCqmResults().stream()
                .forEach(cqmResult -> {
                    normalizeCmsId(cqmResult);
                    populateCqmCriterionData(cqmResult);
                    populateMappedCriteriaIds(listing, cqmResult);
                });
        }
        addUnattestedCqms(listing);
        listing.getCqmResults().stream()
            .forEach(cqmResult -> addAllVersions(cqmResult));
    }


    private void normalizeCmsId(CQMResultDetails cqmResult) {
        String cmsId = cqmResult.getCmsId();
        if (!StringUtils.isEmpty(cmsId) && !StringUtils.startsWithIgnoreCase(cmsId, CMS_ID_BEGIN)) {
            cmsId = CMS_ID_BEGIN + cmsId;
            cqmResult.setCmsId(cmsId);
        }
    }

    private void populateCqmCriterionData(CQMResultDetails cqmResult) {
        if (StringUtils.isEmpty(cqmResult.getCmsId())) {
            return;
        }
        CQMCriterion cqm = cqmDao.getCMSByNumber(cqmResult.getCmsId());
        if (cqm != null) {
            cqmResult.setCqmCriterionId(cqm.getCriterionId());
            cqmResult.setDescription(cqm.getDescription());
            cqmResult.setDomain(cqm.getCqmDomain());
            cqmResult.setNqfNumber(cqm.getNqfNumber());
            cqmResult.setNumber(cqm.getNumber());
            cqmResult.setTitle(cqm.getTitle());
            cqmResult.setTypeId(cqm.getCqmCriterionTypeId());
        }
    }

    private void addAllVersions(CQMResultDetails cqmResult) {
        if (allCqmsWithVersions != null && allCqmsWithVersions.size() > 0) {
            allCqmsWithVersions.stream().forEach(cqm -> {
                if (!StringUtils.isEmpty(cqm.getCmsId())
                        && cqm.getCmsId().equalsIgnoreCase(cqmResult.getCmsId())) {
                    cqmResult.getAllVersions().add(cqm.getCqmVersion());
                }
            });
        }
    }

    private void populateMappedCriteriaIds(CertifiedProductSearchDetails listing, CQMResultDetails cqmResult) {
        if (cqmResult.getCriteria() != null && cqmResult.getCriteria().size() > 0) {
            cqmResult.getCriteria().stream()
                .forEach(criterion -> normalizeCriterion(listing, criterion));
        }
    }

    private void normalizeCriterion(CertifiedProductSearchDetails listing, CQMResultCertification cqmCriterion) {
        String criterionNumber = cqmCriterion.getCertificationNumber();
        if (StringUtils.isEmpty(criterionNumber)) {
            return;
        }
        List<String> criterionLookupKeys = new ArrayList<String>();
        if (criterionNumber.equalsIgnoreCase("c1") || criterionNumber.contains("(c)(1)")) {
            criterionLookupKeys.add(Criteria2015.C_1);
        } else if (criterionNumber.equalsIgnoreCase("c2") || criterionNumber.contains("(c)(2)")) {
            criterionLookupKeys.add(Criteria2015.C_2);
        } else if (criterionNumber.equalsIgnoreCase("c3") || criterionNumber.contains("(c)(3)")) {
            criterionLookupKeys.add(Criteria2015.C_3_CURES);
            criterionLookupKeys.add(Criteria2015.C_3_OLD);
        } else if (criterionNumber.equalsIgnoreCase("c4") || criterionNumber.contains("(c)(4)")) {
            criterionLookupKeys.add(Criteria2015.C_4);
        }

        CertificationCriterion foundCriterion = determineCqmCriterionByAttestedCriteria(
                listing, criterionLookupKeys);
        if (foundCriterion != null) {
            cqmCriterion.setCertificationId(foundCriterion.getId());
            cqmCriterion.setCertificationNumber(foundCriterion.getNumber());
            cqmCriterion.setCriterion(foundCriterion);
        }
    }

    private CertificationCriterion determineCqmCriterionByAttestedCriteria(
            CertifiedProductSearchDetails listing, List<String> lookupKeys) {
        if (!CollectionUtils.isEmpty(lookupKeys) && lookupKeys.size() == 1) {
            return criterionService.get(lookupKeys.get(0));
        } else if (!CollectionUtils.isEmpty(lookupKeys)) {
            Optional<CertificationCriterion> foundCriterion = lookupKeys.stream()
                .map(lookupKey -> criterionService.get(lookupKey))
                .filter(lookupCriterion -> isCriterionAttestedInListing(listing, lookupCriterion))
                .findAny();
            if (foundCriterion.isPresent()) {
                return foundCriterion.get();
            } else {
                return criterionService.get(lookupKeys.get(0));
            }
        }
        return null;
    }

    private boolean isCriterionAttestedInListing(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        Optional<CertificationResult> attestedCert = listing.getCertificationResults().stream()
            .filter(certResult -> certResult.getSuccess() != null && certResult.getSuccess())
            .filter(certResult -> certResult.getCriterion() != null
                && certResult.getCriterion().getId().equals(criterion.getId()))
            .findAny();
        if (attestedCert == null || !attestedCert.isPresent()) {
            return false;
        }
        return true;
    }

    private void addUnattestedCqms(CertifiedProductSearchDetails listing) {
        List<CQMCriterion> cqmsWithDistinctCmsIds = allCqmsWithVersions.stream()
            .filter(distinctByKey(CQMCriterion::getCmsId))
            .collect(Collectors.toList());

        if (cqmsWithDistinctCmsIds != null && cqmsWithDistinctCmsIds.size() > 0) {
            List<CQMCriterion> cqmsToAdd = cqmsWithDistinctCmsIds.stream()
                    .filter(cqm -> !existsInListing(listing.getCqmResults(), cqm))
                    .collect(Collectors.toList());
            cqmsToAdd.stream().forEach(cqmToAdd -> {
                    listing.getCqmResults().add(buildCqmDetails(cqmToAdd));
                });
        }
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private boolean existsInListing(List<CQMResultDetails> cqmsInListing, CQMCriterion cqmCriterion) {
        Optional<CQMResultDetails> cqmInListing = cqmsInListing.stream()
                .filter(cqm -> !StringUtils.isEmpty(cqm.getCmsId()) && cqm.getCmsId().equals(cqmCriterion.getCmsId()))
                .findAny();
        return cqmInListing != null && cqmInListing.isPresent() && BooleanUtils.isTrue(cqmInListing.get().getSuccess());
    }

    private CQMResultDetails buildCqmDetails(CQMCriterion cqmCriterion) {
        CQMResultDetails cqmDetails = CQMResultDetails.builder()
                .cqmCriterionId(cqmCriterion.getCriterionId())
                .cmsId(cqmCriterion.getCmsId())
                .nqfNumber(cqmCriterion.getNqfNumber())
                .number(cqmCriterion.getNumber())
                .title(cqmCriterion.getTitle())
                .description(cqmCriterion.getDescription())
                .success(Boolean.FALSE)
                .typeId(cqmCriterion.getCqmCriterionTypeId())
                .build();
        cqmDetails.getAllVersions().add(cqmCriterion.getCqmVersion());
        return cqmDetails;
    }
}
