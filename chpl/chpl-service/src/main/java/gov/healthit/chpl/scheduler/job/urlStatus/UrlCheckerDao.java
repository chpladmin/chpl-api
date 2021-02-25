package gov.healthit.chpl.scheduler.job.urlStatus;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

/**
 * Methods supporting the gathering of all URLs and recording results.
 * @author kekey
 *
 */
@Repository("urlCheckerDao")
public class UrlCheckerDao extends BaseDAOImpl {

    /**
     * Get all the URLs in the system.
     * Also gets the last time each of those URLs was checked.
     * @return
     */
    @Transactional
    public List<UrlResult> getAllSystemUrls() {
        List<UrlResult> results = new ArrayList<UrlResult>();
        for (UrlType urlType : UrlType.values()) {
            switch (urlType) {
                case ACB:
                    //query from acb table
                    @SuppressWarnings("unchecked") List<String> acbWebsites =
                        entityManager.createQuery("SELECT DISTINCT website FROM CertificationBodyEntity "
                                + "WHERE website IS NOT NULL AND deleted = false")
                        .getResultList();
                    for (String website : acbWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case ATL:
                    //query from atl table
                    @SuppressWarnings("unchecked") List<String> atlWebsites =
                        entityManager.createQuery("SELECT DISTINCT website FROM TestingLabEntity "
                                + "WHERE website IS NOT NULL AND deleted = false")
                        .getResultList();
                    for (String website : atlWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case DEVELOPER:
                    //query from developer table
                    @SuppressWarnings("unchecked") List<String> developerWebsites =
                        entityManager.createQuery("SELECT DISTINCT website FROM DeveloperEntity "
                                + "WHERE website IS NOT NULL AND deleted = false")
                        .getResultList();
                    for (String website : developerWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case MANDATORY_DISCLOSURE:
                    @SuppressWarnings("unchecked") List<String> mandatoryDisclosureWebsites =
                        entityManager.createQuery(
                                "SELECT DISTINCT transparencyAttestationUrl "
                                + "FROM CertifiedProductEntity "
                                + "WHERE transparencyAttestationUrl IS NOT NULL "
                                + "AND deleted = false")
                        .getResultList();
                    for (String website : mandatoryDisclosureWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case TEST_RESULTS_SUMMARY:
                    @SuppressWarnings("unchecked") List<String> testResultsWebsites =
                    entityManager.createQuery(
                            "SELECT DISTINCT reportFileLocation "
                            + "FROM CertifiedProductEntity "
                            + "WHERE reportFileLocation IS NOT NULL AND deleted = false")
                    .getResultList();
                    for (String website : testResultsWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case FULL_USABILITY_REPORT:
                    @SuppressWarnings("unchecked") List<String> fullUsabilityReportWebsites =
                    entityManager.createQuery(
                            "SELECT DISTINCT sedReportFileLocation "
                            + "FROM CertifiedProductEntity "
                            + "WHERE sedReportFileLocation IS NOT NULL AND deleted = false")
                    .getResultList();
                    for (String website : fullUsabilityReportWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case API_DOCUMENTATION:
                    @SuppressWarnings("unchecked") List<String> apiDocumentationWebsites =
                    entityManager.createQuery(
                            "SELECT DISTINCT apiDocumentation "
                            + "FROM CertificationResultEntity "
                            + "WHERE apiDocumentation IS NOT NULL "
                            + "AND apiDocumentation != '' "
                            + "AND deleted = false")
                    .getResultList();
                    for (String website : apiDocumentationWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case EXPORT_DOCUMENTATION:
                    @SuppressWarnings("unchecked") List<String> exportDocumentationWebsites =
                    entityManager.createQuery(
                            "SELECT DISTINCT exportDocumentation "
                            + "FROM CertificationResultEntity "
                            + "WHERE exportDocumentation IS NOT NULL "
                            + "AND exportDocumentation != '' "
                            + "AND deleted = false")
                    .getResultList();
                    for (String website : exportDocumentationWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case DOCUMENTATION:
                    @SuppressWarnings("unchecked") List<String> documentationUrlWebsites =
                    entityManager.createQuery(
                            "SELECT DISTINCT documentationUrl "
                            + "FROM CertificationResultEntity "
                            + "WHERE documentationUrl IS NOT NULL "
                            + "AND documentationUrl != '' "
                            + "AND deleted = false")
                    .getResultList();
                    for (String website : documentationUrlWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case USE_CASES:
                    @SuppressWarnings("unchecked") List<String> useCasesWebsites =
                    entityManager.createQuery(
                            "SELECT DISTINCT useCases "
                            + "FROM CertificationResultEntity "
                            + "WHERE useCases IS NOT NULL "
                            + "AND useCases != '' "
                            + "AND deleted = false")
                    .getResultList();
                    for (String website : useCasesWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case REAL_WORLD_TESTING_PLANS:
                    @SuppressWarnings("unchecked") List<String> rwtPlansWebsites =
                    entityManager.createQuery(
                            "SELECT DISTINCT rwtPlansUrl "
                            + "FROM CertifiedProductEntity "
                            + "WHERE rwtPlansUrl IS NOT NULL "
                            + "AND rwtPlansUrl != '' "
                            + "AND deleted = false")
                    .getResultList();
                    for (String website : rwtPlansWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case REAL_WORLD_TESTING_RESULTS:
                    @SuppressWarnings("unchecked") List<String> rwtResultsWebsites =
                    entityManager.createQuery(
                            "SELECT DISTINCT rwtResultsUrl "
                            + "FROM CertifiedProductEntity "
                            + "WHERE rwtPlansUrl IS NOT NULL "
                            + "AND rwtPlansUrl != '' "
                            + "AND deleted = false")
                    .getResultList();
                    for (String website : rwtResultsWebsites) {
                        if (!StringUtils.isEmpty(website)) {
                            UrlResult checkableUrl = new UrlResult();
                            checkableUrl.setUrl(website);
                            checkableUrl.setUrlType(urlType);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                    case STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE:
                        @SuppressWarnings("unchecked") List<String> svapNoticeWebsites =
                        entityManager.createQuery(
                                "SELECT DISTINCT svapNoticeUrl "
                                + "FROM CertifiedProductEntity "
                                + "WHERE svapNoticeUrl IS NOT NULL "
                                + "AND svapNoticeUrl != '' "
                                + "AND deleted = false")
                        .getResultList();
                        for (String website : svapNoticeWebsites) {
                            if (!StringUtils.isEmpty(website)) {
                                UrlResult checkableUrl = new UrlResult();
                                checkableUrl.setUrl(website);
                                checkableUrl.setUrlType(urlType);
                                results.add(checkableUrl);
                            }
                        }
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

    /**
     * Get a list of all of the existing checked URLs and their results.
     * @return
     */
    @Transactional
    public List<UrlResult> getAllUrlResults() {
        @SuppressWarnings("unchecked") List<UrlResultEntity> entities = entityManager.createQuery("SELECT url "
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

    /**
     * Get a list of checked URLs that have something other than a 2XX response code
     * or any non-null response message.
     * @return
     */
    @Transactional
    public List<UrlResult> getUrlResultsWithError() {
        @SuppressWarnings("unchecked") List<UrlResultEntity> entities = entityManager.createQuery("SELECT url "
                        + "FROM UrlResultEntity url "
                        + "JOIN FETCH url.urlType "
                        + "WHERE url.deleted = false "
                        + "AND ((url.responseCode < 200 OR url.responseCode > 299) OR url.responseMessage IS NOT NULL)")
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
     * Creates an entry for a url that has been checked.
     * @param toCreate
     * @throws EntityCreationException
     */
    @Transactional
    public UrlResult createUrlResult(final UrlResult toCreate) throws EntityCreationException {
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

    /**
     * Updates the URL with a new checked time, response code, and response time
     * @param toUpdate
     * @throws EntityRetrievalException
     */
    @Transactional
    public UrlResult updateUrlResult(final UrlResult toUpdate) throws EntityRetrievalException {
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

    /**
     * Mark the existing result as deleted.
     * @param resultId
     * @throws EntityRetrievalException if no result with the given ID exists.
     */
    @Transactional
    public void deleteUrlResult(final Long resultId) throws EntityRetrievalException {
        UrlResultEntity toDelete = getUrlResultById(resultId);
        if (toDelete == null) {
            throw new EntityRetrievalException("No URL result with id " + resultId + " exists.");
        }
        toDelete.setDeleted(true);
        update(toDelete);
    }

    private UrlResultEntity getUrlResultById(final Long id) {
        Query query = entityManager.createQuery(
                "SELECT url "
                + "FROM UrlResultEntity url "
                + "JOIN FETCH url.urlType "
                + "WHERE url.id = :id and url.deleted = false");
        query.setParameter("id", id);
        @SuppressWarnings("unchecked") List<UrlResultEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0);
    }

    private UrlResultEntity getUrlResultFromUrlAndType(final String url, final UrlType type) {
        Query query =
                entityManager.createQuery("SELECT url "
                        + "FROM UrlResultEntity url "
                        + "JOIN FETCH url.urlType "
                        + "WHERE url.url = :url "
                        + "AND url.urlType.name = :urlTypeName "
                        + "AND url.deleted = false");
        query.setParameter("url", url);
        query.setParameter("urlTypeName", type.getName());
        @SuppressWarnings("unchecked") List<UrlResultEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0);
    }

    private Long getUrlTypeIdFromName(final String name) {
        Query query =
                entityManager.createQuery("SELECT id FROM UrlTypeEntity WHERE name = :name AND deleted = false");
        query.setParameter("name", name);
        @SuppressWarnings("unchecked") List<Long> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0);
    }
}
