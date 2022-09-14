package gov.healthit.chpl.domain.surveillance;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.util.NullSafeEvaluator;
import gov.healthit.chpl.util.Util;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequirementDetailType {
    private Long id;
    private String number;
    private String title;
    private Boolean removed;

    @XmlTransient
    private CertificationEdition certificationEdition;

    @JsonIgnore
    private String edition;

    private SurveillanceRequirementType surveillanceRequirementType;

    public String getEdition() {
        return NullSafeEvaluator.eval(() -> certificationEdition.getYear(), null);
    }

    public String getFormattedTitle() {
        if (StringUtils.isNotEmpty(number)) {
            return Util.formatCriteriaNumber(this);
        } else {
            return title;
        }
    }
}
