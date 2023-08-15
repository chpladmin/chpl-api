package gov.healthit.chpl.certificationCriteria;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("certificationCriteriaManager")
public class CertificationCriteriaManager {
    private CertificationCriterionDAO certificationCriterionDao;

    @Autowired
    public CertificationCriteriaManager(CertificationCriterionDAO certificationCriterionDao) {
        this.certificationCriterionDao = certificationCriterionDao;
    }

    @Transactional
    @Cacheable(value = CacheNames.CERTIFICATION_CRITERIA)
    public List<CertificationCriterion> getAll() {
        LOGGER.debug("Getting all criterion from the database (not cached).");

        return this.certificationCriterionDao.findAll().stream()
                .collect(Collectors.toList());
    }
}
