package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import gov.healthit.chpl.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class NonconformityType implements Serializable {

    private static final long serialVersionUID = -7437221753188417890L;

    private Long id;

    @XmlTransient
    private CertificationEdition certificationEdition;

    @JsonIgnore
    private String edition;

    private String number;
    private String title;
    private Boolean removed;

    @JsonIgnore
    @XmlTransient
    private NonconformityClassification classification;

    @JsonIgnore
    public String getFormattedTitle() {
        if (StringUtils.isNotEmpty(number)) {
            return Util.formatCriteriaNumber(this);
        } else {
            return title;
        }
    }
}
