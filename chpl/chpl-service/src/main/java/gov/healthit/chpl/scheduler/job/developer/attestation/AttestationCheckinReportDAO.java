package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Component
public class AttestationCheckinReportDAO extends BaseDAOImpl {
    public LocalDate getMaxReportDate() {
        List<LocalDate> result = entityManager.createQuery("SELECT MAX(acr.reportDate) "
                + "FROM AttestationCheckinReportEntity acr "
                + "WHERE (NOT acr.deleted = true) ", LocalDate.class)
                .getResultList();
        if (result == null || result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    public List<CheckInReport> getCheckinReports(LocalDate reportDate) {
        return getEntities(reportDate).stream()
                .map(e -> e.toDomain())
                .toList();
    }

    public void deleteByReportDate(LocalDate reportDate) {
        getEntities(reportDate).forEach(e -> {
            e.setDeleted(true);
            update(e);
        });
    }

    public void save(List<CheckInReport> checkinReports) {
        checkinReports.forEach(report -> {
            AttestationCheckinReportEntity entity = AttestationCheckinReportEntity.builder()
                    .reportDate(LocalDate.now())
                    .developerName(report.getDeveloperName())
                    .developerCode(report.getDeveloperCode())
                    .developerId(report.getDeveloperId())
                    .submittedDate(report.getSubmittedDate())
                    .published(report.getPublished())
                    .currentStatusName(report.getCurrentStatusName())
                    .lastStatusChangeDate(report.getLastStatusChangeDate())
                    .relevantAcbs(report.getRelevantAcbs())
                    .attestationPeriod(report.getAttestationPeriod())
                    .informationBlockingResponse(report.getInformationBlockingResponse())
                    .informationBlockingNoncompliantResponse(report.getInformationBlockingNoncompliantResponse())
                    .assurancesResponse(report.getAssurancesResponse())
                    .assurancesNoncompliantResponse(report.getAssurancesNoncompliantResponse())
                    .communicationsResponse(report.getCommunicationsResponse())
                    .communicationsNoncompliantResponse(report.getCommunicationsNoncompliantResponse())
                    .rwtResponse(report.getRwtResponse())
                    .rwtNoncompliantResponse(report.getRwtNoncompliantResponse())
                    .apiResponse(report.getApiResponse())
                    .apiNoncompliantResponse(report.getApiNoncompliantResponse())
                    .signature(report.getSignature())
                    .signatureEmail(report.getSignatureEmail())
                    .totalSurveillances(report.getTotalSurveillances())
                    .totalSurveillanceNonconformities(report.getTotalSurveillanceNonconformities())
                    .openSurveillanceNonconformities(report.getOpenSurveillanceNonconformities())
                    .totalDirectReviewNonconformities(report.getTotalDirectReviewNonconformities())
                    .openDirectReviewNonconformities(report.getOpenDirectReviewNonconformities())
                    .assurancesValidation(report.getAssurancesValidation())
                    .realWorldTestingValidation(report.getRealWorldTestingValidation())
                    .apiValidation(report.getApiValidation())
                    .warnings(report.getWarnings())
                    .build();
            create(entity);
        });
    }

    private List<AttestationCheckinReportEntity> getEntities(LocalDate reportDate) {
        return entityManager.createQuery(
                "SELECT acr "
                + "FROM AttestationCheckinReportEntity acr "
                + "WHERE (NOT acr.deleted = true) "
                + "AND acr.reportDate = :reportDate ", AttestationCheckinReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }
}
