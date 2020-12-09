package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CqmNormalizer {
    private static final String CMS_ID_BEGIN = "CMS";

    private CQMCriterionDAO cqmDao;
    private CertificationCriterionDAO criterionDao;

    private List<CQMCriterionDTO> allCqmsWithVersions;

    @Autowired
    public CqmNormalizer(CQMCriterionDAO cqmDao, CertificationCriterionDAO criterionDao) {
        this.cqmDao = cqmDao;
        this.criterionDao = criterionDao;
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
                    lookupCqmCriterionData(cqmResult);
                    lookupMappedCriteriaIds(listing, cqmResult);
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

    private void lookupCqmCriterionData(CQMResultDetails cqmResult) {
        CQMCriterionDTO cqmDto = cqmDao.getCMSByNumber(cqmResult.getCmsId());
        if (cqmDto != null) {
            cqmResult.setDescription(cqmDto.getDescription());
            cqmResult.setDomain(cqmDto.getCqmDomain());
            cqmResult.setNqfNumber(cqmDto.getNqfNumber());
            cqmResult.setNumber(cqmDto.getNumber());
            cqmResult.setTitle(cqmDto.getTitle());
            cqmResult.setTypeId(cqmDto.getCqmCriterionTypeId());
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

    private void lookupMappedCriteriaIds(CertifiedProductSearchDetails listing, CQMResultDetails cqmResult) {
        if (cqmResult.getCriteria() != null && cqmResult.getCriteria().size() > 0) {
            cqmResult.getCriteria().stream()
                .forEach(criterion -> normalizeCriterion(listing, criterion));
        }
    }

    private void normalizeCriterion(CertifiedProductSearchDetails listing, CQMResultCertification criterion) {
        String criterionNumber = criterion.getCertificationNumber();
        if (StringUtils.isEmpty(criterionNumber)) {
            return;
        }
        if (criterionNumber.equals("c1") || criterionNumber.equals("(c)(1)")) {
            criterion.setCertificationNumber("170.315 (c)(1)");
        } else if (criterionNumber.equals("c2") || criterionNumber.equals("(c)(2)")) {
            criterion.setCertificationNumber("170.315 (c)(2)");
        } else if (criterionNumber.equals("c3") || criterionNumber.equals("(c)(3)")) {
            criterion.setCertificationNumber("170.315 (c)(3)");
        } else if (criterionNumber.equals("c4") || criterionNumber.equals("(c)(4)")) {
            criterion.setCertificationNumber("170.315 (c)(4)");
        }
        CertificationCriterion foundCriteron = lookupCriterion(criterion.getCertificationNumber(),
                determineCures(listing, criterion.getCertificationNumber()));
        if (foundCriteron != null) {
            criterion.setCertificationId(foundCriteron.getId());
            criterion.setCriterion(foundCriteron);
        }
    }

    private boolean determineCures(CertifiedProductSearchDetails listing, String criterionNumber) {
        Optional<CertificationResult> attestedCert = listing.getCertificationResults().stream()
            .filter(certResult -> certResult.isSuccess() != null && certResult.isSuccess())
            .filter(certResult -> certResult.getCriterion() != null
                && certResult.getCriterion().getNumber().equals(criterionNumber))
            .findAny();
        if (attestedCert == null || !attestedCert.isPresent()) {
            return false;
        }
        return Util.isCures(attestedCert.get().getCriterion());
    }

    private CertificationCriterion lookupCriterion(String number, boolean isCures) {
        List<CertificationCriterionDTO> certDtos = criterionDao.getAllByNumber(number);
        if (certDtos == null || certDtos.size() == 0) {
            LOGGER.error("Could not find a certification criterion matching " + number);
        }

        Optional<CertificationCriterionDTO> foundCriterion = null;
        if (isCures) {
            foundCriterion = certDtos.stream()
                    .filter(certDto -> Util.isCures(certDto))
                    .findFirst();
        } else {
            foundCriterion = certDtos.stream()
                    .filter(certDto -> !Util.isCures(certDto))
                    .findFirst();
        }

        if (foundCriterion == null || !foundCriterion.isPresent()) {
            LOGGER.error("Could not find a certification criterion (cures=" + isCures + ") matching " + number);
            return null;
        }
        return new CertificationCriterion(foundCriterion.get());
    }

    private void addUnattestedCqms(CertifiedProductSearchDetails listing) {
        List<CQMCriterionDTO> cqmsWithDistinctCmsIds = allCqmsWithVersions.stream()
            .filter(distinctByKey(CQMCriterionDTO::getCmsId))
            .collect(Collectors.toList());

        if (cqmsWithDistinctCmsIds != null && cqmsWithDistinctCmsIds.size() > 0) {
            List<CQMCriterionDTO> cqmsToAdd = cqmsWithDistinctCmsIds.stream()
                    .filter(cqmDto -> !existsInListing(listing.getCqmResults(), cqmDto))
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

    private boolean existsInListing(List<CQMResultDetails> cqmsInListing, CQMCriterionDTO cqmDto) {
        Optional<CQMResultDetails> cqmInListing =
                cqmsInListing.stream().filter(cqm -> cqm.getCmsId().equals(cqmDto.getCmsId()))
                .findAny();
        return cqmInListing != null && cqmInListing.isPresent() && cqmInListing.get().isSuccess();
    }

    private CQMResultDetails buildCqmDetails(CQMCriterionDTO cqmDto) {
        CQMResultDetails cqmDetails = CQMResultDetails.builder()
                .cmsId(cqmDto.getCmsId())
                .nqfNumber(cqmDto.getNqfNumber())
                .number(cqmDto.getNumber())
                .title(cqmDto.getTitle())
                .description(cqmDto.getDescription())
                .success(Boolean.FALSE)
                .typeId(cqmDto.getCqmCriterionTypeId())
                .build();
        cqmDetails.getAllVersions().add(cqmDto.getCqmVersion());
        return cqmDetails;
    }
}
