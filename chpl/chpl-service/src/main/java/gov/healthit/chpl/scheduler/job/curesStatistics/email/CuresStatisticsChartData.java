package gov.healthit.chpl.scheduler.job.curesStatistics.email;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.statistics.CriterionListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.PrivacyAndSecurityListingStatisticsDAO;
import gov.healthit.chpl.domain.statistics.CriterionListingCountStatistic;
import gov.healthit.chpl.domain.statistics.CriterionUpgradedToCuresFromOriginalListingStatistic;
import gov.healthit.chpl.domain.statistics.CuresCriterionChartStatistic;
import gov.healthit.chpl.domain.statistics.CuresCriterionUpgradedWithoutOriginalListingStatistic;
import gov.healthit.chpl.domain.statistics.PrivacyAndSecurityListingStatistic;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CuresStatisticsChartData {
    private CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO curesCriterionUpgradedWithoutOriginalListingStatisticsDAO;
    private CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalListingStatisticsDAO;
    private CriterionListingStatisticsDAO criterionListingStatisticsDAO;
    private CertificationCriterionService certificationCriterionService;
    private PrivacyAndSecurityListingStatisticsDAO privacyAndSecurityStatisticsDAO;

    private List<CertificationCriterionDTO> curesCriteria = new ArrayList<CertificationCriterionDTO>();

    @Autowired
    public CuresStatisticsChartData(
            CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO curesCriterionUpgradedWithoutOriginalListingStatisticsDAO,
            CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalListingStatisticsDAO,
            CriterionListingStatisticsDAO criterionListingStatisticsDAO, CertificationCriterionService certificationCriterionService,
            PrivacyAndSecurityListingStatisticsDAO privacyAndSecurityStatisticsDAO) {
        this.curesCriterionUpgradedWithoutOriginalListingStatisticsDAO = curesCriterionUpgradedWithoutOriginalListingStatisticsDAO;
        this.criterionUpgradedToCuresFromOriginalListingStatisticsDAO = criterionUpgradedToCuresFromOriginalListingStatisticsDAO;
        this.criterionListingStatisticsDAO = criterionListingStatisticsDAO;
        this.certificationCriterionService = certificationCriterionService;
        this.privacyAndSecurityStatisticsDAO = privacyAndSecurityStatisticsDAO;

        //Create list of cures criteria used in charts
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_1_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_2_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_3_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_7_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_8_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_9_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_10)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_3_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_2_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_3_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_10_CURES)));
        //curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_12)));
        //curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_13)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.E_1_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_5_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_6_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_9_CURES)));
        curesCriteria.add(new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_10)));
    }

    public LocalDate getReportDate() {
        return curesCriterionUpgradedWithoutOriginalListingStatisticsDAO.getDateOfMostRecentStatistics();
    }

    public Map<CertificationCriterionDTO, CuresCriterionChartStatistic> getCuresCriterionChartStatistics(LocalDate reportDate) {
        Map<CertificationCriterionDTO, CuresCriterionChartStatistic> curesCriterionChartStatistics
                = new HashMap<CertificationCriterionDTO, CuresCriterionChartStatistic>();

        Map<CertificationCriterionDTO, Long> existingCertificationCounts = getExistingCertificationCounts(reportDate);
        Map<CertificationCriterionDTO, Long> newCertificationCounts = getNewCertificationCounts(reportDate);
        Map<CertificationCriterionDTO, Long> listingCounts = getListingCounts(reportDate);

        curesCriterionChartStatistics =  curesCriteria.stream()
                .map(criterion -> getCuresCriterionChartStatisticForCriteria(
                        criterion,
                        existingCertificationCounts,
                        newCertificationCounts,
                        listingCounts))
                .collect(Collectors.toMap(CuresCriterionChartStatistic::getCriterion, item -> item));

        // Handle d12 and d13 completely different (Privacy & Security)
        CertificationCriterionDTO d12Criterion = new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_12));
        curesCriterionChartStatistics.put(d12Criterion, getCuresCriterionChartStatisticForPrivacyAndSecurityCriteria(d12Criterion, reportDate));

        CertificationCriterionDTO d13Criterion = new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_13));
        curesCriterionChartStatistics.put(d13Criterion, getCuresCriterionChartStatisticForPrivacyAndSecurityCriteria(d13Criterion, reportDate));

        return curesCriterionChartStatistics;
    }

    private CuresCriterionChartStatistic getCuresCriterionChartStatisticForPrivacyAndSecurityCriteria(CertificationCriterionDTO criterion, LocalDate reportDate) {

        return  CuresCriterionChartStatistic.builder()
                .criterion(criterion)
                .existingCertificationCount(0L)
                .newCertificationCount(getListingsWithPrivacyAndSecurityCount(reportDate))
                .requiresUpdateCount(getListingsRequiringPrivacyAndSecurityCount(reportDate))
                .listingCount(getListingsWithPrivacyAndSecurityCount(reportDate))
                .build();
    }

    private CuresCriterionChartStatistic getCuresCriterionChartStatisticForCriteria(CertificationCriterionDTO criterion,
            Map<CertificationCriterionDTO, Long> existingCertificationCounts,
            Map<CertificationCriterionDTO, Long> newCertificationCounts,
            Map<CertificationCriterionDTO, Long> listingCounts) {

        CuresCriterionChartStatistic cccs = CuresCriterionChartStatistic.builder()
                .criterion(criterion)
                .existingCertificationCount(getExistingCertificationCountByCriteria(existingCertificationCounts, criterion))
                .newCertificationCount(getNewCertificationCountByCriteria(newCertificationCounts, criterion))
                .requiresUpdateCount(getListingCountByCriteria(listingCounts, getOrignalCriterionBasedOnCuresUpdateCriterion(criterion)))
                .listingCount(getListingCountByCriteria(listingCounts, criterion))
                .build();

        return cccs;
    }

    private Long getExistingCertificationCountByCriteria(Map<CertificationCriterionDTO, Long> existingCertificationCounts, CertificationCriterionDTO criterion) {
        CertificationCriterionDTO criterionFromMap = getMatchingCriterionFromSet(existingCertificationCounts.keySet(), criterion);
        if (existingCertificationCounts.containsKey(criterionFromMap)) {
            return existingCertificationCounts.get(criterionFromMap);
        } else {
            return 0L;
        }
    }

    private Long getNewCertificationCountByCriteria(Map<CertificationCriterionDTO, Long> newCertificationCounts, CertificationCriterionDTO criterion) {
        CertificationCriterionDTO criterionFromMap = getMatchingCriterionFromSet(newCertificationCounts.keySet(), criterion);
        if (newCertificationCounts.containsKey(criterionFromMap)) {
            return newCertificationCounts.get(criterionFromMap);
        } else {
            return 0L;
        }
    }

    private Long getListingCountByCriteria(Map<CertificationCriterionDTO, Long> listingCounts, CertificationCriterionDTO criterion) {
        CertificationCriterionDTO criterionFromMap = getMatchingCriterionFromSet(listingCounts.keySet(), criterion);
        if (listingCounts.containsKey(criterionFromMap)) {
            return listingCounts.get(criterionFromMap);
        } else {
            return 0L;
        }
    }

    private Long getListingsRequiringPrivacyAndSecurityCount(LocalDate reportDate) {
        List<PrivacyAndSecurityListingStatistic> privacyAndSecurityListingStatistic =
            privacyAndSecurityStatisticsDAO.getStatisticsForDate(reportDate);

        if (privacyAndSecurityListingStatistic != null && privacyAndSecurityListingStatistic.size() > 0) {
            return privacyAndSecurityListingStatistic.get(0).getListingsRequiringPrivacyAndSecurityCount();
        } else {
            return 0L;
        }
    }

    private Long getListingsWithPrivacyAndSecurityCount(LocalDate reportDate) {
        List<PrivacyAndSecurityListingStatistic> privacyAndSecurityListingStatistic =
            privacyAndSecurityStatisticsDAO.getStatisticsForDate(reportDate);

        if (privacyAndSecurityListingStatistic != null && privacyAndSecurityListingStatistic.size() > 0) {
            return privacyAndSecurityListingStatistic.get(0).getListingsWithPrivacyAndSecurityCount();
        } else {
            return 0L;
        }
    }

    private CertificationCriterionDTO getMatchingCriterionFromSet(Set<CertificationCriterionDTO> criteria, CertificationCriterionDTO criterion) {
        try {
            return criteria.stream()
                    .filter(item -> item.getId().equals(criterion.getId()))
                    .findAny()
                    .get();
        } catch (Exception e) {
            LOGGER.info(criterion.toString());
            return null;
        }
    }

    private Map<CertificationCriterionDTO, Long> getExistingCertificationCounts(LocalDate reportDate) {
        List<CuresCriterionUpgradedWithoutOriginalListingStatistic> counts =
                curesCriterionUpgradedWithoutOriginalListingStatisticsDAO.getStatisticsForDate(reportDate);
        return counts.stream()
                .collect(Collectors.toMap(CuresCriterionUpgradedWithoutOriginalListingStatistic::getCuresCriterion, CuresCriterionUpgradedWithoutOriginalListingStatistic::getListingsUpgradedWithoutAttestingToOriginalCount));
    }

    private Map<CertificationCriterionDTO, Long> getNewCertificationCounts(LocalDate reportDate) {
        List<CriterionUpgradedToCuresFromOriginalListingStatistic> counts =
                criterionUpgradedToCuresFromOriginalListingStatisticsDAO.getStatisticsForDate(reportDate);
        return counts.stream()
                .collect(Collectors.toMap(CriterionUpgradedToCuresFromOriginalListingStatistic::getCuresCriterion, CriterionUpgradedToCuresFromOriginalListingStatistic::getListingsUpgradedFromOriginalCount));
    }

    private Map<CertificationCriterionDTO, Long> getListingCounts(LocalDate reportDate) {
        List<CriterionListingCountStatistic> counts =
                criterionListingStatisticsDAO.getStatisticsForDate(reportDate);
        return counts.stream()
                .collect(Collectors.toMap(CriterionListingCountStatistic::getCriterion, CriterionListingCountStatistic::getListingsCertifyingToCriterionCount));
    }

    private CertificationCriterionDTO getOrignalCriterionBasedOnCuresUpdateCriterion(CertificationCriterionDTO curesUpdatedCriterion) {
        if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_1_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_1_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_2_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_2_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_3_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_3_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_7_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_7_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_8_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_8_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_9_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_9_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_10).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_6));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_3_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_3_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_2_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_2_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_3_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_3_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_10_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_10_OLD));
        //} else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_12).getId())) {
        //    return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_12));
        //} else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_13).getId())) {
        //    return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_13));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.E_1_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.E_1_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_5_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_5_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_6_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_6_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_9_CURES).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_9_OLD));
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_10).getId())) {
            return new CertificationCriterionDTO(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_8));
        } else {
            return null;
        }
    }
}

