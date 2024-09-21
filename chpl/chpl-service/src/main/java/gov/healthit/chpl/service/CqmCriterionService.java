package gov.healthit.chpl.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.domain.CQMCriterion;

@Component
public class CqmCriterionService {
    public static final String CMS_ID_BEGIN = "CMS";
    private CQMCriterionDAO cqmCriterionDao;

    @Autowired
    public CqmCriterionService(CQMCriterionDAO cqmCriterionDao) {
        this.cqmCriterionDao = cqmCriterionDao;
    }

    public List<CQMCriterion> getAllCmsCqms() {
        List<CQMCriterion> cmsCqmsWithVersions = cqmCriterionDao.findAll();
        cmsCqmsWithVersions = cmsCqmsWithVersions.stream()
                .filter(cqm -> !StringUtils.isEmpty(cqm.getCmsId()) && cqm.getCmsId().startsWith(CMS_ID_BEGIN))
                .collect(Collectors.toList());
        return cmsCqmsWithVersions;
    }

    public List<CQMCriterion> getAllNqfCqms() {
        List<CQMCriterion> nqfCqms = cqmCriterionDao.findAll();
        nqfCqms = nqfCqms.stream()
                .filter(cqm -> StringUtils.isEmpty(cqm.getCmsId()) || !cqm.getCmsId().startsWith(CMS_ID_BEGIN))
                .collect(Collectors.toList());
        return nqfCqms;
    }

    //group all cqms by CMS ID - some in each group may have different titles and descriptions
    public Map<String, List<CQMCriterion>> getCqmsGroupedByCmsId() {
        Map<String, List<CQMCriterion>> cqmsGroupedByCmsId = new HashMap<String, List<CQMCriterion>>();
        Set<String> distinctCmsIds = getAllCmsCqms().stream()
            .map(cqm -> cqm.getCmsId())
            .collect(Collectors.toSet());
        distinctCmsIds.stream()
            .forEach(cmsId -> cqmsGroupedByCmsId.put(cmsId, getAllCqmsByCmsId(cmsId)));
        return cqmsGroupedByCmsId;
    }

    private List<CQMCriterion> getAllCqmsByCmsId(String cmsId) {
        return getAllCmsCqms().stream()
            .filter(cqm -> cqm.getCmsId().equals(cmsId))
            .collect(Collectors.toList());
    }

    public List<CQMCriterion> getAllCmsCqmsMostRecentVersionOnly() {
        Map<String, List<CQMCriterion>> cqmsGroupedByCmsId = getCqmsGroupedByCmsId();
        return cqmsGroupedByCmsId.keySet().stream()
                .map(cmsId -> getCqmForMostRecentVersion(cqmsGroupedByCmsId.get(cmsId)))
                .collect(Collectors.toList());
    }

    private CQMCriterion getCqmForMostRecentVersion(List<CQMCriterion> cqmsWithCmsId) {
        int maxVersion = cqmsWithCmsId.stream()
                .map(cqmCrit -> cqmCrit.getCqmVersion().substring(1))
                .mapToInt(Integer::valueOf)
                .max()
                .orElse(0);
        return cqmsWithCmsId.stream()
                    .filter(cqmCrit -> cqmCrit.getCqmVersion().endsWith(maxVersion + ""))
                    .findAny()
                    .orElse(cqmsWithCmsId.get(cqmsWithCmsId.size() - 1));
    }
}
