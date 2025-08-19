package gov.healthit.chpl.changerequest.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestDetailsFactory {
    private ChangeRequestDeveloperDemographicsService crDeveloperDemographicsService;
    private ChangeRequestAttestationService crAttestattionService;
    private ChangeRequestServiceBaseUrlListService crServiceBaseUrlListService;
    private ChangeRequestRwtPlansUrlService crRwtPlansUrlService;
    private ChangeRequestRwtResultsUrlService crRwtResultsUrlService;

    @Value("${changerequest.developerDemographics}")
    private Long developerDemographicsChangeRequestType;

    @Value("${changerequest.attestation}")
    private Long attestationChangeRequestType;

    @Value("${changerequest.serviceBaseUrlList}")
    private Long serviceBaseUrlListChangeRequestType;

    @Value("${changerequest.rwtPlansUrl}")
    private Long rwtPlansUrlChangeRequestType;

    @Value("${changerequest.rwtResultsUrl}")
    private Long rwtResultsUrlChangeRequestType;

    @Autowired
    public ChangeRequestDetailsFactory(ChangeRequestDeveloperDemographicsService crDevDemographicsService,
            ChangeRequestAttestationService crAttestationService,
            ChangeRequestServiceBaseUrlListService crServiceBaseUrlListService,
            ChangeRequestRwtPlansUrlService crRwtPlansUrlService,
            ChangeRequestRwtResultsUrlService crRwtResultsUrlService) {
        this.crDeveloperDemographicsService = crDevDemographicsService;
        this.crAttestattionService = crAttestationService;
        this.crServiceBaseUrlListService = crServiceBaseUrlListService;
        this.crRwtPlansUrlService = crRwtPlansUrlService;
        this.crRwtResultsUrlService = crRwtResultsUrlService;
    }

    public ChangeRequestDetailsService<?> get(Long changeRequestType) {
        ChangeRequestDetailsService<?> crDetailsService = null;

        if (changeRequestType.equals(developerDemographicsChangeRequestType)) {
            crDetailsService = crDeveloperDemographicsService;
        } else if (changeRequestType.equals(attestationChangeRequestType)) {
            crDetailsService = crAttestattionService;
        } else if (changeRequestType.equals(serviceBaseUrlListChangeRequestType)) {
            crDetailsService = crServiceBaseUrlListService;
        } else if (changeRequestType.equals(rwtPlansUrlChangeRequestType)) {
            crDetailsService = crRwtPlansUrlService;
        } else if (changeRequestType.equals(rwtResultsUrlChangeRequestType)) {
            crDetailsService = crRwtResultsUrlService;
        }
        return crDetailsService;
    }
}
