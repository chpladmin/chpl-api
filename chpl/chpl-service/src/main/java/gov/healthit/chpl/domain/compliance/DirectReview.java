package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectReview implements Serializable {
    private static final long serialVersionUID = 7018071377912371691L;

    @JsonProperty(value = "developerId")
    @JsonAlias("customfield_10900")
    @JsonDeserialize(using = DeveloperIdDeserializer.class)
    private Long developerId;

    @JsonProperty(value = "startDate", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11016")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date startDate;

    @JsonProperty(value = "endDate", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11017")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date endDate;

    @JsonProperty(value = "circumstances", access = Access.WRITE_ONLY)
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

    @JsonIgnore
    @XmlTransient
    @JsonAlias("key")
    private String jiraKey;

    private List<DirectReviewNonConformity> nonConformities = new ArrayList<DirectReviewNonConformity>();

    @XmlTransient
    @JsonIgnore
    private Set<String> errorMessages = new HashSet<String>();
}
