package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "results")
public class CertifiedProductDownloadResponse implements Serializable {
	private static final long serialVersionUID = -9189179189014761036L;
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
