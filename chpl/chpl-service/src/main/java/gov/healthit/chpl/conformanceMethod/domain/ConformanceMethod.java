package gov.healthit.chpl.conformanceMethod.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ConformanceMethod implements Serializable {
    private static final long serialVersionUID = -3763885258251744916L;

    /**
     * Conformance Method internal ID.
     */
    @Schema(description = "Conformance Method internal ID.")
    @XmlElement(required = true)
    private Long id;

    /**
     * Conformance method name.
     */
    @Schema(description = "Conformance method name.")
    @XmlElement(required = true)
    private String name;

    @XmlTransient
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate removalDate;

    // Do not include this property if the value is "empty". It will be empty when generating listing details
    // and will be non-empty (this included) when doing CRUD operations on conformance methods
    @JsonInclude(value = Include.NON_EMPTY)
    @XmlTransient
    @Builder.Default
    private List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();

    /**
     * Whether the Conformance Method has been marked as removed.
     */
    @Schema(description = "Whether the Conformance Method has been marked as removed.")
    @XmlElement(required = true)
    public Boolean getRemoved() {
        return this.removalDate != null;
    }
}
