package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;
import java.time.LocalDateTime;
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;
import lombok.NoArgsConstructor;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectReview implements Serializable {
    private static final long serialVersionUID = 7018071377912371691L;

    @JsonProperty(value = "developerId")
    @JsonAlias("customfield_10900")
    @JsonDeserialize(using = DeveloperIdDeserializer.class)
    @XmlElement(required = false, nillable = true)
    private Long developerId;

    @JsonProperty(value = "startDate", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11016")
    @JsonDeserialize(using = TimestampDeserializer.class)
    @XmlTransient
    private Date startDate;

    @JsonProperty(value = "endDate", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11017")
    @JsonDeserialize(using = TimestampDeserializer.class)
    @XmlTransient
    private Date endDate;

    @JsonProperty(value = "circumstances", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11015")
    @JsonDeserialize(using = CircumstancesDeserializer.class)
    @XmlTransient
    private List<String> circumstances = new ArrayList<String>();

    @JsonProperty(value = "lastUpdated")
    @JsonAlias("updated")
    @JsonDeserialize(using = TimestampDeserializer.class)
    @XmlElement(required = true, nillable = false)
    private Date lastUpdated;

    @JsonProperty(value = "created")
    @JsonDeserialize(using = TimestampDeserializer.class)
    @XmlElement(required = true, nillable = false)
    private Date created;

    @JsonIgnore
    @XmlTransient
    @JsonAlias("key")
    private String jiraKey;

    @XmlElementWrapper(name = "nonConformities", nillable = true, required = false)
    @XmlElement(name = "nonConformity")
    private List<DirectReviewNonConformity> nonConformities = new ArrayList<DirectReviewNonConformity>();

    @JsonIgnore
    @XmlTransient
    private LocalDateTime fetched;
}
