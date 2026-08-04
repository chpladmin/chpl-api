package gov.healthit.chpl.upload.listing;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class ListingUploadHeadingUtil {

    private CertificationCriterionService criteriaService;
    private FF4j ff4j;

    @Autowired
    public ListingUploadHeadingUtil(CertificationCriterionService criteriaService,
            FF4j ff4j) {
        this.criteriaService = criteriaService;
        this.ff4j = ff4j;
    }

    public List<Heading> getRequiredHeadings() {
        if (ff4j.check(FeatureList.HTI_5_ERD)) {
            return Arrays.asList(HeadingPostHti5.UNIQUE_ID, HeadingPostHti5.DEVELOPER, HeadingPostHti5.PRODUCT, HeadingPostHti5.VERSION);
        } else {
            return Arrays.asList(HeadingPreHti5.UNIQUE_ID, HeadingPreHti5.DEVELOPER, HeadingPreHti5.PRODUCT, HeadingPreHti5.VERSION);
        }
    }

    public Heading getHeading(String colName) {
        if (StringUtils.isEmpty(colName)) {
            return null;
        }

        if (ff4j.check(FeatureList.HTI_5_ERD)) {
            for (HeadingPostHti5 heading : HeadingPostHti5.values()) {
                if (heading.getColNames().stream()
                        .anyMatch(headingColName -> headingColName.equalsIgnoreCase(colName.trim()))) {
                    return heading;
                }
            }
        } else {
            for (HeadingPreHti5 heading : HeadingPreHti5.values()) {
                if (heading.getColNames().stream()
                        .anyMatch(headingColName -> headingColName.equalsIgnoreCase(colName.trim()))) {
                    return heading;
                }
            }
        }
        return null;
    }

    public List<String> getHeadingOptions(String heading) {
        if (getHeading(heading) != null) {
            return getHeading(heading).getColNames();
        } else if (isCriterionHeading(heading)) {
            //TODO  This really should return the list of other headings that could be used for a criterion,
            //but I don't have a good way of mapping from one heading to all the other headings that could be used.
            //The result is if someone used a column 170_315_A_1__C and 170_315_A_1_C (one heading has one underscore
            //and the other has two underscores) those will not be detected as duplicates at this time.
            return criteriaService.getEquivalentCriterionHeadings(heading);
        }
        return List.of();
    }

    public boolean isValidHeading(String heading) {
        return getHeading(heading) != null || isCriterionHeading(heading);
    }

    public boolean isCriterionHeading(String heading) {
        if (criteriaService.getAllowedCriterionHeadingsForNewListing().contains(heading.toUpperCase())) {
            return true;
        }
        return false;
    }
}
