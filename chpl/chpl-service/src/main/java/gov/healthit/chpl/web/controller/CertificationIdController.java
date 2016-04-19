package gov.healthit.chpl.web.controller;

import java.util.Arrays;
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
@RequestMapping("/certificationids")
public class CertificationIdController {
	
	@Autowired CertifiedProductManager certifiedProductManager;
	@Autowired CertificationIdManager certificationIdManager;

	@ApiOperation(value="Retrieves an EHR Certification ID for a collection of products.",
			notes="Calculates the details of a collection of products in order to retrieve an EHR Certification ID.")
	@RequestMapping(value="/getCertificationId", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CertificationIdResults getCertificationId(@RequestParam(required=false) String products) 
		throws InvalidArgumentsException {

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
		SortedSet<Integer> editionYears = new TreeSet<Integer>();
		List<CertificationIdResults.Product> resultProducts = new ArrayList<CertificationIdResults.Product>();
		for (CertifiedProductDetailsDTO dto : productDtos) {
			CertificationIdResults.Product p = new CertificationIdResults.Product(dto);
			resultProducts.add(p);
			editionYears.add(new Integer(dto.getYear()));
		}
		results.setProducts(resultProducts);
		String attestationYear = Validator.calculateAttestationYear(editionYears);
		results.setYear(attestationYear);
			
		// Validate the collection
		Validator validator = ValidatorFactory.getValidator(attestationYear);
		boolean isValid = validator.validate(productDtos);
		results.setIsValid(isValid);
		results.setMetPercentages(validator.getPercents());
		results.setMetCounts(validator.getCounts());

		// Lookup CERT ID
		if (validator.isValid()) {
			try {
				CertificationIdDTO idDto = certificationIdManager.getByProductIds(certProductIds);
				if (null != idDto) {
					results.setEhrCertificationId(idDto.getCertificationId());
				} else {
					if (results.getIsValid()) {
						// TODO: Generate a new ID, store it, and return it
						String tempId = "XX14E";
						for (int i=0; i<10; ++i)
							tempId += "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(new Random().nextInt(36));
						results.setEhrCertificationId(tempId);
					}
				}
			} catch (EntityRetrievalException ex) {
				ex.printStackTrace();
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