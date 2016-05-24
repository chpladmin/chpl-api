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
import gov.healthit.chpl.dao.ContactDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.entity.ContactEntity;


@Repository("contactDao")
public class ContactDAOImpl extends BaseDAOImpl implements ContactDAO {
	private static final Logger logger = LogManager.getLogger(ContactDAOImpl.class);

	@Override
	public ContactEntity create(ContactDTO dto) throws EntityCreationException, EntityRetrievalException {
		
		ContactEntity toInsert = new ContactEntity();
		toInsert.setEmail(dto.getEmail());
		toInsert.setFirstName(dto.getFirstName());
		toInsert.setLastName(dto.getLastName());
		toInsert.setPhoneNumber(dto.getPhoneNumber());
		toInsert.setTitle(dto.getTitle());
		toInsert.setSignatureDate(null);

		toInsert.setDeleted(false);
		toInsert.setCreationDate(new Date());
		toInsert.setLastModifiedDate(new Date());
		toInsert.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(toInsert);
		entityManager.flush();
		return toInsert;
	}

	@Override
	@Transactional
	public ContactEntity update(ContactDTO dto) throws EntityRetrievalException {
		ContactEntity contact = this.getEntityById(dto.getId());

		contact.setEmail(dto.getEmail());
		contact.setFirstName(dto.getFirstName());
		contact.setLastName(dto.getLastName());
		contact.setPhoneNumber(dto.getPhoneNumber());
		contact.setTitle(dto.getTitle());
		contact.setSignatureDate(dto.getSignatureDate());

		contact.setLastModifiedDate(new Date());
		contact.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.merge(contact);
		return contact;
	}

	@Override
	@Transactional
	public void delete(Long id) throws EntityRetrievalException {
		ContactEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.merge(toDelete);
		}
	}

	@Override
	public List<ContactDTO> findAll() {
		List<ContactEntity> result = this.findAllEntities();
		if(result == null) {
			return null;
		}
		
		List<ContactDTO> dtos = new ArrayList<ContactDTO>(result.size());
		for(ContactEntity entity : result) {
			dtos.add(new ContactDTO(entity));
		}
		return dtos;
	}
	
	@Override
	public ContactDTO getById(Long id) throws EntityRetrievalException {
		
		ContactDTO dto = null;
		ContactEntity ae = this.getEntityById(id);
		
		if (ae != null){
			dto = new ContactDTO(ae); 
		}
		return dto;
	}

	@Override
	public ContactDTO getByValues(ContactDTO contact) {
		ContactEntity ae = this.searchEntities(contact);
		if(ae == null) {
			return null;
		}
		return new ContactDTO(ae);
	}
	
	private List<ContactEntity> findAllEntities() {
		Query query = entityManager.createQuery("SELECT a from ContactEntity a where (NOT a.deleted = true)");
		return query.getResultList();
	}
	
	public ContactEntity getEntityById(Long id) throws EntityRetrievalException {
		ContactEntity entity = null;
		
		Query query = entityManager.createQuery( "from ContactEntity a where (NOT deleted = true) AND (contact_id = :entityid) ", ContactEntity.class );
		query.setParameter("entityid", id);
		List<ContactEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate contact id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private ContactEntity getEntityByValues(ContactDTO contact) {
		return this.searchEntities(contact);
	}
	
	private ContactEntity searchEntities(ContactDTO toSearch) {
		ContactEntity entity = null;
		
		String contactQuery = "from ContactEntity a where (NOT deleted = true) ";
		if(toSearch.getFirstName() != null) {
				contactQuery += " AND (first_name = :firstName) ";
		}
		if(toSearch.getLastName() != null) {
				contactQuery += " AND (last_name = :lastName)";
		}
		
		Query query = entityManager.createQuery(contactQuery, ContactEntity.class );
		if(toSearch.getFirstName() != null) {
			query.setParameter("firstName", toSearch.getFirstName());
		}
		if(toSearch.getLastName() != null) {
			query.setParameter("lastName", toSearch.getLastName());
		}

		List<ContactEntity> result = query.getResultList();
		
		if (result.size() >= 1){
			entity = result.get(0);
		} 
		
		return entity;
	}
}
