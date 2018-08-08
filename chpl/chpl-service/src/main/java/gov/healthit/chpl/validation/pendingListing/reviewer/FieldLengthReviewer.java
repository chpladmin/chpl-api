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
        checkField(listing, listing.getDeveloperName(), "vendorName");
        checkField(listing, listing.getProductName(), "productName");
        checkField(listing, listing.getProductVersion(), "productVersion");
        if(listing.getDeveloperAddress() != null) {
            checkField(listing, listing.getDeveloperAddress().getStreetLineOne(), "vendorStreetAddress");
            checkField(listing, listing.getDeveloperAddress().getStreetLineTwo(), "vendorStreetAddressTwo");
            checkField(listing, listing.getDeveloperAddress().getCity(), "vendorCity");
            checkField(listing, listing.getDeveloperAddress().getState(), "vendorState");
            checkField(listing, listing.getDeveloperAddress().getZipcode(), "vendorZip");
        }  else {
            checkField(listing, listing.getDeveloperStreetAddress(), "vendorStreetAddress");
            checkField(listing, listing.getDeveloperCity(), "vendorCity");
            checkField(listing, listing.getDeveloperState(), "vendorState");
            checkField(listing, listing.getDeveloperZipCode(), "vendorZip");
        }
        checkField(listing, listing.getDeveloperWebsite(), "vendorWebsite");
        checkField(listing, listing.getDeveloperEmail(), "vendorEmail");
        checkField(listing, listing.getDeveloperPhoneNumber(), "vendorPhone");
        checkField(listing, listing.getDeveloperContactName(), "vendorContactName");

    }
    
    private void checkField(final PendingCertifiedProductDTO listing, final Object field, final String errorField) {
        if (field instanceof Long) {
            Long fieldCasted = (Long) field;
            if (fieldCasted.toString().length() > getMaxLength("maxLength." + errorField)) {
                PendingCertifiedProductDTO productCasted = (PendingCertifiedProductDTO) listing;
                productCasted.getErrorMessages().add(msgUtil.getMessage("listing." + errorField + ".maxlength"));
            }
        } else if (field instanceof String) {
            String fieldCasted = (String) field;
            if (fieldCasted.length() > getMaxLength("maxLength." + errorField)) {
                PendingCertifiedProductDTO productCasted = (PendingCertifiedProductDTO) listing;
                productCasted.getErrorMessages().add(msgUtil.getMessage("listing." + errorField + ".maxlength"));
            }
        }
    }
    
    private int getMaxLength(final String field) {
        return Integer.parseInt(String.format(
                messageSource.getMessage(new DefaultMessageSourceResolvable(field),
                        LocaleContextHolder.getLocale())));
    }
}
