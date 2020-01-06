package gov.healthit.chpl.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.env.Environment;

import gov.healthit.chpl.SpringContext;

public class TransparencyAttestationDTO {
    private String transparencyAttestation;
    private Boolean removed;

    public TransparencyAttestationDTO() {
        // This is a temporary bad solution, can be removed after "cures implementation date"
        // when the removed value will always be true
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Date curesRuleEffectiveDate = sdf.parse(
                    SpringContext.getBean(Environment.class).getProperty("cures.ruleEffectiveDate"));
            this.removed = (new Date()).after(curesRuleEffectiveDate) || (new Date()).equals(curesRuleEffectiveDate);
        } catch (ParseException e) {
            // Not sure what to do here?? Runtime Exception?
            throw new RuntimeException("Could not parse cures.ruleEffectiveDate");
        }
    }

    public TransparencyAttestationDTO(String attestation) {
        this();
        this.transparencyAttestation = attestation;
    }

    public String getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(String transparencyAttestation) {
        this.transparencyAttestation = transparencyAttestation;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

}
