package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.SurveillanceCertificationResultDTO;

public interface SurveillanceCertificationResultDAO {
	public SurveillanceCertificationResultDTO create(SurveillanceCertificationResultDTO toCreate) throws EntityCreationException,
		EntityRetrievalException;
	public SurveillanceCertificationResultDTO update(SurveillanceCertificationResultDTO toUpdate) throws EntityRetrievalException;
	public SurveillanceCertificationResultDTO getById(Long id) throws EntityRetrievalException;
	public List<SurveillanceCertificationResultDTO> getAllForSurveillance(Long surveillanceId);
	public void delete(Long id) throws EntityRetrievalException;
}
