package gov.healthit.chpl.scheduler.job.urlStatus.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.CertificationStatusUtil;

@Repository("urlCheckerDao")
public class UrlCheckerDao extends BaseDAOImpl {
    private List<String> activeStatuses = CertificationStatusUtil.getActiveStatusNames();

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
    public List<UrlResult> getAllSystemUrls(Logger logger) {
        List<UrlResult> results = new ArrayList<UrlResult>();
        for (UrlType urlType : UrlType.values()) {
            switch (urlType) {
                case ACB:
                    logger.info("Getting all ACB URLs in the system...");
                    List<UrlResult> acbUrls = getAcbUrls();
                    results.addAll(acbUrls);
                    logger.info("Got " + acbUrls.size() + " ACB URLs in the system.");
                    break;
                case ATL:
                    logger.info("Getting all ATL URLs in the system...");
                    List<UrlResult> atlUrls = getAtlUrls();
                    results.addAll(atlUrls);
                    logger.info("Got " + atlUrls.size() + " ATL URLs in the system.");
                    break;
                case DEVELOPER:
                    logger.info("Getting all Developer URLs in the system...");
                    List<UrlResult> developerUrls = getDeveloperUrls();
                    results.addAll(developerUrls);
                    logger.info("Got " + developerUrls.size() + " Developer URLs in the system.");
                    break;
                case MANDATORY_DISCLOSURE:
                    logger.info("Getting all Mandatory Disclosure URLs in the system...");
                    List<UrlResult> mandatoryDisclosureUrls = getMandatoryDisclosureUrls();
                    results.addAll(mandatoryDisclosureUrls);
                    logger.info("Got " + mandatoryDisclosureUrls.size() + " Mandatory Disclosure URLs in the system.");
                    break;
                case TEST_RESULTS_SUMMARY:
                    logger.info("Getting all Test Results Summary URLs in the system...");
                    List<UrlResult> testResultsSummaryUrls = getTestResultsSummaryUrls();
                    results.addAll(testResultsSummaryUrls);
                    logger.info("Got " + testResultsSummaryUrls.size() + " Test Results Summary URLs in the system.");
                    break;
                case FULL_USABILITY_REPORT:
                    logger.info("Getting all Full Usability Report URLs in the system...");
                    List<UrlResult> fullUsabilityReportUrls = getFullUsabilityReportUrls();
                    results.addAll(fullUsabilityReportUrls);
                    logger.info("Got " + fullUsabilityReportUrls.size() + " Full Usability Report URLs in the system.");
                    break;
                case API_DOCUMENTATION:
                    logger.info("Getting all API Documentation URLs in the system...");
                    List<UrlResult> apiDocumentationUrls = getApiDocumentationUrls();
                    results.addAll(apiDocumentationUrls);
                    logger.info("Got " + apiDocumentationUrls.size() + " API Documentation URLs in the system.");
                    break;
                case EXPORT_DOCUMENTATION:
                    logger.info("Getting all Export Documentation URLs in the system...");
                    List<UrlResult> exportDocumentationUrls = getExportDocumentationUrls();
                    results.addAll(exportDocumentationUrls);
                    logger.info("Got " + exportDocumentationUrls.size() + " Export Documentation URLs in the system.");
                    break;
                case DOCUMENTATION:
                    logger.info("Getting all Documentation URLs in the system...");
                    List<UrlResult> documentationUrls = getDocumentationUrls();
                    results.addAll(documentationUrls);
                    logger.info("Got " + documentationUrls.size() + " Documentation URLs in the system.");
                    break;
                case USE_CASES:
                    logger.info("Getting all Use Cases URLs in the system...");
                    List<UrlResult> useCaseUrls = getUseCaseUrls();
                    results.addAll(useCaseUrls);
                    logger.info("Got " + useCaseUrls.size() + " Use Cases URLs in the system.");
                    break;
                case SERVICE_BASE_URL_LIST:
                    logger.info("Getting all Service Base URLs in the system...");
                    List<UrlResult> serviceBaseUrls = getServiceBaseUrlLists();
                    results.addAll(serviceBaseUrls);
                    logger.info("Got " + serviceBaseUrls.size() + " Service Base URLs in the system.");
                    break;
                case REAL_WORLD_TESTING_PLANS:
                    logger.info("Getting all RWT Plan URLs in the system...");
                    List<UrlResult> rwtPlanUrls = getRealWorldTestingPlanUrls();
                    results.addAll(rwtPlanUrls);
                    logger.info("Got " + rwtPlanUrls.size() + " RWT Plan URLs in the system.");
                    break;
                case REAL_WORLD_TESTING_RESULTS:
                    logger.info("Getting all RWT Result URLs in the system...");
                    List<UrlResult> rwtResultUrls = getRealWorldTestingResultUrls();
                    results.addAll(rwtResultUrls);
                    logger.info("Got " + rwtResultUrls.size() + " RWT Result URLs in the system.");
                    break;
                case STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE:
                    logger.info("Getting all SVAP URLs in the system...");
                    List<UrlResult> svapUrls = getSvapUrls();
                    results.addAll(svapUrls);
                    logger.info("Got " + svapUrls.size() + " SVAP URLs in the system.");
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
                        "SELECT DISTINCT mandatoryDisclosures "
                        + "FROM CertifiedProductDetailsEntity "
                        + "WHERE mandatoryDisclosures IS NOT NULL "
                        + "AND mandatoryDisclosures != '' "
                        + "AND certificationStatusName IN (:activeStatuses) "
                        + "AND deleted = false")
                .setParameter("activeStatuses", activeStatuses)
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
                        + "FROM CertifiedProductDetailsEntity "
                        + "WHERE reportFileLocation IS NOT NULL "
                        + "AND reportFileLocation != '' "
                        + "AND certificationStatusName IN (:activeStatuses) "
                        + "AND deleted = false")
                .setParameter("activeStatuses", activeStatuses)
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
                        + "FROM CertifiedProductDetailsEntity "
                        + "WHERE sedReportFileLocation IS NOT NULL "
                        + "AND sedReportFileLocation != '' "
                        + "AND certificationStatusName IN (:activeStatuses) "
                        + "AND deleted = false")
                .setParameter("activeStatuses", activeStatuses)
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
                        "SELECT DISTINCT cre.apiDocumentation "
                        + "FROM CertificationResultEntity cre, CertifiedProductDetailsEntity cpd "
                        + "WHERE cre.certifiedProductId = cpd.id "
                        + "AND cre.apiDocumentation IS NOT NULL "
                        + "AND cre.apiDocumentation != '' "
                        + "AND cpd.certificationStatusName IN (:activeStatuses) "
                        + "AND cre.deleted = false")
                .setParameter("activeStatuses", activeStatuses)
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
                        "SELECT DISTINCT cre.exportDocumentation "
                        + "FROM CertificationResultEntity cre, CertifiedProductDetailsEntity cpd "
                        + "WHERE cre.certifiedProductId = cpd.id "
                        + "AND cre.exportDocumentation IS NOT NULL "
                        + "AND cre.exportDocumentation != '' "
                        + "AND cpd.certificationStatusName IN (:activeStatuses) "
                        + "AND cre.deleted = false")
                .setParameter("activeStatuses", activeStatuses)
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
                        "SELECT DISTINCT cre.documentationUrl "
                        + "FROM CertificationResultEntity cre, CertifiedProductDetailsEntity cpd "
                        + "WHERE cre.certifiedProductId = cpd.id "
                        + "AND cre.documentationUrl IS NOT NULL "
                        + "AND cre.documentationUrl != '' "
                        + "AND cpd.certificationStatusName IN (:activeStatuses) "
                        + "AND cre.deleted = false")
                .setParameter("activeStatuses", activeStatuses)
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
                        "SELECT DISTINCT cre.useCases "
                        + "FROM CertificationResultEntity cre, CertifiedProductDetailsEntity cpd "
                        + "WHERE cre.certifiedProductId = cpd.id "
                        + "AND cre.useCases IS NOT NULL "
                        + "AND cre.useCases != '' "
                        + "AND cpd.certificationStatusName IN (:activeStatuses) "
                        + "AND cre.deleted = false")
                .setParameter("activeStatuses", activeStatuses)
                .getResultList();
        return useCasesWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.USE_CASES)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getServiceBaseUrlLists() {
        @SuppressWarnings("unchecked")
        List<String> useCasesWebsites = entityManager.createQuery(
                        "SELECT DISTINCT cre.serviceBaseUrlList "
                        + "FROM CertificationResultEntity cre, CertifiedProductDetailsEntity cpd "
                        + "WHERE cre.certifiedProductId = cpd.id "
                        + "AND cre.serviceBaseUrlList IS NOT NULL "
                        + "AND cre.serviceBaseUrlList != '' "
                        + "AND cpd.certificationStatusName IN (:activeStatuses) "
                        + "AND cre.deleted = false")
                .setParameter("activeStatuses", activeStatuses)
                .getResultList();
        return useCasesWebsites.stream()
                .filter(website -> !StringUtils.isEmpty(website))
                .map(website -> UrlResult.builder()
                        .url(website)
                        .urlType(UrlType.SERVICE_BASE_URL_LIST)
                        .build())
                .collect(Collectors.toList());
    }

    private List<UrlResult> getRealWorldTestingPlanUrls() {
        @SuppressWarnings("unchecked")
        List<String> rwtPlansWebsites = entityManager.createQuery(
                        "SELECT DISTINCT rwtPlansUrl "
                        + "FROM CertifiedProductDetailsEntity "
                        + "WHERE rwtPlansUrl IS NOT NULL "
                        + "AND rwtPlansUrl != '' "
                        + "AND certificationStatusName IN (:activeStatuses) "
                        + "AND deleted = false")
                .setParameter("activeStatuses", activeStatuses)
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
                        + "FROM CertifiedProductDetailsEntity "
                        + "WHERE rwtResultsUrl IS NOT NULL "
                        + "AND rwtResultsUrl != '' "
                        + "AND certificationStatusName IN (:activeStatuses) "
                        + "AND deleted = false")
                .setParameter("activeStatuses", activeStatuses)
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
                        + "FROM CertifiedProductDetailsEntity "
                        + "WHERE svapNoticeUrl IS NOT NULL "
                        + "AND svapNoticeUrl != '' "
                        + "AND certificationStatusName IN (:activeStatuses) "
                        + "AND deleted = false")
                .setParameter("activeStatuses", activeStatuses)
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
