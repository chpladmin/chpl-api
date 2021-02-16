package gov.healthit.chpl.surveillance.report.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.surveillance.report.dto.PrivilegedSurveillanceDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportRelevantListingDTO;
import lombok.Data;

@Data
public class RelevantListing extends CertifiedProduct implements Serializable {
    private static final long serialVersionUID = -4490178928672550687L;

    private boolean isExcluded = false;
    private String reason;
    private List<PrivilegedSurveillance> surveillances;

    public RelevantListing() {
        super();
        this.surveillances = new ArrayList<PrivilegedSurveillance>();
    }

    public RelevantListing(QuarterlyReportRelevantListingDTO dto) {
        super(dto);
        this.surveillances = new ArrayList<PrivilegedSurveillance>();
        this.isExcluded = dto.isExcluded();
        this.reason = dto.getExclusionReason();
        if (dto.getSurveillances() != null && dto.getSurveillances().size() > 0) {
            for (PrivilegedSurveillanceDTO survDto : dto.getSurveillances()) {
                this.surveillances.add(new PrivilegedSurveillance(survDto));
            }
        }
    }
}
