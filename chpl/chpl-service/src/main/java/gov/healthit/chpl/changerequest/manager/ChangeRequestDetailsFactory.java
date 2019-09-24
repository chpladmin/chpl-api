package gov.healthit.chpl.changerequest.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestDetailsFactory {
    private ChangeRequestWebsiteHelper crWebsiteHelper;

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    @Autowired
    public ChangeRequestDetailsFactory(final ChangeRequestWebsiteHelper crWebsiteHelper) {
        this.crWebsiteHelper = crWebsiteHelper;
    }

    public ChangeRequestDetailsHelper<?> get(Long changeRequestType) {
        ChangeRequestDetailsHelper<?> crDetailsHelper = null;

        if (changeRequestType.equals(changeRequestType)) {
            crDetailsHelper = crWebsiteHelper;
        }
        return crDetailsHelper;
    }
}
