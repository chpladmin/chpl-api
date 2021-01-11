package gov.healthit.chpl.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import gov.healthit.chpl.DirectReviewDeserializingObjectMapper;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.JiraRequestFailedException;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Component("directReviewService")
@Log4j2
public class DirectReviewService {
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

    private DeveloperDAO developerDao;
    private RestTemplate jiraAuthenticatedRestTemplate;
    private DirectReviewDeserializingObjectMapper mapper;

    @Autowired
    public DirectReviewService(DeveloperDAO developerDao, RestTemplate jiraAuthenticatedRestTemplate,
            DirectReviewDeserializingObjectMapper mapper) {
        this.developerDao = developerDao;
        this.jiraAuthenticatedRestTemplate = jiraAuthenticatedRestTemplate;
        this.mapper = mapper;
    }

    public void populateDirectReviewsCache() throws JiraRequestFailedException {
        LOGGER.info("Fetching all direct review data.");
        //Could this response ever be too big? Maybe we would just do it per developer at that point?
        String directReviewsJson = fetchDirectReviews();
        List<DirectReview> allDirectReviews = convertDirectReviewsFromJira(directReviewsJson);
        for (DirectReview dr : allDirectReviews) {
            String nonConformitiesJson = fetchNonConformities(dr.getJiraKey());
            List<DirectReviewNonConformity> ncs = convertNonConformitiesFromJira(nonConformitiesJson);
            if (ncs != null && ncs.size() > 0) {
                dr.getNonConformities().addAll(ncs);
            }
        }

        if (allDirectReviews != null && allDirectReviews.size() > 0) {
            CacheManager manager = CacheManager.getInstance();
            Cache drCache = manager.getCache(CacheNames.DIRECT_REVIEWS);
            LOGGER.info("Clearing the Direct Review cache.");
            drCache.removeAll();

            //insert an entry in the cache for every developer ID
            List<DeveloperDTO> allDeveloperIds = developerDao.findAllIdsAndNames();
            LOGGER.info("Adding " + allDeveloperIds.size() + " keys to the Direct Review cache.");
            allDeveloperIds.stream()
                .forEach(dev -> drCache.put(new Element(dev.getId(), new ArrayList<DirectReview>())));

            //insert each direct review into the right place in our cache
            LOGGER.info("Inserting " + allDirectReviews.size() + " values into the Direct Review cache.");
            for (DirectReview dr : allDirectReviews) {
                if (dr.getDeveloperId() != null) {
                    if (drCache.get(dr.getDeveloperId()) != null) {
                        Element devDirectReviewElement = drCache.get(dr.getDeveloperId());
                        Object devDirectReviewsObj = devDirectReviewElement.getObjectValue();
                        if (devDirectReviewsObj instanceof List<?>) {
                            List<DirectReview> devDirectReviews = (List<DirectReview>) devDirectReviewsObj;
                            devDirectReviews.add(dr);
                        }
                    } else {
                        List<DirectReview> devDirectReviews = new ArrayList<DirectReview>();
                        devDirectReviews.add(dr);
                        Element devDirectReviewElement = new Element(dr.getDeveloperId(), devDirectReviews);
                        drCache.put(devDirectReviewElement);
                    }
                }
            }
        }
    }

    //this will replace the direct reviews for the supplied developerId in the DR cache
    @CachePut(CacheNames.DIRECT_REVIEWS)
    public List<DirectReview> getDirectReviews(Long developerId) throws JiraRequestFailedException {
        LOGGER.info("Fetching direct review data for developer " + developerId);

        String directReviewsJson = fetchDirectReviews(developerId);
        List<DirectReview> drs = convertDirectReviewsFromJira(directReviewsJson);
        for (DirectReview dr : drs) {
            String nonConformitiesJson = fetchNonConformities(dr.getJiraKey());
            List<DirectReviewNonConformity> ncs = convertNonConformitiesFromJira(nonConformitiesJson);
            if (ncs != null && ncs.size() > 0) {
                dr.getNonConformities().addAll(ncs);
            }
        }
        return drs;
    }

    @Cacheable(CacheNames.DIRECT_REVIEWS)
    public List<DirectReview> getDeveloperDirectReviewsFromCache(Long developerId) {
        List<DirectReview> developerDrs = new ArrayList<DirectReview>();
        try {
            developerDrs = getDirectReviews(developerId);
        } catch (JiraRequestFailedException ex) {
            LOGGER.error("Could not fetch DRs from Jira.", ex);
        }
        return developerDrs;
    }

    public List<DirectReview> getListingDirectReviewsFromCache(Long listingId) {
        CacheManager manager = CacheManager.getInstance();
        Cache drCache = manager.getCache(CacheNames.DIRECT_REVIEWS);
        //drCache has key = developerId and value = list of direct reviews
        List<?> developerIdsWithDirectReviews = drCache.getKeys();
        List<DirectReview> allDirectReviews = developerIdsWithDirectReviews.stream()
            .map(key -> drCache.get(key))
            .map(cacheElement -> cacheElement.getObjectValue())
            .filter(cacheObject -> cacheObject instanceof List<?>)
            .map(cacheObject -> (List<DirectReview>) cacheObject)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        return allDirectReviews.stream()
                .filter(dr -> isAssociatedWithListing(dr, listingId))
                .collect(Collectors.toList());
    }

    private boolean isAssociatedWithListing(DirectReview dr, Long listingId) {
        return dr.getNonConformities().stream()
            .flatMap(nc -> nc.getDeveloperAssociatedListings().stream())
            .filter(devAssocListing -> devAssocListing.getId().equals(listingId))
            .findAny().isPresent();
    }

    private String fetchDirectReviews() throws JiraRequestFailedException {
        String url = jiraBaseUrl + jiraAllDirectReviewsUrl;
        LOGGER.info("Making request to " + url);
        ResponseEntity<String> response = null;
        try {
            response = jiraAuthenticatedRestTemplate.getForEntity(url, String.class);
            LOGGER.debug("Response: " + response.getBody());
        } catch (Exception ex) {
            throw new JiraRequestFailedException(ex.getMessage());
        }
        return response == null ? "" : response.getBody();
    }

    private String fetchDirectReviews(Long developerId) throws JiraRequestFailedException {
        String url = String.format(jiraBaseUrl + jiraDirectReviewsForDeveloperUrl, developerId + "");
        LOGGER.info("Making request to " + url);
        ResponseEntity<String> response = null;
        try {
            response = jiraAuthenticatedRestTemplate.getForEntity(url, String.class);
            LOGGER.debug("Response: " + response.getBody());
        } catch (Exception ex) {
            throw new JiraRequestFailedException(ex.getMessage());
        }
        return response == null ? "" : response.getBody();
    }

    private String fetchNonConformities(String directReviewKey) throws JiraRequestFailedException {
        String url = String.format(jiraBaseUrl + jiraNonconformityUrl, directReviewKey + "");
        LOGGER.info("Making request to " + url);
        ResponseEntity<String> response = null;
        try {
            response = jiraAuthenticatedRestTemplate.getForEntity(url, String.class);
            LOGGER.debug("Response: " + response.getBody());
        } catch (Exception ex) {
            throw new JiraRequestFailedException(ex.getMessage());
        }
        return response.getBody();
    }

    private List<DirectReview> convertDirectReviewsFromJira(String json) {
        List<DirectReview> drs = new ArrayList<DirectReview>();
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (IOException ex) {
            LOGGER.error("Could not convert " + json + " to JsonNode object.", ex);
        }
        if (root != null) {
            JsonNode issuesNode = root.get(JIRA_ISSUES_FIELD);
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
                        LOGGER.error("Cannot map issue JSON to DirectReview class", ex);
                    }
                }
            }
        }
        return drs;
    }

    private List<DirectReviewNonConformity> convertNonConformitiesFromJira(String json) {
        List<DirectReviewNonConformity> ncs = new ArrayList<DirectReviewNonConformity>();
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (IOException ex) {
            LOGGER.error("Could not convert " + json + " to JsonNode object.", ex);
        }
        if (root != null) {
            JsonNode issuesNode = root.get(JIRA_ISSUES_FIELD);
            if (issuesNode != null && issuesNode.isArray() && issuesNode.size() > 0) {
                for (JsonNode issueNode : issuesNode) {
                    try {
                        String fieldsJson = issueNode.get(JIRA_FIELDS_FIELD).toString();
                        DirectReviewNonConformity nc = mapper.readValue(fieldsJson, DirectReviewNonConformity.class);
                        ncs.add(nc);
                    } catch (IOException ex) {
                        LOGGER.error("Cannot map issue JSON to DirectReviewNonconformity class", ex);
                    }
                }
            }
        }
        return ncs;
    }
}
