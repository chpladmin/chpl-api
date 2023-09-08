package gov.healthit.chpl.optionalStandard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardWithCriteria;

@Component("optionalStandardManager")
public class OptionalStandardManager {

    private OptionalStandardDAO optionalStandardDao;

    @Autowired
    public OptionalStandardManager(OptionalStandardDAO optionalStandardDao) {
        this.optionalStandardDao = optionalStandardDao;
    }

    @Cacheable(value = CacheNames.OPTIONAL_STANDARDS)
    public List<OptionalStandardWithCriteria> getAll() {
        return optionalStandardDao.getAll();
    }
}
