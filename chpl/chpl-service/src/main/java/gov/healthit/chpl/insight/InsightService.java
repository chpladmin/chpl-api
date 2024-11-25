package gov.healthit.chpl.insight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class InsightService {
    private DeveloperDAO developerDao;
    private ProductDAO productDao;
    private RestTemplate insightRestTemplate;
    private ObjectMapper objectMapper;
    private String unformattedInsightSubmissionsUrl;

    @Autowired
    public InsightService(DeveloperDAO developerDao,
            ProductDAO productDao,
            @Value("${insight.submissionsUrl}") String insightSubmissionsUrl) {
        this.developerDao = developerDao;
        this.productDao = productDao;
        this.insightRestTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        this.objectMapper = new ObjectMapper();
        this.unformattedInsightSubmissionsUrl = insightSubmissionsUrl;
    }

    @Transactional
    public List<InsightSubmission> getInsightSubmissions(Long developerId) throws EntityRetrievalException, InsightRequestFailedException {
        Developer dev = developerDao.getById(developerId); // throws exception if invalid ID
        JsonNode jsonResults = fetchInsightSubmissionsForDeveloper(dev.getId());
        return convertInsightSubmissionsForAllProducts(dev.getId(), jsonResults);
    }

    private JsonNode fetchInsightSubmissionsForDeveloper(Long developerId) throws InsightRequestFailedException {
        String url = String.format(unformattedInsightSubmissionsUrl, developerId.toString());
        LOGGER.info("Making request to " + url);
        ResponseEntity<String> response = null;
        try {
            response = insightRestTemplate.getForEntity(url, String.class);
            LOGGER.debug("Response: " + response.getBody());
        } catch (Exception ex) {
            HttpStatusCode statusCode =  (response != null ? response.getStatusCode() : null);
            if (statusCode == null && ex instanceof RestClientResponseException) {
                statusCode = ((RestClientResponseException) ex).getStatusCode();
            }
            LOGGER.error("Unable to connect to the URL " + url + ". Got response status code " + statusCode);
            throw new InsightRequestFailedException(ex.getMessage(), ex, statusCode);
        }
        String responseBody = response == null ? "" : response.getBody();
        JsonNode root = null;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (IOException ex) {
            LOGGER.error("Could not convert " + responseBody + " to JsonNode object.", ex);
            throw new InsightRequestFailedException("Could not convert " + responseBody + " to expected object.", ex);
        }
        return root;
    }

    private List<InsightSubmission> convertInsightSubmissionsForAllProducts(Long developerId, JsonNode rootNode) {
        List<InsightSubmission> submissions = new ArrayList<InsightSubmission>();
        List<Product> products = productDao.getByDeveloper(developerId);
        if (!CollectionUtils.isEmpty(products)) {
            submissions = products.stream()
                .map(product -> convertInsightSubmissionsJsonForProduct(product, rootNode))
                .filter(insightSubmissions -> !CollectionUtils.isEmpty(insightSubmissions))
                .flatMap(insightSubmissions -> insightSubmissions.stream())
                .collect(Collectors.toList());
        }

        return submissions;
    }

    private List<InsightSubmission> convertInsightSubmissionsJsonForProduct(Product product, JsonNode rootNode) {
        List<InsightSubmission> insightSubmissions = new ArrayList<InsightSubmission>();
        if (rootNode != null) {
            JsonNode productSubmissionItem = rootNode.get(product.getId().toString());
            if (productSubmissionItem != null && productSubmissionItem.isObject()) {
                JsonNode productSubmissionsArr = productSubmissionItem.get("submissions");
                if (productSubmissionsArr != null && productSubmissionsArr.isArray() && productSubmissionsArr.size() > 0) {
                    for (JsonNode productSubmission : productSubmissionsArr) {
                        try {
                            insightSubmissions.add(InsightSubmission.builder()
                                .productId(product.getId())
                                .year(productSubmission.get("insight_year").textValue())
                                .status(productSubmission.get("submission_status").textValue())
                                .build());
                        } catch (Exception ex) {
                            LOGGER.error("Error parsing insight field(s) about product ID " + product.getId(), ex);
                        }
                    }
                }
            }
        }
        return insightSubmissions;
    }
}

