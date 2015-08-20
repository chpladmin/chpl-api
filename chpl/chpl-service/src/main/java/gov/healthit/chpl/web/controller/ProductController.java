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
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;

@RestController
@RequestMapping("/product")
public class ProductController {
	
	@Autowired ProductManager productManager;
	@Autowired ProductVersionManager versionManager;
	
	@RequestMapping(value="/get_product", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody Product getProductById(@RequestParam(required=true) Long productId) throws EntityRetrievalException {
		ProductDTO product = productManager.getById(productId);
		
		Product result = null;
		if(product != null) {
			result = new Product(product);
		}
		return result;
	}
	
	@RequestMapping(value="/list_products_by_vendor", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody List<Product> getProductsByVendor(@RequestParam(required=true) Long vendorId) {
		List<ProductDTO> productList = productManager.getByVendor(vendorId);		
		
		List<Product> products = new ArrayList<Product>();
		if(productList != null && productList.size() > 0) {
			for(ProductDTO dto : productList) {
				Product result = new Product(dto);
				products.add(result);
			}
		}
		return products;
	}
	
	@RequestMapping(value="/update_product", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public Product updateProduct(@RequestBody(required=true) UpdateProductsRequest productInfo) throws EntityCreationException, EntityRetrievalException {
		ProductDTO result = null;
		
		if(productInfo.getProductIds().size() > 1) {
			//merge these products into one 
			// - create a new product with the rest of the passed in information
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
			toUpdate.setName(productInfo.getProduct().getName());
			toUpdate.setReportFileLocation(productInfo.getProduct().getReportFileLocation());
			//update the vendor if an id is supplied
			if(productInfo.getNewVendorId() != null) {
				toUpdate.setVendorId(productInfo.getNewVendorId());
			}
			result = productManager.update(toUpdate);
		}
		
		if(result == null) {
			throw new EntityCreationException("There was an error inserting or updating the product information.");
		}
		return new Product(result);
	}
}
