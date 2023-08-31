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

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.statistics.CriterionListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO;
import gov.healthit.chpl.domain.statistics.CriterionListingCountStatistic;
import gov.healthit.chpl.domain.statistics.CriterionUpgradedToCuresFromOriginalListingStatistic;
import gov.healthit.chpl.domain.statistics.CuresCriterionChartStatistic;
import gov.healthit.chpl.domain.statistics.CuresCriterionUpgradedWithoutOriginalListingStatistic;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CuresStatisticsChartData {
    private CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO curesCriterionUpgradedWithoutOriginalListingStatisticsDAO;
    private CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalListingStatisticsDAO;
    private CriterionListingStatisticsDAO criterionListingStatisticsDAO;
    private CertificationCriterionService certificationCriterionService;

    private List<CertificationCriterion> curesCriteria = new ArrayList<CertificationCriterion>();

    @Autowired
    public CuresStatisticsChartData(
            CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO curesCriterionUpgradedWithoutOriginalListingStatisticsDAO,
            CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalListingStatisticsDAO,
            CriterionListingStatisticsDAO criterionListingStatisticsDAO, CertificationCriterionService certificationCriterionService) {
        this.curesCriterionUpgradedWithoutOriginalListingStatisticsDAO = curesCriterionUpgradedWithoutOriginalListingStatisticsDAO;
        this.criterionUpgradedToCuresFromOriginalListingStatisticsDAO = criterionUpgradedToCuresFromOriginalListingStatisticsDAO;
        this.criterionListingStatisticsDAO = criterionListingStatisticsDAO;
        this.certificationCriterionService = certificationCriterionService;

        //Create list of cures criteria used in charts
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_1_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_2_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_3_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_7_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_8_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_9_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_10));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_3_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_2_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_3_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_10_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.E_1_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_5_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_6_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_9_CURES));
        curesCriteria.add(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_10));
    }

    public Map<CertificationCriterion, CuresCriterionChartStatistic> getCuresCriterionChartStatistics(LocalDate reportDate) {
        Map<CertificationCriterion, CuresCriterionChartStatistic> curesCriterionChartStatistics
                = new HashMap<CertificationCriterion, CuresCriterionChartStatistic>();

        Map<CertificationCriterion, Long> existingCertificationCounts = getExistingCertificationCounts(reportDate);
        Map<CertificationCriterion, Long> newCertificationCounts = getNewCertificationCounts(reportDate);
        Map<CertificationCriterion, Long> listingCounts = getListingCounts(reportDate);

        curesCriterionChartStatistics =  curesCriteria.stream()
                .map(criterion -> getCuresCriterionChartStatisticForCriteria(
                        criterion,
                        existingCertificationCounts,
                        newCertificationCounts,
                        listingCounts))
                .collect(Collectors.toMap(CuresCriterionChartStatistic::getCriterion, item -> item));

        return curesCriterionChartStatistics;
    }

    private CuresCriterionChartStatistic getCuresCriterionChartStatisticForCriteria(CertificationCriterion criterion,
            Map<CertificationCriterion, Long> existingCertificationCounts,
            Map<CertificationCriterion, Long> newCertificationCounts,
            Map<CertificationCriterion, Long> listingCounts) {

        CuresCriterionChartStatistic cccs = CuresCriterionChartStatistic.builder()
                .criterion(criterion)
                .existingCertificationCount(getExistingCertificationCountByCriteria(existingCertificationCounts, criterion))
                .newCertificationCount(getNewCertificationCountByCriteria(newCertificationCounts, criterion))
                .requiresUpdateCount(getListingCountByCriteria(listingCounts, getOrignalCriterionBasedOnCuresUpdateCriterion(criterion)))
                .listingCount(getListingCountByCriteria(listingCounts, criterion))
                .build();

        return cccs;
    }

    private Long getExistingCertificationCountByCriteria(Map<CertificationCriterion, Long> existingCertificationCounts, CertificationCriterion criterion) {
        CertificationCriterion criterionFromMap = getMatchingCriterionFromSet(existingCertificationCounts.keySet(), criterion);
        if (existingCertificationCounts.containsKey(criterionFromMap)) {
            return existingCertificationCounts.get(criterionFromMap);
        } else {
            return null;
        }
    }

    private Long getNewCertificationCountByCriteria(Map<CertificationCriterion, Long> newCertificationCounts, CertificationCriterion criterion) {
        CertificationCriterion criterionFromMap = getMatchingCriterionFromSet(newCertificationCounts.keySet(), criterion);
        if (newCertificationCounts.containsKey(criterionFromMap)) {
            return newCertificationCounts.get(criterionFromMap);
        } else {
            return null;
        }
    }

    private Long getListingCountByCriteria(Map<CertificationCriterion, Long> listingCounts, CertificationCriterion criterion) {
        CertificationCriterion criterionFromMap = getMatchingCriterionFromSet(listingCounts.keySet(), criterion);
        if (listingCounts.containsKey(criterionFromMap)) {
            return listingCounts.get(criterionFromMap);
        } else {
            return null;
        }
    }

    private CertificationCriterion getMatchingCriterionFromSet(Set<CertificationCriterion> criteria, CertificationCriterion criterion) {
        try {
            return criteria.stream()
                    .filter(item -> item.getId().equals(criterion.getId()))
                    .findAny()
                    .get();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<CertificationCriterion, Long> getNewCertificationCounts(LocalDate reportDate) {
        List<CuresCriterionUpgradedWithoutOriginalListingStatistic> counts =
                curesCriterionUpgradedWithoutOriginalListingStatisticsDAO.getStatisticsForDate(reportDate);
        return counts.stream()
                .collect(Collectors.toMap(CuresCriterionUpgradedWithoutOriginalListingStatistic::getCuresCriterion, CuresCriterionUpgradedWithoutOriginalListingStatistic::getListingsUpgradedWithoutAttestingToOriginalCount));
    }

    private Map<CertificationCriterion, Long> getExistingCertificationCounts(LocalDate reportDate) {
        List<CriterionUpgradedToCuresFromOriginalListingStatistic> counts = criterionUpgradedToCuresFromOriginalListingStatisticsDAO.getStatisticsForDate(reportDate);
        return counts.stream()
                .collect(Collectors.toMap(CriterionUpgradedToCuresFromOriginalListingStatistic::getCuresCriterion,
                        CriterionUpgradedToCuresFromOriginalListingStatistic::getListingsUpgradedFromOriginalCount));
    }

    private Map<CertificationCriterion, Long> getListingCounts(LocalDate reportDate) {
        List<CriterionListingCountStatistic> counts =
                criterionListingStatisticsDAO.getStatisticsForDate(reportDate);
        return counts.stream()
                .collect(Collectors.toMap(CriterionListingCountStatistic::getCriterion, CriterionListingCountStatistic::getListingsCertifyingToCriterionCount));
    }

    private CertificationCriterion getOrignalCriterionBasedOnCuresUpdateCriterion(CertificationCriterion curesUpdatedCriterion) {
        if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_1_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_1_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_2_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_2_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_3_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_3_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_7_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_7_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_8_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_8_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_9_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_9_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_10).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.B_6);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_3_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.C_3_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_2_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_2_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_3_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_3_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_10_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.D_10_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.E_1_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.E_1_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_5_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.F_5_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_6_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_6_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_9_CURES).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_9_OLD);
        } else if (curesUpdatedCriterion.getId().equals(certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_10).getId())) {
            return certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_8);
        } else {
            return null;
        }
    }
}

