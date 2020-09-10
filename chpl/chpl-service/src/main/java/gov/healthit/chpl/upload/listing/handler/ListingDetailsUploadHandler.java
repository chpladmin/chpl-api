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
    private ListingUploadHandlerUtil uploadUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ListingDetailsUploadHandler(DeveloperDetailsUploadHandler devDetailsUploadHandler,
            TargetedUsersUploadHandler targetedUserUploadHandler,
            AccessibilityStandardsUploadHandler accessibilityStandardsHandler,
            ListingUploadHandlerUtil uploadUtil, ErrorMessageUtil msgUtil) {
        this.targetedUserUploadHandler = targetedUserUploadHandler;
        this.devDetailsUploadHandler = devDetailsUploadHandler;
        this.accessibilityStandardsHandler = accessibilityStandardsHandler;
        this.uploadUtil = uploadUtil;
        this.msgUtil = msgUtil;
    }

    public CertifiedProductSearchDetails parseAsListing(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        String chplId = parseChplId(headingRecord, listingRecords);
        Boolean accessibilityCertified = parseAccessibilityCertified(headingRecord, listingRecords);
        Date certificationDate = parseCertificationDate(headingRecord, listingRecords);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(chplId)
                .certifyingBody(parseAcb(headingRecord, listingRecords))
                .testingLabs(parseAtls(headingRecord, listingRecords))
                .acbCertificationId(uploadUtil.parseSingleValueField(
                        Headings.ACB_CERTIFICATION_ID, headingRecord, listingRecords))
                .accessibilityCertified(accessibilityCertified)
                .certificationDate(certificationDate != null ? certificationDate.getTime() : null)
                .developer(devDetailsUploadHandler.handle(headingRecord, listingRecords))
                .product(parseProduct(headingRecord, listingRecords))
                .version(parseVersion(headingRecord, listingRecords))
                .certificationEdition(parseEdition(headingRecord, listingRecords))
                .targetedUsers(targetedUserUploadHandler.handle(headingRecord, listingRecords))
                .accessibilityStandards(accessibilityStandardsHandler.handle(headingRecord, listingRecords))
            .build();

        //TODO: fill in product and version IDs?
        //would have to be done here after building as they depend on the developer
        return listing;
    }

    private String parseChplId(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String chplId = uploadUtil.parseRequiredSingleValueField(
                Headings.UNIQUE_ID, headingRecord, listingRecords);
        return chplId;
    }

    private Boolean parseAccessibilityCertified(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Boolean accessibilityCertified = uploadUtil.parseSingleValueFieldAsBoolean(
                Headings.ACCESSIBILITY_CERTIFIED, headingRecord, listingRecords);
        return accessibilityCertified;
    }

    private Date parseCertificationDate(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Date certificationDate = uploadUtil.parseSingleValueFieldAsDate(
                Headings.CERTIFICATION_DATE, headingRecord, listingRecords);
        return certificationDate;
    }

    private Product parseProduct(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Product product = Product.builder()
                .name(uploadUtil.parseSingleValueField(Headings.PRODUCT, headingRecord, listingRecords))
                .build();
        return product;
    }

    private ProductVersion parseVersion(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        ProductVersion version = ProductVersion.builder()
                .version(uploadUtil.parseSingleValueField(Headings.VERSION, headingRecord, listingRecords))
                .build();
        return version;
    }

    private Map<String, Object> parseEdition(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
       Map<String, Object> edition = new HashMap<String, Object>();
       edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY,
               uploadUtil.parseSingleValueField(Headings.EDITION, headingRecord, listingRecords));
       //TODO: lookup ID?
       return edition;
    }

    private Map<String, Object> parseAcb(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.ACB_NAME_KEY,
                uploadUtil.parseSingleValueField(Headings.CERTIFICATION_BODY_NAME, headingRecord, listingRecords));
        //TODO: lookup ID?
        return edition;
    }

    private List<CertifiedProductTestingLab> parseAtls(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        List<String> atlNames = uploadUtil.parseMultiValueField(Headings.TESTING_LAB_NAME, headingRecord, listingRecords);
        atlNames.stream().forEach(atlName -> {
            CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                    .testingLabName(atlName)
                    .build();
            //TODO: lookup ID?
            atls.add(atl);
        });
        return atls;
    }
}
