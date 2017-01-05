package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.certificationId.Validator;
import gov.healthit.chpl.certificationId.ValidatorFactory;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.dto.CQMMetDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.web.controller.results.CertificationIdLookupResults;
import gov.healthit.chpl.web.controller.results.CertificationIdResults;
import gov.healthit.chpl.web.controller.results.CertificationIdVerifyResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="certification-ids")
@RestController
@RequestMapping("/certification_ids")
@Aspect
public class CertificationIdController {

	@Autowired CertifiedProductManager certifiedProductManager;
	@Autowired CertificationIdManager certificationIdManager;

	//**********************************************************************************************************
	// getAll
	//
	// Mapping: / (Root)
	//
	// Retrieves all CMS Certification IDs and their date of creation.
	//**********************************************************************************************************
	@Secured({"ROLE_ADMIN", "ROLE_CMS_STAFF", "ROLE_ONC_STAFF"})
	@ApiOperation(value="Retrieves a list of all CMS EHR Certification IDs along with the date they were created.")
	@RequestMapping(value="/", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	public List<SimpleCertificationId> getAll() throws IOException {
		List<SimpleCertificationId> results = new ArrayList<SimpleCertificationId>();
		if(Util.isUserRoleAdmin() || Util.isUserRoleOncStaff()) {
			results = certificationIdManager.getAllWithProducts();
		} else {
			results = certificationIdManager.getAll();
		}
		
		return results;
	}


	//**********************************************************************************************************
	// searchCertificationId
	//
	// Mapping: /search
	// Params: List ids
	//
	// Retrieves a CMS EHR Certification ID for a collection of products.
	//**********************************************************************************************************
	@ApiOperation(value="Retrieves a CMS EHR Certification ID for a collection of products.",
			notes="Retrieves a CMS EHR Certification ID for a collection of products. Returns a list of basic product information, " 
			+ "Criteria and CQM calculations, and the associated CMS EHR Certification ID if one exists.")
	@RequestMapping(value="/search", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody CertificationIdResults searchCertificationId(@RequestParam(required=false) List<Long> ids) 
	throws InvalidArgumentsException, CertificationIdException {
		return this.findCertificationByProductIds(ids, false);
	}

	
	//**********************************************************************************************************
	// createCertificationId
	//
	// Mapping: /create
	// Params: List ids
	//
	// Creates a new CMS EHR Certification ID for a collection of products if one does not already exist.
	//**********************************************************************************************************
	@ApiOperation(value="Creates a new CMS EHR Certification ID for a collection of products if one does not already exist.",
			notes="Retrieves a CMS EHR Certification ID for a collection of products or creates a new one if one does not already exist. "
			+ "Returns a list of basic product information, " 
			+ "Criteria and CQM calculations, and the associated CMS EHR Certification ID if one exists.")
	@RequestMapping(value="/create", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces={MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody CertificationIdResults createCertificationId(@RequestParam(required=true) List<Long> ids) 
	throws InvalidArgumentsException, CertificationIdException {
		return this.findCertificationByProductIds(ids, true);
	}
	

	//**********************************************************************************************************
	// getCertificationId
	//
	// Mapping: /{certificationId}
	// Params: Boolean includeCriteria
	// Params: Boolean includeCqms
	//
	// Retrieves detailed information about a specific CMS EHR Certification ID including the list of products
	// that make it up.  It optionally retrieves the Certification Criteria and CQMs of the products
	// associated with the CMS EHR Certification ID.
	//**********************************************************************************************************
	@ApiOperation(value="Get information about a specific EHR Certification ID.", 
			notes="Retrieves detailed information about a specific EHR Certification ID including the list of products that make it up.")
	@RequestMapping(value="/{certificationId}", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody CertificationIdLookupResults getCertificationId(@PathVariable("certificationId") String certificationId, 
		@RequestParam(required=false,defaultValue="false") Boolean includeCriteria,
		@RequestParam(required=false,defaultValue="false") Boolean includeCqms) 
	throws InvalidArgumentsException, CertificationIdException {
		return this.findCertificationIdByCertificationId(certificationId, includeCriteria, includeCqms);
	}


	//**********************************************************************************************************
	// verifyCertificationId
	//
	// Mapping: /verify (POST)
	//
	// Verify whether one or more specific EHR Certification ID is valid or not.
	//**********************************************************************************************************
	@ApiOperation(value="Verify whether one or more specific EHR Certification IDs are valid or not.", 
			notes="Returns a boolean value for each EHR Certification ID specified.")
	@RequestMapping(value="/verify", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces={MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody CertificationIdVerifyResults verifyCertificationId(@RequestBody CertificationIdVerificationBody body) 
	throws InvalidArgumentsException, CertificationIdException {
		return this.verifyCertificationIds(body.getIds());
	}
	
	
	//**********************************************************************************************************
	// verifyCertificationId
	//
	// Mapping: /verify (GET)
	// Params: List ids
	//
	// Verify whether one or more specific EHR Certification ID is valid or not.
	//**********************************************************************************************************
	@ApiOperation(value="Verify whether one or more specific EHR Certification IDs are valid or not.", 
			notes="Returns true or false for each EHR Certification ID specified.")
	@RequestMapping(value="/verify", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody CertificationIdVerifyResults verifyCertificationId(@RequestParam("ids") List<String> certificationIds) 
	throws InvalidArgumentsException, CertificationIdException {
		return this.verifyCertificationIds(certificationIds);
	}


	//**********************************************************************************************************
	// findCertificationIdByCertificationId
	//
	//**********************************************************************************************************
	private CertificationIdLookupResults findCertificationIdByCertificationId(String certificationId, 
	Boolean includeCriteria, Boolean includeCqms) throws InvalidArgumentsException, CertificationIdException {
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

				SortedSet<Integer> yearSet = new TreeSet<Integer>();
				List<Long> certProductIds = new ArrayList<Long>();

				// Add product data to results
				List<CertificationIdLookupResults.Product> productList = results.getProducts();
				for (CertifiedProductDetailsDTO dto : productDtos) {
					productList.add(new CertificationIdLookupResults.Product(dto));
					yearSet.add(new Integer(dto.getYear()));
					certProductIds.add(dto.getId());
				}

				// Add criteria and cqms met to results
				if (includeCriteria || includeCqms) {
					Validator validator = ValidatorFactory.getValidator(certDto.getYear());
					
					// Lookup Criteria for Validating
					List<String> criteriaDtos = certificationIdManager.getCriteriaNumbersMetByCertifiedProductIds(certProductIds);
					
					// Lookup CQMs for Validating
					List<CQMMetDTO> cqmDtos = certificationIdManager.getCqmsMetByCertifiedProductIds(certProductIds);

					boolean isValid = validator.validate(criteriaDtos, cqmDtos, new ArrayList<Integer>(yearSet));
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
	
	
	//**********************************************************************************************************
	// verifyCertificationIds
	//
	//**********************************************************************************************************
	private CertificationIdVerifyResults verifyCertificationIds(List<String> certificationIds) 
	throws InvalidArgumentsException, CertificationIdException {

		CertificationIdVerifyResults results = new CertificationIdVerifyResults();
		if (null != certificationIds) {
		
			try {
				Map<String, Boolean> lookupResults = certificationIdManager.verifyByCertificationId(certificationIds);

				// Put the IDs in the order that they were passed in
				for (String id : certificationIds) {
					results.getResults().add(new CertificationIdVerifyResults.VerifyResult(id, lookupResults.get(id)));
				}
				
			} catch (EntityRetrievalException e) {
				throw new CertificationIdException("Unable to verify EHR Certification IDs. Notify system administrator.");
			}
			
		} else {
			throw new InvalidArgumentsException("No EHR Certification IDs specified.");
		}
		
		return results;
	}

	
	//**********************************************************************************************************
	// findCertificationByProductIds
	//
	//**********************************************************************************************************
	private CertificationIdResults findCertificationByProductIds(List<Long> productIdList, Boolean create)
	throws InvalidArgumentsException, CertificationIdException {

		if (null == productIdList) {
			productIdList = new ArrayList<Long>();
		}
		
		List<CertifiedProductDetailsDTO> productDtos = new ArrayList<CertifiedProductDetailsDTO>();
		try {
			productDtos = certifiedProductManager.getDetailsByIds(productIdList);
		} catch (EntityRetrievalException ex) {
			ex.printStackTrace();				
		}

		// Add products to results
		CertificationIdResults results = new CertificationIdResults();
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
		
		// Lookup Criteria for Validating
		List<String> criteriaDtos = certificationIdManager.getCriteriaNumbersMetByCertifiedProductIds(productIdList);
		
		// Lookup CQMs for Validating
		List<CQMMetDTO> cqmDtos = certificationIdManager.getCqmsMetByCertifiedProductIds(productIdList);
		
		boolean isValid = validator.validate(criteriaDtos, cqmDtos, new ArrayList<Integer>(yearSet));
		results.setIsValid(isValid);
		results.setMetPercentages(validator.getPercents());
		results.setMetCounts(validator.getCounts());

		// Lookup CERT ID
		if (validator.isValid()) {
			CertificationIdDTO idDto = null;
			try {
				idDto = certificationIdManager.getByProductIds(productIdList, year);
				if (null != idDto) {
					results.setEhrCertificationId(idDto.getCertificationId());
				} else {
					if ((create) && (results.getIsValid())) {
						// Generate a new ID
						idDto = certificationIdManager.create(productIdList, year);
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
}