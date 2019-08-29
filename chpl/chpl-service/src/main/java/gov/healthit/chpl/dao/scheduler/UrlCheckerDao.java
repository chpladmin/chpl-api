package gov.healthit.chpl.dao.scheduler;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.scheduler.UrlResultDTO;
import gov.healthit.chpl.dto.scheduler.InheritanceErrorsReportDTO;
import gov.healthit.chpl.dto.scheduler.UrlType;
import gov.healthit.chpl.entity.scheduler.InheritanceErrorsReportEntity;
import gov.healthit.chpl.entity.scheduler.UrlResultEntity;
import gov.healthit.chpl.entity.scheduler.UrlTypeEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.Util;

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
    public List<UrlResultDTO> getAllSystemUrls() {
        List<UrlResultDTO> results = new ArrayList<UrlResultDTO>();
        for (UrlType urlType : UrlType.values()) {
            switch (urlType) {
                case ACB:
                    //query from acb table
                    List<String> acbWebsites =
                        entityManager.createQuery("SELECT website FROM CertificationBodyEntity WHERE deleted = false")
                        .getResultList();
                    for (String website : acbWebsites) {
                        UrlResultDTO checkableUrl = new UrlResultDTO();
                        checkableUrl.setUrl(website);
                        checkableUrl.setUrlType(urlType);
                        results.add(checkableUrl);
                    }
                    break;
                case ATL:
                    //query from atl table
                    List<String> atlWebsites =
                        entityManager.createQuery("SELECT website FROM TestingLabEntity WHERE deleted = false")
                        .getResultList();
                    for (String website : atlWebsites) {
                        UrlResultDTO checkableUrl = new UrlResultDTO();
                        checkableUrl.setUrl(website);
                        checkableUrl.setUrlType(urlType);
                        results.add(checkableUrl);
                    }
                    break;
                case DEVELOPER:
                    //query from developer table
                    List<String> developerWebsites =
                        entityManager.createQuery("SELECT website FROM DeveloperEntity WHERE deleted = false")
                        .getResultList();
                    for (String website : developerWebsites) {
                        UrlResultDTO checkableUrl = new UrlResultDTO();
                        checkableUrl.setUrl(website);
                        checkableUrl.setUrlType(urlType);
                        results.add(checkableUrl);
                    }
                    break;
                case FULL_USABILITY_REPORT:
                    //this query also will get the other two types of urls since they come from the same table
                    List<Object[]> listingWebsites =
                        entityManager.createQuery(
                                "SELECT transparencyAttestationUrl, reportFileLocation, sedReportFileLocation "
                                + "FROM CertifiedProductEntity WHERE deleted = false")
                        .getResultList();
                    for (Object[] websites : listingWebsites) {
                        if (websites[0] != null) {
                            UrlResultDTO checkableUrl = new UrlResultDTO();
                            checkableUrl.setUrl(websites[0].toString());
                            checkableUrl.setUrlType(UrlType.MANDATORY_DISCLOSURE_URL);
                            results.add(checkableUrl);
                        }
                        if (websites[1] != null) {
                            UrlResultDTO checkableUrl = new UrlResultDTO();
                            checkableUrl.setUrl(websites[1].toString());
                            checkableUrl.setUrlType(UrlType.TEST_RESULTS_SUMMARY);
                            results.add(checkableUrl);
                        }
                        if (websites[2] != null) {
                            UrlResultDTO checkableUrl = new UrlResultDTO();
                            checkableUrl.setUrl(websites[2].toString());
                            checkableUrl.setUrlType(UrlType.FULL_USABILITY_REPORT);
                            results.add(checkableUrl);
                        }
                    }
                    break;
                case MANDATORY_DISCLOSURE_URL:
                case TEST_RESULTS_SUMMARY:
                    //handled in the above case since all those urls come from the same table
                    break;
            }
        }

        //get all the existing URL check results to set the last checked time on the returned objects
        List<UrlResultDTO> existingUrlResults = getAllUrlResults();
        for (UrlResultDTO existingUrlResult : existingUrlResults) {
            for (UrlResultDTO systemUrl : results) {
                if (existingUrlResult.getUrl().equalsIgnoreCase(systemUrl.getUrl())
                        && existingUrlResult.getUrlType().getName().equals(systemUrl.getUrlType().getName())) {
                    systemUrl.setLastChecked(existingUrlResult.getLastChecked());
                    systemUrl.setResponseTimeMillis(existingUrlResult.getResponseTimeMillis());
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
    public List<UrlResultDTO> getAllUrlResults() {
        List<UrlResultEntity> entities = entityManager.createQuery("SELECT url "
                        + "FROM UrlResultEntity url "
                        + "JOIN FETCH url.urlType "
                        + "WHERE url.deleted = false")
                .getResultList();
        List<UrlResultDTO> results = new ArrayList<UrlResultDTO>();
        if (entities != null && entities.size() > 0) {
            for (UrlResultEntity entity : entities) {
                UrlResultDTO result = new UrlResultDTO(entity);
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
    public UrlResultDTO createUrlResult(final UrlResultDTO toCreate) throws EntityCreationException {
        if (StringUtils.isEmpty(toCreate.getUrl()) || toCreate.getUrlType() == null) {
            throw new EntityCreationException("A URL and URL Type are required to save a URL result.");
        }

        UrlResultEntity entity = new UrlResultEntity();
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setResponseCode(toCreate.getResponseCode());
        entity.setResponseTimeMillis(toCreate.getResponseTimeMillis());
        entity.setUrl(toCreate.getUrl());
        entity.setLastChecked(toCreate.getLastChecked());
        Long urlTypeId = getUrlTypeIdFromName(toCreate.getUrlType().getName());
        if (urlTypeId == null) {
            throw new EntityCreationException(
                    "No url type ID was found for url type name " + toCreate.getUrlType().getName());
        }
        entity.setUrlTypeId(urlTypeId);
        create(entity);
        return new UrlResultDTO(entity);
    }

    /**
     * Updates the URL with a new checked time, response code, and response time
     * @param toUpdate
     * @throws EntityRetrievalException
     */
    @Transactional
    public UrlResultDTO updateUrlResult(final UrlResultDTO toUpdate) throws EntityRetrievalException {
        UrlResultEntity entity = getUrlResultById(toUpdate.getId());
        if (entity == null) {
            throw new EntityRetrievalException(
                    "No URL with id " + toUpdate.getId() + " was found.");
        }
        entity.setResponseCode(toUpdate.getResponseCode());
        entity.setResponseTimeMillis(toUpdate.getResponseTimeMillis());
        entity.setLastChecked(toUpdate.getLastChecked());
        update(entity);
        return new UrlResultDTO(entity);
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
        List<UrlResultEntity> results = query.getResultList();
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
        List<UrlResultEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0);
    }

    private Long getUrlTypeIdFromName(final String name) {
        Query query =
                entityManager.createQuery("SELECT id FROM UrlTypeEntity WHERE name = :name AND deleted = false");
        query.setParameter("name", name);
        List<Long> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0);
    }
}
