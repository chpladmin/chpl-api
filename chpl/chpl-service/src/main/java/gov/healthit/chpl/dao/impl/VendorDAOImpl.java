package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.VendorEntity;

@Repository("vendorDAO")
public class VendorDAOImpl extends BaseDAOImpl implements VendorDAO {

	private static final Logger logger = LogManager.getLogger(VendorDAOImpl.class);
	@Autowired AddressDAO addressDao;
	
	@Override
	public void create(VendorDTO dto) throws EntityCreationException, EntityRetrievalException {
		
		VendorEntity entity = null;
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
			entity = new VendorEntity();

			if(dto.getAddress() != null)
			{
				entity.setAddress(mergeVendorAddress(dto.getAddress()));
			}
			
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setId(dto.getId());
			entity.setName(dto.getName());
			entity.setWebsite(dto.getWebsite());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
			create(entity);	
		}	
	}

	@Override
	public void update(VendorDTO dto) throws EntityRetrievalException {
		
		VendorEntity entity = this.getEntityById(dto.getId());
		
		if(dto.getAddress() != null)
		{
			try {
				entity.setAddress(mergeVendorAddress(dto.getAddress()));
			} catch(EntityCreationException ex) {
				logger.error("Could not create new address in the database.", ex);
				entity.setAddress(null);
			}
		}
		
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setWebsite(dto.getWebsite());
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
		update(entity);
	}

	@Override
	public void delete(Long id) {
		Query query = entityManager.createQuery("UPDATE VendorEntity SET deleted = true WHERE vendor_id = :entityid");
		query.setParameter("entityid", id);
		query.executeUpdate();
	}

	@Override
	public List<VendorDTO> findAll() {
		
		List<VendorEntity> entities = getAllEntities();
		List<VendorDTO> dtos = new ArrayList<>();
		
		for (VendorEntity entity : entities) {
			VendorDTO dto = new VendorDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public VendorDTO getById(Long id) throws EntityRetrievalException {
		
		VendorEntity entity = getEntityById(id);
		VendorDTO dto = new VendorDTO(entity);
		return dto;
	}
	
	
	private void create(VendorEntity entity) {
		
		entityManager.persist(entity);
		
	}
	
	private void update(VendorEntity entity) {
		
		entityManager.merge(entity);	
	
	}
	
	private List<VendorEntity> getAllEntities() {
		List<VendorEntity> result = entityManager.createQuery( "SELECT v from VendorEntity v LEFT OUTER JOIN FETCH v.address where (NOT v.deleted = true)", VendorEntity.class).getResultList();
		return result;
		
	}

	private VendorEntity getEntityById(Long id) throws EntityRetrievalException {
		
		VendorEntity entity = null;
			
		Query query = entityManager.createQuery( "SELECT v from VendorEntity v LEFT OUTER JOIN FETCH v.address where (NOT v.deleted = true) AND (vendor_id = :entityid) ", VendorEntity.class );
		query.setParameter("entityid", id);
		List<VendorEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate vendor id in database.");
		} else if(result.size() <= 0) {
			throw new EntityRetrievalException("No vendor with id " + id + " was found in the database.");
		} else {
			entity = result.get(0);
		}

		return entity;
	}
	
	private AddressEntity mergeVendorAddress(AddressDTO addressDto) throws EntityRetrievalException, EntityCreationException {
		AddressEntity address = null;
		if(addressDto.getId() != null) {
			//try to lookup via id if it was provided
			try {
				address = addressDao.getEntityById(addressDto.getId());
			} catch(EntityRetrievalException ere) {
				logger.error("Could not get address with id " + addressDto.getId(), ere);
			}
		} else {
			//otherwise look up via all attributes
			address = addressDao.searchForEntity(addressDto.getStreetLineOne(), addressDto.getStreetLineTwo(), 
					addressDto.getCity(), addressDto.getRegion(), addressDto.getCountry());
		}
		
		if(address == null) {
			//otherwise create and save a new address entity before setting it on the vendor
			address = new AddressEntity();
			address.setStreetLineOne(addressDto.getStreetLineOne());
			address.setStreetLineTwo(addressDto.getStreetLineTwo());
			address.setCity(addressDto.getCity());
			address.setRegion(addressDto.getRegion());
			address.setCountry(addressDto.getCountry());
			addressDao.create(addressDto);
		}
		
		return address;
	}
}
