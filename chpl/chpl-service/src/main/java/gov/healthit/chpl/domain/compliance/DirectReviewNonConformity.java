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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateAdapter;
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

    @JsonProperty(value = "capApprovalDate")
    @JsonAlias("customfield_11022")
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate capApprovalDate;

    @JsonProperty(value = "capStartDate", access = Access.WRITE_ONLY)
    @JsonAlias("customfield_11023")
    @JsonDeserialize(using = DateDeserializer.class)
    @XmlTransient
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
