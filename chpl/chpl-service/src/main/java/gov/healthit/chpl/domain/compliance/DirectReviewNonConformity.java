package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectReviewNonConformity implements Serializable {
    private static final long serialVersionUID = 7018071377961783691L;
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CLOSED = "CLOSED";
    private static final String NOT_APPLICABLE = "Not applicable";
    private static final String NOT_COMPLETED = "Not completed";
    private static final String TO_BE_DETERMINED = "To be determined";
    private static final String DEFAULT = "Unknown";

    @JsonProperty(value = "requirement", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11018")
    @JsonDeserialize(using = SimpleValueDeserializer.class)
    @XmlTransient
    private String requirement;

    @JsonProperty(value = "developerAssociatedListings")
    @JsonAlias("customfield_12202")
    @JsonDeserialize(using = ListingDeserializer.class)
    @XmlElementWrapper(name = "developerAssociatedListings", nillable = true, required = false)
    @XmlElement(name = "listing")
    private List<DeveloperAssociatedListing> developerAssociatedListings = new ArrayList<DeveloperAssociatedListing>();

    @JsonProperty(value = "nonConformityType")
    @JsonAlias("customfield_11036")
    @XmlElement(required = false, nillable = true)
    private String nonConformityType;

    @JsonProperty(value = "dateOfDetermination", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11021")
    @JsonDeserialize(using = DateDeserializer.class)
    @XmlTransient
    private LocalDate dateOfDetermination;

    @JsonProperty(value = "nonConformitySummary", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11026")
    @XmlTransient
    private String nonConformitySummary;

    @JsonProperty(value = "nonConformityFindings", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11027")
    @XmlTransient
    private String nonConformityFindings;

    @JsonProperty(value = "nonConformityStatus")
    @JsonAlias("customfield_11035")
    private String nonConformityStatus;

    @JsonProperty(value = "nonConformityResolution", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11029")
    @XmlTransient
    private String nonConformityResolution;

    @JsonProperty(value = "capStatus")
    @JsonAlias("customfield_12300")
    @JsonDeserialize(using = SimpleValueDeserializer.class)
    private String capStatus;

    @JsonProperty(value = "providedCapApprovalDate")
    @JsonAlias("customfield_11022")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlTransient
    private LocalDate providedCapApprovalDate;

    @JsonProperty(value = "capStartDate", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11023")
    @JsonDeserialize(using = DateDeserializer.class)
    @XmlTransient
    private LocalDate capStartDate;

    @JsonProperty(value = "providedCapMustCompleteDate")
    @JsonAlias("customfield_11024")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlTransient
    private LocalDate providedCapMustCompleteDate;

    @JsonProperty(value = "providedCapEndDate")
    @JsonAlias("customfield_11025")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlTransient
    private LocalDate providedCapEndDate;

    @JsonProperty(value = "lastUpdated")
    @JsonAlias("updated")
    @JsonDeserialize(using = TimestampDeserializer.class)
    @XmlElement(required = true, nillable = false)
    private Date lastUpdated;

    @JsonProperty(value = "created")
    @JsonDeserialize(using = TimestampDeserializer.class)
    @XmlElement(required = true, nillable = false)
    private Date created;

    @JsonProperty(value = "capApprovalDate")
    @XmlElement(required = false, nillable = true)
    public String getCapApprovalDate() {
        if (getProvidedCapApprovalDate() != null) {
            return getProvidedCapApprovalDate().toString();
        } else {
            DirectReviewNonConformityCapStatus capStatusValue = DirectReviewNonConformityCapStatus.getByName(getCapStatus());
            switch (capStatusValue) {
            case CAP_APPROVED:
            case FAILED_TO_COMPLETE:
            case TBD:
                return TO_BE_DETERMINED;
            case RESOLVED_WITHOUT_CAP:
            case CAP_NOT_PROVIDED:
            case CAP_REJECTED:
                return getCapStatus();
            default:
                return DEFAULT;
            }
        }
    }

    @JsonProperty(value = "capMustCompleteDate")
    @XmlElement(required = false, nillable = true)
    public String getCapMustCompleteDate() {
        if (getProvidedCapMustCompleteDate() != null) {
            return getProvidedCapMustCompleteDate().toString();
        } else {
            DirectReviewNonConformityCapStatus capStatusValue = DirectReviewNonConformityCapStatus.getByName(getCapStatus());
            switch (capStatusValue) {
            case CAP_APPROVED:
            case FAILED_TO_COMPLETE:
            case TBD:
            case CAP_REJECTED:
            case CAP_NOT_PROVIDED:
                return TO_BE_DETERMINED;
            case RESOLVED_WITHOUT_CAP:
                return NOT_APPLICABLE;
            default:
                return DEFAULT;
            }
        }
    }

    @JsonProperty(value = "capEndDate")
    @XmlElement(required = false, nillable = true)
    public String getCapEndDate() {
        if (getProvidedCapEndDate() != null) {
            return getProvidedCapEndDate().toString();
        } else {
            DirectReviewNonConformityCapStatus capStatusValue = DirectReviewNonConformityCapStatus.getByName(getCapStatus());
            switch (capStatusValue) {
            case CAP_APPROVED:
            case FAILED_TO_COMPLETE:
            case TBD:
            case CAP_REJECTED:
            case CAP_NOT_PROVIDED:
                return NOT_COMPLETED;
            case RESOLVED_WITHOUT_CAP:
                return TO_BE_DETERMINED;
            default:
                return DEFAULT;
            }
        }
    }
}
