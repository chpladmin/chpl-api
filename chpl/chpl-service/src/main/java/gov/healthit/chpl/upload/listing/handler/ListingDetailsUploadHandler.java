package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
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

    private CertificationEditionDAO editionDao;
    private CertificationBodyDAO acbDao;
    private TestingLabDAO atlDao;
    private ProductDAO productDao;
    private ProductVersionDAO versionDao;
    private ListingUploadHandlerUtil uploadUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingDetailsUploadHandler(DeveloperDetailsUploadHandler devDetailsUploadHandler,
            TargetedUsersUploadHandler targetedUserUploadHandler,
            AccessibilityStandardsUploadHandler accessibilityStandardsHandler,
            QmsUploadHandler qmsHandler,
            CertificationEditionDAO editionDao, CertificationBodyDAO acbDao,
            TestingLabDAO atlDao, ProductDAO productDao, ProductVersionDAO versionDao,
            ListingUploadHandlerUtil uploadUtil, ErrorMessageUtil msgUtil) {
        this.devDetailsUploadHandler = devDetailsUploadHandler;
        this.targetedUserUploadHandler = targetedUserUploadHandler;
        this.accessibilityStandardsHandler = accessibilityStandardsHandler;
        this.qmsHandler = qmsHandler;
        this.editionDao = editionDao;
        this.acbDao = acbDao;
        this.atlDao = atlDao;
        this.productDao = productDao;
        this.versionDao = versionDao;
        this.uploadUtil = uploadUtil;
        this.msgUtil = msgUtil;
    }

    public CertifiedProductSearchDetails parseAsListing(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        Date certificationDate = parseCertificationDate(headingRecord, listingRecords);
        Developer developer = devDetailsUploadHandler.handle(headingRecord, listingRecords);
        Product product = parseProduct(developer, headingRecord, listingRecords);
        ProductVersion version = parseVersion(product, headingRecord, listingRecords);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(parseChplId(headingRecord, listingRecords))
                .certifyingBody(parseAcb(headingRecord, listingRecords))
                .testingLabs(parseAtls(headingRecord, listingRecords))
                .acbCertificationId(uploadUtil.parseSingleValueField(
                        Headings.ACB_CERTIFICATION_ID, headingRecord, listingRecords))
                .accessibilityCertified(parseAccessibilityCertified(headingRecord, listingRecords))
                .certificationDate(certificationDate != null ? certificationDate.getTime() : null)
                .developer(developer)
                .product(product)
                .version(version)
                .certificationEdition(parseEdition(headingRecord, listingRecords))
                .transparencyAttestationUrl(parseTransparencyAttestationUrl(headingRecord, listingRecords))
                .targetedUsers(targetedUserUploadHandler.handle(headingRecord, listingRecords))
                .accessibilityStandards(accessibilityStandardsHandler.handle(headingRecord, listingRecords))
                .qmsStandards(qmsHandler.handle(headingRecord, listingRecords))
                //TODO add ics, cqm, sed, participants, tasks
            .build();

        return listing;
    }

    private String parseChplId(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String chplId = null;
        try {
            chplId = uploadUtil.parseRequiredSingleValueField(
                Headings.UNIQUE_ID, headingRecord, listingRecords);
        } catch (ValidationException ex) { }
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

    private Product parseProduct(Developer developer, CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        if (!uploadUtil.hasHeading(Headings.PRODUCT, headingRecord)) {
            return null;
        }

        Product product = Product.builder()
                .name(uploadUtil.parseSingleValueField(Headings.PRODUCT, headingRecord, listingRecords))
                .build();
        if (ObjectUtils.allNotNull(developer, product)
                && ObjectUtils.allNotNull(developer.getDeveloperId(), product.getName())) {
            //TODO: convert this query to use ProductEntitySimple
            ProductDTO foundProduct = productDao.getByDeveloperAndName(developer.getDeveloperId(), product.getName());
            if (foundProduct != null) {
                product.setProductId(foundProduct.getId());
            }
        }
        return product;
    }

    private ProductVersion parseVersion(Product product, CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        if (!uploadUtil.hasHeading(Headings.VERSION, headingRecord)) {
            return null;
        }

        ProductVersion version = ProductVersion.builder()
                .version(uploadUtil.parseSingleValueField(Headings.VERSION, headingRecord, listingRecords))
                .build();
        if (ObjectUtils.allNotNull(product, version)
                && ObjectUtils.allNotNull(product.getProductId(), version.getVersion())) {
            ProductVersionDTO foundVersion = versionDao.getByProductAndVersion(product.getProductId(), version.getVersion());
            if (foundVersion != null) {
                version.setVersionId(foundVersion.getId());
            }
        }
        return version;
    }

    private Map<String, Object> parseEdition(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        if (!uploadUtil.hasHeading(Headings.EDITION, headingRecord)) {
            return null;
        }

        String year = uploadUtil.parseSingleValueField(Headings.EDITION, headingRecord, listingRecords);
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, year);
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, null);
        if (!StringUtils.isEmpty(year)) {
            CertificationEditionDTO editionDto = editionDao.getByYear(year);
            if (editionDto != null) {
                edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, editionDto.getId());
            }
        }
        return edition;
    }

    private Map<String, Object> parseAcb(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        if (!uploadUtil.hasHeading(Headings.CERTIFICATION_BODY_NAME, headingRecord)) {
            return null;
        }

        String acbName = uploadUtil.parseSingleValueField(Headings.CERTIFICATION_BODY_NAME, headingRecord, listingRecords);
        Map<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, acbName);
        acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, null);
        if (!StringUtils.isEmpty(acbName)) {
            CertificationBodyDTO acbDto = acbDao.getByName(acbName);
            if (acbDto != null) {
                acb.put(CertifiedProductSearchDetails.ACB_ID_KEY, acbDto.getId());
            }
        }
        return acb;
    }

    private List<CertifiedProductTestingLab> parseAtls(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        List<String> atlNames = uploadUtil.parseMultiValueField(Headings.TESTING_LAB_NAME, headingRecord, listingRecords);
        if (atlNames != null && atlNames.size() > 0) {
            atlNames.stream().forEach(atlName -> {
                CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                        .testingLabName(atlName)
                        .build();
                if (!StringUtils.isEmpty(atlName)) {
                    TestingLabDTO atlDto = atlDao.getByName(atlName);
                    if (atlDto != null) {
                        atl.setTestingLabId(atlDto.getId());
                    }
                }
                atls.add(atl);
            });
        }
        return atls;
    }

    private String parseTransparencyAttestationUrl(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        return uploadUtil.parseSingleValueField(Headings.K_1_URL, headingRecord, listingRecords);
    }
}
