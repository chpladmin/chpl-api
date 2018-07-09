package gov.healthit.chpl.app.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.app.AppConfig;
import gov.healthit.chpl.app.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.app.presenter.SEDCsvPresenter;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.CertificationResultRules;
@Transactional
@Component("G3Sed2015ResourceCreatorApp")
public class G3Sed2015ResourceCreatorApp extends SEDDownloadableResourceCreatorApp{
	private static final String CRITERIA_NAME = "170.315 (g)(3)";
    private static final String EDITION = "2015";
    private static final Logger LOGGER = LogManager.getLogger(G3Sed2015ResourceCreatorApp.class);
    
    public G3Sed2015ResourceCreatorApp() {
    	super();
    }

    /**
     * This application generates a weekly summary email with an attached CSV
     * providing CHPL statistics
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        G3Sed2015ResourceCreatorApp app = new G3Sed2015ResourceCreatorApp();
        app.setLocalContext();
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        app.initiateSpringBeans(context);
        app.runJob(args);
        context.close();
    }
    
    protected ArrayList<SEDRow> getRelevantListings() throws EntityRetrievalException {
    	ArrayList<SEDRow> sedData = new ArrayList<SEDRow>();
    	CertificationCriterionDTO certCrit = getCriteriaDao().getByNameAndYear(CRITERIA_NAME, EDITION);
    	List<Long> ids = getCertificationResultDao().getCpIdsByCriterionId(certCrit.getId());
    	// go through all of the cps that are certified to 170.315(g)(3)
    	for(Long id : ids){
    		List<CertificationResultDetailsDTO> certificationResultDetailsDTOs = getCertificationResultDetailsDao()
    				.getCertificationResultDetailsByCertifiedProductIdSED(id);
    		CertifiedProductDetailsDTO listingDetails = getCertifiedProductDao().getDetailsById(id);
    		// for each certification result that is part of SED for certified product with certified_product_id = id
    		List<TestTask> listingTestTasks = new ArrayList<TestTask>();
    		for(int i=0;i<certificationResultDetailsDTOs.size();i++){
    			CertificationResultDetailsDTO certResult = certificationResultDetailsDTOs.get(i);
    			CertificationResult result = new CertificationResult(certResult);

    			// get all SED data for the listing
    			// ucd processes and test tasks with participants
    			CertificationCriterion criteria = new CertificationCriterion();
    			criteria.setNumber(result.getNumber());
    			criteria.setTitle(result.getTitle());
    			List<CertificationResultTestTaskDTO> testTasks = getCertificationResultDao().
    					getTestTasksForCertificationResult(certResult.getId());
    			for(CertificationResultTestTaskDTO tt : testTasks){
    				boolean alreadyExists = false;
    				TestTask newTestTask = new TestTask(tt);
    				for (TestTask currTestTask : listingTestTasks) {
    					if (newTestTask.matches(currTestTask)) {
    						alreadyExists = true;
    						currTestTask.getCriteria().add(criteria);
    					}
    				}
    				if (!alreadyExists) {
    					newTestTask.getCriteria().add(criteria);
    					listingTestTasks.add(newTestTask);
    				}
    			}
    		}
    		for(TestTask tt : listingTestTasks){
    			for(TestParticipant tp: tt.getTestParticipants()){
    				SEDRow row = new SEDRow();
    				ArrayList<String> criterion = new ArrayList<String>();
    				for(CertificationCriterion cc : tt.getCriteria()){
    					criterion.add(cc.getNumber());
    				}
    				if(criterion.size() > 1){
    					String criterionList = StringUtils.join(criterion, ";");
    					row.setCriteria(criterionList);
    				}else{
    					row.setCriteria(criterion.get(0));
    				}
    				row.setDetails(listingDetails);
    				row.setTestTask(tt);
    				row.setTestParticipant(tp);
    				sedData.add(row);
    			}
    		}
    	}
    	return sedData;
    }

	@Override
	protected void writeToFile(File downloadFolder, ArrayList<SEDRow> results) throws IOException {
        String csvFilename = downloadFolder.getAbsolutePath() + File.separator + "chpl-sed-all-details.csv";
        File csvFile = new File(csvFilename);
        if (!csvFile.exists()) {
            csvFile.createNewFile();
        } else {
            csvFile.delete();
        }
        SEDCsvPresenter csvPresenter = new SEDCsvPresenter();
        
        Date start = new Date();
        int numRows = csvPresenter.presentAsFile(csvFile, results);
        Date end = new Date();
        LOGGER.info("Wrote " + numRows + " rows to SED detailed data CSV file in " + (end.getTime() - start.getTime()) / 1000 + " seconds");
	}
}
