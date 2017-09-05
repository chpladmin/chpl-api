package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.dao.ChartDataStatTypeDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ChartDataStatTypeDTO;
import gov.healthit.chpl.entity.ChartDataStatTypeEntity;

import org.springframework.stereotype.Repository;

@Repository("chartDataStatTypeDao")
public class ChartDataStatTypeDAOImpl extends BaseDAOImpl implements ChartDataStatTypeDAO {
	@Override
	public ChartDataStatTypeDTO getById(Long id)
			throws EntityRetrievalException {
		
		ChartDataStatTypeDTO dto = null;
		ChartDataStatTypeEntity entity = getEntityById(id);
		
		if (entity != null){
			dto = new ChartDataStatTypeDTO(entity);
		}
		return dto;
	}
	
	@Override
	public ChartDataStatTypeDTO getByName(String name) {
		
		ChartDataStatTypeEntity entity = getEntityByName(name);
		if(entity == null) {
			return null;
		}
		ChartDataStatTypeDTO dto = new ChartDataStatTypeDTO(entity);
		return dto;
		
	}
	
	@Override
	public List<ChartDataStatTypeDTO> getAll() {
		
		List<ChartDataStatTypeEntity> entities = getAllEntities();
		List<ChartDataStatTypeDTO> dtos = new ArrayList<>();
		
		for (ChartDataStatTypeEntity entity : entities) {
			ChartDataStatTypeDTO dto = new ChartDataStatTypeDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}

	private List<ChartDataStatTypeEntity> getAllEntities() {
		
		List<ChartDataStatTypeEntity> result = entityManager.createQuery( "from ChartDataStatTypeEntity", ChartDataStatTypeEntity.class).getResultList();
		return result;
		
	}
	
	private ChartDataStatTypeEntity getEntityByName(String name) {
		ChartDataStatTypeEntity entity = null;

		Query query = entityManager.createQuery( "from ChartDataStatTypeEntity", ChartDataStatTypeEntity.class);
		query.setParameter("name", name.toUpperCase());
		List<ChartDataStatTypeEntity> result = query.getResultList();
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private ChartDataStatTypeEntity getEntityById(Long id) throws EntityRetrievalException {
		
		ChartDataStatTypeEntity entity = null;
			
		Query query = entityManager.createQuery( "from ChartDataStatTypeEntity", ChartDataStatTypeEntity.class );
		query.setParameter("entityid", id);
		List<ChartDataStatTypeEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate chart type type id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
}
