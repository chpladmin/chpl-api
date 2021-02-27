package gov.healthit.chpl.scheduler.job.urlStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("urlCheckerDao")
public class UrlCheckerDao extends BaseDAOImpl {

    @Transactional
    public List<UrlResult> getAllUrlResults() {
        @SuppressWarnings("unchecked")
        List<UrlResultEntity> entities = entityManager.createQuery("SELECT url "
                        + "FROM UrlResultEntity url "
                        + "JOIN FETCH url.urlType "
                        + "WHERE url.deleted = false")
                .getResultList();
        List<UrlResult> results = new ArrayList<UrlResult>();
        if (entities != null && entities.size() > 0) {
            for (UrlResultEntity entity : entities) {
                UrlResult result = new UrlResult(entity);
                results.add(result);
            }
        }
        return results;
    }

    @Transactional
    public List<UrlResult> getUrlResultsWithError() {
        @SuppressWarnings("unchecked")
        List<UrlResultEntity> entities = entityManager.createQuery("SELECT url "
                        + "FROM UrlResultEntity url "
                        + "JOIN FETCH url.urlType "
                        + "WHERE url.deleted = false "
                        + "AND ((url.responseCode < 200 OR url.responseCode > 299) "
                        + "OR url.responseMessage IS NOT NULL)")
                .getResultList();
        List<UrlResult> results = new ArrayList<UrlResult>();
        if (entities != null && entities.size() > 0) {
            for (UrlResultEntity entity : entities) {
                UrlResult result = new UrlResult(entity);
                results.add(result);
            }
        }
        return results;
    }

    /**
     * Get all the URLs in the system.
     * Also gets the last time each of those URLs was checked.
     */
    @Transactional
    public List<UrlResult> getAllSystemUrls() {
        List<UrlResult> results = new ArrayList<UrlResult>();
        for (UrlType urlType : UrlType.values()) {
            switch (urlType) {
                case ACB:
                    results.addAll(getAcbUrls());
                    break;
                case ATL:
                    results.addAll(getAtlUrls());
                    break;
                case DEVELOPER:
                    results.addAll(getDeveloperUrls());
                    break;
                case MANDATORY_DISCLOSURE:
                    results.addAll(getMandatoryDisclosureUrls());
                    break;
                case TEST_RESULTS_SUMMARY:
                    results.addAll(getTestResultsSummaryUrls());
                    break;
                case FULL_USABILITY_REPORT:
                   results.addAll(getFullUsabilityReportUrls());
                    break;
                case API_DOCUMENTATION:
                    results.addAll(getApiDocumentationUrls());
                    break;
                case EXPORT_DOCUMENTATION:
                    results.addAll(getExportDocumentationUrls());
                    break;
                case DOCUMENTATION:
                    results.addAll(getDocumentationUrls());
                    break;
                case USE_CASES:
                    results.addAll(getUseCaseUrls());
                    break;
                case REAL_WORLD_TESTING_PLANS:
                   results.addAll(getRealWorldTestingPlanUrls());
                    break;
                case REAL_WORLD_TESTING_RESULTS:
                    results.addAll(getRealWorldTestingResultUrls());
                    break;
                case STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE:
                    results.addAll(getSvapUrls());
                    break;
                default:
                    break;
            }
        }

        //get all the existing URL check results to set the last checked time on the returned objects
        List<UrlResult> existingUrlResults = getAllUrlResults();
        for (UrlResult existingUrlResult : existingUrlResults) {
            for (UrlResult systemUrl : results) {
                if (existingUrlResult.getUrl().equalsIgnoreCase(systemUrl.getUrl())
                        && existingUrlResult.getUrlType().getName().equals(systemUrl.getUrlType().getName())) {
                    systemUrl.setLastChecked(existingUrlResult.getLastChecked());
                    systemUrl.setResponseMessage(existingUrlResult.getResponseMessage());
                    systemUrl.setResponseCode(existingUrlResult.getResponseCode());
                }
            }
        }
        return results;
    }

    private List<UrlResult> getAcbUrls() {
        @SuppressWarnings("unchecked")
        List<String> acbWebsites = entityManager.createQuery(
                "SELECT DISTINCT website "
                + "FROM CertificationBodyEntity "
                + "WHERE website IS NOT NULL "
                + "AND website != '' "
                + "AND deleted = false")
            .getResultList();
        return acbWebsites.stream()
            .filter(website -> !StringUtils.isEmpty(website))
            .map(website -> UrlResult.builder()
                    .url(website)
                    .urlType(UrlType.ACB)
                    .build())
            .collect(Collectors.toList());
    }

    private List<UrlResult> getAtlUrls() {
        @SuppressWarnings("unchecked")
        List<String> atlWebsites = entityManager.createQuery(
                "SELECT DISTINCT website "
                + "FROM TestingLabEntity "
                + "WHERE website IS NOT NULL "
                + "AND website != '' "
                + "AND deleted = false")
                .getResultList();
        return atlWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.ATL)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getDeveloperUrls() {
        @SuppressWarnings("unchecked")
        List<String> developerWebsites = entityManager.createQuery(
                "SELECT DISTINCT website "
                + "FROM DeveloperEntity "
                + "WHERE website IS NOT NULL "
                + "AND website != '' "
                + "AND deleted = false")
                .getResultList();
        return developerWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.DEVELOPER)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getMandatoryDisclosureUrls() {
        @SuppressWarnings("unchecked")
        List<String> mandatoryDisclosureWebsites = entityManager.createQuery(
                        "SELECT DISTINCT transparencyAttestationUrl "
                        + "FROM CertifiedProductEntity "
                        + "WHERE transparencyAttestationUrl IS NOT NULL "
                        + "AND transparencyAttestationUrl != '' "
                        + "AND deleted = false")
                .getResultList();
        return mandatoryDisclosureWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.MANDATORY_DISCLOSURE)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getTestResultsSummaryUrls() {
        @SuppressWarnings("unchecked")
        List<String> testResultsWebsites = entityManager.createQuery(
                        "SELECT DISTINCT reportFileLocation "
                        + "FROM CertifiedProductEntity "
                        + "WHERE reportFileLocation IS NOT NULL "
                        + "AND reportFileLocation != '' "
                        + "AND deleted = false")
                .getResultList();
        return testResultsWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.TEST_RESULTS_SUMMARY)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getFullUsabilityReportUrls() {
        @SuppressWarnings("unchecked")
        List<String> fullUsabilityReportWebsites = entityManager.createQuery(
                        "SELECT DISTINCT sedReportFileLocation "
                        + "FROM CertifiedProductEntity "
                        + "WHERE sedReportFileLocation IS NOT NULL "
                        + "AND sedReportFileLocation != '' "
                        + "AND deleted = false")
                .getResultList();
        return fullUsabilityReportWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.FULL_USABILITY_REPORT)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getApiDocumentationUrls() {
        @SuppressWarnings("unchecked")
        List<String> apiDocumentationWebsites = entityManager.createQuery(
                        "SELECT DISTINCT apiDocumentation "
                        + "FROM CertificationResultEntity "
                        + "WHERE apiDocumentation IS NOT NULL "
                        + "AND apiDocumentation != '' "
                        + "AND deleted = false")
                .getResultList();
        return apiDocumentationWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.API_DOCUMENTATION)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getExportDocumentationUrls() {
        @SuppressWarnings("unchecked")
        List<String> exportDocumentationWebsites = entityManager.createQuery(
                        "SELECT DISTINCT exportDocumentation "
                        + "FROM CertificationResultEntity "
                        + "WHERE exportDocumentation IS NOT NULL "
                        + "AND exportDocumentation != '' "
                        + "AND deleted = false")
                .getResultList();
        return exportDocumentationWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.EXPORT_DOCUMENTATION)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getDocumentationUrls() {
        @SuppressWarnings("unchecked")
        List<String> documentationUrlWebsites = entityManager.createQuery(
                        "SELECT DISTINCT documentationUrl "
                        + "FROM CertificationResultEntity "
                        + "WHERE documentationUrl IS NOT NULL "
                        + "AND documentationUrl != '' "
                        + "AND deleted = false")
                .getResultList();
        return documentationUrlWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.DOCUMENTATION)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getUseCaseUrls() {
        @SuppressWarnings("unchecked")
        List<String> useCasesWebsites = entityManager.createQuery(
                        "SELECT DISTINCT useCases "
                        + "FROM CertificationResultEntity "
                        + "WHERE useCases IS NOT NULL "
                        + "AND useCases != '' "
                        + "AND deleted = false")
                .getResultList();
        return useCasesWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.USE_CASES)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getRealWorldTestingPlanUrls() {
        @SuppressWarnings("unchecked")
        List<String> rwtPlansWebsites = entityManager.createQuery(
                        "SELECT DISTINCT rwtPlansUrl "
                        + "FROM CertifiedProductEntity "
                        + "WHERE rwtPlansUrl IS NOT NULL "
                        + "AND rwtPlansUrl != '' "
                        + "AND deleted = false")
                .getResultList();
        return rwtPlansWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.REAL_WORLD_TESTING_PLANS)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getRealWorldTestingResultUrls() {
        @SuppressWarnings("unchecked")
        List<String> rwtResultsWebsites = entityManager.createQuery(
                        "SELECT DISTINCT rwtResultsUrl "
                        + "FROM CertifiedProductEntity "
                        + "WHERE rwtResultsUrl IS NOT NULL "
                        + "AND rwtResultsUrl != '' "
                        + "AND deleted = false")
                .getResultList();
        return rwtResultsWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.REAL_WORLD_TESTING_RESULTS)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getSvapUrls() {
        @SuppressWarnings("unchecked")
        List<String> svapNoticeWebsites = entityManager.createQuery(
                        "SELECT DISTINCT svapNoticeUrl "
                        + "FROM CertifiedProductEntity "
                        + "WHERE svapNoticeUrl IS NOT NULL "
                        + "AND svapNoticeUrl != '' "
                        + "AND deleted = false")
                .getResultList();
        return svapNoticeWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public UrlResult createUrlResult(UrlResult toCreate) throws EntityCreationException {
        if (StringUtils.isEmpty(toCreate.getUrl()) || toCreate.getUrlType() == null) {
            throw new EntityCreationException("A URL and URL Type are required to save a URL result.");
        }

        UrlResultEntity entity = new UrlResultEntity();
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setResponseCode(toCreate.getResponseCode());
        entity.setResponseMessage(toCreate.getResponseMessage());
        entity.setUrl(toCreate.getUrl());
        entity.setLastChecked(toCreate.getLastChecked());
        Long urlTypeId = getUrlTypeIdFromName(toCreate.getUrlType().getName());
        if (urlTypeId == null) {
            throw new EntityCreationException(
                    "No url type ID was found for url type name " + toCreate.getUrlType().getName());
        }
        entity.setUrlTypeId(urlTypeId);
        create(entity);
        return new UrlResult(entity);
    }

    @Transactional
    public UrlResult updateUrlResult(UrlResult toUpdate) throws EntityRetrievalException {
        UrlResultEntity entity = getUrlResultById(toUpdate.getId());
        if (entity == null) {
            throw new EntityRetrievalException(
                    "No URL with id " + toUpdate.getId() + " was found.");
        }
        entity.setResponseCode(toUpdate.getResponseCode());
        entity.setResponseMessage(toUpdate.getResponseMessage());
        entity.setLastChecked(toUpdate.getLastChecked());
        update(entity);
        return new UrlResult(entity);
    }

    @Transactional
    public void deleteUrlResult(Long resultId) throws EntityRetrievalException {
        UrlResultEntity toDelete = getUrlResultById(resultId);
        if (toDelete == null) {
            throw new EntityRetrievalException("No URL result with id " + resultId + " exists.");
        }
        toDelete.setDeleted(true);
        update(toDelete);
    }

    private UrlResultEntity getUrlResultById(Long id) {
        Query query = entityManager.createQuery(
                "SELECT url "
                + "FROM UrlResultEntity url "
                + "JOIN FETCH url.urlType "
                + "WHERE url.id = :id and url.deleted = false");
        query.setParameter("id", id);
        @SuppressWarnings("unchecked")
        List<UrlResultEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0);
    }

    private Long getUrlTypeIdFromName(String name) {
        Query query =
                entityManager.createQuery("SELECT id FROM UrlTypeEntity WHERE name = :name AND deleted = false");
        query.setParameter("name", name);
        @SuppressWarnings("unchecked")
        List<Long> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0);
    }
}
