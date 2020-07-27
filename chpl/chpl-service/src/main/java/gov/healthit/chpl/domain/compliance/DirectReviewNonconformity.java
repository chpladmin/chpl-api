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
public class DirectReviewNonconformity implements Serializable {
    private static final long serialVersionUID = 7018071377961783691L;

    @JsonProperty(value = "requirement")
    @JsonAlias("customfield_10933")
    @JsonDeserialize(using = SimpleValueDeserializer.class)
    private String requirement;

    @JsonProperty(value = "developerAssociatedListings")
    @JsonAlias("customfield_10943")
    private List<String> developerAssociatedListings;

    @JsonProperty(value = "nonconformityType")
    @JsonAlias("customfield_10934")
    @JsonDeserialize(using = SimpleValueDeserializer.class)
    private String nonconformityType;

    @JsonProperty(value = "dateOfDetermination")
    @JsonAlias("customfield_10921")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate dateOfDetermination;

    @JsonProperty(value = "nonconformityStatus")
    @JsonAlias("customfield_10935")
    @JsonDeserialize(using = SimpleValueDeserializer.class)
    private String nonconformityStatus;

    @JsonProperty(value = "nonconformitySummary")
    @JsonAlias("customfield_10927")
    private String nonconformitySummary;

    @JsonProperty(value = "nonconformityFindings")
    @JsonAlias("customfield_10928")
    private String nonconformityFindings;

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
