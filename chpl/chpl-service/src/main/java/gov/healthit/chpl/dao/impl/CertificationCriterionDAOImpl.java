package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("certificationCriterionDAO")
public class CertificationCriterionDAOImpl extends BaseDAOImpl implements CertificationCriterionDAO {
	private static final Logger LOGGER = LogManager.getLogger(CertificationCriterionDAOImpl.class);

	@Override
    @Transactional
	public CertificationCriterionDTO create(CertificationCriterionDTO dto)
			throws EntityCreationException, EntityRetrievalException {

		CertificationCriterionEntity entity = null;
		try {
			if (dto.getId() != null) {
				entity = this.getEntityById(dto.getId());
			}
		} catch (final EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}

		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {

			entity = new CertificationCriterionEntity();
			entity.setAutomatedMeasureCapable(dto.getAutomatedMeasureCapable());
			entity.setAutomatedNumeratorCapable(dto.getAutomatedNumeratorCapable());
			entity.setCertificationEdition(dto.getCertificationEditionId());
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setDescription(dto.getDescription());
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setNumber(dto.getNumber());
			entity.setRequiresSed(dto.getRequiresSed());
			entity.setTitle(dto.getTitle());

			create(entity);
		}
		return new CertificationCriterionDTO(entity);
	}

	@Override
    @Transactional
	public CertificationCriterionDTO update(CertificationCriterionDTO dto)
			throws EntityRetrievalException, EntityCreationException {

		CertificationCriterionEntity entity = this.getEntityById(dto.getId());
		;

		entity.setAutomatedMeasureCapable(dto.getAutomatedMeasureCapable());
		entity.setAutomatedNumeratorCapable(dto.getAutomatedNumeratorCapable());
		entity.setCertificationEdition(dto.getCertificationEditionId());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setDescription(dto.getDescription());
		entity.setId(dto.getId());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setNumber(dto.getNumber());
		entity.setRequiresSed(dto.getRequiresSed());
		entity.setTitle(dto.getTitle());
		update(entity);

		return new CertificationCriterionDTO(entity);

	}

	@Override
	@Transactional
	public void delete(Long criterionId) {

		Query query = entityManager.createQuery(
				"UPDATE CertificationCriterionEntity SET deleted = true WHERE certification_criterion_id = :entityid");
		query.setParameter("entityid", criterionId);
		query.executeUpdate();

	}

	@Override
	public List<CertificationCriterionDTO> findAll() {

		List<CertificationCriterionEntity> entities = getAllEntities();
		List<CertificationCriterionDTO> dtos = new ArrayList<>();

		for (CertificationCriterionEntity entity : entities) {
			CertificationCriterionDTO dto = new CertificationCriterionDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<CertificationCriterionDTO> findByCertificationEditionYear(String year) {

		List<CertificationCriterionEntity> entities = getEntitiesByCertificationEditionYear(year);
		List<CertificationCriterionDTO> dtos = new ArrayList<>();

		for (CertificationCriterionEntity entity : entities) {
			CertificationCriterionDTO dto = new CertificationCriterionDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public CertificationCriterionDTO getById(Long criterionId) throws EntityRetrievalException {

		CertificationCriterionDTO dto = null;
		CertificationCriterionEntity entity = getEntityById(criterionId);

		if (entity != null) {
			dto = new CertificationCriterionDTO(entity);
		}
		return dto;
	}

	@Override
	public CertificationCriterionDTO getByName(String criterionName) {
		CertificationCriterionEntity entity = getEntityByName(criterionName);
		if (entity == null) {
			return null;
		}
		return new CertificationCriterionDTO(entity);
	}

	@Override
	public CertificationCriterionDTO getByNameAndYear(String criterionName, String year) {
		Query query = entityManager
				.createQuery(
						"SELECT cce " + "FROM CertificationCriterionEntity cce "
								+ "LEFT JOIN FETCH cce.certificationEdition " + "where (NOT cce.deleted = true) "
								+ "AND (cce.number = :number) " + "AND (cce.certificationEdition.year = :year) ",
						CertificationCriterionEntity.class);
		query.setParameter("year", year);
		query.setParameter("number", criterionName);
		List<CertificationCriterionEntity> results = query.getResultList();

		CertificationCriterionEntity entity = null;
		if (results.size() > 0) {
			entity = results.get(0);
		}
		CertificationCriterionDTO result = null;
		if (entity != null) {
			result = new CertificationCriterionDTO(entity);
		}
		return result;
	}

	@Transactional
	private void create(CertificationCriterionEntity entity) {

		entityManager.persist(entity);
		entityManager.flush();

	}

	@Transactional
	private void update(CertificationCriterionEntity entity) {

		entityManager.merge(entity);
		entityManager.flush();

	}

	private List<CertificationCriterionEntity> getAllEntities() {
		Query query = entityManager
				.createQuery(
						"SELECT cce " + "FROM CertificationCriterionEntity cce "
								+ "LEFT JOIN FETCH cce.certificationEdition " + "WHERE cce.deleted = false",
						CertificationCriterionEntity.class);
		List<CertificationCriterionEntity> result = query.getResultList();

		return result;
	}

	private List<CertificationCriterionEntity> getEntitiesByCertificationEditionYear(String year) {
		Query query = entityManager.createQuery("SELECT cce " + "FROM CertificationCriterionEntity cce "
				+ "LEFT JOIN FETCH cce.certificationEdition " + "where (NOT cce.deleted = true) "
				+ "AND (cce.certificationEditionId = cce.certificationEdition.id) "
				+ "AND (cce.certificationEdition.year = :year)", CertificationCriterionEntity.class);
		query.setParameter("year", year);
		return query.getResultList();
	}

	public CertificationCriterionEntity getEntityById(Long id) throws EntityRetrievalException {

		CertificationCriterionEntity entity = null;

		if (id != null) {

			Query query = entityManager.createQuery(
					"SELECT cce " + "FROM CertificationCriterionEntity cce "
							+ "LEFT JOIN FETCH cce.certificationEdition "
							+ "WHERE (cce.deleted <> true) AND (cce.id = :entityid) ",
					CertificationCriterionEntity.class);
			query.setParameter("entityid", id);
			List<CertificationCriterionEntity> result = query.getResultList();

			if (result.size() > 1) {
				throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
			}

			if (result.size() > 0) {
				entity = result.get(0);
			}
		}

		return entity;
	}

	public CertificationCriterionEntity getEntityByName(String name) {

		CertificationCriterionEntity entity = null;

		Query query = entityManager.createQuery(
				"SELECT cce " + "FROM CertificationCriterionEntity cce " + "LEFT JOIN FETCH cce.certificationEdition "
						+ "where (NOT cce.deleted = true) AND (cce.number = :name) ",
				CertificationCriterionEntity.class);
		query.setParameter("name", name);
		List<CertificationCriterionEntity> result = query.getResultList();

		if (result.size() > 0) {
			entity = result.get(0);
		}

		return entity;
	}
}
