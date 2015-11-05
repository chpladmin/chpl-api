package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.certifiedProduct.validation.PendingCertifiedProductValidator;
import gov.healthit.chpl.certifiedProduct.validation.PendingCertifiedProductValidatorFactory;
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
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.AdditionalSoftware;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
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
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
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
	
	@Autowired
	public ActivityManager activityManager;
	
	@Autowired
	public CertifiedProductDetailsManager detailsManager;
		
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
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		CertifiedProductDTO toCreate = new CertifiedProductDTO();
		toCreate.setAcbCertificationId(pendingCp.getAcbCertificationId());
		
		String certifyingBodyId = null;
		if(pendingCp.getCertifyingBody().get("id") == null) {
			CertificationBodyDTO acbDto = new CertificationBodyDTO();
			acbDto.setName(pendingCp.getCertifyingBody().get("name").toString());
			if(StringUtils.isEmpty(acbDto.getName())) {
				throw new EntityCreationException("Cannot create a certifying body without a name.");
			}
			acbDto = acbDao.create(acbDto);
			certifyingBodyId = acbDto.getId().toString();
		} else {
			certifyingBodyId = pendingCp.getCertifyingBody().get("id").toString();
		}
		toCreate.setCertificationBodyId(new Long(certifyingBodyId));
		
		if(pendingCp.getCertificationEdition().get("id") == null) {
			throw new EntityCreationException("The ID of an existing certification edition (year) must be provided. A new certification edition cannot be created via this process.");
		}
		String certificationEditionId = pendingCp.getCertificationEdition().get("id").toString();
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
		
		//can be null
		if(pendingCp.getPracticeType().get("id") != null) {
			String practiceTypeId = pendingCp.getPracticeType().get("id").toString();
			toCreate.setPracticeTypeId(new Long(practiceTypeId));
		}
		
		//can be null
		if(pendingCp.getClassificationType().get("id") != null) {
			String productClassificationTypeId = pendingCp.getClassificationType().get("id").toString();
			toCreate.setProductClassificationTypeId(new Long(productClassificationTypeId));
		}
		
		String vendorId = null; 
		if(pendingCp.getVendor().get("id") == null) {
			VendorDTO newVendor = new VendorDTO();
			if(pendingCp.getVendor().get("name") == null) {
				throw new EntityCreationException("You must provide a vendor name to create a new vendor.");
			}
			newVendor.setName(pendingCp.getVendor().get("name").toString());
			Map<String, Object> vendorAddress = pendingCp.getVendorAddress();
			if(vendorAddress != null) {
				AddressDTO address = new AddressDTO();
				if(vendorAddress.get("line1") != null) {
					address.setStreetLineOne(vendorAddress.get("line1").toString());
				}
				if(vendorAddress.get("city") != null) {
					address.setCity(vendorAddress.get("city").toString());
				}
				if(vendorAddress.get("state") != null) {
					address.setState(vendorAddress.get("state").toString());
				}
				if(vendorAddress.get("zipcode") != null) {
					address.setZipcode(vendorAddress.get("zipcode").toString());
				}
				address.setCountry("USA");
				newVendor.setAddress(address);
			}
			
			newVendor = vendorDao.create(newVendor);
			vendorId = newVendor.getId().toString();
		} else {
			vendorId = pendingCp.getVendor().get("id").toString();
		}
		
		String productId = null;
		if(pendingCp.getProduct().get("id") == null) {
			ProductDTO newProduct = new ProductDTO();
			if(pendingCp.getProduct().get("name") == null) {
				throw new EntityCreationException("Either product name or ID must be provided.");
			}
			newProduct.setName(pendingCp.getProduct().get("name").toString());
			newProduct.setVendorId(new Long(vendorId));
			newProduct.setReportFileLocation(pendingCp.getReportFileLocation());
			newProduct = productDao.create(newProduct);
			productId = newProduct.getId().toString();
		} else {
			productId = pendingCp.getProduct().get("id").toString();
		}
		
		String productVersionId = null;
		if(pendingCp.getProduct().get("versionId") == null) {
			ProductVersionDTO newVersion = new ProductVersionDTO();
			if(pendingCp.getProduct().get("version") == null) {
				throw new EntityCreationException("Either version id or version must be provided.");
			}
			newVersion.setVersion(pendingCp.getProduct().get("version").toString());
			newVersion.setProductId(new Long(productId));
			newVersion = versionDao.create(newVersion);
			productVersionId = newVersion.getId().toString();
		} else {
			productVersionId = pendingCp.getProduct().get("versionId").toString();
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
				if(StringUtils.isEmpty(cqmResult.getCmsId())) {
					criterion = cqmCriterionDao.getNQFByNumber(cqmResult.getNumber());
				} else if(cqmResult.getCmsId().startsWith("CMS")) {
					criterion = cqmCriterionDao.getCMSByNumberAndVersion(cqmResult.getCmsId(), cqmResult.getVersion());
				}
				
				if(criterion == null) {
					throw new EntityCreationException("Could not find a CQM with number " + cqmResult.getCmsId() + 
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
		
		CertifiedProductSearchDetails details = detailsManager.getCertifiedProductDetails(newCertifiedProduct.getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, details.getId(), "Certified Product "+newCertifiedProduct.getId()+" was created.", null, details);
		
		return newCertifiedProduct;
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = false) 
	public CertifiedProductDTO changeOwnership(Long certifiedProductId, Long acbId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		CertifiedProductDTO toUpdate = dao.getById(certifiedProductId);
		toUpdate.setCertificationBodyId(acbId);
		return update(acbId, toUpdate);
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
	public CertifiedProductDTO update(Long acbId, CertifiedProductDTO dto) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		CertifiedProductSearchDetails before = detailsManager.getCertifiedProductDetails(dto.getId());
		CertifiedProductDTO result = dao.update(dto);
		CertifiedProductSearchDetails after = detailsManager.getCertifiedProductDetails(result.getId());
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, result.getId(), "Certified Product "+result.getId()+" was updated." , before , after);
		return result;
	}
	
	/**
	 * both successes and failures are passed in
	 * @throws JsonProcessingException 
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void replaceCertifications(Long acbId, CertifiedProductDTO productDto, Map<CertificationCriterionDTO, Boolean> certResults)
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		CertifiedProductSearchDetails before = detailsManager.getCertifiedProductDetails(productDto.getId());
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
		
		CertifiedProductSearchDetails after = detailsManager.getCertifiedProductDetails(productDto.getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, productDto.getId(), "Certifications for Certified Product "+productDto.getId()+" was updated." , before , after);
	}
	
	
	/**
	 * both successes and failures are passed in
	 * @throws JsonProcessingException 
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void updateCertifications(Long acbId, CertifiedProductDTO productDto, Map<CertificationCriterionDTO, Boolean> certResults)
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		CertifiedProductSearchDetails before = detailsManager.getCertifiedProductDetails(productDto.getId());
		//delete existing certifiations for the product
		//certDao.deleteByCertifiedProductId(productDto.getId());
		List<CertificationResultDTO> oldCertificationResults = certDao.findByCertifiedProductId(productDto.getId());
		
		for (CertificationResultDTO oldResult : oldCertificationResults){
			
			Long certificationCriterionId = oldResult.getCertificationCriterionId();
			CertificationCriterionDTO criterionDTO = certCriterionDao.getById(certificationCriterionId);
			
			for (Map.Entry<CertificationCriterionDTO, Boolean> certResult : certResults.entrySet()){
				if (certResult.getKey().getNumber().equals(criterionDTO.getNumber())){	
					// replace the value of the result
					oldResult.setSuccessful(certResult.getValue());
					certDao.update(oldResult);
					break;
				}
			}
		}
		
		CertifiedProductSearchDetails after = detailsManager.getCertifiedProductDetails(productDto.getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, productDto.getId(), "Certifications for Certified Product "+productDto.getId()+" was updated." , before , after);
	}
	
	
	/**
	 * for NQF's, both successes and failures are passed in.
	 * for CMS's it is only those which were successful
	 * @throws JsonProcessingException 
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void replaceCqms(Long acbId, CertifiedProductDTO productDto, Map<CQMCriterionDTO, Boolean> cqmResults) 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		CertifiedProductSearchDetails before = detailsManager.getCertifiedProductDetails(productDto.getId());
		
		cqmResultDAO.deleteByCertifiedProductId(productDto.getId());
		
		for(CQMCriterionDTO cqmDto : cqmResults.keySet()) {		
			CQMCriterionDTO criterion = null;
			if(StringUtils.isEmpty(cqmDto.getCmsId())) {
				criterion = cqmCriterionDao.getNQFByNumber(cqmDto.getNumber());
			} else if(cqmDto.getCmsId().startsWith("CMS")) {
				criterion = cqmCriterionDao.getCMSByNumberAndVersion(cqmDto.getCmsId(), cqmDto.getCqmVersion());
			}
			
			if(criterion == null) {
				throw new EntityRetrievalException("Could not find CQM with number " + cqmDto.getCmsId() + " and version " + cqmDto.getCqmVersion());
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
		
		CertifiedProductSearchDetails after = detailsManager.getCertifiedProductDetails(productDto.getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, productDto.getId(), "CQMs for Certified Product "+productDto.getId()+" was updated." , before , after);
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public void replaceAdditionalSoftware(Long acbId, CertifiedProductDTO productDto, List<AdditionalSoftwareDTO> newSoftware) 
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		CertifiedProductSearchDetails before = detailsManager.getCertifiedProductDetails(productDto.getId());
		
		softwareDao.deleteByCertifiedProduct(productDto.getId());
		for(AdditionalSoftwareDTO software : newSoftware) {
			software.setCertifiedProductId(productDto.getId());
			softwareDao.create(software);
		}
		
		CertifiedProductSearchDetails after = detailsManager.getCertifiedProductDetails(productDto.getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, productDto.getId(), "Additional Software for Certified Product "+productDto.getId()+" was updated." , before , after);
		
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