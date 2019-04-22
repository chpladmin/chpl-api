package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("fieldLengthReviewer")
public class FieldLengthReviewer implements Reviewer {
    @Autowired
    private ErrorMessageUtil msgUtil;
    @Autowired
    private MessageSource messageSource;

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationEdition() != null
                && listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY) != null) {
            checkField(listing, listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY),
                    "certificationEdition");
        }
        if (listing.getProduct() != null && !StringUtils.isEmpty(listing.getProduct().getName())) {
            checkField(listing, listing.getProduct().getName(), "productName");
        }
        if (listing.getVersion() != null && !StringUtils.isEmpty(listing.getVersion().getVersion())) {
            checkField(listing, listing.getVersion().getVersion(), "productVersion");
        }
    }

    private void checkField(final CertifiedProductSearchDetails product, final Object field, final String errorField) {
        if (field instanceof Long) {
            Long fieldCasted = (Long) field;
            if (fieldCasted.toString().length() > getMaxLength("maxLength." + errorField)) {
                product.getErrorMessages().add(msgUtil.getMessage("listing." + errorField + ".maxlength",
                        String.valueOf(getMaxLength("maxLength." + errorField)), fieldCasted));
            }
        } else if (field instanceof String) {
            String fieldCasted = (String) field;
            if (fieldCasted.length() > getMaxLength("maxLength." + errorField)) {
                product.getErrorMessages().add(msgUtil.getMessage("listing." + errorField + ".maxlength",
                        String.valueOf(getMaxLength("maxLength." + errorField)), fieldCasted));
            }
        }
    }

    private int getMaxLength(final String field) {
        return Integer.parseInt(String.format(
                messageSource.getMessage(new DefaultMessageSourceResolvable(field), LocaleContextHolder.getLocale())));
    }
}
