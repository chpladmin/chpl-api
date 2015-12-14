package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.SurveillanceCertificationResultDTO;
import gov.healthit.chpl.dto.SurveillanceDTO;

public class SurveillanceDetails {
	
	private Long id;
	private Long certifiedProductId;
	private Date startDate;
	private Date endDate;
	private List<SurveillanceCertificationResult> certifications;
	
	public SurveillanceDetails() {
		this.certifications = new ArrayList<SurveillanceCertificationResult>();
	}
	public SurveillanceDetails(SurveillanceDTO dto) {
		this();
		this.id = dto.getId();
		this.certifiedProductId = dto.getCertifiedProductId();
		this.startDate = dto.getStartDate();
		this.endDate = dto.getEndDate();
	}
	public SurveillanceDetails(SurveillanceDTO dto, List<SurveillanceCertificationResultDTO> certDtos) {
		this(dto);
		if(certDtos != null && certDtos.size() > 0) {
			for(SurveillanceCertificationResultDTO certDto : certDtos) {
				SurveillanceCertificationResult currCert = new SurveillanceCertificationResult(certDto);
				this.certifications.add(currCert);
			}
		}
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCertifiedProductId() {
		return certifiedProductId;
	}
	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}
	
	public void setCertificationDtos(List<SurveillanceCertificationResultDTO> certDtos) {
		if(certDtos != null && certDtos.size() > 0) {
			for(SurveillanceCertificationResultDTO certDto : certDtos) {
				SurveillanceCertificationResult currCert = new SurveillanceCertificationResult(certDto);
				this.certifications.add(currCert);
			}
		}
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public List<SurveillanceCertificationResult> getCertifications() {
		return certifications;
	}
	public void setCertifications(List<SurveillanceCertificationResult> certifications) {
		this.certifications = certifications;
	}
}
