package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEventDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.EventTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.domain.AdditionalSoftware;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationEventDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.EventTypeDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.manager.CertifiedProductManager;

@Service
public class CertifiedProductManagerImpl implements CertifiedProductManager {

	@Autowired CertifiedProductDAO dao;
	@Autowired CertificationResultDAO certDao;
	@Autowired CertificationCriterionDAO certCriterionDao;
	@Autowired CQMResultDAO cqmResultDAO;
	@Autowired CQMCriterionDAO cqmCriterionDao;
	@Autowired AdditionalSoftwareDAO softwareDao;
	@Autowired CertificationBodyDAO acbDao;
	@Autowired VendorDAO vendorDao;
	@Autowired ProductDAO productDao;
	@Autowired ProductVersionDAO versionDao;
	@Autowired CertificationEventDAO eventDao;
	@Autowired EventTypeDAO eventTypeDao;
		
	public CertifiedProductManagerImpl() {
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CertifiedProductDTO> getAll() {
		return dao.findAll();
	}
	@Autowired CertificationStatusDAO statusDao;
	
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
	
	@Override
	@PreAuthorize("(hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) "
			+ "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	@Transactional(readOnly = false)
	public CertifiedProductDTO createFromPending(Long acbId, PendingCertifiedProductDetails pendingCp) 
			throws EntityRetrievalException, EntityCreationException {
		CertifiedProductDTO toCreate = new CertifiedProductDTO();
		toCreate.setAcbCertificationId(pendingCp.getAcbCertificationId());
		
		String certifyingBodyId = pendingCp.getCertifyingBody().get("id").toString();
		if(StringUtils.isEmpty(certifyingBodyId)) {
			CertificationBodyDTO acbDto = new CertificationBodyDTO();
			acbDto.setName(pendingCp.getCertifyingBody().get("name").toString());
			if(StringUtils.isEmpty(acbDto.getName())) {
				throw new EntityCreationException("Cannot create a certifying body without a name.");
			}
			acbDto = acbDao.create(acbDto);
			certifyingBodyId = acbDto.getId().toString();
		}
		toCreate.setCertificationBodyId(new Long(certifyingBodyId));
		
		String certificationEditionId = pendingCp.getCertificationEdition().get("id").toString();
		if(StringUtils.isEmpty(certificationEditionId)) {
			throw new EntityCreationException("The ID of an existing certification edition (year) must be provided. A new certification edition cannot be created via this process.");
		}
		toCreate.setCertificationEditionId(new Long(certificationEditionId));
		
		String status = pendingCp.getRecordStatus();
		if(StringUtils.isEmpty(status)) {
			throw new EntityCreationException("Cannot determine certification status. Is this a new record? An update? A removal?");
		}
		if(status.trim().equalsIgnoreCase("new")) {
			CertificationStatusDTO statusDto = statusDao.getByStatusName("Active");
			toCreate.setCertificationStatusId(statusDto.getId());
		} //TODO: handle the other cases?
		
		toCreate.setCreationDate(new Date());
		toCreate.setDeleted(false);
		toCreate.setLastModifiedDate(new Date());
		toCreate.setLastModifiedUser(Util.getCurrentUser().getId());
		
		String practiceTypeId = pendingCp.getPracticeType().get("id").toString();
		//can be null
		if(!StringUtils.isEmpty(practiceTypeId)) {
			toCreate.setPracticeTypeId(new Long(practiceTypeId));
		}
		
		String classificationTypeId = pendingCp.getClassificationType().get("id").toString();
		//can be null
		if(!StringUtils.isEmpty(classificationTypeId)) {
			toCreate.setProductClassificationTypeId(new Long(classificationTypeId));
		}
		
		String vendorId = pendingCp.getVendor().get("id").toString();
		if(StringUtils.isEmpty(vendorId)) {
			VendorDTO newVendor = new VendorDTO();
			String vendorName = pendingCp.getVendor().get("name").toString();
			if(StringUtils.isEmpty(vendorName)) {
				throw new EntityCreationException("You must provide a vendor name to create a new vendor.");
			}
			newVendor.setName(vendorName);
			Map<String, Object> vendorAddress = pendingCp.getVendorAddress();
			if(vendorAddress != null) {
				AddressDTO address = new AddressDTO();
				address.setStreetLineOne(vendorAddress.get("line1").toString());
				address.setCity(vendorAddress.get("city").toString());
				address.setState(vendorAddress.get("state").toString());
				address.setZipcode(vendorAddress.get("zipcode").toString());
				address.setCountry("USA");
				newVendor.setAddress(address);
			}
			
			newVendor = vendorDao.create(newVendor);
			vendorId = newVendor.getId().toString();
		}
		
		String productId = pendingCp.getProduct().get("id").toString();
		if(StringUtils.isEmpty(productId)) {
			ProductDTO newProduct = new ProductDTO();
			String productName = pendingCp.getProduct().get("name").toString();
			if(StringUtils.isEmpty(productName)) {
				throw new EntityCreationException("Either product name or ID must be provided.");
			}
			newProduct.setName(productName);
			newProduct.setVendorId(new Long(vendorId));
			newProduct.setReportFileLocation(pendingCp.getReportFileLocation());
			newProduct = productDao.create(newProduct);
			productId = newProduct.getId().toString();
		}
		
		String productVersionId = pendingCp.getProduct().get("versionId").toString();
		if(StringUtils.isEmpty(productVersionId)) {
			ProductVersionDTO newVersion = new ProductVersionDTO();
			String version = pendingCp.getProduct().get("version").toString();
			if(StringUtils.isEmpty(version)) {
				throw new EntityCreationException("Either version id or version must be provided.");
			}
			newVersion.setVersion(version);
			newVersion.setProductId(new Long(productId));
			newVersion = versionDao.create(newVersion);
			productVersionId = newVersion.getId().toString();
		}
		
		toCreate.setProductVersionId(new Long(productVersionId));
		toCreate.setReportFileLocation(pendingCp.getReportFileLocation());
		toCreate.setVisibleOnChpl(true);
		
		CertifiedProductDTO newCertifiedProduct = dao.create(toCreate);
		
		//additional software
		if(pendingCp.getAdditionalSoftware() != null && pendingCp.getAdditionalSoftware().size() > 0) {
			for(AdditionalSoftware software : pendingCp.getAdditionalSoftware()) {
				AdditionalSoftwareDTO newSoftware = new AdditionalSoftwareDTO();
				newSoftware.setCertifiedProductId(newCertifiedProduct.getId());
				newSoftware.setName(software.getName());
				newSoftware.setVersion("-1");
				softwareDao.create(newSoftware);
			}
		}
		
		//certs
		if(pendingCp.getCertificationResults() != null && pendingCp.getCertificationResults().size() > 0) {
			for(CertificationResult certResult : pendingCp.getCertificationResults()) {
				CertificationCriterionDTO criterion = certCriterionDao.getByName(certResult.getNumber());
				if(criterion == null) {
					throw new EntityCreationException("Could not find certification criterion with number " + certResult.getNumber());
				}
				CertificationResultDTO certResultToCreate = new CertificationResultDTO();
				certResultToCreate.setAutomatedMeasureCapable(criterion.getAutomatedMeasureCapable());
				certResultToCreate.setAutomatedNumerator(criterion.getAutomatedNumeratorCapable());
				certResultToCreate.setCertificationCriterionId(criterion.getId());
				certResultToCreate.setCertifiedProduct(newCertifiedProduct.getId());
				certResultToCreate.setCreationDate(new Date());
				certResultToCreate.setDeleted(false);
				certResultToCreate.setLastModifiedDate(new Date());
				certResultToCreate.setLastModifiedUser(Util.getCurrentUser().getId());
				certResultToCreate.setSuccessful(certResult.isSuccess());
				certResultToCreate.setInherited(criterion.getParentCriterionId() != null ? true : false);
				certDao.create(certResultToCreate);
			}
		}
		
		//cqms
		if(pendingCp.getCqmResults() != null && pendingCp.getCqmResults().size() > 0) {
			for(CQMResultDetails cqmResult : pendingCp.getCqmResults()) {
				CQMCriterionDTO criterion = null;
				if(cqmResult.getNumber().startsWith("NQF")) {
					criterion = cqmCriterionDao.getByNumber(cqmResult.getNumber());
				} else if(cqmResult.getNumber().startsWith("CMS")) {
					criterion = cqmCriterionDao.getByNumberAndVersion(cqmResult.getNumber(), cqmResult.getVersion());
				}
				
				if(criterion == null) {
					throw new EntityCreationException("Could not find a CQM with number " + cqmResult.getNumber() + 
							"and/or version " + cqmResult.getVersion() + ".");
				}
				
				CQMResultDTO cqmResultToCreate = new CQMResultDTO();
				cqmResultToCreate.setCqmCriterionId(criterion.getId());
				cqmResultToCreate.setCertifiedProductId(newCertifiedProduct.getId());
				cqmResultToCreate.setCreationDate(new Date());
				cqmResultToCreate.setDeleted(false);
				cqmResultToCreate.setLastModifiedDate(new Date());
				cqmResultToCreate.setLastModifiedUser(Util.getCurrentUser().getId());
				cqmResultToCreate.setSuccess(cqmResult.isSuccess());
				cqmResultDAO.create(cqmResultToCreate);
			}
		}
		
		
		//if all this was successful, insert a certification_event for the certification date, and the date it went active in CHPL (right now)
		EventTypeDTO certificationEventType = eventTypeDao.getByName("Certification");
		CertificationEventDTO certEvent = new CertificationEventDTO();
		certEvent.setCreationDate(new Date());
		certEvent.setDeleted(false);
		Date certificationDate = new Date(new Long(pendingCp.getCertificationDate()));
		certEvent.setEventDate(certificationDate);
		certEvent.setEventTypeId(certificationEventType.getId());
		certEvent.setLastModifiedDate(new Date());
		certEvent.setLastModifiedUser(Util.getCurrentUser().getId());
		certEvent.setCertifiedProductId(newCertifiedProduct.getId());
		eventDao.create(certEvent);

		//active event
		EventTypeDTO activeEventType = eventTypeDao.getByName("Active");
		CertificationEventDTO activeEvent = new CertificationEventDTO();
		activeEvent.setCreationDate(new Date());
		activeEvent.setDeleted(false);
		activeEvent.setEventDate(new Date());
		activeEvent.setEventTypeId(activeEventType.getId());
		activeEvent.setLastModifiedDate(new Date());
		activeEvent.setLastModifiedUser(Util.getCurrentUser().getId());
		activeEvent.setCertifiedProductId(newCertifiedProduct.getId());
		eventDao.create(activeEvent);
		
		return newCertifiedProduct;
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = false) 
	public CertifiedProductDTO changeOwnership(Long certifiedProductId, Long acbId) throws EntityRetrievalException {
		CertifiedProductDTO toUpdate = dao.getById(certifiedProductId);
		toUpdate.setCertificationBodyId(acbId);
		return dao.update(toUpdate);
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	public CertifiedProductDTO updateCertifiedProductVersion(Long certifiedProductId, Long newVersionId) 
		throws EntityRetrievalException {
		CertifiedProductDTO toUpdate = dao.getById(certifiedProductId);
		toUpdate.setProductVersionId(newVersionId);
		return dao.update(toUpdate);
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public CertifiedProductDTO update(Long acbId, CertifiedProductDTO dto) throws EntityRetrievalException {
		return dao.update(dto);
	}
	
	/**
	 * both successes and failures are passed in
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void replaceCertifications(Long acbId, CertifiedProductDTO productDto, Map<CertificationCriterionDTO, Boolean> certResults)
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
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void replaceCqms(Long acbId, CertifiedProductDTO productDto, Map<CQMCriterionDTO, Boolean> cqmResults) 
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
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void replaceAdditionalSoftware(Long acbId, CertifiedProductDTO productDto, List<AdditionalSoftwareDTO> newSoftware) 
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