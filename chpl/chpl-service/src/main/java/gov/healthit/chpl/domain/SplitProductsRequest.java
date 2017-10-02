package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.List;

public class SplitProductsRequest implements Serializable {
	private static final long serialVersionUID = -5814308900559692235L;

	private String newProductName;
	private String newProductCode;
	private List<ProductVersion> newVersions;
	private Product oldProduct;
	private List<ProductVersion> oldVersions;

	public String getNewProductName() {
		return newProductName;
	}
	public void setNewProductName(String newProductName) {
		this.newProductName = newProductName;
	}
	public String getNewProductCode() {
		return newProductCode;
	}
	public void setNewProductCode(String newProductCode) {
		this.newProductCode = newProductCode;
	}
	public List<ProductVersion> getNewVersions() {
		return newVersions;
	}
	public void setNewVersions(List<ProductVersion> newVersions) {
		this.newVersions = newVersions;
	}
	public Product getOldProduct() {
		return oldProduct;
	}
	public void setOldProduct(Product oldProduct) {
		this.oldProduct = oldProduct;
	}
	public List<ProductVersion> getOldVersions() {
		return oldVersions;
	}
	public void setOldVersions(List<ProductVersion> oldVersions) {
		this.oldVersions = oldVersions;
	}
}
