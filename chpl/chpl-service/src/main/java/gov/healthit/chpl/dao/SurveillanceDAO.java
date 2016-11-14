package gov.healthit.chpl.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.entity.PendingSurveillanceEntity;

public interface SurveillanceDAO {
	public Surveillance insertSurveillance(Surveillance surv);
	public Long insertPendingSurveillance(Surveillance surv);
	public PendingSurveillanceEntity getPendingSurveillanceById(Long id);
	public List<PendingSurveillanceEntity> getPendingSurveillanceByAcb(Long acbId);
	public void deleteSurveillance(Surveillance surv) throws EntityNotFoundException ;
	public void deletePendingSurveillance(Surveillance surv) throws EntityNotFoundException;
	
	public SurveillanceType findSurveillanceType(String type);
	public SurveillanceRequirementType findSurveillanceRequirementType(String type);
	public SurveillanceResultType findSurveillanceResultType(String type);
	public SurveillanceNonconformityStatus findSurveillanceNonconformityStatusType(String type);
}
