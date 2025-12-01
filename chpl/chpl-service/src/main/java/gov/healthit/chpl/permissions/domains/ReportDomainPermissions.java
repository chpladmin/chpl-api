package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.report.GetActionPermissions;

@Component
public class ReportDomainPermissions extends DomainPermissions {
    public static final String GET_REPORT_METADATA = "GET_REPORT_METADATA";

    @Autowired
    public ReportDomainPermissions(
            @Qualifier("reportMetadataGetActionPermissions") GetActionPermissions getActionPermissions) {

        getActionPermissions().put(GET_REPORT_METADATA, getActionPermissions);
    }
}
