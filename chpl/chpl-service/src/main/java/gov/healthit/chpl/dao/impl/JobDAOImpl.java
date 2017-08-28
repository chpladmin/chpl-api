package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.dto.JobDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.entity.JobEntity;
import gov.healthit.chpl.entity.UcdProcessEntity;

@Repository("jobDAO")
public class JobDAOImpl extends BaseDAOImpl implements JobDAO {
	private static final Logger logger = LogManager.getLogger(JobDAOImpl.class);
	@Autowired MessageSource messageSource;
	
	@Override
	public JobDTO create(JobDTO dto) throws EntityCreationException {
		JobEntity entity = null;
		if (dto.getId() != null){
			entity = this.getEntityById(dto.getId());
		}
		
		if (entity != null) {
			throw new EntityCreationException("A job entity with this ID already exists.");
		} else {
			entity = new JobEntity();
			if(dto.getContact() != null && dto.getContact().getId() != null) {
				entity.setContactId(dto.getContact().getId());
			} else {
				throw new EntityCreationException("A contact ID must be specified when creating a new job.");
			}
			if(dto.getJobType() != null && dto.getJobType().getId() != null) {
				entity.setJobTypeId(dto.getJobType().getId());
			} else {
				throw new EntityCreationException("A job type ID must be specified when creating a new job.");
			}
			entity.setData(dto.getData());
			entity.setStartTime(dto.getStartTime());
			entity.setEndTime(dto.getEndTime());
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
			try {
				entityManager.persist(entity);
				entityManager.flush();
			} catch(Exception ex) {
				String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.badUcdProcess"), LocaleContextHolder.getLocale()), 
						dto.getName());
				logger.error(msg, ex);
				throw new EntityCreationException(msg);
			}
			return new JobDTO(entity);
		}		
	}

	@Override
	public JobDTO update(JobDTO dto) throws EntityRetrievalException {
		JobEntity entity = this.getEntityById(dto.getId());
		if(entity == null) {
			throw new EntityRetrievalException("Job entity with id " + dto.getId() + " does not exist");
		}
		if(dto.getContact() != null && dto.getContact().getId() != null) {
			entity.setContactId(dto.getContact().getId());
		}
		if(dto.getJobType() != null && dto.getJobType().getId() != null) {
			entity.setJobTypeId(dto.getJobType().getId());
		}
		entity.setData(dto.getData());
		entity.setStartTime(dto.getStartTime());
		entity.setEndTime(dto.getEndTime());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setLastModifiedDate(new Date());
		entityManager.merge(entity);
		entityManager.flush();
		return new JobDTO(entity);
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		JobEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.merge(toDelete);
			entityManager.flush();
		}
	}

	@Override
	public JobDTO getById(Long id) {
		
		JobDTO dto = null;
		JobEntity entity = getEntityById(id);
		if (entity != null){
			dto = new JobDTO(entity);
		}
		return dto;
	}
	
	@Override
	public List<JobDTO> findAll() {
		List<JobEntity> entities = getAllEntities();
		List<JobDTO> dtos = new ArrayList<JobDTO>();
		
		for (JobEntity entity : entities) {
			JobDTO dto = new JobDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}
	
	private List<JobEntity> getAllEntities() {
		return entityManager.createQuery( "SELECT job "
				+ "FROM JobEntity job "
				+ "WHERE (job.deleted <> true) ", JobEntity.class).getResultList();
	}
	
	private JobEntity getEntityById(Long id) {
		JobEntity entity = null;
			
		Query query = entityManager.createQuery( "SELECT job "
				+ "FROM JobEntity job "
				+ "WHERE (job.deleted <> true) "
				+ "AND (job.id = :entityid) ", JobEntity.class );
		query.setParameter("entityid", id);
		List<JobEntity> result = query.getResultList();
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
}