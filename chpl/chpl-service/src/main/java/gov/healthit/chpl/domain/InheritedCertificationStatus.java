package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@Data
public class InheritedCertificationStatus implements Serializable {
    private static final long serialVersionUID = 2456763191912903082L;

    public InheritedCertificationStatus() {
    }

    /**
     * Boolean constructor provided for backwards compatibility with older listing details objects so that activity can
     * be reconstructed with a JSON parser.
     * @param value
     */
    public InheritedCertificationStatus(final boolean value) {
        inherits = value;
    }

    /**
     * This variable indicates whether or not the certification issued was a result of an inherited certified status
     * request. This variable is applicable for 2014 and 2015 Edition and a binary variable that takes either true or
     * false value.
     */
    @Schema(description = "This variable indicates whether or not the certification issued was a result of an inherited certified status "
            + "request. This variable is applicable for 2014 and 2015 Edition and a binary variable that takes either true or "
            + "false value.")
    @XmlElement(name = "inherits")
    private Boolean inherits;

    @XmlTransient
    @JsonIgnore
    private String inheritsStr;

    /**
     * The first-level parent listings that this listing inherits from
     */
    @Schema(description = "The first-level parent listings that this listing inherits from")
    @XmlElementWrapper(name = "parents", nillable = true, required = false)
    @XmlElement(name = "parent")
    @Builder.Default
    private List<CertifiedProduct> parents = new ArrayList<CertifiedProduct>();

    /**
     * The first-level child listings that inherit from this listings
     */
    @Schema(description = "The first-level child listings that inherit from this listings")
    @XmlElementWrapper(name = "children", nillable = true, required = false)
    @XmlElement(name = "child")
    @Builder.Default
    private List<CertifiedProduct> children = new ArrayList<CertifiedProduct>();

}
