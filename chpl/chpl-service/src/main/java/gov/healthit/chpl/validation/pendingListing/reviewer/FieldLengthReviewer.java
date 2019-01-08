package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("pendingFieldLengthReviewer")
public class FieldLengthReviewer implements Reviewer {
    @Autowired
    private ErrorMessageUtil msgUtil;
    @Autowired
    private MessageSource messageSource;

    private static String ERROR = "error";
    private static String WARNING = "warning";

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        checkField(listing, listing.getCertificationEditionId(), "certificationEdition", ERROR);
        checkField(listing, listing.getAcbCertificationId(), "acbCertificationId", ERROR);
        checkField(listing, listing.getCertificationBodyId(), "certifyingAcb", ERROR);
        for (PendingCertifiedProductTargetedUserDTO tu : listing.getTargetedUsers()) {
            checkField(listing, tu.getName(), "targetedUser", WARNING);
        }
        checkField(listing, listing.getUniqueId(), "uniqueCHPLId", ERROR);
        checkField(listing, listing.getDeveloperName(), "developerName", ERROR); // TODO: change vendor to developer
        checkField(listing, listing.getProductName(), "productName", ERROR);
        checkField(listing, listing.getProductVersion(), "productVersion", ERROR);
        if (listing.getDeveloperAddress() != null) {
            checkField(listing, listing.getDeveloperAddress().getStreetLineOne(), "vendorStreetAddress", ERROR);
            checkField(listing, listing.getDeveloperAddress().getStreetLineTwo(), "vendorStreetAddressTwo", ERROR);
            checkField(listing, listing.getDeveloperAddress().getCity(), "vendorCity", ERROR);
            checkField(listing, listing.getDeveloperAddress().getState(), "vendorState", ERROR);
            checkField(listing, listing.getDeveloperAddress().getZipcode(), "vendorZip", ERROR);
        } else {
            checkField(listing, listing.getDeveloperStreetAddress(), "vendorStreetAddress", ERROR);
            checkField(listing, listing.getDeveloperCity(), "vendorCity", ERROR);
            checkField(listing, listing.getDeveloperState(), "vendorState", ERROR);
            checkField(listing, listing.getDeveloperZipCode(), "vendorZip", ERROR);
        }
        checkField(listing, listing.getDeveloperWebsite(), "vendorWebsite", ERROR);
        checkField(listing, listing.getDeveloperEmail(), "vendorEmail", ERROR);
        checkField(listing, listing.getDeveloperPhoneNumber(), "vendorPhone", ERROR);
        checkField(listing, listing.getDeveloperContactName(), "vendorContactName", ERROR);

    }

    private void checkField(final PendingCertifiedProductDTO listing, final Object field, final String errorField,
            String type) {
        if (type.equals(ERROR)) {
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
        } else {
            if (field instanceof Long) {
                Long fieldCasted = (Long) field;
                if (fieldCasted.toString().length() > getMaxLength("maxLength." + errorField)) {
                    PendingCertifiedProductDTO productCasted = (PendingCertifiedProductDTO) listing;
                    productCasted.getWarningMessages().add(msgUtil.getMessage("listing." + errorField + ".maxlength"));
                }
            } else if (field instanceof String) {
                String fieldCasted = (String) field;
                if (fieldCasted.length() > getMaxLength("maxLength." + errorField)) {
                    PendingCertifiedProductDTO productCasted = (PendingCertifiedProductDTO) listing;
                    productCasted.getWarningMessages().add(msgUtil.getMessage("listing." + errorField + ".maxlength"));
                }
            }
        }
    }

    private int getMaxLength(final String field) {
        return Integer.parseInt(String.format(
                messageSource.getMessage(new DefaultMessageSourceResolvable(field), LocaleContextHolder.getLocale())));
    }
}
