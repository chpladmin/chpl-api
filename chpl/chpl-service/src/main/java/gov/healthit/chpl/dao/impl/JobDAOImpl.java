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
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobStatusDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.entity.job.JobEntity;
import gov.healthit.chpl.entity.job.JobMessageEntity;
import gov.healthit.chpl.entity.job.JobStatusEntity;
import gov.healthit.chpl.entity.job.JobStatusType;
import gov.healthit.chpl.entity.job.JobTypeEntity;

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
			throw new EntityCreationException(
					String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("job.exists"), LocaleContextHolder.getLocale()), 
					dto.getId()+""));			
		} else {
			entity = new JobEntity();
			if(dto.getContact() != null && dto.getContact().getId() != null) {
				entity.setContactId(dto.getContact().getId());
			} else {
				throw new EntityCreationException(
						String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("job.missingRequiredData"), LocaleContextHolder.getLocale()), 
						"A contact ID "));			
			}
			if(dto.getJobType() != null && dto.getJobType().getId() != null) {
				entity.setJobTypeId(dto.getJobType().getId());
			} else {
				throw new EntityCreationException(
						String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("job.missingRequiredData"), LocaleContextHolder.getLocale()), 
						"A type ID "));
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
				String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("job.couldNotCreate"), LocaleContextHolder.getLocale()));
				logger.error(msg, ex);
				throw new EntityCreationException(msg);
			}
			return new JobDTO(entity);
		}		
	}

	@Override
	public void markStarted(JobDTO dto) throws EntityRetrievalException {
		JobStatusDTO status = updateStatus(dto, 0, JobStatusType.In_Progress);
		
		dto.setStartTime(new Date());
		dto.setStatus(status);
		update(dto);		
	}
	
	@Override
	public JobStatusDTO updateStatus(JobDTO dto, Integer percentComplete, JobStatusType status) throws EntityRetrievalException {
		JobEntity entity = this.getEntityById(dto.getId());
		entity.setEndTime(new Date());
		entityManager.merge(entity);

		JobStatusEntity endStatus = null;
		if(entity.getStatus() != null) {
			endStatus = entity.getStatus();
			endStatus.setPercentComplete(percentComplete);
			endStatus.setStatus(status);
			endStatus.setLastModifiedUser(Util.getCurrentUser().getId());
			endStatus.setCreationDate(new Date());
			endStatus.setDeleted(false);
			endStatus.setLastModifiedDate(new Date());
			entityManager.merge(endStatus);
		} else {
			endStatus = new JobStatusEntity();
			endStatus.setPercentComplete(percentComplete);
			endStatus.setStatus(status);
			endStatus.setLastModifiedUser(Util.getCurrentUser().getId());
			endStatus.setCreationDate(new Date());
			endStatus.setDeleted(false);
			endStatus.setLastModifiedDate(new Date());
			entityManager.persist(endStatus);
		}
		entityManager.flush();
		return new JobStatusDTO(endStatus);
	}
	
	@Override
	public void addJobMessage(JobDTO job, String message) {
		JobMessageEntity entity = new JobMessageEntity();
		entity.setJobId(job.getId());
		entity.setMessage(message);
		entity.setDeleted(false);
		entity.setCreationDate(new Date());
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	public JobDTO update(JobDTO dto) throws EntityRetrievalException {
		JobEntity entity = this.getEntityById(dto.getId());
		if(entity == null) {
			throw new EntityRetrievalException(
					String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("job.doesNotExist"), LocaleContextHolder.getLocale()), 
					dto.getId()+""));		
		}
		if(dto.getContact() != null && dto.getContact().getId() != null) {
			entity.setContactId(dto.getContact().getId());
		}
		if(dto.getJobType() != null && dto.getJobType().getId() != null) {
			entity.setJobTypeId(dto.getJobType().getId());
		}
		if(dto.getStatus() != null && dto.getStatus().getId() != null) {
			entity.setStatusId(dto.getStatus().getId());
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
			
			if(toDelete.getStatus() != null) {
				JobStatusEntity status = toDelete.getStatus();
				status.setDeleted(true);
				status.setLastModifiedDate(new Date());
				status.setLastModifiedUser(Util.getCurrentUser().getId());
				entityManager.merge(status);
			}
			
			if(toDelete.getMessages() != null && toDelete.getMessages().size() > 0) {
				for(JobMessageEntity message : toDelete.getMessages()) {
					message.setDeleted(true);
					message.setLastModifiedDate(new Date());
					message.setLastModifiedUser(Util.getCurrentUser().getId());
					entityManager.merge(message);
				}
			}
			
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
	public List<JobDTO> getByUser(Long contactId) {
		entityManager.clear();
		
		Query query = entityManager.createQuery( "SELECT job "
				+ "FROM JobEntity job "
				+ "JOIN FETCH job.contact contact "
				+ "JOIN FETCH job.jobType type "
				+ "LEFT OUTER JOIN FETCH job.status status "
				+ "LEFT OUTER JOIN FETCH job.messages messages "
				+ "WHERE (job.deleted <> true) "
				+ "AND (job.contactId = :contactId) ", JobEntity.class );
		query.setParameter("contactId", contactId);
		List<JobEntity> entities = query.getResultList();
		
		List<JobDTO> dtos = new ArrayList<JobDTO>();
		for (JobEntity entity : entities) {
			JobDTO dto = new JobDTO(entity);
			dtos.add(dto);
		}
		return dtos;		
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
	
	@Override
	public List<JobDTO> findAllRunningAndCompletedBetweenDates(Date startDate, Date endDate) {
		entityManager.clear();

		Query query = entityManager.createQuery( "SELECT job "
				+ "FROM JobEntity job "
				+ "JOIN FETCH job.contact contact "
				+ "JOIN FETCH job.jobType type "
				+ "LEFT OUTER JOIN FETCH job.status status "
				+ "LEFT OUTER JOIN FETCH job.messages messages "
				+ "WHERE "
				//not deleted and still running
				+ "(job.deleted <> true AND job.endTime IS NULL) "
				+ "OR "
				//completed between start and end date
				+ "(job.endTime IS NOT NULL AND job.endTime >= :startDate AND job.endTime <= :endDate)", JobEntity.class);
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);
		List<JobEntity> entities = query.getResultList();
		List<JobDTO> dtos = new ArrayList<JobDTO>();
		for (JobEntity entity : entities) {
			JobDTO dto = new JobDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	@Override
	public List<JobDTO> findAllRunning() {
		entityManager.clear();

		List<JobEntity> entities = entityManager.createQuery( "SELECT job "
				+ "FROM JobEntity job "
				+ "JOIN FETCH job.contact contact "
				+ "JOIN FETCH job.jobType type "
				+ "LEFT OUTER JOIN FETCH job.status status "
				+ "LEFT OUTER JOIN FETCH job.messages messages "
				+ "WHERE (job.deleted <> true) "
				+ "AND (job.endTime IS NULL OR job.endTime < NOW())", JobEntity.class).getResultList();
		List<JobDTO> dtos = new ArrayList<JobDTO>();
		for (JobEntity entity : entities) {
			JobDTO dto = new JobDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	@Override
	public List<JobTypeDTO> findAllTypes() {
		List<JobTypeEntity> entities = entityManager.createQuery( "SELECT type "
				+ "FROM JobTypeEntity type "
				+ "WHERE (type.deleted <> true) ", JobTypeEntity.class).getResultList();
		
		List<JobTypeDTO> dtos = new ArrayList<JobTypeDTO>();
		for (JobTypeEntity entity : entities) {
			JobTypeDTO dto = new JobTypeDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	private List<JobEntity> getAllEntities() {
		entityManager.clear();
		return entityManager.createQuery( "SELECT job "
				+ "FROM JobEntity job "
				+ "JOIN FETCH job.contact contact "
				+ "JOIN FETCH job.jobType type "
				+ "LEFT OUTER JOIN FETCH job.status status "
				+ "LEFT OUTER JOIN FETCH job.messages messages "
				+ "WHERE (job.deleted <> true) ", JobEntity.class).getResultList();
	}
	
	private JobEntity getEntityById(Long id) {
		entityManager.clear();
		JobEntity entity = null;
			
		Query query = entityManager.createQuery( "SELECT job "
				+ "FROM JobEntity job "
				+ "LEFT OUTER JOIN FETCH job.contact contact "
				+ "LEFT OUTER JOIN FETCH job.jobType type "
				+ "LEFT OUTER JOIN FETCH job.status status "
				+ "LEFT OUTER JOIN FETCH job.messages messages "
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