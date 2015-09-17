package gov.healthit.chpl.manager.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.certifiedProduct.upload.CertifiedProductUploadHandler;
import gov.healthit.chpl.certifiedProduct.upload.CertifiedProductUploadHandlerFactory;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Service
public class PendingCertifiedProductManagerImpl extends ApplicationObjectSupport implements PendingCertifiedProductManager {

	@Autowired CertifiedProductUploadHandlerFactory uploadHandlerFactory;
	@Autowired CQMCriterionDAO cqmCriterionDAO;
	
	@Transactional
	@PreAuthorize("(hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) and hasPermission(#acb, admin)")
	@Override
	public List<PendingCertifiedProductDetails> upload(CertificationBodyDTO acb, MultipartFile file) 
		throws InvalidArgumentsException, IOException {
		
		List<PendingCertifiedProductDetails> results = new ArrayList<PendingCertifiedProductDetails>();
		
		BufferedReader reader = null;
		CSVParser parser = null;
		try {
			reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			parser = new CSVParser(reader, CSVFormat.EXCEL);
			
			List<CSVRecord> records = parser.getRecords();
			if(records.size() <= 1) {
				throw new InvalidArgumentsException("The file appears to have a header line with no other information. Please make sure there are at least two rows in the CSV file.");
			}
			
			CSVRecord heading = records.get(0);
			for(int i = 1; i < records.size(); i++) {
				CSVRecord record = records.get(i);
				
				//some rows may be blank, we just look at the first column to see if it's empty or not
				if(!StringUtils.isEmpty(record.get(0))) {
					CertifiedProductUploadHandler handler = uploadHandlerFactory.getHandler(heading, record);
				
					//create a certified product to pass into the handler
					try {
						PendingCertifiedProductDTO pendingCp = handler.parseRow();
						PendingCertifiedProductDetails details = new PendingCertifiedProductDetails(pendingCp);
						
						//set applicable criteria
						List<CQMCriterion> cqmCriteria = loadCQMCriteria();
						details.setApplicableCqmCriteria(handler.getApplicableCqmCriterion(cqmCriteria));
						
						//TODO: somehow have to associate this with an ACB
						results.add(details);
					} catch(EntityCreationException ex) {
						logger.error("could not create entity at row " + i + ". Message is " + ex.getMessage());
					}
				}
			}
		} catch(IOException ioEx) {
			logger.error("Could not get input stream for uploaded file " + file.getName());
			throw new IOException("Could not get input stream for uploaded file " + file.getName());
		} finally {
			 try { parser.close(); } catch(Exception ignore) {}
			try { reader.close(); } catch(Exception ignore) {}
		}
		return results;
	}

	@Override
	public void delete(PendingCertifiedProductDTO product) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<PendingCertifiedProductDTO> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PendingCertifiedProductDTO getById(Long id) throws EntityRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@PreAuthorize("(hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')) and hasPermission(#acb, admin)")
	@Override
	public List<PendingCertifiedProductDTO> getByAcb(CertificationBodyDTO acb) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<CQMCriterion> loadCQMCriteria() {
		List<CQMCriterion> result = new ArrayList<CQMCriterion>();
		
		List<CQMCriterionDTO> dtos = cqmCriterionDAO.findAll();
		for (CQMCriterionDTO dto: dtos) {
			CQMCriterion criterion = new CQMCriterion();
			criterion.setCmsId(dto.getCmsId());
			criterion.setCqmCriterionTypeId(dto.getCqmCriterionTypeId());
			criterion.setCqmDomain(dto.getCqmDomain());
			criterion.setCqmVersionId(dto.getCqmVersionId());
			criterion.setCqmVersion(dto.getCqmVersion());
			criterion.setCriterionId(dto.getId());
			criterion.setDescription(dto.getDescription());
			criterion.setNqfNumber(dto.getNqfNumber());
			criterion.setNumber(dto.getNumber());
			criterion.setTitle(dto.getTitle());
			result.add(criterion);
		}
		
		return result;
	}
}
