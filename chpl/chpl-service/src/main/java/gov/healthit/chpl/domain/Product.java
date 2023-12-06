package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.util.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class Product implements Serializable {
    private static final long serialVersionUID = 2177195816284265811L;

    @Schema(description = "Product internal ID")
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

    @Schema(description = "The name of the product being uploaded. It is applicable for 2014 and 2015 Edition.")
    private String name;

    @Schema(description = "A hyperlink to the test results used to certify the Complete EHRs and/or "
            + "EHR Modules that can be accessed by the public. This variable is "
            + "applicable to 2014 Edition. Fully qualified URL which is reachable via "
            + "web browser validation and verification.")
    private String reportFileLocation;

    @Schema(description = "The point of contact for the product")
    private PointOfContact contact;

    @Schema(description = "The developer that owns this product.")
    private Developer owner;

    @Schema(description = "History of which developers have owned this product.")
    private List<ProductOwner> ownerHistory;

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
        currentOwner.setTransferDay(LocalDate.now());
        localOwnerHistory.add(currentOwner);
        // first we need to make sure the status events are in ascending order
        localOwnerHistory.sort(new Comparator<ProductOwner>() {
            @Override
            public int compare(ProductOwner o1, ProductOwner o2) {
                if (o1.getTransferDay() != null && o2.getTransferDay() != null) {
                    return o1.getTransferDay().compareTo(o2.getTransferDay());
                }
                return 0;
            }
        });

        ProductOwner result = null;
        for (int i = 0; i < localOwnerHistory.size() && result == null; i++) {
            ProductOwner currOwner = localOwnerHistory.get(i);
            if (currOwner.getTransferDay() != null
                    && currOwner.getTransferDay().isAfter(DateUtil.toLocalDate(date.getTime()))) {
                result = currOwner;
            }
        }
        return result;
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

    // Not all attributes have been included. The attributes being used were selected so the ProductManager could
    // determine equality when updating a Product
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((reportFileLocation == null) ? 0 : reportFileLocation.hashCode());
        result = prime * result + ((contact == null) ? 0 : contact.hashCode());
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        result = prime * result + ((ownerHistory == null) ? 0 : ownerHistory.hashCode());
        return result;
    }

    // Not all attributes have been included. The attributes being used were selected so the ProductManager could
    // determine equality when updating a Product
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
        Product other = (Product) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (StringUtils.isEmpty(name)) {
            if (!StringUtils.isEmpty(other.name)) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (StringUtils.isEmpty(reportFileLocation)) {
            if (!StringUtils.isEmpty(other.reportFileLocation)) {
                return false;
            }
        } else if (!reportFileLocation.equals(other.reportFileLocation)) {
            return false;
        }
        if (contact == null) {
            if (other.contact != null) {
                return false;
            }
        } else if (!contact.equals(other.contact)) {
            return false;
        }
        if (owner == null) {
            if (other.owner != null) {
                return false;
            }
        } else if (!owner.equals(other.owner)) {
            return false;
        }
        if (ownerHistory == null) {
            if (other.ownerHistory != null) {
                return false;
            }
        } else if (!isOwnerHistoryListEqual(other.ownerHistory)) {
            return false;
        }
        return true;
    }

    private boolean isOwnerHistoryListEqual(List<ProductOwner> other) {
        if (ownerHistory.size() != other.size()) {
            return false;
        } else {
            // Make copies of both lists and order them
            List<ProductOwner> clonedThis = ownerHistory.stream()
                    .sorted(Comparator.comparing(ProductOwner::getTransferDay))
                    .toList();
            List<ProductOwner> clonedOther = other.stream()
                    .sorted(Comparator.comparing(ProductOwner::getTransferDay))
                    .toList();
            return clonedThis.equals(clonedOther);
        }
    }
}
