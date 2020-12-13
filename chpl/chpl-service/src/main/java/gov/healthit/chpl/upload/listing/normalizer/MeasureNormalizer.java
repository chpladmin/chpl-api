package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.listing.measure.MeasureDAO;

@Component
public class MeasureNormalizer {
    private MacraMeasureDAO legacyMacraMeasureDao;
    private MeasureDAO measureDao;
    private ListingMeasureDAO listingMeasureDao;

    private Set<MeasureType> measureTypes;

    @Autowired
    public MeasureNormalizer(MacraMeasureDAO legacyMacraMeasureDao,
            MeasureDAO measureDao,
            ListingMeasureDAO listingMeasureDao) {
        this.legacyMacraMeasureDao = legacyMacraMeasureDao;
        this.measureDao = measureDao;
        this.listingMeasureDao = listingMeasureDao;
    }

    @PostConstruct
    public void initialize() {
        measureTypes = listingMeasureDao.getMeasureTypes();
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getMeasures() != null && listing.getMeasures().size() > 0) {
            listing.getMeasures().stream()
                .forEach(listingMeasure -> {
                    populateMeasureType(listingMeasure);
                    populateMeasure(listingMeasure);
                });
            List<ListingMeasure> combinedListingMeasures = new ArrayList<ListingMeasure>();
            combineListingMeasures(combinedListingMeasures, listing.getMeasures());
            combinedListingMeasures.stream()
                .filter(listingMeasure -> listingMeasure.getMeasure() != null && listingMeasure.getMeasure().getId() != null)
                .forEach(listingMeasure -> setAssociatedCriteriaIfCriteriaSelectionNotRequired(listingMeasure));
            listing.setMeasures(combinedListingMeasures);
        }
    }

    private void populateMeasureType(ListingMeasure listingMeasure) {
        if (listingMeasure.getMeasureType() != null
                && !StringUtils.isEmpty(listingMeasure.getMeasureType().getName())) {
            MeasureType foundType = getMeasureTypeByName(listingMeasure.getMeasureType().getName());
            if (foundType != null) {
                listingMeasure.getMeasureType().setId(foundType.getId());
            }
        }
    }

    private void populateMeasure(ListingMeasure listingMeasure) {
        if (listingMeasure.getMeasure() != null
                && !StringUtils.isEmpty(listingMeasure.getMeasure().getLegacyMacraMeasureValue())
                && listingMeasure.getAssociatedCriteria() != null
                && listingMeasure.getAssociatedCriteria().size() > 0) {
            //there should only be one associated criterion per measure at this point
            //when it's just been parsed with the upload handler
            Long macraMeasureId = legacyMacraMeasureDao.getMacraMeasureIdByCriterionAndValue(
                    listingMeasure.getAssociatedCriteria().iterator().next().getId(),
                    listingMeasure.getMeasure().getLegacyMacraMeasureValue());
            if (macraMeasureId != null) {
                Measure mappedMeasure = measureDao.getMeasureByMacraMeasureId(macraMeasureId);
                if (mappedMeasure != null) {
                    listingMeasure.setMeasure(mappedMeasure);
                }
            }
        }
    }

    private void setAssociatedCriteriaIfCriteriaSelectionNotRequired(ListingMeasure listingMeasure) {
        if (!listingMeasure.getMeasure().getRequiresCriteriaSelection()) {
            //if the user can't select the criteria add all the allowed as associated
            listingMeasure.getMeasure().getAllowedCriteria().stream()
                .forEach(allowedCriterion -> listingMeasure.getAssociatedCriteria().add(allowedCriterion));
        }
    }

    private MeasureType getMeasureTypeByName(String name) {
        return measureTypes.stream()
            .filter(type -> type.getName().equalsIgnoreCase(name))
            .findAny().get();
    }

    private void combineListingMeasures(List<ListingMeasure> combinedListingMeasures, List<ListingMeasure> currListingMeasures) {
        currListingMeasures.stream().forEach(currListingMeasure -> {
            if (containsMeasure(combinedListingMeasures, currListingMeasure)) {
                addCriteriaToCombinedListingMeasure(combinedListingMeasures, currListingMeasure);
            } else {
                combinedListingMeasures.add(currListingMeasure);
            }
        });
    }

    private boolean containsMeasure(List<ListingMeasure> combinedListingMeasures, ListingMeasure currListingMeasure) {
        return combinedListingMeasures.stream()
            .filter(listingMeasure -> areMeasuresEqual(listingMeasure, currListingMeasure))
            .findAny().isPresent();
    }

    private void addCriteriaToCombinedListingMeasure(List<ListingMeasure> combinedListingMeasures, ListingMeasure currListingMeasure) {
        combinedListingMeasures.stream()
            .filter(listingMeasure -> areMeasuresEqual(listingMeasure, currListingMeasure))
            .forEach(listingMeasure -> {
                listingMeasure.getAssociatedCriteria().addAll(currListingMeasure.getAssociatedCriteria());
            });
    }

    private boolean areMeasuresEqual(ListingMeasure listingMeasure1, ListingMeasure listingMeasure2) {
        return ObjectUtils.allNotNull(listingMeasure1.getMeasure(), listingMeasure2.getMeasure(),
                listingMeasure1.getMeasure().getId(), listingMeasure2.getMeasure().getId(),
                listingMeasure1.getMeasureType(), listingMeasure2.getMeasureType(),
                listingMeasure1.getMeasureType().getId(), listingMeasure2.getMeasureType().getId())
            && listingMeasure1.getMeasure().getId().equals(listingMeasure2.getMeasure().getId())
            && listingMeasure1.getMeasureType().getId().equals(listingMeasure2.getMeasureType().getId());
    }
}
