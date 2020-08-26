package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;
import java.util.ArrayList;
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
public class DirectReview implements Serializable {
    private static final long serialVersionUID = 7018071377912371691L;

    @JsonProperty(value = "startDate")
    @JsonAlias("customfield_11016")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date startDate;

    @JsonProperty(value = "endDate")
    @JsonAlias("customfield_11017")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date endDate;

    @JsonProperty(value = "circumstances")
    @JsonAlias("customfield_11015")
    @JsonDeserialize(using = CircumstancesDeserializer.class)
    private List<String> circumstances = new ArrayList<String>();

    @JsonProperty(value = "lastUpdated")
    @JsonAlias("updated")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date lastUpdated;

    @JsonProperty(value = "created")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date created;

    private String jiraKey;

    private List<DirectReviewNonConformity> nonConformities = new ArrayList<DirectReviewNonConformity>();
}
