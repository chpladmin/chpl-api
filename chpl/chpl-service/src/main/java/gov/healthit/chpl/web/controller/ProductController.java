package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
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

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.web.controller.results.ProductResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="products")
@RestController
@RequestMapping("/products")
public class ProductController {
	
	@Autowired ProductManager productManager;
	@Autowired ProductVersionManager versionManager;
	
	@ApiOperation(value="List all products", 
			notes="Either list all products or optionally just all products belonging to a specific developer.")
	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody ProductResults getAllProducts(@RequestParam(required=false) Long developerId) {
		
		List<ProductDTO> productList = null;
		
		if(developerId != null && developerId > 0) {
			productList = productManager.getByDeveloper(developerId);	
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
	
	@ApiOperation(value="Get information about a specific product.", 
			notes="")
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
	
	@ApiOperation(value="Update a product or merge products.", 
			notes="This method serves two purposes: to update a single product's information and to merge two products into one. "
					+ " A user of this service should pass in a single productId to update just that product. "
					+ " If multiple product IDs are passed in, the service performs a merge meaning that a new product "
					+ " is created with all of the information provided and all of the versions "
					+ " previously assigned to the productIds specified are reassigned to the newly created product. The "
					+ " old products are then deleted. "
					+ " The logged in user must have ROLE_ADMIN, ROLE_ACB_ADMIN, or ROLE_ACB_STAFF. ")
	@RequestMapping(value="/update", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public Product updateProduct(@RequestBody(required=true) UpdateProductsRequest productInfo) throws EntityCreationException, 
		EntityRetrievalException, InvalidArgumentsException, JsonProcessingException {
		
		ProductDTO result = null;
		
		if(productInfo.getProductIds() == null || productInfo.getProductIds().size() == 0) {
			throw new InvalidArgumentsException("At least one product id must be provided in the request.");
		}
		
		if(productInfo.getProduct() == null && productInfo.newDeveloperId() != null) {
			//no new product is specified, so we just need to update the developer id
			for(Long productId : productInfo.getProductIds()) {
				ProductDTO toUpdate = productManager.getById(productId);
				toUpdate.setDeveloperId(productInfo.newDeveloperId());
				result = productManager.update(toUpdate);
			}
		} else {
			if(productInfo.getProductIds().size() > 1) {
				//if a product was send in, we need to do a "merge" of the new product and old products 
				//create a new product with the rest of the passed in information
				ProductDTO newProduct = new ProductDTO();
				newProduct.setName(productInfo.getProduct().getName());
				newProduct.setReportFileLocation(productInfo.getProduct().getReportFileLocation());
				if(productInfo.newDeveloperId() != null) {
					newProduct.setDeveloperId(productInfo.newDeveloperId());
				}
				result = productManager.merge(productInfo.getProductIds(), newProduct);
				
			} else if(productInfo.getProductIds().size() == 1) {
				//update the given product id with new data
				ProductDTO toUpdate = new ProductDTO();
				toUpdate.setId(productInfo.getProductIds().get(0));
				toUpdate.setName(productInfo.getProduct().getName());
				toUpdate.setReportFileLocation(productInfo.getProduct().getReportFileLocation());
				//update the developer if an id is supplied
				if(productInfo.newDeveloperId() != null) {
					toUpdate.setDeveloperId(productInfo.newDeveloperId());
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
