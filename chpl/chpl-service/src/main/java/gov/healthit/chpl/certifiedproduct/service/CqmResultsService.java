package gov.healthit.chpl.certifiedproduct.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.comparator.CQMCriteriaComparator;
import gov.healthit.chpl.domain.comparator.CQMResultComparator;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.service.CqmCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CqmResultsService {
    private CQMResultDetailsDAO cqmResultDetailsDAO;
    private CQMResultDAO cqmResultDao;
    private CqmCriterionService cqmCriterionService;

    private CQMCriteriaComparator cqmCriteriaComparator;
    private CQMResultComparator cqmResultComparator;

    @Autowired
    public CqmResultsService(CQMResultDetailsDAO cqmResultDetailsDAO, CQMResultDAO cqmResultDao,
            CqmCriterionService cqmCriterionService,
            CQMCriteriaComparator cqmCriteriaComparator) {
        this.cqmResultDetailsDAO = cqmResultDetailsDAO;
        this.cqmResultDao = cqmResultDao;
        this.cqmCriterionService = cqmCriterionService;
        this.cqmCriteriaComparator = cqmCriteriaComparator;
        this.cqmResultComparator = new CQMResultComparator();
    }

    public List<CQMResultDetails> getCqmResultDetails(Long listingId, String year) {
        //Get a flat list of all CQM results
        //If these are of CMS-type they then need to be grouped by CMS ID and have versions attached.
        //If these are of NQF-type then they do not need any further processing.
        List<CQMResultDetails> cqmResults = cqmResultDetailsDAO.getCQMResultDetailsByCertifiedProductId(listingId);

        //NQF-type of CQMs are only associated with 2011 listings
        if (!StringUtils.isEmpty(year) && year.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2011.getYear())) {
            return cqmResults;
        }

        //group the CQMs so there is only 1 per CMS ID and it contains the data related to the
        //most recent successVersion of that CQM
        List<CQMResultDetails> groupedCqmResults = new ArrayList<CQMResultDetails>();
        Map<String, List<CQMResultDetails>> cqmResultsByCmsId = getCqmResultsGroupedByCmsId(cqmResults);
        cqmResultsByCmsId.keySet().stream()
            .map(cmsId -> (List<CQMResultDetails>) cqmResultsByCmsId.get(cmsId))
            .forEach(cqmResultsForCmsId -> addToGroupedCqmResults(cqmResultsForCmsId, groupedCqmResults));

        // now fill in the "allVersions" data for that CQM
        List<CQMCriterion> allCmsCqmCriteria = cqmCriterionService.getAllCmsCqms();
        allCmsCqmCriteria.stream()
            .forEach(cqmCrit -> {
                CQMResultDetails cqmResultForCmsId = groupedCqmResults.stream()
                        .filter(cqmResult -> cqmResult.getCmsId().equals(cqmCrit.getCmsId()))
                        .findAny()
                        .orElse(null);
                if (cqmResultForCmsId != null) {
                    cqmResultForCmsId.setAllVersions(
                            getAllVersionsForCmsId(cqmResultForCmsId.getCmsId(), allCmsCqmCriteria));
                }
            });

        //Add c1,c2,c3,c4 criteria mappings to all CQM results
        for (CQMResultDetails cqmResult : groupedCqmResults) {
            cqmResult.setCriteria(getCqmCriteriaMapping(cqmResult));
        }

        //sort everything
        groupedCqmResults.stream()
            .forEach(cqmResult -> {
                sortCqmCriteriaMapping(cqmResult);
                sortSuccessVersions(cqmResult);
                sortAllVersions(cqmResult);
            });

        return groupedCqmResults.stream()
            .sorted(cqmResultComparator)
            .collect(Collectors.toList());
    }

    private Map<String, List<CQMResultDetails>> getCqmResultsGroupedByCmsId(List<CQMResultDetails> cqmResults) {
        Map<String, List<CQMResultDetails>> cqmResultsGroupedByCmsId = new HashMap<String, List<CQMResultDetails>>();
        Set<String> distinctCmsIds = cqmResults.stream()
            .map(cqm -> cqm.getCmsId())
            .collect(Collectors.toSet());
        distinctCmsIds.stream()
            .forEach(cmsId -> cqmResultsGroupedByCmsId.put(cmsId, getAllCqmResultsByCmsId(cmsId, cqmResults)));
        return cqmResultsGroupedByCmsId;
    }

    private List<CQMResultDetails> getAllCqmResultsByCmsId(String cmsId, List<CQMResultDetails> cqmResults) {
        return cqmResults.stream()
            .filter(cqm -> cqm.getCmsId().equals(cmsId))
            .collect(Collectors.toList());
    }

    private void addToGroupedCqmResults(List<CQMResultDetails> cqmResultsForCmsId, List<CQMResultDetails> allCqmResults) {
        LinkedHashSet<String> successVersions = cqmResultsForCmsId.stream()
                .flatMap(cqmResult -> cqmResult.getSuccessVersions().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        //get the CQM for the max success version
        CQMResultDetails maxVersionCqmResult = getCqmResultForMostRecentVersion(cqmResultsForCmsId);
        maxVersionCqmResult.setSuccessVersions(successVersions);

        allCqmResults.add(maxVersionCqmResult);
    }

    private CQMResultDetails getCqmResultForMostRecentVersion(List<CQMResultDetails> cqmResultsForCmsId) {
        int maxVersion = cqmResultsForCmsId.stream()
                .flatMap(cqmResult -> cqmResult.getSuccessVersions().stream())
                .map(ver -> ver.substring(1))
                .mapToInt(Integer::valueOf)
                .max()
                .orElse(0);
        return cqmResultsForCmsId.stream()
                    .filter(cqmResult -> cqmResult.getSuccessVersions().contains("v" + maxVersion))
                    .findAny()
                    .orElse(cqmResultsForCmsId.get(cqmResultsForCmsId.size() - 1));
    }

    private LinkedHashSet<String> getAllVersionsForCmsId(String cmsId, List<CQMCriterion> allCmsCqmCriteria) {
        return allCmsCqmCriteria.stream()
            .filter(cqmCrit -> cqmCrit.getCmsId().equals(cmsId))
            .map(cqmCrit -> cqmCrit.getCqmVersion())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void sortCqmCriteriaMapping(CQMResultDetails cqmResult) {
        List<CQMResultCertification> sortedCqmCriteria = cqmResult.getCriteria().stream()
            .sorted(cqmCriteriaComparator)
            .collect(Collectors.toList());
        cqmResult.setCriteria(sortedCqmCriteria);
    }

    private void sortSuccessVersions(CQMResultDetails cqmResult) {
        LinkedHashSet<String> sortedSuccessVersions = cqmResult.getSuccessVersions().stream()
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
        cqmResult.setSuccessVersions(sortedSuccessVersions);
    }

    private void sortAllVersions(CQMResultDetails cqmResult) {
        LinkedHashSet<String> sortedAllVersions = cqmResult.getAllVersions().stream()
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
        cqmResult.setAllVersions(sortedAllVersions);
    }

    private List<CQMResultCertification> getCqmCriteriaMapping(CQMResultDetails cqmResult) {
        if (BooleanUtils.isTrue(cqmResult.getSuccess()) && cqmResult.getId() != null) {
            return cqmResultDao.getCriteriaForCqmResult(cqmResult.getId());
        } else {
            return new ArrayList<CQMResultCertification>();
        }
    }

    private CQMResultDetails getCqmResultDetails(CQMCriterion cqm) {
        return CQMResultDetails.builder()
                .cmsId(cqm.getCmsId())
                .nqfNumber(cqm.getNqfNumber())
                .number(cqm.getNumber())
                .title(cqm.getTitle())
                .description(cqm.getDescription())
                .success(Boolean.FALSE)
                .allVersions(new LinkedHashSet<String>(Arrays.asList(cqm.getCqmVersion())))
                .typeId(cqm.getCqmCriterionTypeId())
                .build();
    }
}
