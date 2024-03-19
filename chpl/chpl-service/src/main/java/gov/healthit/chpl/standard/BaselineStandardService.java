package gov.healthit.chpl.standard;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
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

    public List<Standard> getBaselineStandardsForCriteriaAndListing(CertifiedProductSearchDetails listing, CertificationCriterion criterion, LocalDate standardCheckDate) {
        try {
            List<StandardCriteriaMap> maps = standardDao.getAllStandardCriteriaMap();
            Map<String, List<Standard>> standardGroups = standardGroupService.getGroupedStandardsForCriteria(criterion, standardCheckDate);

            maps.removeIf(map -> !map.getCriterion().getId().equals(criterion.getId()));
            return maps.stream()
                    .filter(map -> !isStandardInAGroup(standardGroups, map.getStandard())
                            && DateUtil.isDateBetweenInclusive(Pair.of(map.getStandard().getRequiredDay(), map.getStandard().getEndDay()), standardCheckDate))
                    .map(map -> map.getStandard())
                    .toList();
        } catch (EntityRetrievalException e) {
            LOGGER.info("Error retrieving Standards for Criterion");
            throw new RuntimeException(e);
        }
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
