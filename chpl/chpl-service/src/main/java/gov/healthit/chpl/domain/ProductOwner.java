package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((developer == null) ? 0 : developer.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((transferDate == null) ? 0 : transferDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProductOwner other = (ProductOwner) obj;
        if (developer == null) {
            if (other.developer != null) {
                return false;
            }
        } else if (!developer.getId().equals(other.developer.getId())) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (transferDate == null) {
            if (other.transferDate != null) {
                return false;
            }
        } else if (!transferDate.equals(other.transferDate)) {
            return false;
        }
        return true;
    }

}
