package gov.healthit.chpl.certifiedproduct.service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.util.CertificationCriterionComparator;
import lombok.NoArgsConstructor;

@Component
public class ListingMeasuresService {

    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private ListingMeasureDAO listingMeasureDAO;
    private CertificationCriterionComparator criterionComparator;

    private ListingMeasureComparator measureComparator;

    @Autowired
    public ListingMeasuresService(CertifiedProductSearchResultDAO certifiedProductSearchResultDAO,
            ListingMeasureDAO listingMeasureDAO, CertificationCriterionComparator criterionComparator) {

        this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
        this.listingMeasureDAO = listingMeasureDAO;
        this.criterionComparator = criterionComparator;
        this.measureComparator = new ListingMeasureComparator();
    }

    public List<ListingMeasure> getCertifiedProductMeasures(Long listingId, Boolean checkIfListingExists) throws EntityRetrievalException {
        //This is used when called from the controller to ensure that the listing exists
        if (checkIfListingExists) {
            certifiedProductSearchResultDAO.getById(listingId);
        }
        List<ListingMeasure> listingMeasures = listingMeasureDAO.getMeasuresByListingId(listingId);
        listingMeasures.stream()
            .forEach(listingMeasure -> {
                sortAssociatedCriteria(listingMeasure);
                sortAllowedCriteria(listingMeasure);
            });
        listingMeasures.sort(measureComparator);
        return listingMeasures;
    }

    private void sortAssociatedCriteria(ListingMeasure listingMeasure) {
        Set<CertificationCriterion> sortedAssociatedCriteria = listingMeasure.getAssociatedCriteria().stream()
                .sorted(criterionComparator)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        listingMeasure.setAssociatedCriteria(sortedAssociatedCriteria);
    }

    private void sortAllowedCriteria(ListingMeasure listingMeasure) {
        Set<CertificationCriterion> sortedAllowedCriteria = listingMeasure.getMeasure().getAllowedCriteria().stream()
                .sorted(criterionComparator)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        listingMeasure.getMeasure().setAllowedCriteria(sortedAllowedCriteria);
    }

    @NoArgsConstructor
    private class ListingMeasureComparator implements Comparator<ListingMeasure> {
        private boolean descending = false;

        @Override
        public int compare(ListingMeasure measure1, ListingMeasure measure2) {
            if (ObjectUtils.anyNull(measure1.getMeasure(), measure2.getMeasure())
                    || StringUtils.isAnyEmpty(measure1.getMeasure().getAbbreviation(),
                            measure2.getMeasure().getAbbreviation())) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (measure1.getMeasure().getAbbreviation().compareTo(measure2.getMeasure().getAbbreviation())) * sortFactor;
        }
    }
}
