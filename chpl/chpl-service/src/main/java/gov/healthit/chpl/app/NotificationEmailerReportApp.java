package gov.healthit.chpl.app;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

public abstract class NotificationEmailerReportApp extends App {
    protected SimpleDateFormat timestampFormat;
    protected CertifiedProductDetailsManager cpdManager;
    protected CertifiedProductDAO certifiedProductDAO;
    protected SendMailUtil mailUtils;
    protected NotificationDAO notificationDAO;
    protected CertificationBodyDAO certificationBodyDAO;
    protected CertificationEditionDAO editionDAO;

    protected static final Logger LOGGER = LogManager.getLogger(NotificationEmailerReportApp.class);

    public NotificationEmailerReportApp() {
        timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }

    public Map<CertificationBodyDTO, CertifiedProductDownloadResponse> getCertificationDownloadResponse(
            List<CertifiedProductSearchDetails> allCertifiedProductDetails, List<CertificationBodyDTO> acbs) {
        Map<CertificationBodyDTO, CertifiedProductDownloadResponse> certificationDownloadResponse = new HashMap<CertificationBodyDTO, CertifiedProductDownloadResponse>();

        for (CertificationBodyDTO cbDTO : acbs) {
            CertifiedProductDownloadResponse cpDlResponse = new CertifiedProductDownloadResponse();
            List<CertifiedProductSearchDetails> acbCpSearchDetails = new ArrayList<CertifiedProductSearchDetails>();
            for (CertifiedProductSearchDetails cpDetail : allCertifiedProductDetails) {
                if (cpDetail.getCertifyingBody().get("code").toString().equalsIgnoreCase(cbDTO.getAcbCode())) {
                    acbCpSearchDetails.add(cpDetail);
                }
            }
            cpDlResponse.setListings(acbCpSearchDetails);
            certificationDownloadResponse.put(cbDTO, cpDlResponse);
        }
        return certificationDownloadResponse;
    }

    @Override
    protected void initiateSpringBeans(AbstractApplicationContext context) throws IOException {
        this.setCpdManager((CertifiedProductDetailsManager) context.getBean("certifiedProductDetailsManager"));
        this.setCertifiedProductDAO((CertifiedProductDAO) context.getBean("certifiedProductDAO"));
        this.setNotificationDAO((NotificationDAO) context.getBean("notificationDAO"));
        this.setCertificationBodyDAO((CertificationBodyDAO) context.getBean("certificationBodyDAO"));
        this.setEditionDAO((CertificationEditionDAO) context.getBean("certificationEditionDAO"));
        this.setMailUtils((SendMailUtil) context.getBean("SendMailUtil"));
    }

    protected List<CertifiedProductSearchDetails> getAllCertifiedProductSearchDetails() {
        List<CertifiedProductDetailsDTO> allCertifiedProducts = this.getCertifiedProductDAO().findAll();
        List<CertifiedProductSearchDetails> allCertifiedProductDetails = new ArrayList<CertifiedProductSearchDetails>(
                allCertifiedProducts.size());
        for (CertifiedProductDetailsDTO currProduct : allCertifiedProducts) {
            try {
                CertifiedProductSearchDetails product = this.getCpdManager()
                        .getCertifiedProductDetails(currProduct.getId());
                allCertifiedProductDetails.add(product);
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not find certified product details for certified product with id = "
                        + currProduct.getId());
            }
        }
        return allCertifiedProductDetails;
    }

    public CertifiedProductDetailsManager getCpdManager() {
        return cpdManager;
    }

    public void setCpdManager(final CertifiedProductDetailsManager cpdManager) {
        this.cpdManager = cpdManager;
    }

    public CertifiedProductDAO getCertifiedProductDAO() {
        return certifiedProductDAO;
    }

    public void setCertifiedProductDAO(final CertifiedProductDAO certifiedProductDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
    }

    public SendMailUtil getMailUtils() {
        return mailUtils;
    }

    public void setMailUtils(final SendMailUtil mailUtils) {
        this.mailUtils = mailUtils;
    }

    public NotificationDAO getNotificationDAO() {
        return notificationDAO;
    }

    public void setNotificationDAO(final NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public CertificationBodyDAO getCertificationBodyDAO() {
        return certificationBodyDAO;
    }

    public void setCertificationBodyDAO(final CertificationBodyDAO certificationBodyDAO) {
        this.certificationBodyDAO = certificationBodyDAO;
    }

    public CertificationEditionDAO getEditionDAO() {
        return editionDAO;
    }

    public void setEditionDAO(final CertificationEditionDAO editionDAO) {
        this.editionDAO = editionDAO;
    }
}
