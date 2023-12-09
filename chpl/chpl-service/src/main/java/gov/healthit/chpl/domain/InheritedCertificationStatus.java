package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InheritedCertificationStatus implements Serializable {
    private static final long serialVersionUID = 2456763191912903082L;

    /**
     * Boolean constructor provided for backwards compatibility with older listing details objects so that activity can
     * be reconstructed with a JSON parser.
     * @param value
     */
    public InheritedCertificationStatus(final boolean value) {
        inherits = value;
    }

    @Schema(description = "This variable indicates whether or not the certification issued was a result of an inherited certified status "
            + "request. This is a binary variable that takes either true or false value.")
    private Boolean inherits;

    @JsonIgnore
    private String inheritsStr;

    @Schema(description = "The first-level parent listings that this listing inherits from")
    @Builder.Default
    private List<CertifiedProduct> parents = new ArrayList<CertifiedProduct>();

    @Schema(description = "The first-level child listings that inherit from this listings")
    @Builder.Default
    private List<CertifiedProduct> children = new ArrayList<CertifiedProduct>();

}
