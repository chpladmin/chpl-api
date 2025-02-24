package gov.healthit.chpl.cqm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CqmCriteriaManager {

    private CqmCriterionService cqmCriterionService;

    @Autowired
    public CqmCriteriaManager(CqmCriterionService cqmCriterionService) {
        this.cqmCriterionService = cqmCriterionService;
    }

    public List<CQMCriterionAllVersions> getAllCqmCriteria() {
        List<CQMCriterionAllVersions> allCqms = cqmCriterionService.getAllCmsCqmsWithAllVersions();
        allCqms.stream().sorted();
        List<CQMCriterion> nqfCqms = cqmCriterionService.getAllNqfCqms();
        nqfCqms.stream()
            .forEach(nqfCqm -> {
                allCqms.add(CQMCriterionAllVersions.builder()
                        .cmsId(null)
                        .description(nqfCqm.getDescription())
                        .domain(nqfCqm.getCqmDomain())
                        .nqfNumber(nqfCqm.getNqfNumber())
                        .title(nqfCqm.getTitle())
                        .versions(List.of())
                        .build());
            });
        return allCqms;
    }
}
