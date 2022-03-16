package gov.healthit.chpl.attestation.domain;

import java.io.Serializable;
import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttestationPeriod implements Serializable {
    private static final long serialVersionUID = 6251042464421884050L;

    /**
     * The internal ID of the attestation period.
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The starting date for which the submitted Attestations are based.
     */
    @XmlElement(required = true)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate periodStart;

    /**
     * The ending date for which the submitted Attestations are based.
     */
    @XmlElement(required = true)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate periodEnd;

    @XmlTransient
    @JsonIgnore
    private LocalDate submissionStart;

    @XmlTransient
    @JsonIgnore
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
