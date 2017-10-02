package gov.healthit.chpl.dao.impl;

import java.util.Date;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.MeaningfulUseDAO;
import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;
import gov.healthit.chpl.entity.MeaningfulUseAccurateAsOfEntity;

@Repository("meaningfulUseDAO")
public class MeaningfulUseDAOImpl extends BaseDAOImpl implements MeaningfulUseDAO {

	@Transactional
	public MeaningfulUseAccurateAsOfDTO getMeaningfulUseAccurateAsOf(){
		Query query = entityManager.createQuery("SELECT muu from MeaningfulUseAccurateAsOfEntity muu where (NOT muu.deleted = true)",
				MeaningfulUseAccurateAsOfEntity.class);
		MeaningfulUseAccurateAsOfEntity entity = null;
		entity = (MeaningfulUseAccurateAsOfEntity) query.getSingleResult();
		MeaningfulUseAccurateAsOfDTO muuAccurateDTO = new MeaningfulUseAccurateAsOfDTO(entity);
		return muuAccurateDTO;
	}

	@Transactional
	public MeaningfulUseAccurateAsOfDTO updateAccurateAsOf(MeaningfulUseAccurateAsOfDTO muuAccurateDTO){
		MeaningfulUseAccurateAsOfEntity muuEntity = new MeaningfulUseAccurateAsOfEntity();
		muuEntity.setId(muuAccurateDTO.getId());
		muuEntity.setAccurateAsOfDate(muuAccurateDTO.getAccurateAsOfDate());
		muuEntity.setCreationDate(muuAccurateDTO.getCreationDate());
		muuEntity.setLastModifiedDate(new Date());
		muuEntity.setLastModifiedUser(Util.getCurrentUser().getId());
		muuEntity.setDeleted(false);
		update(muuEntity);
		MeaningfulUseAccurateAsOfDTO dto = new MeaningfulUseAccurateAsOfDTO(muuEntity);
		return dto;
	}

	private void update(MeaningfulUseAccurateAsOfEntity entity) {
		entityManager.merge(entity);
		entityManager.flush();
	}
}
