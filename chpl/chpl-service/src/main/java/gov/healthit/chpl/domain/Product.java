package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.domain.contact.PointOfContact;
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
     * This property exists solely to be able to deserialize product activity events.
     * When deserializing the activity we sometimes care about the product ID.
     * This property should not be visible in the generated XSD (and eventually gone from the JSON).
     */
    @XmlTransient
    @Deprecated
    @DeprecatedResponseField(removalDate = "2022-10-15",
        message = "This field is deprecated and will be removed from the response data in a future release. Please use id.")
    private Long productId;

    /**
     * Product internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * This property exists solely to be able to deserialize product activity events. When deserializing
     * the activity we sometimes care about the developer ID and in older activity it came from this field.
     * In newer activity it comes from the "owner field.
     * This property should not be visible in the generated XSD or any response from an API call. 
     */
    @JsonProperty(access = Access.WRITE_ONLY)
    @XmlTransient
    @Deprecated
    private Long developerId;

    /**
     * This property exists solely to be able to deserialize product activity events. When deserializing
     * the activity we sometimes care about the developer name and in older activity it came from this field.
     * In newer activity it comes from the "owner" field. 
     * This property should not be visible in the generated XSD or any response from an API call. 
     */
    @JsonProperty(access = Access.WRITE_ONLY)
    @XmlTransient
    @Deprecated
    private String developerName;

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

    public ProductOwner getOwnerOnDate(Date date) {
        List<ProductOwner> localOwnerHistory = new ArrayList<ProductOwner>();
        if (this.getOwnerHistory() != null && this.getOwnerHistory().size() > 0) {
            localOwnerHistory.addAll(this.getOwnerHistory().stream().collect(Collectors.toList()));
        }
        ProductOwner currentOwner = new ProductOwner();
        currentOwner.setDeveloper(this.getOwner());
        currentOwner.setTransferDate(System.currentTimeMillis());
        localOwnerHistory.add(currentOwner);
        // first we need to make sure the status events are in ascending order
        localOwnerHistory.sort(new Comparator<ProductOwner>() {
            @Override
            public int compare(ProductOwner o1, ProductOwner o2) {
                if (o1.getTransferDate() != null && o2.getTransferDate() != null) {
                    return o1.getTransferDate().compareTo(o2.getTransferDate());
                }
                return 0;
            }
        });

        ProductOwner result = null;
        for (int i = 0; i < localOwnerHistory.size() && result == null; i++) {
            ProductOwner currOwner = localOwnerHistory.get(i);
            if (currOwner.getTransferDate() != null && currOwner.getTransferDate().longValue() >= date.getTime()) {
                result = currOwner;
            }
        }
        return result;
    }

    @Deprecated
    public Long getProductId() {
        return productId;
    }

    @Deprecated
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Deprecated
    @JsonProperty(access = Access.WRITE_ONLY)
    public Long getDeveloperId() {
        return developerId;
    }

    @Deprecated
    public void setDeveloperId(Long developerId) {
        this.developerId = developerId;
    }

    @Deprecated
    @JsonProperty(access = Access.WRITE_ONLY)
    public String getDeveloperName() {
        return developerName;
    }

    @Deprecated
    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
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
