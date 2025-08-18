package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeRequestType implements Serializable {
    private static final long serialVersionUID = -4282000227446957351L;

    public static final String ATTESTATION_TYPE = "Developer Attestation Change Request";
    public static final String DEMOGRAPHICS_TYPE = "Developer Demographics Change Request";
    public static final String SBUL_TYPE = "Service Base URL List Change Request";
    public static final String RWT_PLANS_TYPE = "RWT Plans URL Change Request";
    public static final String RWT_RESULTS_TYPE = "RWT Results URL Change Request";

    private Long id;
    private String name;

    @JsonIgnore
    public Boolean isDemographics() {
        return this.name.equalsIgnoreCase(DEMOGRAPHICS_TYPE);
    }

    @JsonIgnore
    public Boolean isAttestation() {
        return this.name.equalsIgnoreCase(ATTESTATION_TYPE);
    }

    @JsonIgnore
    public Boolean isSbul() {
        return this.name.equalsIgnoreCase(SBUL_TYPE);
    }

    @JsonIgnore
    public Boolean isRwtPlans() {
        return this.name.equalsIgnoreCase(RWT_PLANS_TYPE);
    }

    @JsonIgnore
    public Boolean isRwtResults() {
        return this.name.equalsIgnoreCase(RWT_RESULTS_TYPE);
    }

    @JsonIgnore
    public Boolean isListingUrl() {
        return isSbul() || isRwtPlans() || isRwtResults();
    }
}
