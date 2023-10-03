package gov.healthit.chpl.conformanceMethod;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;

@Component("conformanceMethodManager")
public class ConformanceMethodManager {

    private ConformanceMethodDAO conformanceMethodDao;

    @Autowired
    public ConformanceMethodManager(ConformanceMethodDAO conformanceMethodDao) {
        this.conformanceMethodDao = conformanceMethodDao;
    }

    @Transactional
    @Cacheable(value = CacheNames.CONFORMANCE_METHODS)
    public List<ConformanceMethod> getAll() {
        return conformanceMethodDao.getAllWithCriteria();
    }
}
