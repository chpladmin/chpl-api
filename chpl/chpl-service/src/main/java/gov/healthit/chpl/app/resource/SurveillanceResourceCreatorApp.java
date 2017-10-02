package gov.healthit.chpl.app.resource;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.AppConfig;
import gov.healthit.chpl.app.surveillance.presenter.NonconformityCsvPresenter;
import gov.healthit.chpl.app.surveillance.presenter.SurveillanceCsvPresenter;
import gov.healthit.chpl.app.surveillance.presenter.SurveillanceReportCsvPresenter;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

@Component("surveillanceResourceCreatorApp")
public class SurveillanceResourceCreatorApp extends DownloadableResourceCreatorApp {
	private static final Logger logger = LogManager.getLogger(SurveillanceResourceCreatorApp.class);

	public SurveillanceResourceCreatorApp() {
		super();
	}

	protected List<CertifiedProductDetailsDTO> getRelevantListings() {
		logger.info("Finding all listings with surveillance");
		Date start = new Date();
		List<CertifiedProductDetailsDTO> listingsForEdition = getCertifiedProductDao().findWithSurveillance();
		Date end = new Date();
		logger.info("Found " + listingsForEdition.size() + " listings with surveillance in " + (end.getTime() - start.getTime())/1000 + " seconds");
		return listingsForEdition;
	}

	protected void writeToFile(File downloadFolder, CertifiedProductDownloadResponse results) throws IOException {
		 //write out a csv file containing all surveillance
        String allSurvCsvFilename = downloadFolder.getAbsolutePath() + File.separator +
        		"surveillance-all.csv";
        File allSurvCsvFile = new File(allSurvCsvFilename);
        if(!allSurvCsvFile.exists()) {
        	allSurvCsvFile.createNewFile();
        } else {
        	allSurvCsvFile.delete();
        }
        SurveillanceCsvPresenter survCsvPresenter = new SurveillanceCsvPresenter();
        survCsvPresenter.setProps(getProperties());

        logger.info("Writing all surveillance CSV file");
        Date start = new Date();
        survCsvPresenter.presentAsFile(allSurvCsvFile, results);
        Date end = new Date();
        logger.info("Wrote all surveillance CSV file in " + (end.getTime() - start.getTime())/1000 + " seconds");

        //write out a csv file containing surveillance with nonconformities
        String nonconformityCsvFilename = downloadFolder.getAbsolutePath() + File.separator +
        		"surveillance-with-nonconformities.csv";
        File nonconformityCsvFile = new File(nonconformityCsvFilename);
        if(!nonconformityCsvFile.exists()) {
        	nonconformityCsvFile.createNewFile();
        } else {
        	nonconformityCsvFile.delete();
        }

        NonconformityCsvPresenter ncCsvPresenter = new NonconformityCsvPresenter();
        ncCsvPresenter.setProps(getProperties());
        logger.info("Writing nonconformity CSV file");
        start = new Date();
        ncCsvPresenter.presentAsFile(nonconformityCsvFile, results);
        end = new Date();
        logger.info("Wrote nonconformity CSV file in " + (end.getTime() - start.getTime())/1000 + " seconds");

        //write out a csv file containing surveillance basic report
        String basicReportCsvName = downloadFolder.getAbsolutePath() + File.separator +
        		"surveillance-basic-report.csv";
        File basicReportCsvFile = new File(basicReportCsvName);
        if(!basicReportCsvFile.exists()) {
        	basicReportCsvFile.createNewFile();
        } else {
        	basicReportCsvFile.delete();
        }

        SurveillanceReportCsvPresenter basicReportCsvPresenter = new SurveillanceReportCsvPresenter();
        basicReportCsvPresenter.setProps(getProperties());
        logger.info("Writing basic surveillance report file");
        start = new Date();
        basicReportCsvPresenter.presentAsFile(basicReportCsvFile, results);
        end = new Date();
        logger.info("Wrote basic surveillance report file in " + (end.getTime() - start.getTime())/1000 + " seconds");
	}

	public static void main(String[] args) throws Exception {
		SurveillanceResourceCreatorApp app = new SurveillanceResourceCreatorApp();
		app.setLocalContext();
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		app.initiateSpringBeans(context);
		app.runJob(args);
		context.close();
	}
}
