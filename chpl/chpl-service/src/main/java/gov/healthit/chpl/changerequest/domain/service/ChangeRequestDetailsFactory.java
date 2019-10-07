package gov.healthit.chpl.changerequest.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestDetailsFactory {
    private ChangeRequestWebsiteService crWebsiteService;

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    @Autowired
    public ChangeRequestDetailsFactory(final ChangeRequestWebsiteService crWebsiteService) {
        this.crWebsiteService = crWebsiteService;
    }

    public ChangeRequestDetailsService<?> get(Long changeRequestType) {
        ChangeRequestDetailsService<?> crDetailsService = null;

        if (changeRequestType.equals(changeRequestType)) {
            crDetailsService = crWebsiteService;
        }
        return crDetailsService;
    }
}
