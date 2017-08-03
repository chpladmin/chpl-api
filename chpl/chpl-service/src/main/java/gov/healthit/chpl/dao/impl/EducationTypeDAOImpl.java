package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.entity.EducationTypeEntity;

@Repository("educationTypeDao")
public class EducationTypeDAOImpl extends BaseDAOImpl implements EducationTypeDAO {

	@Override
	public EducationTypeDTO getById(Long id)
			throws EntityRetrievalException {
		
		EducationTypeDTO dto = null;
		EducationTypeEntity entity = getEntityById(id);
		
		if (entity != null){
			dto = new EducationTypeDTO(entity);
		}
		return dto;
	}
	
	@Override
	public EducationTypeDTO getByName(String name) {
		
		EducationTypeEntity entity = getEntityByName(name);
		if(entity == null) {
			return null;
		}
		EducationTypeDTO dto = new EducationTypeDTO(entity);
		return dto;
		
	}
	
	@Override
	public List<EducationTypeDTO> getAll() {
		
		List<EducationTypeEntity> entities = getAllEntities();
		List<EducationTypeDTO> dtos = new ArrayList<>();
		
		for (EducationTypeEntity entity : entities) {
			EducationTypeDTO dto = new EducationTypeDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}

	private List<EducationTypeEntity> getAllEntities() {
		
		List<EducationTypeEntity> result = entityManager.createQuery( "from EducationTypeEntity where (NOT deleted = true) ", EducationTypeEntity.class).getResultList();
		return result;
		
	}
	
	private EducationTypeEntity getEntityByName(String name) {
		EducationTypeEntity entity = null;

		Query query = entityManager.createQuery( "from EducationTypeEntity where (NOT deleted = true) and (UPPER(name) = :name)", EducationTypeEntity.class);
		query.setParameter("name", name.toUpperCase());
		List<EducationTypeEntity> result = query.getResultList();
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private EducationTypeEntity getEntityById(Long id) throws EntityRetrievalException {
		
		EducationTypeEntity entity = null;
			
		Query query = entityManager.createQuery( "from EducationTypeEntity where (NOT deleted = true) AND (id = :entityid) ", EducationTypeEntity.class );
		query.setParameter("entityid", id);
		List<EducationTypeEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate education type id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
}