package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

/**
 * Given a user tries to edit a listing
 * And the listing has an old version of (d)(2), (d)(3), (d)(10), or (b)(3)
 * And the listing does not have ICS
 * And the certification date is on or after the criterion's "no longer allowed" date
 * Then the user should have errors
 */
@Log4j2
@Component("oldCriteriaWithoutIcsReviewer")
public class OldCriteriaWithoutIcsReviewer implements Reviewer {
    private static final String B3_CHANGE_DATE = "questionableActivity.b3ChangeDate";
    private static final String RULE_EFFECTIVE_DATE = "cures.ruleEffectiveDate";

    private static final String B3_CRITERIA_NUMBER = "170.315 (b)(3)";
    private static final String D2_CRITERIA_NUMBER = "170.315 (d)(2)";
    private static final String D3_CRITERIA_NUMBER = "170.315 (d)(3)";
    private static final String D10_CRITERIA_NUMBER = "170.315 (d)(10)";
    private long b3Id;
    private long d2Id;
    private long d3Id;
    private long d10Id;

    private Environment env;
    private ErrorMessageUtil msgUtil;
    private CertificationCriterionService criteriaService;

    @Autowired
    public OldCriteriaWithoutIcsReviewer(Environment env, ErrorMessageUtil msgUtil,
            CertificationCriterionService criteriaService) {
        this.env = env;
        this.msgUtil = msgUtil;
        this.criteriaService = criteriaService;
    }

    @PostConstruct
    public void postConstruct() {
        b3Id = criteriaService.get(Criteria2015.B_3_OLD).getId();
        d2Id = criteriaService.get(Criteria2015.D_2_OLD).getId();
        d3Id = criteriaService.get(Criteria2015.D_3_OLD).getId();
        d10Id = criteriaService.get(Criteria2015.D_10_OLD).getId();
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (hasICS(listing)) {
            return;
        }
        long b3ChangeDate;
        long ruleEffectiveDate;
        try {
            b3ChangeDate = getPropertyDate(B3_CHANGE_DATE);
            ruleEffectiveDate = getPropertyDate(RULE_EFFECTIVE_DATE);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return;
        }

        String error = checkForError(listing, B3_CRITERIA_NUMBER, b3Id, b3ChangeDate);
        if (!StringUtils.isEmpty(error)) {
            listing.getErrorMessages().add(error);
        }
        error = checkForError(listing, D2_CRITERIA_NUMBER, d2Id, ruleEffectiveDate);
        if (!StringUtils.isEmpty(error)) {
            listing.getErrorMessages().add(error);
        }
        error = checkForError(listing, D3_CRITERIA_NUMBER, d3Id, ruleEffectiveDate);
        if (!StringUtils.isEmpty(error)) {
            listing.getErrorMessages().add(error);
        }
        error = checkForError(listing, D10_CRITERIA_NUMBER, d10Id, ruleEffectiveDate);
        if (!StringUtils.isEmpty(error)) {
            listing.getErrorMessages().add(error);
        }
    }

    private String checkForError(CertifiedProductSearchDetails listing, String criteria, long criteriaId, long relevantDate) {
        long certificationDate = listing.getCertificationDate();
        if (hasRelevantCriteria(listing, criteriaId) && certificationDate >= relevantDate) {
            return getErrorMessage("listing.criteria.hasOldVersionOfCriteria", criteria,
                    Util.getDateFormatter().format(relevantDate));
        }
        return null;
    }

    private String getErrorMessage(String messageCode, String criteria, String relevantDate) {
        return msgUtil.getMessage(messageCode, criteria, relevantDate);
    }

    private long getPropertyDate(String date) throws ParseException {
        String dateAsString = env.getProperty(date);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return sdf.parse(dateAsString).getTime();
    }

    private boolean hasRelevantCriteria(CertifiedProductSearchDetails listing, long criteriaId) {
        return listing.getCertificationResults().stream()
                .anyMatch(result -> result.getCriterion().getId().equals(criteriaId)
                        && result.isSuccess());
    }

    private boolean hasICS(CertifiedProductSearchDetails listing) {
        if (listing != null && listing.getIcs() != null && listing.getIcs().getInherits() != null) {
            return listing.getIcs().getInherits();
        } else {
            return false;
        }
    }
}
