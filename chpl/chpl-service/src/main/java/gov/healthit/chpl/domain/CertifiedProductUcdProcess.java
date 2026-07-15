package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@AllArgsConstructor
public class CertifiedProductUcdProcess implements Serializable {
    private static final long serialVersionUID = 7248865611086710891L;
    public static final Long CUSTOM_UCD_PROCESS_ID = 186L;

    private Long id;

    @Schema(description = "The UCD Process name")
    private String name;

    @JsonIgnore
    private String userEnteredName;

    @Schema(description = "A description of the UCD process used. "
            + "This is a string variable that does not take any restrictions on formatting or values.")
    private String details;

    @JsonIgnore
    private String userEnteredDetails;

    @Builder.Default
    private LinkedHashSet<CertificationCriterion> criteria = new LinkedHashSet<CertificationCriterion>();

    public CertifiedProductUcdProcess() {
        super();
        this.criteria = new LinkedHashSet<CertificationCriterion>();
    }

    public CertifiedProductUcdProcess(CertificationResultUcdProcessDTO dto) {
        this();
        this.id = dto.getUcdProcessId();
        this.name = dto.getUcdProcessName();
        this.details = dto.getUcdProcessDetails();
    }

    public boolean matches(CertifiedProductUcdProcess anotherUcd) {
        boolean result = false;
        if (ObjectUtils.allNotNull(this.getName(), this.getDetails(), anotherUcd.getName(), anotherUcd.getDetails())
                && this.getName().equals(anotherUcd.getName())
                && this.getDetails().equals(anotherUcd.getDetails())) {
            result = true;
        } else if (StringUtils.isAllBlank(this.getDetails(), anotherUcd.getDetails()) && ObjectUtils.allNotNull(this.getName(), anotherUcd.getName())
                && this.getName().equals(anotherUcd.getName())) {
            result = true;
        } else if (StringUtils.isAllEmpty(this.getName(), anotherUcd.getName()) && ObjectUtils.allNotNull(this.getDetails(), anotherUcd.getDetails())
                && this.getDetails().equals(anotherUcd.getDetails())) {
            result = true;
        }
        return result;
    }
}
