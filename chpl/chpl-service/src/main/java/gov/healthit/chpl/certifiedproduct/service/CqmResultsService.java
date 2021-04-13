package gov.healthit.chpl.certifiedproduct.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DimensionalDataManager;

@Component
public class CqmResultsService {
    private CQMResultDetailsDAO cqmResultDetailsDAO;
    private CQMResultDAO cqmResultDao;
    private DimensionalDataManager dimensionalDataManager;

    @Autowired
    public CqmResultsService(CQMResultDetailsDAO cqmResultDetailsDAO, CQMResultDAO cqmResultDao, DimensionalDataManager dimensionalDataManager) {
        this.cqmResultDetailsDAO = cqmResultDetailsDAO;
        this.cqmResultDao = cqmResultDao;
        this.dimensionalDataManager = dimensionalDataManager;
    }

    public List<CQMResultDetails> getCqmResultDetails(Long id, String year) {
        List<CQMResultDetailsDTO> cqmResultDTOs = getCqmResultDetailsDTOs(id);


        List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();
        for (CQMResultDetailsDTO cqmResultDTO : cqmResultDTOs) {
            boolean existingCms = false;
            // for a CMS, first check to see if we already have an object with
            // the same CMS id
            // so we can just add to it's success versions.
            if (!year.equals("2011") && !StringUtils.isEmpty(cqmResultDTO.getCmsId())) {
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
                if (!year.equals("2011") && !StringUtils.isEmpty(cqmResultDTO.getCmsId())) {
                    result.getSuccessVersions().add(cqmResultDTO.getVersion());
                } else {
                    result.setSuccess(cqmResultDTO.getSuccess());
                }
                cqmResults.add(result);
            }
        }

        // now add allVersions for CMSs
        if (!year.startsWith("2011")) {
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
        return cqmResults;
    }

    private List<CQMResultDetailsDTO> getCqmResultDetailsDTOs(Long id) {
        List<CQMResultDetailsDTO> cqmResultDetailsDTOs = null;
        try {
            cqmResultDetailsDTOs = cqmResultDetailsDAO.getCQMResultDetailsByCertifiedProductId(id);
        } catch (EntityRetrievalException e) {
            e.printStackTrace();
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
                .allVersions(new HashSet<String>(Arrays.asList(cqm.getCqmVersion())))
                .typeId(cqm.getCqmCriterionTypeId())
                .build();
    }
}
