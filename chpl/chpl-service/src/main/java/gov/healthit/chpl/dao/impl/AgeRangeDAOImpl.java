package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.AgeRangeDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.entity.AgeRangeEntity;

@Repository("ageRangeDao")
public class AgeRangeDAOImpl extends BaseDAOImpl implements AgeRangeDAO {

	@Override
	public AgeRangeDTO getById(Long id)
			throws EntityRetrievalException {

		AgeRangeDTO dto = null;
		AgeRangeEntity entity = getEntityById(id);

		if (entity != null){
			dto = new AgeRangeDTO(entity);
		}
		return dto;
	}

	@Override
	public AgeRangeDTO getByName(String name) {

		AgeRangeEntity entity = getEntityByName(name);
		if(entity == null) {
			return null;
		}
		AgeRangeDTO dto = new AgeRangeDTO(entity);
		return dto;

	}

	@Override
	public List<AgeRangeDTO> getAll() {

		List<AgeRangeEntity> entities = getAllEntities();
		List<AgeRangeDTO> dtos = new ArrayList<AgeRangeDTO>();

		for (AgeRangeEntity entity : entities) {
			AgeRangeDTO dto = new AgeRangeDTO(entity);
			dtos.add(dto);
		}
		return dtos;

	}

	private List<AgeRangeEntity> getAllEntities() {

		List<AgeRangeEntity> result = entityManager.createQuery( "from AgeRangeEntity where (NOT deleted = true) ", AgeRangeEntity.class).getResultList();
		return result;

	}

	private AgeRangeEntity getEntityByName(String name) {
		AgeRangeEntity entity = null;

		Query query = entityManager.createQuery( "from AgeRangeEntity where (NOT deleted = true) and (UPPER(age) = :age)", AgeRangeEntity.class);
		query.setParameter("age", name.toUpperCase());
		List<AgeRangeEntity> result = query.getResultList();
		if (result.size() > 0){
			entity = result.get(0);
		}

		return entity;
	}

	private AgeRangeEntity getEntityById(Long id) throws EntityRetrievalException {

		AgeRangeEntity entity = null;

		Query query = entityManager.createQuery( "from AgeRangeEntity where (NOT deleted = true) AND (id = :entityid) ", AgeRangeEntity.class );
		query.setParameter("entityid", id);
		List<AgeRangeEntity> result = query.getResultList();

		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate age range id in database.");
		}

		if (result.size() > 0){
			entity = result.get(0);
		}

		return entity;
	}
}