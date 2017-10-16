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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
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
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.SEDRow;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestTaskDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.entity.listing.TestTaskParticipantMapEntity;
@Transactional
@Component("G3Sed2015ResourceCreatorApp")
public class G3Sed2015ResourceCreatorApp extends SEDDownloadableResourceCreatorApp{
	private static final String CRITERIA_NAME = "170.315 (g)(3)";
    private static final String EDITION = "2015";
    private static final Logger LOGGER = LogManager.getLogger(G3Sed2015ResourceCreatorApp.class);
    int num = 0;
    
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
    	for(Long id : ids){
    		List<CertificationResultDTO> certificationResultsForId = getCertificationResultDao().findByCertifiedProductIdSED(id);
    		CertifiedProductDetailsDTO listingDetails = getCertifiedProductDao().getDetailsById(id);
    		for(int i=0;i<certificationResultsForId.size();i++){
    			CertificationResultDTO certificationResult = certificationResultsForId.get(i);
    			String criterionName = getCriteriaDao().getById(certificationResult.getCertificationCriterionId()).getNumber();
    			List<CertificationResultTestTaskDTO> certificationResultsTestTasks = getCertificationResultDao().getTestTasksForCertificationResult(certificationResult.getId());
    			for(int j=0;j<certificationResultsTestTasks.size();j++){
    				SEDRow row = new SEDRow();
    				row.setDetails(listingDetails);
    				row.setCriteria(criterionName);
    				row.setCertificationResult(certificationResult);
    				row.setTestTask(certificationResultsTestTasks.get(j));
    				sedData.add(row);
    			}
    		}
    	}
    	return sedData;
    }

	@Override
	protected void writeToFile(File downloadFolder, ArrayList<SEDRow> results) throws IOException {
		Date now = new Date();
		// present as csv
        String csvFilename = downloadFolder.getAbsolutePath() + File.separator + "chpl-" + "sed-all-details" + "-"
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
