package gov.healthit.chpl.app.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.app.App;
import gov.healthit.chpl.app.AppConfig;
import gov.healthit.chpl.app.LocalContext;
import gov.healthit.chpl.app.LocalContextFactory;
import gov.healthit.chpl.app.NotificationEmailerReportApp;
import gov.healthit.chpl.app.presenter.CertifiedProduct2014CsvPresenter;
import gov.healthit.chpl.app.presenter.CertifiedProductCsvPresenter;
import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.SEDRow;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestTaskDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.entity.listing.TestTaskParticipantMapEntity;
import gov.healthit.chpl.util.CertificationResultRules;
@Transactional
@Component("G3Sed2015ResourceCreatorApp")
public class G3Sed2015ResourceCreatorApp extends SEDDownloadableResourceCreatorApp{
	private static final String CRITERIA_NAME = "170.315 (g)(3)";
    private static final String EDITION = "2015";
    private static final Logger LOGGER = LogManager.getLogger(G3Sed2015ResourceCreatorApp.class);
    int num = 0;
    

    private CertificationResultRules certRules = new CertificationResultRules();
    
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
    
    public List<TestTask> addCriteriaToTestTasks(CertificationResultDetailsDTO certResult) throws EntityRetrievalException{
    	ArrayList<TestTask> returnTestTasks = new ArrayList<TestTask>();
    	CertificationResult result = new CertificationResult(certResult);

    	// get all SED data for the listing
    	// ucd processes and test tasks with participants
    	CertificationCriterion criteria = new CertificationCriterion();
    	criteria.setNumber(result.getNumber());
    	criteria.setTitle(result.getTitle());

    	List<CertificationResultTestTaskDTO> testTasks = getCertificationResultDao().getTestTasksForCertificationResult(certResult.getId());
    	for (CertificationResultTestTaskDTO currResult : testTasks) {
            boolean alreadyExists = false;
            TestTask newTestTask = new TestTask(currResult);
            for (TestTask currTestTask : searchDetails.getSed().getTestTasks()) {
                if (newTestTask.matches(currTestTask)) {
                    alreadyExists = true;
                    currTestTask.getCriteria().add(criteria);
                }
            }
            if (!alreadyExists) {
                newTestTask.getCriteria().add(criteria);
                searchDetails.getSed().getTestTasks().add(newTestTask);
            }
        }
    	return returnTestTasks;
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
    		for(int i=0;i<certificationResultDetailsDTOs.size();i++){
    			CertificationResultDetailsDTO certificationResult = certificationResultDetailsDTOs.get(i);
    			List<TestTask> testTasks = addCriteriaToTestTasks(certificationResult);
    			// for each test task of a certification result 
    			for(TestTask tt : testTasks){
    				// for each test participant create a row in the SED file
    				for(TestParticipant tp : tt.getTestParticipants()){
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
    					row.setCertificationResult(certificationResult);
    					row.setTestParticipant(tp);
    					sedData.add(row);
    				}
    			}
    		}
    	}
    	return sedData;
    }

	@Override
	protected void writeToFile(File downloadFolder, ArrayList<SEDRow> results) throws IOException {
		Date now = new Date();
		// present as csv
        String csvFilename = downloadFolder.getAbsolutePath() + File.separator + "chpl-" + "sed" + "-"
                + getTimestampFormat().format(now) + ".csv";
        File csvFile = new File(csvFilename);
        if (!csvFile.exists()) {
            csvFile.createNewFile();
        } else {
            csvFile.delete();
        }
        CertifiedProductCsvPresenter csvPresenter = new CertifiedProductCsvPresenter();
        
        Date start = new Date();
        int numRows = csvPresenter.presentAsFileSED(csvFile, results);
        Date end = new Date();
        LOGGER.info("Wrote " + numRows + " rows to SED detailed data CSV file in " + (end.getTime() - start.getTime()) / 1000 + " seconds");
	}
}
