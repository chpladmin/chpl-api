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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectReviewNonConformity implements Serializable {
    private static final long serialVersionUID = 7018071377961783691L;
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CLOSED = "CLOSED";

    @JsonProperty(value = "requirement")
    @JsonAlias("customfield_11018")
    @JsonDeserialize(using = SimpleValueDeserializer.class)
    @XmlElement(required = false, nillable = true)
    private String requirement;

    @JsonProperty(value = "developerAssociatedListings")
    @JsonAlias("customfield_11034")
    @JsonDeserialize(using = ListingDeserializer.class)
    @XmlElementWrapper(name = "developerAssociatedListings", nillable = true, required = false)
    @XmlElement(name = "listing")
    private List<DeveloperAssociatedListing> developerAssociatedListings = new ArrayList<DeveloperAssociatedListing>();

    @JsonProperty(value = "nonConformityType")
    @JsonAlias("customfield_11036")
    @XmlElement(required = false, nillable = true)
    private String nonConformityType;

    @JsonProperty(value = "dateOfDetermination")
    @JsonAlias("customfield_11021")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate dateOfDetermination;

    @JsonProperty(value = "nonConformitySummary")
    @JsonAlias("customfield_11026")
    @XmlElement(required = false, nillable = true)
    private String nonConformitySummary;

    @JsonProperty(value = "nonConformityFindings")
    @JsonAlias("customfield_11027")
    @XmlElement(required = false, nillable = true)
    private String nonConformityFindings;

    @JsonProperty(value = "nonConformityStatus")
    @JsonAlias("customfield_11035")
    @XmlElement(required = false, nillable = true)
    private String nonConformityStatus;

    @JsonProperty(value = "resolution")
    @JsonAlias("customfield_11029")
    @XmlElement(required = false, nillable = true)
    private String resolution;

    @JsonProperty(value = "capApprovalDate")
    @JsonAlias("customfield_11022")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate capApprovalDate;

    @JsonProperty(value = "capStartDate")
    @JsonAlias("customfield_11023")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate capStartDate;

    @JsonProperty(value = "capMustCompleteDate")
    @JsonAlias("customfield_11024")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate capMustCompleteDate;

    @JsonProperty(value = "capEndDate")
    @JsonAlias("customfield_11025")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate capEndDate;

    @JsonProperty(value = "lastUpdated")
    @JsonAlias("updated")
    @JsonDeserialize(using = TimestampDeserializer.class)
    @XmlElement(required = true, nillable = false)
    private Date lastUpdated;

    @JsonProperty(value = "created")
    @JsonDeserialize(using = TimestampDeserializer.class)
    @XmlElement(required = true, nillable = false)
    private Date created;

}
