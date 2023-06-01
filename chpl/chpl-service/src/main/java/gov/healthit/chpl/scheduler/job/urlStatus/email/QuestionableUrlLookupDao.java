package gov.healthit.chpl.scheduler.job.urlStatus.email;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductSummaryDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlResult;
import lombok.extern.log4j.Log4j2;

@Component("questionableUrlLookupDao")
@Log4j2(topic = "questionableUrlReportGeneratorJobLogger")
public class QuestionableUrlLookupDao {
    private CertificationBodyDAO acbDao;
    private TestingLabDAO atlDao;
    private DeveloperDAO devDao;
    private CertifiedProductDAO cpDao;
    private CertificationResultDetailsDAO certResultDao;

    @Autowired
    public QuestionableUrlLookupDao(CertificationBodyDAO acbDao, TestingLabDAO atlDao,
            DeveloperDAO devDao, CertifiedProductDAO cpDao, CertificationResultDetailsDAO certResultDao) {
        this.acbDao = acbDao;
        this.atlDao = atlDao;
        this.devDao = devDao;
        this.cpDao = cpDao;
        this.certResultDao = certResultDao;
    }

    @Transactional
    public List<FailedUrlResult> getAcbsWithUrl(UrlResult urlResult) {
        List<CertificationBody> acbsWithBadUrl = acbDao.getByWebsite(urlResult.getUrl());
        return acbsWithBadUrl.stream()
            .map(acb -> FailedUrlResult.builder()
                    .lastChecked(urlResult.getLastChecked())
                    .responseCode(urlResult.getResponseCode())
                    .responseMessage(urlResult.getResponseMessage())
                    .url(urlResult.getUrl())
                    .urlType(urlResult.getUrlType())
                    .acb(acb)
                    .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public List<FailedUrlResult> getAtlsWithUrl(UrlResult urlResult) {
        List<TestingLabDTO> atlsWithBadUrl = atlDao.getByWebsite(urlResult.getUrl());
        return atlsWithBadUrl.stream()
            .map(atlDto -> FailedUrlResult.builder()
                    .lastChecked(urlResult.getLastChecked())
                    .responseCode(urlResult.getResponseCode())
                    .responseMessage(urlResult.getResponseMessage())
                    .url(urlResult.getUrl())
                    .urlType(urlResult.getUrlType())
                    .atl(new TestingLab(atlDto))
                    .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public List<FailedUrlResult> getDevelopersWithUrl(UrlResult urlResult) {
        List<Developer> devsWithBadUrl = devDao.getByWebsite(urlResult.getUrl());
        return devsWithBadUrl.stream()
            .map(dev -> FailedUrlResult.builder()
                    .lastChecked(urlResult.getLastChecked())
                    .responseCode(urlResult.getResponseCode())
                    .responseMessage(urlResult.getResponseMessage())
                    .url(urlResult.getUrl())
                    .urlType(urlResult.getUrlType())
                    .developer(dev)
                    .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public List<FailedUrlResult> getListingsWithUrl(UrlResult urlResult) {
        List<CertifiedProductSummaryDTO> listingsWithBadUrl
            = cpDao.getSummaryByUrl(urlResult.getUrl(), urlResult.getUrlType());
        return listingsWithBadUrl.stream()
                .map(cpDto -> FailedUrlResult.builder()
                        .lastChecked(urlResult.getLastChecked())
                        .responseCode(urlResult.getResponseCode())
                        .responseMessage(urlResult.getResponseMessage())
                        .url(urlResult.getUrl())
                        .urlType(urlResult.getUrlType())
                        .listing(cpDto)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<FailedUrlResult> getCertificationResultsWithUrl(UrlResult urlResult) {
        List<CertificationResultDetailsDTO> certResultsWithBadUrl
            = certResultDao.getByUrl(urlResult.getUrl(), urlResult.getUrlType());
        return certResultsWithBadUrl.stream()
                .map(certResultDto -> FailedUrlResult.builder()
                        .lastChecked(urlResult.getLastChecked())
                        .responseCode(urlResult.getResponseCode())
                        .responseMessage(urlResult.getResponseMessage())
                        .url(urlResult.getUrl())
                        .urlType(urlResult.getUrlType())
                        .certResult(new CertificationResult(certResultDto))
                        .listing(getAssociatedListing(certResultDto.getCertifiedProductId()))
                        .build())
                .collect(Collectors.toList());
    }

    private CertifiedProductSummaryDTO getAssociatedListing(Long listingId) {
        CertifiedProductSummaryDTO associatedListing = null;
        try {
            associatedListing = cpDao.getSummaryById(listingId);
        } catch (EntityRetrievalException ex) {
            LOGGER.info("Could not find associated listing with id " + listingId);
        }
        return associatedListing;
    }
}
