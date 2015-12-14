package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "results")
public class CertifiedProductDownloadResponse {
	
	private List<CertifiedProductDownloadDetails> products;
	
	public CertifiedProductDownloadResponse(){
		products = new ArrayList<CertifiedProductDownloadDetails>();
	}

	public List<CertifiedProductDownloadDetails> getProducts() {
		return products;
	}

	public void setProducts(List<CertifiedProductDownloadDetails> products) {
		this.products = products;
	}
}
