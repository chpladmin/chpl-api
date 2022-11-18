package gov.healthit.chpl.attestation.manager;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.scheduler.job.developer.attestation.DeveloperAttestationPeriodCalculator;

@Component
public class AttestationDeveloperService {

    private DeveloperAttestationPeriodCalculator devAttestationPeriodCalculator;

    private void getDevelopersRequiringAttestationForMostRecentPastAttestationPeriod() {
        return devAttestationPeriodCalculator.getDevelopersWithActiveListingsDuringMostRecentPastAttestationPeriod(null);

    }
}
