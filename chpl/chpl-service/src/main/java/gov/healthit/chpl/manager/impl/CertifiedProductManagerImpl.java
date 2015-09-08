package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;

@Service
public class CertifiedProductManagerImpl implements CertifiedProductManager {

	@Autowired CertifiedProductDAO dao;
	
	@Override
	@Transactional(readOnly = true)
	public CertifiedProductDTO getById(Long id) throws EntityRetrievalException {
		return dao.getById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CertifiedProductDTO> getByVersion(Long versionId) {
		return dao.getByVersionId(versionId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CertifiedProductDTO> getByVersions(List<Long> versionIds) {
		return dao.getByVersionIds(versionIds);
	}
	
//	@Override
//	@Transactional(readOnly = true)
//	public CertifiedProductDTO create(CertifiedProductDTO dto) throws EntityRetrievalException, EntityCreationException {
//		return dao.create(dto);
//	}
//	
	@Override
	@Transactional(readOnly = true)
	public CertifiedProductDTO update(CertifiedProductDTO dto) throws EntityRetrievalException {
		return dao.update(dto);
	}
//	
//	@Override
//	@Transactional(readOnly = true)
//	public void delete(CertifiedProductDTO dto) throws EntityRetrievalException {
//		
//	}
//	
//	@Override
//	@Transactional(readOnly = true)
//	public void delete(Long certifiedProductId) throws EntityRetrievalException {
//		
//	}
}