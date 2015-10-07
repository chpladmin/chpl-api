package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.web.controller.results.ProductResults;

@RestController
@RequestMapping("/products")
public class ProductController {
	
	@Autowired ProductManager productManager;
	@Autowired ProductVersionManager versionManager;
	
	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody ProductResults getAllProducts(@RequestParam(required=false) Long vendorId) {
		
		List<ProductDTO> productList = null;
		
		if(vendorId != null && vendorId > 0) {
			productList = productManager.getByVendor(vendorId);	
		} else {
			productList = productManager.getAll();
		}
		
		List<Product> products = new ArrayList<Product>();
		if(productList != null && productList.size() > 0) {
			for(ProductDTO dto : productList) {
				Product result = new Product(dto);
				products.add(result);
			}
		}
		
		ProductResults results = new ProductResults();
		results.setProducts(products);
		return results;
	}
	
	@RequestMapping(value="/{productId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Product getProductById(@PathVariable("productId") Long productId) throws EntityRetrievalException {
		ProductDTO product = productManager.getById(productId);
		
		Product result = null;
		if(product != null) {
			result = new Product(product);
		}
		return result;
	}
	
	@RequestMapping(value="/update", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public Product updateProduct(@RequestBody(required=true) UpdateProductsRequest productInfo) throws EntityCreationException, 
		EntityRetrievalException, InvalidArgumentsException, JsonProcessingException {
		
		ProductDTO result = null;
		
		if(productInfo.getProductIds() == null || productInfo.getProductIds().size() == 0) {
			throw new InvalidArgumentsException("At least one product id must be provided in the request.");
		}
		
		if(productInfo.getProduct() == null && productInfo.getNewVendorId() != null) {
			//no new product is specified, so we just need to update the vendor id
			for(Long productId : productInfo.getProductIds()) {
				ProductDTO toUpdate = productManager.getById(productId);
				toUpdate.setVendorId(productInfo.getNewVendorId());
				result = productManager.update(toUpdate);
			}
		} else {
			if(productInfo.getProductIds().size() > 1) {
				//if a product was send in, we need to do a "merge" of the new product and old products 
				//create a new product with the rest of the passed in information
				ProductDTO newProduct = new ProductDTO();
				newProduct.setName(productInfo.getProduct().getName());
				newProduct.setLastModifiedUser(Util.getCurrentUser().getId());
				newProduct.setReportFileLocation(productInfo.getProduct().getReportFileLocation());
				if(productInfo.getNewVendorId() != null) {
					newProduct.setVendorId(productInfo.getNewVendorId());
				}
				result = productManager.create(newProduct);
				
				//search for any versions assigned to the list of products passed in
				List<ProductVersionDTO> assignedVersions = versionManager.getByProducts(productInfo.getProductIds());
				//reassign those versions to the new product
				for(ProductVersionDTO version : assignedVersions) {
					version.setProductId(result.getId());
					versionManager.update(version);
				}
				
				// - mark the passed in products as deleted
				for(Long productId : productInfo.getProductIds()) {
					productManager.delete(productId);
				}
			} else if(productInfo.getProductIds().size() == 1) {
				//update the given product id with new data
				ProductDTO toUpdate = new ProductDTO();
				toUpdate.setId(productInfo.getProductIds().get(0));
				toUpdate.setLastModifiedDate(new Date());
				toUpdate.setLastModifiedUser(Util.getCurrentUser().getId());
				toUpdate.setDeleted(false);
				toUpdate.setName(productInfo.getProduct().getName());
				toUpdate.setReportFileLocation(productInfo.getProduct().getReportFileLocation());
				//update the vendor if an id is supplied
				if(productInfo.getNewVendorId() != null) {
					toUpdate.setVendorId(productInfo.getNewVendorId());
				}
				result = productManager.update(toUpdate);
			}	
		}

		if(result == null) {
			throw new EntityCreationException("There was an error inserting or updating the product information.");
		}
		return new Product(result);
	}
}
