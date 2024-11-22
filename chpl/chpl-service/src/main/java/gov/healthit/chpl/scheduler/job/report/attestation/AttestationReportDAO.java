package gov.healthit.chpl.scheduler.job.report.attestation;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.CertificationBodyEntity;

@Component
public class AttestationReportDAO extends BaseDAOImpl {

    public void insert(AttestationReport attestationReport) {
        create(AttestationReportEntity.builder()
                .approvedCount(attestationReport.getApprovedCount())
                .reportDate(attestationReport.getReportDate())
                .attestationPeriod(AttestationPeriodEntity.builder()
                        .id(attestationReport.getAttestationPeriod().getId())
                        .build())
                .certificationBody(CertificationBodyEntity.builder()
                        .id(attestationReport.getCertificationBody().getId())
                        .build())
                .developerCount(attestationReport.getDeveloperCount())
                .noSubmissionCount(attestationReport.getNoSubmissionCount())
                .pendingAcbActionCount(attestationReport.getPendingAcbActionCount())
                .pendingDeveloperActionCount(attestationReport.getPendingDeveloperActionCount())
                .build());
    }

}
