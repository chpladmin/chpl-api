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
import gov.healthit.chpl.manager.ProductManager;

@RestController
@RequestMapping("/product")
public class ProductController {
	
	@Autowired
	ProductManager productManager;
	
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
	public String updateProduct(@RequestBody(required=true) UpdateProductsRequest productInfo) throws EntityCreationException, EntityRetrievalException {
		if(productInfo.getProductIds().size() > 1) {
			//merge these products into one 
			// - create a new product with the rest of the passed in information
			ProductDTO newProduct = new ProductDTO();
			newProduct.setName(productInfo.getProduct().getName());
			newProduct.setLastModifiedUser(Util.getCurrentUser().getId());
			newProduct.setReportFileLocation(productInfo.getProduct().getReportFileLocation());
			productManager.create(newProduct);
			//TODO - search for any versions assigned to the list of products passed in
			
			//TODO - reassign those versions to the new product
			
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
			//TODO: what about the version stuff?
			productManager.update(toUpdate);
		}
		
		//TODO: return something better here
		String isSuccess = String.valueOf(true);
		return "{\"success\" : "+isSuccess+" }";
		
	}
}
