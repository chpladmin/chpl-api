package gov.healthit.chpl.app.resource;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import gov.healthit.chpl.app.App;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

public abstract class DownloadableResourceCreatorApp extends App {
    private static final Logger LOGGER = LogManager.getLogger(DownloadableResourceCreatorApp.class);

    protected SimpleDateFormat timestampFormat;
    protected CertifiedProductDetailsManager cpdManager;
    protected CertifiedProductDAO certifiedProductDao;
    protected CertificationCriterionDAO criteriaDao;
    protected CertificationResultDAO certificationResultDao;
    protected CertificationResultDetailsDAO certificationResultDetailsDAO;
    private AbstractApplicationContext applicationContext;

    public DownloadableResourceCreatorApp() {
        timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }

    @Override
    protected void initiateSpringBeans(final AbstractApplicationContext context) throws IOException {
        this.setCpdManager((CertifiedProductDetailsManager) context.getBean("certifiedProductDetailsManager"));
        this.setCertifiedProductDao((CertifiedProductDAO) context.getBean("certifiedProductDAO"));
        this.setCriteriaDao((CertificationCriterionDAO) context.getBean("certificationCriterionDAO"));
        this.setCertificationResultDao((CertificationResultDAO) context.getBean("certificationResultDAO"));
        this.setCertificationResultDetailsDao((CertificationResultDetailsDAO) 
                context.getBean("certificationResultDetailsDAO"));

        setApplicationContext(context);
    }

    protected abstract void runJob(String[] args) throws Exception;

    public CertifiedProductDAO getCertifiedProductDao() {
        return certifiedProductDao;
    }

    public void setCertifiedProductDao(final CertifiedProductDAO certifiedProductDAO) {
        this.certifiedProductDao = certifiedProductDAO;
    }

    public SimpleDateFormat getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(final SimpleDateFormat timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public CertifiedProductDetailsManager getCpdManager() {
        return cpdManager;
    }

    public void setCpdManager(final CertifiedProductDetailsManager cpdManager) {
        this.cpdManager = cpdManager;
    }

    public CertificationCriterionDAO getCriteriaDao() {
        return criteriaDao;
    }

    public void setCriteriaDao(final CertificationCriterionDAO criteriaDao) {
        this.criteriaDao = criteriaDao;
    }

    public CertificationResultDAO getCertificationResultDao() {
        return certificationResultDao;
    }

    public void setCertificationResultDao(
			CertificationResultDAO certificationResultDao) {
		this.certificationResultDao = certificationResultDao;
    }

    public CertificationResultDetailsDAO getCertificationResultDetailsDao() {
		return certificationResultDetailsDAO;
    }

    public void setCertificationResultDetailsDao(
			CertificationResultDetailsDAO certificationResultDetailsDAO) {
		this.certificationResultDetailsDAO = certificationResultDetailsDAO;
    }

	public AbstractApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(final AbstractApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
