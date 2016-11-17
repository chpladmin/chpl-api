package gov.healthit.chpl.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.entity.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.SurveillanceEntity;

public interface SurveillanceDAO {
	public Long insertSurveillance(Surveillance surv);
	public Long updateSurveillance(Surveillance newSurv);
	public SurveillanceEntity getSurveillanceByCertifiedProductAndFriendlyId(Long certifiedProductId, String survFriendlyId);
	public SurveillanceEntity getSurveillanceById(Long id);
	public List<SurveillanceEntity> getSurveillanceByCertifiedProductId(Long id);
	public void deleteSurveillance(Surveillance surv) throws EntityNotFoundException ;
	
	public Long insertPendingSurveillance(Surveillance surv);
	public PendingSurveillanceEntity getPendingSurveillanceById(Long id);
	public List<PendingSurveillanceEntity> getPendingSurveillanceByAcb(Long acbId);
	public void deletePendingSurveillance(Surveillance surv) throws EntityNotFoundException;
	
	public List<SurveillanceType> getAllSurveillanceTypes();
	public SurveillanceType findSurveillanceType(String type);
	public SurveillanceType findSurveillanceType(Long id);
	public List<SurveillanceRequirementType> getAllSurveillanceRequirementTypes();
	public SurveillanceRequirementType findSurveillanceRequirementType(String type);
	public SurveillanceRequirementType findSurveillanceRequirementType(Long id);
	public List<SurveillanceResultType> getAllSurveillanceResultTypes();
	public SurveillanceResultType findSurveillanceResultType(String type);
	public SurveillanceResultType findSurveillanceResultType(Long id);
	public List<SurveillanceNonconformityStatus> getAllSurveillanceNonconformityStatusTypes();
	public SurveillanceNonconformityStatus findSurveillanceNonconformityStatusType(String type);
	public SurveillanceNonconformityStatus findSurveillanceNonconformityStatusType(Long id);
}
