package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.CacheUtil;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Service
public class CertifiedProductSearchManagerImpl implements CertifiedProductSearchManager {

    @Autowired
    CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    @Autowired
    CertifiedProductSearchDAO basicCpSearchDao;

    @Transactional(readOnly = true)
    @Override
    @Cacheable(CacheNames.COLLECTIONS_LISTINGS)
    public List<CertifiedProductFlatSearchResult> search() {
        List<CertifiedProductFlatSearchResult> results = basicCpSearchDao.getAllCertifiedProducts();
        return results;
    }

    @Transactional
    @Override
    public SearchResponse search(SearchRequest searchRequest) {

        List<CertifiedProductSearchResult> searchResults = new ArrayList<CertifiedProductSearchResult>();
        Integer countSearchResults = certifiedProductSearchResultDAO.countMultiFilterSearchResults(searchRequest)
                .intValue();

        for (CertifiedProductDetailsDTO dto : certifiedProductSearchResultDAO.search(searchRequest)) {

            CertifiedProductSearchResult searchResult = new CertifiedProductSearchResult();

            searchResult.setId(dto.getId());
            searchResult.setAcbCertificationId(dto.getAcbCertificationId());

            if (dto.getCertificationDate() != null) {
                searchResult.setCertificationDate(dto.getCertificationDate().getTime());
            }
            if (dto.getDecertificationDate() != null) {
                searchResult.setDecertificationDate(dto.getDecertificationDate().getTime());
            }

            searchResult.getCertificationEdition().put("id", dto.getCertificationEditionId());
            searchResult.getCertificationEdition().put("name", dto.getYear());

            searchResult.getCertificationStatus().put("id", dto.getCertificationStatusId());
            searchResult.getCertificationStatus().put("name", dto.getCertificationStatusName());

            searchResult.getCertifyingBody().put("id", dto.getCertificationBodyId());
            searchResult.getCertifyingBody().put("name", dto.getCertificationBodyName());

            if (!StringUtils.isEmpty(dto.getChplProductNumber())) {
                searchResult.setChplProductNumber(dto.getChplProductNumber());
            } else {
                searchResult.setChplProductNumber(dto.getYearCode() + "." + dto.getTestingLabCode() + "."
                        + dto.getCertificationBodyCode() + "." + dto.getDeveloper().getDeveloperCode() + "."
                        + dto.getProductCode() + "." + dto.getVersionCode() + "." + dto.getIcsCode() + "."
                        + dto.getAdditionalSoftwareCode() + "." + dto.getCertifiedDateCode());
            }

            searchResult.getClassificationType().put("id", dto.getProductClassificationTypeId());
            searchResult.getClassificationType().put("name", dto.getProductClassificationName());

            searchResult.setOtherAcb(dto.getOtherAcb());

            searchResult.getPracticeType().put("id", dto.getPracticeTypeId());
            searchResult.getPracticeType().put("name", dto.getPracticeTypeName());

            searchResult.getProduct().put("id", dto.getProduct().getId());
            searchResult.getProduct().put("name", dto.getProduct().getName());
            searchResult.getProduct().put("versionId", dto.getVersion().getId());
            searchResult.getProduct().put("version", dto.getVersion().getVersion());

            searchResult.setReportFileLocation(dto.getReportFileLocation());
            searchResult.setSedReportFileLocation(dto.getSedReportFileLocation());
            searchResult.setSedIntendedUserDescription(dto.getSedIntendedUserDescription());
            searchResult.setSedTestingEnd(dto.getSedTestingEnd());
            searchResult.setTestingLabId(dto.getTestingLabId());
            searchResult.setTestingLabName(dto.getTestingLabName());

            searchResult.getDeveloper().put("id", dto.getDeveloper().getId());
            searchResult.getDeveloper().put("name", dto.getDeveloper().getName());

            searchResult.setCountCerts(dto.getCountCertifications());
            searchResult.setCountCqms(dto.getCountCqms());
            searchResult.setCountSurveillance(dto.getCountSurveillance());
            searchResult.setCountOpenSurveillance(dto.getCountOpenSurveillance());
            searchResult.setCountClosedSurveillance(dto.getCountClosedSurveillance());
            searchResult.setCountOpenNonconformities(dto.getCountOpenNonconformities());
            searchResult.setCountClosedNonconformities(dto.getCountClosedNonconformities());
            searchResult.setIcs(dto.getIcs());
            searchResult.setSedTesting(dto.getSedTesting());
            searchResult.setQmsTesting(dto.getQmsTesting());
            searchResult.setAccessibilityCertified(dto.getAccessibilityCertified());
            searchResult.setProductAdditionalSoftware(dto.getProductAdditionalSoftware());
            searchResult.setTransparencyAttestation(dto.getTransparencyAttestation());
            searchResult.setTransparencyAttestationUrl(dto.getTransparencyAttestationUrl());
            searchResult.setNumMeaningfulUse(dto.getNumMeaningfulUse());

            searchResults.add(searchResult);
        }

        SearchResponse response = new SearchResponse(countSearchResults, searchResults, searchRequest.getPageSize(),
                searchRequest.getPageNumber());
        return response;
    }
}
