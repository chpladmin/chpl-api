package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SurveillanceNonconformity {
	private Long id;
	//this is either a certification criteria number or a textual description
	private String nonconformityType;
	private SurveillanceNonconformityStatus status;
	private Date dateOfDetermination;
	private Date capApprovalDate;
	private Date capStartDate;
	private Date capEndDate;
	private Date capMustCompleteDate;
	private String summary;
	private String findings;
	private Integer sitesPassed;
	private Integer totalSites;
	private String developerExplanation;
	private String resolution;
	private List<SurveillanceNonconformityDocument> documents = new ArrayList<SurveillanceNonconformityDocument>();
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNonconformityType() {
		return nonconformityType;
	}
	public void setNonconformityType(String nonconformityType) {
		this.nonconformityType = nonconformityType;
	}
	public SurveillanceNonconformityStatus getStatus() {
		return status;
	}
	public void setStatus(SurveillanceNonconformityStatus status) {
		this.status = status;
	}
	public Date getDateOfDetermination() {
		return dateOfDetermination;
	}
	public void setDateOfDetermination(Date dateOfDetermination) {
		this.dateOfDetermination = dateOfDetermination;
	}
	public Date getCapApprovalDate() {
		return capApprovalDate;
	}
	public void setCapApprovalDate(Date capApprovalDate) {
		this.capApprovalDate = capApprovalDate;
	}
	public Date getCapStartDate() {
		return capStartDate;
	}
	public void setCapStartDate(Date capStartDate) {
		this.capStartDate = capStartDate;
	}
	public Date getCapEndDate() {
		return capEndDate;
	}
	public void setCapEndDate(Date capEndDate) {
		this.capEndDate = capEndDate;
	}
	public Date getCapMustCompleteDate() {
		return capMustCompleteDate;
	}
	public void setCapMustCompleteDate(Date capMustCompleteDate) {
		this.capMustCompleteDate = capMustCompleteDate;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getFindings() {
		return findings;
	}
	public void setFindings(String findings) {
		this.findings = findings;
	}
	public Integer getSitesPassed() {
		return sitesPassed;
	}
	public void setSitesPassed(Integer sitesPassed) {
		this.sitesPassed = sitesPassed;
	}
	public Integer getTotalSites() {
		return totalSites;
	}
	public void setTotalSites(Integer totalSites) {
		this.totalSites = totalSites;
	}
	public String getDeveloperExplanation() {
		return developerExplanation;
	}
	public void setDeveloperExplanation(String developerExplanation) {
		this.developerExplanation = developerExplanation;
	}
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	public List<SurveillanceNonconformityDocument> getDocuments() {
		return documents;
	}
	public void setDocuments(List<SurveillanceNonconformityDocument> documents) {
		this.documents = documents;
	}
}
