package gov.healthit.chpl.conformanceMethod.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.listing.CertificationResultConformanceMethodEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationResultConformanceMethod implements Serializable {
    private static final long serialVersionUID = -8648559250833503194L;

    @Schema(description = "Conformance Method to certification result mapping internal ID")
    private Long id;

    @Schema(description = "The method used to evaluate compliance with a certification criterion. It is applicable for 2015 Edition. "
            + "For the Test Procedure method, this also includes the version used during testing of the certification "
            + "criterion functionality.")
    private ConformanceMethod conformanceMethod;

    @Schema(description = "The conformance method version used for a given certification criteria. "
            + "It is valid for Test Procedure method only. "
            + "This variable is a string variable that does not take any restrictions on "
            + "formatting or values and is applicable for 2015 Edition.")
    private String conformanceMethodVersion;

    public CertificationResultConformanceMethod(CertificationResultConformanceMethodEntity entity) {
        this.id = entity.getId();
        ConformanceMethod cm = new ConformanceMethod();
        if (entity.getConformanceMethod() == null) {
            cm.setId(entity.getConformanceMethodId());
        } else {
            cm.setId(entity.getConformanceMethod().getId());
            cm.setName(entity.getConformanceMethod().getName());
            cm.setRemovalDate(entity.getConformanceMethod().getRemovalDate());
        }
        this.conformanceMethod = cm;
        this.conformanceMethodVersion = entity.getVersion();
    }

    public boolean matches(CertificationResultConformanceMethod anotherMethod) {
        boolean result = false;
        if (this.getConformanceMethod() != null && anotherMethod.getConformanceMethod() != null
                && this.getConformanceMethod().getId() != null && anotherMethod.getConformanceMethod().getId() != null
                && this.getConformanceMethod().getId().longValue() == anotherMethod.getConformanceMethod().getId().longValue()
                && (StringUtils.isAllBlank(this.getConformanceMethodVersion(), anotherMethod.getConformanceMethodVersion())
                        || StringUtils.equals(this.getConformanceMethodVersion(), anotherMethod.getConformanceMethodVersion()))) {
            result = true;
        }
        return result;
    }
}
