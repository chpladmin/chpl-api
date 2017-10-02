package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.ProductOwnerDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductOwner implements Serializable {
    private static final long serialVersionUID = 5678373560374145870L;

    /**
     * Product owner internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Developer that either owns or used to own a given product.
     */
    @XmlElement(required = true)
    private Developer developer;

    /**
     * Date product owner was transferred to the associated developer. Given in
     * milliseconds since epoch.
     */
    @XmlElement(required = true)
    private Long transferDate;

    public ProductOwner() {
    }

    public ProductOwner(ProductOwnerDTO dto) {
        this.id = dto.getId();
        if (dto.getDeveloper() != null) {
            this.developer = new Developer(dto.getDeveloper());
        }
        this.transferDate = dto.getTransferDate();
    }

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(Developer developer) {
        this.developer = developer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(Long transferDate) {
        this.transferDate = transferDate;
    }
}
