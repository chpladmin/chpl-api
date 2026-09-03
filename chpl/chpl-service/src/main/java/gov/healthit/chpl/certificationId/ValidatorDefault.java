package gov.healthit.chpl.certificationId;

public class ValidatorDefault extends Validator {

    public ValidatorDefault() {
    }

    // **********************************************************************
    // onValidate
    //
    // **********************************************************************
    public boolean onValidate() {
        return false;
    }

    // **********************************************************************
    // isCriteriaValid
    //
    // Must meet all required criteria.
    // **********************************************************************
    protected boolean isCriteriaValid() {
        return false;
    }

    // **********************************************************************
    // isCqmsValid
    //
    // Either Inpatient or Ambulatory CQMs required.
    // **********************************************************************
    protected boolean isCqmsValid() {
        return false;
    }

    // **********************************************************************
    // isDomainsValid
    //
    // At least 3 CQM Domains must be met.
    // **********************************************************************
    protected boolean isDomainsValid() {
        return false;
    }
}
