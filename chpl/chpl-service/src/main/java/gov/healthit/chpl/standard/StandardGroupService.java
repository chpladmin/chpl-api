package gov.healthit.chpl.standard;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class StandardGroupService {

    private StandardDAO standardDAO;

    @Autowired
    public StandardGroupService(StandardDAO standardDAO) {
        this.standardDAO = standardDAO;
    }

    public Map<String, List<Standard>> getGroupedStandardsForCriteria(CertificationCriterion criterion, LocalDate validAsOfDateRangeStart, LocalDate validAsOfDateRangeEnd) {
        Map<String, List<Standard>> groupedStandardsForCriteria = standardDAO.getAllStandardCriteriaMap().stream()
                .filter(stdCriteriaMap -> stdCriteriaMap.getCriterion().getId().equals(criterion.getId())
                        && StringUtils.isNotEmpty(stdCriteriaMap.getStandard().getGroupName())
                        && DateUtil.datesOverlap(Pair.of(stdCriteriaMap.getStandard().getStartDay(), stdCriteriaMap.getStandard().getEndDay()),
                                Pair.of(validAsOfDateRangeStart, validAsOfDateRangeEnd)))
                .collect(Collectors.groupingBy(value -> value.getStandard().getGroupName(), Collectors.mapping(value -> value.getStandard(), Collectors.toList())));

        //Remove any entries where the group only has 1 standard in the list
        groupedStandardsForCriteria.entrySet().removeIf(entry -> entry.getValue().size() < 2);

        return groupedStandardsForCriteria;
    }
}
