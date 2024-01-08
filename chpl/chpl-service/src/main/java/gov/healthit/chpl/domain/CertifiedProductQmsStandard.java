package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
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
public class CertifiedProductQmsStandard implements Serializable {
    private static final long serialVersionUID = -2085183878828053974L;

    @Schema(description = "QMS Standard to listing mapping internal ID")
    private Long id;

    @Schema(description = "QMS Standard internal ID")
    private Long qmsStandardId;

    @Schema(description = "QMS Standard name")
    private String qmsStandardName;

    @JsonIgnore
    private String userEnteredQmsStandardName;

    @Schema(description = " This variable indicates if a QMS standard or mapping was modified, "
            + "documentation on the changes made. This is a string variable that does not take any "
            + "restrictions on formatting or values.")
    private String qmsModification;

    @Schema(description = "QMS Applicable criteria. "
            + "This is a string variable that does not take any restrictions on formatting or values.")
    private String applicableCriteria;

    public CertifiedProductQmsStandard(CertifiedProductQmsStandardDTO dto) {
        this.id = dto.getId();
        this.qmsStandardId = dto.getQmsStandardId();
        this.qmsStandardName = dto.getQmsStandardName();
        this.qmsModification = dto.getQmsModification();
        this.applicableCriteria = dto.getApplicableCriteria();
    }

    public boolean matches(CertifiedProductQmsStandard other) {
        boolean result = false;
        if (this.getQmsStandardId() != null && other.getQmsStandardId() != null
                && this.getQmsStandardId().longValue() == other.getQmsStandardId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getQmsStandardName()) && !StringUtils.isEmpty(other.getQmsStandardName())
                && this.getQmsStandardName().equals(other.getQmsStandardName())) {
            result = true;
        }
        return result;
    }
}
