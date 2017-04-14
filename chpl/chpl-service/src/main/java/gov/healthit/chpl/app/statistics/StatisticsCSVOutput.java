package gov.healthit.chpl.app.statistics;

import java.util.Date;

public class StatisticsCSVOutput {
	Date date;
	Long totalDevelopers;
	Long totalDevelopersWith2014Listings;
	Long totalDevelopersWith2015Listings;
	Long totalCertifiedProducts;
	Long totalCPsActive2014Listings;
	Long totalCPsActive2015Listings;
	Long totalCPsActiveListings;
	Long totalListings;
	Long total2014Listings;
	Long total2015Listings;
	Long total2011Listings;
	Long totalSurveillanceActivities;
	Long totalOpenSurveillanceActivities;
	Long totalClosedSurveillanceActivities;
	Long totalNonConformities;
	Long totalOpenNonconformities;
	Long totalClosedNonconformities;
	
	public StatisticsCSVOutput(Date date, Long totalDevelopers, Long totalDevelopersWith2014Listings, Long totalDevelopersWith2015Listings, Long totalCertifiedProducts,
	Long totalCPsActive2014Listings, Long totalCPsActive2015Listings, Long totalCPsActiveListings, Long totalListings, Long total2014Listings, Long total2015Listings, 
	Long total2011Listings, Long totalSurveillanceActivities, Long totalOpenSurveillanceActivities, Long totalClosedSurveillanceActivities, Long totalNonConformities, 
	Long totalOpenNonconformities, Long totalClosedNonconformities) {
		super();
		this.date = date;
		this.totalDevelopers = totalDevelopers;
		this.totalDevelopersWith2014Listings = totalDevelopersWith2014Listings;
		this.totalDevelopersWith2015Listings = totalDevelopersWith2015Listings;
		this.totalCertifiedProducts = totalCertifiedProducts;
		this.totalCPsActive2014Listings = totalCPsActive2014Listings;
		this.totalCPsActive2015Listings = totalCPsActive2015Listings;
		this.totalCPsActiveListings = totalCPsActiveListings;
		this.totalListings = totalListings;
		this.total2014Listings = total2014Listings;
		this.total2015Listings = total2015Listings;
		this.total2011Listings = total2011Listings;
		this.totalSurveillanceActivities = totalSurveillanceActivities;
		this.totalOpenSurveillanceActivities = totalOpenSurveillanceActivities;
		this.totalClosedSurveillanceActivities = totalClosedSurveillanceActivities;
		this.totalNonConformities = totalNonConformities;
		this.totalOpenNonconformities = totalOpenNonconformities;
		this.totalClosedNonconformities = totalClosedNonconformities;
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
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
	
	 @Override
    public String toString() {
        return "StatisticsCSVOutput [date=" + date + ", totalDevelopers=" + totalDevelopers
                + ", totalDevelopersWith2014Listings=" + totalDevelopersWith2014Listings + ", totalDevelopersWith2015Listings=" + totalDevelopersWith2015Listings + 
                ", totalCertifiedProducts=" + totalCertifiedProducts + ", totalCPsActive2014Listings=" + totalCPsActive2014Listings 
                + ", totalCPsActive2015Listings=" + totalCPsActive2015Listings + ", totalCPsActiveListings=" + totalCPsActiveListings
                + ", totalListings=" + totalListings + ", total2014Listings=" + total2014Listings + ", total2015Listings=" + total2015Listings 
                + ", total2011Listings=" + total2011Listings + ", totalSurveillanceActivities=" + totalSurveillanceActivities 
                + ", totalOpenSurveillanceActivities=" + totalOpenSurveillanceActivities + ", totalClosedSurveillanceActivities=" + totalClosedSurveillanceActivities 
                + ", totalNonConformities=" + totalNonConformities + ", totalOpenNonconformities=" + totalOpenNonconformities
                + ", totalClosedNonconformities=" + totalClosedNonconformities + "]";
    }
}
