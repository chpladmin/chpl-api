package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace = "http://chpl.healthit.gov/listings", name = "results")
public class CertifiedProductDownloadResponse implements Serializable {
    private static final long serialVersionUID = -9189179189014761036L;

    @XmlElementWrapper(name = "listings", nillable = false, required = true)
    @XmlElement(name = "listing")
    private List<CertifiedProductSearchDetails> listings;

    public CertifiedProductDownloadResponse() {
        listings = new ArrayList<CertifiedProductSearchDetails>();
    }

    public List<CertifiedProductSearchDetails> getListings() {
        return listings;
    }

    public void setListings(List<CertifiedProductSearchDetails> listings) {
        this.listings = listings;
    }
}
