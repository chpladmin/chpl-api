package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.SurveillanceDTO;
import gov.healthit.chpl.entity.SurveillanceEntity;

public interface SurveillanceDAO {
	public SurveillanceDTO create(SurveillanceDTO toCreate) throws EntityCreationException,
	EntityRetrievalException;
	public SurveillanceDTO update(SurveillanceDTO toUpdate) throws EntityRetrievalException;
	public SurveillanceDTO getById(Long id) throws EntityRetrievalException;
	public SurveillanceEntity getEntityById(Long id) throws EntityRetrievalException;
	public List<SurveillanceDTO> getAllForCertifiedProduct(Long certifiedProductId);
	public void delete(Long id) throws EntityRetrievalException;
}
