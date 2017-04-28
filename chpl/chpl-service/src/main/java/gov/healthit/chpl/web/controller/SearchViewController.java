package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.SearchOption;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.domain.SurveillanceRequirementOptions;
import gov.healthit.chpl.domain.SurveillanceSearchOptions;
import gov.healthit.chpl.domain.search.BasicSearchResponse;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.search.SearchViews;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.SearchMenuManager;
import gov.healthit.chpl.web.controller.results.DecertifiedDeveloperResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api
@RestController
public class SearchViewController {
	
	@Autowired Environment env;
	
	@Autowired
	private SearchMenuManager searchMenuManager;
	
	@Autowired
	private CertifiedProductSearchManager certifiedProductSearchManager;
	
	@Autowired
	private CertifiedProductDetailsManager certifiedProductDetailsManager;
	
	@Autowired
	private DeveloperManager developerManager;
	
	@Autowired
	private CertifiedProductSearchResultDAO certifiedProductSearchResultDao;
		
	private static final Logger logger = LogManager.getLogger(SearchViewController.class);

	@ApiOperation(value="Get basic data about all certified products in the system.", 
			notes="")
	@RequestMapping(value="/certified_products", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody String getAllCertifiedProducts(@RequestParam(value="fields", required=false) String delimitedFieldNames) 
	throws JsonProcessingException {
		
		List<CertifiedProductFlatSearchResult> cachedSearchResults = certifiedProductSearchManager.search();
		
		String result = "";		
		if(!StringUtils.isEmpty(delimitedFieldNames)) {
			//write out objects as json but do not include properties with a null value
			ObjectMapper nonNullJsonMapper = new ObjectMapper();
			nonNullJsonMapper.setSerializationInclusion(Include.NON_NULL);
			
			//create a copy of the search results since we will be manipulating them 
			//by setting fields to null but do not want to overwrite the cached data
			List<CertifiedProductFlatSearchResult> mutableSearchResults = new ArrayList<CertifiedProductFlatSearchResult>(cachedSearchResults.size());
			for(CertifiedProductFlatSearchResult cachedSearchResult : cachedSearchResults) {
				mutableSearchResults.add(new CertifiedProductFlatSearchResult(cachedSearchResult));
			}
			
			//parse the field names that we want to send back
			String[] fieldNames = delimitedFieldNames.split(",");
			List<String> requiredFields = new ArrayList<String>(fieldNames.length);
			for(int i = 0; i < fieldNames.length; i++) {
				requiredFields.add(fieldNames[i].toUpperCase());
			}
			
			//compare all the field names in the results with the required field names
			List<Field> searchResultFields = getAllInheritedFields(CertifiedProductFlatSearchResult.class, new ArrayList<Field>());
			for(Field searchResultField : searchResultFields) {
				//is this searchResultField a required one?
				boolean isSearchResultFieldRequired = false;
				for(String requiredField : requiredFields) {
					if(searchResultField.getName().equalsIgnoreCase(requiredField)) {
						isSearchResultFieldRequired = true;
					}
				}
				
				//if the field is not required, set it to null
				//assumes standard java bean getter/setter names
				if(!isSearchResultFieldRequired && !searchResultField.getName().equalsIgnoreCase("serialVersionUID")) {
					//what type is the field? String? Long?
					Class searchResultFieldTypeClazz = searchResultField.getType();
					//find the setter method that accepts the correct type
					String firstUppercaseChar = searchResultField.getName().charAt(0)+"";
					firstUppercaseChar = firstUppercaseChar.toUpperCase();
					String setterMethodName = "set" + firstUppercaseChar + searchResultField.getName().substring(1);
					try {
						Method setter = CertifiedProductFlatSearchResult.class.getMethod(setterMethodName, searchResultFieldTypeClazz);
						//call the setter method and set to null
						if(setter != null) {
							for(CertifiedProductSearchResult searchResult : mutableSearchResults) {
								setter.invoke(searchResult, new Object[]{ null });
							}
						} else {
							logger.error("No method with name " + setterMethodName + " was found for field " + searchResultField.getName() + " and argument type " + searchResultFieldTypeClazz.getName());
						}
					} catch(NoSuchMethodException ex) {
						logger.error("No method with name " + setterMethodName + " was found for field " + searchResultField.getName() + " and argument type " + searchResultFieldTypeClazz.getName(), ex);
					} catch(InvocationTargetException ex) {
						logger.error("exception invoking method " + setterMethodName, ex);
					} catch(IllegalArgumentException ex) {
						logger.error("bad arguments to method " + setterMethodName, ex);
					} catch(IllegalAccessException ex) {
						logger.error("Cannot access method " + setterMethodName, ex);
					}
				}
			}
			BasicSearchResponse response = new BasicSearchResponse();
			response.setResults(mutableSearchResults);
			result = nonNullJsonMapper.writeValueAsString(response);
		} else {
			ObjectMapper viewMapper = new ObjectMapper();
			BasicSearchResponse response = new BasicSearchResponse();
			response.setResults(cachedSearchResults);
			 result = viewMapper.writerWithView(SearchViews.Default.class).writeValueAsString(response);
		}

		return result;
	}
	
	@ApiOperation(value="Get all data about a certified product.", 
			notes="")
	@RequestMapping(value="/certified_product_details", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertifiedProductSearchDetails getCertifiedProductDetails(@RequestParam("productId") Long id) throws EntityRetrievalException{
		
		CertifiedProductSearchDetails product = certifiedProductDetailsManager.getCertifiedProductDetails(id);
		return product;
	}

	@ApiOperation(value="Download the entire CHPL as XML.", 
			notes="Once per day, the entire certified product listing is written out to an XML "
					+ "file on the CHPL servers. This method allows any user to download that XML file. "
					+ "It is formatted in such a way that users may import it into Microsoft Excel or any other XML "
					+ "tool of their choosing.")
	@RequestMapping(value="/download", method=RequestMethod.GET,
			produces="application/xml")
	public void download(@RequestParam(value="edition", required=false) String edition, 
			@RequestParam(value="format", defaultValue="xml", required=false) String format,
			HttpServletRequest request, HttpServletResponse response) throws IOException {	
		String downloadFileLocation = env.getProperty("downloadFolderPath");
		File downloadFile = new File(downloadFileLocation);
		if(!downloadFile.exists() || !downloadFile.canRead()) {
			response.getWriter().write("Cannot read download file at " + downloadFileLocation + ". File does not exist or cannot be read.");
			return;
		}
		
		if(downloadFile.isDirectory()) {
			//find the most recent file in the directory and use that
			File[] children = downloadFile.listFiles();
			if(children == null || children.length == 0) {
				response.getWriter().write("No files for download were found in directory " + downloadFileLocation);
				return;
			} else {
				if(!StringUtils.isEmpty(edition)) {
					//make sure it's a 4 character year
					edition = edition.trim();
					if(!edition.startsWith("20")) {
						edition = "20"+edition;
					}
				} else {
					edition = "all";
				}
				
				if(!StringUtils.isEmpty(format) && format.equalsIgnoreCase("csv")) {
					format = "csv";
				} else {
					format = "xml";
				}
				
				File newestFileWithFormat = null;
				for(int i = 0; i < children.length; i++) {
					
					if(children[i].getName().matches("^chpl-" + edition + "-.+\\." + format+"$")) {
						if(newestFileWithFormat == null) {
							newestFileWithFormat = children[i];
						} else {
							if(children[i].lastModified() > newestFileWithFormat.lastModified()) {
								newestFileWithFormat = children[i];
							}
						}
					}
				}
				if(newestFileWithFormat != null) {
					downloadFile = newestFileWithFormat;
				} else {
					response.getWriter().write("Downloadable files exist, but none were found for certification edition " + edition + " and format " + format);
					return;
				}
			}
		}
		
		logger.info("Downloading " + downloadFile.getName());
		
		FileInputStream inputStream = new FileInputStream(downloadFile);

		// set content attributes for the response
		response.setContentType("application/xml");
		response.setContentLength((int) downloadFile.length());
	 
		// set headers for the response
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
		response.setHeader(headerKey, headerValue);
	 
		// get output stream of the response
		OutputStream outStream = response.getOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead = -1;
	 
		// write bytes read from the input stream into the output stream
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}
	 
		inputStream.close();
		outStream.close();
	}
	
	@ApiOperation(value="Search the CHPL", 
			notes="If paging parameters are not specified, the first 20 records are returned by default. "
			+ "All parameters are optional. "
			+ "Note that Retired products are not included by default, so to see 2011 products (all of which are retired) "
			+ "both the certificationEditions and certificationStatuses parameters must be provided - ex: "
			+ "?certificationEditions=2011&certificationStatuses=Retired ."
			+ "Any parameter that can accept multiple things (i.e. certificationStatuses) expects a comma-delimited list of those things (i.e. certificationStatuses=Active,Suspended). "
			+ "Date parameters are required to be in the format " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT + ". ")
	 @ApiImplicitParams({
		    @ApiImplicitParam(name = "searchTerm", value = "CHPL ID, Developer Name, Product Name, ONC-ACB Certification ID", required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "certificationStatuses", value = "A comma-separated list of certification statuses "
		    		+ "(ex: \"Active,Retired,Withdrawn by Developer\")). Retired listings are excluded unless requested with this parameter."
				      + " Defaults to \"Active,Suspended by ONC, Suspended by ONC-ACB\"", required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "certificationEditions", value = "A comma-separated list of certification editions to be 'or'ed together (ex: \"2014,2015\" finds listings with either edition 2014 or 2015)."
				      + " Defaults to \"2014,2015\"", required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "certificationCriteria", value = "A comma-separated list of certification criteria to be 'or'ed together (ex: \"170.314 (a)(1),170.314 (a)(2)\" finds listings attesting to either 170.314 (a)(1) or 170.314 (a(2)).", 
		    	required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "cqms", value = "A comma-separated list of cqms to be 'or'ed together (ex: \"CMS2,CMS9\" finds listings with either CMS2 or CMS9).", 
		    	required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "certificationBodies", value = "A comma-separated list of certification body names to be 'or'ed together (ex: \"Drummond,ICSA\" finds listings belonging to either Drummond or ICSA).", 
		    	required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "surveillance", value = "A comma-separated list of surveillance options to be 'or'ed together "
		    	+ "(ex: \"OPEN_SURVEILLANCE,CLOSED_SURVEILLANCE,OPEN_NONCONFORMITY,CLOSED_NONCONFORMITY\" finds listings that have either open or closed surveillance with open or closed nonconformities).", 
	    		required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "hasHadSurveillance", value = "True or False if a listing has ever had surveillance.", 
	    		required = false, dataType = "boolean", paramType = "query"),
		    @ApiImplicitParam(name = "developer", value = "The full name of a developer.", 
	    		required = false, dataType = "string", paramType = "query"),		
		    @ApiImplicitParam(name = "product", value = "The full name of a product.", 
    			required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "version", value = "The full name of a version.", 
				required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "practiceType", value = "A practice type (either Ambulatory or Inpatient). Valid only for 2014 listings.", 
				required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "certificationDateStart", value = "To return only listings certified after this date. Required format is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT, 
				required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "certificationDateEnd", value = "To return only listings certified before this date. Required format is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT, 
				required = false, dataType = "string", paramType = "query"),	
		    @ApiImplicitParam(name = "pageNumber", value = "Zero-based page number used in concert with pageSize. Defaults to 0.", 
				required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "pageSize", value = "Number of results to return used in concert with pageNumber. Defaults to 20.", 
				required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "orderBy", value = "What to order by. Options are one of the following: " 
		    	+ "developer, product, version, certificationEdition, productClassification, certificationBody, "
		    	+ "certificationDate, or practiceType. Defaults to product.",
				required = false, dataType = "string", paramType = "query"),
		    @ApiImplicitParam(name = "sortDescending", value = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
				required = false, dataType = "boolean", paramType = "query")		    
		  })
	@RequestMapping(value="/search", method=RequestMethod.GET,
			produces={"application/json; charset=utf-8", "application/xml"})
	public @ResponseBody SearchResponse simpleSearch(
			@RequestParam(value = "searchTerm", required=false, defaultValue="") String searchTerm, 
			@RequestParam(value = "certificationStatuses", required = false, defaultValue="Active,Suspended by ONC,Suspended by ONC-ACB") String certificationStatusesDelimited,
			@RequestParam(value="certificationEditions", required=false, defaultValue="2014,2015") String certificationEditionsDelimited,
			@RequestParam(value="certificationCriteria", required=false, defaultValue="") String certificationCriteriaDelimited,
			@RequestParam(value="cqms", required=false, defaultValue="") String cqmsDelimited,
			@RequestParam(value="certificationBodies", required=false, defaultValue="") String certificationBodiesDelimited,
			@RequestParam(value="surveillance", required=false, defaultValue="") String surveillanceDelimited,
			@RequestParam(value="hasHadSurveillance", required=false) Boolean hasHadSurveillance,
			@RequestParam(value="developer", required=false, defaultValue="") String developer,
			@RequestParam(value="product", required=false, defaultValue="") String product,
			@RequestParam(value="version", required=false, defaultValue="") String version,
			@RequestParam(value="practiceType", required=false, defaultValue="") String practiceType,
			@RequestParam(value = "certificationDateStart", required = false, defaultValue="") String certificationDateStart,
			@RequestParam(value = "certificationDateEnd", required = false, defaultValue="") String certificationDateEnd,
			@RequestParam(value = "pageNumber", required = false, defaultValue="0") Integer pageNumber, 
			@RequestParam(value = "pageSize", required = false, defaultValue="20") Integer pageSize,
			@RequestParam(value = "orderBy", required = false, defaultValue="product") String orderBy,
			@RequestParam(value = "sortDescending", required = false, defaultValue="false") Boolean sortDescending
			) throws InvalidArgumentsException, EntityRetrievalException {
		
		SearchRequest searchRequest = new SearchRequest();
		if(searchTerm != null) {
			searchRequest.setSearchTerm(searchTerm.trim());
		}
		
		if(certificationStatusesDelimited != null) {
			certificationStatusesDelimited = certificationStatusesDelimited.trim();
			if(!StringUtils.isEmpty(certificationStatusesDelimited)) {
				String[] certificationStatusArr = certificationStatusesDelimited.split(",");
				if(certificationStatusArr != null && certificationStatusArr.length > 0) {
					List<String> certificationStatuses = new ArrayList<String>();
					Set<KeyValueModel> availableCertificationStatuses = searchMenuManager.getCertificationStatuses();
					
					for(int i = 0; i < certificationStatusArr.length; i++) {
						String certStatusParam = certificationStatusArr[i].trim();
						boolean found = false;
						for(KeyValueModel currAvailableCertStatus : availableCertificationStatuses) {
							if(currAvailableCertStatus.getName().equalsIgnoreCase(certStatusParam)) {
								found = true;
							}
						}
						if(!found) {
							logger.error("Could not find certification status with value " + certStatusParam);
							throw new InvalidArgumentsException("Could not find certification status with value " + certStatusParam);
						} else {
							certificationStatuses.add(certStatusParam);
						}
					}
					searchRequest.setCertificationStatuses(certificationStatuses);
				}
			}
		}
		
		if(certificationEditionsDelimited != null) {
			certificationEditionsDelimited = certificationEditionsDelimited.trim();
			if(!StringUtils.isEmpty(certificationEditionsDelimited)) {
				String[] certificationEditionsArr = certificationEditionsDelimited.split(",");
				if(certificationEditionsArr != null && certificationEditionsArr.length > 0) {
					List<String> certificationEditions = new ArrayList<String>();
					Set<KeyValueModel> availableCertificationEditions = searchMenuManager.getEditionNames(false);
					
					for(int i = 0; i < certificationEditionsArr.length; i++) {
						String certEditionParam = certificationEditionsArr[i].trim();
						boolean found = false;
						for(KeyValueModel currAvailableEdition : availableCertificationEditions) {
							if(currAvailableEdition.getName().equalsIgnoreCase(certEditionParam)) {
								found = true;
							}
						}
						if(!found) {
							logger.error("Could not find certification edition with value " + certEditionParam);
							throw new InvalidArgumentsException("Could not find certification edition with value " + certEditionParam);
						} else {
							certificationEditions.add(certEditionParam);
						}
					}
					
					searchRequest.setCertificationEditions(certificationEditions);
				}
			}
		}
		
		if(certificationCriteriaDelimited != null) {
			certificationCriteriaDelimited = certificationCriteriaDelimited.trim();
			if(!StringUtils.isEmpty(certificationCriteriaDelimited)) {
				String[] certificationCriteriaArr = certificationCriteriaDelimited.split(",");
				if(certificationCriteriaArr != null && certificationCriteriaArr.length > 0) {
					List<String> certificationCriterion = new ArrayList<String>();
					Set<DescriptiveModel> availableCriterion = searchMenuManager.getCertificationCriterionNumbers(false);
					
					for(int i = 0; i < certificationCriteriaArr.length; i++) {
						String certCriteriaParam = certificationCriteriaArr[i].trim();
						boolean found = false;
						for(DescriptiveModel currAvailableCriteria : availableCriterion) {
							if(currAvailableCriteria.getName().equalsIgnoreCase(certCriteriaParam)) {
								found = true;
							}
						}
						if(!found) {
							logger.error("Could not find certification criterion with value " + certCriteriaParam);
							throw new InvalidArgumentsException("Could not find certification criterion with value " + certCriteriaParam);
						} else {
							certificationCriterion.add(certCriteriaParam);
						}
					}
					searchRequest.setCertificationCriteria(certificationCriterion);
				}
			}
		}
		
		if(cqmsDelimited != null) {
			cqmsDelimited = cqmsDelimited.trim();
			if(!StringUtils.isEmpty(cqmsDelimited)) {
				String[] cqmsArr = cqmsDelimited.split(",");
				if(cqmsArr != null && cqmsArr.length > 0) {
					List<String> cqms = new ArrayList<String>();
					Set<DescriptiveModel> availableCqms = searchMenuManager.getCQMCriterionNumbers(false);
					
					for(int i = 0; i < cqmsArr.length; i++) {
						String cqmParam = cqmsArr[i].trim();
						boolean found = false;
						for(DescriptiveModel currAvailableCqm : availableCqms) {
							if(currAvailableCqm.getName().equalsIgnoreCase(cqmParam)) {
								found = true;
							}
						}
						if(!found) {
							logger.error("Could not find CQM with value " + cqmParam);
							throw new InvalidArgumentsException("Could not find CQM with value " + cqmParam);
						} else {
							cqms.add(cqmParam.trim());
						}
					}
					searchRequest.setCqms(cqms);
				}
			}
		}
		
		if(certificationBodiesDelimited != null) {
			certificationBodiesDelimited = certificationBodiesDelimited.trim();
			if(!StringUtils.isEmpty(certificationBodiesDelimited)) {
				String[] certificationBodiesArr = certificationBodiesDelimited.split(",");
				if(certificationBodiesArr != null && certificationBodiesArr.length > 0) {
					List<String> certBodies = new ArrayList<String>();
					Set<KeyValueModel> availableCertBodies = searchMenuManager.getCertBodyNames(true);
					
					for(int i = 0; i < certificationBodiesArr.length; i++) {
						String certBodyParam = certificationBodiesArr[i].trim();
						boolean found = false;
						for(KeyValueModel currAvailableCertBody : availableCertBodies) {
							if(currAvailableCertBody.getName().equalsIgnoreCase(certBodyParam)) {
								found = true;
							}
						}
						if(!found) {
							logger.error("Could not find certification body with value " + certBodyParam);
							throw new InvalidArgumentsException("Could not find certification body with value " + certBodyParam);
						} else {
							certBodies.add(certBodyParam);
						}
					}
					searchRequest.setCertificationBodies(certBodies);
				}
			}
		}
		
		if(surveillanceDelimited != null) {
			surveillanceDelimited = surveillanceDelimited.trim();
			if(!StringUtils.isEmpty(surveillanceDelimited)) {
				String[] surveillanceArr = surveillanceDelimited.split(",");
				if(surveillanceArr != null && surveillanceArr.length > 0) {
					Set<SurveillanceSearchOptions> surveillanceSearchOptions = new HashSet<SurveillanceSearchOptions>();
					for(int i = 0; i < surveillanceArr.length; i++) {
						String surveillanceParam = surveillanceArr[i].trim();
						try {
							SurveillanceSearchOptions searchOpt = SurveillanceSearchOptions.valueOf(surveillanceParam);
							if(searchOpt != null) {
								surveillanceSearchOptions.add(searchOpt);
							} else {
								logger.error("No surveillance search option for the string " + surveillanceParam);
								throw new InvalidArgumentsException("No surveillance search option matches " + surveillanceParam);
							}
						} catch(Exception ex) {
							logger.error("No surveillance search option for the string " + surveillanceParam, ex);
							throw new InvalidArgumentsException("No surveillance search option matches " + surveillanceParam);
						}
					}
					searchRequest.setSurveillance(surveillanceSearchOptions);
				}
			}
		}
		
		searchRequest.setHasHadSurveillance(hasHadSurveillance);

		if(developer != null) {
			developer = developer.trim();
			if(!StringUtils.isEmpty(developer)) {
				searchRequest.setDeveloper(developer.trim());
			}
		}
		
		if(product != null) {
			product = product.trim();
			if(!StringUtils.isEmpty(product)) {
				searchRequest.setProduct(product.trim());
			}
		}
		
		if(version != null) {
			version = version.trim();
			if(!StringUtils.isEmpty(version)) {
				searchRequest.setVersion(version.trim());
			}
		}
		
		if(practiceType != null) {
			practiceType = practiceType.trim();
			if(!StringUtils.isEmpty(practiceType)) {
				Set<KeyValueModel> availablePracticeTypes = searchMenuManager.getPracticeTypeNames();
				boolean found = false;
				for(KeyValueModel currAvailablePracticeType : availablePracticeTypes) {
					if(currAvailablePracticeType.getName().equalsIgnoreCase(practiceType)) {
						found = true;
					}
				}
				
				if(!found) {
					logger.error("No practice type exists with name " + practiceType);
					throw new InvalidArgumentsException("No practice type exists with name " + practiceType);
				} else {
					searchRequest.setPracticeType(practiceType);
				}
			}
		}
		
		//check date formats
		SimpleDateFormat format = new SimpleDateFormat(SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
		if(certificationDateStart != null) {
			certificationDateStart = certificationDateStart.trim();
			if(!StringUtils.isEmpty(certificationDateStart)) {
				try {
					format.parse(certificationDateStart);
				} catch(ParseException ex) {
					logger.error("Could not parse " + certificationDateStart + " as date in the format " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
					throw new InvalidArgumentsException("Certification Date format expected is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT + " Cannot parse " + certificationDateStart);
				}
				searchRequest.setCertificationDateStart(certificationDateStart);
			}
		}
		
		if(certificationDateEnd != null) {
			certificationDateEnd = certificationDateEnd.trim();
			if(!StringUtils.isEmpty(certificationDateEnd)) {
				try {
					format.parse(certificationDateEnd);
				} catch(ParseException ex) {
					logger.error("Could not parse " + certificationDateEnd + " as date in the format " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
					throw new InvalidArgumentsException("Certification Date format expected is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT + " Cannot parse " + certificationDateEnd);
				}
				searchRequest.setCertificationDateEnd(certificationDateEnd);
			}
		}

		searchRequest.setPageNumber(pageNumber);
		searchRequest.setPageSize(pageSize);
		searchRequest.setOrderBy(orderBy.trim());
		searchRequest.setSortDescending(sortDescending);
		
		return certifiedProductSearchManager.search(searchRequest);
		
	}
	
	@ApiOperation(value="Advanced search for the CHPL", 
			notes="Search the CHPL by specifycing multiple fields of the data to search. "
					+ "If paging fields are not specified, the first 20 records are returned by default.")
	@RequestMapping(value="/search", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchResponse advancedSearch(
			@RequestBody SearchRequest searchFilters) throws InvalidArgumentsException {
		//check date params for format
		SimpleDateFormat format = new SimpleDateFormat(SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
		
		if(!StringUtils.isEmpty(searchFilters.getCertificationDateStart())) {
			try {
				format.parse(searchFilters.getCertificationDateStart());
			} catch(ParseException ex) {
				logger.error("Could not parse " + searchFilters.getCertificationDateStart() + " as date in the format " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
				throw new InvalidArgumentsException("Certification Date format expected is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT + " Cannot parse " + searchFilters.getCertificationDateStart());
			}
		}
		if(!StringUtils.isEmpty(searchFilters.getCertificationDateEnd())) {
			try {
				format.parse(searchFilters.getCertificationDateEnd());
			} catch(ParseException ex) {
				logger.error("Could not parse " + searchFilters.getCertificationDateEnd() + " as date in the format " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
				throw new InvalidArgumentsException("Certification Date format expected is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT + " Cannot parse " + searchFilters.getCertificationDateEnd());
			}
		}
		
		return certifiedProductSearchManager.search(searchFilters);
	}
	
	@Secured({Authority.ROLE_ADMIN, Authority.ROLE_ACB_ADMIN})
	@ApiOperation(value="Get all possible types of notifications that a user can sign up for.")
	@RequestMapping(value="/data/notification_types", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<DescriptiveModel> getNotificationTypes() {
		return searchMenuManager.getNotificationTypes();
	}
	
	@ApiOperation(value="Get all possible classifications in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/classification_types", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getClassificationNames() {
		return searchMenuManager.getClassificationNames();
	}
	
	@ApiOperation(value="Get all possible certificaiton editions in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/certification_editions", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getEditionNames() {
		return searchMenuManager.getEditionNames(false);
	}
	
	@ApiOperation(value="Get all possible certification statuses in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/certification_statuses", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getCertificationStatuses() {
		return searchMenuManager.getCertificationStatuses();
	}
	
	@ApiOperation(value="Get all possible practice types in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/practice_types", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getPracticeTypeNames() {
		return searchMenuManager.getPracticeTypeNames();
	}
	
	@ApiOperation(value="Get all possible product names in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/products", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModelStatuses> getProductNames() {
		return searchMenuManager.getProductNames();
	}
	
	@ApiOperation(value="Get all possible developer names in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/developers", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModelStatuses> getDeveloperNames() {
		return searchMenuManager.getDeveloperNames();
	}
	
	@ApiOperation(value="Get all possible ACBs in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/certification_bodies", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getCertBodyNames() {
		return searchMenuManager.getCertBodyNames(false);
	}
	
	@ApiOperation(value="Get all possible education types in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/education_types", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchOption getEducationTypes() {
		Set<KeyValueModel> data = searchMenuManager.getEducationTypes();
		SearchOption result = new SearchOption();
		result.setExpandable(false);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible test participant age ranges in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/age_ranges", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchOption getAgeRanges() {
		Set<KeyValueModel> data = searchMenuManager.getAgeRanges();
		SearchOption result = new SearchOption();
		result.setExpandable(false);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible test functionality options in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/test_functionality", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchOption getTestFunctionality() {
		Set<KeyValueModel> data = searchMenuManager.getTestFunctionality();
		SearchOption result = new SearchOption();
		result.setExpandable(false);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible test tool options in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/test_tools", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchOption getTestTools() {
		Set<KeyValueModel> data = searchMenuManager.getTestTools();
		SearchOption result = new SearchOption();
		result.setExpandable(false);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible test standard options in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/test_standards", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchOption getTestStandards() {
		Set<KeyValueModel> data = searchMenuManager.getTestStandards();
		SearchOption result = new SearchOption();
		result.setExpandable(true);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible qms standard options in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/qms_standards", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchOption getQmsStandards() {
		Set<KeyValueModel> data = searchMenuManager.getQmsStandards();
		SearchOption result = new SearchOption();
		result.setExpandable(true);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible targeted user options in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/targeted_users", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchOption getTargetedUsers() {
		Set<KeyValueModel> data = searchMenuManager.getTargetedUesrs();
		SearchOption result = new SearchOption();
		result.setExpandable(true);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible UCD process options in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/ucd_processes", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchOption getUcdProcesses() {
		Set<KeyValueModel> data = searchMenuManager.getUcdProcesses();
		SearchOption result = new SearchOption();
		result.setExpandable(true);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible accessibility standard options in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/accessibility_standards", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchOption getAccessibilityStandards() {
		Set<KeyValueModel> data = searchMenuManager.getAccessibilityStandards();
		SearchOption result = new SearchOption();
		result.setExpandable(true);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible macra measure options in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/macra_measures", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchOption getMacraMeasures() {
		Set<CriteriaSpecificDescriptiveModel> data = searchMenuManager.getMacraMeasures();
		SearchOption result = new SearchOption();
		result.setExpandable(false);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible developer status options in the CHPL")
	@RequestMapping(value="/data/developer_statuses", method=RequestMethod.GET,
				produces = "application/json; charset=utf-8")
	public @ResponseBody SearchOption getDeveloperStatuses() {
		Set<KeyValueModel> data = searchMenuManager.getDeveloperStatuses();
		SearchOption result = new SearchOption();
		result.setExpandable(false);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible surveillance type options in the CHPL")
	@RequestMapping(value="/data/surveillance_types", method=RequestMethod.GET,
				produces = "application/json; charset=utf-8")
	public @ResponseBody SearchOption getSurveillanceTypes() {
		Set<KeyValueModel> data = searchMenuManager.getSurveillanceTypes();
		SearchOption result = new SearchOption();
		result.setExpandable(false);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible surveillance result type options in the CHPL")
	@RequestMapping(value="/data/surveillance_result_types", method=RequestMethod.GET,
				produces = "application/json; charset=utf-8")
	public @ResponseBody SearchOption getSurveillanceResultTypes() {
		Set<KeyValueModel> data = searchMenuManager.getSurveillanceResultTypes();
		SearchOption result = new SearchOption();
		result.setExpandable(false);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible surveillance requirement type options in the CHPL")
	@RequestMapping(value="/data/surveillance_requirement_types", method=RequestMethod.GET,
				produces = "application/json; charset=utf-8")
	public @ResponseBody SearchOption getSurveillanceRequirementTypes() {
		Set<KeyValueModel> data = searchMenuManager.getSurveillanceRequirementTypes();
		SearchOption result = new SearchOption();
		result.setExpandable(false);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible surveillance requirement options in the CHPL")
	@RequestMapping(value="/data/surveillance_requirements", method=RequestMethod.GET,
				produces = "application/json; charset=utf-8")
	public @ResponseBody SurveillanceRequirementOptions getSurveillanceRequirementOptions() {
		SurveillanceRequirementOptions data = searchMenuManager.getSurveillanceRequirementOptions();
		return data;
	}
	
	@ApiOperation(value="Get all possible nonconformity status type options in the CHPL")
	@RequestMapping(value="/data/nonconformity_status_types", method=RequestMethod.GET,
				produces = "application/json; charset=utf-8")
	public @ResponseBody SearchOption getNonconformityStatusTypes() {
		Set<KeyValueModel> data = searchMenuManager.getNonconformityStatusTypes();
		SearchOption result = new SearchOption();
		result.setExpandable(false);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all possible nonconformity type options in the CHPL")
	@RequestMapping(value="/data/nonconformity_types", method=RequestMethod.GET,
				produces = "application/json; charset=utf-8")
	public @ResponseBody SearchOption getNonconformityTypeOptions() {
		Set<KeyValueModel> data = searchMenuManager.getNonconformityTypeOptions();
		SearchOption result = new SearchOption();
		result.setExpandable(false);
		result.setData(data);
		return result;
	}
	
	@ApiOperation(value="Get all search options in the CHPL", 
			notes="This returns all of the other /data/{something} results in one single response.")
	@RequestMapping(value="/data/search_options", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody PopulateSearchOptions getPopulateSearchData(
			@RequestParam(value = "simple", required = false) Boolean simple,
			@RequestParam(value = "showDeleted", required = false, defaultValue="false") Boolean showDeleted
			) throws EntityRetrievalException {
		if (simple == null){
			simple = false;
		}
		
		PopulateSearchOptions searchOptions = new PopulateSearchOptions();
		searchOptions.setCertBodyNames(searchMenuManager.getCertBodyNames(showDeleted));
		searchOptions.setEditions(searchMenuManager.getEditionNames(simple));
		searchOptions.setCertificationStatuses(searchMenuManager.getCertificationStatuses());
		searchOptions.setPracticeTypeNames(searchMenuManager.getPracticeTypeNames());
		searchOptions.setProductClassifications(searchMenuManager.getClassificationNames());
		searchOptions.setProductNames(searchMenuManager.getProductNames());
		searchOptions.setDeveloperNames(searchMenuManager.getDeveloperNames());
		searchOptions.setCqmCriterionNumbers(searchMenuManager.getCQMCriterionNumbers(simple));
		searchOptions.setCertificationCriterionNumbers(searchMenuManager.getCertificationCriterionNumbers(simple));
		
		return searchOptions;
	}
	
	@ApiOperation(value="Get all developer decertifications in the CHPL", 
			notes="This returns all decertified developers.")
	@RequestMapping(value="/decertifications/developers", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody DecertifiedDeveloperResults getDecertifiedDevelopers() throws EntityRetrievalException {
		DecertifiedDeveloperResults ddr = developerManager.getDecertifiedDevelopers();
		return ddr;
	}
	
	@ApiOperation(value="Get all decertified certified products in the CHPL", 
			notes="Returns all decertified certified products, their decertified statuses, and the total count of decertified certified products as the recordCount.")
	@RequestMapping(value="/decertifications/certified_products", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchResponse getDecertifiedCertifiedProducts (
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber, 
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "orderBy", required = false) String orderBy,
			@RequestParam(value = "sortDescending", required = false) Boolean sortDescending) throws EntityRetrievalException {
		SearchResponse resp = new SearchResponse();
		
		if (pageNumber == null){
			pageNumber = 0;
		}
		
		SearchRequest searchRequest = new SearchRequest();
		List<String> allowedCertificationStatuses = new ArrayList<String>();
		allowedCertificationStatuses.add(String.valueOf(CertificationStatusType.WithdrawnByAcb));
		allowedCertificationStatuses.add(String.valueOf(CertificationStatusType.WithdrawnByDeveloperUnderReview));
		allowedCertificationStatuses.add(String.valueOf(CertificationStatusType.TerminatedByOnc));
		
		searchRequest.setCertificationStatuses(allowedCertificationStatuses);
		
		searchRequest.setPageNumber(pageNumber);
		if(pageSize == null){
			searchRequest.setPageSize(certifiedProductSearchResultDao.countMultiFilterSearchResults(searchRequest).intValue());
		}
		
		if (orderBy != null){
			searchRequest.setOrderBy(orderBy);
		}
		
		if (sortDescending != null){
			searchRequest.setSortDescending(sortDescending);
		}
		
		resp = certifiedProductSearchManager.search(searchRequest);
		
		return resp;
	}
	
	@ApiOperation(value="Get decertified certified products in the CHPL with inactive certificates", 
			notes="Returns only decertified certified products with inactive certificates. "
					+ "Includes their decertified statuses and the total count of decertified certified products as the recordCount.")
	@RequestMapping(value="/decertifications/inactive_certificates", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchResponse getDecertifiedInactiveCertificateCertifiedProducts (
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber, 
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "orderBy", required = false) String orderBy,
			@RequestParam(value = "sortDescending", required = false) Boolean sortDescending) throws EntityRetrievalException {
		SearchResponse resp = new SearchResponse();
		
		if (pageNumber == null){
			pageNumber = 0;
		}
		
		SearchRequest searchRequest = new SearchRequest();
		List<String> allowedCertificationStatuses = new ArrayList<String>();
		allowedCertificationStatuses.add(String.valueOf(CertificationStatusType.WithdrawnByDeveloper));
		
		searchRequest.setCertificationStatuses(allowedCertificationStatuses);
		
		searchRequest.setPageNumber(pageNumber);
		if(pageSize == null){
			searchRequest.setPageSize(certifiedProductSearchResultDao.countMultiFilterSearchResults(searchRequest).intValue());
		}
		
		if (orderBy != null){
			searchRequest.setOrderBy(orderBy);
		}
		
		if (sortDescending != null){
			searchRequest.setSortDescending(sortDescending);
		}
		
		resp = certifiedProductSearchManager.search(searchRequest);
		
		return resp;
	}
	
	private List<Field> getAllInheritedFields(Class clazz, List<Field> fields) {
	    Class superClazz = clazz.getSuperclass();
	    if(superClazz != null){
	    	getAllInheritedFields(superClazz, fields);
	    }
	    fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
	    return fields;
	}
}
