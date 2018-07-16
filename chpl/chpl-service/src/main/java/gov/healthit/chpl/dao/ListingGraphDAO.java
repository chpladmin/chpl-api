package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ListingToListingMapDTO;
import gov.healthit.chpl.exception.EntityCreationException;

public interface ListingGraphDAO {
    ListingToListingMapDTO createListingMap(ListingToListingMapDTO toCreate) throws EntityCreationException;

    void deleteListingMap(ListingToListingMapDTO toDelete);

    Integer getLargestIcs(List<Long> listingIds);

    List<CertifiedProductDetailsDTO> getParents(Long listingId);

    List<CertifiedProductDetailsDTO> getChildren(Long listingId);

    ListingToListingMapDTO getListingMap(Long childId, Long parentId);
}
