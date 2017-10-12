package gov.healthit.chpl.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.presenter.InvalidInheritanceCsvPresenter;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;

@Component("inheritanceReportWeeklyApp")
public class InheritanceReportWeeklyApp extends NotificationEmailerReportApp {
    private InvalidInheritanceCsvPresenter presenter;

    public InheritanceReportWeeklyApp() {
    }

    public static void main(String[] args) throws Exception {
        InheritanceReportWeeklyApp app = new InheritanceReportWeeklyApp();
        app.setLocalContext();
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        app.initiateSpringBeans(context);
        File downloadFolder = app.getDownloadFolder();

        // Get ACBs for ONC-ACB emails
        List<CertificationBodyDTO> acbs = app.getCertificationBodyDAO().findAll(false);
        Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
        permissions.add(new GrantedPermission("ROLE_ADMIN"));
        List<RecipientWithSubscriptionsDTO> oncRecipientSubscriptions = app.getNotificationDAO()
                .getAllNotificationMappingsForType(permissions, NotificationTypeConcept.ONC_WEEKLY_ICS_FAMILY_ERRORS,
                        null);
        List<RecipientWithSubscriptionsDTO> allAcbRecipientSubscriptions = app.getNotificationDAO()
                .getAllNotificationMappingsForType(permissions,
                        NotificationTypeConcept.ONC_ACB_WEEKLY_ICS_FAMILY_ERRORS, acbs);

        if (oncRecipientSubscriptions.size() > 0 || allAcbRecipientSubscriptions.size() > 0) {
            // Get full set of data to send in ONC email
            List<CertifiedProductSearchDetails> allCertifiedProductDetails = app.getAllCertifiedProductSearchDetails();
            CertifiedProductDownloadResponse allCps = new CertifiedProductDownloadResponse();
            allCps.setListings(allCertifiedProductDetails);
            // Get Certification-specific set of data to send in emails
            Map<CertificationBodyDTO, CertifiedProductDownloadResponse> certificationDownloadMap = app
                    .getCertificationDownloadResponse(allCertifiedProductDetails, acbs);

            // send emails
            app.sendOncWeeklyEmail(oncRecipientSubscriptions, downloadFolder, allCps);
            for (CertificationBodyDTO acb : acbs) {
                List<CertificationBodyDTO> currAcbList = new ArrayList<CertificationBodyDTO>();
                currAcbList.add(acb);
                List<RecipientWithSubscriptionsDTO> acbRecipientSubscriptions = app.getNotificationDAO()
                        .getAllNotificationMappingsForType(permissions,
                                NotificationTypeConcept.ONC_ACB_WEEKLY_ICS_FAMILY_ERRORS, currAcbList);
                app.sendAcbWeeklyEmail(acb, acbRecipientSubscriptions, downloadFolder, certificationDownloadMap);
            }
        }
        context.close();
    }

    @Override
    protected void initiateSpringBeans(AbstractApplicationContext context) throws IOException {
        super.initiateSpringBeans(context);
        this.setPresenter((InvalidInheritanceCsvPresenter) context.getBean("invalidInheritanceCsvPresenter"));
        this.getPresenter().setProps(getProperties());
        this.getPresenter().setMessageSource((MessageSource) context.getBean("messageSource"));
        this.getPresenter().setInheritanceDao((ListingGraphDAO) context.getBean("listingGraphDao"));
    }

    private void sendOncWeeklyEmail(List<RecipientWithSubscriptionsDTO> oncRecipientSubscriptions, File downloadFolder,
            CertifiedProductDownloadResponse cpList) throws IOException, AddressException, MessagingException {
        Properties props = getProperties();

        String reportFilename = props.getProperty("inheritanceReportEmailWeeklyFileName");
        File reportFile = new File(downloadFolder.getAbsolutePath() + File.separator + reportFilename);
        String subject = props.getProperty("inheritanceReportEmailWeeklySubject");
        String htmlMessage = props.getProperty("inheritanceReportEmailWeeklyHtmlMessage");
        int numRows = this.getPresenter().presentAsFile(reportFile, cpList);

        // get emails
        Set<String> oncEmails = new HashSet<String>();
        for (RecipientWithSubscriptionsDTO recip : oncRecipientSubscriptions) {
            oncEmails.add(recip.getEmail());
        }
        String[] bccEmail = oncEmails.toArray(new String[oncEmails.size()]);

        if (bccEmail.length > 0) {
            // TODO: put in the no content html body if empty
            htmlMessage += createHtmlEmailBody(numRows, props.getProperty("oversightEmailWeeklyNoContent"));
            List<File> files = new ArrayList<File>();
            files.add(reportFile);
            this.getMailUtils().sendEmail(null, bccEmail, subject, htmlMessage, files, props);
        }
    }

    private void sendAcbWeeklyEmail(CertificationBodyDTO acb,
            List<RecipientWithSubscriptionsDTO> acbRecipientSubscriptions, File downloadFolder,
            Map<CertificationBodyDTO, CertifiedProductDownloadResponse> acbDownloadMap)
            throws IOException, AddressException, MessagingException {
        Properties props = getProperties();

        // get emails
        Set<String> acbEmails = new HashSet<String>();
        for (RecipientWithSubscriptionsDTO recip : acbRecipientSubscriptions) {
            acbEmails.add(recip.getEmail());
        }

        List<File> files = new ArrayList<File>();
        String fmtAcbName = acb.getName().replaceAll("\\W", "").toLowerCase();
        String reportFilename = fmtAcbName + "-" + props.getProperty("inheritanceReportEmailWeeklyFileName");
        File reportFile = new File(downloadFolder.getAbsolutePath() + File.separator + reportFilename);

        // Generate this ACB's download file
        int numRows = this.getPresenter().presentAsFile(reportFile, acbDownloadMap.get(acb));
        files.add(reportFile);

        String subject = acb.getName() + " " + props.getProperty("inheritanceReportEmailWeeklySubject");
        String htmlMessage = props.getProperty("inheritanceReportEmailWeeklyHtmlMessage");
        String[] bccEmail = acbEmails.toArray(new String[acbEmails.size()]);

        // Get broken rules for email body
        if (bccEmail.length > 0) {
            // TODO: put in the no content html body if empty
            htmlMessage += createHtmlEmailBody(numRows, props.getProperty("oversightEmailWeeklyNoContent"));
            this.getMailUtils().sendEmail(null, bccEmail, subject, htmlMessage, files, props);
        }
    }

    @Override
    public List<CertifiedProductSearchDetails> getAllCertifiedProductSearchDetails() {
        List<CertifiedProductDetailsDTO> allCertifiedProducts = this.getCertifiedProductDAO().findWithInheritance();
        // we only care about 2015 listings for this report
        CertificationEditionDTO edition2015 = this.getEditionDAO().getByYear("2015");
        List<CertifiedProductSearchDetails> allCertifiedProductDetails = new ArrayList<CertifiedProductSearchDetails>(
                allCertifiedProducts.size());
        for (CertifiedProductDetailsDTO currProduct : allCertifiedProducts) {
            if (edition2015.getId().longValue() == currProduct.getCertificationEditionId().longValue()) {
                try {
                    CertifiedProductSearchDetails product = this.getCpdManager()
                            .getCertifiedProductDetails(currProduct.getId());
                    allCertifiedProductDetails.add(product);
                } catch (final EntityRetrievalException ex) {
                    LOGGER.error("Could not find certified product details for certified product with id = "
                            + currProduct.getId());
                }
            }
        }
        return allCertifiedProductDetails;
    }

    protected String createHtmlEmailBody(int numRecords, String noContentMsg) throws IOException {
        String htmlMessage = "";
        if (numRecords == 0) {
            htmlMessage = noContentMsg;
        } else {
            htmlMessage = "<p>" + numRecords + " inheritance error" + (numRecords > 1 ? "s" : "") + " were found.";
        }
        return htmlMessage;
    }

    private InvalidInheritanceCsvPresenter getPresenter() {
        return presenter;
    }

    private void setPresenter(InvalidInheritanceCsvPresenter presenter) {
        this.presenter = presenter;
    }
}
