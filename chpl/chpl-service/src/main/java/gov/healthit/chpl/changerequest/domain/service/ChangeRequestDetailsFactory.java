package gov.healthit.chpl.changerequest.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestDetailsFactory {
    private ChangeRequestDeveloperDemographicsService crDeveloperDemographicsService;
    private ChangeRequestAttestationService crAttestattionService;

    @Value("${changerequest.developerDemographics}")
    private Long developerDemographicsChangeRequestType;

    @Value("${changerequest.attestation}")
    private Long attestationChangeRequestType;

    @Autowired
    public ChangeRequestDetailsFactory(ChangeRequestDeveloperDemographicsService crDevDemographicsService,
            ChangeRequestAttestationService crAttestationService) {
        this.crDeveloperDemographicsService = crDevDemographicsService;
        this.crAttestattionService = crAttestationService;
    }

    public ChangeRequestDetailsService<?> get(Long changeRequestType) {
        ChangeRequestDetailsService<?> crDetailsService = null;

        if (changeRequestType.equals(developerDemographicsChangeRequestType)) {
            crDetailsService = crDeveloperDemographicsService;
        } else if (changeRequestType.equals(attestationChangeRequestType)) {
            crDetailsService = crAttestattionService;
        }
        return crDetailsService;
    }
}
