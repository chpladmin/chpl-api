package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("pendingFieldLengthReviewer")
public class FieldLengthReviewer implements Reviewer {
    @Autowired
    private ErrorMessageUtil msgUtil;
    @Autowired
    private MessageSource messageSource;

    private static String ERROR = "error";
    private static String WARNING = "warning";
    private static String UPLOAD = "upload";

    @Override
    public void review(PendingCertifiedProductDTO listing) throws ValidationException {
        checkField(listing, listing.getCertificationEditionId(), "certificationEdition", ERROR);
        checkField(listing, listing.getAcbCertificationId(), "acbCertificationId", ERROR);
        checkField(listing, listing.getCertificationBodyId(), "certifyingAcb", ERROR);
        for (PendingCertificationResultDTO certResult : listing.getCertificationCriterion()) {
            for (PendingCertificationResultTestTaskDTO tt : certResult.getTestTasks()) {
                checkField(listing, tt.getPendingTestTask().getUniqueId(), "taskIdentifier", UPLOAD);
            }
        }
        ArrayList<PendingCertifiedProductTargetedUserDTO> toRemove = new ArrayList<PendingCertifiedProductTargetedUserDTO>();
        for (PendingCertifiedProductTargetedUserDTO tu : listing.getTargetedUsers()) {
            checkField(listing, tu.getName(), "targetedUser", WARNING);
            if (listing.getWarningMessages().contains(msgUtil.getMessage("listing.targetedUser.maxlength",
                    String.valueOf(getMaxLength("maxLength.targetedUser")), tu.getName()))) {
                toRemove.add(tu);
            }
        }
        listing.getTargetedUsers().removeAll(toRemove);
        checkField(listing, listing.getUniqueId(), "uniqueCHPLId", ERROR);
        checkField(listing, listing.getDeveloperName(), "developerName", ERROR);
        checkField(listing, listing.getProductName(), "productName", ERROR);
        checkField(listing, listing.getProductVersion(), "productVersion", ERROR);
        if (listing.getDeveloperAddress() != null) {
            checkField(listing, listing.getDeveloperAddress().getStreetLineOne(), "developerStreetAddress", ERROR);
            checkField(listing, listing.getDeveloperAddress().getStreetLineTwo(), "developerStreetAddressTwo", ERROR);
            checkField(listing, listing.getDeveloperAddress().getCity(), "developerCity", ERROR);
            checkField(listing, listing.getDeveloperAddress().getState(), "developerState", ERROR);
            checkField(listing, listing.getDeveloperAddress().getZipcode(), "developerZip", ERROR);
        } else {
            checkField(listing, listing.getDeveloperStreetAddress(), "developerStreetAddress", ERROR);
            checkField(listing, listing.getDeveloperCity(), "developerCity", ERROR);
            checkField(listing, listing.getDeveloperState(), "developerState", ERROR);
            checkField(listing, listing.getDeveloperZipCode(), "developerZip", ERROR);
        }
        checkField(listing, listing.getDeveloperWebsite(), "developerWebsite", ERROR);
        checkField(listing, listing.getDeveloperEmail(), "developerEmail", ERROR);
        checkField(listing, listing.getDeveloperPhoneNumber(), "developerPhone", ERROR);
        checkField(listing, listing.getDeveloperContactName(), "developerContactName", ERROR);

    }

    private void checkField(final PendingCertifiedProductDTO listing, final Object field, final String errorField, String type) throws ValidationException {
        String message = null;
        
        if (field instanceof Long) {
            Long fieldCasted = (Long) field;
            if (fieldCasted.toString().length() > getMaxLength("maxLength." + errorField)) {
                message = msgUtil.getMessage("listing." + errorField + ".maxlength",
                        String.valueOf(getMaxLength("maxLength." + errorField)), fieldCasted);
            }
        } else if (field instanceof String) {
            String fieldCasted = (String) field;
            if (fieldCasted.length() > getMaxLength("maxLength." + errorField)) {
                message = msgUtil.getMessage("listing." + errorField + ".maxlength",
                        String.valueOf(getMaxLength("maxLength." + errorField)), fieldCasted);
            }
        }
        if (type.equals(ERROR)) {
            listing.getErrorMessages().add(message);
        } else if (type.equals(WARNING)){
            listing.getWarningMessages().add(message);
        } else {
            throw new ValidationException(message);
        }
    }

    private int getMaxLength(final String field) {
        return Integer.parseInt(String.format(
                messageSource.getMessage(new DefaultMessageSourceResolvable(field), LocaleContextHolder.getLocale())));
    }
}
