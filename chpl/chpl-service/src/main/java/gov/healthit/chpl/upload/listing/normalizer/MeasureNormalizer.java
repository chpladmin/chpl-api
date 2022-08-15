package gov.healthit.chpl.upload.listing.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.listing.measure.MeasureDAO;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class MeasureNormalizer {
    private MacraMeasureDAO legacyMacraMeasureDao;
    private MeasureDAO measureDao;
    private ListingMeasureDAO listingMeasureDao;
    private CertificationCriterionService criteriaService;

    private Set<MeasureType> measureTypes;

    @Autowired
    public MeasureNormalizer(MacraMeasureDAO legacyMacraMeasureDao,
            MeasureDAO measureDao,
            ListingMeasureDAO listingMeasureDao,
            CertificationCriterionService criteriaService) {
        this.legacyMacraMeasureDao = legacyMacraMeasureDao;
        this.measureDao = measureDao;
        this.listingMeasureDao = listingMeasureDao;
        this.criteriaService = criteriaService;
    }

    @PostConstruct
    public void initialize() {
        measureTypes = listingMeasureDao.getMeasureTypes();
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getMeasures() != null && listing.getMeasures().size() > 0) {
            listing.getMeasures().stream()
                .forEach(listingMeasure -> populateMeasureType(listingMeasure));
            listing.getMeasures().stream()
                .filter(listingMeasure -> listingMeasure.getMeasure() != null
                    && StringUtils.isEmpty(listingMeasure.getMeasure().getLegacyMacraMeasureValue()))
                .forEach(listingMeasure -> {
                    populateMeasureWithMipsValues(listingMeasure);
                    populateAssociatedCriteriaFields(listingMeasure);
                });
            //if we ever get rid of legacy macra measures we can remove this
            listing.getMeasures().stream()
                .filter(listingMeasure -> listingMeasure.getMeasure() != null
                    && !StringUtils.isEmpty(listingMeasure.getMeasure().getLegacyMacraMeasureValue()))
                .forEach(listingMeasure -> populateMeasureWithLegacyValues(listingMeasure));

            List<ListingMeasure> combinedListingMeasures = new ArrayList<ListingMeasure>();
            combineListingMeasures(combinedListingMeasures, listing.getMeasures());
            combinedListingMeasures.stream()
                .filter(listingMeasure -> listingMeasure.getMeasure() != null && listingMeasure.getMeasure().getId() != null)
                .forEach(listingMeasure -> populateMissingAssociatedCriteria(listingMeasure));
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

    private void populateMeasureWithMipsValues(ListingMeasure listingMeasure) {
        if (listingMeasure.getMeasure() != null
                && listingMeasure.getMeasure().getId() == null
                && listingMeasure.getMeasure().getDomain() != null
                && !StringUtils.isEmpty(listingMeasure.getMeasure().getDomain().getName())
                && !StringUtils.isEmpty(listingMeasure.getMeasure().getAbbreviation())) {
            Measure foundMeasure = measureDao.getByDomainAndAbbreviation(
                    listingMeasure.getMeasure().getDomain().getName(), listingMeasure.getMeasure().getAbbreviation());
            if (foundMeasure != null) {
                listingMeasure.setMeasure(foundMeasure);
            }
        }
    }

    private void populateAssociatedCriteriaFields(ListingMeasure listingMeasure) {
        if (!CollectionUtils.isEmpty(listingMeasure.getAssociatedCriteria())) {
            listingMeasure.getAssociatedCriteria().stream()
                .forEach(criterion -> populateAssociatedCriterionFields(criterion));
        }
    }

    private void populateAssociatedCriterionFields(CertificationCriterion criterion) {
        List<CertificationCriterion> matchingCriteria = criteriaService.getByNumber(criterion.getNumber());
        if (!CollectionUtils.isEmpty(matchingCriteria)) {
            CertificationCriterion matchingCriterion = matchingCriteria.get(0);
            criterion.setId(matchingCriterion.getId());
            criterion.setCertificationEdition(matchingCriterion.getCertificationEdition());
            criterion.setCertificationEditionId(matchingCriterion.getCertificationEditionId());
            criterion.setDescription(matchingCriterion.getDescription());
            criterion.setRemoved(matchingCriterion.getRemoved());
            criterion.setTitle(matchingCriterion.getTitle());
        }
    }

    private void populateMeasureWithLegacyValues(ListingMeasure listingMeasure) {
        if (listingMeasure.getMeasure() != null
                && listingMeasure.getMeasure().getId() == null
                && !StringUtils.isEmpty(listingMeasure.getMeasure().getLegacyMacraMeasureValue())
                && listingMeasure.getAssociatedCriteria() != null
                && listingMeasure.getAssociatedCriteria().size() > 0) {
            //there should only be one associated criterion per measure at this point
            //when it's just been parsed with the criteria-level upload handler
            CertificationCriterion associatedCriterion = listingMeasure.getAssociatedCriteria().iterator().next();
            if (associatedCriterion.getId() != null) {
                Long macraMeasureId = legacyMacraMeasureDao.getMacraMeasureIdByCriterionAndValue(
                        associatedCriterion.getId(),
                        listingMeasure.getMeasure().getLegacyMacraMeasureValue());
                if (macraMeasureId != null) {
                    Measure mappedMeasure = measureDao.getMeasureByMacraMeasureId(macraMeasureId);
                    if (mappedMeasure != null) {
                        listingMeasure.setMeasure(mappedMeasure);
                    }
                }
            } else {
                LOGGER.warn("There was no criterion ID found for criterion " + associatedCriterion.getNumber() + " so the legacy Macra Measure cannot be mapped.");
            }
        }
    }

    private void populateMissingAssociatedCriteria(ListingMeasure listingMeasure) {
        associateAllowedCriteriaIfCriteriaSelectionNotRequired(listingMeasure);
        associateCuresAndOriginalCriteria(listingMeasure);
    }

    private void associateAllowedCriteriaIfCriteriaSelectionNotRequired(ListingMeasure listingMeasure) {
        if (!listingMeasure.getMeasure().getRequiresCriteriaSelection()) {
            //if the user can't select the criteria add all the allowed as associated
            listingMeasure.getMeasure().getAllowedCriteria().stream()
                .forEach(allowedCriterion -> listingMeasure.getAssociatedCriteria().add(allowedCriterion));
        }
    }

    private void associateCuresAndOriginalCriteria(ListingMeasure listingMeasure) {
        Set<CertificationCriterion> associatedCriteriaCopy = listingMeasure.getAssociatedCriteria().stream().collect(Collectors.toSet());

        listingMeasure.getAssociatedCriteria().stream()
            .forEach(associatedCriterion -> {
                List<CertificationCriterion> criteriaWithNumber = criteriaService.getByNumber(associatedCriterion.getNumber());
                if (criteriaWithNumber != null && criteriaWithNumber.size() > 1) {
                    associatedCriteriaCopy.addAll(criteriaWithNumber);
                }
            });
        listingMeasure.setAssociatedCriteria(associatedCriteriaCopy);
    }

    private MeasureType getMeasureTypeByName(String name) {
        Optional<MeasureType> foundMeasureType = measureTypes.stream()
            .filter(type -> type.getName().equalsIgnoreCase(name))
            .findAny();
        if (foundMeasureType.isPresent()) {
            return foundMeasureType.get();
        }
        return null;
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
        if (ObjectUtils.allNotNull(listingMeasure1.getMeasure(), listingMeasure2.getMeasure(),
                listingMeasure1.getMeasure().getId(), listingMeasure2.getMeasure().getId(),
                listingMeasure1.getMeasureType(), listingMeasure2.getMeasureType(),
                listingMeasure1.getMeasureType().getId(), listingMeasure2.getMeasureType().getId())) {
            return listingMeasure1.getMeasure().getId().equals(listingMeasure2.getMeasure().getId())
                    && listingMeasure1.getMeasureType().getId().equals(listingMeasure2.getMeasureType().getId());
        } else if (ObjectUtils.allNotNull(listingMeasure1.getMeasure(), listingMeasure2.getMeasure(),
                listingMeasure1.getMeasureType(), listingMeasure2.getMeasureType(),
                listingMeasure1.getMeasureType().getId(), listingMeasure2.getMeasureType().getId(),
                listingMeasure1.getMeasure().getLegacyMacraMeasureValue(), listingMeasure2.getMeasure().getLegacyMacraMeasureValue())) {
            return listingMeasure1.getMeasure().getLegacyMacraMeasureValue().equals(listingMeasure2.getMeasure().getLegacyMacraMeasureValue())
                    && listingMeasure1.getMeasureType().getId().equals(listingMeasure2.getMeasureType().getId());
        }
        return false;
    }
}
