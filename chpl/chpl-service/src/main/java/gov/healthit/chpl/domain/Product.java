package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class Product implements Serializable {
    private static final long serialVersionUID = 2177195816284265811L;

    /**
     * Product internal ID
     */
    @XmlElement(required = true)
    private Long productId;

    /**
     * The name of the product being uploaded. It is applicable for 2014 and
     * 2015 Edition.
     */
    @XmlElement(required = true)
    private String name;

    /**
     * A hyperlink to the test results used to certify the Complete EHRs and/or
     * EHR Modules that can be accessed by the public. This variable is
     * applicable to 2014 Edition. Fully qualified URL which is reachable via
     * web browser validation and verification.
     */
    @XmlElement(required = false, nillable = true)
    private String reportFileLocation;

    /**
     * The point of contact for the product
     */
    @XmlElement(required = false, nillable = true)
    private PointOfContact contact;

    /**
     * The developer that owns this product.
     */
    @XmlElement(required = true)
    private Developer owner;

    /**
     * History of which developers have owned this product.
     */
    @XmlElementWrapper(name = "ownerHistory", nillable = true, required = false)
    @XmlElement(name = "owner")
    private List<ProductOwner> ownerHistory;

    @XmlTransient
    private String lastModifiedDate;

    public Product() {
        ownerHistory = new ArrayList<ProductOwner>();
    }

    public Product(ProductDTO dto) {
        this();
        this.productId = dto.getId();
        this.name = dto.getName();
        this.reportFileLocation = dto.getReportFileLocation();
        if (dto.getLastModifiedDate() != null) {
            this.lastModifiedDate = dto.getLastModifiedDate().getTime() + "";
        }
        if (dto.getContact() != null) {
            this.contact = new PointOfContact(dto.getContact());
        }
        if (dto.getOwner() != null) {
            this.owner = new Developer();
            this.owner.setDeveloperId(dto.getOwner().getId());
            this.owner.setName(dto.getOwner().getName());
            this.owner.setDeveloperCode(dto.getOwner().getDeveloperCode());
            this.owner.setSelfDeveloper(dto.getOwner().getSelfDeveloper());
        }
        if (dto.getOwnerHistory() != null && dto.getOwnerHistory().size() > 0) {
            for (ProductOwnerDTO prevOwnerDto : dto.getOwnerHistory()) {
                ProductOwner prevOwner = new ProductOwner(prevOwnerDto);
                this.ownerHistory.add(prevOwner);
            }
        }
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getReportFileLocation() {
        return reportFileLocation;
    }

    public void setReportFileLocation(String reportFileLocation) {
        this.reportFileLocation = reportFileLocation;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Developer getOwner() {
        return owner;
    }

    public void setOwner(Developer owner) {
        this.owner = owner;
    }

    public List<ProductOwner> getOwnerHistory() {
        return ownerHistory;
    }

    public void setOwnerHistory(List<ProductOwner> ownerHistory) {
        this.ownerHistory = ownerHistory;
    }

    public PointOfContact getContact() {
        return contact;
    }

    public void setContact(PointOfContact contact) {
        this.contact = contact;
    }

}
