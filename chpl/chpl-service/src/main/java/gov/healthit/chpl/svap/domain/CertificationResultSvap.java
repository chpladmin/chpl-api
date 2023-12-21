package gov.healthit.chpl.svap.domain;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.svap.entity.CertificationResultSvapEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "ONC has established the Standards Version Advancement Process (SVAP) to enable health IT developers' "
     + "ability to incorporate newer versions of Secretary-adopted standards and implementation specifications, "
     + "as part of the \"Real World Testing\" Condition and Maintenance of Certification requirement (§170.405) "
     + "of the 21st Century Cures Act.")
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CertificationResultSvap implements Serializable {
    private static final long serialVersionUID = -1935940788953178006L;

    @Schema(description = "SVAP to certification result mapping internal ID")
    private Long id;

    @Schema(description = "SVAP internal ID")
    private Long svapId;

    @Schema(description = "Regulatory Text Citation for Standard / Implementation Specification Adopted")
    private String regulatoryTextCitation;

    @Schema(description = "National Coordinator Approved Advanced Version(s)")
    private String approvedStandardVersion;

    @Schema(description = "Indicates if the SVAP has been replaced")
    private boolean replaced;

    public boolean matches(CertificationResultSvap anotherSvap) {
        boolean result = false;
        if (this.getSvapId() != null && anotherSvap.getSvapId() != null
                && Objects.equals(this.getSvapId(), anotherSvap.getSvapId())) {
            result = true;
        } else if ((this.getSvapId() == null || anotherSvap.getSvapId() == null)
                && Objects.equals(this.getApprovedStandardVersion(), anotherSvap.getApprovedStandardVersion())
                && Objects.equals(this.getRegulatoryTextCitation(), anotherSvap.getRegulatoryTextCitation())
                && Objects.equals(this.isReplaced(), anotherSvap.isReplaced())) {
            result = true;
        }
        return result;
    }

    public CertificationResultSvap(CertificationResultSvapEntity entity) {
        this.id = entity.getId();
        this.svapId = entity.getSvapId();
        this.regulatoryTextCitation = entity.getSvap().getRegulatoryTextCitation();
        this.approvedStandardVersion = entity.getSvap().getApprovedStandardVersion();
        this.replaced = entity.getSvap().getReplaced();
    }
 }
