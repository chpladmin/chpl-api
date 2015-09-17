package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public class PendingCertifiedProductResults {
	private List<PendingCertifiedProductDetails> pendingCertifiedProducts;

	public PendingCertifiedProductResults() {
		pendingCertifiedProducts = new ArrayList<PendingCertifiedProductDetails>();
	}

	public List<PendingCertifiedProductDetails> getPendingCertifiedProducts() {
		return pendingCertifiedProducts;
	}

	public void setPendingCertifiedProducts(List<PendingCertifiedProductDetails> pendingCertifiedProducts) {
		this.pendingCertifiedProducts = pendingCertifiedProducts;
	}
	
	
}
