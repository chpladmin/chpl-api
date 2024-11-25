package gov.healthit.chpl.scheduler.job.report.attestation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import jakarta.persistence.Query;

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

    public List<AttestationReport> getAttestationReportByDate(LocalDate date) {
        return getEntitiesByDate(date).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public void deleteAttestationReportByDate(LocalDate date) {
        getEntitiesByDate(date).forEach(entity -> {
            entity.setDeleted(true);
            update(entity);
        });
    }

    private List<AttestationReportEntity> getEntitiesByDate(LocalDate date) {
        Query query = entityManager.createQuery(
                "from AttestationReportEntity where (NOT deleted = true) and report_date = :date", AttestationReportEntity.class);
        query.setParameter("date", date);
        return query.getResultList();
    }


}
