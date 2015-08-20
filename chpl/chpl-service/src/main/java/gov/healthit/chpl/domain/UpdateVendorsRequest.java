package gov.healthit.chpl.domain;

import java.util.List;

public class UpdateVendorsRequest {
	private List<Long> vendorIds;
	private Vendor vendor;
	public List<Long> getVendorIds() {
		return vendorIds;
	}
	public void setVendorIds(List<Long> vendorIds) {
		this.vendorIds = vendorIds;
	}
	public Vendor getVendor() {
		return vendor;
	}
	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}
}
