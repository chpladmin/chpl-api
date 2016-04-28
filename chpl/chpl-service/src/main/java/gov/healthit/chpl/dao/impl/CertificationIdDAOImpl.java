package gov.healthit.chpl.dao.impl;

import java.lang.StringBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

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

	public static String CERT_ID_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static int CERT_ID_LENGTH = 15;
	private static long MODIFIED_USER_ID = -4L

	@Override
	@Transactional
	public CertificationIdDTO create(List<Long> productIds, String year) throws EntityCreationException {
		CertificationIdEntity entity = null;
		CertificationIdDTO newDto = null;
			
		if (null != entity) {
			throw new EntityCreationException("An entity with this Certification ID already exists.");
		} else {
				
			// Create a new EHR Certification ID record
			entity = new CertificationIdEntity();
			entity.setCertificationId(this.generateCertificationIdString(year));
			entity.setYear(year);
			entity.setKey(this.encodeCollectionKey(productIds));
			entity.setLastModifiedDate(new Date());
			entity.setCreationDate(new Date());
			entity.setLastModifiedUser(MODIFIED_USER_ID);
			entity.setPracticeTypeId(null);

			// Store the map entities
			entityManager.persist(entity);
			try {
				entity = getEntityByCertificationId(entity.getCertificationId());
			} catch (EntityRetrievalException e) {
				throw new EntityCreationException("Unable to create Certification ID and Product Map.");
			}
			newDto = new CertificationIdDTO(entity);

			// Create map records
			for (Long prodId : productIds) {
				CertificationIdProductMapEntity mapEntity = new CertificationIdProductMapEntity();
				mapEntity.setCertifiedProductId(prodId);
				mapEntity.setCertificationIdId(newDto.getId());
				mapEntity.setLastModifiedDate(new Date());
				mapEntity.setCreationDate(new Date());
				mapEntity.setLastModifiedUser(MODIFIED_USER_ID);
				mapEntity.setDeleted(false);
				entityManager.persist(mapEntity);
			}

			// Store the map entities
			entityManager.flush();
		}
		
		return newDto;
	}
	
	@Override
	@Transactional
	public CertificationIdDTO create(CertificationIdDTO dto) throws EntityCreationException {
		
		CertificationIdEntity entity = null;
		try {
			if (null != dto.getId()) 
				entity = this.getEntityById(dto.getId());
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this record ID or Certification ID already exists.");
		} else {
			
			entity = new CertificationIdEntity();
			entity.setCertificationId(dto.getCertificationId());
			entity.setYear(dto.getYear());
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
		
		if(dto.getYear() != null)
		{
			entity.setYear(dto.getYear());
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
	public CertificationIdDTO getByProductIds(List<Long> productIds, String year) throws EntityRetrievalException {
		
		CertificationIdEntity entity = getEntityByProductIds(productIds, year);
		if (entity == null) {
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
	
	private CertificationIdEntity getEntityByProductIds(List<Long> productIds, String year) throws EntityRetrievalException {

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
			"and year = :year",
			CertificationIdEntity.class
		);
		
		query.setParameter("productIds", productIds);
		query.setParameter("productCount", new Long(productIds.size()));
		query.setParameter("year", year);
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

	private static String generateCertificationIdString(String year) {
		// Form the EHR Certification ID prefix and edition year identifier.
		// The identifier begins with the two-digit year followed by an "E" to indicate
		// an edition year (e.g. "2015") or "H" to indicate a hybrid edition year (e.g. "2014/2015").
		// To create it we take the last two digits of the year value which would
		// represent the highest (current) year number...
		StringBuffer newId = new StringBuffer("00");
		newId.append(year.substring(year.length() - 2));
		
		// ...Decide if it's a hybrid year or not and attach the "E" or "H".
		if (-1 == year.indexOf("/")) {
			newId.append("E");
		} else {
			newId.append("H");
		}
		
		int suffixLength = (CERT_ID_LENGTH - newId.length());

		// Generate the remainder of the ID
		for (int i = 0; i < suffixLength; ++i) {
			newId.append(CERT_ID_CHARS.charAt(new Random().nextInt(CERT_ID_CHARS.length())));
		}

		// Safeguard we have a proper ID
		if (newId.length() != CERT_ID_LENGTH) {
			return null;
		}

		return newId.toString();
	}
}
