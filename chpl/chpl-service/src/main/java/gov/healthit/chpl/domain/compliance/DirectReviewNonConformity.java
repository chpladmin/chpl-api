package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectReviewNonConformity implements Serializable {
    private static final long serialVersionUID = 7018071377961783691L;

    @JsonProperty(value = "requirement")
    @JsonAlias("customfield_10933")
    @JsonDeserialize(using = SimpleValueDeserializer.class)
    private String requirement;

    @JsonProperty(value = "developerAssociatedListings")
    @JsonAlias("customfield_11206")
    @JsonDeserialize(using = ListingDeserializer.class)
    private List<DeveloperAssociatedListing> developerAssociatedListings;

    @JsonProperty(value = "nonConformityType")
    @JsonAlias("customfield_10934")
    @JsonDeserialize(using = SimpleValueDeserializer.class)
    private String nonConformityType;

    @JsonProperty(value = "dateOfDetermination")
    @JsonAlias("customfield_10921")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate dateOfDetermination;

    @JsonProperty(value = "nonConformitySummary")
    @JsonAlias("customfield_10927")
    private String nonConformitySummary;

    @JsonProperty(value = "nonConformityFindings")
    @JsonAlias("customfield_10928")
    private String nonConformityFindings;

    @JsonProperty(value = "nonConformityStatus")
    @JsonAlias("customfield_10944")
    @JsonDeserialize(using = SimpleValueDeserializer.class)
    private String nonConformityStatus;

    @JsonProperty(value = "developerExplanation")
    @JsonAlias("customfield_10929")
    private String developerExplanation;

    @JsonProperty(value = "resolution")
    @JsonAlias("customfield_10930")
    private String resolution;

    @JsonProperty(value = "capApprovalDate")
    @JsonAlias("customfield_10922")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate capApprovalDate;

    @JsonProperty(value = "capStartDate")
    @JsonAlias("customfield_10923")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate capStartDate;

    @JsonProperty(value = "capMustCompleteDate")
    @JsonAlias("customfield_10925")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate capMustCompleteDate;

    @JsonProperty(value = "capEndDate")
    @JsonAlias("customfield_10926")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate capEndDate;

    @JsonProperty(value = "lastUpdated")
    @JsonAlias("updated")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date lastUpdated;

    @JsonProperty(value = "created")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date created;

}
