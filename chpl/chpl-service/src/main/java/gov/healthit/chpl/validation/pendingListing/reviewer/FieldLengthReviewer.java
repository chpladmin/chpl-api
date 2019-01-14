package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("pendingFieldLengthReviewer")
public class FieldLengthReviewer implements Reviewer {
    @Autowired private ErrorMessageUtil msgUtil;
    @Autowired private MessageSource messageSource;

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        checkField(listing, listing.getCertificationEditionId(), "certificationEdition");
        checkField(listing, listing.getAcbCertificationId(), "acbCertificationId");
        checkField(listing, listing.getCertificationBodyId(), "certifyingAcb");
        checkField(listing, listing.getUniqueId(), "uniqueCHPLId");
        checkField(listing, listing.getDeveloperName(), "developerName");
        checkField(listing, listing.getProductName(), "productName");
        checkField(listing, listing.getProductVersion(), "productVersion");
        if (listing.getDeveloperAddress() != null) {
            checkField(listing, listing.getDeveloperAddress().getStreetLineOne(), "developerStreetAddress");
            checkField(listing, listing.getDeveloperAddress().getStreetLineTwo(), "developerStreetAddressTwo");
            checkField(listing, listing.getDeveloperAddress().getCity(), "developerCity");
            checkField(listing, listing.getDeveloperAddress().getState(), "developerState");
            checkField(listing, listing.getDeveloperAddress().getZipcode(), "developerZip");
        }  else {
            checkField(listing, listing.getDeveloperStreetAddress(), "developerStreetAddress");
            checkField(listing, listing.getDeveloperCity(), "developerCity");
            checkField(listing, listing.getDeveloperState(), "developerState");
            checkField(listing, listing.getDeveloperZipCode(), "developerZip");
        }
        checkField(listing, listing.getDeveloperWebsite(), "developerWebsite");
        checkField(listing, listing.getDeveloperEmail(), "developerEmail");
        checkField(listing, listing.getDeveloperPhoneNumber(), "developerPhone");
        checkField(listing, listing.getDeveloperContactName(), "developerContactName");

    }

    private void checkField(final PendingCertifiedProductDTO listing, final Object field, final String errorField) {
        if (field instanceof Long) {
            Long fieldCasted = (Long) field;
            if (fieldCasted.toString().length() > getMaxLength("maxLength." + errorField)) {
                PendingCertifiedProductDTO productCasted = (PendingCertifiedProductDTO) listing;
                productCasted.getErrorMessages().add(msgUtil.getMessage("listing." + errorField + ".maxlength",
                        String.valueOf(getMaxLength("maxLength." + errorField)), fieldCasted));
            }
        } else if (field instanceof String) {
            String fieldCasted = (String) field;
            if (fieldCasted.length() > getMaxLength("maxLength." + errorField)) {
                PendingCertifiedProductDTO productCasted = (PendingCertifiedProductDTO) listing;
                productCasted.getErrorMessages().add(msgUtil.getMessage("listing." + errorField + ".maxlength",
                        String.valueOf(getMaxLength("maxLength." + errorField)), fieldCasted));
            }
        }
    }

    private int getMaxLength(final String field) {
        return Integer.parseInt(String.format(
                messageSource.getMessage(new DefaultMessageSourceResolvable(field),
                        LocaleContextHolder.getLocale())));
    }
}
