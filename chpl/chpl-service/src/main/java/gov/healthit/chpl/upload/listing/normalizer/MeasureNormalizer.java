package gov.healthit.chpl.upload.listing.normalizer;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private MeasureDAO measureDao;
    private ListingMeasureDAO listingMeasureDao;
    private CertificationCriterionService criteriaService;

    private Set<MeasureType> measureTypes;

    @Autowired
    public MeasureNormalizer(MeasureDAO measureDao,
            ListingMeasureDAO listingMeasureDao,
            CertificationCriterionService criteriaService) {
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
                .filter(listingMeasure -> listingMeasure.getMeasure() != null)
                .forEach(listingMeasure -> {
                    populateMeasureWithMipsValues(listingMeasure);
                    populateAssociatedCriteriaFields(listingMeasure);
                    populateMissingAssociatedCriteria(listingMeasure);
                });
        }
    }

    private void populateMeasureType(ListingMeasure listingMeasure) {
        if (listingMeasure.getMeasureType() != null
                && !StringUtils.isEmpty(listingMeasure.getMeasureType().getName())) {
            MeasureType foundType = getMeasureTypeByName(listingMeasure.getMeasureType().getName());
            if (foundType != null) {
                listingMeasure.getMeasureType().setName(listingMeasure.getMeasureType().getName().toUpperCase());
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
        String formattedCriterionNumber = criteriaService.coerceToCriterionNumberFormat(criterion.getNumber());
        if (!formattedCriterionNumber.equals(criterion.getNumber())) {
            LOGGER.debug("Formatted " + criterion.getNumber() + " as " + formattedCriterionNumber);
            criterion.setNumber(formattedCriterionNumber);
        }

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

    private void populateMissingAssociatedCriteria(ListingMeasure listingMeasure) {
        associateAllowedCriteriaIfCriteriaSelectionNotRequired(listingMeasure);
        associateCuresAndOriginalCriteria(listingMeasure);
    }

    private void associateAllowedCriteriaIfCriteriaSelectionNotRequired(ListingMeasure listingMeasure) {
        if (BooleanUtils.isFalse(listingMeasure.getMeasure().getRequiresCriteriaSelection())) {
            //if the user can't select the criteria add all the allowed as associated
            listingMeasure.getMeasure().getAllowedCriteria().stream()
                .forEach(allowedCriterion -> listingMeasure.getAssociatedCriteria().add(allowedCriterion));
        }
    }

    private void associateCuresAndOriginalCriteria(ListingMeasure listingMeasure) {
        if (!CollectionUtils.isEmpty(listingMeasure.getAssociatedCriteria())) {
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
}
