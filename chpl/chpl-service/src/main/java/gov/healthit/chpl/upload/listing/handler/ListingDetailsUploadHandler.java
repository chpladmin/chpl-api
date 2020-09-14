package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("listingDetailsUploadHandler")
@Log4j2
public class ListingDetailsUploadHandler {
    private DeveloperDetailsUploadHandler devDetailsUploadHandler;
    private TargetedUsersUploadHandler targetedUserUploadHandler;
    private AccessibilityStandardsUploadHandler accessibilityStandardsHandler;
    private QmsUploadHandler qmsHandler;
    private ListingUploadHandlerUtil uploadUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingDetailsUploadHandler(DeveloperDetailsUploadHandler devDetailsUploadHandler,
            TargetedUsersUploadHandler targetedUserUploadHandler,
            AccessibilityStandardsUploadHandler accessibilityStandardsHandler,
            QmsUploadHandler qmsHandler,
            ListingUploadHandlerUtil uploadUtil, ErrorMessageUtil msgUtil) {
        this.devDetailsUploadHandler = devDetailsUploadHandler;
        this.targetedUserUploadHandler = targetedUserUploadHandler;
        this.accessibilityStandardsHandler = accessibilityStandardsHandler;
        this.qmsHandler = qmsHandler;
        this.uploadUtil = uploadUtil;
        this.msgUtil = msgUtil;
    }

    //I originally had code to look up various IDs in the parsers...
    //I went back and forth on it, and I think we should probably not do it in here
    //and just have a data normalizer that runs before the validators to pick out
    //any pieces of data that could be looked up for IDs. That way the lookups will only run once.

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
                //TODO: tests for ics, cqm
                //TODO parsing for sed, participants, tasks, criteria stuff
                //TODO: data normalizer
            .build();

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
}
