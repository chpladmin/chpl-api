package gov.healthit.chpl.changerequest.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestDetailsFactory {
    private ChangeRequestWebsiteService crWebsiteService;
    private ChangeRequestDeveloperDetailsService crDevDetailsService;
    private ChangeRequestAttestationService crAttestattionService;

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    @Value("${changerequest.developerDetails}")
    private Long developerDetailsChangeRequestType;

    @Value("${changerequest.attestation}")
    private Long attestationChangeRequestType;

    @Autowired
    public ChangeRequestDetailsFactory(ChangeRequestWebsiteService crWebsiteService,
            ChangeRequestDeveloperDetailsService crDevDetailsService,
            ChangeRequestAttestationService crAttestationService) {
        this.crWebsiteService = crWebsiteService;
        this.crDevDetailsService = crDevDetailsService;
        this.crAttestattionService = crAttestationService;
    }

    public ChangeRequestDetailsService<?> get(Long changeRequestType) {
        ChangeRequestDetailsService<?> crDetailsService = null;

        if (changeRequestType.equals(websiteChangeRequestType)) {
            crDetailsService = crWebsiteService;
        } else if (changeRequestType.equals(developerDetailsChangeRequestType)) {
            crDetailsService = crDevDetailsService;
        } else if (changeRequestType.equals(attestationChangeRequestType)) {
            crDetailsService = crAttestattionService;
        }
        return crDetailsService;
    }
}
