package gov.healthit.chpl.certifiedproduct.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;

@Component
public class ListingMeasuresService {

    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private ListingMeasureDAO listingMeasureDAO;

    @Autowired
    public ListingMeasuresService(CertifiedProductSearchResultDAO certifiedProductSearchResultDAO,
            ListingMeasureDAO listingMeasureDAO) {

        this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
        this.listingMeasureDAO = listingMeasureDAO;
    }

    public List<ListingMeasure> getCertifiedProductMeasures(Long listingId, Boolean checkIfListingExists) throws EntityRetrievalException {
        //This is used when called from the controller to ensure that the listing exists
        if (checkIfListingExists) {
            certifiedProductSearchResultDAO.getById(listingId);
        }
        return listingMeasureDAO.getMeasuresByListingId(listingId);
    }

}
