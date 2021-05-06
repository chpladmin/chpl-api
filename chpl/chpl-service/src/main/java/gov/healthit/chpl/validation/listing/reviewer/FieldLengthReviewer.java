package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("fieldLengthReviewer")
public class FieldLengthReviewer implements Reviewer {
    private static final String MAX_LENGTH_PROPERTY_PREFIX = "maxLength.";
    private static final String MAX_LENGTH_PROPERTY_SUFFIX = ".maxlength";

    private ErrorMessageUtil msgUtil;
    private MessageSource messageSource;

    @Autowired
    public FieldLengthReviewer(ErrorMessageUtil msgUtil, MessageSource messageSource) {
        this.msgUtil = msgUtil;
        this.messageSource = messageSource;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY) != null) {
            checkFieldLength(listing, MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY),
                    "certificationEdition");
        }
        if (listing.getDeveloper() != null) {
            if (!StringUtils.isEmpty(listing.getDeveloper().getName())) {
                checkFieldLength(listing, listing.getDeveloper().getName(), "developerName");
            }
            if (listing.getDeveloper().getAddress() != null && !StringUtils.isEmpty(listing.getDeveloper().getAddress().getLine1())) {
                checkFieldLength(listing, listing.getDeveloper().getAddress().getLine1(), "developerStreetAddress");
            }
            if (listing.getDeveloper().getAddress() != null && !StringUtils.isEmpty(listing.getDeveloper().getAddress().getLine2())) {
                checkFieldLength(listing, listing.getDeveloper().getAddress().getLine2(), "developerStreetAddress");
            }
            if (listing.getDeveloper().getAddress() != null && !StringUtils.isEmpty(listing.getDeveloper().getAddress().getCity())) {
                checkFieldLength(listing, listing.getDeveloper().getAddress().getCity(), "developerCity");
            }
            if (listing.getDeveloper().getAddress() != null && !StringUtils.isEmpty(listing.getDeveloper().getAddress().getState())) {
                checkFieldLength(listing, listing.getDeveloper().getAddress().getState(), "developerState");
            }
            if (listing.getDeveloper().getAddress() != null && !StringUtils.isEmpty(listing.getDeveloper().getAddress().getZipcode())) {
                checkFieldLength(listing, listing.getDeveloper().getAddress().getZipcode(), "developerZip");
            }
            if (!StringUtils.isEmpty(listing.getDeveloper().getWebsite())) {
                checkFieldLength(listing, listing.getDeveloper().getWebsite(), "developerWebsite");
            }
            if (listing.getDeveloper().getContact() != null && !StringUtils.isEmpty(listing.getDeveloper().getContact().getFullName())) {
                checkFieldLength(listing, listing.getDeveloper().getContact().getFullName(), "developerContactName");
            }
            if (listing.getDeveloper().getContact() != null && !StringUtils.isEmpty(listing.getDeveloper().getContact().getEmail())) {
                checkFieldLength(listing, listing.getDeveloper().getContact().getEmail(), "developerEmail");
            }
            if (listing.getDeveloper().getContact() != null && !StringUtils.isEmpty(listing.getDeveloper().getContact().getPhoneNumber())) {
                checkFieldLength(listing, listing.getDeveloper().getContact().getPhoneNumber(), "developerPhone");
            }
        }
        if (listing.getProduct() != null && !StringUtils.isEmpty(listing.getProduct().getName())) {
            checkFieldLength(listing, listing.getProduct().getName(), "productName");
        }
        if (listing.getVersion() != null && !StringUtils.isEmpty(listing.getVersion().getVersion())) {
            checkFieldLength(listing, listing.getVersion().getVersion(), "productVersion");
        }
        checkCriteria(listing);
    }

    private void checkCriteria(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
        .forEach(criteria -> {
            checkFieldLength(listing, criteria.getApiDocumentation(), "apiDocumentationLink");
            checkFieldLength(listing, criteria.getExportDocumentation(), "exportDocumentationLink");
            checkFieldLength(listing, criteria.getDocumentationUrl(), "documentationUrlLink");
            checkFieldLength(listing, criteria.getUseCases(), "useCasesLink");
            checkFieldLength(listing, criteria.getServiceBaseUrlList(), "serviceBaseUrlListLink");
        });
    }

    private void checkFieldLength(CertifiedProductSearchDetails product, String field, String errorField) {
        int maxAllowedFieldLength = getMaxLength(MAX_LENGTH_PROPERTY_PREFIX + errorField);
        if (field != null && field.length() > maxAllowedFieldLength) {
            product.getErrorMessages().add(
                    msgUtil.getMessage("listing." + errorField + MAX_LENGTH_PROPERTY_SUFFIX, maxAllowedFieldLength, field));
        }
    }

    private int getMaxLength(String field) {
        return Integer.parseInt(String.format(messageSource.getMessage(field, null, LocaleContextHolder.getLocale())));
    }
}
