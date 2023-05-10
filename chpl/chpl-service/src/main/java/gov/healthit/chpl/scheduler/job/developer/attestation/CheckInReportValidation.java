package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.service.AttestationResponseValidationService;
import gov.healthit.chpl.attestation.service.ListingApplicabilityService;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;

@Component
public class CheckInReportValidation {
    private static final String YES = "Yes";
    private static final String NO = "No";

    private AttestationResponseValidationService attestationValidation;
    private ListingApplicabilityService listingApplicabilityService;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public CheckInReportValidation(AttestationResponseValidationService attestationValidation,
            ListingApplicabilityService listingApplicabilityService,
            ErrorMessageUtil errorMessageUtil) {
        this.attestationValidation = attestationValidation;
        this.listingApplicabilityService = listingApplicabilityService;
        this.errorMessageUtil = errorMessageUtil;
    }

    public String getRealWorldTestingValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        String warning = NullSafeEvaluator.eval(() -> getRealWordTestingWarningMessage(allActiveListingsForDeveloper, attestationForm), "");
        if (listingApplicabilityService.isRealWorldTestingApplicable(allActiveListingsForDeveloper)) {
            return YES + (warning != "" ? " - " : "") + warning;
        } else {
            return NO + (warning != "" ? " - " : "") + warning;
        }
    }

    public String getAssurancesValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm, AttestationPeriod period) {
        String warning = NullSafeEvaluator.eval(() -> getAssurancesWarningMessage(allActiveListingsForDeveloper, attestationForm, period.getId()), "");
        if (listingApplicabilityService.isAssurancesApplicable(allActiveListingsForDeveloper)) {
            return YES + (warning != "" ? " - " : "") + warning;
        } else {
            return NO + (warning != "" ? " - " : "") + warning;
        }
    }

    public String getApiValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        String warning = NullSafeEvaluator.eval(() -> getApiWarningMessage(allActiveListingsForDeveloper, attestationForm), "");
        if (listingApplicabilityService.isApiApplicable(allActiveListingsForDeveloper)) {
            return YES + (warning != "" ? " - " : "") + warning;
        } else {
            return NO + (warning != "" ? " - " : "") + warning;
        }
    }

    private String getApiWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        if (attestationValidation.isApiApplicableAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm)
                || attestationValidation.isApiNotApplicableAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm)) {
            return errorMessageUtil.getMessage("attestation.checkInReport.apiResponseNotConsistent");
        } else {
            return null;
        }
    }

    private String getAssurancesWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm, Long attestationPeriodId) {
        if (attestationValidation.isAssurancesApplicableAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm, attestationPeriodId)
                || attestationValidation.isAssurancesNotApplicableAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm, attestationPeriodId)) {
            return errorMessageUtil.getMessage("attestation.checkInReport.assurancesResponseNotConsistent");
        } else {
            return null;
        }
    }

    private String getRealWordTestingWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        if (attestationValidation.isRwtApplicableAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm)
                || attestationValidation.isRwtNotApplicableAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm)) {
            return errorMessageUtil.getMessage("attestation.checkInReport.rwtResponseNotConsistent");
        } else {
            return null;
        }
    }
}
