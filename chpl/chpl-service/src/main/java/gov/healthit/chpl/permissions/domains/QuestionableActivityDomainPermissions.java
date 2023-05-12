package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.questionableActivity.GetActionPermissions;

@Component
public class QuestionableActivityDomainPermissions extends DomainPermissions {
    public static final String GET = "GET";

    @Autowired
    public QuestionableActivityDomainPermissions(
            @Qualifier("questionableActivityGetActionPermissions") GetActionPermissions getActionPermissions) {

        getActionPermissions().put(GET, getActionPermissions);
    }
}
