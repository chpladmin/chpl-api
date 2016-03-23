package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.web.controller.results.CertificationIdResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="certificationids")
@RestController
@RequestMapping("/certificationids")
public class CertificationIdController {
	
	@Autowired ProductManager productManager;

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

		// Lookup specified products
		List<ProductDTO> productDtos = new ArrayList<ProductDTO>();
		if (null != products) {
			String[] productArray = products.trim().split("\\|");
			List<Long> productIdList = new ArrayList<Long>();
			if (productArray.length > 0) {
				for (String id : productArray) {
					if (null != id)
						productIdList.add(new Long(id));
				}
				productDtos = productManager.getByIds(productIdList);
			}
		}
		
		CertificationIdResults results = new CertificationIdResults();
		Map<String, Integer> percents = new HashMap<String, Integer>();
		Map<String, Integer> counts = new HashMap<String, Integer>();
		
		results.setMetPercentages(percents);
		results.setMetCounts(counts);
		
		percents.put("criteria", 10*productDtos.size());
		counts.put("criteria", 1);
		percents.put("cqmDomains", 100);
		counts.put("cqmDomains", 1);
		percents.put("cqmsAmbulatory", 100);
		counts.put("cqmsAmbulatory", 1);
		percents.put("cqmsInpatient", 100);
		counts.put("cqmsInpatient", 1);
		
		List<Product> resultProducts = new ArrayList<Product>();
		for (ProductDTO dto : productDtos) {
			resultProducts.add(new Product(dto));
		}
		results.setProducts(resultProducts);

		results.setEhrCertificationId("TESTCERTID" + productDtos.size());

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
