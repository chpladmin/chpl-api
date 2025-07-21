package gov.healthit.chpl.listing.measure;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class MeasureManager {

    private MeasureDAO measureDao;
    private ListingMeasureDAO listingMeasureDao;

    @Autowired
    public MeasureManager(MeasureDAO measureDao,
            ListingMeasureDAO listingMeasureDao) {
        this.measureDao = measureDao;
        this.listingMeasureDao = listingMeasureDao;
    }

    @Transactional
    @Cacheable(value = CacheNames.MEASURES)
    public Set<Measure> getAll() {
        return measureDao.findAll();
    }

    @Transactional
    @Cacheable(value = CacheNames.MEASURE_TYPES)
    public Set<MeasureType> getMeasureTypes() {
        return listingMeasureDao.getMeasureTypes();
    }
}
