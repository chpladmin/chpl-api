package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobStatusDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.entity.job.JobEntity;
import gov.healthit.chpl.entity.job.JobMessageEntity;
import gov.healthit.chpl.entity.job.JobStatusEntity;
import gov.healthit.chpl.entity.job.JobStatusType;
import gov.healthit.chpl.entity.job.JobTypeEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.UserMapper;

@Repository("jobDAO")
public class JobDAOImpl extends BaseDAOImpl implements JobDAO {
    private static final Logger LOGGER = LogManager.getLogger(JobDAOImpl.class);

    private MessageSource messageSource;
    private UserMapper userMapper;

    @Autowired
    public JobDAOImpl(@Lazy final UserMapper userMapper, final MessageSource messageSource) {
        this.userMapper = userMapper;
        this.messageSource = messageSource;
    }

    @Override
    @Transactional
    public JobDTO create(JobDTO dto) throws EntityCreationException {
        JobEntity entity = null;
        if (dto.getId() != null) {
            entity = this.getEntityById(dto.getId());
        }

        if (entity != null) {
            throw new EntityCreationException(
                    String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("job.exists"),
                            LocaleContextHolder.getLocale()), dto.getId() + ""));
        } else {
            entity = new JobEntity();
            if (dto.getUser() != null && dto.getUser().getId() != null) {
                entity.setUserId(dto.getUser().getId());
            } else {
                throw new EntityCreationException(String
                        .format(messageSource.getMessage(new DefaultMessageSourceResolvable("job.missingRequiredData"),
                                LocaleContextHolder.getLocale()), "A user ID "));
            }
            if (dto.getJobType() != null && dto.getJobType().getId() != null) {
                entity.setJobTypeId(dto.getJobType().getId());
            } else {
                throw new EntityCreationException(String
                        .format(messageSource.getMessage(new DefaultMessageSourceResolvable("job.missingRequiredData"),
                                LocaleContextHolder.getLocale()), "A type ID "));
            }
            entity.setData(dto.getData());
            entity.setStartTime(dto.getStartTime());
            entity.setEndTime(dto.getEndTime());
            entity.setCreationDate(new Date());
            entity.setDeleted(false);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(dto.getUser().getId());

            try {
                entityManager.persist(entity);
                entityManager.flush();
            } catch (Exception ex) {
                String msg = String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("job.couldNotCreate"), LocaleContextHolder.getLocale()));
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
            return mapEntityToDto(entity);
        }
    }

    @Override
    @Transactional
    public void markStarted(JobDTO dto) throws EntityRetrievalException {
        JobEntity jobEntity = this.getEntityById(dto.getId());
        // delete any messages that currently exist for this job
        if (jobEntity.getMessages() != null && jobEntity.getMessages().size() > 0) {
            for (JobMessageEntity message : jobEntity.getMessages()) {
                message.setDeleted(true);
                message.setLastModifiedDate(new Date());
                message.setLastModifiedUser(
                        AuthUtil.getCurrentUser() != null ? AuthUtil.getAuditId() : jobEntity.getUserId());
                entityManager.merge(message);
            }
            entityManager.flush();
        }

        JobStatusDTO status = updateStatus(dto, 0, JobStatusType.In_Progress);
        dto.setStartTime(new Date());
        dto.setEndTime(null);
        dto.setStatus(status);
        update(dto);
    }

    @Override
    @Transactional
    public JobStatusDTO updateStatus(JobDTO dto, Integer percentComplete, JobStatusType status)
            throws EntityRetrievalException {
        JobEntity entity = this.getEntityById(dto.getId());
        if (status != null && (status == JobStatusType.Complete || status == JobStatusType.Error)) {
            entity.setEndTime(new Date());
            entityManager.merge(entity);
        }

        JobStatusEntity updatedStatus = null;
        if (entity.getStatus() != null) {
            updatedStatus = entity.getStatus();
            updatedStatus.setPercentComplete(percentComplete);
            updatedStatus.setStatus(status);
            updatedStatus.setLastModifiedUser(entity.getUserId());
            updatedStatus.setCreationDate(new Date());
            updatedStatus.setDeleted(false);
            updatedStatus.setLastModifiedDate(new Date());
            entityManager.merge(updatedStatus);
        } else {
            updatedStatus = new JobStatusEntity();
            updatedStatus.setPercentComplete(percentComplete);
            updatedStatus.setStatus(status);
            updatedStatus.setLastModifiedUser(entity.getUserId());
            updatedStatus.setCreationDate(new Date());
            updatedStatus.setDeleted(false);
            updatedStatus.setLastModifiedDate(new Date());
            entityManager.persist(updatedStatus);
        }
        entityManager.flush();
        return new JobStatusDTO(updatedStatus);
    }

    @Override
    @Transactional
    public void addJobMessage(JobDTO job, String message) {
        JobMessageEntity entity = new JobMessageEntity();
        entity.setJobId(job.getId());
        entity.setMessage(message);
        entity.setDeleted(false);
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        entity.setLastModifiedUser(job.getUser().getId());
        entityManager.persist(entity);
        entityManager.flush();
    }

    @Override
    @Transactional
    public JobDTO update(JobDTO dto) throws EntityRetrievalException {
        JobEntity entity = this.getEntityById(dto.getId());
        if (entity == null) {
            throw new EntityRetrievalException(
                    String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("job.doesNotExist"),
                            LocaleContextHolder.getLocale()), dto.getId() + ""));
        }
        if (dto.getUser() != null && dto.getUser().getId() != null) {
            entity.setUserId(dto.getUser().getId());
        }
        if (dto.getJobType() != null && dto.getJobType().getId() != null) {
            entity.setJobTypeId(dto.getJobType().getId());
        }
        if (dto.getStatus() != null && dto.getStatus().getId() != null) {
            entity.setStatusId(dto.getStatus().getId());
        }
        entity.setData(dto.getData());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setLastModifiedUser(AuthUtil.getCurrentUser() != null ? AuthUtil.getAuditId() : entity.getUserId());
        entity.setLastModifiedDate(new Date());
        entityManager.merge(entity);
        entityManager.flush();
        return mapEntityToDto(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) throws EntityRetrievalException {
        JobEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(
                    AuthUtil.getCurrentUser() != null ? AuthUtil.getAuditId() : toDelete.getUserId());
            entityManager.merge(toDelete);

            if (toDelete.getStatus() != null) {
                JobStatusEntity status = toDelete.getStatus();
                status.setDeleted(true);
                status.setLastModifiedDate(new Date());
                status.setLastModifiedUser(
                        AuthUtil.getCurrentUser() != null ? AuthUtil.getAuditId() : toDelete.getUserId());
                entityManager.merge(status);
            }

            if (toDelete.getMessages() != null && toDelete.getMessages().size() > 0) {
                for (JobMessageEntity message : toDelete.getMessages()) {
                    message.setDeleted(true);
                    message.setLastModifiedDate(new Date());
                    message.setLastModifiedUser(
                            AuthUtil.getCurrentUser() != null ? AuthUtil.getAuditId() : toDelete.getUserId());
                    entityManager.merge(message);
                }
            }

            entityManager.flush();
        }
    }

    @Override
    @Transactional
    public JobDTO getById(Long id) {

        JobDTO dto = null;
        JobEntity entity = getEntityById(id);
        if (entity != null) {
            dto = mapEntityToDto(entity);
        }
        return dto;
    }

    @Override
    @Transactional
    public List<JobDTO> getByUser(Long userId) {
        entityManager.clear();

        Query query = entityManager
                .createQuery("SELECT DISTINCT job " + "FROM JobEntity job " + "JOIN FETCH job.user user "
                        + "LEFT OUTER JOIN FETCH user.contact contact " + "JOIN FETCH job.jobType type "
                        + "LEFT OUTER JOIN FETCH job.status status " + "LEFT OUTER JOIN FETCH job.messages messages "
                        + "WHERE (job.deleted <> true) " + "AND (job.userId = :userId) ", JobEntity.class);
        query.setParameter("userId", userId);
        List<JobEntity> entities = query.getResultList();

        List<JobDTO> dtos = new ArrayList<JobDTO>();
        for (JobEntity entity : entities) {
            JobDTO dto = mapEntityToDto(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    @Transactional
    public List<JobDTO> findAll() {
        List<JobEntity> entities = getAllEntities();
        List<JobDTO> dtos = new ArrayList<JobDTO>();

        for (JobEntity entity : entities) {
            JobDTO dto = mapEntityToDto(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    @Transactional
    public List<JobDTO> findAllRunningAndCompletedBetweenDates(Date startDate, Date endDate, Long userId) {
        entityManager.clear();

        String queryStr = "SELECT DISTINCT job " + "FROM JobEntity job " + "JOIN FETCH job.user user "
                + "LEFT OUTER JOIN FETCH user.contact contact " + "JOIN FETCH job.jobType type "
                + "LEFT OUTER JOIN FETCH job.status status " + "LEFT OUTER JOIN FETCH job.messages messages " + "WHERE "
                // not deleted and still running
                + "((job.deleted <> true AND job.endTime IS NULL) " + "OR "
                // completed between start and end date
                + "(job.endTime IS NOT NULL AND job.endTime >= :startDate AND job.endTime <= :endDate))";
        if (userId != null && userId > 0) {
            queryStr += " AND user.id = :userId";
        }

        Query query = entityManager.createQuery(queryStr, JobEntity.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        if (userId != null && userId > 0) {
            query.setParameter("userId", userId);
        }
        List<JobEntity> entities = query.getResultList();
        List<JobDTO> dtos = new ArrayList<JobDTO>();
        for (JobEntity entity : entities) {
            JobDTO dto = mapEntityToDto(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    @Transactional
    public List<JobDTO> findAllRunning() {
        entityManager.clear();

        List<JobEntity> entities = entityManager.createQuery("SELECT DISTINCT job " + "FROM JobEntity job "
                + "JOIN FETCH job.user user " + "LEFT OUTER JOIN FETCH user.contact contact "
                + "JOIN FETCH job.jobType type " + "LEFT OUTER JOIN FETCH job.status status "
                + "LEFT OUTER JOIN FETCH job.messages messages " + "WHERE job.deleted <> true "
                + "AND (job.endTime IS NULL OR status.percentComplete != 100 OR status.status != '"
                + JobStatusType.Complete.toString() + "')", JobEntity.class).getResultList();
        List<JobDTO> dtos = new ArrayList<JobDTO>();
        for (JobEntity entity : entities) {
            JobDTO dto = mapEntityToDto(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    @Transactional
    public List<JobTypeDTO> findAllTypes() {
        List<JobTypeEntity> entities = entityManager
                .createQuery("SELECT type " + "FROM JobTypeEntity type " + "WHERE (type.deleted <> true) ",
                        JobTypeEntity.class)
                .getResultList();

        List<JobTypeDTO> dtos = new ArrayList<JobTypeDTO>();
        for (JobTypeEntity entity : entities) {
            JobTypeDTO dto = new JobTypeDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    private List<JobEntity> getAllEntities() {
        entityManager.clear();
        return entityManager
                .createQuery(
                        "SELECT DISTINCT job " + "FROM JobEntity job " + "JOIN FETCH job.user user "
                                + "LEFT OUTER JOIN FETCH user.contact contact " + "JOIN FETCH job.jobType type "
                                + "LEFT OUTER JOIN FETCH job.status status "
                                + "LEFT OUTER JOIN FETCH job.messages messages " + "WHERE (job.deleted <> true) ",
                        JobEntity.class)
                .getResultList();
    }

    private JobEntity getEntityById(Long id) {
        entityManager.clear();
        JobEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT DISTINCT job " + "FROM JobEntity job " + "LEFT OUTER JOIN FETCH job.user user "
                        + "LEFT OUTER JOIN FETCH user.contact contact " + "LEFT OUTER JOIN FETCH job.jobType type "
                        + "LEFT OUTER JOIN FETCH job.status status " + "LEFT OUTER JOIN FETCH job.messages messages "
                        + "WHERE (job.deleted <> true) " + "AND (job.id = :entityid) ", JobEntity.class);
        query.setParameter("entityid", id);
        List<JobEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private JobDTO mapEntityToDto(JobEntity entity) {
        JobDTO dto = new JobDTO(entity);
        dto.setUser(userMapper.from(entity.getUser()));
        return dto;
    }
}
