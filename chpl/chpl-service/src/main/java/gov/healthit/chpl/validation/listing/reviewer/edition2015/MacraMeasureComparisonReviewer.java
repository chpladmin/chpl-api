package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

/**
 * This reviewer confirms that an ACB user does not attempt to add a 'removed' macra measure
 * to any certification result.
 * @author kekey
 *
 */
@Component("macraMeasureComparisonReviewer")
public class MacraMeasureComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public MacraMeasureComparisonReviewer(final ResourcePermissions resourcePermissions,
            final ErrorMessageUtil msgUtil) {
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(final CertifiedProductSearchDetails existingListing,
            final CertifiedProductSearchDetails updatedListing) {
        //checking for the addition of a removed macra measure.
        //this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        for (CertificationResult updatedCert : updatedListing.getCertificationResults()) {
            for (CertificationResult existingCert : existingListing.getCertificationResults()) {
                //find matching criteria in existing/updated listings
                if (!StringUtils.isEmpty(updatedCert.getNumber()) && !StringUtils.isEmpty(existingCert.getNumber())
                        && updatedCert.getNumber().equals(existingCert.getNumber()) && updatedCert.isReviewable()) {
                    //if the updated listing has attested to the cert, check its g1/g2 macra measures
                    if (updatedCert.getG1MacraMeasures() != null
                            && updatedCert.getG1MacraMeasures().size() > 0) {
                        List<MacraMeasure> addedG1Measures =
                                getAddedMacraMeasures(existingCert.getG1MacraMeasures(), updatedCert.getG1MacraMeasures());
                        for (MacraMeasure addedG1Measure : addedG1Measures) {
                            if (addedG1Measure.getRemoved() != null && addedG1Measure.getRemoved().booleanValue()) {
                                updatedListing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.removedG1MacraMeasure",
                                        updatedCert.getNumber(), addedG1Measure.getAbbreviation()));
                            }
                        }
                    }

                    if (updatedCert.getG2MacraMeasures() != null
                            && updatedCert.getG2MacraMeasures().size() > 0) {
                        List<MacraMeasure> addedG2Measures =
                                getAddedMacraMeasures(existingCert.getG2MacraMeasures(), updatedCert.getG2MacraMeasures());
                        for (MacraMeasure addedG2Measure : addedG2Measures) {
                            if (addedG2Measure.getRemoved() != null && addedG2Measure.getRemoved().booleanValue()) {
                                updatedListing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.removedG2MacraMeasure",
                                        updatedCert.getNumber(), addedG2Measure.getAbbreviation()));
                            }
                        }
                    }
                }
            }
        }
    }

    private List<MacraMeasure> getAddedMacraMeasures(
            final List<MacraMeasure> existingMeasures,
            final List<MacraMeasure> updatedMeasures) {
        List<MacraMeasure> addedMeasures = new ArrayList<MacraMeasure>();

        // figure out which macra measures to add
        if (updatedMeasures != null && updatedMeasures.size() > 0) {
            if (existingMeasures == null || existingMeasures.size() == 0) {
                // existing criteria has none, add all from the update
                for (MacraMeasure updatedMeasure : updatedMeasures) {
                    addedMeasures.add(updatedMeasure);
                }
            } else if (existingMeasures.size() > 0) {
                // existing criteria has some, compare to the update to see if
                // any are different
                for (MacraMeasure updatedMeasure : updatedMeasures) {
                    boolean inExistingCriteria = false;
                    for (MacraMeasure existingMeasure : existingMeasures) {
                        inExistingCriteria = !inExistingCriteria ? updatedMeasure.matches(existingMeasure) : inExistingCriteria;
                    }

                    if (!inExistingCriteria) {
                        addedMeasures.add(updatedMeasure);
                    }
                }
            }
        }
        return addedMeasures;
    }
}
