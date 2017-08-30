package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.PendingCertifiedProductDetails;

public class PendingCertifiedProductResults implements Serializable {
	private static final long serialVersionUID = 4239063235095932795L;
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
