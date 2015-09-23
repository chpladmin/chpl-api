package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;

@Service
public class CertifiedProductManagerImpl implements CertifiedProductManager {

	@Autowired CertifiedProductDAO dao;
	@Autowired CertificationResultDAO certDao;
	@Autowired CertificationCriterionDAO certCriterionDao;
	@Autowired CQMResultDAO cqmResultDAO;
	@Autowired CQMCriterionDAO cqmCriterionDao;
	@Autowired AdditionalSoftwareDAO softwareDao;
	
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
	
	/**
	 * both successes and failures are passed in
	 */
	@Override
	@Transactional(readOnly = false)
	public void replaceCertifications(CertifiedProductDTO productDto, Map<CertificationCriterionDTO, Boolean> certResults)
		throws EntityCreationException, EntityRetrievalException {
		//delete existing certifiations for the product
		certDao.deleteByCertifiedProductId(productDto.getId());
		
		//add in new certs for the product
		for(CertificationCriterionDTO newCertDto : certResults.keySet()) {
			CertificationCriterionDTO criterion = null;
			if(newCertDto.getId() == null || newCertDto.getId() < 0) {
				criterion = certCriterionDao.getByName(newCertDto.getNumber());
			} else {
				criterion = certCriterionDao.getById(newCertDto.getId());
			}
			
			if(criterion == null) {
				throw new EntityRetrievalException("Could not find entity with id " + newCertDto.getId() + " or number " + newCertDto.getNumber());
			}
			
			CertificationResultDTO toCreate = new CertificationResultDTO();
			toCreate.setAutomatedMeasureCapable(criterion.getAutomatedMeasureCapable());
			toCreate.setAutomatedNumerator(criterion.getAutomatedNumeratorCapable());
			toCreate.setCertificationCriterionId(criterion.getId());
			toCreate.setCertifiedProduct(productDto.getId());
			toCreate.setCreationDate(new Date());
			toCreate.setDeleted(false);
			toCreate.setLastModifiedDate(new Date());
			toCreate.setLastModifiedUser(Util.getCurrentUser().getId());
			toCreate.setSuccessful(certResults.get(newCertDto));
			toCreate.setInherited(criterion.getParentCriterionId() != null ? true : false);
			certDao.create(toCreate);
		}
	}
	
	/**
	 * for NQF's, both successes and failures are passed in.
	 * for CMS's it is only those which were successful
	 */
	@Override
	@Transactional(readOnly = false)
	public void replaceCqms(CertifiedProductDTO productDto, Map<CQMCriterionDTO, Boolean> cqmResults) 
			throws EntityRetrievalException, EntityCreationException {
		cqmResultDAO.deleteByCertifiedProductId(productDto.getId());
		
		for(CQMCriterionDTO cqmDto : cqmResults.keySet()) {		
			CQMCriterionDTO criterion = null;
			if(cqmDto.getNumber().startsWith("NQF")) {
				criterion = cqmCriterionDao.getByNumber(cqmDto.getNumber());
			} else if(cqmDto.getNumber().startsWith("CMS")) {
				criterion = cqmCriterionDao.getByNumberAndVersion(cqmDto.getNumber(), cqmDto.getCqmVersion());
			}
			
			if(criterion == null) {
				throw new EntityRetrievalException("Could not find CQM with number " + cqmDto.getNumber() + " and version " + cqmDto.getCqmVersion());
			}
			
			CQMResultDTO toCreate = new CQMResultDTO();
			toCreate.setCqmCriterionId(criterion.getId());
			toCreate.setCertifiedProductId(productDto.getId());
			toCreate.setCreationDate(new Date());
			toCreate.setDeleted(false);
			toCreate.setLastModifiedDate(new Date());
			toCreate.setLastModifiedUser(Util.getCurrentUser().getId());
			toCreate.setSuccess(cqmResults.get(cqmDto));
			cqmResultDAO.create(toCreate);
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void replaceAdditionalSoftware(CertifiedProductDTO productDto, List<AdditionalSoftwareDTO> newSoftware) 
		throws EntityCreationException {
		softwareDao.deleteByCertifiedProduct(productDto.getId());
		
		for(AdditionalSoftwareDTO software : newSoftware) {
			software.setCertifiedProductId(productDto.getId());
			softwareDao.create(software);
		}
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