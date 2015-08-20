package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.entity.VendorEntity;
import gov.healthit.chpl.manager.VendorManager;

@Service
public class VendorManagerImpl implements VendorManager {

	@Autowired
	VendorDAO vendorDao;
	
	@Override
	@Transactional(readOnly = true)
	public List<VendorDTO> getAll() {
		return vendorDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public VendorDTO getById(Long id) throws EntityRetrievalException {
		return vendorDao.getById(id);
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = false)
	public VendorDTO update(VendorDTO vendor) throws EntityRetrievalException {
		VendorEntity result = vendorDao.update(vendor);
		return new VendorDTO(result);
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = false)
	public VendorDTO create(VendorDTO dto) throws EntityRetrievalException, EntityCreationException {
		VendorEntity result = vendorDao.create(dto);
		return new VendorDTO(result);
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = false)
	public void delete(VendorDTO dto) {
		vendorDao.delete(dto.getId());
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = false)
	public void delete(Long vendorId) {
		vendorDao.delete(vendorId);
	}
}
