package gov.healthit.chpl.scheduler.job.onetime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.upload.listing.ListingUploadEntity;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.handler.ListingDetailsUploadHandler;
import gov.healthit.chpl.upload.listing.normalizer.ListingDetailsNormalizer;
import gov.healthit.chpl.upload.listing.validation.ListingUploadValidator;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "updateParticipantsJobLogger")
public class UpdateParticipantsJob implements Job {
    @Autowired
    private ProcessedListingUploadDao processedListingUploadDao;

    @Autowired
    private ProcessedListingUploadManager processedListingUploadManager;

    @Autowired
    private CertificationCriterionService criteriaService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private CertificationCriterion G3 = null;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Update Participants job. *********");
        try {
            G3 = criteriaService.get(Criteria2015.G_3);
            //the bug came to exist when we turned on flexible upload
            Date startDate = formatter.parse("2022-06-01 0:00:00.000");
            //the ticket OCD-4117 that fixes this bug was deployed on 1/9/23
            Date endDate = formatter.parse("2023-01-09 23:59:59.999");
            LOGGER.info("Getting listing uploads between " + startDate + " and " + endDate);
            List<ListingUpload> allListingUploads = processedListingUploadDao.getAllProcessedUploadsBetweenDates(startDate, endDate);
            LOGGER.info("Found " + allListingUploads.size() + " listing uploads between " + startDate + " and " + endDate);

            List<CertifiedProductSearchDetails> allG3Listings = allListingUploads.stream()
                .map(listingUpload -> processedListingUploadManager.getDetailsById(listingUpload.getId()))
                .filter(listingDetails -> listingDetails != null && attestsToG3(listingDetails))
                .toList();

            //TODO for each g3 listing
                //extract test participants
                //make sure there is a test participant in the live listing with the same values
                //maybe we can make sure it's assigned to the same test tasks as well?
                //save the updated test participant values for that listing
        } catch (Exception ex) {
            LOGGER.fatal("Unexpected exception was caught. All listings may not have been processed.", ex);
        }
        LOGGER.info("********* Completed the Update Participants job. *********");
    }

    private boolean attestsToG3(CertifiedProductSearchDetails listing) {
        if (!CollectionUtils.isEmpty(listing.getCertificationResults())) {
            Optional<CertificationResult> g3CertResultOpt = listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess())
                        && certResult.getCriterion() != null
                        && certResult.getCriterion().getId().equals(G3.getId()))
                .findAny();
            if (g3CertResultOpt.isPresent()) {
                LOGGER.info("Uploaded listing " + listing.getChplProductNumber() + " attests to G3");
                return true;
            } else {
                LOGGER.info("Uploaded listing " + listing.getChplProductNumber() + " does not attest to G3");
                return false;
            }
        } else {
            LOGGER.warn("Uploaded listing " + listing.getChplProductNumber() + " has no certification results.");
        }
        return false;
    }

    @Component
    @NoArgsConstructor
    private static class ProcessedListingUploadDao extends BaseDAOImpl {
        private static final String GET_PROCESSED_ENTITY_HQL_BEGIN = "SELECT ul "
                + "FROM ListingUploadEntity ul "
                + "LEFT JOIN FETCH ul.certificationBody acb "
                + "LEFT JOIN FETCH acb.address "
                + "WHERE ul.certifiedProductId IS NOT NULL ";

        @Transactional
        public List<ListingUpload> getAllProcessedUploadsBetweenDates(Date startDate, Date endDate) {
            Query query = entityManager.createQuery(GET_PROCESSED_ENTITY_HQL_BEGIN
                    + "AND ul.creationDate >= :startDate "
                    + "AND ul.creationDate <= :endDate", ListingUploadEntity.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            List<ListingUploadEntity> entities = query.getResultList();
            return entities.stream()
                    .map(entity -> entity.toDomain())
                    .toList();
        }

        public ListingUpload getByIdIncludingRecords(Long id) {
            Query query = entityManager.createQuery(GET_PROCESSED_ENTITY_HQL_BEGIN
                    + "WHERE ul.id = :id ", ListingUploadEntity.class);
            query.setParameter("id", id);
            List<ListingUploadEntity> entities = query.getResultList();
            if (entities == null || entities.size() == 0) {
                LOGGER.error("No processed uploaded listing found with ID " + id);
                return null;
            }
            return entities.get(0).toDomainWithRecords();
        }
    }

    @Component
    @NoArgsConstructor
    private static class ProcessedListingUploadManager {

        private ListingDetailsNormalizer listingNormalizer;
        private ListingUploadHandlerUtil uploadUtil;
        private ProcessedListingUploadDao processedListingUploadDao;
        private ListingUploadValidator listingUploadValidator;
        private ListingDetailsUploadHandler listingDetailsHandler;

        @Autowired
        ProcessedListingUploadManager(ListingDetailsNormalizer listingNormalizer,
                ListingUploadValidator listingUploadValidator,
                ListingDetailsUploadHandler listingDetailsHandler,
                ProcessedListingUploadDao processedListingUploadDao,
                ListingUploadHandlerUtil uploadUtil) {
            this.listingNormalizer = listingNormalizer;
            this.listingUploadValidator = listingUploadValidator;
            this.listingDetailsHandler = listingDetailsHandler;
            this.processedListingUploadDao = processedListingUploadDao;
            this.uploadUtil = uploadUtil;
        }

        @Transactional
        public CertifiedProductSearchDetails getDetailsById(Long id) {
            ListingUpload listingUpload = processedListingUploadDao.getByIdIncludingRecords(id);
            LOGGER.debug("Got listing upload with ID " + id);
            List<CSVRecord> allCsvRecords = listingUpload.getRecords();
            if (allCsvRecords == null) {
                LOGGER.debug("Listing upload with ID " + id + " has no CSV records associated with it.");
                return null;
            }
            LOGGER.debug("Listing upload with ID " + id + " has " + allCsvRecords.size() + " CSV records associated with it.");
            int headingRowIndex = uploadUtil.getHeadingRecordIndex(allCsvRecords);
            CSVRecord headingRecord = uploadUtil.getHeadingRecord(allCsvRecords);
            List<CSVRecord> allListingRecords = allCsvRecords.subList(headingRowIndex + 1, allCsvRecords.size());
            LOGGER.debug("Converting listing upload with ID " + id + " into CertifiedProductSearchDetails object");
            CertifiedProductSearchDetails listing =
                    listingDetailsHandler.parseAsListing(headingRecord, allListingRecords);
            listing.setId(id);
            LOGGER.debug("Converted listing upload with ID " + id + " into CertifiedProductSearchDetails object");
            listingNormalizer.normalize(listing);
            LOGGER.debug("Normalized listing upload with ID " + id);
            listingUploadValidator.review(listingUpload, listing);
            LOGGER.debug("Validated listing upload with ID " + id);
            return listing;
        }
    }
}
