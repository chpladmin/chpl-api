package gov.healthit.chpl.domain;

import java.io.Serializable;

public class DeveloperTransparency implements Serializable {
    private static final long serialVersionUID = -5492650176812222242L;

    private Long id;
    private String name;
    private String status;
    private ListingCount listingCounts;
    private String transparencyAttestationUrls;
    private String acbAttestations;

    public DeveloperTransparency() {
        listingCounts = new ListingCount();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public ListingCount getListingCounts() {
        return listingCounts;
    }

    public void setListingCounts(final ListingCount listingCounts) {
        this.listingCounts = listingCounts;
    }

    public String getTransparencyAttestationUrls() {
        return transparencyAttestationUrls;
    }

    public void setTransparencyAttestationUrls(final String transparencyAttestationUrls) {
        this.transparencyAttestationUrls = transparencyAttestationUrls;
    }

    public String getAcbAttestations() {
        return acbAttestations;
    }

    public void setAcbAttestations(final String acbAttestations) {
        this.acbAttestations = acbAttestations;
    }

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

        public Long getActive() {
            return active;
        }

        public void setActive(final Long active) {
            this.active = active;
        }

        public Long getRetired() {
            return retired;
        }

        public void setRetired(final Long retired) {
            this.retired = retired;
        }

        public Long getPending() {
            return pending;
        }

        public void setPending(final Long pending) {
            this.pending = pending;
        }

        public Long getWithdrawnByDeveloper() {
            return withdrawnByDeveloper;
        }

        public void setWithdrawnByDeveloper(final Long withdrawnByDeveloper) {
            this.withdrawnByDeveloper = withdrawnByDeveloper;
        }

        public Long getWithdrawnByOncAcb() {
            return withdrawnByOncAcb;
        }

        public void setWithdrawnByOncAcb(final Long withdrawnByOncAcb) {
            this.withdrawnByOncAcb = withdrawnByOncAcb;
        }

        public Long getSuspendedByOncAcb() {
            return suspendedByOncAcb;
        }

        public void setSuspendedByOncAcb(final Long suspendedByOncAcb) {
            this.suspendedByOncAcb = suspendedByOncAcb;
        }

        public Long getSuspendedByOnc() {
            return suspendedByOnc;
        }

        public void setSuspendedByOnc(final Long suspendedByOnc) {
            this.suspendedByOnc = suspendedByOnc;
        }

        public Long getTerminatedByOnc() {
            return terminatedByOnc;
        }

        public void setTerminatedByOnc(final Long terminatedByOnc) {
            this.terminatedByOnc = terminatedByOnc;
        }

        public Long getWithdrawnByDeveloperUnderSurveillance() {
            return withdrawnByDeveloperUnderSurveillance;
        }

        public void setWithdrawnByDeveloperUnderSurveillance(final Long withdrawnByDeveloperUnderSurveillance) {
            this.withdrawnByDeveloperUnderSurveillance = withdrawnByDeveloperUnderSurveillance;
        }
    }
}
