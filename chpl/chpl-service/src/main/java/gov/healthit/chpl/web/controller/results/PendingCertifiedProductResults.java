package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public class PendingCertifiedProductResults {
	private List<PendingCertifiedProductDTO> pendingCertifiedProducts;

	public PendingCertifiedProductResults() {
		pendingCertifiedProducts = new ArrayList<PendingCertifiedProductDTO>();
	}

	public List<PendingCertifiedProductDTO> getPendingCertifiedProducts() {
		return pendingCertifiedProducts;
	}

	public void setPendingCertifiedProducts(List<PendingCertifiedProductDTO> pendingCertifiedProducts) {
		this.pendingCertifiedProducts = pendingCertifiedProducts;
	}
	
	
}
