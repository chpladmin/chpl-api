package gov.healthit.chpl.cqm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CqmManager {

    private CqmCriterionService cqmCriterionService;

    @Autowired
    public CqmManager(CqmCriterionService cqmCriterionService) {
        this.cqmCriterionService = cqmCriterionService;
    }

    public List<CQMCriterionAllVersions> getAllCqms() {
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
