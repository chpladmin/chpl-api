package gov.healthit.chpl.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.regex.Pattern;

 
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

import gov.healthit.chpl.certificationId.Validator;
import gov.healthit.chpl.certificationId.ValidatorFactory;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.web.controller.results.CertificationIdResults;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="certificationids")
@RestController
@RequestMapping("/certificationIds")
public class CertificationIdController {

	@Autowired CertifiedProductManager certifiedProductManager;
	@Autowired CertificationIdManager certificationIdManager;

	@ApiOperation(value="Retrieves an EHR Certification ID for a collection of products.",
			notes="Calculates the details of a collection of products in order to retrieve an EHR Certification ID.")
	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertificationIdResults getCertificationId(@RequestParam(required=false) String products, 
		@RequestParam(required=false,defaultValue="false") Boolean create) 
		throws InvalidArgumentsException, ValidationException {

		// Make sure the value is formatted correctly (Ex: "123|234|345")
		if ((null != products) && (0 != products.trim().length())) {
			if (!Pattern.matches("[0-9]+(\\|[0-9]+)*", products)) {
				throw new InvalidArgumentsException("Product ids are integers and must be separated by pipes.");
			}
		} else {
			products = null;
		}

		List<Long> certProductIds = new ArrayList<Long>();
		if (null != products) {
			String[] productArray = products.trim().split("\\|");
			for (String id : productArray) {
				if (null != id)
					certProductIds.add(new Long(id));
			}
		}
		
		CertificationIdResults results = new CertificationIdResults();

		// Lookup specified products
		List<CertifiedProductDetailsDTO> productDtos = new ArrayList<CertifiedProductDetailsDTO>();
		if (null != products) {
			try {
			String[] productArray = products.trim().split("\\|");
			List<Long> productIdList = new ArrayList<Long>();
			if (productArray.length > 0) {
				for (String id : productArray) {
					if (null != id)
						productIdList.add(new Long(id));
				}
				productDtos = certifiedProductManager.getDetailsByIds(productIdList);
			}
			} catch (EntityRetrievalException ex) {
				ex.printStackTrace();				
			}
		}

		// Add products to results
		SortedSet<Integer> yearSet = new TreeSet<Integer>();
		List<CertificationIdResults.Product> resultProducts = new ArrayList<CertificationIdResults.Product>();
		for (CertifiedProductDetailsDTO dto : productDtos) {
			CertificationIdResults.Product p = new CertificationIdResults.Product(dto);
			resultProducts.add(p);
			yearSet.add(new Integer(dto.getYear()));
		}
		results.setProducts(resultProducts);
		String year = Validator.calculateAttestationYear(yearSet);
		results.setYear(year);
			
		// Validate the collection
		Validator validator = ValidatorFactory.getValidator(year);
		boolean isValid = validator.validate(productDtos);
		results.setIsValid(isValid);
		results.setMetPercentages(validator.getPercents());
		results.setMetCounts(validator.getCounts());

		// Lookup CERT ID
		if (validator.isValid()) {
			CertificationIdDTO idDto = null;
			try {
				idDto = certificationIdManager.getByProductIds(certProductIds, year);
				if (null != idDto) {
					results.setEhrCertificationId(idDto.getCertificationId());
				} else {
					if ((create) && (results.getIsValid())) {
						// Generate a new ID
						idDto = certificationIdManager.create(certProductIds, year);
						results.setEhrCertificationId(idDto.getCertificationId());
					}
				}
			} catch (EntityRetrievalException ex) {
				throw new ValidationException("Unable to retrieve a Certification ID.");
			} catch (EntityCreationException ex) {
				throw new ValidationException("Unable to create a new Certification ID.");
			} catch (JsonProcessingException ex) {
				throw new ValidationException("Unable to create a new Certification ID.");
			}
		}
		
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