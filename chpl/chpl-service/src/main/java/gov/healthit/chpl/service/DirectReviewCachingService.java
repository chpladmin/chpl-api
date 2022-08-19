package gov.healthit.chpl.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import gov.healthit.chpl.DirectReviewDeserializingObjectMapper;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewContainer;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.exception.JiraRequestFailedException;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.validation.compliance.DirectReviewValidator;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Component("directReviewCachingService")
@Log4j2
public class DirectReviewCachingService {
    private static final int JIRA_DIRECT_REVIEWS_PAGE_SIZE = 50;
    private static final String JIRA_TOTAL_FIELD = "total";
    private static final String JIRA_PAGE_START_INDEX_FIELD = "startAt";
    private static final String JIRA_MAX_RESULTS_FIELD = "maxResults";
    private static final String JIRA_KEY_FIELD = "key";
    private static final String JIRA_ISSUES_FIELD = "issues";
    private static final String JIRA_FIELDS_FIELD = "fields";

    @Value("${jira.baseUrl}")
    private String jiraBaseUrl;

    @Value("${jira.directReviewsUrl}")
    private String jiraAllDirectReviewsUrl;

    @Value("${jira.directReviewsForDeveloperUrl}")
    private String jiraDirectReviewsForDeveloperUrl;

    @Value("${jira.nonconformityUrl}")
    private String jiraNonconformityUrl;

    private DirectReviewValidator drValidator;
    private DeveloperDAO developerDao;
    private RestTemplate jiraAuthenticatedRestTemplate;
    private DirectReviewDeserializingObjectMapper mapper;

    @Autowired
    public DirectReviewCachingService(DirectReviewValidator drValidator,
            DeveloperDAO developerDao, RestTemplate jiraAuthenticatedRestTemplate,
            DirectReviewDeserializingObjectMapper mapper) {
        this.drValidator = drValidator;
        this.developerDao = developerDao;
        this.jiraAuthenticatedRestTemplate = jiraAuthenticatedRestTemplate;
        this.mapper = mapper;
    }

    @CacheEvict(value = { CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH }, allEntries = true)
    public void populateDirectReviewsCache() {
        populateDirectReviewsCache(LOGGER);
    }

    @CacheEvict(value = { CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH }, allEntries = true)
    public void populateDirectReviewsCache(Logger logger) {
        logger.info("Fetching all direct review data.");
        HttpStatus calculatedHttpStatus = null;
        List<DirectReview> allDirectReviews = new ArrayList<DirectReview>();
        try {
            int nextPageStart = 0;
            while (nextPageStart >= 0) {
                JsonNode pageOfDirectReviewsJson = fetchDirectReviewsPage(nextPageStart, JIRA_DIRECT_REVIEWS_PAGE_SIZE, logger);
                List<DirectReview> pageOfDirectReviews = convertDirectReviewsFromJira(pageOfDirectReviewsJson, logger);
                for (DirectReview dr : pageOfDirectReviews) {
                    JsonNode nonConformitiesJson = fetchNonConformities(dr.getJiraKey(), logger);
                    List<DirectReviewNonConformity> ncs = convertNonConformitiesFromJira(nonConformitiesJson, logger);
                    if (ncs != null && ncs.size() > 0) {
                        dr.getNonConformities().addAll(ncs);
                    }
                }
                allDirectReviews.addAll(pageOfDirectReviews);
                nextPageStart = calculateNextPageStart(pageOfDirectReviewsJson);
            }
            calculatedHttpStatus = HttpStatus.OK;
        } catch (JiraRequestFailedException ex) {
            calculatedHttpStatus = ex.getStatusCode();
        }

        if (calculatedHttpStatus == null || !calculatedHttpStatus.is2xxSuccessful()
                && doesCacheHaveAnyOkData()) {
            logger.warn("The request to Jira failed with status " + calculatedHttpStatus + ", but the DR cache "
                    + "currently has at least one record with 'OK' data. Not replacing the cache contents.");
        } else {
            replaceAllDataInDirectReviewCache(allDirectReviews, calculatedHttpStatus, logger);
        }
    }

    public boolean doesCacheHaveAnyOkData() {
        List<DirectReviewContainer> drContainers = getAll();
        return !CollectionUtils.isEmpty(drContainers)
                && drContainers.stream()
                    .filter(drResponse -> drResponse.getHttpStatus() != null && drResponse.getHttpStatus().is2xxSuccessful())
                    .findAny().isPresent();
    }

    private List<DirectReviewContainer> getAll() {
        List<DirectReviewContainer> drContainers = new ArrayList<DirectReviewContainer>();

        Cache drCache = getDirectReviewsCache();
        drContainers = drCache.getAll(drCache.getKeys()).values().stream()
            .map(value -> value.getObjectValue())
            .filter(objValue -> objValue != null && (objValue instanceof DirectReviewContainer))
            .map(objValue -> (DirectReviewContainer) objValue)
            .toList();

        return drContainers;
    }

    private void replaceAllDataInDirectReviewCache(List<DirectReview> allDirectReviews, HttpStatus httpStatus, Logger logger) {
        final LocalDateTime drFetchTime = LocalDateTime.now();
        Cache drCache = getDirectReviewsCache();
        logger.info("Clearing the Direct Review cache.");
        drCache.removeAll();

        //insert an entry in the cache for every developer ID
        List<Developer> allDeveloperIds = developerDao.findAllIdsAndNames();
        logger.info("Adding " + allDeveloperIds.size() + " keys to the Direct Review cache.");
        allDeveloperIds.stream()
            .forEach(dev -> drCache.put(new Element(dev.getId(),
                    DirectReviewContainer.builder()
                        .httpStatus(httpStatus)
                        .fetched(drFetchTime)
                        .build())));

        //insert each direct review into the right place in our cache
        logger.info("Validating " + allDirectReviews.size() + " values into the Direct Review cache.");
        for (DirectReview dr : allDirectReviews) {
            drValidator.review(dr);
            if (CollectionUtils.isEmpty(dr.getErrorMessages())) {
                logger.info("Adding " + dr.getJiraKey() + " for Developer " + dr.getDeveloperId() + " to Direct Review Cache");
                addDirectReviewToCache(drCache, dr, httpStatus, drFetchTime);
            } else {
                logger.warn("Not adding Direct Review " + dr.getJiraKey() + " to the cache. "
                        + "The following error(s) were found: " + System.lineSeparator()
                        + dr.getErrorMessages().stream()
                            .map(errMsg -> "\t" + errMsg)
                            .collect(Collectors.joining(System.lineSeparator())));
            }
        }
    }

    //This is PUBLIC so that AOP/aspectj works correctly.  Aspectj supports weaving for private methods - just need to figure
    //out how to make it work.
    @ListingStoreRemove(removeBy = RemoveBy.DEVELOPER_ID, id = "#dr.developerId")
    public void addDirectReviewToCache(Cache drCache, DirectReview dr, HttpStatus httpStatus, LocalDateTime drFetchTime) {
        if (drCache.get(dr.getDeveloperId()) != null) {
            Element devDirectReviewElement = drCache.get(dr.getDeveloperId());
            Object devDirectReviewsContainerObj = devDirectReviewElement.getObjectValue();
            if (devDirectReviewsContainerObj instanceof DirectReviewContainer) {
                DirectReviewContainer drContainer = (DirectReviewContainer) devDirectReviewsContainerObj;
                drContainer.getDirectReviews().add(dr);
            }
        } else {
            List<DirectReview> devDirectReviews = new ArrayList<DirectReview>();
            devDirectReviews.add(dr);
            DirectReviewContainer drContainer = DirectReviewContainer.builder()
                    .httpStatus(httpStatus)
                    .fetched(drFetchTime)
                    .directReviews(devDirectReviews)
                    .build();
            Element devDirectReviewElement = new Element(dr.getDeveloperId(), drContainer);
            drCache.put(devDirectReviewElement);
        }
    }

    @CachePut(value = CacheNames.DIRECT_REVIEWS, key = "#developerId")
    public DirectReviewContainer getDirectReviews(Long developerId)  {
        return getDirectReviews(developerId, LOGGER);
    }

    @CachePut(value = CacheNames.DIRECT_REVIEWS, key = "#developerId")
    public DirectReviewContainer getDirectReviews(Long developerId, Logger logger) {
        DirectReviewContainer drContainer = null;
        DirectReviewContainer fetchedDrContainer  = getDirectReviewsForDeveloperFromJira(developerId, logger);
        if (fetchedDrContainer != null) {
            drContainer = fetchedDrContainer;
        } else {
            drContainer = getDirectReviewsForDeveloperFromCache(developerId, logger);
        }
        return drContainer;
    }

    private DirectReviewContainer getDirectReviewsForDeveloperFromCache(Long developerId, Logger logger) {
        DirectReviewContainer drContainer = null;
        Element devDirectReviewElement = getDirectReviewsCache().get(developerId);
        if (devDirectReviewElement != null) {
            Object devDirectReviewsContainerObj = devDirectReviewElement.getObjectValue();
            if (devDirectReviewsContainerObj instanceof DirectReviewContainer) {
                drContainer = (DirectReviewContainer) devDirectReviewsContainerObj;
                logger.debug("Developer " + developerId + " has an entry in the cache.");
            }
        }
        return drContainer;
    }

    private DirectReviewContainer getDirectReviewsForDeveloperFromJira(Long developerId, Logger logger) {
        DirectReviewContainer drContainer = null;

        try {
            logger.debug("Trying to fetch direct review data for developer " + developerId);
            JsonNode directReviewsJson = fetchDirectReviews(developerId, logger);
            List<DirectReview> fetchedDrs = convertDirectReviewsFromJira(directReviewsJson, logger);
            for (DirectReview fetchedDr : fetchedDrs) {
                JsonNode nonConformitiesJson = fetchNonConformities(fetchedDr.getJiraKey(), logger);
                List<DirectReviewNonConformity> ncs = convertNonConformitiesFromJira(nonConformitiesJson, logger);
                if (ncs != null && ncs.size() > 0) {
                    fetchedDr.getNonConformities().addAll(ncs);
                }
            }

            Iterator<DirectReview> fetchedDrIter = fetchedDrs.iterator();
            while (fetchedDrIter.hasNext()) {
                DirectReview fetchedDr = fetchedDrIter.next();
                drValidator.review(fetchedDr);
                if (!CollectionUtils.isEmpty(fetchedDr.getErrorMessages())) {
                    logger.warn("Not adding Direct Review " + fetchedDr.getJiraKey() + " to the cache. "
                            + "The following error(s) were found: " + System.lineSeparator()
                            + fetchedDr.getErrorMessages().stream()
                                .map(errMsg -> "\t" + errMsg)
                                .collect(Collectors.joining(System.lineSeparator())));
                    fetchedDrIter.remove();
                }
            }
            drContainer = DirectReviewContainer.builder()
                    .httpStatus(HttpStatus.OK)
                    .fetched(LocalDateTime.now())
                    .directReviews(fetchedDrs)
                    .build();
        } catch (JiraRequestFailedException ex) {
            logger.error("JIRA Request to get Direct Reviews for developer ID " + developerId + " failed.", ex);
        }

        return drContainer;
    }

    @Cacheable(value = CacheNames.DIRECT_REVIEWS, key = "#developerId")
    public DirectReviewContainer getDeveloperDirectReviewsFromCache(Long developerId, Logger logger) {
        DirectReviewContainer drContainer = getDirectReviewsForDeveloperFromCache(developerId, logger);
        if (drContainer == null) {
            drContainer = getDirectReviewsForDeveloperFromJira(developerId, logger);
        }
        return drContainer;
    }

    private Cache getDirectReviewsCache() {
        return CacheManager.getInstance().getCache(CacheNames.DIRECT_REVIEWS);
    }

    private JsonNode fetchDirectReviewsPage(int startAt, int maxResults, Logger logger) throws JiraRequestFailedException {
        String url = String.format(jiraBaseUrl + jiraAllDirectReviewsUrl, startAt, maxResults);
        logger.info("Making request to " + url);
        ResponseEntity<String> response = null;
        try {
            response = jiraAuthenticatedRestTemplate.getForEntity(url, String.class);
            logger.debug("Response: " + response.getBody());
        } catch (HttpClientErrorException httpEx) {
            logger.error("Unable to connect to Jira with the URL " + url + ". Message: " + httpEx.getMessage() + "; response status code " + httpEx.getStatusCode());
            throw new JiraRequestFailedException(httpEx.getMessage(), httpEx, httpEx.getStatusCode());
        } catch (Exception ex) {
            HttpStatus statusCode =  (response != null && response.getStatusCode() != null
                    ? response.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR);
            logger.error("Unable to connect to Jira with the URL " + url + ". Message: " + ex.getMessage() + "; response status code " + statusCode);
            throw new JiraRequestFailedException(ex.getMessage(), ex, statusCode);
        }
        String responseBody = response == null ? "" : response.getBody();
        JsonNode root = null;
        try {
            root = mapper.readTree(responseBody);
        } catch (IOException ex) {
            logger.error("Could not convert " + responseBody + " to JsonNode object.", ex);
        }
        return root;
    }

    private JsonNode fetchDirectReviews(Long developerId, Logger logger) throws JiraRequestFailedException {
        String url = String.format(jiraBaseUrl + jiraDirectReviewsForDeveloperUrl, developerId + "");
        logger.info("Making request to " + url);
        ResponseEntity<String> response = null;
        try {
            response = jiraAuthenticatedRestTemplate.getForEntity(url, String.class);
            logger.debug("Response: " + response.getBody());
        } catch (Exception ex) {
            HttpStatus statusCode =  (response != null ? response.getStatusCode() : null);
            logger.error("Unable to connect to Jira with the URL " + url + ". Got response status code " + statusCode);
            throw new JiraRequestFailedException(ex.getMessage(), ex, statusCode);
        }
        String responseBody = response == null ? "" : response.getBody();
        JsonNode root = null;
        try {
            root = mapper.readTree(responseBody);
        } catch (IOException ex) {
            logger.error("Could not convert " + responseBody + " to JsonNode object.", ex);
        }
        return root;
    }

    private JsonNode fetchNonConformities(String directReviewKey, Logger logger) throws JiraRequestFailedException {
        String url = String.format(jiraBaseUrl + jiraNonconformityUrl, directReviewKey + "");
        logger.info("Making request to " + url);
        ResponseEntity<String> response = null;
        try {
            response = jiraAuthenticatedRestTemplate.getForEntity(url, String.class);
            logger.debug("Response: " + response.getBody());
        } catch (Exception ex) {
            HttpStatus statusCode =  (response != null ? response.getStatusCode() : null);
            logger.error("Unable to connect to Jira with the URL " + url + ". Got response status code " + statusCode);
            throw new JiraRequestFailedException(ex.getMessage(), ex, statusCode);
        }
        String responseBody = response == null ? "" : response.getBody();
        JsonNode root = null;
        try {
            root = mapper.readTree(responseBody);
        } catch (IOException ex) {
            logger.error("Could not convert " + responseBody + " to JsonNode object.", ex);
        }
        return root;
    }

    private List<DirectReview> convertDirectReviewsFromJira(JsonNode rootNode, Logger logger) {
        List<DirectReview> drs = new ArrayList<DirectReview>();
        if (rootNode != null) {
            JsonNode issuesNode = rootNode.get(JIRA_ISSUES_FIELD);
            if (issuesNode != null && issuesNode.isArray() && issuesNode.size() > 0) {
                for (JsonNode issueNode : issuesNode) {
                    try {
                        String jiraKey = issueNode.get(JIRA_KEY_FIELD).textValue();
                        JsonNode fields = issueNode.get(JIRA_FIELDS_FIELD);
                        DirectReview dr = mapper.readValue(fields.toString(), DirectReview.class);
                        dr.setJiraKey(jiraKey);
                        if (dr.getStartDate() != null) {
                            drs.add(dr);
                        }
                    } catch (IOException ex) {
                        logger.error("Cannot map issue JSON to DirectReview class", ex);
                    }
                }
            }
        }
        return drs;
    }

    private List<DirectReviewNonConformity> convertNonConformitiesFromJira(JsonNode rootNode, Logger logger) {
        List<DirectReviewNonConformity> ncs = new ArrayList<DirectReviewNonConformity>();
        if (rootNode != null) {
            JsonNode issuesNode = rootNode.get(JIRA_ISSUES_FIELD);
            if (issuesNode != null && issuesNode.isArray() && issuesNode.size() > 0) {
                for (JsonNode issueNode : issuesNode) {
                    try {
                        String fieldsJson = issueNode.get(JIRA_FIELDS_FIELD).toString();
                        DirectReviewNonConformity nc = mapper.readValue(fieldsJson, DirectReviewNonConformity.class);
                        ncs.add(nc);
                    } catch (IOException ex) {
                        logger.error("Cannot map issue JSON to DirectReviewNonconformity class", ex);
                    }
                }
            }
        }
        return ncs;
    }

    private int calculateNextPageStart(JsonNode rootNode) {
        if (rootNode == null) {
            return 0;
        }
        int totalRecords = rootNode.get(JIRA_TOTAL_FIELD).asInt();
        int currentPageStartIndex = rootNode.get(JIRA_PAGE_START_INDEX_FIELD).asInt();
        int maxResultsReturned = rootNode.get(JIRA_MAX_RESULTS_FIELD).asInt();

        if ((currentPageStartIndex + maxResultsReturned) < totalRecords) {
            return currentPageStartIndex + maxResultsReturned;
        }
        return -1;
    }

    public void setLogger(Logger logger) {

    }
}
