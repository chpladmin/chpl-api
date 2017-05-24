package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class SurveillanceNonconformity implements Serializable {
	private static final long serialVersionUID = -1116153210791576784L;
	
	/**
	 * Nonconformity internal ID
	 */
	@XmlElement(required = true)
	private Long id;
	
	/**
	 * Type of nonconformity; this is either a certification criteria 
	 * number or a textual description
	 */
	@XmlElement(required = true)
	private String nonconformityType;
	
	/**
	 * Nonconformity status. Open or Closed.
	 */
	@XmlElement(required = true)
	private SurveillanceNonconformityStatus status;
	
	/**
	 * Date of determination of nonconformity
	 */
	@XmlElement(required = true)
	private Date dateOfDetermination;
	
	/**
	 * Corrective action plan approval date
	 */
	@XmlElement(required = false, nillable=true)
	private Date capApprovalDate;
	
	/**
	 * Corrective action plan start date
	 */
	@XmlElement(required = false, nillable=true)
	private Date capStartDate;
	
	/**
	 * Corrective action plan end date
	 */
	@XmlElement(required = false, nillable=true)
	private Date capEndDate;
	
	/**
	 * Corrective action plan must complete date
	 */
	@XmlElement(required = false, nillable=true)
	private Date capMustCompleteDate;
	
	/**
	 * Nonconformity summary
	 */
	@XmlElement(required = false, nillable=true)
	private String summary;
	
	/**
	 * Nonconformity findings.
	 */
	@XmlElement(required = false, nillable=true)
	private String findings;
	
	/**
	 * Number of sites passed 
	 */
	@XmlElement(required = false, nillable=true)
	private Integer sitesPassed;
	
	/**
	 * Total number of sites tested
	 */
	@XmlElement(required = false, nillable=true)
	private Integer totalSites;
	
	/**
	 * Developer explanation for the nonconformity
	 */
	@XmlElement(required = false, nillable=true)
	private String developerExplanation;
	
	/**
	 * Resolution description of the nonconformity
	 */
	@XmlElement(required = false, nillable=true)
	private String resolution;
	
	/**
	 * Any documents associated with the nonconformity
	 */
	@XmlElementWrapper(name = "documents", nillable = true, required = false)
	@XmlElement(name = "document")
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
