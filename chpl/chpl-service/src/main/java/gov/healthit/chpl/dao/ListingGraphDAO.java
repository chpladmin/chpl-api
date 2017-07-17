package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ListingToListingMapDTO;

public interface ListingGraphDAO {
	public ListingToListingMapDTO createListingMap(ListingToListingMapDTO toCreate) throws EntityCreationException;
	public void deleteListingMap(ListingToListingMapDTO toDelete);
	public Integer getLargestIcs(List<Long> listingIds);
	public List<CertifiedProductDetailsDTO> getParents(Long listingId);
	public List<CertifiedProductDetailsDTO> getChildren(Long listingId);
	public ListingToListingMapDTO getListingMap(Long childId, Long parentId);
}
