package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.web.controller.results.CertificationIdResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="certificationids")
@RestController
@RequestMapping("/certificationids")
public class CertificationIdController {
	
	@Autowired ProductManager productManager;
	@Autowired ProductVersionManager versionManager;

	@ApiOperation(value="Retrieves an EHR Certification ID for a collection of products.",
			notes="Calculates the details of a collection of products in order to retrieve an EHR Certification ID.")
	@RequestMapping(value="/getCertificationId", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertificationIdResults getCertificationId(@RequestParam(required=true) String products) 
		throws InvalidArgumentsException {
	
		// Make sure a value is provided
		if (null == products || 0 == products.length) {
			throw new InvalidArgumentsException("At least one product id must be provided in the request.");
		}

		// Make sure the value is formatted correctly (Ex: "123;234;345")
		if (!Pattern.matches("[0-9]+(\\;[0-9]+)*", products)) {
			throw new InvalidArgumentsException("Product ids are integers and must be separated by semi-colons.");
		}

		// Split into individual product ids
		String[] productArray = products.split(";").trim();
		if (null == productArray || 0 == productArray.length) {
			throw new InvalidArgumentsException("At least one product id must be provided in the request.");
		}

		// Lookup the certification id
		// TODO
		
		CertificationIdResults results = new CertificationIdResults();
		
		Map<String, Integer> percents = new HashMap<String, Integer>();
		Map<String, Integer> counts = new HashMap<String, Integer>();
		
		results.setMetPercentages(percents);
		results.setMetCounts(counts);
		
		percents.put("criteria", 100);
		counts.put("criteria", 1);
		
		results.setEhrCertificationId("TESTCERTID" + productArray);

		return results;
	}
	
	@ApiOperation(value="Get information about a specific EHR Certification ID.", 
			notes="Retrieves detailed information about a specific EHR Certification ID including the list of products that make it up.")
	@RequestMapping(value="/{certificationId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertificationIdResults getCertificationIdByCertificationId(@PathVariable("certificationId") String certificationId) {
		CertificationIdResults results = new CertificationIdResults();
		results.setEhrCertificationId(certificationId);
		return results;
	}
	
}
