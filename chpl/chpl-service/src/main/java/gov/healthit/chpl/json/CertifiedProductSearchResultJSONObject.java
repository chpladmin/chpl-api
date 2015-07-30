package gov.healthit.chpl.json;

import java.util.Date;
import java.util.List;

public class CertifiedProductSearchResultJSONObject {
	
	
	private Long id;
	private String vendor;
	private String product;
	private String version;
	private String chplNum;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getChplNum() {
		return chplNum;
	}
	public void setChplNum(String chplNum) {
		this.chplNum = chplNum;
	}
	
	

}
