package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import gov.healthit.chpl.util.Util;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
public class NonconformityType implements Serializable {
    private static final long serialVersionUID = -7437221753188417890L;

    private Long id;
    private CertificationEdition certificationEdition;
    private String number;
    private String title;
    private Boolean removed;

    @JsonIgnore
    private NonconformityClassification classification;

    public String getFormattedTitle() {
        if (StringUtils.isNotEmpty(number)) {
            return Util.formatCriteriaNumber(this);
        } else {
            return title;
        }
    }
}
