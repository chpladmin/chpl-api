package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.entity.CertificationResultAdditionalSoftwareEntity;
import gov.healthit.chpl.entity.CertificationResultEntity;
import gov.healthit.chpl.entity.CertificationResultTestDataEntity;
import gov.healthit.chpl.entity.CertificationResultTestFunctionalityEntity;
import gov.healthit.chpl.entity.CertificationResultTestProcedureEntity;
import gov.healthit.chpl.entity.CertificationResultTestStandardEntity;
import gov.healthit.chpl.entity.CertificationResultTestToolEntity;
import gov.healthit.chpl.entity.CertificationResultUcdProcessEntity;

@Repository(value="certificationResultDAO")
public class CertificationResultDAOImpl extends BaseDAOImpl implements CertificationResultDAO {
	
	@Override
	public CertificationResultDTO create(CertificationResultDTO result) throws EntityCreationException {
		
		CertificationResultEntity entity = null;
		try {
			if (result.getId() != null){
				entity = this.getEntityById(result.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {
			
			entity = new CertificationResultEntity();
			entity.setCertificationCriterionId(result.getCertificationCriterionId());
			entity.setCertifiedProductId(result.getCertifiedProductId());
			entity.setGap(result.getGap());
			entity.setSed(result.getSed());
			entity.setG1Success(result.getG1Success());
			entity.setG2Success(result.getG2Success());
			entity.setSuccess(result.getSuccessful());
			
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			
			create(entity);
		}
		
		return new CertificationResultDTO(entity);
		
	}

	@Override
	public CertificationResultDTO update(CertificationResultDTO result) throws EntityRetrievalException {
	
		CertificationResultEntity entity = getEntityById(result.getId());
		entity.setCertificationCriterionId(result.getCertificationCriterionId());
		entity.setCertifiedProductId(result.getCertifiedProductId());
		entity.setGap(result.getGap());
		entity.setSed(result.getSed());
		entity.setG1Success(result.getG1Success());
		entity.setG2Success(result.getG2Success());
		entity.setSuccess(result.getSuccessful());
		
		if(result.getDeleted() != null) {
			entity.setDeleted(result.getDeleted());
		}
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setLastModifiedDate(new Date());
		
		update(entity);
		return new CertificationResultDTO(entity);
	}

	@Override
	public void delete(Long resultId) {
		
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE CertificationResultEntity SET deleted = true WHERE certification_result_id = :resultid");
		query.setParameter("resultid", resultId);
		query.executeUpdate();
		
	}

	@Override
	public void deleteByCertifiedProductId(Long certifiedProductId) {
		
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE CertificationResultEntity SET deleted = true WHERE certified_product_id = :certifiedProductId");
		query.setParameter("certifiedProductId", certifiedProductId);
		query.executeUpdate();
		
	}
	
	@Override
	public List<CertificationResultDTO> findAll() {
			
		List<CertificationResultEntity> entities = getAllEntities();
		List<CertificationResultDTO> products = new ArrayList<>();
		
		for (CertificationResultEntity entity : entities) {
			CertificationResultDTO result = new CertificationResultDTO(entity);
			products.add(result);
		}
		return products;
		
	}

	@Override
	public CertificationResultDTO getById(Long resultId) throws EntityRetrievalException {
		
		CertificationResultDTO dto = null;
		CertificationResultEntity entity = getEntityById(resultId);
		
		if (entity != null){
			dto = new CertificationResultDTO(entity);
		}
		return dto;
	}
	
	private void create(CertificationResultEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(CertificationResultEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	
	}
	
	private List<CertificationResultEntity> getAllEntities() {
		
		List<CertificationResultEntity> result = entityManager.createQuery( "from CertificationResultEntity where (NOT deleted = true) ", CertificationResultEntity.class).getResultList();
		return result;
		
	}
	
	private CertificationResultEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CertificationResultEntity entity = null;
			
		Query query = entityManager.createQuery( "from CertificationResultEntity where (NOT deleted = true) AND (certification_result_id = :entityid) ", CertificationResultEntity.class );
		query.setParameter("entityid", id);
		List<CertificationResultEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate result id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
	@Override
	public List<CertificationResultDTO> findByCertifiedProductId(
			Long certifiedProductId) {
		List<CertificationResultEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
		List<CertificationResultDTO> cqmResults = new ArrayList<>();
		
		for (CertificationResultEntity entity : entities) {
			CertificationResultDTO cqmResult = new CertificationResultDTO(entity);
			cqmResults.add(cqmResult);
		}
		return cqmResults;
	}
	
	private List<CertificationResultEntity> getEntitiesByCertifiedProductId(Long certifiedProductId) {
		
		Query query = entityManager.createQuery( "from CertificationResultEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ", CertificationResultEntity.class );
		query.setParameter("entityid", certifiedProductId);
		List<CertificationResultEntity> result = query.getResultList();
		return result;
	}

	/******************************************************
	 * UCD Details for Certification Results
	 * 
	 *******************************************************/
	
	@Override
	public List<CertificationResultUcdProcessDTO> getUcdProcessesForCertificationResult(Long certificationResultId){
		
		List<CertificationResultUcdProcessEntity> entities = getUcdProcessesForCertification(certificationResultId);
		List<CertificationResultUcdProcessDTO> dtos = new ArrayList<CertificationResultUcdProcessDTO>();

		for(CertificationResultUcdProcessEntity entity : entities) {
			dtos.add(new CertificationResultUcdProcessDTO(entity));
		}
		return dtos;
	}
	
	public CertificationResultUcdProcessDTO addUcdProcessMapping(CertificationResultUcdProcessDTO dto) throws EntityCreationException {
		CertificationResultUcdProcessEntity mapping = new CertificationResultUcdProcessEntity();
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setUcdProcessId(dto.getUcdProcessId());
		mapping.setUcdProcessDetails(dto.getUcdProcessDetails());
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		
		return new CertificationResultUcdProcessDTO(mapping);
	}
	
	public void deleteUcdProcessMapping(Long mappingId){
		CertificationResultUcdProcessEntity toDelete = getCertificationResultUcdProcessById(mappingId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}
	
	private CertificationResultUcdProcessEntity getCertificationResultUcdProcessById(Long id) {
		CertificationResultUcdProcessEntity entity = null;
		
		Query query = entityManager.createQuery( "SELECT up "
				+ "FROM CertificationResultUcdProcessEntity up "
				+ "LEFT OUTER JOIN FETCH up.ucdProcess "
				+ "where (NOT up.deleted = true) AND (certification_result_ucd_process_id = :id) ", 
				CertificationResultUcdProcessEntity.class );
		query.setParameter("id", id);
		List<CertificationResultUcdProcessEntity> result = query.getResultList();

		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private List<CertificationResultUcdProcessEntity> getUcdProcessesForCertification(Long certificationResultId){
		Query query = entityManager.createQuery( "SELECT up "
				+ "FROM CertificationResultUcdProcessEntity up "
				+ "LEFT OUTER JOIN FETCH up.ucdProcess "
				+ "where (NOT up.deleted = true) AND (certification_result_id = :certificationResultId) ", 
				CertificationResultUcdProcessEntity.class );
		query.setParameter("certificationResultId", certificationResultId);
		
		return query.getResultList();
	}
	
	/******************************************************
	 * Additional Software for Certification Results
	 * 
	 *******************************************************/
	
	@Override
	public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareForCertificationResult(Long certificationResultId){
		
		List<CertificationResultAdditionalSoftwareEntity> entities = getAdditionalSoftwareForCertification(certificationResultId);
		List<CertificationResultAdditionalSoftwareDTO> dtos = new ArrayList<CertificationResultAdditionalSoftwareDTO>();
		
		for (CertificationResultAdditionalSoftwareEntity entity : entities){
			CertificationResultAdditionalSoftwareDTO dto = new CertificationResultAdditionalSoftwareDTO(entity);
			dtos.add(dto);	
		}
		return dtos;
	}
	
	public CertificationResultAdditionalSoftwareDTO addAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareDTO dto) throws EntityCreationException {
		CertificationResultAdditionalSoftwareEntity mapping = new CertificationResultAdditionalSoftwareEntity();
		mapping = new CertificationResultAdditionalSoftwareEntity();
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setCertifiedProductId(dto.getCertifiedProductId());
		mapping.setName(dto.getName());
		mapping.setVersion(dto.getVersion());
		mapping.setJustification(dto.getJustification());
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		
		return new CertificationResultAdditionalSoftwareDTO(mapping);
	}
	
	public CertificationResultAdditionalSoftwareDTO updateAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareDTO dto){
		CertificationResultAdditionalSoftwareEntity mapping = getCertificationResultAdditionalSoftwareById(dto.getId());
		if(mapping == null) {
			return null;
		}
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setCertifiedProductId(dto.getCertifiedProductId());
		mapping.setName(dto.getName());
		mapping.setVersion(dto.getVersion());
		mapping.setJustification(dto.getJustification());
		if(dto.getDeleted() != null) {
			mapping.setDeleted(dto.getDeleted());
		}
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.merge(mapping);
		entityManager.flush();
		return new CertificationResultAdditionalSoftwareDTO(mapping);
	}

	public void deleteAdditionalSoftwareMapping(Long mappingId){
		CertificationResultAdditionalSoftwareEntity toDelete = getCertificationResultAdditionalSoftwareById(mappingId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}
	
	private CertificationResultAdditionalSoftwareEntity getCertificationResultAdditionalSoftwareById(Long id) {
		CertificationResultAdditionalSoftwareEntity entity = null;
		
		Query query = entityManager.createQuery( "from CertificationResultAdditionalSoftwareEntity "
				+ "where (NOT deleted = true) AND (certification_result_additional_software_id = :entityid) ", 
				CertificationResultAdditionalSoftwareEntity.class );
		query.setParameter("entityid", id);
		List<CertificationResultAdditionalSoftwareEntity> result = query.getResultList();

		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private List<CertificationResultAdditionalSoftwareEntity> getAdditionalSoftwareForCertification(Long certificationResultId){
		Query query = entityManager.createQuery( "from CertificationResultAdditionalSoftwareEntity "
				+ "where (NOT deleted = true) AND (certification_result_id = :certificationResultId) ", 
				CertificationResultAdditionalSoftwareEntity.class );
		query.setParameter("certificationResultId", certificationResultId);
		
		List<CertificationResultAdditionalSoftwareEntity> result = query.getResultList();
		if(result == null) {
			return null;
		}
		return result;
	}
	
	
	/******************************************************
	 * Test Standard methods
	 * 
	 *******************************************************/
	
	@Override
	public List<CertificationResultTestStandardDTO> getTestStandardsForCertificationResult(Long certificationResultId){
		
		List<CertificationResultTestStandardEntity> entities = getTestStandardsForCertification(certificationResultId);
		List<CertificationResultTestStandardDTO> dtos = new ArrayList<CertificationResultTestStandardDTO>();
		
		for (CertificationResultTestStandardEntity entity : entities){
			CertificationResultTestStandardDTO dto = new CertificationResultTestStandardDTO(entity);
			dtos.add(dto);	
		}
		return dtos;
	}
	
	@Override
	public CertificationResultTestStandardDTO addTestStandardMapping(CertificationResultTestStandardDTO dto) throws EntityCreationException {
		CertificationResultTestStandardEntity mapping = new CertificationResultTestStandardEntity();
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setTestStandardId(dto.getTestStandardId());
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		
		return new CertificationResultTestStandardDTO(mapping);
	}

	@Override
	public void deleteTestStandardMapping(Long mappingId){
		CertificationResultTestStandardEntity toDelete = getCertificationResultTestStandardById(mappingId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}
	
	private CertificationResultTestStandardEntity getCertificationResultTestStandardById(Long id) {
		CertificationResultTestStandardEntity entity = null;
		
		Query query = entityManager.createQuery( "SELECT ts "
				+ "FROM CertificationResultTestStandardEntity ts "
				+ "LEFT OUTER JOIN FETCH ts.testStandard "
				+ "where (NOT ts.deleted = true) AND (ts.id = :id) ", 
				CertificationResultTestStandardEntity.class );
		query.setParameter("id", id);
		List<CertificationResultTestStandardEntity> result = query.getResultList();

		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private List<CertificationResultTestStandardEntity> getTestStandardsForCertification(Long certificationResultId){
		Query query = entityManager.createQuery( "SELECT ts "
				+ "FROM CertificationResultTestStandardEntity ts "
				+ "LEFT OUTER JOIN FETCH ts.testStandard "
				+ "where (NOT ts.deleted = true) AND (certification_result_id = :certificationResultId) ", 
				CertificationResultTestStandardEntity.class );
		query.setParameter("certificationResultId", certificationResultId);
		
		List<CertificationResultTestStandardEntity> result = query.getResultList();
		if(result == null) {
			return null;
		}
		return result;
	}
	

	/******************************************************
	 * Test Tool methods
	 * 
	 *******************************************************/
	
	@Override
	public List<CertificationResultTestToolDTO> getTestToolsForCertificationResult(Long certificationResultId){
		
		List<CertificationResultTestToolEntity> entities = getTestToolsForCertification(certificationResultId);
		List<CertificationResultTestToolDTO> dtos = new ArrayList<CertificationResultTestToolDTO>();
		
		for (CertificationResultTestToolEntity entity : entities){
			CertificationResultTestToolDTO dto = new CertificationResultTestToolDTO(entity);
			dtos.add(dto);	
		}
		return dtos;
	}
	
	@Override
	public CertificationResultTestToolDTO addTestToolMapping(CertificationResultTestToolDTO dto) throws EntityCreationException {
		CertificationResultTestToolEntity mapping = new CertificationResultTestToolEntity();
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setTestToolId(dto.getTestToolId());
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		
		return new CertificationResultTestToolDTO(mapping);
	}

	@Override
	public void deleteTestToolMapping(Long mappingId){
		CertificationResultTestToolEntity toDelete = getCertificationResultTestToolById(mappingId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}
	
	private CertificationResultTestToolEntity getCertificationResultTestToolById(Long id) {
		CertificationResultTestToolEntity entity = null;
		
		Query query = entityManager.createQuery( "SELECT tt "
				+ "FROM CertificationResultTestToolEntity tt "
				+ "LEFT OUTER JOIN FETCH tt.testTool "
				+ "where (NOT tt.deleted = true) AND (tt.id = :id) ", 
				CertificationResultTestToolEntity.class );
		query.setParameter("id", id);
		List<CertificationResultTestToolEntity> result = query.getResultList();

		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private List<CertificationResultTestToolEntity> getTestToolsForCertification(Long certificationResultId){
		Query query = entityManager.createQuery( "SELECT tt "
				+ "FROM CertificationResultTestToolEntity tt "
				+ "LEFT OUTER JOIN FETCH tt.testTool "
				+ "where (NOT tt.deleted = true) AND (certification_result_id = :certificationResultId) ", 
				CertificationResultTestToolEntity.class );
		query.setParameter("certificationResultId", certificationResultId);
		
		List<CertificationResultTestToolEntity> result = query.getResultList();
		if(result == null) {
			return null;
		}
		return result;
	}
	
	/******************************************************
	 * Test Data for Certification Results
	 * 
	 *******************************************************/
	
	@Override
	public List<CertificationResultTestDataDTO> getTestDataForCertificationResult(Long certificationResultId){
		
		List<CertificationResultTestDataEntity> entities = getTestDataForCertification(certificationResultId);
		List<CertificationResultTestDataDTO> dtos = new ArrayList<CertificationResultTestDataDTO>();
		
		for (CertificationResultTestDataEntity entity : entities){
			CertificationResultTestDataDTO dto = new CertificationResultTestDataDTO(entity);
			dtos.add(dto);	
		}
		return dtos;
	}
	
	public CertificationResultTestDataDTO addTestDataMapping(CertificationResultTestDataDTO dto) throws EntityCreationException {
		CertificationResultTestDataEntity mapping = new CertificationResultTestDataEntity();
		mapping = new CertificationResultTestDataEntity();
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setTestDataVersion(dto.getVersion());
		mapping.setAlterationDescription(dto.getAlteration());
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		
		return new CertificationResultTestDataDTO(mapping);
	}
	
	public CertificationResultTestDataDTO updateTestDataMapping(CertificationResultTestDataDTO dto){
		CertificationResultTestDataEntity mapping = getCertificationResultTestDataById(dto.getId());
		if(mapping == null) {
			return null;
		}
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setTestDataVersion(dto.getVersion());
		mapping.setAlterationDescription(dto.getAlteration());
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.merge(mapping);
		entityManager.flush();
		return new CertificationResultTestDataDTO(mapping);
	}

	public void deleteTestDataMapping(Long mappingId){
		CertificationResultTestDataEntity toDelete = getCertificationResultTestDataById(mappingId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}
	
	private CertificationResultTestDataEntity getCertificationResultTestDataById(Long id) {
		CertificationResultTestDataEntity entity = null;
		
		Query query = entityManager.createQuery( "from CertificationResultTestDataEntity "
				+ "where (NOT deleted = true) AND (certification_result_test_data_id = :entityid) ", 
				CertificationResultTestDataEntity.class );
		query.setParameter("entityid", id);
		List<CertificationResultTestDataEntity> result = query.getResultList();

		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private List<CertificationResultTestDataEntity> getTestDataForCertification(Long certificationResultId){
		Query query = entityManager.createQuery( "from CertificationResultTestDataEntity "
				+ "where (NOT deleted = true) AND (certification_result_id = :certificationResultId) ", 
				CertificationResultTestDataEntity.class );
		query.setParameter("certificationResultId", certificationResultId);
		
		List<CertificationResultTestDataEntity> result = query.getResultList();
		if(result == null) {
			return null;
		}
		return result;
	}
	
	/******************************************************
	 * Test Procedure methods
	 * 
	 *******************************************************/
	
	@Override
	public List<CertificationResultTestProcedureDTO> getTestProceduresForCertificationResult(Long certificationResultId){
		
		List<CertificationResultTestProcedureEntity> entities = getTestProceduresForCertification(certificationResultId);
		List<CertificationResultTestProcedureDTO> dtos = new ArrayList<CertificationResultTestProcedureDTO>();
		
		for (CertificationResultTestProcedureEntity entity : entities){
			CertificationResultTestProcedureDTO dto = new CertificationResultTestProcedureDTO(entity);
			dtos.add(dto);	
		}
		return dtos;
	}
	
	@Override
	public CertificationResultTestProcedureDTO addTestProcedureMapping(CertificationResultTestProcedureDTO dto) throws EntityCreationException {
		CertificationResultTestProcedureEntity mapping = new CertificationResultTestProcedureEntity();
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setTestProcedureId(dto.getTestProcedureId());
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		
		return new CertificationResultTestProcedureDTO(mapping);
	}

	@Override
	public void deleteTestProcedureMapping(Long mappingId){
		CertificationResultTestProcedureEntity toDelete = getCertificationResultTestProcedureById(mappingId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}
	
	private CertificationResultTestProcedureEntity getCertificationResultTestProcedureById(Long id) {
		CertificationResultTestProcedureEntity entity = null;
		
		Query query = entityManager.createQuery( "SELECT tp "
				+ "FROM CertificationResultTestProcedureEntity tp "
				+ "LEFT OUTER JOIN FETCH tp.testProcedure "
				+ "where (NOT tp.deleted = true) AND (tp.id = :entityid) ", 
				CertificationResultTestProcedureEntity.class );
		query.setParameter("entityid", id);
		List<CertificationResultTestProcedureEntity> result = query.getResultList();

		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private List<CertificationResultTestProcedureEntity> getTestProceduresForCertification(Long certificationResultId){
		Query query = entityManager.createQuery( "SELECT tp "
				+ "FROM CertificationResultTestProcedureEntity tp "
				+ "LEFT OUTER JOIN FETCH tp.testProcedure "
				+ "where (NOT tp.deleted = true) AND (certification_result_id = :certificationResultId) ", 
				CertificationResultTestProcedureEntity.class );
		query.setParameter("certificationResultId", certificationResultId);
		
		List<CertificationResultTestProcedureEntity> result = query.getResultList();
		if(result == null) {
			return null;
		}
		return result;
	}
	

	/******************************************************
	 * Test Functionality for Certification Results
	 * 
	 *******************************************************/
	
	@Override
	public List<CertificationResultTestFunctionalityDTO> getTestFunctionalityForCertificationResult(Long certificationResultId){
		
		List<CertificationResultTestFunctionalityEntity> entities = getTestFunctionalityForCertification(certificationResultId);
		List<CertificationResultTestFunctionalityDTO> dtos = new ArrayList<CertificationResultTestFunctionalityDTO>();
		
		for (CertificationResultTestFunctionalityEntity entity : entities){
			CertificationResultTestFunctionalityDTO dto = new CertificationResultTestFunctionalityDTO(entity);
			dtos.add(dto);	
		}
		return dtos;
	}
	
	public CertificationResultTestFunctionalityDTO addTestFunctionalityMapping(CertificationResultTestFunctionalityDTO dto) throws EntityCreationException {
		CertificationResultTestFunctionalityEntity mapping = new CertificationResultTestFunctionalityEntity();
		mapping = new CertificationResultTestFunctionalityEntity();
		mapping.setCertificationResultId(dto.getCertificationResultId());
		mapping.setTestFunctionalityId(dto.getTestFunctionalityId());
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		
		return new CertificationResultTestFunctionalityDTO(mapping);
	}

	public void deleteTestFunctionalityMapping(Long mappingId){
		CertificationResultTestFunctionalityEntity toDelete = getCertificationResultTestFunctionalityById(mappingId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}
	
	private CertificationResultTestFunctionalityEntity getCertificationResultTestFunctionalityById(Long id) {
		CertificationResultTestFunctionalityEntity entity = null;
		
		Query query = entityManager.createQuery( "SELECT tf "
				+ "FROM CertificationResultTestFunctionalityEntity tf "
				+ "LEFT OUTER JOIN FETCH tf.testFunctionality "
				+ "where (NOT tf.deleted = true) AND (tf.id = :entityid) ", 
				CertificationResultTestFunctionalityEntity.class );
		query.setParameter("entityid", id);
		List<CertificationResultTestFunctionalityEntity> result = query.getResultList();

		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private List<CertificationResultTestFunctionalityEntity> getTestFunctionalityForCertification(Long certificationResultId){
		Query query = entityManager.createQuery( "SELECT tf "
				+ "FROM CertificationResultTestFunctionalityEntity tf "
				+ "LEFT OUTER JOIN FETCH tf.testFunctionality "
				+ "where (NOT tf.deleted = true) "
				+ "AND (certification_result_id = :certificationResultId) ", 
				CertificationResultTestFunctionalityEntity.class );
		query.setParameter("certificationResultId", certificationResultId);
		
		List<CertificationResultTestFunctionalityEntity> result = query.getResultList();
		if(result == null) {
			return null;
		}
		return result;
	}
}
