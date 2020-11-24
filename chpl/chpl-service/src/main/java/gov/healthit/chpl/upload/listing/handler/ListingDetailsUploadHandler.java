package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("listingDetailsUploadHandler")
public class ListingDetailsUploadHandler {
    private DeveloperDetailsUploadHandler devDetailsUploadHandler;
    private TargetedUsersUploadHandler targetedUserUploadHandler;
    private AccessibilityStandardsUploadHandler accessibilityStandardsHandler;
    private QmsUploadHandler qmsHandler;
    private IcsUploadHandler icsHandler;
    private CqmUploadHandler cqmHandler;
    private SedUploadHandler sedUploadHandler;
    private CertificationResultUploadHandler certResultHandler;
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingDetailsUploadHandler(DeveloperDetailsUploadHandler devDetailsUploadHandler,
            TargetedUsersUploadHandler targetedUserUploadHandler,
            AccessibilityStandardsUploadHandler accessibilityStandardsHandler,
            QmsUploadHandler qmsHandler, IcsUploadHandler icsHandler,
            CqmUploadHandler cqmHandler, SedUploadHandler sedUploadHandler,
            CertificationResultUploadHandler certResultHandler,
            ListingUploadHandlerUtil uploadUtil) {
        this.devDetailsUploadHandler = devDetailsUploadHandler;
        this.targetedUserUploadHandler = targetedUserUploadHandler;
        this.accessibilityStandardsHandler = accessibilityStandardsHandler;
        this.qmsHandler = qmsHandler;
        this.icsHandler = icsHandler;
        this.cqmHandler = cqmHandler;
        this.sedUploadHandler = sedUploadHandler;
        this.certResultHandler = certResultHandler;
        this.uploadUtil = uploadUtil;
    }

    public CertifiedProductSearchDetails parseAsListing(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        Date certificationDate = parseCertificationDate(headingRecord, listingRecords);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(parseChplId(headingRecord, listingRecords))
                .certifyingBody(parseAcb(headingRecord, listingRecords))
                .testingLabs(parseAtls(headingRecord, listingRecords))
                .acbCertificationId(uploadUtil.parseSingleRowField(
                        Headings.ACB_CERTIFICATION_ID, headingRecord, listingRecords))
                .accessibilityCertified(parseAccessibilityCertified(headingRecord, listingRecords))
                .certificationDate(certificationDate != null ? certificationDate.getTime() : null)
                .developer(devDetailsUploadHandler.handle(headingRecord, listingRecords))
                .product(parseProduct(headingRecord, listingRecords))
                .version(parseVersion(headingRecord, listingRecords))
                .certificationEdition(parseEdition(headingRecord, listingRecords))
                .transparencyAttestationUrl(parseTransparencyAttestationUrl(headingRecord, listingRecords))
                .targetedUsers(targetedUserUploadHandler.handle(headingRecord, listingRecords))
                .accessibilityStandards(accessibilityStandardsHandler.handle(headingRecord, listingRecords))
                .qmsStandards(qmsHandler.handle(headingRecord, listingRecords))
                .ics(icsHandler.handle(headingRecord, listingRecords))
                .cqmResults(cqmHandler.handle(headingRecord, listingRecords))
                .sedReportFileLocation(parseSedReportLocationUrl(headingRecord, listingRecords))
                .sedIntendedUserDescription(parseSedIntendedUserDescription(headingRecord, listingRecords))
                .sedTestingEndDate(parseSedTestingDate(headingRecord, listingRecords))
                .sed(sedUploadHandler.parseAsSed(headingRecord, listingRecords))
            .build();

        //add cert result data
        List<CertificationResult> certResultList = new ArrayList<CertificationResult>();
        int nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(0, headingRecord);
        while (nextCertResultIndex >= 0) {
            List<CSVRecord> parsedCertResultRecords = uploadUtil.getCertificationResultRecordsFromIndex(
                    nextCertResultIndex, headingRecord, listingRecords);
            CSVRecord certHeadingRecord = uploadUtil.getHeadingRecord(parsedCertResultRecords);

            CertificationResult certResult = certResultHandler.parseAsCertificationResult(certHeadingRecord,
                    parsedCertResultRecords.subList(1, parsedCertResultRecords.size()));
            certResult.setSed(sedExists(listing.getSed(), certResult.getCriterion()));
            certResultList.add(certResult);

            nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(
                    nextCertResultIndex + parsedCertResultRecords.size() - 1, headingRecord);
        }
        listing.setCertificationResults(certResultList);

        //TODO: data normalizer - look up IDs for everywhere that could have one
        //add criteria that weren't in the file
        return listing;
    }

    private String parseChplId(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String chplId = null;
        try {
            chplId = uploadUtil.parseRequiredSingleRowField(
                Headings.UNIQUE_ID, headingRecord, listingRecords);
        } catch (ValidationException ex) { }
        return chplId;
    }

    private Boolean parseAccessibilityCertified(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Boolean accessibilityCertified = uploadUtil.parseSingleRowFieldAsBoolean(
                Headings.ACCESSIBILITY_CERTIFIED, headingRecord, listingRecords);
        return accessibilityCertified;
    }

    private Date parseCertificationDate(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Date certificationDate = uploadUtil.parseSingleRowFieldAsDate(
                Headings.CERTIFICATION_DATE, headingRecord, listingRecords);
        return certificationDate;
    }

    private Product parseProduct(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String productName = uploadUtil.parseSingleRowField(Headings.PRODUCT, headingRecord, listingRecords);
        if (productName == null) {
            return null;
        }
        Product product = Product.builder()
                .name(productName)
                .build();
        return product;
    }

    private ProductVersion parseVersion(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String versionName = uploadUtil.parseSingleRowField(Headings.VERSION, headingRecord, listingRecords);
        if (versionName == null) {
            return null;
        }

        ProductVersion version = ProductVersion.builder()
                .version(versionName)
                .build();
        return version;
    }

    private Map<String, Object> parseEdition(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String year = uploadUtil.parseSingleRowField(Headings.EDITION, headingRecord, listingRecords);
        if (year == null) {
            return null;
        }
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, year);
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, null);
        return edition;
    }

    private Map<String, Object> parseAcb(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String acbName = uploadUtil.parseSingleRowField(Headings.CERTIFICATION_BODY_NAME, headingRecord, listingRecords);
        if (acbName == null) {
            return null;
        }

        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, acbName);
        acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, null);
        return acb;
    }

    private List<CertifiedProductTestingLab> parseAtls(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        List<String> atlNames = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                Headings.TESTING_LAB_NAME, headingRecord, listingRecords);
        if (atlNames != null && atlNames.size() > 0) {
            atlNames.stream().forEach(atlName -> {
                CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                        .testingLabName(atlName)
                        .build();
                atls.add(atl);
            });
        }
        return atls;
    }

    private String parseTransparencyAttestationUrl(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Headings.K_1_URL, headingRecord, listingRecords);
    }

    private String parseSedReportLocationUrl(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Headings.SED_REPORT_URL, headingRecord, listingRecords);
    }

    private String parseSedIntendedUserDescription(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Headings.SED_INTENDED_USERS, headingRecord, listingRecords);
    }

    private Date parseSedTestingDate(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Date sedTestingDate = uploadUtil.parseSingleRowFieldAsDate(
                Headings.SED_TESTING_DATE, headingRecord, listingRecords);
        return sedTestingDate;
    }

    private boolean sedExists(CertifiedProductSed sed, CertificationCriterion criterion) {
        if (sed == null || (sed.getUcdProcesses() == null && sed.getTestTasks() == null)
                || (sed.getUcdProcesses().size() == 0 && sed.getTestTasks().size() == 0)) {
            return false;
        }

        Optional<UcdProcess> ucdWithCriterion = null;
        Optional<TestTask> taskWithCriterion = null;
        if (sed.getUcdProcesses() != null) {
            ucdWithCriterion = sed.getUcdProcesses().stream()
                .filter(ucdProcess -> containsCriterion(ucdProcess.getCriteria(), criterion))
                .findAny();
        }
        if (sed.getTestTasks() != null) {
            taskWithCriterion = sed.getTestTasks().stream()
                .filter(testTask -> containsCriterion(testTask.getCriteria(), criterion))
                .findAny();
        }
        return (ucdWithCriterion != null && ucdWithCriterion.isPresent())
                || (taskWithCriterion != null && taskWithCriterion.isPresent());
    }

    private boolean containsCriterion(Set<CertificationCriterion> criteriaList, CertificationCriterion criterion) {
        if (criteriaList == null || criteriaList.size() == 0) {
            return false;
        }
        return criteriaList.stream()
            .filter(criterionFromList -> criterionFromList.getId().equals(criterion.getId()))
            .findAny().isPresent();
    }
}
