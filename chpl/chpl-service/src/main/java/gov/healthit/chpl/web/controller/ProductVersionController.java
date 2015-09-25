package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.UpdateVersionRequest;
import gov.healthit.chpl.domain.UpdateVersionsRequest;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;

@RestController
@RequestMapping("/product/version")
public class ProductVersionController {
	
	@Autowired
	ProductVersionManager pvManager;
	
	@Autowired 
	CertifiedProductManager cpManager;
	
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
	public ProductVersion updateVersion(@RequestBody(required=true) UpdateVersionsRequest versionInfo) throws 
		EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
		
		ProductVersionDTO result = null;
		
		if(versionInfo.getVersionIds() == null || versionInfo.getVersionIds().size() == 0) {
			throw new InvalidArgumentsException("At least one version id must be provided in the request.");
		}
		
		if(versionInfo.getVersion() == null && versionInfo.getNewProductId() != null) {
			//no new version is specified, so we just need to update the product id
			for(Long versionId : versionInfo.getVersionIds()) {
				ProductVersionDTO toUpdate = pvManager.getById(versionId);
				if(versionInfo.getNewProductId() != null) {
					toUpdate.setProductId(versionInfo.getNewProductId());
				}
				result = pvManager.update(toUpdate);
			}
		} else {
			if(versionInfo.getVersionIds().size() > 1) {
				//if a version was send in, we need to do a "merge" of the new version and old versions 
				//create a new version with the rest of the passed in information
				if(versionInfo.getNewProductId() == null) {
					throw new InvalidArgumentsException("A product ID must be specified.");
				}
				
				ProductVersionDTO newVersion = new ProductVersionDTO();
				newVersion.setVersion(versionInfo.getVersion().getVersion());
				newVersion.setLastModifiedUser(Util.getCurrentUser().getId());
				newVersion.setProductId(versionInfo.getNewProductId());				
				result = pvManager.create(newVersion);
				
				//search for any certified products assigned to the list of versions passed in
				List<CertifiedProductDTO> assignedCps = cpManager.getByVersions(versionInfo.getVersionIds());
					
				//reassign those certified products to the new version
				for(CertifiedProductDTO certifiedProduct : assignedCps) {
					certifiedProduct.setProductVersionId(result.getId());
					cpManager.update(certifiedProduct);
				}
				
				// - mark the passed in versions as deleted
				for(Long versionId : versionInfo.getVersionIds()) {
					pvManager.delete(versionId);
				}
			} else if(versionInfo.getVersionIds().size() == 1) {
				//update the given version id with new data
				ProductVersionDTO toUpdate = new ProductVersionDTO();
				toUpdate.setId(versionInfo.getVersionIds().get(0));
				toUpdate.setLastModifiedDate(new Date());
				toUpdate.setLastModifiedUser(Util.getCurrentUser().getId());
				toUpdate.setDeleted(false);
				toUpdate.setVersion(versionInfo.getVersion().getVersion());
				if(versionInfo.getNewProductId() != null) {
					toUpdate.setProductId(versionInfo.getNewProductId());
				}
				result = pvManager.update(toUpdate);
			}	
		}

		if(result == null) {
			throw new EntityCreationException("There was an error inserting or updating the version information.");
		}
		return new ProductVersion(result);
		
	}
}
