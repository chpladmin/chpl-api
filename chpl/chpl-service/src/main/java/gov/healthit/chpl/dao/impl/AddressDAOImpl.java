package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.entity.AddressEntity;


@Repository("addressDao")
public class AddressDAOImpl extends BaseDAOImpl implements AddressDAO {
	private static final Logger logger = LogManager.getLogger(AddressDAOImpl.class);

	@Override
	public AddressEntity create(AddressDTO dto) throws EntityCreationException, EntityRetrievalException {
		
		AddressEntity toInsert = new AddressEntity();
		toInsert.setStreetLineOne(dto.getStreetLineOne());
		toInsert.setStreetLineTwo(dto.getStreetLineTwo());
		toInsert.setCity(dto.getCity());
		toInsert.setState(dto.getState());
		toInsert.setZipcode(dto.getZipcode());
		toInsert.setCountry(dto.getCountry());

		if(dto.getDeleted() != null) {
			toInsert.setDeleted(dto.getDeleted());
		} else {
			toInsert.setDeleted(false);
		}
		
		if(dto.getLastModifiedUser() != null) {
			toInsert.setLastModifiedUser(dto.getLastModifiedUser());
		} else {
			toInsert.setLastModifiedUser(Util.getCurrentUser().getId());
		}
		if(dto.getLastModifiedDate() != null) {
			toInsert.setLastModifiedDate(dto.getLastModifiedDate());
		} else {
			toInsert.setLastModifiedDate(new Date());
		}
		if(dto.getCreationDate() != null) {
			toInsert.setCreationDate(dto.getCreationDate());
		} else {
			toInsert.setCreationDate(new Date());
		}		
		
		entityManager.persist(toInsert);
		entityManager.flush();
		return toInsert;
	}

	@Override
	@Transactional
	public AddressEntity update(AddressDTO addressDto) throws EntityRetrievalException {
		AddressEntity address = this.getEntityById(addressDto.getId());

		address.setStreetLineTwo(addressDto.getStreetLineTwo());
		if(addressDto.getStreetLineOne() != null) {
			address.setStreetLineOne(addressDto.getStreetLineOne());
		}
		
		if(addressDto.getCity() != null) {
			address.setCity(addressDto.getCity());
		}
		
		if(addressDto.getState() != null) {
			address.setState(addressDto.getState());
		}
		
		if(addressDto.getZipcode() != null) {
			address.setZipcode(addressDto.getZipcode());
		}
		
		if(addressDto.getCountry() != null) {
			address.setCountry(addressDto.getCountry());
		}
		
		if(addressDto.getDeleted() != null) {
			address.setDeleted(addressDto.getDeleted());
		}
		
		if(addressDto.getLastModifiedUser() != null) {
			address.setLastModifiedUser(addressDto.getLastModifiedUser());
		} else {
			address.setLastModifiedUser(Util.getCurrentUser().getId());
		}
		if(addressDto.getLastModifiedDate() != null) {
			address.setLastModifiedDate(addressDto.getLastModifiedDate());
		} else {
			address.setLastModifiedDate(new Date());
		}
		
		entityManager.merge(address);
		return address;
	}

	@Override
	@Transactional
	public void delete(Long id) throws EntityRetrievalException {
		AddressEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.merge(toDelete);
		}
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
	public AddressDTO getById(Long id) throws EntityRetrievalException {
		
		AddressDTO dto = null;
		AddressEntity ae = this.getEntityById(id);
		
		if (ae != null){
			dto = new AddressDTO(ae); 
		}
		return dto;
	}

	@Override
	public AddressDTO getByValues(AddressDTO address) {
		AddressEntity ae = this.searchEntities(address);
		if(ae == null) {
			return null;
		}
		return new AddressDTO(ae);
	}
	
	@Override
	public AddressEntity mergeAddress(AddressDTO addressDto) throws EntityRetrievalException, EntityCreationException {
		AddressEntity address = null;
		if(addressDto.getId() != null) {
			//update the address
			AddressDTO toUpdate = getById(addressDto.getId());
			if(toUpdate != null) {
				toUpdate.setStreetLineOne(addressDto.getStreetLineOne());
				toUpdate.setStreetLineTwo(addressDto.getStreetLineTwo());
				toUpdate.setCity(addressDto.getCity());
				toUpdate.setState(addressDto.getState());
				toUpdate.setZipcode(addressDto.getZipcode());
				toUpdate.setCountry(addressDto.getCountry());
				address = update(toUpdate);
			}
		} else {
			address = getEntityByValues(addressDto);
		}
		
		if(address == null) {
			//if we didn't find it, create and save a new address entity before setting it on the developer
			address = create(addressDto);
		}
		
		return address;
	}
	
	private List<AddressEntity> findAllEntities() {
		Query query = entityManager.createQuery("SELECT a from AddressEntity a where (NOT a.deleted = true)");
		return query.getResultList();
	}
	
	public AddressEntity getEntityById(Long id) throws EntityRetrievalException {
		AddressEntity entity = null;
		
		Query query = entityManager.createQuery( "from AddressEntity a where (NOT deleted = true) AND (address_id = :entityid) ", AddressEntity.class );
		query.setParameter("entityid", id);
		List<AddressEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate address id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private AddressEntity getEntityByValues(AddressDTO address) {
		return this.searchEntities(address);
	}
	
	private AddressEntity searchEntities(AddressDTO toSearch) {
		AddressEntity entity = null;
		
		String addressQuery = "from AddressEntity a where (NOT deleted = true) "
				+ " AND (street_line_1 = :line1) "
				+ " AND (city = :city)"
				+ " AND (state = :state)"
				+ " AND (zipcode = :zipcode)"
				+ " AND (country = :country)";
		if(toSearch.getStreetLineTwo() != null) {
			addressQuery += " AND (street_line_2 = :line2)";
		} else {
			addressQuery += " AND (street_line_2 IS NULL)";
		}
		Query query = entityManager.createQuery(addressQuery, AddressEntity.class );
		query.setParameter("line1", toSearch.getStreetLineOne());
		query.setParameter("city", toSearch.getCity());
		query.setParameter("state", toSearch.getState());
		query.setParameter("zipcode", toSearch.getZipcode());
		query.setParameter("country", toSearch.getCountry());
		if(toSearch.getStreetLineTwo() != null) {
			query.setParameter("line2", toSearch.getStreetLineTwo());
		}
		
		List<AddressEntity> result = query.getResultList();
		
		if (result.size() >= 1){
			entity = result.get(0);
		} 
		
		return entity;
	}
}
