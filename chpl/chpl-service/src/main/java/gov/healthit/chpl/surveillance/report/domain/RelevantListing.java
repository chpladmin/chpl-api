package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class RelevantListing extends CertifiedProduct implements Serializable {
    private static final long serialVersionUID = -4490178928672550687L;

    @JsonIgnore
    private CertificationBody acb;

    @JsonIgnore
    private QuarterlyReport quarterlyReport;

    private List<PrivilegedSurveillance> surveillances = new ArrayList<PrivilegedSurveillance>();

    public RelevantListing() {
        super();
        this.surveillances = new ArrayList<PrivilegedSurveillance>();
    }
}
