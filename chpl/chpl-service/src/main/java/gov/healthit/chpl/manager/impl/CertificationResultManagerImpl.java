package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.manager.CertificationResultManager;

@Service
public class CertificationResultManagerImpl implements
		CertificationResultManager {
	
	@Autowired private CertificationResultDAO certResultDAO;
	@Autowired private TestStandardDAO testStandardDAO;
	@Autowired private TestToolDAO testToolDAO;
	@Autowired private TestProcedureDAO testProcedureDAO;
	@Autowired private TestFunctionalityDAO testFunctionalityDAO;
	@Autowired private UcdProcessDAO ucdDao;

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public CertificationResultDTO create(Long acbId, CertificationResultDTO toCreate) throws EntityRetrievalException, EntityCreationException {
		CertificationResultDTO created = certResultDAO.create(toCreate);
		
		if(toCreate.getUcdProcesses() != null && toCreate.getUcdProcesses().size() > 0) {
			for(CertificationResultUcdProcessDTO ucdMapping : toCreate.getUcdProcesses()) {
				ucdMapping.setCertificationResultId(created.getId());
				CertificationResultUcdProcessDTO createdMapping = certResultDAO.addUcdProcessMapping(ucdMapping);
				created.getUcdProcesses().add(createdMapping);	
			}
		}
		
		for (CertificationResultAdditionalSoftwareDTO asMapping : toCreate.getAdditionalSoftware()){
			asMapping.setCertificationResultId(created.getId());
			CertificationResultAdditionalSoftwareDTO createdMapping = certResultDAO.addAdditionalSoftwareMapping(asMapping);
			created.getAdditionalSoftware().add(createdMapping);
		}
		
		for (CertificationResultTestStandardDTO mapping : toCreate.getTestStandards()){
			if(mapping.getTestStandardId() == null) {
				TestStandardDTO newTestStandard = new TestStandardDTO();
				newTestStandard.setName(mapping.getTestStandardName());
				newTestStandard = testStandardDAO.create(newTestStandard);
				mapping.setTestStandardId(newTestStandard.getId());
			}
			CertificationResultTestStandardDTO mappingToCreate = new CertificationResultTestStandardDTO();
			mappingToCreate.setCertificationResultId(created.getId());
			mappingToCreate.setTestStandardId(mapping.getTestStandardId());
			CertificationResultTestStandardDTO createdMapping = certResultDAO.addTestStandardMapping(mappingToCreate);
			created.getTestStandards().add(createdMapping);
		}
		
		for (CertificationResultTestToolDTO mapping : toCreate.getTestTools()){
			if(mapping.getTestToolId() == null) {
				TestToolDTO newTestTool = new TestToolDTO();
				newTestTool.setName(mapping.getTestToolName());
				newTestTool.setVersion(mapping.getTestToolVersion());
				newTestTool = testToolDAO.create(newTestTool);
				mapping.setTestToolId(newTestTool.getId());
			}
			CertificationResultTestToolDTO mappingToCreate = new CertificationResultTestToolDTO();
			mappingToCreate.setCertificationResultId(created.getId());
			mappingToCreate.setTestToolId(mapping.getTestToolId());
			CertificationResultTestToolDTO createdMapping = certResultDAO.addTestToolMapping(mappingToCreate);
			created.getTestTools().add(createdMapping);
		}
		
		for (CertificationResultTestDataDTO mapping : toCreate.getTestData()){
			mapping.setCertificationResultId(created.getId());
			CertificationResultTestDataDTO createdMapping = certResultDAO.addTestDataMapping(mapping);
			created.getTestData().add(createdMapping);
		}
		
		for (CertificationResultTestProcedureDTO mapping : toCreate.getTestProcedures()){
			if(mapping.getTestProcedureId() == null) {
				TestProcedureDTO newTestProcedure = new TestProcedureDTO();
				newTestProcedure.setVersion(mapping.getTestProcedureVersion());
				newTestProcedure = testProcedureDAO.create(newTestProcedure);
				mapping.setTestProcedureId(newTestProcedure.getId());
			}
			CertificationResultTestProcedureDTO mappingToCreate = new CertificationResultTestProcedureDTO();
			mappingToCreate.setCertificationResultId(created.getId());
			mappingToCreate.setTestProcedureId(mapping.getTestProcedureId());
			CertificationResultTestProcedureDTO createdMapping = certResultDAO.addTestProcedureMapping(mappingToCreate);
			created.getTestProcedures().add(createdMapping);
		}
		
		for (CertificationResultTestFunctionalityDTO mapping : toCreate.getTestFunctionality()){
			if(mapping.getTestFunctionalityId() == null) {
				TestFunctionalityDTO newTestFunctionality = new TestFunctionalityDTO();
				newTestFunctionality.setName(mapping.getTestFunctionalityName());
				newTestFunctionality = testFunctionalityDAO.create(newTestFunctionality);
				mapping.setTestFunctionalityId(newTestFunctionality.getId());
			}
			CertificationResultTestFunctionalityDTO mappingToCreate = new CertificationResultTestFunctionalityDTO();
			mappingToCreate.setCertificationResultId(created.getId());
			mappingToCreate.setTestFunctionalityId(mapping.getTestFunctionalityId());
			CertificationResultTestFunctionalityDTO createdMapping = certResultDAO.addTestFunctionalityMapping(mappingToCreate);
			created.getTestFunctionality().add(createdMapping);
		}
		return created;
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public CertificationResultDTO update(Long acbId, CertificationResultDTO toUpdate) throws EntityRetrievalException, EntityCreationException {
		CertificationResultDTO updated = certResultDAO.update(toUpdate);
		
		//update ucd processes
		List<CertificationResultUcdProcessDTO> existingUcdProcesses = certResultDAO.getUcdProcessesForCertificationResult(toUpdate.getId());
		List<CertificationResultUcdProcessDTO> ucdProcessesToAdd = new ArrayList<CertificationResultUcdProcessDTO>();
		List<CertificationResultUcdProcessDTO> ucdProcessesToRemove = new ArrayList<CertificationResultUcdProcessDTO>();

		for (CertificationResultUcdProcessDTO toUpdateMapping : toUpdate.getUcdProcesses()){
			if(toUpdateMapping.getId() == null) {
				if(toUpdateMapping.getUcdProcessId() == null) {
					UcdProcessDTO ucdProc = ucdDao.getByName(toUpdateMapping.getUcdProcessName());
					if(ucdProc == null) {
						UcdProcessDTO ucdToCreate = new UcdProcessDTO();
						ucdToCreate.setName(toUpdateMapping.getUcdProcessName());
						ucdProc = ucdDao.create(ucdToCreate);
					}
					toUpdateMapping.setUcdProcessId(ucdProc.getId());
				}
				toUpdateMapping.setCertificationResultId(toUpdate.getId());
				toUpdateMapping.setUcdProcessDetails(toUpdateMapping.getUcdProcessDetails());
				ucdProcessesToAdd.add(toUpdateMapping);
			} 
		}
				
		for(CertificationResultUcdProcessDTO currMapping : existingUcdProcesses) {
			boolean isInUpdate = false;
			for (CertificationResultUcdProcessDTO toUpdateMapping : toUpdate.getUcdProcesses()){
				if(toUpdateMapping.getId() != null && 
						toUpdateMapping.getId().longValue() == currMapping.getId().longValue()) {
					isInUpdate = true;
				}
			}
			if(!isInUpdate) {
				ucdProcessesToRemove.add(currMapping);
			}
		}
					
		for(CertificationResultUcdProcessDTO toAdd : ucdProcessesToAdd) {
			certResultDAO.addUcdProcessMapping(toAdd);
		}
		for(CertificationResultUcdProcessDTO toRemove : ucdProcessesToRemove) {
			certResultDAO.deleteUcdProcessMapping(toRemove.getId());
		}
		updated.setUcdProcesses(certResultDAO.getUcdProcessesForCertificationResult(updated.getId()));
		
		
		//update additional software mappings
		List<CertificationResultAdditionalSoftwareDTO> existingMappings = certResultDAO.getAdditionalSoftwareForCertificationResult(toUpdate.getId());
		List<CertificationResultAdditionalSoftwareDTO> mappingsToAdd = new ArrayList<CertificationResultAdditionalSoftwareDTO>();
		List<CertificationResultAdditionalSoftwareDTO> mappingsToRemove = new ArrayList<CertificationResultAdditionalSoftwareDTO>();

		for (CertificationResultAdditionalSoftwareDTO toUpdateMapping : toUpdate.getAdditionalSoftware()){
			if(toUpdateMapping.getId() == null) {
				toUpdateMapping.setCertificationResultId(toUpdate.getId());
				mappingsToAdd.add(toUpdateMapping);
			} 
		}
		
		for(CertificationResultAdditionalSoftwareDTO currMapping : existingMappings) {
			boolean isInUpdate = false;
			for (CertificationResultAdditionalSoftwareDTO toUpdateMapping : toUpdate.getAdditionalSoftware()){
				if(toUpdateMapping.getId() != null && 
						toUpdateMapping.getId().longValue() == currMapping.getId().longValue()) {
					isInUpdate = true;
				}
			}
			if(!isInUpdate) {
				mappingsToRemove.add(currMapping);
			}
		}
			
		for(CertificationResultAdditionalSoftwareDTO toAdd : mappingsToAdd) {
			certResultDAO.addAdditionalSoftwareMapping(toAdd);
		}
		for(CertificationResultAdditionalSoftwareDTO toRemove : mappingsToRemove) {
			certResultDAO.deleteAdditionalSoftwareMapping(toRemove.getId());
		}
		updated.setAdditionalSoftware(certResultDAO.getAdditionalSoftwareForCertificationResult(updated.getId()));
		
		
		//update test standard mappings
		List<CertificationResultTestStandardDTO> existingTestStandards = certResultDAO.getTestStandardsForCertificationResult(toUpdate.getId());
		List<CertificationResultTestStandardDTO> testStandardsToAdd = new ArrayList<CertificationResultTestStandardDTO>();
		List<CertificationResultTestStandardDTO> testStandardsToRemove = new ArrayList<CertificationResultTestStandardDTO>();

		for (CertificationResultTestStandardDTO toUpdateMapping : toUpdate.getTestStandards()){
			if(toUpdateMapping.getId() == null) {
				if(toUpdateMapping.getTestStandardId() == null) {
					TestStandardDTO testStd = testStandardDAO.getByNumber(toUpdateMapping.getTestStandardNumber());
					if(testStd == null) {
						TestStandardDTO testStandardToCreate = new TestStandardDTO();
						testStandardToCreate.setNumber(toUpdateMapping.getTestStandardNumber());
						testStd = testStandardDAO.create(testStandardToCreate);
					}
					toUpdateMapping.setTestStandardId(testStd.getId());
				}
				toUpdateMapping.setCertificationResultId(toUpdate.getId());
				testStandardsToAdd.add(toUpdateMapping);
			} 
		}
				
		for(CertificationResultTestStandardDTO currMapping : existingTestStandards) {
			boolean isInUpdate = false;
			for (CertificationResultTestStandardDTO toUpdateMapping : toUpdate.getTestStandards()){
				if(toUpdateMapping.getId() != null && 
						toUpdateMapping.getId().longValue() == currMapping.getId().longValue()) {
					isInUpdate = true;
				}
			}
			if(!isInUpdate) {
				testStandardsToRemove.add(currMapping);
			}
		}
					
		for(CertificationResultTestStandardDTO toAdd : testStandardsToAdd) {
			certResultDAO.addTestStandardMapping(toAdd);
		}
		for(CertificationResultTestStandardDTO toRemove : testStandardsToRemove) {
			certResultDAO.deleteTestStandardMapping(toRemove.getId());
		}
		updated.setTestStandards(certResultDAO.getTestStandardsForCertificationResult(updated.getId()));
		
		
		//update test tool mappings
		List<CertificationResultTestToolDTO> existingTestTools = certResultDAO.getTestToolsForCertificationResult(toUpdate.getId());
		List<CertificationResultTestToolDTO> testToolsToAdd = new ArrayList<CertificationResultTestToolDTO>();
		List<CertificationResultTestToolDTO> testToolsToRemove = new ArrayList<CertificationResultTestToolDTO>();

		for (CertificationResultTestToolDTO toUpdateMapping : toUpdate.getTestTools()){
			if(toUpdateMapping.getId() == null) {
				if(toUpdateMapping.getTestToolId() == null) {
					TestToolDTO testToolToCreate = new TestToolDTO();
					testToolToCreate.setName(toUpdateMapping.getTestToolName());
					testToolToCreate.setVersion(toUpdateMapping.getTestToolVersion());
					testToolToCreate = testToolDAO.create(testToolToCreate);
					toUpdateMapping.setTestToolId(testToolToCreate.getId());
				} 
				toUpdateMapping.setCertificationResultId(toUpdate.getId());
				testToolsToAdd.add(toUpdateMapping);
			} 
		}
				
		for(CertificationResultTestToolDTO currMapping : existingTestTools) {
			boolean isInUpdate = false;
			for (CertificationResultTestToolDTO toUpdateMapping : toUpdate.getTestTools()){
				if(toUpdateMapping.getId() != null && 
						toUpdateMapping.getId().longValue() == currMapping.getId().longValue()) {
					isInUpdate = true;
				}
			}
			if(!isInUpdate) {
				testToolsToRemove.add(currMapping);
			}
		}
					
		for(CertificationResultTestToolDTO toAdd : testToolsToAdd) {
			certResultDAO.addTestToolMapping(toAdd);
		}
		for(CertificationResultTestToolDTO toRemove : testToolsToRemove) {
			certResultDAO.deleteTestToolMapping(toRemove.getId());
		}
		updated.setTestTools(certResultDAO.getTestToolsForCertificationResult(updated.getId()));
				
		//update test data
		List<CertificationResultTestDataDTO> existingTestData = certResultDAO.getTestDataForCertificationResult(toUpdate.getId());
		List<CertificationResultTestDataDTO> testDataToAdd = new ArrayList<CertificationResultTestDataDTO>();
		List<CertificationResultTestDataDTO> testDataToRemove = new ArrayList<CertificationResultTestDataDTO>();

		for (CertificationResultTestDataDTO toUpdateMapping : toUpdate.getTestData()){
			if(toUpdateMapping.getId() == null) {
				toUpdateMapping.setCertificationResultId(toUpdate.getId());
				testDataToAdd.add(toUpdateMapping);
			} 
		}
		
		for(CertificationResultTestDataDTO currMapping : existingTestData) {
			boolean isInUpdate = false;
			for (CertificationResultTestDataDTO toUpdateMapping : toUpdate.getTestData()){
				if(toUpdateMapping.getId() != null && 
						toUpdateMapping.getId().longValue() == currMapping.getId().longValue()) {
					isInUpdate = true;
				}
			}
			if(!isInUpdate) {
				testDataToRemove.add(currMapping);
			}
		}
			
		for(CertificationResultTestDataDTO toAdd : testDataToAdd) {
			certResultDAO.addTestDataMapping(toAdd);
		}
		for(CertificationResultTestDataDTO toRemove : testDataToRemove) {
			certResultDAO.deleteTestDataMapping(toRemove.getId());
		}
		updated.setTestData(certResultDAO.getTestDataForCertificationResult(updated.getId()));
		
		//update test procedure mappings
		List<CertificationResultTestProcedureDTO> existingTestProcedures = certResultDAO.getTestProceduresForCertificationResult(toUpdate.getId());
		List<CertificationResultTestProcedureDTO> testProceduresToAdd = new ArrayList<CertificationResultTestProcedureDTO>();
		List<CertificationResultTestProcedureDTO> testProceduresToRemove = new ArrayList<CertificationResultTestProcedureDTO>();

		for (CertificationResultTestProcedureDTO toUpdateMapping : toUpdate.getTestProcedures()){
			if(toUpdateMapping.getId() == null) {
				if(toUpdateMapping.getTestProcedureId() == null) {
					TestProcedureDTO testProcedureToCreate = new TestProcedureDTO();
					testProcedureToCreate.setVersion(toUpdateMapping.getTestProcedureVersion());
					testProcedureToCreate = testProcedureDAO.create(testProcedureToCreate);
					toUpdateMapping.setTestProcedureId(testProcedureToCreate.getId());
				}
				toUpdateMapping.setCertificationResultId(toUpdate.getId());
				testProceduresToAdd.add(toUpdateMapping);
			} 
		}
				
		for(CertificationResultTestProcedureDTO currMapping : existingTestProcedures) {
			boolean isInUpdate = false;
			for (CertificationResultTestProcedureDTO toUpdateMapping : toUpdate.getTestProcedures()){
				if(toUpdateMapping.getId() != null && 
						toUpdateMapping.getId().longValue() == currMapping.getId().longValue()) {
					isInUpdate = true;
				}
			}
			if(!isInUpdate) {
				testProceduresToRemove.add(currMapping);
			}
		}
					
		for(CertificationResultTestProcedureDTO toAdd : testProceduresToAdd) {
			certResultDAO.addTestProcedureMapping(toAdd);
		}
		for(CertificationResultTestProcedureDTO toRemove : testProceduresToRemove) {
			certResultDAO.deleteTestProcedureMapping(toRemove.getId());
		}
		updated.setTestProcedures(certResultDAO.getTestProceduresForCertificationResult(updated.getId()));
		
		//update test functionality mappings
		List<CertificationResultTestFunctionalityDTO> existingTestFunctionality = certResultDAO.getTestFunctionalityForCertificationResult(toUpdate.getId());
		List<CertificationResultTestFunctionalityDTO> testFunctionalityToAdd = new ArrayList<CertificationResultTestFunctionalityDTO>();
		List<CertificationResultTestFunctionalityDTO> testFunctionalityToRemove = new ArrayList<CertificationResultTestFunctionalityDTO>();

		for (CertificationResultTestFunctionalityDTO toUpdateMapping : toUpdate.getTestFunctionality()){
			if(toUpdateMapping.getId() == null) {
				if(toUpdateMapping.getTestFunctionalityId() == null) {
					TestFunctionalityDTO testFunc = testFunctionalityDAO.getByNumber(toUpdateMapping.getTestFunctionalityNumber());
					if(testFunc == null) {
						TestFunctionalityDTO testFunctionalityToCreate = new TestFunctionalityDTO();
						testFunctionalityToCreate.setNumber(toUpdateMapping.getTestFunctionalityNumber());
						testFunc = testFunctionalityDAO.create(testFunctionalityToCreate);
					}
					toUpdateMapping.setTestFunctionalityId(testFunc.getId());
				}
				toUpdateMapping.setCertificationResultId(toUpdate.getId());
				testFunctionalityToAdd.add(toUpdateMapping);
			} 
		}
				
		for(CertificationResultTestFunctionalityDTO currMapping : existingTestFunctionality) {
			boolean isInUpdate = false;
			for (CertificationResultTestFunctionalityDTO toUpdateMapping : toUpdate.getTestFunctionality()){
				if(toUpdateMapping.getId() != null && 
						toUpdateMapping.getId().longValue() == currMapping.getId().longValue()) {
					isInUpdate = true;
				}
			}
			if(!isInUpdate) {
				testFunctionalityToRemove.add(currMapping);
			}
		}
					
		for(CertificationResultTestFunctionalityDTO toAdd : testFunctionalityToAdd) {
			certResultDAO.addTestFunctionalityMapping(toAdd);
		}
		for(CertificationResultTestFunctionalityDTO toRemove : testFunctionalityToRemove) {
			certResultDAO.deleteTestFunctionalityMapping(toRemove.getId());
		}
		updated.setTestFunctionality(certResultDAO.getTestFunctionalityForCertificationResult(updated.getId()));
		
		return updated;
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
}