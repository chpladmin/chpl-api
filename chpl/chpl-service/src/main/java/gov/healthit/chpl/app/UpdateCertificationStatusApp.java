package gov.healthit.chpl.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.concept.CertificationBodyConcept;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.SearchMenuManager;

@Component("updateCertificationStatusApp")
public class UpdateCertificationStatusApp extends App {
	@Autowired private CertifiedProductManager cpManager;
	@Autowired private CertificationBodyManager cbManager;
	@Autowired private SearchMenuManager searchMenuManager;
	private static final Logger logger = LogManager.getLogger(UpdateCertificationStatusApp.class);
	
	public static void main(String[] args) throws Exception {
		// setup application
		UpdateCertificationStatusApp updateCertStatus = new UpdateCertificationStatusApp();
		Properties props = updateCertStatus.getProperties();
		updateCertStatus.setLocalContext(props);
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		updateCertStatus.initiateSpringBeans(context, props);
		
		// Get Certification Body for CCHIT
		CertificationBodyDTO cbDTO = updateCertStatus.getCertificationBody(CertificationBodyConcept.CERTIFICATION_BODY_CCHIT.getName());
		// Get certification edition for year 2014
		KeyValueModel certificationEdition = updateCertStatus.getCertificationEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear());
		// Get Map<String, Object> certificationStatus for "Withdrawn by Developer"
		Map<String, Object> updatedCertificationStatus = updateCertStatus.getCertificationStatus(CertificationStatusType.WithdrawnByDeveloper);
		// Get all listings certified by CCHIT with 2014 edition and 'Retired' (216 total according to spreadsheet/DB)
		List<CertifiedProductDTO> listings = updateCertStatus.getListings(cbDTO.getName(), certificationEdition, CertificationStatusType.Retired);
		// Get Map<CertifiedProductDTO, ListingUpdateRequest> for update
		Map<CertifiedProductDTO, ListingUpdateRequest> listingUpdatesMap = updateCertStatus.getListingsUpdateRequests(listings, updatedCertificationStatus);
		// Update each listing's certification status to 'Withdrawn by Developer'
		updateCertStatus.updateListingsCertificationStatus(2L, listingUpdatesMap);
	}

	@Override
	protected void initiateSpringBeans(AbstractApplicationContext context, Properties props) {
		this.setCpManager((CertifiedProductManager)context.getBean("certifiedProductManager"));
		this.setCbManager((CertificationBodyManager)context.getBean("certificationBodyManager"));
		this.setSearchMenuManager((SearchMenuManager)context.getBean("searchMenuManager"));
	}
	
	private List<CertifiedProductDTO> getListings(String certificationBodyName, KeyValueModel certificationEdition, CertificationStatusType certificationStatusType) throws EntityRetrievalException{
		List<CertifiedProductDTO> cps = new ArrayList<CertifiedProductDTO>();
		List<CertifiedProductDetailsDTO> allCpDetails = cpManager.getAll();
		for(CertifiedProductDetailsDTO dto : allCpDetails){
			if(dto.getCertificationBodyName().equalsIgnoreCase(certificationBodyName) 
					&& dto.getCertificationEditionId().longValue() == certificationEdition.getId() 
					&& dto.getCertificationStatusName().equalsIgnoreCase(certificationStatusType.getName())){
				CertifiedProductDTO cpDTO = cpManager.getById(dto.getId());
				cps.add(cpDTO);
			}
		}
		logger.info("Found " + cps.size() + " listings for " + certificationBodyName + " with edition " + certificationEdition + " and status " + certificationStatusType.getName());
		return cps;
	}

	private void updateListingsCertificationStatus(Long acbId, Map<CertifiedProductDTO, ListingUpdateRequest> cpUpdateMap) throws JsonProcessingException, EntityRetrievalException, EntityCreationException{
		for(CertifiedProductDTO cpDTO : cpUpdateMap.keySet()){
			cpManager.update(acbId, cpDTO, cpUpdateMap.get(cpDTO));
			logger.info("Updated CP " + cpDTO.getChplProductNumber() + " for acb id " + acbId + 
					" to Certification Status = '" + cpUpdateMap.get(cpDTO).getListing().getCertificationStatus() + "'.");
		}
	}
	
	private CertificationBodyDTO getCertificationBody(String certificationBodyName){
		List<CertificationBodyDTO> cbDTOs = this.cbManager.getAll(true);
		for(CertificationBodyDTO dto : cbDTOs){
			if(dto.getName().equalsIgnoreCase(certificationBodyName)){
				return dto;
			}
		}
		return null;
	}
	
	private KeyValueModel getCertificationEdition(String certificationEditionYear){
		Set<KeyValueModel> editionNames = this.searchMenuManager.getEditionNames(true);
		for(KeyValueModel editionName : editionNames){
			if(editionName.getName().equalsIgnoreCase(certificationEditionYear)){
				return editionName;
			}
		}
		return null;
	}
	
	private Map<String, Object> getCertificationStatus(CertificationStatusType certificationStatusType){
		Set<KeyValueModel> certStatuses = searchMenuManager.getCertificationStatuses();
		Map<String, Object> certStatusMap = new HashMap<String, Object>();
		for(KeyValueModel certStatus : certStatuses){
			if(certStatus.getName().equalsIgnoreCase(certificationStatusType.getName())){
				certStatusMap.put(certStatus.getId().toString(), certStatus.getName());
				return certStatusMap;
			}
		}
		
		return certStatusMap;
	}
	
	private Map<CertifiedProductDTO, ListingUpdateRequest> getListingsUpdateRequests(List<CertifiedProductDTO> cpDTOs, Map<String, Object> newCertificationStatus){
		Map<CertifiedProductDTO, ListingUpdateRequest> listingUpdatesMap = new HashMap<CertifiedProductDTO, ListingUpdateRequest>();
		
		for(CertifiedProductDTO dto : cpDTOs){
			CertifiedProductSearchDetails cpDetails = new CertifiedProductSearchDetails();
			
			ListingUpdateRequest listingUpdate = new ListingUpdateRequest();
			listingUpdate.setListing(cpDetails);
			listingUpdate.getListing().setCertificationStatus(newCertificationStatus);
			listingUpdatesMap.put(dto, listingUpdate);
		}
		
		return listingUpdatesMap;
	}

	public CertifiedProductManager getCpManager() {
		return cpManager;
	}

	public void setCpManager(CertifiedProductManager cpManager) {
		this.cpManager = cpManager;
	}

	public CertificationBodyManager getCbManager() {
		return cbManager;
	}

	public void setCbManager(CertificationBodyManager cbManager) {
		this.cbManager = cbManager;
	}

	public SearchMenuManager getSearchMenuManager() {
		return searchMenuManager;
	}

	public void setSearchMenuManager(SearchMenuManager searchMenuManager) {
		this.searchMenuManager = searchMenuManager;
	}

}
