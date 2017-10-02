package gov.healthit.chpl.domain.statistics;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.domain.DateRange;

public class Statistics implements Serializable {
	private static final long serialVersionUID = 6977674702447513779L;
	private DateRange dateRange;
	private Long totalDevelopers;
	private Long totalDevelopersWith2014Listings;
	private Long totalDevelopersWithActive2014Listings;
	private List<CertifiedBodyStatistics> totalDevelopersByCertifiedBodyWithListingsEachYear;
	private List<CertifiedBodyStatistics> totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear;
	private Long totalSuspendedDevelopersWith2014Listings;
	private Long totalDevelopersWith2015Listings;
	private Long totalDevelopersWithActive2015Listings;
	private Long totalCertifiedProducts;
	private List<CertifiedBodyStatistics> totalCPListingsEachYearByCertifiedBody;
	private List<CertifiedBodyStatistics> totalCPListingsEachYearByCertifiedBodyAndCertificationStatus;
	private Long totalCPs2014Listings;
	private Long totalCPsActive2014Listings;
	private Long totalCPsSuspended2014Listings;
	private Long totalCPs2015Listings;
	private Long totalCPsActive2015Listings;
	private Long totalCPsSuspended2015Listings;
	private Long totalCPsActiveListings;
	private Long totalListings;
	private Long totalActive2014Listings;
	private Long totalActive2015Listings;
	private List<CertifiedBodyStatistics> totalActiveListingsByCertifiedBody;
	private Long total2014Listings;
	private Long total2015Listings;
	private Long total2011Listings;
	private Long totalSurveillanceActivities;
	private Long totalOpenSurveillanceActivities;
	private Long totalClosedSurveillanceActivities;
	private Long totalNonConformities;
	private Long totalOpenNonconformities;
	private Long totalClosedNonconformities;

	public Statistics() {}

	public Long getTotalDevelopers() {
		return totalDevelopers;
	}
	public void setTotalDevelopers(Long totalDevelopers) {
		this.totalDevelopers = totalDevelopers;
	}
	public Long getTotalDevelopersWith2014Listings() {
		return totalDevelopersWith2014Listings;
	}
	public void setTotalDevelopersWith2014Listings(Long totalDevelopersWith2014Listings) {
		this.totalDevelopersWith2014Listings = totalDevelopersWith2014Listings;
	}
	public Long getTotalDevelopersWith2015Listings() {
		return totalDevelopersWith2015Listings;
	}
	public void setTotalDevelopersWith2015Listings(Long totalDevelopersWith2015Listings) {
		this.totalDevelopersWith2015Listings = totalDevelopersWith2015Listings;
	}
	public Long getTotalCertifiedProducts() {
		return totalCertifiedProducts;
	}
	public void setTotalCertifiedProducts(Long totalCertifiedProducts) {
		this.totalCertifiedProducts = totalCertifiedProducts;
	}
	public Long getTotalCPsActive2014Listings() {
		return totalCPsActive2014Listings;
	}
	public void setTotalCPsActive2014Listings(Long totalCPsActive2014Listings) {
		this.totalCPsActive2014Listings = totalCPsActive2014Listings;
	}
	public Long getTotalCPsActive2015Listings() {
		return totalCPsActive2015Listings;
	}
	public void setTotalCPsActive2015Listings(Long totalCPsActive2015Listings) {
		this.totalCPsActive2015Listings = totalCPsActive2015Listings;
	}
	public Long getTotalCPsActiveListings() {
		return totalCPsActiveListings;
	}
	public void setTotalCPsActiveListings(Long totalCPsActiveListings) {
		this.totalCPsActiveListings = totalCPsActiveListings;
	}
	public Long getTotalListings() {
		return totalListings;
	}
	public void setTotalListings(Long totalListings) {
		this.totalListings = totalListings;
	}
	public Long getTotalActive2014Listings() {
		return totalActive2014Listings;
	}
	public void setTotalActive2014Listings(Long totalActive2014Listings) {
		this.totalActive2014Listings = totalActive2014Listings;
	}
	public Long getTotalActive2015Listings() {
		return totalActive2015Listings;
	}
	public void setTotalActive2015Listings(Long totalActive2015Listings) {
		this.totalActive2015Listings = totalActive2015Listings;
	}
	public List<CertifiedBodyStatistics> getTotalActiveListingsByCertifiedBody() {
		return totalActiveListingsByCertifiedBody;
	}
	public void setTotalActiveListingsByCertifiedBody(List<CertifiedBodyStatistics> totalActiveListingsByCertifiedBody) {
		this.totalActiveListingsByCertifiedBody = totalActiveListingsByCertifiedBody;
	}
	public Long getTotal2014Listings() {
		return total2014Listings;
	}
	public void setTotal2014Listings(Long total2014Listings) {
		this.total2014Listings = total2014Listings;
	}
	public Long getTotal2015Listings() {
		return total2015Listings;
	}
	public void setTotal2015Listings(Long total2015Listings) {
		this.total2015Listings = total2015Listings;
	}
	public Long getTotal2011Listings() {
		return total2011Listings;
	}
	public void setTotal2011Listings(Long total2011Listings) {
		this.total2011Listings = total2011Listings;
	}
	public Long getTotalSurveillanceActivities() {
		return totalSurveillanceActivities;
	}
	public void setTotalSurveillanceActivities(Long totalSurveillanceActivities) {
		this.totalSurveillanceActivities = totalSurveillanceActivities;
	}
	public Long getTotalOpenSurveillanceActivities() {
		return totalOpenSurveillanceActivities;
	}
	public void setTotalOpenSurveillanceActivities(Long totalOpenSurveillanceActivities) {
		this.totalOpenSurveillanceActivities = totalOpenSurveillanceActivities;
	}
	public Long getTotalClosedSurveillanceActivities() {
		return totalClosedSurveillanceActivities;
	}
	public void setTotalClosedSurveillanceActivities(Long totalClosedSurveillanceActivities) {
		this.totalClosedSurveillanceActivities = totalClosedSurveillanceActivities;
	}
	public Long getTotalNonConformities() {
		return totalNonConformities;
	}
	public void setTotalNonConformities(Long totalNonConformities) {
		this.totalNonConformities = totalNonConformities;
	}
	public Long getTotalOpenNonconformities() {
		return totalOpenNonconformities;
	}
	public void setTotalOpenNonconformities(Long totalOpenNonconformities) {
		this.totalOpenNonconformities = totalOpenNonconformities;
	}
	public Long getTotalClosedNonconformities() {
		return totalClosedNonconformities;
	}
	public void setTotalClosedNonconformities(Long totalClosedNonconformities) {
		this.totalClosedNonconformities = totalClosedNonconformities;
	}

	public DateRange getDateRange() {
		return dateRange;
	}

	public void setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
	}

	public Long getTotalSuspendedDevelopersWith2014Listings() {
		return totalSuspendedDevelopersWith2014Listings;
	}

	public void setTotalSuspendedDevelopersWith2014Listings(Long totalSuspendedDevelopersWith2014Listings) {
		this.totalSuspendedDevelopersWith2014Listings = totalSuspendedDevelopersWith2014Listings;
	}

	public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsEachYear() {
		return totalDevelopersByCertifiedBodyWithListingsEachYear;
	}

	public void setTotalDevelopersByCertifiedBodyWithListingsEachYear(
			List<CertifiedBodyStatistics> totalDevelopersByCertifiedBodyWithListingsEachYear) {
		this.totalDevelopersByCertifiedBodyWithListingsEachYear = totalDevelopersByCertifiedBodyWithListingsEachYear;
	}

	public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear() {
		return totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear;
	}

	public void setTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
			List<CertifiedBodyStatistics> totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear) {
		this.totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear = totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear;
	}

	public Long getTotalDevelopersWithActive2014Listings() {
		return totalDevelopersWithActive2014Listings;
	}

	public void setTotalDevelopersWithActive2014Listings(Long totalDevelopersWithActive2014Listings) {
		this.totalDevelopersWithActive2014Listings = totalDevelopersWithActive2014Listings;
	}

	public Long getTotalDevelopersWithActive2015Listings() {
		return totalDevelopersWithActive2015Listings;
	}

	public void setTotalDevelopersWithActive2015Listings(Long totalDevelopersWithActive2015Listings) {
		this.totalDevelopersWithActive2015Listings = totalDevelopersWithActive2015Listings;
	}

	public Long getTotalCPs2014Listings() {
		return totalCPs2014Listings;
	}

	public void setTotalCPs2014Listings(Long totalCPs2014Listings) {
		this.totalCPs2014Listings = totalCPs2014Listings;
	}

	public Long getTotalCPs2015Listings() {
		return totalCPs2015Listings;
	}

	public void setTotalCPs2015Listings(Long totalCPs2015Listings) {
		this.totalCPs2015Listings = totalCPs2015Listings;
	}

	public Long getTotalCPsSuspended2014Listings() {
		return totalCPsSuspended2014Listings;
	}

	public void setTotalCPsSuspended2014Listings(Long totalCPsSuspended2014Listings) {
		this.totalCPsSuspended2014Listings = totalCPsSuspended2014Listings;
	}

	public Long getTotalCPsSuspended2015Listings() {
		return totalCPsSuspended2015Listings;
	}

	public void setTotalCPsSuspended2015Listings(Long totalCPsSuspended2015Listings) {
		this.totalCPsSuspended2015Listings = totalCPsSuspended2015Listings;
	}

	public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBody() {
		return totalCPListingsEachYearByCertifiedBody;
	}

	public void setTotalCPListingsEachYearByCertifiedBody(List<CertifiedBodyStatistics> totalCPListingsEachYearByCertifiedBody) {
		this.totalCPListingsEachYearByCertifiedBody = totalCPListingsEachYearByCertifiedBody;
	}

	public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus() {
		return totalCPListingsEachYearByCertifiedBodyAndCertificationStatus;
	}

	public void setTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(
			List<CertifiedBodyStatistics> totalCPListingsEachYearByCertifiedBodyAndCertificationStatus) {
		this.totalCPListingsEachYearByCertifiedBodyAndCertificationStatus = totalCPListingsEachYearByCertifiedBodyAndCertificationStatus;
	}

}
