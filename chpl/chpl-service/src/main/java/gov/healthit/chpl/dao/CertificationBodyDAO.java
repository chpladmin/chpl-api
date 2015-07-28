package gov.healthit.chpl.dao;

import gov.healthit.chpl.entity.CertificationBodyEntity;

import java.util.List;

public interface CertificationBodyDAO {
	
	public void create(CertificationBodyEntity acb);

	public void delete(Long contactId);

	public List<CertificationBodyEntity> findAll();

	public CertificationBodyEntity getById(Long id);

	public void update(CertificationBodyEntity contact);
	
}
