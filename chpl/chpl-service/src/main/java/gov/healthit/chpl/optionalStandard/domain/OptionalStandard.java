package gov.healthit.chpl.optionalStandard.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OptionalStandard implements Serializable {
    private static final long serialVersionUID = 620315627813875501L;

    @Schema(description = "Optional Standard internal ID.")
    private Long id;

    @Schema(description = "Optional Standard citation.")
    private String citation;

    @Schema(description = "Optional Standard description.")
    private String description;

    // Do not include this property if the value is "empty". It will be empty when generating listing details
    // and will be non-empty (this included) when doing CRUD operations on optional standards
    @JsonInclude(value = Include.NON_EMPTY)
    @XmlTransient
    @Builder.Default
    private List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();
}
