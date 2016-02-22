package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.entity.ActivityEntity;

@Repository("activityDAO")
public class ActivityDAOImpl extends BaseDAOImpl implements ActivityDAO {

	@Override
	public ActivityDTO create(ActivityDTO dto) throws EntityCreationException,
			EntityRetrievalException {
		
		ActivityEntity entity = null;
		try {
			if (dto.getId() != null){
				entity = this.getEntityById(false, dto.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {
			
			entity = new ActivityEntity();
			
			entity.setId(dto.getId());
			entity.setDescription(dto.getDescription());
			entity.setOriginalData(dto.getOriginalData());
			entity.setNewData(dto.getNewData());
			entity.setActivityDate(dto.getActivityDate());
			entity.setConcept(dto.getConcept());
			entity.setActivityObjectId(dto.getActivityObjectId());
			entity.setCreationDate(new Date());
			entity.setLastModifiedDate(new Date());
			//user may be null because when they get an API Key they do not have to be logged in
			entity.setLastModifiedUser(dto.getLastModifiedUser());
			
			entity.setDeleted(false);
			
			create(entity);
			
		}
		ActivityDTO result = null;
		if (entity != null){
			result = new ActivityDTO(entity);
		}
		return result;
	}

	@Override
	public ActivityDTO update(ActivityDTO dto) throws EntityRetrievalException {
		
		ActivityEntity entity =  this.getEntityById(false, dto.getId());
		
		entity.setId(dto.getId());
		entity.setDescription(dto.getDescription());
		entity.setOriginalData(dto.getOriginalData());
		entity.setNewData(dto.getNewData());
		entity.setActivityDate(dto.getActivityDate());
		entity.setActivityObjectConceptId(dto.getConcept().getId());
		entity.setActivityObjectId(dto.getActivityObjectId());
		entity.setCreationDate(new Date());
		entity.setLastModifiedDate(new Date());
		if (dto.getLastModifiedUser() == null){
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
		} else {
			entity.setLastModifiedUser(dto.getLastModifiedUser());
		}
		entity.setDeleted(dto.getDeleted());
		
		update(entity);
		
		ActivityDTO result = null;
		if (entity != null){
			result = new ActivityDTO(entity);
		}
		return result;
		
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		
		Query query = entityManager.createQuery("UPDATE ActivityEntity SET deleted = true WHERE activity_id = :resultid");
		query.setParameter("resultid", id);
		query.executeUpdate();

	}

	@Override
	public ActivityDTO getById(Long id) throws EntityRetrievalException {
		
		ActivityEntity entity = getEntityById(false, id);
		ActivityDTO dto = null;
		if (entity != null){
			dto = new ActivityDTO(entity);
		}
		return dto;
	}
	
	@Override
	public ActivityDTO getById(boolean showDeleted, Long id) throws EntityRetrievalException {
		
		ActivityEntity entity = getEntityById(showDeleted, id);
		ActivityDTO dto = null;
		if (entity != null){
			dto = new ActivityDTO(entity);
		}
		return dto;
	}

	@Override
	public List<ActivityDTO> findAll(boolean showDeleted) {
		
		List<ActivityEntity> entities = getAllEntities(showDeleted);
		List<ActivityDTO> activities = new ArrayList<>();
		
		for (ActivityEntity entity : entities) {
			ActivityDTO result = new ActivityDTO(entity);
			activities.add(result);
		}
		return activities;
	}
	
	@Override
	public List<ActivityDTO> findByObjectId(boolean showDeleted, Long objectId, ActivityConcept concept) {
		
		List<ActivityEntity> entities = this.getEntitiesByObjectId(showDeleted, objectId, concept);
		List<ActivityDTO> activities = new ArrayList<>();
		
		for (ActivityEntity entity : entities) {
			ActivityDTO result = new ActivityDTO(entity);
			activities.add(result);
		}
		return activities;
	}
	
	@Override
	public List<ActivityDTO> findByConcept(boolean showDeleted, ActivityConcept concept) {
		
		List<ActivityEntity> entities = this.getEntitiesByConcept(showDeleted, concept);
		List<ActivityDTO> activities = new ArrayList<>();
		
		for (ActivityEntity entity : entities) {
			ActivityDTO result = new ActivityDTO(entity);
			activities.add(result);
		}
		return activities;
	}
	
	@Override
	public List<ActivityDTO> findAllInLastNDays(boolean showDeleted, Integer lastNDays) {
		
		List<ActivityEntity> entities = this.getAllEntitiesInLastNDays(showDeleted, lastNDays);
		List<ActivityDTO> activities = new ArrayList<>();
		
		for (ActivityEntity entity : entities) {
			ActivityDTO result = new ActivityDTO(entity);
			activities.add(result);
		}
		return activities;
	}
	
	@Override
	public List<ActivityDTO> findByObjectId(boolean showDeleted, Long objectId, ActivityConcept concept, Integer lastNDays) {
		
		List<ActivityEntity> entities = this.getEntitiesByObjectId(showDeleted, objectId, concept, lastNDays);
		List<ActivityDTO> activities = new ArrayList<>();
		
		for (ActivityEntity entity : entities) {
			ActivityDTO result = new ActivityDTO(entity);
			activities.add(result);
		}
		return activities;
	}
	
	@Override
	public List<ActivityDTO> findByConcept(boolean showDeleted, ActivityConcept concept, Integer lastNDays) {
		
		List<ActivityEntity> entities = this.getEntitiesByConcept(showDeleted, concept, lastNDays);
		List<ActivityDTO> activities = new ArrayList<>();
		
		for (ActivityEntity entity : entities) {
			ActivityDTO result = new ActivityDTO(entity);
			activities.add(result);
		}
		return activities;
	}
	
	
	@Override
	public List<ActivityDTO> findByUserId(Long userId, Integer lastNDays) {
		
		List<ActivityEntity> entities = this.getEntitiesByUserId(false, userId, lastNDays);
		List<ActivityDTO> activities = new ArrayList<>();
		
		for (ActivityEntity entity : entities) {
			ActivityDTO result = new ActivityDTO(entity);
			activities.add(result);
		}
		return activities;
	}
	
	@Override
	public List<ActivityDTO> findByUserId(Long userId) {
		
		List<ActivityEntity> entities = this.getEntitiesByUserId(false, userId);
		List<ActivityDTO> activities = new ArrayList<>();
		
		for (ActivityEntity entity : entities) {
			ActivityDTO result = new ActivityDTO(entity);
			activities.add(result);
		}
		return activities;
	}
	
	@Override
	public Map<Long, List<ActivityDTO> > findAllByUser(){
		
		Map<Long, List<ActivityDTO> > activityByUser = new HashMap<Long, List<ActivityDTO> >();
		
		List<ActivityEntity> entities = getAllEntities(false);
		
		for (ActivityEntity entity : entities) {
			
			ActivityDTO result = new ActivityDTO(entity);
			Long userId = result.getLastModifiedUser();
			if(userId != null) {
				if( activityByUser.containsKey(userId)){
					activityByUser.get(userId).add(result);
				} else {
					List<ActivityDTO> activity = new ArrayList<ActivityDTO>();
					activity.add(result);
					activityByUser.put(userId, activity);
				}
			}
		}
		return activityByUser;
	}
	
	@Override
	public Map<Long, List<ActivityDTO> > findAllByUserInLastNDays(Integer lastNDays){
		
		Map<Long, List<ActivityDTO> > activityByUser = new HashMap<Long, List<ActivityDTO> >();
		
		List<ActivityEntity> entities = this.getAllEntitiesInLastNDays(false, lastNDays);
		
		for (ActivityEntity entity : entities) {
			
			ActivityDTO result = new ActivityDTO(entity);
			Long userId = result.getLastModifiedUser();
			if(userId != null) {
				if( activityByUser.containsKey(userId)){
					activityByUser.get(userId).add(result);
				} else {
					List<ActivityDTO> activity = new ArrayList<ActivityDTO>();
					activity.add(result);
					activityByUser.put(userId, activity);
				}
			}
		}
		return activityByUser;
	}
	
	
	private void create(ActivityEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(ActivityEntity entity) {
		
		entityManager.merge(entity);
		entityManager.flush();
	
	}
	
	
	private ActivityEntity getEntityById(boolean showDeleted, Long id) throws EntityRetrievalException {
		
		ActivityEntity entity = null;
		Query query = null;
		if(showDeleted){
			query = entityManager.createQuery( "from ActivityEntity where (activity_id = :entityid) ", ActivityEntity.class );
		}else{
			query = entityManager.createQuery( "from ActivityEntity where (NOT deleted = true) AND (activity_id = :entityid) ", ActivityEntity.class );
		}
		query.setParameter("entityid", id);
		List<ActivityEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}

	private List<ActivityEntity> getEntitiesByObjectId(boolean showDeleted, Long objectId, ActivityConcept concept) {
		
		Query query = null;
		if(showDeleted){
			query = entityManager.createQuery( "from ActivityEntity where (activity_object_id = :objectid)  AND (activity_object_concept_id = :conceptid) ", ActivityEntity.class );
		}else{
			query = entityManager.createQuery( "from ActivityEntity where (NOT deleted = true) AND (activity_object_id = :objectid)  AND (activity_object_concept_id = :conceptid) ", ActivityEntity.class );
		}
		query.setParameter("objectid", objectId);
		query.setParameter("conceptid", concept.getId());
		List<ActivityEntity> result = query.getResultList();
		return result;
	}
	
	private List<ActivityEntity> getEntitiesByConcept(boolean showDeleted, ActivityConcept concept) {
		
		Query query = null;
		
		if(showDeleted){
			query = entityManager.createQuery( "from ActivityEntity where (activity_object_concept_id = :conceptid) ", ActivityEntity.class );
		}else{
			query = entityManager.createQuery( "from ActivityEntity where (NOT deleted = true) AND (activity_object_concept_id = :conceptid) ", ActivityEntity.class );
		}
		query.setParameter("conceptid", concept.getId());
		List<ActivityEntity> result = query.getResultList();
		return result;
	}
	
	private List<ActivityEntity> getAllEntities(boolean showDeleted) {
		
		List<ActivityEntity> result = null;
		
		if(showDeleted){
			result = entityManager.createQuery( "from ActivityEntity ", ActivityEntity.class).getResultList();
		}else{
			result = entityManager.createQuery( "from ActivityEntity where (NOT deleted = true) ", ActivityEntity.class).getResultList();
		}
		return result;
	}
	
	private List<ActivityEntity> getEntitiesByObjectId(boolean showDeleted, Long objectId, ActivityConcept concept, Integer lastNDays) {

		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, -lastNDays);
		Date nDaysAgo = cal.getTime();

		Query query = null;

		if(showDeleted){
			query = entityManager.createQuery(
					"from ActivityEntity where "
							+ "(activity_object_id = :objectid)  "
							+ "AND (activity_object_concept_id = :conceptid) "
							+ "AND (activity_date >= :startdate) "
							+ "AND (activity_date <= CURRENT_DATE + 1) ", ActivityEntity.class );
			query.setParameter("objectid", objectId);
			query.setParameter("conceptid", concept.getId());
			query.setParameter("startdate", nDaysAgo);
		}else{
			query = entityManager.createQuery(
					"from ActivityEntity where (NOT deleted = true) "
							+ "AND (activity_object_id = :objectid)  "
							+ "AND (activity_object_concept_id = :conceptid) "
							+ "AND (activity_date >= :startdate) "
							+ "AND (activity_date <= CURRENT_DATE + 1) ", ActivityEntity.class );
			query.setParameter("objectid", objectId);
			query.setParameter("conceptid", concept.getId());
			query.setParameter("startdate", nDaysAgo);
		}
		List<ActivityEntity> result = query.getResultList();
		return result;
	}
	
	private List<ActivityEntity> getEntitiesByConcept(boolean showDeleted, ActivityConcept concept, Integer lastNDays) {


		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, -lastNDays);
		Date nDaysAgo = cal.getTime();

		Query query = null;

		if(showDeleted){
			query = entityManager.createQuery( "from ActivityEntity where "
					+ "(activity_object_concept_id = :conceptid) "
					+ "AND (activity_date >= :startdate) "
					+ "AND (activity_date <= CURRENT_DATE + 1) ", ActivityEntity.class );
			query.setParameter("conceptid", concept.getId());
			query.setParameter("startdate", nDaysAgo);
		}else{
			query = entityManager.createQuery( "from ActivityEntity where (NOT deleted = true) "
					+ "AND (activity_object_concept_id = :conceptid) "
					+ "AND (activity_date >= :startdate) "
					+ "AND (activity_date <= CURRENT_DATE + 1) ", ActivityEntity.class );
			query.setParameter("conceptid", concept.getId());
			query.setParameter("startdate", nDaysAgo);
		}
		List<ActivityEntity> result = query.getResultList();
		return result;
	}
	
	private List<ActivityEntity> getAllEntitiesInLastNDays(boolean showDeleted, Integer lastNDays) {

		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, -lastNDays);
		Date nDaysAgo = cal.getTime();

		Query query = null;

		if(showDeleted){
			query = entityManager.createQuery( "from ActivityEntity where "
					+ "(activity_date >= :startdate) "
					+ "AND (activity_date <= CURRENT_DATE + 1) ", ActivityEntity.class);
			query.setParameter("startdate", nDaysAgo);
		}else{
			query = entityManager.createQuery( "from ActivityEntity where (NOT deleted = true) "
					+ "AND (activity_date >= :startdate) "
					+ "AND (activity_date <= CURRENT_DATE + 1) ", ActivityEntity.class);
			query.setParameter("startdate", nDaysAgo);
		}

		List<ActivityEntity> result = query.getResultList();

		return result;
	}
	
	private List<ActivityEntity> getEntitiesByUserId(boolean showDeleted, Long userId) {

		Query query = null;

		if(showDeleted){
			query = entityManager.createQuery("from ActivityEntity where (last_modified_user = :userid) ", ActivityEntity.class );
			query.setParameter("userid", userId);
		}else{
			query = entityManager.createQuery("from ActivityEntity where (NOT deleted = true) AND (last_modified_user = :userid) ", ActivityEntity.class );
			query.setParameter("userid", userId);
		}
		List<ActivityEntity> result = query.getResultList();
		return result;
	}
	
	private List<ActivityEntity> getEntitiesByUserId(boolean showDeleted, Long userId, Integer lastNDays) {


		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, -lastNDays);
		Date nDaysAgo = cal.getTime();

		Query query = null;

		if(showDeleted){
			query = entityManager.createQuery( "from ActivityEntity where "
					+ "(last_modified_user = :userid) "
					+ "AND (activity_date >= :startdate) "
					+ "AND (activity_date <= CURRENT_DATE + 1) ", ActivityEntity.class );
			query.setParameter("userid", userId);
			query.setParameter("startdate", nDaysAgo);
		}else{
			query = entityManager.createQuery( "from ActivityEntity where (NOT deleted = true) "
					+ "AND (last_modified_user = :userid) "
					+ "AND (activity_date >= :startdate) "
					+ "AND (activity_date <= CURRENT_DATE + 1) ", ActivityEntity.class );
			query.setParameter("userid", userId);
			query.setParameter("startdate", nDaysAgo);
		}
		List<ActivityEntity> result = query.getResultList();
		return result;
	}

}
