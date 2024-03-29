package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String requirement;

    @JsonProperty(value = "developerAssociatedListings")
    @JsonAlias("customfield_12202")
    @JsonDeserialize(using = ListingDeserializer.class)
    private List<DeveloperAssociatedListing> developerAssociatedListings = new ArrayList<DeveloperAssociatedListing>();

    @JsonProperty(value = "nonConformityType")
    @JsonAlias("customfield_11036")
    private String nonConformityType;

    @JsonProperty(value = "dateOfDetermination", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11021")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate dateOfDetermination;

    @JsonProperty(value = "nonConformitySummary", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11026")
    private String nonConformitySummary;

    @JsonProperty(value = "nonConformityFindings", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11027")
    private String nonConformityFindings;

    @JsonProperty(value = "nonConformityStatus")
    @JsonAlias("customfield_11035")
    private String nonConformityStatus;

    @JsonProperty(value = "nonConformityResolution", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11029")
    private String nonConformityResolution;

    @JsonProperty(value = "capStatus")
    @JsonAlias("customfield_12300")
    @JsonDeserialize(using = SimpleValueDeserializer.class)
    private String capStatus;

    @JsonProperty(value = "providedCapApprovalDate")
    @JsonAlias("customfield_11022")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate providedCapApprovalDate;

    @JsonProperty(value = "capStartDate", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11023")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate capStartDate;

    @JsonProperty(value = "providedCapMustCompleteDate")
    @JsonAlias("customfield_11024")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate providedCapMustCompleteDate;

    @JsonProperty(value = "providedCapEndDate")
    @JsonAlias("customfield_11025")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate providedCapEndDate;

    @JsonProperty(value = "lastUpdated")
    @JsonAlias("updated")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date lastUpdated;

    @JsonProperty(value = "created")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date created;

    @JsonProperty(value = "capApprovalDate")
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
            case CAP_NOT_APPROVED:
                return getCapStatus();
            default:
                return DEFAULT;
            }
        }
    }

    @JsonProperty(value = "capMustCompleteDate")
    public String getCapMustCompleteDate() {
        if (getProvidedCapMustCompleteDate() != null) {
            return getProvidedCapMustCompleteDate().toString();
        } else {
            DirectReviewNonConformityCapStatus capStatusValue = DirectReviewNonConformityCapStatus.getByName(getCapStatus());
            switch (capStatusValue) {
            case CAP_APPROVED:
            case FAILED_TO_COMPLETE:
            case TBD:
            case CAP_NOT_APPROVED:
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
    public String getCapEndDate() {
        if (getProvidedCapEndDate() != null) {
            return getProvidedCapEndDate().toString();
        } else {
            DirectReviewNonConformityCapStatus capStatusValue = DirectReviewNonConformityCapStatus.getByName(getCapStatus());
            switch (capStatusValue) {
            case CAP_APPROVED:
            case FAILED_TO_COMPLETE:
            case TBD:
            case CAP_NOT_APPROVED:
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
