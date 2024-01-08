package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@AllArgsConstructor
public class CertifiedProductTargetedUser implements Serializable {
    private static final long serialVersionUID = -2078691100124619582L;

    @Schema(description = "Targeted user to listing mapping internal ID")
    private Long id;

    @Schema(description = "Targeted user internal ID")
    private Long targetedUserId;

    @Schema(description = "Targeted user name")
    private String targetedUserName;

    public CertifiedProductTargetedUser() {
        super();
    }

    public CertifiedProductTargetedUser(final CertifiedProductTargetedUserDTO dto) {
        this.id = dto.getId();
        this.targetedUserId = dto.getTargetedUserId();
        this.targetedUserName = dto.getTargetedUserName();
    }

    public boolean matches(final CertifiedProductTargetedUser other) {
        boolean result = false;
        if (this.getTargetedUserId() != null && other.getTargetedUserId() != null
                && this.getTargetedUserId().longValue() == other.getTargetedUserId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getTargetedUserName()) && !StringUtils.isEmpty(other.getTargetedUserName())
                && this.getTargetedUserName().equals(other.getTargetedUserName())) {
            result = true;
        }
        return result;
    }
}
