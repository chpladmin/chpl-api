package gov.healthit.chpl.upload.listing.handler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("listingDetailsUploadHandler")
public class ListingDetailsUploadHandler {
    private static final int SECONDS_TO_MILLISECONDS = 1000;

    private CertificationEditionHandler editionHandler;
    private CertificationDateHandler certDateHandler;
    private DeveloperDetailsUploadHandler devDetailsUploadHandler;
    private TargetedUsersUploadHandler targetedUserUploadHandler;
    private AccessibilityStandardsUploadHandler accessibilityStandardsHandler;
    private QmsUploadHandler qmsHandler;
    private IcsUploadHandler icsHandler;
    private CqmUploadHandler cqmHandler;
    private MeasuresUploadHandler measuresUploadHandler;
    private SedUploadHandler sedUploadHandler;
    private CertificationResultUploadHandler certResultHandler;
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingDetailsUploadHandler(CertificationEditionHandler editionHandler,
            CertificationDateHandler certDateHandler,
            DeveloperDetailsUploadHandler devDetailsUploadHandler,
            TargetedUsersUploadHandler targetedUserUploadHandler,
            AccessibilityStandardsUploadHandler accessibilityStandardsHandler,
            QmsUploadHandler qmsHandler, IcsUploadHandler icsHandler,
            CqmUploadHandler cqmHandler, MeasuresUploadHandler measuresUploadHandler,
            SedUploadHandler sedUploadHandler, CertificationResultUploadHandler certResultHandler,
            ListingUploadHandlerUtil uploadUtil) {
        this.editionHandler = editionHandler;
        this.certDateHandler = certDateHandler;
        this.devDetailsUploadHandler = devDetailsUploadHandler;
        this.targetedUserUploadHandler = targetedUserUploadHandler;
        this.accessibilityStandardsHandler = accessibilityStandardsHandler;
        this.qmsHandler = qmsHandler;
        this.icsHandler = icsHandler;
        this.cqmHandler = cqmHandler;
        this.measuresUploadHandler = measuresUploadHandler;
        this.sedUploadHandler = sedUploadHandler;
        this.certResultHandler = certResultHandler;
        this.uploadUtil = uploadUtil;
    }

    public CertifiedProductSearchDetails parseAsListing(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(parseChplId(headingRecord, listingRecords))
                .certifyingBody(parseAcb(headingRecord, listingRecords))
                .testingLabs(parseAtls(headingRecord, listingRecords))
                .acbCertificationId(uploadUtil.parseSingleRowField(
                        Headings.ACB_CERTIFICATION_ID, headingRecord, listingRecords))
                .accessibilityCertified(parseAccessibilityCertified(headingRecord, listingRecords))
                .accessibilityCertifiedStr(parseAccessibilityCertifiedStr(headingRecord, listingRecords))
                .certificationDate(parseCertificationDateMillis(headingRecord, listingRecords))
                .certificationDateStr(parseCertificationDateStr(headingRecord, listingRecords))
                .developer(devDetailsUploadHandler.handle(headingRecord, listingRecords))
                .product(parseProduct(headingRecord, listingRecords))
                .version(parseVersion(headingRecord, listingRecords))
                .certificationEdition(editionHandler.handle(headingRecord, listingRecords))
                .mandatoryDisclosures(parseMandatoryDisclosures(headingRecord, listingRecords))
                .targetedUsers(targetedUserUploadHandler.handle(headingRecord, listingRecords))
                .accessibilityStandards(accessibilityStandardsHandler.handle(headingRecord, listingRecords))
                .qmsStandards(qmsHandler.handle(headingRecord, listingRecords))
                .ics(icsHandler.handle(headingRecord, listingRecords))
                .svapNoticeUrl(parseSvapNoticeUrl(headingRecord, listingRecords))
                .cqmResults(cqmHandler.handle(headingRecord, listingRecords))
                .measures(measuresUploadHandler.parseAsMeasures(headingRecord, listingRecords))
                .sedReportFileLocation(parseSedReportLocationUrl(headingRecord, listingRecords))
                .sedIntendedUserDescription(parseSedIntendedUserDescription(headingRecord, listingRecords))
                .sedTestingEndDay(parseSedTestingDay(headingRecord, listingRecords))
                .sedTestingEndDateStr(parseSedTestingDayStr(headingRecord, listingRecords))
                .sed(sedUploadHandler.parseAsSed(headingRecord, listingRecords))
            .build();

        //add cert result data
        List<CertificationResult> certResultList = new ArrayList<CertificationResult>();
        int prevCertResultIndex = -1;
        int nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(0, headingRecord);
        while (nextCertResultIndex >= 0 && prevCertResultIndex != nextCertResultIndex) {
            List<CSVRecord> parsedCertResultRecords = uploadUtil.getCertificationResultRecordsFromIndex(
                    nextCertResultIndex, headingRecord, listingRecords);
            CSVRecord certHeadingRecord = uploadUtil.getHeadingRecord(parsedCertResultRecords);

            CertificationResult certResult = certResultHandler.parseAsCertificationResult(certHeadingRecord,
                    parsedCertResultRecords.subList(1, parsedCertResultRecords.size()));
            certResultList.add(certResult);

            prevCertResultIndex = nextCertResultIndex;
            nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(
                    nextCertResultIndex + certHeadingRecord.size(), headingRecord);
        }
        listing.setCertificationResults(certResultList);
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
        Boolean accessibilityCertified = null;
        try {
            accessibilityCertified = uploadUtil.parseSingleRowFieldAsBoolean(
                    Headings.ACCESSIBILITY_CERTIFIED, headingRecord, listingRecords);
        } catch (Exception ex) {
        }
        return accessibilityCertified;
    }

    private String parseAccessibilityCertifiedStr(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Headings.ACCESSIBILITY_CERTIFIED, headingRecord, listingRecords);
    }

    private Long parseCertificationDateMillis(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        LocalDate certificationLocalDate = null;
        try {
            certificationLocalDate = certDateHandler.handle(headingRecord, listingRecords);
        } catch (Exception ex) {
        }

        Long certificationDateMilliseconds = null;
        if (certificationLocalDate != null) {
            ZoneId zoneId = ZoneId.systemDefault();
            certificationDateMilliseconds = certificationLocalDate.atStartOfDay(zoneId).toEpochSecond() * SECONDS_TO_MILLISECONDS;
        }
        return certificationDateMilliseconds;
    }

    private String parseCertificationDateStr(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(
                Headings.CERTIFICATION_DATE, headingRecord, listingRecords);
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

    private Map<String, Object> parseAcb(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String acbName = uploadUtil.parseSingleRowField(Headings.CERTIFICATION_BODY_NAME, headingRecord, listingRecords);
        if (acbName == null) {
            return null;
        }

        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, acbName);
        acb.put(CertifiedProductSearchDetails.ACB_CODE_KEY, null);
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

    private String parseMandatoryDisclosures(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Headings.K_1_URL, headingRecord, listingRecords);
    }

    private String parseSvapNoticeUrl(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Headings.SVAP_NOTICE_URL, headingRecord, listingRecords);
    }

    private String parseSedReportLocationUrl(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Headings.SED_REPORT_URL, headingRecord, listingRecords);
    }

    private String parseSedIntendedUserDescription(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Headings.SED_INTENDED_USERS, headingRecord, listingRecords);
    }

    private LocalDate parseSedTestingDay(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        LocalDate sedTestingDate = null;
        try {
            sedTestingDate = uploadUtil.parseSingleRowFieldAsLocalDate(
                    Headings.SED_TESTING_DATE, headingRecord, listingRecords);
        } catch (Exception ex) {
        }
        return sedTestingDate;
    }

    private String parseSedTestingDayStr(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleRowField(Headings.SED_TESTING_DATE, headingRecord, listingRecords);
    }
}
