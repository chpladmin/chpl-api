package gov.healthit.chpl.changerequest.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestDetailsFactory {
    private ChangeRequestWebsiteService crWebsiteService;
    private ChangeRequestDeveloperDetailsService crDevDetailsService;

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    @Value("${changerequest.developerDetails}")
    private Long developerDetailsChangeRequestType;

    @Autowired
    public ChangeRequestDetailsFactory(ChangeRequestWebsiteService crWebsiteService,
            ChangeRequestDeveloperDetailsService crDevDetailsService) {
        this.crWebsiteService = crWebsiteService;
        this.crDevDetailsService = crDevDetailsService;
    }

    public ChangeRequestDetailsService<?> get(Long changeRequestType) {
        ChangeRequestDetailsService<?> crDetailsService = null;

        if (changeRequestType.equals(websiteChangeRequestType)) {
            crDetailsService = crWebsiteService;
        } else if (changeRequestType.equals(developerDetailsChangeRequestType)) {
            crDetailsService = crDevDetailsService;
        }
        return crDetailsService;
    }
}
