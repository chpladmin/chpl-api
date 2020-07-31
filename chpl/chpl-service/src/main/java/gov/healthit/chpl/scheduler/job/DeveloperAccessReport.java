package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.auth.UserContactEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.util.EmailBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DeveloperAccessReport extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("developerAccessEmailJobLogger");

    @Autowired
    private DeveloperAccessDAO developerAccessDao;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private Environment env;

    private static final int DEVELOPER_NAME = 0;
    private static final int DEVELOPER_ID = 1;
    private static final int DEVELOPER_CONTACT_NAME = 2;
    private static final int DEVELOPER_CONTACT_EMAIL = 3;
    private static final int DEVELOPER_CONTACT_PHONE_NUMBER = 4;
    private static final int DEVELOPER_USER_COUNT = 5;
    private static final int DEVELOPER_USER_EMAILS = 6;
    private static final int LAST_LOGIN_DATE = 7;
    private static final int ONC_ACB_START = 8;
    private static final int NUM_REPORT_COLS = 8;

    private static final String SPLIT_CHAR = "\u263A";

    public DeveloperAccessReport() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Developer Access job. *********");
        LOGGER.info(
                "Creating developer access report for: " + jobContext.getMergedJobDataMap().getString("email"));

        try {
            List<CertificationBodyDTO> acbs = getAppropriateAcbs(jobContext);
            List<List<String>> developerAccessRows = createDeveloperAccessRows(acbs);
            sendEmail(jobContext, developerAccessRows, acbs);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("********* Completed the Developer Access job. *********");
    }

    private List<CertificationBodyDTO> getAppropriateAcbs(JobExecutionContext jobContext) {
        List<CertificationBodyDTO> acbs = certificationBodyDAO.findAllActive();
        if (jobContext.getMergedJobDataMap().getBooleanValue("acbSpecific")) {
            List<Long> acbsFromJob = getAcbsFromJobContext(jobContext);
            acbs = acbs.stream()
                    .filter(acb -> acbsFromJob.contains(acb.getId()))
                    .collect(Collectors.toList());
        }
        return acbs;
    }

    private List<Long> getAcbsFromJobContext(JobExecutionContext jobContext) {
        return Arrays.asList(jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acbIdAsString -> Long.parseLong(acbIdAsString))
                .collect(Collectors.toList());
    }

    private File getOutputFile(final List<List<String>> rows, final String reportFilename,
            final List<CertificationBodyDTO> activeAcbs) {
        File temp = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(temp),
                    Charset.forName("UTF-8").newEncoder());
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
                writer.write('\ufeff');
                csvPrinter.printRecord(getHeaderRow(activeAcbs));
                for (List<String> rowValue : rows) {
                    csvPrinter.printRecord(rowValue);
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return temp;
    }

    private List<String> getHeaderRow(final List<CertificationBodyDTO> activeAcbs) {
        List<String> row = createEmptyRow(activeAcbs);
        row.set(DEVELOPER_NAME, "Developer Name");
        row.set(DEVELOPER_ID, "Developer ID");
        row.set(DEVELOPER_CONTACT_NAME, "Developer Contact Name");
        row.set(DEVELOPER_CONTACT_EMAIL, "Developer Contact Email");
        row.set(DEVELOPER_CONTACT_PHONE_NUMBER, "Developer Contact Phone Number");
        row.set(DEVELOPER_USER_COUNT, "# Users");
        row.set(DEVELOPER_USER_EMAILS, "User Email Addresses");
        row.set(LAST_LOGIN_DATE, "Last Login Date");
        for (int i = 0; i < activeAcbs.size(); i++) {
            row.set(ONC_ACB_START + i, activeAcbs.get(i).getName());
        }
        return row;
    }

    private List<List<String>> createDeveloperAccessRows(List<CertificationBodyDTO> activeAcbs)
            throws EntityRetrievalException {
        LOGGER.debug("Getting developer access data");
        List<DeveloperAcbMap> developerAcbMaps = getDeveloperAccessFilteredByACBs(activeAcbs);
        LOGGER.debug("Found " + developerAcbMaps.size() + " developers with active listings on appropriate ACBs");

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        for (DeveloperAcbMap developerAcbMap : developerAcbMaps) {
            List<String> currRow = createEmptyRow(activeAcbs);
            putDeveloperAccessActivityInRow(developerAcbMap, currRow, activeAcbs);
            activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }

    private List<DeveloperAcbMap> getDeveloperAccessFilteredByACBs(List<CertificationBodyDTO> acbs) throws EntityRetrievalException {
        List<Long> acbIds = acbs.stream()
                .map(CertificationBodyDTO::getId)
                .collect(Collectors.toList());
        List<CertificationStatusType> activeStatuses = new ArrayList<CertificationStatusType>();
        activeStatuses.add(CertificationStatusType.Active);
        activeStatuses.add(CertificationStatusType.SuspendedByAcb);
        activeStatuses.add(CertificationStatusType.SuspendedByOnc);

        return developerAccessDao.getDevelopersWithListingsInStatusForAcbs(activeStatuses, acbIds);
    }

    private void putDeveloperAccessActivityInRow(DeveloperAcbMap devAcbMap,
            List<String> currRow, List<CertificationBodyDTO> activeAcbs) {
        // Straightforward data
        currRow.set(DEVELOPER_NAME, devAcbMap.getDeveloperName());
        currRow.set(DEVELOPER_ID, devAcbMap.getDeveloperId()+"");
        currRow.set(DEVELOPER_CONTACT_NAME, devAcbMap.getContactName());
        currRow.set(DEVELOPER_CONTACT_EMAIL, devAcbMap.getContactEmail());
        currRow.set(DEVELOPER_CONTACT_PHONE_NUMBER, devAcbMap.getContactPhoneNumber());
        currRow.set(DEVELOPER_USER_COUNT, developerAccessDao.getUserCountForDeveloper(devAcbMap.getDeveloperId())+"");
        List<Contact> userContactList = developerAccessDao.getContactForDeveloperUsers(devAcbMap.developerId);
        currRow.set(DEVELOPER_USER_EMAILS, (userContactList == null) ? "" : formatContacts(userContactList));
        Date lastLoginDate = developerAccessDao.getLastLoginDateForDeveloper(devAcbMap.getDeveloperId());
        currRow.set(LAST_LOGIN_DATE, lastLoginDate == null ? "" : getTimestampFormatter().format(lastLoginDate));

        // Is the CR relevant for each ONC-ACB?
        for (int i = 0; i < activeAcbs.size(); i++) {
            boolean isRelevant = false;
            for (CertificationBodyDTO acb : devAcbMap.getAcbs()) {
                if (activeAcbs.get(i).getId().equals(acb.getId())) {
                    isRelevant = true;
                }
            }
            currRow.set(ONC_ACB_START + i, isRelevant ? "Applicable" : "Not Applicable");
        }
    }

    private List<String> createEmptyRow(List<CertificationBodyDTO> activeAcbs) {
        List<String> row = new ArrayList<String>(NUM_REPORT_COLS);
        for (int i = 0; i < NUM_REPORT_COLS + activeAcbs.size(); i++) {
            row.add("");
        }
        return row;
    }

    private String formatContacts(List<Contact> userContactList) {
        List<String> contactStrings = new ArrayList<String>();
        userContactList.stream()
            .forEach(contact -> contactStrings.add(contact.getFullName() + " <" + contact.getEmail() + ">"));
        return String.join("; ", contactStrings);
    }

    private DateFormat getTimestampFormatter() {
        return DateFormat.getDateTimeInstance(
                DateFormat.LONG,
                DateFormat.LONG,
                Locale.US);
    }

    private void sendEmail(JobExecutionContext jobContext, List<List<String>> csvRows, List<CertificationBodyDTO> acbs)
            throws MessagingException {
        LOGGER.info("Sending email to {} with contents {} and a total of {} developer access rows",
                getEmailRecipients(jobContext).get(0), getHtmlMessage(csvRows.size()), csvRows.size());

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(getEmailRecipients(jobContext))
                .subject(getSubject(jobContext))
                .htmlMessage(getHtmlMessage(csvRows.size()))
                .fileAttachments(getAttachments(csvRows, acbs))
                .sendEmail();
    }

    private String getSubject(JobExecutionContext jobContext) {
        return env.getProperty("developerAccessEmailSubject");
    }

    private List<File> getAttachments(List<List<String>> csvRows, List<CertificationBodyDTO> acbs) {
        List<File> attachments = new ArrayList<File>();
        File csvFile = getCsvFile(csvRows, acbs);
        if (csvFile != null) {
            attachments.add(csvFile);
        }
        return attachments;
    }

    private File getCsvFile(List<List<String>> csvRows, List<CertificationBodyDTO> acbs) {
        File csvFile = null;
        if (csvRows.size() > 0) {
            String filename = env.getProperty("developerAccessReportFilename");
            if (csvRows.size() > 0) {
                csvFile = getOutputFile(csvRows, filename, acbs);
            }
        }
        return csvFile;
    }

    private String getHtmlMessage(Integer rowCount) {
        if (rowCount > 0) {
            return String.format(env.getProperty("developerAccessHasDataEmailBody"), rowCount);
        } else {
            return String.format(env.getProperty("developerAccessNoDataEmailBody"));
        }
    }

    private List<String> getEmailRecipients(JobExecutionContext jobContext) {
        return Arrays.asList(jobContext.getMergedJobDataMap().getString("email"));
    }

    @Component("developerAccessDao")
    private static class DeveloperAccessDAO extends BaseDAOImpl {

        @SuppressWarnings("unused")
        DeveloperAccessDAO() {
            super();
        }

        @Transactional
        public List<DeveloperAcbMap> getDevelopersWithListingsInStatusForAcbs(
                List<CertificationStatusType> listingStatuses, List<Long> acbIds) {
            String hql = "SELECT developerId, developerName, "
                    + "fullName, email, phoneNumber, "
                    + "certificationBodyId, certificationBodyName "
                    + "FROM CertifiedProductDetailsEntity "
                    + "WHERE certificationStatusName IN (:listingStatusNames) "
                    + "AND certificationBodyId IN (:acbIds) "
                    + "AND deleted = false ";
            Query query = entityManager.createQuery(hql);
            List<String> listingStatusNames = listingStatuses.stream()
                    .map(CertificationStatusType::getName)
                    .collect(Collectors.toList());
            query.setParameter("listingStatusNames", listingStatusNames);
            query.setParameter("acbIds", acbIds);

            List<DeveloperAcbMap> results = new ArrayList<DeveloperAcbMap>();
            List<Object[]> queryResults = query.getResultList();
            if (queryResults == null || queryResults.size() == 0) {
                return results;
            }

            for (Object[] queryResult : queryResults) {
                DeveloperAcbMap map = new DeveloperAcbMap();
                map.setDeveloperId(Long.valueOf(queryResult[0].toString()));
                map.setDeveloperName(queryResult[1].toString());
                map.setContactName(queryResult[2].toString());
                map.setContactEmail(queryResult[3].toString());
                map.setContactPhoneNumber(queryResult[4].toString());
                CertificationBodyDTO acb = new CertificationBodyDTO();
                acb.setId(Long.valueOf(queryResult[5].toString()));
                acb.setName(queryResult[6].toString());

                int index = results.indexOf(map);
                if (index >= 0) {
                    results.get(index).getAcbs().add(acb);
                } else {
                    map.getAcbs().add(acb);
                    results.add(map);
                }
            }
            return results;
        }

        @Transactional
        public List<Contact> getContactForDeveloperUsers(Long developerId) {
            List<Contact> contacts = new ArrayList<Contact>();
            Query query = entityManager.createQuery("SELECT contact "
                    + "FROM UserDeveloperMapEntity udm "
                    + "JOIN udm.developer developer "
                    + "JOIN udm.user u "
                    + "JOIN u.contact contact "
                    + "WHERE udm.deleted = false "
                    + "AND developer.deleted = false "
                    + "AND u.deleted = false "
                    + "AND u.accountExpired = false "
                    + "AND u.accountEnabled = true "
                    + "AND contact.deleted = false "
                    + "AND (developer.id = :developerId)", UserContactEntity.class);
            query.setParameter("developerId", developerId);
            List<UserContactEntity> queryResults = query.getResultList();
            if (queryResults == null || queryResults.size() == 0) {
                return contacts;
            }
            for (UserContactEntity queryResult : queryResults) {
                Contact contact = new Contact();
                contact.setEmail(queryResult.getEmail());
                contact.setFullName(queryResult.getFullName());
                contacts.add(contact);
            }
            return contacts;
        }

        @Transactional
        public int getUserCountForDeveloper(Long developerId) {
            int userCount = -1;
            Query query = entityManager.createQuery("SELECT count(*) "
                    + "FROM UserDeveloperMapEntity udm "
                    + "JOIN udm.developer developer "
                    + "JOIN udm.user u "
                    + "WHERE udm.deleted = false "
                    + "AND developer.deleted = false "
                    + "AND u.deleted = false "
                    + "AND u.accountExpired = false "
                    + "AND u.accountEnabled = true "
                    + "AND (developer.id = :developerId)");
            query.setParameter("developerId", developerId);
            Object userCountObj = query.getSingleResult();
            if (userCountObj != null && userCountObj instanceof Long) {
                userCount = ((Long) userCountObj).intValue();
            }
            return userCount;
        }

        @Transactional
        public Date getLastLoginDateForDeveloper(Long developerId) {
            Date lastLoggedIn = null;
            Query query = entityManager.createQuery("SELECT MAX(u.lastLoggedInDate) "
                    + "FROM UserDeveloperMapEntity udm "
                    + "JOIN udm.developer developer "
                    + "JOIN udm.user u "
                    + "WHERE udm.deleted = false "
                    + "AND developer.deleted = false "
                    + "AND u.deleted = false "
                    + "AND (developer.id = :developerId)");
            query.setParameter("developerId", developerId);
            Object lastLoggedInObj = query.getSingleResult();
            if (lastLoggedInObj != null && lastLoggedInObj instanceof Date) {
                lastLoggedIn = (Date) lastLoggedInObj;
            }
            return lastLoggedIn;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class DeveloperAcbMap {
        private Long developerId;
        private String developerName;
        private String contactName;
        private String contactEmail;
        private String contactPhoneNumber;
        private Set<CertificationBodyDTO> acbs = new HashSet<CertificationBodyDTO>();

        @Override
        public boolean equals(Object anotherObj) {
            if (anotherObj == null) {
                return false;
            }
            if (!(anotherObj instanceof DeveloperAcbMap)) {
                return false;
            }
            DeveloperAcbMap anotherMap = (DeveloperAcbMap) anotherObj;
            if (ObjectUtils.allNotNull(this.getDeveloperId(), anotherMap.getDeveloperId())
                    && this.getDeveloperId().equals(anotherMap.getDeveloperId())) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (this.getDeveloperId() == null) {
                return -1;
            }
            return this.getDeveloperId().hashCode();
        }
    }
}
