package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import gov.healthit.chpl.dao.AgeRangeDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestParticipantDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.TestTaskDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestParticipant;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTask;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertificationResultUcdProcess;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestTaskDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.manager.CertificationResultManager;

@Service
public class CertificationResultManagerImpl implements
		CertificationResultManager {
	private static final Logger logger = LogManager.getLogger(CertificationResultManagerImpl.class);
	private static final String G1_MEASURE = "G1";
	private static final String G2_MEASURE = "G2";
	
	@Autowired private CertifiedProductSearchDAO cpDao;
	@Autowired private CertificationCriterionDAO criteriaDao;
	@Autowired private CertificationResultDAO certResultDAO;
	@Autowired private TestStandardDAO testStandardDAO;
	@Autowired private TestToolDAO testToolDAO;
	@Autowired private TestProcedureDAO testProcedureDAO;
	@Autowired private TestFunctionalityDAO testFunctionalityDAO;
	@Autowired private TestParticipantDAO testParticipantDAO;
	@Autowired private AgeRangeDAO ageDao;
	@Autowired private EducationTypeDAO educDao;
	@Autowired private TestTaskDAO testTaskDAO;
	@Autowired private UcdProcessDAO ucdDao;
	@Autowired private MacraMeasureDAO mmDao;
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#listing.getCertificationBodyId(), 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public int update(CertifiedProductDTO listing, CertificationResult orig, CertificationResult updated) 
		throws EntityCreationException, EntityRetrievalException {
		int numChanges = 0;
		//does the cert result need updated?
		boolean hasChanged = false;
		if(!StringUtils.equals(orig.getApiDocumentation(), updated.getApiDocumentation()) ||
			!StringUtils.equals(orig.getPrivacySecurityFramework(), updated.getPrivacySecurityFramework()) ||
			!ObjectUtils.equals(orig.isG1Success(), updated.isG1Success()) ||
			!ObjectUtils.equals(orig.isG2Success(), updated.isG2Success()) ||
			!ObjectUtils.equals(orig.isGap(), updated.isGap()) ||
			!ObjectUtils.equals(orig.isSed(), updated.isSed()) ||
			!ObjectUtils.equals(orig.isSuccess(), updated.isSuccess())) {
			hasChanged = true;
		}
		if(hasChanged) {
			CertificationResultDTO toUpdate = new CertificationResultDTO();
			toUpdate.setId(orig.getId());
			toUpdate.setCertifiedProductId(listing.getId());
			toUpdate.setApiDocumentation(updated.getApiDocumentation());
			toUpdate.setPrivacySecurityFramework(updated.getPrivacySecurityFramework());
			toUpdate.setG1Success(updated.isG1Success());
			toUpdate.setG2Success(updated.isG2Success());
			toUpdate.setGap(updated.isGap());
			toUpdate.setSed(updated.isSed());
			toUpdate.setSuccessful(updated.isSuccess());
			CertificationCriterionDTO criteria = criteriaDao.getByName(orig.getNumber());
			if(criteria == null || criteria.getId() == null) {
				throw new EntityCreationException("Cannot add certification result mapping for unknown criteria " + orig.getNumber());
			} else {
				toUpdate.setCertificationCriterionId(criteria.getId());
			}
			certResultDAO.update(toUpdate);
			numChanges++;
		}

		if(updated.isSuccess() == null || updated.isSuccess() == Boolean.FALSE) {
			//similar to delete - remove all related items
			numChanges += updateAdditionalSoftware(listing, updated, orig.getAdditionalSoftware(), null);
			numChanges += updateMacraMeasures(listing, updated, orig.getG1MacraMeasures(), null, G1_MEASURE);
			numChanges += updateMacraMeasures(listing, updated, orig.getG2MacraMeasures(), null, G2_MEASURE);
			numChanges += updateUcdProcesses(listing, updated, orig.getUcdProcesses(), null);
			numChanges += updateTestStandards(listing, updated, orig.getTestStandards(), null);
			numChanges += updateTestTools(listing, updated, orig.getTestToolsUsed(), null);
			numChanges += updateTestData(listing, updated, orig.getTestDataUsed(), null);
			numChanges += updateTestProcedures(listing, updated, orig.getTestProcedures(), null);
			numChanges += updateTestFunctionality(listing, updated, orig.getTestFunctionality(), null);
			numChanges += updateTestTasks(listing, updated, orig.getTestTasks(), null);
		} else {
			//create/update all related items
			numChanges += updateAdditionalSoftware(listing, updated, 
					orig.getAdditionalSoftware(), updated.getAdditionalSoftware());
			numChanges += updateMacraMeasures(listing, updated, 
					orig.getG1MacraMeasures(), updated.getG1MacraMeasures(),
					G1_MEASURE);
			numChanges += updateMacraMeasures(listing, updated, 
					orig.getG2MacraMeasures(), updated.getG2MacraMeasures(),
					G2_MEASURE);
			numChanges += updateUcdProcesses(listing, updated, 
					orig.getUcdProcesses(), updated.getUcdProcesses());
			numChanges += updateTestStandards(listing, updated, 
					orig.getTestStandards(), updated.getTestStandards());
			numChanges += updateTestTools(listing, updated, 
					orig.getTestToolsUsed(), updated.getTestToolsUsed());
			numChanges += updateTestData(listing, updated, 
					orig.getTestDataUsed(), updated.getTestDataUsed());
			numChanges += updateTestProcedures(listing, updated, 
					orig.getTestProcedures(), updated.getTestProcedures());
			numChanges += updateTestFunctionality(listing, updated,
					orig.getTestFunctionality(), updated.getTestFunctionality());
			numChanges += updateTestTasks(listing, updated, 
					orig.getTestTasks(), updated.getTestTasks());
		}
		
		return numChanges;
	}
	
	private int updateAdditionalSoftware(CertifiedProductDTO listing, CertificationResult certResult,
			List<CertificationResultAdditionalSoftware> existingAdditionalSoftware,
			List<CertificationResultAdditionalSoftware> updatedAdditionalSoftware) 
	throws EntityCreationException {
		int numChanges = 0;
		List<CertificationResultAdditionalSoftwareDTO> additionalSoftwareToAdd = new ArrayList<CertificationResultAdditionalSoftwareDTO>();
		List<Long> idsToRemove = new ArrayList<Long>();
		
		//figure out which additional software to add
		if(updatedAdditionalSoftware != null && updatedAdditionalSoftware.size() > 0) {
			//fill in potentially missing cp id
			for(CertificationResultAdditionalSoftware updatedItem : updatedAdditionalSoftware) {
				if(updatedItem.getCertifiedProductId() == null && !StringUtils.isEmpty(updatedItem.getCertifiedProductNumber())) {
					Long cpId = cpDao.getListingIdByUniqueChplNumber(updatedItem.getCertifiedProductNumber());
					updatedItem.setCertifiedProductId(cpId);
				}
			}
			
			if(existingAdditionalSoftware == null || existingAdditionalSoftware.size() == 0) {
				//existing listing has none, add all from the update
				for(CertificationResultAdditionalSoftware updatedItem : updatedAdditionalSoftware) {
					CertificationResultAdditionalSoftwareDTO toAdd = new CertificationResultAdditionalSoftwareDTO();
					toAdd.setCertificationResultId(certResult.getId());
					toAdd.setCertifiedProductId(updatedItem.getCertifiedProductId());
					toAdd.setCertifiedProductNumber(updatedItem.getCertifiedProductNumber());
					toAdd.setGrouping(updatedItem.getGrouping());
					toAdd.setJustification(updatedItem.getJustification());
					toAdd.setName(updatedItem.getName());
					toAdd.setVersion(updatedItem.getVersion());;
					additionalSoftwareToAdd.add(toAdd);
				}
			} else if(existingAdditionalSoftware.size() > 0) {
				//existing listing has some, compare to the update to see if any are different
				for(CertificationResultAdditionalSoftware updatedItem : updatedAdditionalSoftware) { 
					boolean inExistingListing = false;
					for(CertificationResultAdditionalSoftware existingItem : existingAdditionalSoftware) {
						inExistingListing = updatedItem.matches(existingItem);
					}
					
					if(!inExistingListing) {
						CertificationResultAdditionalSoftwareDTO toAdd = new CertificationResultAdditionalSoftwareDTO();
						toAdd.setCertificationResultId(certResult.getId());
						toAdd.setCertifiedProductId(updatedItem.getCertifiedProductId());
						toAdd.setCertifiedProductNumber(updatedItem.getCertifiedProductNumber());
						toAdd.setGrouping(updatedItem.getGrouping());
						toAdd.setJustification(updatedItem.getJustification());
						toAdd.setName(updatedItem.getName());
						toAdd.setVersion(updatedItem.getVersion());;
						additionalSoftwareToAdd.add(toAdd);
					}
				}
			}
		}
				
		//figure out which additional software to remove
		if(existingAdditionalSoftware != null && existingAdditionalSoftware.size() > 0) {
			//if the updated listing has none, remove them all from existing
			if(updatedAdditionalSoftware == null || updatedAdditionalSoftware.size() == 0) {
				for(CertificationResultAdditionalSoftware existingItem : existingAdditionalSoftware) {
					idsToRemove.add(existingItem.getId());
				}
			} else if(updatedAdditionalSoftware.size() > 0) {
				for(CertificationResultAdditionalSoftware existingItem : existingAdditionalSoftware) {
					boolean inUpdatedListing = false;
					for(CertificationResultAdditionalSoftware updatedItem : updatedAdditionalSoftware) {
						inUpdatedListing = updatedItem.matches(existingItem);
					}
					if(!inUpdatedListing) {
						idsToRemove.add(existingItem.getId());
					}
				}
			}
		}
		
		numChanges = additionalSoftwareToAdd.size() + idsToRemove.size();
		for(CertificationResultAdditionalSoftwareDTO toAdd : additionalSoftwareToAdd) {
			certResultDAO.addAdditionalSoftwareMapping(toAdd);
		}
		
		for(Long idToRemove : idsToRemove) {
			certResultDAO.deleteAdditionalSoftwareMapping(idToRemove);
		}	
		return numChanges;
	}
	
	private int updateMacraMeasures(CertifiedProductDTO listing, CertificationResult certResult,
			List<MacraMeasure> existingMeasures,
			List<MacraMeasure> updatedMeasures,
			String g1OrG2) 
	throws EntityCreationException {
		int numChanges = 0;
		List<CertificationResultMacraMeasureDTO> measureToAdd = new ArrayList<CertificationResultMacraMeasureDTO>();
		List<Long> idsToRemove = new ArrayList<Long>();
		
		//figure out which macra measures to add
		if(updatedMeasures != null && updatedMeasures.size() > 0) {
			//fill in potentially missing macra measure id info
			for(MacraMeasure updatedItem : updatedMeasures) {
				if(updatedItem != null && updatedItem.getId() == null && 
					!StringUtils.isEmpty(updatedItem.getName())) {
					MacraMeasureDTO foundMeasure = mmDao.getByCriteriaNumberAndValue(certResult.getNumber(), updatedItem.getName());
					updatedItem.setId(foundMeasure.getId());
				}
			}
			
			if(existingMeasures == null || existingMeasures.size() == 0) {
				//existing listing has none, add all from the update
				for(MacraMeasure updatedItem : updatedMeasures) {
					CertificationResultMacraMeasureDTO toAdd = new CertificationResultMacraMeasureDTO();
					toAdd.setCertificationResultId(certResult.getId());
					MacraMeasureDTO measure = new MacraMeasureDTO();
					measure.setId(updatedItem.getId());
					toAdd.setMeasure(measure);
					measureToAdd.add(toAdd);
				}
			} else if(existingMeasures.size() > 0) {
				//existing listing has some, compare to the update to see if any are different
				for(MacraMeasure updatedItem : updatedMeasures) { 
					boolean inExistingListing = false;
					for(MacraMeasure existingItem : existingMeasures) {
						inExistingListing = updatedItem.matches(existingItem);
					}
					
					if(!inExistingListing) {
						CertificationResultMacraMeasureDTO toAdd = new CertificationResultMacraMeasureDTO();
						toAdd.setCertificationResultId(certResult.getId());
						MacraMeasureDTO measure = new MacraMeasureDTO();
						measure.setId(updatedItem.getId());
						toAdd.setMeasure(measure);
						measureToAdd.add(toAdd);
					}
				}
			}
		}
				
		//figure out which macra measures to remove
		if(existingMeasures != null && existingMeasures.size() > 0) {
			//if the updated listing has none, remove them all from existing
			if(updatedMeasures == null || updatedMeasures.size() == 0) {
				for(MacraMeasure existingItem : existingMeasures) {
					idsToRemove.add(existingItem.getId());
				}
			} else if(updatedMeasures.size() > 0) {
				for(MacraMeasure existingItem : existingMeasures) {
					boolean inUpdatedListing = false;
					for(MacraMeasure updatedItem : updatedMeasures) {
						inUpdatedListing = updatedItem.matches(existingItem);
					}
					if(!inUpdatedListing) {
						idsToRemove.add(existingItem.getId());
					}
				}
			}
		}
		
		numChanges = measureToAdd.size() + idsToRemove.size();
		for(CertificationResultMacraMeasureDTO toAdd : measureToAdd) {
			if(g1OrG2.equalsIgnoreCase(G1_MEASURE)) {
				certResultDAO.addG1MacraMeasureMapping(toAdd);
			} else if(g1OrG2.equalsIgnoreCase(G2_MEASURE)) {
				certResultDAO.addG2MacraMeasureMapping(toAdd);
			}
		}
		
		for(Long idToRemove : idsToRemove) {
			if(g1OrG2.equalsIgnoreCase(G1_MEASURE)) {
				certResultDAO.deleteG1MacraMeasureMapping(idToRemove);
			} else if(g1OrG2.equalsIgnoreCase(G2_MEASURE)) {
				certResultDAO.deleteG2MacraMeasureMapping(idToRemove);
			}
		}	
		return numChanges;
	}
	
	private int updateUcdProcesses(CertifiedProductDTO listing, CertificationResult certResult,
			List<CertificationResultUcdProcess> existingUcdProcesses,
			List<CertificationResultUcdProcess> updatedUcdProcesses) 
	throws EntityCreationException {
		int numChanges = 0;
		List<CertificationResultUcdProcessDTO> ucdToAdd = new ArrayList<CertificationResultUcdProcessDTO>();
		List<Long> idsToRemove = new ArrayList<Long>();
		
		//figure out which ucd processes to add
		if(updatedUcdProcesses != null && updatedUcdProcesses.size() > 0) {
			//fill in potentially missing ucd process id
			for(CertificationResultUcdProcess updatedItem : updatedUcdProcesses) {
				if(updatedItem.getUcdProcessId() == null && !StringUtils.isEmpty(updatedItem.getUcdProcessName())) {
					UcdProcessDTO foundUcd = ucdDao.getByName(updatedItem.getUcdProcessName());
					if(foundUcd == null) {
						UcdProcessDTO ucdToCreate = new UcdProcessDTO();
						ucdToCreate.setName(updatedItem.getUcdProcessName());
						UcdProcessDTO created = ucdDao.create(ucdToCreate);
						updatedItem.setUcdProcessId(created.getId());
					} else {
						updatedItem.setUcdProcessId(foundUcd.getId());
					}
				}
			}
			
			if(existingUcdProcesses == null || existingUcdProcesses.size() == 0) {
				//existing listing has none, add all from the update
				for(CertificationResultUcdProcess updatedItem : updatedUcdProcesses) {
					CertificationResultUcdProcessDTO toAdd = new CertificationResultUcdProcessDTO();
					toAdd.setCertificationResultId(certResult.getId());
					toAdd.setUcdProcessId(updatedItem.getUcdProcessId());
					toAdd.setUcdProcessDetails(updatedItem.getUcdProcessDetails());
					ucdToAdd.add(toAdd);
				}
			} else if(existingUcdProcesses.size() > 0) {
				//existing listing has some, compare to the update to see if any are different
				for(CertificationResultUcdProcess updatedItem : updatedUcdProcesses) { 
					boolean inExistingListing = false;
					for(CertificationResultUcdProcess existingItem : existingUcdProcesses) {
						inExistingListing = updatedItem.matches(existingItem);
					}
					
					if(!inExistingListing) {
						CertificationResultUcdProcessDTO toAdd = new CertificationResultUcdProcessDTO();
						toAdd.setCertificationResultId(certResult.getId());
						toAdd.setUcdProcessId(updatedItem.getUcdProcessId());
						toAdd.setUcdProcessDetails(updatedItem.getUcdProcessDetails());
						ucdToAdd.add(toAdd);
					}
				}
			}
		}
				
		//figure out which ucd processes to remove
		if(existingUcdProcesses != null && existingUcdProcesses.size() > 0) {
			//if the updated listing has none, remove them all from existing
			if(updatedUcdProcesses == null || updatedUcdProcesses.size() == 0) {
				for(CertificationResultUcdProcess existingItem : existingUcdProcesses) {
					idsToRemove.add(existingItem.getId());
				}
			} else if(updatedUcdProcesses.size() > 0) {
				for(CertificationResultUcdProcess existingItem : existingUcdProcesses) {
					boolean inUpdatedListing = false;
					for(CertificationResultUcdProcess updatedItem : updatedUcdProcesses) {
						inUpdatedListing = updatedItem.matches(existingItem);
					}
					if(!inUpdatedListing) {
						idsToRemove.add(existingItem.getId());
					}
				}
			}
		}
		
		numChanges = ucdToAdd.size() + idsToRemove.size();
		for(CertificationResultUcdProcessDTO toAdd : ucdToAdd) {
			certResultDAO.addUcdProcessMapping(toAdd);
		}
		
		for(Long idToRemove : idsToRemove) {
			certResultDAO.deleteUcdProcessMapping(idToRemove);
		}	
		return numChanges;
	}
	
	private int updateTestStandards(CertifiedProductDTO listing, CertificationResult certResult,
			List<CertificationResultTestStandard> existingTestStandards,
			List<CertificationResultTestStandard> updatedTestStandards) 
	throws EntityCreationException {
		int numChanges = 0;
		List<CertificationResultTestStandardDTO> testStandardsToAdd = new ArrayList<CertificationResultTestStandardDTO>();
		List<Long> idsToRemove = new ArrayList<Long>();
		
		//figure out which test standards to add
		if(updatedTestStandards != null && updatedTestStandards.size() > 0) {
			//fill in potentially missing test standard id
			for(CertificationResultTestStandard updatedItem : updatedTestStandards) {
				if(updatedItem.getTestStandardId() == null && !StringUtils.isEmpty(updatedItem.getTestStandardName())) {
					TestStandardDTO foundStd = testStandardDAO.getByNumberAndEdition(updatedItem.getTestStandardName(), listing.getCertificationEditionId());
					if(foundStd == null) {
						TestStandardDTO stdToCreate = new TestStandardDTO();
						stdToCreate.setName(updatedItem.getTestStandardName());
						stdToCreate.setDescription(updatedItem.getTestStandardDescription());
						stdToCreate.setCertificationEditionId(listing.getCertificationEditionId());
						TestStandardDTO created = testStandardDAO.create(stdToCreate);
						updatedItem.setTestStandardId(created.getId());
					} else {
						updatedItem.setTestStandardId(foundStd.getId());
					}
				}
			}
			
			if(existingTestStandards == null || existingTestStandards.size() == 0) {
				//existing listing has none, add all from the update
				for(CertificationResultTestStandard updatedItem : updatedTestStandards) {
					CertificationResultTestStandardDTO toAdd = new CertificationResultTestStandardDTO();
					toAdd.setCertificationResultId(certResult.getId());
					toAdd.setTestStandardId(updatedItem.getTestStandardId());
					testStandardsToAdd.add(toAdd);
				}
			} else if(existingTestStandards.size() > 0) {
				//existing listing has some, compare to the update to see if any are different
				for(CertificationResultTestStandard updatedItem : updatedTestStandards) { 
					boolean inExistingListing = false;
					for(CertificationResultTestStandard existingItem : existingTestStandards) {
						inExistingListing = updatedItem.matches(existingItem);
					}
					
					if(!inExistingListing) {
						CertificationResultTestStandardDTO toAdd = new CertificationResultTestStandardDTO();
						toAdd.setCertificationResultId(certResult.getId());
						toAdd.setTestStandardId(updatedItem.getTestStandardId());
						testStandardsToAdd.add(toAdd);
					}
				}
			}
		}
				
		//figure out which ucd processes to remove
		if(existingTestStandards != null && existingTestStandards.size() > 0) {
			//if the updated listing has none, remove them all from existing
			if(updatedTestStandards == null || updatedTestStandards.size() == 0) {
				for(CertificationResultTestStandard existingItem : existingTestStandards) {
					idsToRemove.add(existingItem.getId());
				}
			} else if(updatedTestStandards.size() > 0) {
				for(CertificationResultTestStandard existingItem : existingTestStandards) {
					boolean inUpdatedListing = false;
					for(CertificationResultTestStandard updatedItem : updatedTestStandards) {
						inUpdatedListing = updatedItem.matches(existingItem);
					}
					if(!inUpdatedListing) {
						idsToRemove.add(existingItem.getId());
					}
				}
			}
		}
		
		numChanges = testStandardsToAdd.size() + idsToRemove.size();
		for(CertificationResultTestStandardDTO toAdd : testStandardsToAdd) {
			certResultDAO.addTestStandardMapping(toAdd);
		}
		
		for(Long idToRemove : idsToRemove) {
			certResultDAO.deleteTestStandardMapping(idToRemove);
		}	
		return numChanges;
	}
	
	private int updateTestTools(CertifiedProductDTO listing, CertificationResult certResult,
			List<CertificationResultTestTool> existingTestTools,
			List<CertificationResultTestTool> updatedTestTools) 
	throws EntityCreationException {
		int numChanges = 0;
		List<CertificationResultTestToolDTO> testToolsToAdd = new ArrayList<CertificationResultTestToolDTO>();
		List<Long> idsToRemove = new ArrayList<Long>();
		
		//figure out which test tools to add
		if(updatedTestTools != null && updatedTestTools.size() > 0) {
			//fill in potentially missing test standard id
			for(CertificationResultTestTool updatedItem : updatedTestTools) {
				if(updatedItem.getTestToolId() == null && !StringUtils.isEmpty(updatedItem.getTestToolName())) {
					TestToolDTO foundTool = testToolDAO.getByName(updatedItem.getTestToolName());
					if(foundTool == null) {
						logger.error("Could not find test tool " + updatedItem.getTestToolName() + 
								"; will not be adding this as a test tool to listing id " + listing.getId() + 
								", criteria " + certResult.getNumber());
					} else {
						updatedItem.setTestToolId(foundTool.getId());
					}
				}
			}
			
			if(existingTestTools == null || existingTestTools.size() == 0) {
				//existing listing has none, add all from the update
				for(CertificationResultTestTool updatedItem : updatedTestTools) {
					if(updatedItem.getTestToolId() != null) {
						CertificationResultTestToolDTO toAdd = new CertificationResultTestToolDTO();
						toAdd.setCertificationResultId(certResult.getId());
						toAdd.setTestToolId(updatedItem.getTestToolId());
						testToolsToAdd.add(toAdd);
					}
				}
			} else if(existingTestTools.size() > 0) {
				//existing listing has some, compare to the update to see if any are different
				for(CertificationResultTestTool updatedItem : updatedTestTools) { 
					boolean inExistingListing = false;
					for(CertificationResultTestTool existingItem : existingTestTools) {
						inExistingListing = updatedItem.matches(existingItem);
					}
					
					if(!inExistingListing) {
						if(updatedItem.getTestToolId() != null) {
							CertificationResultTestToolDTO toAdd = new CertificationResultTestToolDTO();
							toAdd.setCertificationResultId(certResult.getId());
							toAdd.setTestToolId(updatedItem.getTestToolId());
							testToolsToAdd.add(toAdd);
						}
					}
				}
			}
		}
				
		//figure out which test tools to remove
		if(existingTestTools != null && existingTestTools.size() > 0) {
			//if the updated listing has none, remove them all from existing
			if(updatedTestTools == null || updatedTestTools.size() == 0) {
				for(CertificationResultTestTool existingItem : existingTestTools) {
					idsToRemove.add(existingItem.getId());
				}
			} else if(updatedTestTools.size() > 0) {
				for(CertificationResultTestTool existingItem : existingTestTools) {
					boolean inUpdatedListing = false;
					for(CertificationResultTestTool updatedItem : updatedTestTools) {
						inUpdatedListing = updatedItem.matches(existingItem);
					}
					if(!inUpdatedListing) {
						idsToRemove.add(existingItem.getId());
					}
				}
			}
		}
		
		numChanges = testToolsToAdd.size() + idsToRemove.size();
		for(CertificationResultTestToolDTO toAdd : testToolsToAdd) {
			certResultDAO.addTestToolMapping(toAdd);
		}
		
		for(Long idToRemove : idsToRemove) {
			certResultDAO.deleteTestToolMapping(idToRemove);
		}	
		return numChanges;
	}
	
	private int updateTestData(CertifiedProductDTO listing, CertificationResult certResult,
			List<CertificationResultTestData> existingTestData,
			List<CertificationResultTestData> updatedTestData) 
	throws EntityCreationException {
		int numChanges = 0;
		List<CertificationResultTestDataDTO> testDataToAdd = new ArrayList<CertificationResultTestDataDTO>();
		List<Long> idsToRemove = new ArrayList<Long>();
		
		//figure out which test data to add
		if(updatedTestData != null && updatedTestData.size() > 0) {
			if(existingTestData == null || existingTestData.size() == 0) {
				//existing listing has none, add all from the update
				for(CertificationResultTestData updatedItem : updatedTestData) {
					CertificationResultTestDataDTO toAdd = new CertificationResultTestDataDTO();
					toAdd.setCertificationResultId(certResult.getId());
					toAdd.setAlteration(updatedItem.getAlteration());
					toAdd.setVersion(updatedItem.getVersion());
					testDataToAdd.add(toAdd);
				}
			} else if(existingTestData.size() > 0) {
				//existing listing has some, compare to the update to see if any are different
				for(CertificationResultTestData updatedItem : updatedTestData) { 
					boolean inExistingListing = false;
					for(CertificationResultTestData existingItem : existingTestData) {
						inExistingListing = updatedItem.matches(existingItem);
					}
					
					if(!inExistingListing) {
						CertificationResultTestDataDTO toAdd = new CertificationResultTestDataDTO();
						toAdd.setCertificationResultId(certResult.getId());
						toAdd.setAlteration(updatedItem.getAlteration());
						toAdd.setVersion(updatedItem.getVersion());
						testDataToAdd.add(toAdd);
					}
				}
			}
		}
				
		//figure out which test data to remove
		if(existingTestData != null && existingTestData.size() > 0) {
			//if the updated listing has none, remove them all from existing
			if(updatedTestData == null || updatedTestData.size() == 0) {
				for(CertificationResultTestData existingItem : existingTestData) {
					idsToRemove.add(existingItem.getId());
				}
			} else if(updatedTestData.size() > 0) {
				for(CertificationResultTestData existingItem : existingTestData) {
					boolean inUpdatedListing = false;
					for(CertificationResultTestData updatedItem : updatedTestData) {
						inUpdatedListing = updatedItem.matches(existingItem);
					}
					if(!inUpdatedListing) {
						idsToRemove.add(existingItem.getId());
					}
				}
			}
		}
		
		numChanges = testDataToAdd.size() + idsToRemove.size();
		for(CertificationResultTestDataDTO toAdd : testDataToAdd) {
			certResultDAO.addTestDataMapping(toAdd);
		}
		
		for(Long idToRemove : idsToRemove) {
			certResultDAO.deleteTestDataMapping(idToRemove);
		}	
		return numChanges;
	}
	
	private int updateTestProcedures(CertifiedProductDTO listing, CertificationResult certResult,
			List<CertificationResultTestProcedure> existingTestProcedures,
			List<CertificationResultTestProcedure> updatedTestProcedures) 
	throws EntityCreationException {
		int numChanges = 0;
		List<CertificationResultTestProcedureDTO> testProceduresToAdd = new ArrayList<CertificationResultTestProcedureDTO>();
		List<Long> idsToRemove = new ArrayList<Long>();
		
		//figure out which test procedures to add
		if(updatedTestProcedures != null && updatedTestProcedures.size() > 0) {
			//fill in potentially missing test procedure id
			for(CertificationResultTestProcedure updatedItem : updatedTestProcedures) {
				if(updatedItem.getTestProcedureId() == null && !StringUtils.isEmpty(updatedItem.getTestProcedureVersion())) {
					TestProcedureDTO foundProcedure = testProcedureDAO.getByName(updatedItem.getTestProcedureVersion());
					if(foundProcedure == null) {
						TestProcedureDTO procToCreate = new TestProcedureDTO();
						procToCreate.setVersion(updatedItem.getTestProcedureVersion());
						TestProcedureDTO createdProc = testProcedureDAO.create(procToCreate);
						updatedItem.setTestProcedureId(createdProc.getId());
					} else {
						updatedItem.setTestProcedureId(foundProcedure.getId());
					}
				}
			}
			
			if(existingTestProcedures == null || existingTestProcedures.size() == 0) {
				//existing listing has none, add all from the update
				for(CertificationResultTestProcedure updatedItem : updatedTestProcedures) {
					CertificationResultTestProcedureDTO toAdd = new CertificationResultTestProcedureDTO();
					toAdd.setCertificationResultId(certResult.getId());
					toAdd.setTestProcedureId(updatedItem.getTestProcedureId());
					testProceduresToAdd.add(toAdd);
				}
			} else if(existingTestProcedures.size() > 0) {
				//existing listing has some, compare to the update to see if any are different
				for(CertificationResultTestProcedure updatedItem : updatedTestProcedures) { 
					boolean inExistingListing = false;
					for(CertificationResultTestProcedure existingItem : existingTestProcedures) {
						inExistingListing = updatedItem.matches(existingItem);
					}
					
					if(!inExistingListing) {
						CertificationResultTestProcedureDTO toAdd = new CertificationResultTestProcedureDTO();
						toAdd.setCertificationResultId(certResult.getId());
						toAdd.setTestProcedureId(updatedItem.getTestProcedureId());
						testProceduresToAdd.add(toAdd);
					}
				}
			}
		}
				
		//figure out which test data to remove
		if(existingTestProcedures != null && existingTestProcedures.size() > 0) {
			//if the updated listing has none, remove them all from existing
			if(updatedTestProcedures == null || updatedTestProcedures.size() == 0) {
				for(CertificationResultTestProcedure existingItem : existingTestProcedures) {
					idsToRemove.add(existingItem.getId());
				}
			} else if(updatedTestProcedures.size() > 0) {
				for(CertificationResultTestProcedure existingItem : existingTestProcedures) {
					boolean inUpdatedListing = false;
					for(CertificationResultTestProcedure updatedItem : updatedTestProcedures) {
						inUpdatedListing = updatedItem.matches(existingItem);
					}
					if(!inUpdatedListing) {
						idsToRemove.add(existingItem.getId());
					}
				}
			}
		}
		
		numChanges = testProceduresToAdd.size() + idsToRemove.size();
		for(CertificationResultTestProcedureDTO toAdd : testProceduresToAdd) {
			certResultDAO.addTestProcedureMapping(toAdd);
		}
		
		for(Long idToRemove : idsToRemove) {
			certResultDAO.deleteTestProcedureMapping(idToRemove);
		}	
		return numChanges;
	}
	
	private int updateTestFunctionality(CertifiedProductDTO listing, CertificationResult certResult,
			List<CertificationResultTestFunctionality> existingTestFunctionality,
			List<CertificationResultTestFunctionality> updatedTestFunctionality) 
	throws EntityCreationException {
		int numChanges = 0;
		List<CertificationResultTestFunctionalityDTO> testFuncToAdd = new ArrayList<CertificationResultTestFunctionalityDTO>();
		List<Long> idsToRemove = new ArrayList<Long>();
		
		//figure out which test funcs to add
		if(updatedTestFunctionality != null && updatedTestFunctionality.size() > 0) {
			//fill in potentially missing test func id
			for(CertificationResultTestFunctionality updatedItem : updatedTestFunctionality) {
				if(updatedItem.getTestFunctionalityId() == null && !StringUtils.isEmpty(updatedItem.getName())) {
					TestFunctionalityDTO foundFunc = testFunctionalityDAO.getByNumberAndEdition(updatedItem.getName(), listing.getCertificationEditionId());
					if(foundFunc == null) {
						logger.error("Could not find test functionality " + updatedItem.getName() +
								" for certifiation edition id " + listing.getCertificationEditionId() + 
								"; will not be adding this as a test functionality to listing id " + listing.getId() + 
								", criteria " + certResult.getNumber());
					} else {
						updatedItem.setTestFunctionalityId(foundFunc.getId());
					}
				}
			}
			
			if(existingTestFunctionality == null || existingTestFunctionality.size() == 0) {
				//existing listing has none, add all from the update
				for(CertificationResultTestFunctionality updatedItem : updatedTestFunctionality) {
					if(updatedItem.getTestFunctionalityId() != null) {
						CertificationResultTestFunctionalityDTO toAdd = new CertificationResultTestFunctionalityDTO();
						toAdd.setCertificationResultId(certResult.getId());
						toAdd.setTestFunctionalityId(updatedItem.getTestFunctionalityId());
						testFuncToAdd.add(toAdd);
					}
				}
			} else if(existingTestFunctionality.size() > 0) {
				//existing listing has some, compare to the update to see if any are different
				for(CertificationResultTestFunctionality updatedItem : updatedTestFunctionality) { 
					boolean inExistingListing = false;
					for(CertificationResultTestFunctionality existingItem : existingTestFunctionality) {
						inExistingListing = updatedItem.matches(existingItem);
					}
					
					if(!inExistingListing) {
						if(updatedItem.getTestFunctionalityId() != null) {
							CertificationResultTestFunctionalityDTO toAdd = new CertificationResultTestFunctionalityDTO();
							toAdd.setCertificationResultId(certResult.getId());
							toAdd.setTestFunctionalityId(updatedItem.getTestFunctionalityId());
							testFuncToAdd.add(toAdd);
						}
					}
				}
			}
		}
				
		//figure out which test func to remove
		if(existingTestFunctionality != null && existingTestFunctionality.size() > 0) {
			//if the updated listing has none, remove them all from existing
			if(updatedTestFunctionality == null || updatedTestFunctionality.size() == 0) {
				for(CertificationResultTestFunctionality existingItem : existingTestFunctionality) {
					idsToRemove.add(existingItem.getId());
				}
			} else if(updatedTestFunctionality.size() > 0) {
				for(CertificationResultTestFunctionality existingItem : existingTestFunctionality) {
					boolean inUpdatedListing = false;
					for(CertificationResultTestFunctionality updatedItem : updatedTestFunctionality) {
						inUpdatedListing = updatedItem.matches(existingItem);
					}
					if(!inUpdatedListing) {
						idsToRemove.add(existingItem.getId());
					}
				}
			}
		}
		
		numChanges = testFuncToAdd.size() + idsToRemove.size();
		for(CertificationResultTestFunctionalityDTO toAdd : testFuncToAdd) {
			certResultDAO.addTestFunctionalityMapping(toAdd);
		}
		
		for(Long idToRemove : idsToRemove) {
			certResultDAO.deleteTestFunctionalityMapping(idToRemove);
		}	
		return numChanges;
	}

	private int updateTestTasks(CertifiedProductDTO listing, CertificationResult certResult,
			List<CertificationResultTestTask> existingTestTasks,
			List<CertificationResultTestTask> updatedTestTasks) 
	throws EntityCreationException, EntityRetrievalException {
		int numChanges = 0;
		List<CertificationResultTestTask> testTasksToAdd = new ArrayList<CertificationResultTestTask>();
		List<CertificationResultTestTaskPair> testTasksToUpdate = new ArrayList<CertificationResultTestTaskPair>();
		List<CertificationResultTestTask> testTasksToRemove = new ArrayList<CertificationResultTestTask>();
		
		//figure out which test tasks to add
		if(updatedTestTasks != null && updatedTestTasks.size() > 0) {
			if(existingTestTasks == null || existingTestTasks.size() == 0) {
				//existing listing has none, add all from the update
				for(CertificationResultTestTask updatedItem : updatedTestTasks) {
					testTasksToAdd.add(updatedItem);
				}
			} else if(existingTestTasks.size() > 0) {
				//existing listing has some, compare to the update to see if any are different
				for(CertificationResultTestTask updatedItem : updatedTestTasks) { 
					boolean inExistingListing = false;
					for(CertificationResultTestTask existingItem : existingTestTasks) {
						if(updatedItem.getId() != null && existingItem.getId() != null && 
							updatedItem.getId().longValue() == existingItem.getId().longValue() &&
							updatedItem.getTestTaskId() != null && existingItem.getTestTaskId() != null && 
							updatedItem.getTestTaskId().longValue() == existingItem.getTestTaskId().longValue()) {
							inExistingListing = true;
							testTasksToUpdate.add(new CertificationResultTestTaskPair(existingItem, updatedItem));
						}
					}
					
					if(!inExistingListing) {
						testTasksToAdd.add(updatedItem);
					} 
				}
			}
		}
				
		//figure out which test tasks to remove
		if(existingTestTasks != null && existingTestTasks.size() > 0) {
			//if the updated listing has none, remove them all from existing
			if(updatedTestTasks == null || updatedTestTasks.size() == 0) {
				for(CertificationResultTestTask existingItem : existingTestTasks) {
					testTasksToRemove.add(existingItem);
				}
			} else if(updatedTestTasks.size() > 0) {
				for(CertificationResultTestTask existingItem : existingTestTasks) {
					boolean inUpdatedListing = false;
					for(CertificationResultTestTask updatedItem : updatedTestTasks) {
						if(updatedItem.getId() != null && existingItem.getId() != null && 
							updatedItem.getId().longValue() == existingItem.getId().longValue() &&
							updatedItem.getTestTaskId() != null && existingItem.getTestTaskId() != null && 
							updatedItem.getTestTaskId().longValue() == existingItem.getTestTaskId().longValue()) {
							inUpdatedListing = true;
						}
					}
					if(!inUpdatedListing) {
						testTasksToRemove.add(existingItem);
					}
				}
			}
		}
		
		for(CertificationResultTestTask toAdd : testTasksToAdd) {
			numChanges += createTestTask(certResult, toAdd);
		}
		
		for(CertificationResultTestTask toRemove : testTasksToRemove) {
			numChanges += deleteTestTask(certResult, toRemove);
		}	
		
		for(CertificationResultTestTaskPair toUpdate : testTasksToUpdate) {
			numChanges += updateTestTask(certResult, toUpdate.getOrig(), toUpdate.getUpdated());
		}
		
		return numChanges;
	}
	
	private int createTestTask(CertificationResult certResult, CertificationResultTestTask task) 
			throws EntityCreationException, EntityRetrievalException {
		int numChanges = 1;
		TestTaskDTO taskToCreate = convert(task);
		TestTaskDTO createdTask = testTaskDAO.create(taskToCreate);
		CertificationResultTestTaskDTO certResultTask = new CertificationResultTestTaskDTO();
		certResultTask.setCertificationResultId(certResult.getId());
		certResultTask.setTestTaskId(createdTask.getId());
		certResultTask = certResultDAO.addTestTaskMapping(certResultTask);
		
		numChanges += updateTaskParticipants(certResultTask.getId(), new ArrayList<CertificationResultTestParticipant>(), 
						task.getTestParticipants());
		return numChanges;
	}
	
	
	private int deleteTestTask(CertificationResult certResult, CertificationResultTestTask task) 
			throws EntityCreationException, EntityRetrievalException {
		int numChanges = 0;
		numChanges += updateTaskParticipants(task.getId(), task.getTestParticipants(), null);
		certResultDAO.deleteTestTaskMapping(task.getId());
		numChanges++;
		
		if(task.getTestTaskId() != null) {
			testTaskDAO.delete(task.getTestTaskId());
			numChanges++;
		}
		return numChanges;
	}
	
	private int updateTestTask(CertificationResult certResult, CertificationResultTestTask existingTask, 
			CertificationResultTestTask updatedTask) throws EntityRetrievalException {
		int numChanges = 0;
		boolean isDifferent = false;
		if(!StringUtils.equals(existingTask.getDescription(), updatedTask.getDescription()) || 
			!StringUtils.equals(existingTask.getTaskRatingScale(), updatedTask.getTaskRatingScale()) ||
			!ObjectUtils.equals(existingTask.getTaskErrors(), updatedTask.getTaskErrors()) ||
			!ObjectUtils.equals(existingTask.getTaskErrorsStddev(), updatedTask.getTaskErrorsStddev()) ||
			!ObjectUtils.equals(existingTask.getTaskPathDeviationObserved(), updatedTask.getTaskPathDeviationObserved()) ||
			!ObjectUtils.equals(existingTask.getTaskPathDeviationOptimal(), updatedTask.getTaskPathDeviationOptimal()) ||
			!ObjectUtils.equals(existingTask.getTaskRating(), updatedTask.getTaskRating()) ||
			!StringUtils.equals(existingTask.getTaskRatingScale(), updatedTask.getTaskRatingScale()) ||
			!ObjectUtils.equals(existingTask.getTaskRatingStddev(), updatedTask.getTaskRatingStddev()) ||
			!ObjectUtils.equals(existingTask.getTaskSuccessAverage(), updatedTask.getTaskSuccessAverage()) ||
			!ObjectUtils.equals(existingTask.getTaskSuccessStddev(), updatedTask.getTaskSuccessStddev()) ||
			!ObjectUtils.equals(existingTask.getTaskTimeAvg(), updatedTask.getTaskTimeAvg()) ||
			!ObjectUtils.equals(existingTask.getTaskTimeDeviationObservedAvg(), updatedTask.getTaskTimeDeviationObservedAvg()) ||
			!ObjectUtils.equals(existingTask.getTaskTimeDeviationOptimalAvg(), updatedTask.getTaskTimeDeviationOptimalAvg()) ||
			!ObjectUtils.equals(existingTask.getTaskTimeStddev(), updatedTask.getTaskTimeStddev())) {
			isDifferent = true;
		}
		
		if(isDifferent) {
			TestTaskDTO taskToUpdate = convert(updatedTask);
			taskToUpdate.setId(existingTask.getId());
			testTaskDAO.update(taskToUpdate);
			numChanges++;
		}
		return numChanges;
	}
	
	private int updateTaskParticipants(Long certResultTaskId,
			List<CertificationResultTestParticipant> existingParticipants,
			List<CertificationResultTestParticipant> updatedParticipants) 
	throws EntityCreationException, EntityRetrievalException {
		int numChanges = 0;
		List<CertificationResultTestTaskParticipantDTO> participantsToAdd = new ArrayList<CertificationResultTestTaskParticipantDTO>();
		List<CertificationResultTestParticipantPair> participantsToUpdate = new ArrayList<CertificationResultTestParticipantPair>();
		List<Long> idsToRemove = new ArrayList<Long>();
		
		//figure out which participants to add
		if(updatedParticipants != null && updatedParticipants.size() > 0) {
			//fill in potentially missing participant id
			for(CertificationResultTestParticipant updatedItem : updatedParticipants) {
				if(updatedItem.getTestParticipantId() == null) {
					TestParticipantDTO participantToAdd = convert(updatedItem);
					TestParticipantDTO addedParticipant = testParticipantDAO.create(participantToAdd);
					updatedItem.setTestParticipantId(addedParticipant.getId());
				}
			}
			
			if(existingParticipants == null || existingParticipants.size() == 0) {
				//existing listing has none, add all from the update
				for(CertificationResultTestParticipant updatedItem : updatedParticipants) {
					if(updatedItem.getTestParticipantId() != null) {
						CertificationResultTestTaskParticipantDTO toAdd = new CertificationResultTestTaskParticipantDTO();
						toAdd.setCertTestTaskId(certResultTaskId);
						toAdd.setTestParticipantId(updatedItem.getTestParticipantId());
						participantsToAdd.add(toAdd);
					}
				}
			} else if(existingParticipants.size() > 0) {
				//existing listing has some, compare to the update to see if any are different
				for(CertificationResultTestParticipant updatedItem : updatedParticipants) { 
					boolean inExistingListing = false;
					for(CertificationResultTestParticipant existingItem : existingParticipants) {
						inExistingListing = updatedItem.matches(existingItem);
						participantsToUpdate.add(new CertificationResultTestParticipantPair(existingItem, updatedItem));
					}
					
					if(!inExistingListing) {
						if(updatedItem.getTestParticipantId() != null) {
							CertificationResultTestTaskParticipantDTO toAdd = new CertificationResultTestTaskParticipantDTO();
							toAdd.setCertTestTaskId(certResultTaskId);
							toAdd.setTestParticipantId(updatedItem.getTestParticipantId());
							participantsToAdd.add(toAdd);
						}
					}
				}
			}
		}
				
		//figure out which participants to remove
		if(existingParticipants != null && existingParticipants.size() > 0) {
			//if the updated listing has none, remove them all from existing
			if(updatedParticipants == null || updatedParticipants.size() == 0) {
				for(CertificationResultTestParticipant existingItem : existingParticipants) {
					idsToRemove.add(existingItem.getId());
				}
			} else if(updatedParticipants.size() > 0) {
				for(CertificationResultTestParticipant existingItem : existingParticipants) {
					boolean inUpdatedListing = false;
					for(CertificationResultTestParticipant updatedItem : updatedParticipants) {
						inUpdatedListing = updatedItem.matches(existingItem);
					}
					if(!inUpdatedListing) {
						idsToRemove.add(existingItem.getId());
					}
				}
			}
		}
		
		numChanges = participantsToAdd.size() + idsToRemove.size();
		for(CertificationResultTestTaskParticipantDTO toAdd : participantsToAdd) {
			certResultDAO.addTestParticipantMapping(toAdd);
		}
		
		for(CertificationResultTestParticipantPair toUpdate : participantsToUpdate) {
			boolean isDifferent = false;
			CertificationResultTestParticipant existingPart = toUpdate.getOrig();
			CertificationResultTestParticipant updatedPart = toUpdate.getUpdated();
			if(!StringUtils.equals(existingPart.getAgeRange(), updatedPart.getAgeRange()) || 
				!StringUtils.equals(existingPart.getAssistiveTechnologyNeeds(), updatedPart.getAssistiveTechnologyNeeds()) ||
				!ObjectUtils.equals(existingPart.getComputerExperienceMonths(), updatedPart.getComputerExperienceMonths()) ||
				!StringUtils.equals(existingPart.getEducationTypeName(), updatedPart.getEducationTypeName()) ||
				!StringUtils.equals(existingPart.getGender(), updatedPart.getGender()) ||
				!StringUtils.equals(existingPart.getOccupation(), updatedPart.getOccupation()) ||
				!ObjectUtils.equals(existingPart.getProductExperienceMonths(), updatedPart.getProductExperienceMonths()) ||
				!ObjectUtils.equals(existingPart.getProfessionalExperienceMonths(), updatedPart.getProfessionalExperienceMonths())) {
				isDifferent = true;
			}
			
			if(isDifferent) {
				TestParticipantDTO toUpdateDto = convert(toUpdate.getUpdated());
				testParticipantDAO.update(toUpdateDto);
				numChanges++;
			}
		}
		
		for(Long idToRemove : idsToRemove) {
			certResultDAO.deleteTestParticipantMapping(idToRemove);
		}	
		return numChanges;
	}

	private TestTaskDTO convert(CertificationResultTestTask task) {
		TestTaskDTO result = new TestTaskDTO();
		result.setId(task.getTestTaskId());
		result.setDescription(task.getDescription());
		result.setTaskErrors(task.getTaskErrors());
		result.setTaskErrorsStddev(task.getTaskErrorsStddev());
		result.setTaskPathDeviationObserved(task.getTaskPathDeviationObserved());
		result.setTaskPathDeviationOptimal(task.getTaskPathDeviationOptimal());
		result.setTaskRating(task.getTaskRating());
		result.setTaskRatingScale(task.getTaskRatingScale());
		result.setTaskRatingStddev(task.getTaskRatingStddev());
		result.setTaskSuccessAverage(task.getTaskSuccessAverage());
		result.setTaskSuccessStddev(task.getTaskSuccessStddev());
		result.setTaskTimeAvg(task.getTaskTimeAvg());
		result.setTaskTimeDeviationObservedAvg(task.getTaskTimeDeviationObservedAvg());
		result.setTaskTimeDeviationOptimalAvg(task.getTaskTimeDeviationOptimalAvg());
		result.setTaskTimeStddev(task.getTaskTimeStddev());
		return result;
	}
	
	private TestParticipantDTO convert(CertificationResultTestParticipant domain) {
		TestParticipantDTO result = new TestParticipantDTO();
		result.setId(domain.getTestParticipantId());
		result.setAssistiveTechnologyNeeds(domain.getAssistiveTechnologyNeeds());
		result.setComputerExperienceMonths(domain.getComputerExperienceMonths());
		result.setGender(domain.getGender());
		result.setOccupation(domain.getOccupation());
		result.setProductExperienceMonths(domain.getProductExperienceMonths());
		result.setProfessionalExperienceMonths(domain.getProfessionalExperienceMonths());
		if(domain.getAgeRangeId() == null && !StringUtils.isEmpty(domain.getAgeRange())) {
			AgeRangeDTO age = ageDao.getByName(domain.getAgeRange());
			if(age != null) {
				result.setAgeRangeId(age.getId());
			} else {
				logger.error("Could not find matching age range for " + domain.getAgeRange());
			}
		} else if(domain.getAgeRangeId() != null) {
			result.setAgeRangeId(domain.getAgeRangeId());
		}
		
		if(domain.getEducationTypeId() == null && !StringUtils.isEmpty(domain.getEducationTypeName())) {
			EducationTypeDTO educ = educDao.getByName(domain.getEducationTypeName());
			if(educ != null) {
				result.setEducationTypeId(educ.getId());
			} else {
				logger.error("Could not find matching education level " + domain.getEducationTypeName());
			}
		} else if(domain.getEducationTypeId() != null) {
			result.setEducationTypeId(domain.getEducationTypeId());
		}	
		return result;
	}
	
	@Override
	public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareMappingsForCertificationResult(Long certificationResultId){
		return certResultDAO.getAdditionalSoftwareForCertificationResult(certificationResultId);
	}
	
	@Override
	public List<CertificationResultUcdProcessDTO> getUcdProcessesForCertificationResult(Long certificationResultId) {
		return certResultDAO.getUcdProcessesForCertificationResult(certificationResultId);
	}
	@Override
	public List<CertificationResultTestStandardDTO> getTestStandardsForCertificationResult(Long certificationResultId){
		return certResultDAO.getTestStandardsForCertificationResult(certificationResultId);
	}
	
	@Override
	public List<CertificationResultTestToolDTO> getTestToolsForCertificationResult(Long certificationResultId){
		return certResultDAO.getTestToolsForCertificationResult(certificationResultId);
	}
	
	@Override
	public List<CertificationResultTestDataDTO> getTestDataForCertificationResult(Long certificationResultId){
		return certResultDAO.getTestDataForCertificationResult(certificationResultId);
	}
	
	@Override
	public List<CertificationResultTestProcedureDTO> getTestProceduresForCertificationResult(Long certificationResultId) {
		return certResultDAO.getTestProceduresForCertificationResult(certificationResultId);
	}
	
	@Override
	public List<CertificationResultTestFunctionalityDTO> getTestFunctionalityForCertificationResult(Long certificationResultId) {
		return certResultDAO.getTestFunctionalityForCertificationResult(certificationResultId);
	}
	
	@Override
	public List<CertificationResultMacraMeasureDTO> getG1MacraMeasuresForCertificationResult(Long certificationResultId) {
		return certResultDAO.getG1MacraMeasuresForCertificationResult(certificationResultId);
	}
	
	@Override
	public List<CertificationResultMacraMeasureDTO> getG2MacraMeasuresForCertificationResult(Long certificationResultId) {
		return certResultDAO.getG2MacraMeasuresForCertificationResult(certificationResultId);
	}
	
	@Override
	public List<CertificationResultTestTaskDTO> getTestTasksForCertificationResult(Long certificationResultId) {
		return certResultDAO.getTestTasksForCertificationResult(certificationResultId);
	}
	
	@Override
	public List<CertificationResultTestTaskParticipantDTO> getTestParticipantsForTask(Long certificationResultTaskId) {
		return certResultDAO.getTestParticipantsForTask(certificationResultTaskId);
	}
	
	private class CertificationResultTestTaskPair {
		private CertificationResultTestTask orig;
		private CertificationResultTestTask updated;
		
		public CertificationResultTestTaskPair() {}
		public CertificationResultTestTaskPair(CertificationResultTestTask orig, CertificationResultTestTask updated) {
			this.orig = orig;
			this.updated = updated;
		}
		public CertificationResultTestTask getOrig() {
			return orig;
		}
		public void setOrig(CertificationResultTestTask orig) {
			this.orig = orig;
		}
		public CertificationResultTestTask getUpdated() {
			return updated;
		}
		public void setUpdated(CertificationResultTestTask updated) {
			this.updated = updated;
		}
	}
	
	private class CertificationResultTestParticipantPair {
		private CertificationResultTestParticipant orig;
		private CertificationResultTestParticipant updated;
		
		public CertificationResultTestParticipantPair() {}
		public CertificationResultTestParticipantPair(CertificationResultTestParticipant orig, CertificationResultTestParticipant updated) {
			this.orig = orig;
			this.updated = updated;
		}
		public CertificationResultTestParticipant getOrig() {
			return orig;
		}
		public void setOrig(CertificationResultTestParticipant orig) {
			this.orig = orig;
		}
		public CertificationResultTestParticipant getUpdated() {
			return updated;
		}
		public void setUpdated(CertificationResultTestParticipant updated) {
			this.updated = updated;
		}
	}
}