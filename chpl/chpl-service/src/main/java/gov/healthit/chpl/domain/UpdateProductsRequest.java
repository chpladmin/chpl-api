package gov.healthit.chpl.domain;

import java.util.List;

public class UpdateProductsRequest {
	private List<Long> productIds;
	private Product product;
	private Long newDeveloperId;
	
	public List<Long> getProductIds() {
		return productIds;
	}
	public void setProductIds(List<Long> productIds) {
		this.productIds = productIds;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public Long newDeveloperId() {
		return newDeveloperId;
	}
	public void setNewDeveloperId(Long newDeveloperId) {
		this.newDeveloperId = newDeveloperId;
	}
}
