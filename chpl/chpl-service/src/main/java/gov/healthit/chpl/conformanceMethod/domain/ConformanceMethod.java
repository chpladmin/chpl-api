package gov.healthit.chpl.conformanceMethod.domain;

import java.io.Serializable;
import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    @XmlTransient
    @JsonIgnore
    private LocalDate removalDate;

    /**
     * Whether the Conformance Method has been marked as removed.
     */
    @XmlElement(required = true)
    private Boolean removed;

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

    public Boolean getRemoved() {
        return this.removalDate != null;
    }
}
