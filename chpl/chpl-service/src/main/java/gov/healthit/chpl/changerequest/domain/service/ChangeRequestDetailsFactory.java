package gov.healthit.chpl.changerequest.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestDetailsFactory {
    private ChangeRequestDeveloperDemographicService crDeveloperDemographicService;
    private ChangeRequestAttestationService crAttestattionService;

    @Value("${changerequest.developerDemographic}")
    private Long developerDemographicChangeRequestType;

    @Value("${changerequest.attestation}")
    private Long attestationChangeRequestType;

    @Autowired
    public ChangeRequestDetailsFactory(ChangeRequestDeveloperDemographicService crDevDemographicService,
            ChangeRequestAttestationService crAttestationService) {
        this.crDeveloperDemographicService = crDevDemographicService;
        this.crAttestattionService = crAttestationService;
    }

    public ChangeRequestDetailsService<?> get(Long changeRequestType) {
        ChangeRequestDetailsService<?> crDetailsService = null;

        if (changeRequestType.equals(developerDemographicChangeRequestType)) {
            crDetailsService = crDeveloperDemographicService;
        } else if (changeRequestType.equals(attestationChangeRequestType)) {
            crDetailsService = crAttestattionService;
        }
        return crDetailsService;
    }
}
