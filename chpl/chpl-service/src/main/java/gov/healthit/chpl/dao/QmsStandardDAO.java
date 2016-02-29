package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.QmsStandardDTO;

public interface QmsStandardDAO {
	
	public QmsStandardDTO create(QmsStandardDTO dto) throws EntityCreationException, EntityRetrievalException;	
	public QmsStandardDTO update(QmsStandardDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<QmsStandardDTO> findAll();
	public QmsStandardDTO getById(Long id) throws EntityRetrievalException;
	public QmsStandardDTO getByName(String name) ;
}
