package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "results")
public class CertifiedProductDownloadResponse {
	
	private List<CertifiedProductSearchDetails> products;
	
	public CertifiedProductDownloadResponse(){
		products = new ArrayList<CertifiedProductSearchDetails>();
	}

	public List<CertifiedProductSearchDetails> getProducts() {
		return products;
	}

	public void setProducts(List<CertifiedProductSearchDetails> products) {
		this.products = products;
	}
}
