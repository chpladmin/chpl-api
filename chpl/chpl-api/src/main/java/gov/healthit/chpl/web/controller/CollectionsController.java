package gov.healthit.chpl.web.controller;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.DecertifiedDeveloper;
import gov.healthit.chpl.domain.DeveloperTransparency;
import gov.healthit.chpl.domain.search.BasicSearchResponseLegacy;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResultLegacy;
import gov.healthit.chpl.domain.search.CertifiedProductSearchResultLegacy;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.search.CertifiedProductSearchManager;
import gov.healthit.chpl.search.domain.BasicSearchResponse;
import gov.healthit.chpl.search.domain.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.search.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.search.domain.SearchViews;
import gov.healthit.chpl.service.DirectReviewSearchService;
import gov.healthit.chpl.web.controller.annotation.CacheControl;
import gov.healthit.chpl.web.controller.annotation.CacheMaxAge;
import gov.healthit.chpl.web.controller.annotation.CachePolicy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "collections", description = "Endpoints to get collections of the CHPL listings.")
@RestController
@RequestMapping("/collections")
@Log4j2
@Loggable
public class CollectionsController {
    private CertifiedProductSearchManager certifiedProductSearchManager;
    private DeveloperManager developerManager;
    private DirectReviewSearchService drService;

    @Autowired
    public CollectionsController(CertifiedProductSearchManager certifiedProductSearchManager,
            DeveloperManager developerManager, DirectReviewSearchService drService) {
        this.certifiedProductSearchManager = certifiedProductSearchManager;
        this.developerManager = developerManager;
        this.drService = drService;
    }

    /**
     * Get basic data about all listings in the system.
     * @param delimitedFieldNames the names of the fields needed for each listing
     * @return an array of the listings
     * @throws JsonProcessingException if processing fails
     */
    @Operation(summary = "Get basic data about all certified products in the system.", description = "")
    @RequestMapping(value = "/certified-products", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.SIX_HOURS)
    public @ResponseBody String getAllCertifiedProducts(
            @RequestParam(value = "fields", required = false) final String delimitedFieldNames)
            throws JsonProcessingException {
        List<CertifiedProductFlatSearchResult> cachedSearchResults = certifiedProductSearchManager.getFlatListingCollection();

        String result = "";
        if (!StringUtils.isEmpty(delimitedFieldNames)) {
            // write out objects as json but do not include properties with a
            // null value
            ObjectMapper nonNullJsonMapper = new ObjectMapper();
            nonNullJsonMapper.setSerializationInclusion(Include.NON_NULL);
            nonNullJsonMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);

            // create a copy of the search results since we will be manipulating
            // them by setting fields to null but do not want to overwrite the cached data
            List<CertifiedProductFlatSearchResult> mutableSearchResults
            = new ArrayList<CertifiedProductFlatSearchResult>(cachedSearchResults.size());
            for (CertifiedProductFlatSearchResult cachedSearchResult : cachedSearchResults) {
                mutableSearchResults.add(new CertifiedProductFlatSearchResult(cachedSearchResult));
            }

            // parse the field names that we want to send back
            String[] fieldNames = delimitedFieldNames.split(",");
            List<String> requiredFields = new ArrayList<String>(fieldNames.length);
            for (int i = 0; i < fieldNames.length; i++) {
                requiredFields.add(fieldNames[i].toUpperCase(Locale.ENGLISH));
            }

            // compare all the field names in the results with the required
            // field names
            List<Field> searchResultFields = getAllInheritedFields(CertifiedProductFlatSearchResult.class,
                    new ArrayList<Field>());
            for (Field searchResultField : searchResultFields) {
                // is this searchResultField a required one?
                boolean isSearchResultFieldRequired = false;
                for (String requiredField : requiredFields) {
                    if (searchResultField.getName().equalsIgnoreCase(requiredField)) {
                        isSearchResultFieldRequired = true;
                    }
                }

                // if the field is not required, set it to null
                // assumes standard java bean getter/setter names
                if (!isSearchResultFieldRequired
                        && !searchResultField.getName().equalsIgnoreCase("serialVersionUID")
                        && !searchResultField.getName().equalsIgnoreCase("CERTS_SPLIT_CHAR")) {
                    // what type is the field? String? Long?
                    Class searchResultFieldTypeClazz = searchResultField.getType();
                    // find the setter method that accepts the correct type
                    String firstUppercaseChar = searchResultField.getName().charAt(0) + "";
                    firstUppercaseChar = firstUppercaseChar.toUpperCase(Locale.ENGLISH);
                    String setterMethodName = "set" + firstUppercaseChar + searchResultField.getName().substring(1);
                    try {
                        Method setter = CertifiedProductFlatSearchResult.class.getMethod(setterMethodName,
                                searchResultFieldTypeClazz);
                        // call the setter method and set to null
                        if (setter != null) {
                            for (CertifiedProductSearchResult searchResult : mutableSearchResults) {
                                setter.invoke(searchResult, new Object[] {
                                        null
                                });
                            }
                        } else {
                            LOGGER.error("No method with name " + setterMethodName + " was found for field "
                                    + searchResultField.getName() + " and argument type "
                                    + searchResultFieldTypeClazz.getName());
                        }
                    } catch (final NoSuchMethodException ex) {
                        LOGGER.error("No method with name " + setterMethodName + " was found for field "
                                + searchResultField.getName() + " and argument type "
                                + searchResultFieldTypeClazz.getName(), ex);
                    } catch (final InvocationTargetException ex) {
                        LOGGER.error("exception invoking method " + setterMethodName, ex);
                    } catch (final IllegalArgumentException ex) {
                        LOGGER.error("bad arguments to method " + setterMethodName, ex);
                    } catch (final IllegalAccessException ex) {
                        LOGGER.error("Cannot access method " + setterMethodName, ex);
                    }
                }
            }
            BasicSearchResponse response = new BasicSearchResponse();
            response.setResults(mutableSearchResults);
            response.setDirectReviewsAvailable(drService.getDirectReviewsAvailable());
            result = nonNullJsonMapper.writeValueAsString(response);
        } else {
            ObjectMapper viewMapper = new ObjectMapper();
            viewMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
            BasicSearchResponse response = new BasicSearchResponse();
            response.setResults(cachedSearchResults);
            response.setDirectReviewsAvailable(drService.getDirectReviewsAvailable());
            result = viewMapper.writerWithView(SearchViews.Default.class).writeValueAsString(response);
        }

        return result;
    }

    /**
     * DEPRECATED. Use /collections/certified-products.
     * Get basic data about all listings in the system.
     * @param delimitedFieldNames the names of the fields needed for each listing
     * @return an array of the listings
     * @throws JsonProcessingException if processing fails
     */
    @Deprecated
    @Operation(summary = "Get basic data about all certified products in the system.", description = "", deprecated = true)
    @RequestMapping(value = "/certified_products", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.SIX_HOURS)
    public @ResponseBody String getAllCertifiedProductsLegacy(
            @RequestParam(value = "fields", required = false) final String delimitedFieldNames)
            throws JsonProcessingException {
        List<CertifiedProductFlatSearchResultLegacy> cachedSearchResults = certifiedProductSearchManager.searchLegacy();

        String result = "";
        if (!StringUtils.isEmpty(delimitedFieldNames)) {
            // write out objects as json but do not include properties with a
            // null value
            ObjectMapper nonNullJsonMapper = new ObjectMapper();
            nonNullJsonMapper.setSerializationInclusion(Include.NON_NULL);
            nonNullJsonMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);

            // create a copy of the search results since we will be manipulating
            // them by setting fields to null but do not want to overwrite the cached data
            List<CertifiedProductFlatSearchResultLegacy> mutableSearchResults
            = new ArrayList<CertifiedProductFlatSearchResultLegacy>(cachedSearchResults.size());
            for (CertifiedProductFlatSearchResultLegacy cachedSearchResult : cachedSearchResults) {
                mutableSearchResults.add(new CertifiedProductFlatSearchResultLegacy(cachedSearchResult));
            }

            // parse the field names that we want to send back
            String[] fieldNames = delimitedFieldNames.split(",");
            List<String> requiredFields = new ArrayList<String>(fieldNames.length);
            for (int i = 0; i < fieldNames.length; i++) {
                requiredFields.add(fieldNames[i].toUpperCase(Locale.ENGLISH));
            }

            // compare all the field names in the results with the required
            // field names
            List<Field> searchResultFields = getAllInheritedFields(CertifiedProductFlatSearchResultLegacy.class,
                    new ArrayList<Field>());
            for (Field searchResultField : searchResultFields) {
                // is this searchResultField a required one?
                boolean isSearchResultFieldRequired = false;
                for (String requiredField : requiredFields) {
                    if (searchResultField.getName().equalsIgnoreCase(requiredField)) {
                        isSearchResultFieldRequired = true;
                    }
                }

                // if the field is not required, set it to null
                // assumes standard java bean getter/setter names
                if (!isSearchResultFieldRequired
                        && !searchResultField.getName().equalsIgnoreCase("serialVersionUID")
                        && !searchResultField.getName().equalsIgnoreCase("CERTS_SPLIT_CHAR")) {
                    // what type is the field? String? Long?
                    Class searchResultFieldTypeClazz = searchResultField.getType();
                    // find the setter method that accepts the correct type
                    String firstUppercaseChar = searchResultField.getName().charAt(0) + "";
                    firstUppercaseChar = firstUppercaseChar.toUpperCase(Locale.ENGLISH);
                    String setterMethodName = "set" + firstUppercaseChar + searchResultField.getName().substring(1);
                    try {
                        Method setter = CertifiedProductFlatSearchResultLegacy.class.getMethod(setterMethodName,
                                searchResultFieldTypeClazz);
                        // call the setter method and set to null
                        if (setter != null) {
                            for (CertifiedProductSearchResultLegacy searchResult : mutableSearchResults) {
                                setter.invoke(searchResult, new Object[] {
                                        null
                                });
                            }
                        } else {
                            LOGGER.error("No method with name " + setterMethodName + " was found for field "
                                    + searchResultField.getName() + " and argument type "
                                    + searchResultFieldTypeClazz.getName());
                        }
                    } catch (final NoSuchMethodException ex) {
                        LOGGER.error("No method with name " + setterMethodName + " was found for field "
                                + searchResultField.getName() + " and argument type "
                                + searchResultFieldTypeClazz.getName(), ex);
                    } catch (final InvocationTargetException ex) {
                        LOGGER.error("exception invoking method " + setterMethodName, ex);
                    } catch (final IllegalArgumentException ex) {
                        LOGGER.error("bad arguments to method " + setterMethodName, ex);
                    } catch (final IllegalAccessException ex) {
                        LOGGER.error("Cannot access method " + setterMethodName, ex);
                    }
                }
            }
            BasicSearchResponseLegacy response = new BasicSearchResponseLegacy();
            response.setResults(mutableSearchResults);
            result = nonNullJsonMapper.writeValueAsString(response);
        } else {
            ObjectMapper viewMapper = new ObjectMapper();
            viewMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
            BasicSearchResponseLegacy response = new BasicSearchResponseLegacy();
            response.setResults(cachedSearchResults);
            result = viewMapper.writerWithView(SearchViews.Default.class).writeValueAsString(response);
        }

        return result;
    }

    @Deprecated
    @Operation(summary = "DEPRECATED. Get a list of all developers with transparency attestation URLs and ACB attestations.",
            deprecated = true)
    @RequestMapping(value = "/developers", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody List<DeveloperTransparency> getDeveloperCollection() {
        List<DeveloperTransparency> developerResults = developerManager.getDeveloperCollection();
        return developerResults;
    }

    @Operation(summary = "Get a list of all banned developers.",
            description = "")
    @RequestMapping(value = "/decertified-developers", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody List<DecertifiedDeveloper> getDecertifiedDeveloperCollection() {
        List<DecertifiedDeveloper> developerResults = developerManager.getDecertifiedDeveloperCollection();
        return developerResults;
    }

    private List<Field> getAllInheritedFields(final Class clazz, final List<Field> fields) {
        Class superClazz = clazz.getSuperclass();
        if (superClazz != null) {
            getAllInheritedFields(superClazz, fields);
        }
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        return fields;
    }
}
