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
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public ListingCount getListingCounts() {
		return listingCounts;
	}
	public void setListingCounts(ListingCount listingCounts) {
		this.listingCounts = listingCounts;
	}
	public String getTransparencyAttestationUrls() {
		return transparencyAttestationUrls;
	}
	public void setTransparencyAttestationUrls(String transparencyAttestationUrls) {
		this.transparencyAttestationUrls = transparencyAttestationUrls;
	}
	public String getAcbAttestations() {
		return acbAttestations;
	}
	public void setAcbAttestations(String acbAttestations) {
		this.acbAttestations = acbAttestations;
	}
	
	public class ListingCount {
	    private Long activeListings;
	    private Long retiredListings;
	    private Long pendingListings;
	    private Long withdrawnByDeveloperListings;
	    private Long withdrawnByOncAcbListings;
	    private Long suspendedByOncAcbListings;
	    private Long suspendedByOncListings;
	    private Long terminatedByOncListings;
	    private Long withdrawnByDeveloperUnderSurveillanceListings;
	    
		public Long getActiveListings() {
			return activeListings;
		}
		public void setActiveListings(Long activeListings) {
			this.activeListings = activeListings;
		}
		public Long getRetiredListings() {
			return retiredListings;
		}
		public void setRetiredListings(Long retiredListings) {
			this.retiredListings = retiredListings;
		}
		public Long getPendingListings() {
			return pendingListings;
		}
		public void setPendingListings(Long pendingListings) {
			this.pendingListings = pendingListings;
		}
		public Long getWithdrawnByDeveloperListings() {
			return withdrawnByDeveloperListings;
		}
		public void setWithdrawnByDeveloperListings(Long withdrawnByDeveloperListings) {
			this.withdrawnByDeveloperListings = withdrawnByDeveloperListings;
		}
		public Long getWithdrawnByOncAcbListings() {
			return withdrawnByOncAcbListings;
		}
		public void setWithdrawnByOncAcbListings(Long withdrawnByOncAcbListings) {
			this.withdrawnByOncAcbListings = withdrawnByOncAcbListings;
		}
		public Long getSuspendedByOncAcbListings() {
			return suspendedByOncAcbListings;
		}
		public void setSuspendedByOncAcbListings(Long suspendedByOncAcbListings) {
			this.suspendedByOncAcbListings = suspendedByOncAcbListings;
		}
		public Long getSuspendedByOncListings() {
			return suspendedByOncListings;
		}
		public void setSuspendedByOncListings(Long suspendedByOncListings) {
			this.suspendedByOncListings = suspendedByOncListings;
		}
		public Long getTerminatedByOncListings() {
			return terminatedByOncListings;
		}
		public void setTerminatedByOncListings(Long terminatedByOncListings) {
			this.terminatedByOncListings = terminatedByOncListings;
		}
		public Long getWithdrawnByDeveloperUnderSurveillanceListings() {
			return withdrawnByDeveloperUnderSurveillanceListings;
		}
		public void setWithdrawnByDeveloperUnderSurveillanceListings(Long withdrawnByDeveloperUnderSurveillanceListings) {
			this.withdrawnByDeveloperUnderSurveillanceListings = withdrawnByDeveloperUnderSurveillanceListings;
		}
	}
}
