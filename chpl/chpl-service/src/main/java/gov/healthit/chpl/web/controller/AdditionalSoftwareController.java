package gov.healthit.chpl.web.controller;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.manager.AdditionalSoftwareManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/additional_software")
public class AdditionalSoftwareController {
	
	@Autowired
	AdditionalSoftwareManager additionalSoftwareManager;
	
	
	@RequestMapping(value="/add_certified_product_self", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody String associateAdditionalSoftwareCerifiedProductSelf(
			@RequestParam Long additionalSoftwareId,
			@RequestParam Long certifiedProductId)
	{
		additionalSoftwareManager.associateAdditionalSoftwareCerifiedProductSelf(additionalSoftwareId, certifiedProductId);
		return "{\"success\" : true }";
	}
	
	
	@RequestMapping(value="/add_additional_software_certification_result_mapping", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody String addAdditionalSoftwareCertificationResultMapping(@RequestParam Long additionalSoftwareId, Long certificationResultId)
	{
		additionalSoftwareManager.addAdditionalSoftwareCertificationResultMapping(additionalSoftwareId, certificationResultId);
		return "{\"success\" : true }";
	}
	
	
	@RequestMapping(value="/add_additional_software_cqm_result_mapping", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody String addAdditionalSoftwareCQMResultMapping(@RequestParam Long additionalSoftwareId, Long cqmResultId)
	{
		additionalSoftwareManager.addAdditionalSoftwareCQMResultMapping(additionalSoftwareId, cqmResultId);
		return "{\"success\" : true }";
	}	
	
	
	
}
