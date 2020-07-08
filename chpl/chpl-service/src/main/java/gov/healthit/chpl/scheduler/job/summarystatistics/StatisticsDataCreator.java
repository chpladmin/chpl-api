package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;

public abstract class StatisticsDataCreator {

    public boolean includeListingBasedOnStatus(CertifiedProductDetailsDTO listing, List<CertificationStatusType> statuses) {
        if (Objects.isNull(statuses)) {
            return true;
        } else {
            return statuses.stream()
                    .filter(status -> status.getName().equals(listing.getCertificationStatusName()))
                    .findAny()
                    .isPresent();
        }
    }

    public boolean includeListingBasedOnEdition(CertifiedProductDetailsDTO listing, EditionCriteria listingsToInclude) {
        switch (listingsToInclude) {
        case ALL :
            return true;
        case EDITION_2011 :
            return listing.getCertificationEditionId().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2011.getId());
        case EDITION_2014 :
            return listing.getCertificationEditionId().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getId());
        case EDITION_2015_CURES :
            return listing.getCertificationEditionId().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                    && listing.getCuresUpdate();
        case EDITION_2015_NON_CURES :
            return listing.getCertificationEditionId().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                    && !listing.getCuresUpdate();
        case EDITION_2015_CURES_N_NON_CURES :
            return listing.getCertificationEditionId().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId());
        default :
            return false;
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

}
