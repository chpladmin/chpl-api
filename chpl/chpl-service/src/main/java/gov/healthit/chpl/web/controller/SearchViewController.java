package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.SearchMenuManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Tag;

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
	
	
	private static final Logger logger = LogManager.getLogger(SearchViewController.class);

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
	public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {	
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
				File newestFile = null;
				for(int i = 0; i < children.length; i++) {
					if(newestFile == null) {
						newestFile = children[i];
					} else {
						if(children[i].lastModified() > newestFile.lastModified()) {
							newestFile = children[i];
						}
					}
				}
				if(newestFile != null) {
					downloadFile = newestFile;
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
			notes="If paging parameters are not specified, the first 20 records are returned by default.")
	@RequestMapping(value="/search", method=RequestMethod.GET,
			produces={"application/json; charset=utf-8", "application/xml"})
	public @ResponseBody SearchResponse simpleSearch(
			@RequestParam("searchTerm") String searchTerm, 
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber, 
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "orderBy", required = false) String orderBy,
			@RequestParam(value = "sortDescending", required = false) Boolean sortDescending
			) throws EntityRetrievalException {
		
		
		if (pageNumber == null){
			pageNumber = 0;
		}
		
		if (pageSize == null){
			pageSize = 20;
		}
		
		SearchRequest searchRequest = new SearchRequest();
		
		searchRequest.setSearchTerm(searchTerm);
		searchRequest.setPageNumber(pageNumber);
		searchRequest.setPageSize(pageSize);
		
		if (orderBy != null){
			searchRequest.setOrderBy(orderBy);
		}
		
		if (sortDescending != null){
			searchRequest.setSortDescending(sortDescending);
		}
		
		return certifiedProductSearchManager.search(searchRequest);
		
	}
	
	@ApiOperation(value="Advanced search for the CHPL", 
			notes="Search the CHPL by specifycing multiple fields of the data to search. "
					+ "If paging fields are not specified, the first 20 records are returned by default.")
	@RequestMapping(value="/search", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public @ResponseBody SearchResponse advancedSearch(
			@RequestBody SearchRequest searchFilters) {
		return certifiedProductSearchManager.search(searchFilters);
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
	public @ResponseBody Set<KeyValueModel> getProductNames() {
		return searchMenuManager.getProductNames();
	}
	
	@ApiOperation(value="Get all possible developer names in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/developers", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getDeveloperNames() {
		return searchMenuManager.getDeveloperNames();
	}
	
	@ApiOperation(value="Get all possible ACBs in the CHPL", 
			notes="This is useful for knowing what values one might possibly search for.")
	@RequestMapping(value="/data/certification_bodies", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Set<KeyValueModel> getCertBodyNames() {
		return searchMenuManager.getCertBodyNames();
	}
	
	@ApiOperation(value="Get all search options in the CHPL", 
			notes="This returns all of the other /data/{something} results in one single response.")
	@RequestMapping(value="/data/search_options", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody PopulateSearchOptions getPopulateSearchData(
			@RequestParam(value = "simple", required = false) Boolean simple
			) throws EntityRetrievalException {
		if (simple == null){
			simple = false;
		}
		return searchMenuManager.getPopulateSearchOptions(simple);
	}
	
}
