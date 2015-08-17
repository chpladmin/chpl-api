package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.entity.VendorEntity;

public class AddressDAOImpl extends BaseDAOImpl implements AddressDAO {

	@Override
	public void create(AddressDTO dto) throws EntityCreationException, EntityRetrievalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(AddressDTO dto) throws EntityRetrievalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AddressDTO> findAll() {
		List<AddressEntity> result = this.findAllEntities();
		List<AddressDTO> dtos = new ArrayList<AddressDTO>(result.size());
		for(AddressEntity entity : result) {
			dtos.add(new AddressDTO(entity));
		}
		return dtos;
	}

	@Override
	public List<AddressEntity> findAllEntities() {
		Query query = entityManager.createQuery("SELECT a from Address where (NOT v.deleted = true)");
		return query.getResultList();
	}
	
	@Override
	public AddressDTO getById(Long id) throws EntityRetrievalException {
		AddressEntity ae = this.getEntityById(id);
		return new AddressDTO(ae);
	}

	@Override
	public AddressEntity getEntityById(Long id) throws EntityRetrievalException {
		AddressEntity entity = null;
		
		Query query = entityManager.createQuery( "from Address where (NOT deleted = true) AND (address_id = :entityid) ", AddressEntity.class );
		query.setParameter("entityid", id);
		List<AddressEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate address id in database.");
		}
		
		if (result.size() < 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	@Override
	public AddressDTO search(String line1, String line2, String city, String region, String country) {
		AddressDTO result = null;
		AddressEntity ae = getEntityByValues(line1, line2, city, region, country);
		if(ae != null) {
			result = new AddressDTO(ae);
		}
		return result;
	}
	
	@Override
	public AddressEntity searchForEntity(String line1, String line2, String city, String region, String country) {
		return getEntityByValues(line1, line2, city, region, country);
	}
	
	private AddressEntity getEntityByValues(String line1, String line2, String city, String region, String country) {		
		Query query = entityManager.createQuery( "SELECT a from Address where (NOT v.deleted = true) "
				+ "AND (street_line_1 = :line1) "
				+ "AND (street_line_2 = :line2) "
				+ "AND (city = :city) "
				+ "AND (region = :region)"
				+ "AND (country = :country)", AddressEntity.class );
		query.setParameter("line1", line1)
			 .setParameter("line2", line2)
			 .setParameter("city", city)
			 .setParameter("region", region)
			 .setParameter("country", country);
		
		List<AddressEntity> result = query.getResultList();
		if(result.size() == 0) {
			return null;
		}
		return result.get(0);
	}
}
