package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("pendingFieldLengthReviewer")
public class FieldLengthReviewer implements Reviewer {
    @Autowired
    private ErrorMessageUtil msgUtil;

    private static String ERROR = "error";
    private static String WARNING = "warning";

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        checkField(listing, listing.getCertificationEditionId(), "certificationEdition", ERROR);
        checkField(listing, listing.getAcbCertificationId(), "acbCertificationId", ERROR);
        checkField(listing, listing.getCertificationBodyId(), "certifyingAcb", ERROR);
        ArrayList<PendingCertifiedProductTargetedUserDTO> targetedUserToRemove = new ArrayList<PendingCertifiedProductTargetedUserDTO>();
        for (PendingCertifiedProductTargetedUserDTO tu : listing.getTargetedUsers()) {
            checkField(listing, tu.getName(), "targetedUser", WARNING);
            if (listing.getWarningMessages().contains(msgUtil.getMessage("listing.targetedUser.maxlength",
                    String.valueOf(msgUtil.getMessageAsInteger("maxLength.targetedUser")), tu.getName()))) {
                targetedUserToRemove.add(tu);
            }
        }
        listing.getTargetedUsers().removeAll(targetedUserToRemove);
        checkField(listing, listing.getUniqueId(), "uniqueCHPLId", ERROR);
        checkField(listing, listing.getDeveloperName(), "developerName", ERROR);
        checkField(listing, listing.getProductName(), "productName", ERROR);
        checkField(listing, listing.getProductVersion(), "productVersion", ERROR);
        if (listing.getDeveloperAddress() != null) {
            checkField(listing, listing.getDeveloperAddress().getStreetLineOne(), "developerStreetAddress", ERROR);
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
        ArrayList<PendingCertifiedProductQmsStandardDTO> qmsToRemove = new ArrayList<PendingCertifiedProductQmsStandardDTO>();
        for (PendingCertifiedProductQmsStandardDTO qms : listing.getQmsStandards()) {
            checkField(listing, qms.getName(), "qmsStandard", ERROR);
            if (listing.getErrorMessages().contains(msgUtil.getMessage("listing.qmsStandard.maxlength",
                    String.valueOf(msgUtil.getMessageAsInteger("maxLength.qmsStandard")), qms.getName()))) {
                qmsToRemove.add(qms);
            }
        }
        listing.getQmsStandards().removeAll(qmsToRemove);

    }

    private void checkField(final PendingCertifiedProductDTO listing, final Object field, final String errorField, String type) {
        String message = null;

        if (field instanceof Long) {
            Long fieldCasted = (Long) field;
            if (fieldCasted.toString().length() > msgUtil.getMessageAsInteger("maxLength." + errorField)) {
                message = msgUtil.getMessage("listing." + errorField + ".maxlength",
                        String.valueOf(msgUtil.getMessageAsInteger("maxLength." + errorField)), fieldCasted);
            }
        } else if (field instanceof String) {
            String fieldCasted = (String) field;
            if (fieldCasted.length() > msgUtil.getMessageAsInteger("maxLength." + errorField)) {
                message = msgUtil.getMessage("listing." + errorField + ".maxlength",
                        String.valueOf(msgUtil.getMessageAsInteger("maxLength." + errorField)), fieldCasted);
            }
        }
        if (message != null) {
            if (type.equals(ERROR)) {
                listing.getErrorMessages().add(message);
            } else {
                listing.getWarningMessages().add(message);
            }
        }
    }
}
