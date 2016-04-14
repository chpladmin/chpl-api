package gov.healthit.chpl.web.controller;

import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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

import gov.healthit.chpl.certificationId.Validator2014;
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

		Map<String, Long> criteriaMet = new HashMap<String, Long>();
		Map<String, Long> cqmsMet = new HashMap<String, Long>();
		Map<String, Long> domainsMet = new HashMap<String, Long>();
		TreeSet<String> years = new TreeSet<String>();
		List<CertificationIdResults.Product> resultProducts = new ArrayList<CertificationIdResults.Product>();
		for (CertifiedProductDetailsDTO dto : productDtos) {
			CertificationIdResults.Product p = new CertificationIdResults.Product(dto);
			resultProducts.add(p);

			// Collect the certification years
			years.add(dto.getYear());
			
			// Collect criteria met
			for (CertificationResultDetailsDTO certDetail : dto.getCertResults()) {
				if (certDetail.getSuccess()) {
					criteriaMet.put(certDetail.getNumber(), 1L);
				}
			}

			// Collect cqms met
			for (CQMResultDetailsDTO cqmDetail : dto.getCqmResults()) {
				if (cqmDetail.getSuccess()) {
					cqmsMet.put(cqmDetail.getCmsId(), 1L);
				}
			}
			
		}
		results.setProducts(resultProducts);

		
		// Calculate Attestation Year
//		String attYearString = calculateAttestationYear(years);

		Validator2014 validator = new Validator2014(criteriaMet, cqmsMet, domainsMet);
		results.setIsValid(validator.validate());
		
		// TODO: Calculate percentages met (add call)
		Map<String, Integer> percents = new HashMap<String, Integer>();
		Map<String, Integer> counts = new HashMap<String, Integer>();

		results.setMetPercentages(validator.getPercents());
		results.setMetCounts(validator.getCounts());

		// Lookup CERT ID
		try {
			if (certProductIds.size() > 0) {
				CertificationIdDTO idDto = certificationIdManager.getByProductIds(certProductIds);
				if (null != idDto) {
					results.setEhrCertificationId(idDto.getCertificationId());
				} else {
					if (results.getIsValid()) {
						// TODO: Generate a new ID, store it, and return it
					}
				}
			}
		} catch (EntityRetrievalException ex) {
			ex.printStackTrace();
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

	private String calculateAttestationYear(TreeSet<String> years) {
		// Get the lowest year...
		String attYearString = years.first();

		// ...if there are two years then we have a hybrid
		if (years.size() > 0) {
			attYearString += "/" + years.last();
		}
		return attYearString;
	}

}