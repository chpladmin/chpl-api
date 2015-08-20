package gov.healthit.chpl.domain;

import java.util.List;

public class UpdateProductsRequest {
	private List<Long> productIds;
	private Product product;
	private Long newVendorId;
	
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
	public Long getNewVendorId() {
		return newVendorId;
	}
	public void setNewVendorId(Long newVendorId) {
		this.newVendorId = newVendorId;
	}
}
