package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.AdditionalSoftware;
import gov.healthit.chpl.manager.AdditionalSoftwareManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;

@RestController
@RequestMapping("/additional_software")
public class AdditionalSoftwareController {
	
	@Autowired
	AdditionalSoftwareManager additionalSoftwareManager;
	
	
	@RequestMapping(value="/add_certified_product_self", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody String associateAdditionalSoftwareCerifiedProductSelf(
			@RequestParam Long additionalSoftwareId,
			@RequestParam Long certifiedProductId) throws EntityRetrievalException
	{
		additionalSoftwareManager.associateAdditionalSoftwareCertifiedProductSelf(additionalSoftwareId, certifiedProductId);
		return "{\"success\" : true }";
	}
	
	
	@RequestMapping(value="/add_additional_software_certification_result_mapping", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody String addAdditionalSoftwareCertificationResultMapping(@RequestParam Long additionalSoftwareId, Long certificationResultId) throws EntityCreationException
	{
		additionalSoftwareManager.addAdditionalSoftwareCertificationResultMapping(additionalSoftwareId, certificationResultId);
		return "{\"success\" : true }";
	}
	
	
	@RequestMapping(value="/delete_additional_software_certification_result_mapping", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody String deleteAdditionalSoftwareCertificationResultMapping(@RequestParam Long additionalSoftwareId, Long certificationResultId) throws EntityCreationException
	{
		additionalSoftwareManager.deleteAdditionalSoftwareCertificationResultMapping(additionalSoftwareId, certificationResultId);
		return "{\"deleted\" : true }";
	}
	
	
	@RequestMapping(value="/by_certification_result/{id}", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public List<AdditionalSoftware> getAddtionalSoftwareForCertificationResult(@PathVariable("id") Long id) throws JsonParseException, IOException, EntityRetrievalException
	{
		return additionalSoftwareManager.getAdditionalSoftwareByCertificationResultId(id);
	}
	
	
}
