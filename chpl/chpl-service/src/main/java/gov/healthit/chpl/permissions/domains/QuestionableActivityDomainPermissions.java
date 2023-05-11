package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.questionableActivity.SearchActionPermissions;

@Component
public class QuestionableActivityDomainPermissions extends DomainPermissions {
    public static final String SEARCH = "SEARCH";

    @Autowired
    public QuestionableActivityDomainPermissions(
            @Qualifier("questionableActivitySearchActionPermissions") SearchActionPermissions searchActionPermissions) {

        getActionPermissions().put(SEARCH, searchActionPermissions);
    }
}
