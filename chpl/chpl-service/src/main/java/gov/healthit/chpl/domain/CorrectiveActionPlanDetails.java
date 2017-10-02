package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.CorrectiveActionPlanCertificationResultDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDocumentationDTO;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "The details about a corrective action plan. If resolved, all fields are required.")
public class CorrectiveActionPlanDetails implements Serializable {
    private static final long serialVersionUID = 1673042559452716629L;
    private Long id;
    private Long certifiedProductId;
    private String acbName;
    private Date surveillanceStartDate;
    private Date surveillanceEndDate;
    private Boolean randomizedSurveillance;
    private Date noncomplianceDate;
    private Date approvalDate;
    private Date effectiveDate;
    private Date estimatedCompletionDate;
    private Date actualCompletionDate;
    private String acbSummary;
    private String developerSummary;
    private String resolution;

    private List<CorrectiveActionPlanCertificationResult> certifications;
    private List<CorrectiveActionPlanDocumentation> documentation;

    public CorrectiveActionPlanDetails() {
        this.certifications = new ArrayList<CorrectiveActionPlanCertificationResult>();
        this.documentation = new ArrayList<CorrectiveActionPlanDocumentation>();
    }

    public CorrectiveActionPlanDetails(CorrectiveActionPlanDTO dto) {
        this();
        this.id = dto.getId();
        this.certifiedProductId = dto.getCertifiedProductId();
        this.surveillanceStartDate = dto.getSurveillanceStartDate();
        this.surveillanceEndDate = dto.getSurveillanceEndDate();
        this.randomizedSurveillance = dto.getSurveillanceResult();
        this.noncomplianceDate = dto.getNonComplianceDeterminationDate();
        this.approvalDate = dto.getApprovalDate();
        this.effectiveDate = dto.getStartDate();
        this.estimatedCompletionDate = dto.getRequiredCompletionDate();
        this.actualCompletionDate = dto.getActualCompletionDate();
        this.acbSummary = dto.getSummary();
        this.developerSummary = dto.getDeveloperExplanation();
        this.resolution = dto.getResolution();

    }

    public CorrectiveActionPlanDetails(CorrectiveActionPlanDTO dto,
            List<CorrectiveActionPlanCertificationResultDTO> certDtos) {
        this(dto);
        if (certDtos != null && certDtos.size() > 0) {
            for (CorrectiveActionPlanCertificationResultDTO certDto : certDtos) {
                CorrectiveActionPlanCertificationResult currCert = new CorrectiveActionPlanCertificationResult(certDto);
                this.certifications.add(currCert);
            }
        }
    }

    public CorrectiveActionPlanDetails(CorrectiveActionPlanDTO dto,
            List<CorrectiveActionPlanCertificationResultDTO> certDtos,
            List<CorrectiveActionPlanDocumentationDTO> docs) {
        this(dto, certDtos);
        if (docs != null && docs.size() > 0) {
            for (CorrectiveActionPlanDocumentationDTO doc : docs) {
                CorrectiveActionPlanDocumentation currDoc = new CorrectiveActionPlanDocumentation(doc);
                this.documentation.add(currDoc);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(final Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public Date getActualCompletionDate() {
        return actualCompletionDate;
    }

    public void setActualCompletionDate(final Date actualCompletionDate) {
        this.actualCompletionDate = actualCompletionDate;
    }

    public List<CorrectiveActionPlanCertificationResult> getCertifications() {
        return certifications;
    }

    public void setCertifications(final List<CorrectiveActionPlanCertificationResult> certifications) {
        this.certifications = certifications;
    }

    public void setCertificationDtos(final List<CorrectiveActionPlanCertificationResultDTO> certDtos) {
        if (certDtos != null && certDtos.size() > 0) {
            for (CorrectiveActionPlanCertificationResultDTO certDto : certDtos) {
                CorrectiveActionPlanCertificationResult currCert = new CorrectiveActionPlanCertificationResult(certDto);
                this.certifications.add(currCert);
            }
        }
    }

    public List<CorrectiveActionPlanDocumentation> getDocumentation() {
        return documentation;
    }

    public void setDocumentation(final List<CorrectiveActionPlanDocumentation> documentation) {
        this.documentation = documentation;
    }

    public void setDocumentationDtos(final List<CorrectiveActionPlanDocumentationDTO> docDtos) {
        if (docDtos != null && docDtos.size() > 0) {
            for (CorrectiveActionPlanDocumentationDTO docDto : docDtos) {
                CorrectiveActionPlanDocumentation currDoc = new CorrectiveActionPlanDocumentation(docDto);
                this.documentation.add(currDoc);
            }
        }
    }

    public Date getSurveillanceStartDate() {
        return surveillanceStartDate;
    }

    public void setSurveillanceStartDate(final Date surveillanceStartDate) {
        this.surveillanceStartDate = surveillanceStartDate;
    }

    public Date getSurveillanceEndDate() {
        return surveillanceEndDate;
    }

    public void setSurveillanceEndDate(final Date surveillanceEndDate) {
        this.surveillanceEndDate = surveillanceEndDate;
    }

    public Boolean getRandomizedSurveillance() {
        return randomizedSurveillance;
    }

    public void setRandomizedSurveillance(final Boolean randomizedSurveillance) {
        this.randomizedSurveillance = randomizedSurveillance;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(final Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }

    public void setEstimatedCompletionDate(final Date estimatedCompletionDate) {
        this.estimatedCompletionDate = estimatedCompletionDate;
    }

    public Date getNoncomplianceDate() {
        return noncomplianceDate;
    }

    public void setNoncomplianceDate(final Date noncomplianceDate) {
        this.noncomplianceDate = noncomplianceDate;
    }

    public String getAcbSummary() {
        return acbSummary;
    }

    public void setAcbSummary(final String acbSummary) {
        this.acbSummary = acbSummary;
    }

    public String getDeveloperSummary() {
        return developerSummary;
    }

    public void setDeveloperSummary(final String developerSummary) {
        this.developerSummary = developerSummary;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(final String resolution) {
        this.resolution = resolution;
    }

    public String getAcbName() {
        return acbName;
    }

    public void setAcbName(final String acbName) {
        this.acbName = acbName;
    }
}
