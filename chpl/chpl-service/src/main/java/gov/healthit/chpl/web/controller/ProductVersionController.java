package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.UpdateVersionRequest;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.manager.ProductVersionManager;

@RestController
@RequestMapping("/product/version")
public class ProductVersionController {
	
	@Autowired
	ProductVersionManager pvManager;
	
	@RequestMapping(value="/get_version", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody ProductVersion getProductVersionById(@RequestParam(required=true) Long versionId) throws EntityRetrievalException {
		ProductVersionDTO version = pvManager.getById(versionId);
		
		ProductVersion result = null;
		if(version != null) {
			result = new ProductVersion(version);
		}
		return result;
	}
	
	@RequestMapping(value="/list_versions_by_product", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<ProductVersion> getVersionsByProduct(@RequestParam(required=true) Long productId) {
		List<ProductVersionDTO> versionList = pvManager.getByProduct(productId);		
		
		List<ProductVersion> versions = new ArrayList<ProductVersion>();
		if(versionList != null && versionList.size() > 0) {
			for(ProductVersionDTO dto : versionList) {
				ProductVersion result = new ProductVersion(dto);
				versions.add(result);
			}
		}
		return versions;
	}
	
	@RequestMapping(value="/update_version", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public ProductVersion updateVersion(@RequestBody(required=true) UpdateVersionRequest versionInfo) throws 
		EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
		ProductVersionDTO result = null;
		
		if(versionInfo == null || versionInfo.getVersionId() == null) {
			throw new InvalidArgumentsException("versionId must be provided in the request body.");
		}
		
		ProductVersionDTO toUpdate = new ProductVersionDTO();
		toUpdate.setId(versionInfo.getVersionId());
		toUpdate.setVersion(versionInfo.getVersion());
		result = pvManager.update(toUpdate);

		if(result == null) {
			throw new EntityCreationException("There was an error inserting or updating the version information.");
		}
		return new ProductVersion(result);
		
	}
}
