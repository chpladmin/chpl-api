package gov.healthit.chpl.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class DeveloperTransparency implements Serializable {
    private static final long serialVersionUID = -5492650176812222242L;

    private Long id;
    private String name;
    private String status;
    private ListingCount listingCounts;
    private String mandatoryDisclosures;
    private String acbAttestations;

    public DeveloperTransparency() {
        listingCounts = new ListingCount();
    }

    @Data
    public static class ListingCount implements Serializable {
        private static final long serialVersionUID = -78889892927001067L;

        private Long active;
        private Long retired;
        private Long pending;
        private Long withdrawnByDeveloper;
        private Long withdrawnByOncAcb;
        private Long suspendedByOncAcb;
        private Long suspendedByOnc;
        private Long terminatedByOnc;
        private Long withdrawnByDeveloperUnderSurveillance;
    }
}
