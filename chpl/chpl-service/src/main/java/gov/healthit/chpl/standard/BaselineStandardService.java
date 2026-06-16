package gov.healthit.chpl.standard;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class BaselineStandardService {
    private StandardGroupService standardGroupService;
    private StandardDAO standardDao;

    @Autowired
    public BaselineStandardService(StandardGroupService standardGroupService,
            StandardDAO standardDao) {
        this.standardGroupService = standardGroupService;
        this.standardDao = standardDao;
    }

    public List<Standard> getBaselineStandards(CertificationCriterion criterion,
        LocalDate standardCheckDateRangeStart, LocalDate standardCheckDateRangeEnd) {
        List<StandardCriteriaMap> stdCriteriaMaps = standardDao.getAllStandardCriteriaMaps();
        Map<String, List<Standard>> standardGroups = standardGroupService.getGroupedStandardsForCriteria(criterion, standardCheckDateRangeStart, standardCheckDateRangeEnd);

        stdCriteriaMaps.removeIf(map -> !map.getCriterion().getId().equals(criterion.getId()));
        return stdCriteriaMaps.stream()
                .filter(stdCriteriaMap -> !isStandardInAGroup(standardGroups, stdCriteriaMap.getStandard())
                        && DateUtil.isDateBetweenInclusive(
                                Pair.of(stdCriteriaMap.getStandard().getRequiredDay() != null ? stdCriteriaMap.getStandard().getRequiredDay().plusDays(1) : null,
                                        stdCriteriaMap.getStandard().getEndDay()),
                                standardCheckDateRangeEnd))
                .map(map -> map.getStandard())
                .toList();
    }

    public List<Standard> getActiveBaselineStandardsForCriterion(CertificationCriterion criterion,
        LocalDate standardCheckDateRangeStart, LocalDate standardCheckDateRangeEnd) {
        List<StandardCriteriaMap> standardCriteriaMaps = standardDao.getAllStandardCriteriaMaps();
        Map<String, List<Standard>> standardGroups = standardGroupService.getGroupedStandardsForCriteria(criterion, standardCheckDateRangeStart, standardCheckDateRangeEnd);

        standardCriteriaMaps.removeIf(map -> !map.getCriterion().getId().equals(criterion.getId()));
        return standardCriteriaMaps.stream()
                .filter(map -> !isStandardInAGroup(standardGroups, map.getStandard())
                        && DateUtil.isDateBetweenInclusive(Pair.of(map.getStandard().getStartDay(), map.getStandard().getEndDay()), standardCheckDateRangeStart))
                .map(map -> map.getStandard())
                .toList();
    }

    private Boolean isStandardInAGroup(Map<String, List<Standard>> standardGroups, Standard standard) {
        var x = standardGroups.entrySet().stream()
            .flatMap(mapEntry -> mapEntry.getValue().stream())
            .filter(std -> std.getId().equals(standard.getId()))
            .findAny()
            .isPresent();
        return x;
    }
}
