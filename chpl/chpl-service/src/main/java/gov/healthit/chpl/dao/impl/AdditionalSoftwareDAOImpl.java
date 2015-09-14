package gov.healthit.chpl.dao.impl;

import java.util.List;

import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.entity.AdditionalSoftwareEntity;

public class AdditionalSoftwareDAOImpl extends BaseDAOImpl implements AdditionalSoftwareDAO {

	@Override
	public void create(AdditionalSoftwareDTO dto)
			throws EntityCreationException {
		
		AdditionalSoftwareEntity entity = null;
		try {
			if (dto.getId() != null){
				entity = this.getEntityById(dto.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {
		
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AdditionalSoftwareDTO> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AdditionalSoftwareDTO> findByAdditionalSoftwareId(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdditionalSoftwareDTO getById(Long id)
			throws EntityRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(AdditionalSoftwareDTO dto)
			throws EntityRetrievalException {
		// TODO Auto-generated method stub
		
	}
	
	

}
