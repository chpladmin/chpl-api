package gov.healthit.chpl.certifiedproduct.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

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
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CqmResultsService {
    private CQMResultDetailsDAO cqmResultDetailsDAO;
    private CQMResultDAO cqmResultDao;
    private DimensionalDataManager dimensionalDataManager;

    private CQMCriteriaComparator cqmCriteriaComparator;
    private CQMResultComparator cqmResultComparator;

    @Autowired
    public CqmResultsService(CQMResultDetailsDAO cqmResultDetailsDAO, CQMResultDAO cqmResultDao,
            DimensionalDataManager dimensionalDataManager,
            CQMCriteriaComparator cqmCriteriaComparator) {
        this.cqmResultDetailsDAO = cqmResultDetailsDAO;
        this.cqmResultDao = cqmResultDao;
        this.dimensionalDataManager = dimensionalDataManager;
        this.cqmCriteriaComparator = cqmCriteriaComparator;
        this.cqmResultComparator = new CQMResultComparator();
    }

    public List<CQMResultDetails> getCqmResultDetails(Long id, String year) {
        List<CQMResultDetailsDTO> cqmResultDTOs = getCqmResultDetailsDTOs(id);

        List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();
        for (CQMResultDetailsDTO cqmResultDTO : cqmResultDTOs) {
            boolean existingCms = false;
            // for a CMS, first check to see if we already have an object with
            // the same CMS id
            // so we can just add to it's success versions.
            if (!StringUtils.isEmpty(year)
                    && !year.equals("2011") && !StringUtils.isEmpty(cqmResultDTO.getCmsId())) {
                for (CQMResultDetails result : cqmResults) {
                    if (cqmResultDTO.getCmsId().equals(result.getCmsId())) {
                        existingCms = true;
                        result.getSuccessVersions().add(cqmResultDTO.getVersion());
                    }
                }
            }

            if (!existingCms) {
                CQMResultDetails result = new CQMResultDetails();
                result.setId(cqmResultDTO.getId());
                result.setCmsId(cqmResultDTO.getCmsId());
                result.setNqfNumber(cqmResultDTO.getNqfNumber());
                result.setNumber(cqmResultDTO.getNumber());
                result.setTitle(cqmResultDTO.getTitle());
                result.setDescription(cqmResultDTO.getDescription());
                result.setTypeId(cqmResultDTO.getCqmCriterionTypeId());
                if (!StringUtils.isEmpty(year)
                        && !year.equals("2011") && !StringUtils.isEmpty(cqmResultDTO.getCmsId())) {
                    result.getSuccessVersions().add(cqmResultDTO.getVersion());
                } else {
                    result.setSuccess(cqmResultDTO.getSuccess());
                }
                cqmResults.add(result);
            }
        }

        // now add allVersions for CMSs
        if (!StringUtils.isEmpty(year) && !year.startsWith("2011")) {
            List<CQMCriterion> cqms = getAvailableCQMVersions();
            for (CQMCriterion cqm : cqms) {
                boolean cqmExists = false;
                for (CQMResultDetails details : cqmResults) {
                    if (cqm.getCmsId().equals(details.getCmsId())) {
                        cqmExists = true;
                        details.getAllVersions().add(cqm.getCqmVersion());
                    }
                }
                if (!cqmExists) {
                    cqmResults.add(getCqmResultDetails(cqm));
                }
            }
        }

        // now add criteria mappings to all of our cqms
        for (CQMResultDetails cqmResult : cqmResults) {
            cqmResult.setCriteria(getCqmCriteriaMapping(cqmResult));
        }

        //sort everything
        cqmResults.stream()
            .forEach(cqmResult -> {
                sortCqmCriteriaMapping(cqmResult);
                sortSuccessVersions(cqmResult);
                sortAllVersions(cqmResult);
            });

        return cqmResults.stream()
            .sorted(cqmResultComparator)
            .collect(Collectors.toList());
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

    private List<CQMResultDetailsDTO> getCqmResultDetailsDTOs(Long id) {
        List<CQMResultDetailsDTO> cqmResultDetailsDTOs = null;
        try {
            cqmResultDetailsDTOs = cqmResultDetailsDAO.getCQMResultDetailsByCertifiedProductId(id);
        } catch (EntityRetrievalException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return cqmResultDetailsDTOs;
    }

    private List<CQMResultCertification> getCqmCriteriaMapping(CQMResultDetails cqmResult) {
        if (cqmResult.isSuccess() && cqmResult.getId() != null) {
            return cqmResultDao.getCriteriaForCqmResult(cqmResult.getId()).stream()
                    .map(dto -> new CQMResultCertification(dto))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<CQMResultCertification>();
        }
    }

    private List<CQMCriterion> getAvailableCQMVersions() {
        return dimensionalDataManager.getCQMCriteria().stream()
                .filter(criterion -> !StringUtils.isEmpty(criterion.getCmsId()) && criterion.getCmsId().startsWith("CMS"))
                .collect(Collectors.toList());
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
