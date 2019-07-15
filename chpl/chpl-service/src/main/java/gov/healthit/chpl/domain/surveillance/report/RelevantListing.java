package gov.healthit.chpl.domain.surveillance.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.surveillance.privileged.PrivilegedSurveillance;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportSurveillanceMapDTO;

public class RelevantListing extends CertifiedProduct implements Serializable {
    private static final long serialVersionUID = -4490178928672550687L;

    private boolean isExcluded = false;
    private String reason;
    private List<PrivilegedSurveillance> surveillances;

    public RelevantListing() {
        super();
        this.surveillances = new ArrayList<PrivilegedSurveillance>();
    }

    public RelevantListing(final QuarterlyReportRelevantListingDTO dto) {
        super(dto);
        this.surveillances = new ArrayList<PrivilegedSurveillance>();
        this.isExcluded = dto.isExcluded();
        this.reason = dto.getExclusionReason();
        if (dto.getSurveillances() != null && dto.getSurveillances().size() > 0) {
            for (QuarterlyReportSurveillanceMapDTO survDto : dto.getSurveillances()) {
                this.surveillances.add(new PrivilegedSurveillance(survDto));
            }
        }
    }

    public boolean isExcluded() {
        return isExcluded;
    }

    public void setExcluded(final boolean isExcluded) {
        this.isExcluded = isExcluded;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public List<PrivilegedSurveillance> getSurveillances() {
        return surveillances;
    }

    public void setSurveillances(final List<PrivilegedSurveillance> surveillances) {
        this.surveillances = surveillances;
    }
}
