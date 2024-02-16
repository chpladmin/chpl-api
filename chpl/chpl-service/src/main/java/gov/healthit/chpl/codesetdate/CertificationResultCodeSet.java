package gov.healthit.chpl.codesetdate;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Need this text")
   @JsonIgnoreProperties(ignoreUnknown = true)
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   @Builder
public class CertificationResultCodeSet implements Serializable {
    private static final long serialVersionUID = 3834736549928411678L;

    @Schema(description = "Code set to certification result mapping internal ID")
    private Long id;

    @Schema(description = "Code set internal ID")
    private CodeSet codeSet;

    @JsonIgnore
    private Long certificationResultId;
}
