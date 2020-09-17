package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectReviewNonConformity implements Serializable {
    private static final long serialVersionUID = 7018071377961783691L;

    @JsonProperty(value = "requirement")
    @JsonAlias("customfield_11018")
    @JsonDeserialize(using = SimpleValueDeserializer.class)
    private String requirement;

    @JsonProperty(value = "developerAssociatedListings")
    @JsonAlias("customfield_11034")
    @JsonDeserialize(using = ListingDeserializer.class)
    private List<DeveloperAssociatedListing> developerAssociatedListings;

    @JsonProperty(value = "nonConformityType")
    @JsonAlias("customfield_11036")
    private String nonConformityType;

    @JsonProperty(value = "dateOfDetermination")
    @JsonAlias("customfield_11021")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dateOfDetermination;

    @JsonProperty(value = "nonConformitySummary")
    @JsonAlias("customfield_11026")
    private String nonConformitySummary;

    @JsonProperty(value = "nonConformityFindings")
    @JsonAlias("customfield_11027")
    private String nonConformityFindings;

    @JsonProperty(value = "nonConformityStatus")
    @JsonAlias("customfield_11035")
    private String nonConformityStatus;

    @JsonProperty(value = "developerExplanation")
    @JsonAlias("customfield_11028")
    private String developerExplanation;

    @JsonProperty(value = "resolution")
    @JsonAlias("customfield_11029")
    private String resolution;

    @JsonProperty(value = "capApprovalDate")
    @JsonAlias("customfield_11022")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate capApprovalDate;

    @JsonProperty(value = "capStartDate")
    @JsonAlias("customfield_11023")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate capStartDate;

    @JsonProperty(value = "capMustCompleteDate")
    @JsonAlias("customfield_11024")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate capMustCompleteDate;

    @JsonProperty(value = "capEndDate")
    @JsonAlias("customfield_11025")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate capEndDate;

    @JsonProperty(value = "lastUpdated")
    @JsonAlias("updated")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date lastUpdated;

    @JsonProperty(value = "created")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date created;

}
