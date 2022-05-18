package gov.healthit.chpl.conformanceMethod.domain;

import java.io.Serializable;
import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.domain.compliance.DateDeserializer;
import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConformanceMethod implements Serializable {
    private static final long serialVersionUID = -3763885258251744916L;

    /**
     * Conformance Method internal ID.
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Conformance method name.
     */
    @XmlElement(required = true)
    private String name;

    /**
     * Conformance method removal date.
     */
    @XmlElement(required = false, nillable = true)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate removalDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getRemovalDate() {
        return removalDate;
    }

    public void setRemovalDate(LocalDate removalDate) {
        this.removalDate = removalDate;
    }
}
