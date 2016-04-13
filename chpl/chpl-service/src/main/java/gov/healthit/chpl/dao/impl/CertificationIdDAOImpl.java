package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.CertificationIdDAO;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.entity.CertificationIdEntity;
import gov.healthit.chpl.entity.CertificationIdProductMapEntity;

@Repository("certificationIdDAO")
public class CertificationIdDAOImpl extends BaseDAOImpl implements CertificationIdDAO {

	@Override
	@Transactional
	public CertificationIdDTO create(CertificationIdDTO dto) throws EntityCreationException {
		
		CertificationIdEntity entity = null;
		try {
			if (dto.getId() != null){
				entity = this.getEntityById(dto.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {
			
			entity = new CertificationIdEntity();
			entity.setCertificationId(dto.getCertificationId());
			entity.setAttestationYearId(dto.getAttestationYearId());
			entity.setPracticeTypeId(dto.getPracticeTypeId());
			
			if(dto.getLastModifiedUser() != null) {
				entity.setLastModifiedUser(dto.getLastModifiedUser());
			} else {
				entity.setLastModifiedUser(Util.getCurrentUser().getId());
			}		
			
			if(dto.getLastModifiedDate() != null) {
				entity.setLastModifiedDate(dto.getLastModifiedDate());
			} else {
				entity.setLastModifiedDate(new Date());
			}
			
			if(dto.getCreationDate() != null) {
				entity.setCreationDate(dto.getCreationDate());
			} else {
				entity.setCreationDate(new Date());
			}
			
			create(entity);	
			return new CertificationIdDTO(entity);
		}
		
	}

	@Override
	@Transactional
	public CertificationIdEntity update(CertificationIdDTO dto) throws EntityRetrievalException {
		CertificationIdEntity entity = this.getEntityById(dto.getId());
		
		if(dto.getCertificationId() != null) {
			entity.setCertificationId(dto.getCertificationId());
		}
		
		if(dto.getAttestationYearId() != null)
		{
			entity.setAttestationYearId(dto.getAttestationYearId());
		}

		if(dto.getPracticeTypeId() != null)
		{
			entity.setPracticeTypeId(dto.getPracticeTypeId());
		}
		
		if(dto.getCreationDate() != null) {
			entity.setCreationDate(dto.getCreationDate());
		} 
		
		if(dto.getLastModifiedUser() != null) {
			entity.setLastModifiedUser(dto.getLastModifiedUser());
		} else {
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
		}		
		
		if(dto.getLastModifiedDate() != null) {
			entity.setLastModifiedDate(dto.getLastModifiedDate());
		} else {
			entity.setLastModifiedDate(new Date());
		}
		
		update(entity);
		return entity;
	}

	@Override
	@Transactional
	public void delete(Long id) throws EntityRetrievalException {
		CertificationIdEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
	}

	@Override
	public List<CertificationIdDTO> findAll() {
		
		List<CertificationIdEntity> entities = getAllEntities();
		List<CertificationIdDTO> dtos = new ArrayList<>();
		
		for (CertificationIdEntity entity : entities) {
			CertificationIdDTO dto = new CertificationIdDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public CertificationIdDTO getById(Long id) throws EntityRetrievalException {
		
		CertificationIdEntity entity = getEntityById(id);
		if(entity == null) { 
			return null;
		}
		CertificationIdDTO dto = new CertificationIdDTO(entity);
		return dto;
		
	}

	@Override
	public CertificationIdDTO getByCertificationId(String certificationId) throws EntityRetrievalException {
		
		CertificationIdEntity entity = getEntityByCertificationId(certificationId);
		if(entity == null) { 
			return null;
		}
		CertificationIdDTO dto = new CertificationIdDTO(entity);
		return dto;
		
	}

	@Override
	public CertificationIdDTO getByProductIds(List<Long> productIds) throws EntityRetrievalException {
		
		Long editionYearId = 2L;
		
		CertificationIdEntity entity = getEntityByProductIds(productIds, editionYearId);
		if(entity == null) {
			return null;
		}
		CertificationIdDTO dto = new CertificationIdDTO(entity);
		return dto;
		
	}
	
	private void create(CertificationIdEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	private void update(CertificationIdEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<CertificationIdEntity> getAllEntities() {
		
		List<CertificationIdEntity> result = entityManager.createQuery( "from CertificationIdEntity ", CertificationIdEntity.class).getResultList();
		return result;
		
	}
	
	private CertificationIdEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CertificationIdEntity entity = null;
			
		Query query = entityManager.createQuery( "from CertificationIdEntity where (ehr_certification_id_id = :entityid) ", CertificationIdEntity.class );
		query.setParameter("entityid", id);
		List<CertificationIdEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate certificationId id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}
		
		return entity;
	}

	private CertificationIdEntity getEntityByCertificationId(String certificationId) throws EntityRetrievalException {
		
		CertificationIdEntity entity = null;
			
		Query query = entityManager.createQuery( "from CertificationIdEntity where (certification_id = :certid) ", CertificationIdEntity.class );
		query.setParameter("certid", certificationId);
		List<CertificationIdEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate certificationId in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private CertificationIdEntity getEntityByProductIds(List<Long> productIds, Long editionYearId) throws EntityRetrievalException {

		CertificationIdEntity entity = null;

		// Lookup the EHR Certification ID record by:
		// 1. Looking up all CertificationIDs that are associated with the products.
		// 2. Reduce the set by removing records that contain products other than those specified.
		// 3. Make sure the number of products for the CertID matches the number of products specified,
		//		this filters out CertIDs that only contain a subset of those products specified.
		List<CertificationIdEntity> result = new ArrayList<CertificationIdEntity>();
		Query query = entityManager.createQuery(
				
			"from CertificationIdEntity " +
			"where ehr_certification_id_id in (" +
			
				"select mpx.certificationIdId " +
				"from CertificationIdProductMapEntity as mpx " +
				"where mpx.certifiedProductId in :productIds " +
				"and mpx.certificationIdId not in ( " +
					"select mpa.certificationIdId " +
					"from CertificationIdProductMapEntity as mpa " +
					"where mpa.certificationIdId in ( " +
						"select mpy.certificationIdId " +
						"from CertificationIdProductMapEntity as mpy " +
						"where mpy.certifiedProductId in :productIds " +
						"group by mpy.certificationIdId " +
					") " +
					"and mpa.certifiedProductId not in :productIds " +
					"group by mpa.certificationIdId " +
				") " +
				"group by mpx.certificationIdId " +
				"having count(mpx.certificationIdId) = :productCount " +
				
			") " +
			"and certification_edition_id = :editionYearId",
			CertificationIdEntity.class
		);
		
		query.setParameter("productIds", productIds);
		query.setParameter("productCount", new Long(productIds.size()));
		query.setParameter("editionYearId", editionYearId);
		result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate certificationId in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}
		
		return entity;
	}

    private static String encodeCollectionKey(List<Long> products) {
        
        // Sort the product numbers before we encode them so they are in order
		Collections.sort(products);
        
        // Collect Hex version of all numbers.
		String numbers = "";
        for (Long id : products) {
            String encodedNumber = String.format("%05X", id);
			numbers = numbers + encodedNumber;
        }
        
        return numbers.toUpperCase();
    }
	
}
