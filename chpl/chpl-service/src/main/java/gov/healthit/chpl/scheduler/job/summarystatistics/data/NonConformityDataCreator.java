package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.domain.concept.NonconformityStatusConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class NonConformityDataCreator {
    private static final Logger LOGGER = LogManager.getLogger("summaryStatisticsCreatorJobLogger");
    private static final Long NONCONFORMITY_SURVEILLANCE_RESULT = 1L;

    private SurveillanceStatisticsDAO surveillanceStatisticsDAO;
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    public NonConformityDataCreator(SurveillanceStatisticsDAO surveillanceStatisticsDAO,
            CertificationBodyDAO certificationBodyDAO) {
        this.surveillanceStatisticsDAO = surveillanceStatisticsDAO;
        this.certificationBodyDAO = certificationBodyDAO;
    }

    public Long getTotalNonConformities() {
        return surveillanceStatisticsDAO.getTotalNonConformities(null);
    }

    public Long getTotalClosedNonconformities() {
        return surveillanceStatisticsDAO.getTotalClosedNonconformities(null);
    }

    public EmailStatistic getTotalOpenNonconformities() {
        EmailStatistic totalOpen = new EmailStatistic();
        totalOpen.setCount(surveillanceStatisticsDAO.getTotalOpenNonconformities(null));
        totalOpen.setAcbStatistics(getTotalOpenNonconformitiesByAcb());
        return totalOpen;
    }

    private List<EmailCertificationBodyStatistic> getTotalOpenNonconformitiesByAcb() {
        return surveillanceStatisticsDAO.getTotalOpenNonconformitiesByAcb(null);
    }

    public Long getAverageTimeToAssessConformity() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformitiesWithDeterminationDate = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getDateOfDetermination() != null)
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformitiesWithDeterminationDate.stream()
                .map(nc -> getDaysToAssessNonconformtity(findSurveillanceForNonconformity(nc, surveillances), nc))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    public Long getAverageTimeToApproveCAP() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformities = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getDateOfDetermination() != null && nc.getCapApproval() != null)
                .distinct() // Want to figure out how to get rid of this
                .collect(Collectors.toList());

        Long totalDuration = nonconformities.stream()
                .map(nc -> Math
                        .abs(ChronoUnit.DAYS.between(nc.getDateOfDetermination().toInstant(), nc.getCapApproval().toInstant())))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / nonconformities.size();
    }

    public Long getAverageDurationOfCAP() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformities = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null)
                .distinct() // Want to figure out how to get rid of this
                .collect(Collectors.toList());

        Long totalDuration = nonconformities.stream()
                .map(nc -> Math
                        .abs(ChronoUnit.DAYS.between(
                                nc.getCapApproval().toInstant(),
                                nc.getCapEndDate() != null ? nc.getCapEndDate().toInstant() : Instant.now())))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / nonconformities.size();
    }

    public Long getAverageTimeFromCAPApprovalToSurveillanceClose() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformitiesWithDeterminationDate = surveillances.stream()
                .filter(surv -> surv.getEndDate() != null)
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null)
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformitiesWithDeterminationDate.stream()
                .map(nc -> getDaysFromCAPApprovalToSurveillanceClose(findSurveillanceForNonconformity(nc, surveillances), nc))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    public Long getAverageTimeFromCAPEndToSurveillanceClose() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformitiesWithDeterminationDate = surveillances.stream()
                .filter(surv -> surv.getEndDate() != null)
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapEndDate() != null
                && nc.getNonconformityStatus().getName().equals(NonconformityStatusConcept.CLOSED.getName()))
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformitiesWithDeterminationDate.stream()
                .map(nc -> getDaysFromCAPEndToSurveillanceClose(findSurveillanceForNonconformity(nc, surveillances), nc))
                .collect(Collectors.summingLong(n -> n.longValue()));

        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    public Long getAverageTimeFromSurveillanceOpenToSurveillanceClose() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformities = surveillances.stream()
                .filter(surv -> surv.getStartDate() != null
                && surv.getEndDate() != null)
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .filter(req -> req.getSurveillanceResultTypeId().equals(NONCONFORMITY_SURVEILLANCE_RESULT))
                .flatMap(req -> req.getNonconformities().stream())
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformities.stream()
                .map(nc -> getDaysFromSurveillanceOpenToSurveillanceClose(findSurveillanceForNonconformity(nc, surveillances)))
                .collect(Collectors.summingLong(n -> n.longValue()));

        return totalDuration / surveillances.size();
    }

    public List<EmailCertificationBodyStatistic> getOpenCAPCountByAcb() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        Map<String, Long> openCapsByAcb = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null
                && nc.getCapEndDate() == null)
                .distinct()
                .map(nc -> new NonconformanceStatistic(
                        getCertificationBody(
                                findSurveillanceForNonconformity(
                                        nc, surveillances).getCertifiedProduct().getCertificationBodyId()).getName(), nc))
                .collect(Collectors.groupingBy(stat -> stat.getCertificationBodyName(), Collectors.counting()));

        return openCapsByAcb.entrySet().stream()
                .map(entry -> {
                    EmailCertificationBodyStatistic stat = new EmailCertificationBodyStatistic();
                    stat.setAcbName(entry.getKey());
                    stat.setCount(entry.getValue());
                    return stat;
                })
                .collect(Collectors.toList());
    }

    public List<EmailCertificationBodyStatistic> getClosedCAPCountByAcb() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        Map<String, Long> closedCapsByAcb = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null
                && nc.getCapEndDate() != null)
                .distinct()
                .map(nc -> new NonconformanceStatistic(
                        getCertificationBody(
                                findSurveillanceForNonconformity(
                                        nc, surveillances).getCertifiedProduct().getCertificationBodyId()).getName(), nc))
                .collect(Collectors.groupingBy(stat -> stat.getCertificationBodyName(), Collectors.counting()));

        return closedCapsByAcb.entrySet().stream()
                .map(entry -> {
                    EmailCertificationBodyStatistic stat = new EmailCertificationBodyStatistic();
                    stat.setAcbName(entry.getKey());
                    stat.setCount(entry.getValue());
                    return stat;
                })
                .collect(Collectors.toList());
    }


    private SurveillanceEntity findSurveillanceForNonconformity(SurveillanceNonconformityEntity nonconformity,
            List<SurveillanceEntity> surveillances) {

        return surveillances.stream()
                .filter(surv -> surv.getSurveilledRequirements().stream()
                        .anyMatch(req -> req.getNonconformities().stream()
                                .anyMatch(nc -> nc.getId().equals(nonconformity.getId()))))
                .findFirst()
                .orElse(null);
    }

    private Long getDaysToAssessNonconformtity(SurveillanceEntity surveillance, SurveillanceNonconformityEntity nonconformity) {
        return Math.abs(ChronoUnit.DAYS.between(surveillance.getStartDate().toInstant(),
                nonconformity.getDateOfDetermination().toInstant()));
    }

    private Long getDaysFromCAPApprovalToSurveillanceClose(SurveillanceEntity surveillance,
            SurveillanceNonconformityEntity nonconformity) {
        return Math.abs(ChronoUnit.DAYS.between(
                nonconformity.getCapApproval().toInstant(),
                surveillance.getEndDate().toInstant()));
    }

    private Long getDaysFromCAPEndToSurveillanceClose(SurveillanceEntity surveillance,
            SurveillanceNonconformityEntity nonconformity) {
        return Math.abs(ChronoUnit.DAYS.between(
                nonconformity.getCapEndDate().toInstant(),
                surveillance.getEndDate().toInstant()));
    }

    private Long getDaysFromSurveillanceOpenToSurveillanceClose(SurveillanceEntity surveillance) {
        return Math.abs(ChronoUnit.DAYS.between(
                surveillance.getStartDate().toInstant(),
                surveillance.getEndDate().toInstant()));
    }

    private CertificationBodyDTO getCertificationBody(Long id) {
        try {
            return certificationBodyDAO.getById(id);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve ACB: " + id, e);
            return new CertificationBodyDTO();
        }
    }

    private class NonconformanceStatistic {
        private String certificationBodyName;
        private SurveillanceNonconformityEntity nonconformity;

        NonconformanceStatistic(String certificationBodyName, SurveillanceNonconformityEntity nonconformity) {
            this.certificationBodyName = certificationBodyName;
            this.nonconformity = nonconformity;
        }

        public String getCertificationBodyName() {
            return certificationBodyName;
        }

        public SurveillanceNonconformityEntity getNonconformity() {
            return nonconformity;
        }
    }



}
