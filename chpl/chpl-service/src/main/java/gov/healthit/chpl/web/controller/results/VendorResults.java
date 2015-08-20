package gov.healthit.chpl.web.controller.results;

import java.util.List;

import gov.healthit.chpl.domain.Vendor;

public class VendorResults {
	private List<Vendor> vendors;

	public List<Vendor> getVendors() {
		return vendors;
	}

	public void setVendors(List<Vendor> vendors) {
		this.vendors = vendors;
	}
}
