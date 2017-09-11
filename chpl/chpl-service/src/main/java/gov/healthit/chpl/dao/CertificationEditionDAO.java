package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationEditionDTO;

public interface CertificationEditionDAO {
	
	public void create(CertificationEditionDTO dto) throws EntityCreationException, EntityRetrievalException;

	public void update(CertificationEditionDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id);
	
	public List<CertificationEditionDTO> findAll();
	public List<CertificationEditionDTO> getEditions(List<Long> listingIds);
	public CertificationEditionDTO getById(Long id) throws EntityRetrievalException;
	public CertificationEditionDTO getByYear(String year);
	
}
