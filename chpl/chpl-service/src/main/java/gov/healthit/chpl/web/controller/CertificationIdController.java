package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
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
import gov.healthit.chpl.web.controller.results.CertificationIdLookupResults;
import gov.healthit.chpl.web.controller.results.CertificationIdResults;
import gov.healthit.chpl.web.controller.results.CertificationIdVerifyResults;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(value="certification-ids")
@RestController
@RequestMapping("/certification_ids")
public class CertificationIdController {

	@Autowired CertifiedProductManager certifiedProductManager;
	@Autowired CertificationIdManager certificationIdManager;

	@ApiOperation(value="Retrieves an EHR Certification ID for a collection of products.",
			notes="Calculates the details of a collection of products in order to retrieve an EHR Certification ID.")
	@RequestMapping(value="/", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody CertificationIdResults getCertificationId(@RequestParam(required=false) String products, 
		@RequestParam(required=false,defaultValue="false") Boolean create) 
		throws InvalidArgumentsException, CertificationIdException {

		// Make sure the value is formatted correctly (Ex: "123|234|345")
		if ((null != products) && (0 != products.trim().length())) {
			if (!Pattern.matches("[0-9]+(\\|[0-9]+)*", products)) {
				throw new InvalidArgumentsException("Invalid Product ID(s).  Product IDs are integers and must be separated by pipes.");
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
				throw new CertificationIdException("Unable to retrieve a Certification ID.");
			} catch (EntityCreationException ex) {
				throw new CertificationIdException("Unable to create a new Certification ID.");
			} catch (JsonProcessingException ex) {
				throw new CertificationIdException("Unable to create a new Certification ID.");
			}
		}
		
		return results;
	}

	@ApiOperation(value="Get information about a specific EHR Certification ID.", 
			notes="Retrieves detailed information about a specific EHR Certification ID including the list of products that make it up.")
	@RequestMapping(value="/{certificationId}", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody CertificationIdLookupResults getCertificationIdByCertificationId(@PathVariable("certificationId") String certificationId, 
		@RequestParam(required=false,defaultValue="false") Boolean includeCriteria,
		@RequestParam(required=false,defaultValue="false") Boolean includeCqms) 
	throws InvalidArgumentsException, CertificationIdException {
		
		CertificationIdLookupResults results = new CertificationIdLookupResults();
		
		try {
			// Lookup the Cert ID
			CertificationIdDTO certDto = certificationIdManager.getByCertificationId(certificationId);
			if (null != certDto) {
				results.setEhrCertificationId(certDto.getCertificationId());
				results.setYear(certDto.getYear());

				// Find the products associated with the Cert ID
				List<Long> productIds = certificationIdManager.getProductIdsById(certDto.getId());
				List<CertifiedProductDetailsDTO> productDtos = certifiedProductManager.getDetailsByIds(productIds);
				
				// Add product data to results
				List<CertificationIdLookupResults.Product> productList = results.getProducts();
				for (CertifiedProductDetailsDTO dto : productDtos) {
					productList.add(new CertificationIdLookupResults.Product(dto));
				}
				
				// Add criteria and cqms met to results
				if (includeCriteria || includeCqms) {
					Validator validator = ValidatorFactory.getValidator(certDto.getYear());
					boolean isValid = validator.validate(productDtos);
					if (isValid) {
						if (includeCriteria) {
							results.setCriteria(validator.getCriteriaMet().keySet());
						}
						if (includeCqms) {
							results.setCqms(validator.getCqmsMet().keySet());
						}
					}
				}
				
			}
			
		} catch (EntityRetrievalException ex) {
			throw new CertificationIdException("Unable to lookup Certification ID " + certificationId + ".");
		}
		
		return results;
	}

	@ApiOperation(value="Verify whether a specific EHR Certification ID is valid or not.", 
			notes="Returns true or false for each EHR Certification ID specified.")
	@RequestMapping(value="/verify", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces={MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody CertificationIdVerifyResults verifyCertificationId(@RequestBody CertificationIdVerificationBody body) 
	throws InvalidArgumentsException, CertificationIdException {

		CertificationIdVerifyResults results = new CertificationIdVerifyResults();
		if (null != body) {
		
			try {
				Map<String, Boolean> lookupResults = certificationIdManager.verifyByCertificationId(body.getIds());

				// Put the IDs in the order that they were passed in
				for (String id : body.getIds()) {
					results.getResults().add(new CertificationIdVerifyResults.VerifyResult(id, lookupResults.get(id)));
				}
				
			} catch (EntityRetrievalException e) {
				throw new CertificationIdException("Unable to verify EHR Certification IDs. Notify system administrator.");
			}
			
		} else {
			throw new InvalidArgumentsException("No EHR Certification IDs specified in request body.");
		}
		
		return results;
	}

}