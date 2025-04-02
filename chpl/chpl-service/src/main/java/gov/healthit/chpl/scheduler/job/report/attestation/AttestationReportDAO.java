package gov.healthit.chpl.scheduler.job.report.attestation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.entity.AttestationPeriodEntity;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import jakarta.persistence.Query;

@Component
public class AttestationReportDAO extends BaseDAOImpl {
    private static final Long CHANGE_REQUEST_PENDING_DEVELOPER_ACTION_ID = 2L;
    private static final Long CHANGE_REQUEST_PENDING_ACB_ACTION_ID = 1L;
    private static final Long CHANGE_REQUEST_APPROVED_ID = 3L;


    public void insert(AttestationReport attestationReport) {
        AttestationReportEntity entity = AttestationReportEntity.builder()
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
                .build();
        create(entity);

        insertAttestationReportDeveloper(entity, attestationReport.getDevelopersWithApprovedAttestations(),
                ChangeRequestStatusType.builder().id(CHANGE_REQUEST_APPROVED_ID).build());
        insertAttestationReportDeveloper(entity, attestationReport.getDevelopersWithPendingAcbActionAttestations(),
                ChangeRequestStatusType.builder().id(CHANGE_REQUEST_PENDING_ACB_ACTION_ID).build());
        insertAttestationReportDeveloper(entity, attestationReport.getDeveloperWithPendingDeveloperActionAttestations(),
                ChangeRequestStatusType.builder().id(CHANGE_REQUEST_PENDING_DEVELOPER_ACTION_ID).build());
        insertAttestationReportDeveloper(entity, attestationReport.getDevelopersWithNoSubmissionAttestations(),
                null);
    }

    private void insertAttestationReportDeveloper(AttestationReportEntity attestationReportEntity, List<Developer> developers,
            ChangeRequestStatusType attestationStatus) {

        developers.forEach(developer -> {
            AttestationReportDeveloperEntity entity = AttestationReportDeveloperEntity.builder()
                    .attestationReport(AttestationReportEntity.builder()
                            .id(attestationReportEntity.getId())
                            .build())
                    .developer(DeveloperEntity.builder()
                            .id(developer.getId())
                            .build())
                    .changeRequestStatusType(attestationStatus == null ? null  : ChangeRequestStatusTypeEntity.builder()
                            .id(attestationStatus.getId())
                            .build())
                    .build();

            create(entity);
        });
    }

    public List<AttestationReport> getAttestationReportByDate(LocalDate date) {
        return getEntitiesByDate(date).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public List<AttestationReport> getAttestationReportByAttestationPeriod(AttestationPeriod period) {
        return getEntitiesByAttestationPeriod(period).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public List<AttestationReportDeveloper> getAttestationReportDeveloperByAttestationPeriod(AttestationPeriod period) {
        return getAttestationReportDeveloperEntitiesByAttestationPeriod(period).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public void deleteAttestationReportByDate(LocalDate date) {
        getEntitiesByDate(date).forEach(entity -> {
            entity.setDeleted(true);
            update(entity);
        });
    }

    public void deleteAttestationReportDeveloperByAttestationReportId(Long attestationReportId) {
        Query query = entityManager.createQuery(
                "delete from AttestationReportDeveloperEntity where attestationReport.id = :attestationReportId");
        query.setParameter("attestationReportId", attestationReportId);
        query.executeUpdate();
    }

    private List<AttestationReportEntity> getEntitiesByDate(LocalDate date) {
        Query query = entityManager.createQuery(
                "from AttestationReportEntity where (NOT deleted = true) and reportDate = :date", AttestationReportEntity.class);
        query.setParameter("date", date);
        return query.getResultList();
    }

    private List<AttestationReportEntity> getEntitiesByAttestationPeriod(AttestationPeriod period) {
        Query query = entityManager.createQuery(
                "from AttestationReportEntity where (NOT deleted = true) and attestationPeriod.id = :attestationPeriodId", AttestationReportEntity.class);
        query.setParameter("attestationPeriodId", period.getId());
        return query.getResultList();
    }

    private List<AttestationReportDeveloperEntity> getAttestationReportDeveloperEntitiesByAttestationPeriod(AttestationPeriod period) {
        Query query = entityManager.createQuery(
                "from AttestationReportDeveloperEntity ard "
                + "where (NOT deleted = true) "
                + "and ard.attestationReport.attestationPeriod.id = :attestationPeriodId", AttestationReportDeveloperEntity.class);
        query.setParameter("attestationPeriodId", period.getId());
        return query.getResultList();
    }
}
