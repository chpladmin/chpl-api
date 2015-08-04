package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CQMResultDTO;

public interface CQMResultDAO {
	
	public void create(CQMResultDTO cqmResult);
	public void delete(Long cqmResultId);
	public List<CQMResultDTO> findAll();
	public CQMResultDTO getById(Long cqmResultId);
	public void update(CQMResultDTO cqmResult);

}
