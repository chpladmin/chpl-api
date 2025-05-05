package gov.healthit.chpl.changerequest.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestDetailsFactory {
    private ChangeRequestDeveloperDemographicsService crDeveloperDemographicsService;
    private ChangeRequestAttestationService crAttestattionService;
    private ChangeRequestServiceBaseUrlListService crServiceBaseUrlListService;

    @Value("${changerequest.developerDemographics}")
    private Long developerDemographicsChangeRequestType;

    @Value("${changerequest.attestation}")
    private Long attestationChangeRequestType;

    //@Value("${changerequest.serviceBaseUrlList}")
    private Long serviceBaseUrlListChangeRequestType = 4L;

    @Autowired
    public ChangeRequestDetailsFactory(ChangeRequestDeveloperDemographicsService crDevDemographicsService,
            ChangeRequestAttestationService crAttestationService, ChangeRequestServiceBaseUrlListService crServiceBaseUrlListService) {
        this.crDeveloperDemographicsService = crDevDemographicsService;
        this.crAttestattionService = crAttestationService;
        this.crServiceBaseUrlListService = crServiceBaseUrlListService;
    }

    public ChangeRequestDetailsService<?> get(Long changeRequestType) {
        ChangeRequestDetailsService<?> crDetailsService = null;

        if (changeRequestType.equals(developerDemographicsChangeRequestType)) {
            crDetailsService = crDeveloperDemographicsService;
        } else if (changeRequestType.equals(attestationChangeRequestType)) {
            crDetailsService = crAttestattionService;
        } else if (changeRequestType.equals(serviceBaseUrlListChangeRequestType)) {
            crDetailsService = crServiceBaseUrlListService;
        }
        return crDetailsService;
    }
}
