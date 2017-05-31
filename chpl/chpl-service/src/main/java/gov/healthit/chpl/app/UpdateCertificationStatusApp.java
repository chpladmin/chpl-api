package gov.healthit.chpl.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.manager.SearchMenuManager;

@Component("updateCertificationStatusApp")
public class UpdateCertificationStatusApp extends App {
	private static final String CERTIFICATION_NAME = "CCHIT"; 
	
    private CertificationBodyDAO certificationBodyDAO;
	private CertifiedProductDAO certifiedProductDAO;
	private SearchMenuManager searchMenuManager;
	
	private static final Logger logger = LogManager.getLogger(UpdateCertificationStatusApp.class);
	
	public static void main(String[] args) throws Exception {
		// setup application
		UpdateCertificationStatusApp updateCertStatus = new UpdateCertificationStatusApp();
		Properties props = updateCertStatus.getProperties();
		updateCertStatus.setLocalContext(props);
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		updateCertStatus.initiateSpringBeans(context, props);
		
		// Get Certification Body for CCHIT
		CertificationBodyDTO cbDTO = updateCertStatus.getCertificationBody(CERTIFICATION_NAME);
		// Get certification edition for year 2014
		KeyValueModel certificationEdition = updateCertStatus.getCertificationEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear());
		// Get Map<String, Object> certificationStatus for "Withdrawn by Developer"
		Map<String, Object> updatedCertificationStatus = updateCertStatus.getCertificationStatus(CertificationStatusType.WithdrawnByDeveloper);
		// Get all listings certified by CCHIT with 2014 edition and 'Retired' (216 total according to spreadsheet/DB)
		List<CertifiedProductDTO> listings = updateCertStatus.getListings(cbDTO.getName(), certificationEdition, CertificationStatusType.Retired);
		// Get authentication token for REST call to API
		Token token = new Token(props);
		// Get Map<CertifiedProductDTO, ListingUpdateRequest> for update
		Map<CertifiedProductDTO, ListingUpdateRequest> listingUpdatesMap = updateCertStatus.getListingUpdateRequests(listings, updatedCertificationStatus, props, token);
		// Update each listing's certification status to 'Withdrawn by Developer'
		updateCertStatus.updateListingsCertificationStatus(cbDTO.getId(), listingUpdatesMap, props, token);
	}

	@Override
	protected void initiateSpringBeans(AbstractApplicationContext context, Properties props) {
		logger.info("Initiate Spring Beans");
		this.setCertificationBodyDAO((CertificationBodyDAO)context.getBean("certificationBodyDAO"));
		this.setCertifiedProductDAO((CertifiedProductDAO)context.getBean("certifiedProductDAO"));
		this.setSearchMenuManager((SearchMenuManager)context.getBean("searchMenuManager"));
		logger.info("Finished initiating Spring Beans");
	}
	
	private List<CertifiedProductDTO> getListings(String certificationBodyName, KeyValueModel certificationEdition, CertificationStatusType certificationStatusType) throws EntityRetrievalException{
		logger.info("Get listings for " + certificationBodyName + " " + certificationEdition + " " + certificationStatusType);
		List<CertifiedProductDTO> cps = new ArrayList<CertifiedProductDTO>();
		List<CertifiedProductDetailsDTO> allCpDetails = certifiedProductDAO.findAll();
		for(CertifiedProductDetailsDTO dto : allCpDetails){
			if(dto.getCertificationBodyName().equalsIgnoreCase(certificationBodyName) 
					&& dto.getCertificationEditionId().longValue() == certificationEdition.getId() 
					&& dto.getCertificationStatusName().equalsIgnoreCase(certificationStatusType.getName())){
				CertifiedProductDTO cpDTO = certifiedProductDAO.getById(dto.getId());
				cps.add(cpDTO);
			}
		}
		logger.info("Found " + cps.size() + " listings for " + certificationBodyName + " with edition " + certificationEdition + " and status " + certificationStatusType.getName());
		return cps;
	}

	private void updateListingsCertificationStatus(Long acbId, Map<CertifiedProductDTO, ListingUpdateRequest> cpUpdateMap, Properties props, Token token) throws JsonProcessingException, EntityRetrievalException, EntityCreationException{
		for(CertifiedProductDTO cpDTO : cpUpdateMap.keySet()){
			logger.info("Update listing Certification Status for " + cpDTO.getChplProductNumber());
			String url = props.getProperty("chplUrlBegin") + props.getProperty("basePath") + props.getProperty("updateCertifiedProduct");
			ObjectMapper mapper = new ObjectMapper();
			ListingUpdateRequest updateRequest = cpUpdateMap.get(cpDTO);
			String json = mapper.writeValueAsString(updateRequest);
			HttpUtil.postAuthenticatedBodyRequest(url, null, props, token, json);
			logger.info("Finished updating listing Certification Status for " + cpDTO.getChplProductNumber());
		}
	}
	
	private CertificationBodyDTO getCertificationBody(String certificationBodyName){
		logger.info("Get certification body");
		List<CertificationBodyDTO> cbDTOs = this.certificationBodyDAO.findAll(true);
		for(CertificationBodyDTO dto : cbDTOs){
			if(dto.getName().equalsIgnoreCase(certificationBodyName)){
				logger.info("Finished getting certification body for " + certificationBodyName);
				return dto;
			}
		}
		return null;
	}
	
	private KeyValueModel getCertificationEdition(String certificationEditionYear){
		logger.info("Get certification edition for " + certificationEditionYear);
		Set<KeyValueModel> editionNames = this.searchMenuManager.getEditionNames(true);
		for(KeyValueModel editionName : editionNames){
			if(editionName.getName().equalsIgnoreCase(certificationEditionYear)){
				logger.info("Finished getting certification edition for " + certificationEditionYear);
				return editionName;
			}
		}
		return null;
	}
	
	private Map<String, Object> getCertificationStatus(CertificationStatusType certificationStatusType){
		logger.info("Getting certification status for " + certificationStatusType.getName());
		Set<KeyValueModel> certStatuses = searchMenuManager.getCertificationStatuses();
		Map<String, Object> certStatusMap = new HashMap<String, Object>();
		for(KeyValueModel certStatus : certStatuses){
			if(certStatus.getName().equalsIgnoreCase(certificationStatusType.getName())){
				certStatusMap.put("date", certStatus.getDescription());
				certStatusMap.put("name", certStatus.getName());
				certStatusMap.put("id", certStatus.getId().toString());
				logger.info("Finished getting certification status for " + certificationStatusType.getName());
				return certStatusMap;
			}
		}
		return certStatusMap;
	}
	
	private Map<CertifiedProductDTO, ListingUpdateRequest> getListingUpdateRequests(List<CertifiedProductDTO> cpDTOs, Map<String, Object> newCertificationStatus, Properties props, Token token) throws JsonParseException, JsonMappingException, IOException{
		logger.info("Getting listing update requests");
		Map<CertifiedProductDTO, ListingUpdateRequest> listingUpdatesMap = new HashMap<CertifiedProductDTO, ListingUpdateRequest>();
		for(CertifiedProductDTO dto : cpDTOs){
			String urlRequest = props.getProperty("chplUrlBegin") + props.getProperty("basePath") + String.format(props.getProperty("getCertifiedProductDetails"), dto.getId().toString());
			String result = HttpUtil.getAuthenticatedRequest(urlRequest, null, props, token);
			// convert json to CertifiedProductSearchDetails object
			ObjectMapper mapper = new ObjectMapper();
			CertifiedProductSearchDetails cpDetails = mapper.readValue(result, CertifiedProductSearchDetails.class);
			ListingUpdateRequest listingUpdate = new ListingUpdateRequest();
			listingUpdate.setBanDeveloper(false);
			listingUpdate.setListing(cpDetails);
			listingUpdate.getListing().setCertificationStatus(newCertificationStatus);
			listingUpdatesMap.put(dto, listingUpdate);
		}
		logger.info("Finished getting listing update requests");
		return listingUpdatesMap;
	}

	public SearchMenuManager getSearchMenuManager() {
		return searchMenuManager;
	}

	public void setSearchMenuManager(SearchMenuManager searchMenuManager) {
		this.searchMenuManager = searchMenuManager;
	}

	public CertificationBodyDAO getCertificationBodyDAO() {
		return certificationBodyDAO;
	}

	public void setCertificationBodyDAO(CertificationBodyDAO certificationBodyDAO) {
		this.certificationBodyDAO = certificationBodyDAO;
	}

	public CertifiedProductDAO getCertifiedProductDAO() {
		return certifiedProductDAO;
	}

	public void setCertifiedProductDAO(CertifiedProductDAO certifiedProductDAO) {
		this.certifiedProductDAO = certifiedProductDAO;
	}

}
