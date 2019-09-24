package gov.healthit.chpl.changerequest.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestDetailsFactory {
    private ChangeRequestWebsiteService crWebsiteHelper;

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    @Autowired
    public ChangeRequestDetailsFactory(final ChangeRequestWebsiteService crWebsiteHelper) {
        this.crWebsiteHelper = crWebsiteHelper;
    }

    public ChangeRequestDetailsService<?> get(Long changeRequestType) {
        ChangeRequestDetailsService<?> crDetailsHelper = null;

        if (changeRequestType.equals(changeRequestType)) {
            crDetailsHelper = crWebsiteHelper;
        }
        return crDetailsHelper;
    }
}
