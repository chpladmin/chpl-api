package gov.healthit.chpl.attestation.domain;

import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttestationPeriod {

    /**
     * The internal ID of the attestation period.
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The date the attestation period starts.
     */
    @XmlElement(required = true)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate periodStart;

    /**
     * The date the attestation period ends.
     */
    @XmlElement(required = true)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate periodEnd;

    /**
     * The date the submission window of the attestation period starts.
     */
    @XmlElement(required = true)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate submissionStart;

    /**
     * The date the submission window of the attestation period ends.
     */
    @XmlElement(required = true)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate submissionEnd;

    /**
     * A description of the attestation period.
     */
    @XmlElement(required = true)
    private String description;

    public AttestationPeriod(AttestationPeriodEntity entity) {
        this.id = entity.getId();
        this.periodStart = entity.getPeriodStart();
        this.periodEnd = entity.getPeriodEnd();
        this.submissionEnd = entity.getSubmissionEnd();
        this.submissionStart = entity.getSubmissionStart();
        this.description = entity.getDescription();
    }
}
