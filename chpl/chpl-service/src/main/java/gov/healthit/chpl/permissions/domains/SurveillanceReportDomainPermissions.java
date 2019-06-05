package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.surveillance.report.CreateAnnualReportActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.report.CreateQuarterlyReportActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.report.DeleteAnnualReportActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.report.DeleteQuarterlyReportActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.report.ExportAnnualReportActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.report.ExportQuarterlyReportActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.report.GetAnnualReportActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.report.GetQuarterlyReportActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.report.UpdateAnnualReportActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.report.UpdateQuarterlyReportActionPermissions;

@Component
public class SurveillanceReportDomainPermissions extends DomainPermissions {
    public static final String GET_ANNUAL = "GET_ANNUAL";
    public static final String CREATE_ANNUAL = "CREATE_ANNUAL";
    public static final String UPDATE_ANNUAL = "UPDATE_ANNUAL";
    public static final String DELETE_ANNUAL = "DELETE_ANNUAL";
    public static final String EXPORT_ANNUAL = "EXPORT_ANNUAL";

    public static final String GET_QUARTERLY = "GET_QUARTERLY";
    public static final String CREATE_QUARTERLY = "CREATE_QUARTERLY";
    public static final String UPDATE_QUARTERLY = "UPDATE_QUARTERLY";
    public static final String DELETE_QUARTERLY = "DELETE_QUARTERLY";
    public static final String EXPORT_QUARTERLY = "EXPORT_QUARTERLY";

    @Autowired
    public SurveillanceReportDomainPermissions(
            @Qualifier("surveillanceReportGetAnnualReportActionPermissions")
            final GetAnnualReportActionPermissions getAnnualReportActionPermissions,
            @Qualifier("surveillanceReportCreateAnnualReportActionPermissions")
            final CreateAnnualReportActionPermissions createAnnualReportActionPermissions,
            @Qualifier("surveillanceReportUpdateAnnualReportActionPermissions")
            final UpdateAnnualReportActionPermissions updateAnnualReportActionPermissions,
            @Qualifier("surveillanceReportDeleteAnnualReportActionPermissions")
            final DeleteAnnualReportActionPermissions deleteAnnualReportActionPermissions,
            @Qualifier("surveillanceReportExportAnnualReportActionPermissions")
            final ExportAnnualReportActionPermissions exportAnnualReportActionPermissions,
            @Qualifier("surveillanceReportGetQuarterlyReportActionPermissions")
            final GetQuarterlyReportActionPermissions getQuarterlyReportActionPermissions,
            @Qualifier("surveillanceReportCreateQuarterlyReportActionPermissions")
            final CreateQuarterlyReportActionPermissions createQuarterlyReportActionPermissions,
            @Qualifier("surveillanceReportUpdateQuarterlyReportActionPermissions")
            final UpdateQuarterlyReportActionPermissions updateQuarterlyReportActionPermissions,
            @Qualifier("surveillanceReportDeleteQuarterlyReportActionPermissions")
            final DeleteQuarterlyReportActionPermissions deleteQuarterlyReportActionPermissions,
            @Qualifier("surveillanceReportExportQuarterlyReportActionPermissions")
            final ExportQuarterlyReportActionPermissions exportQuarterlyReportActionPermissions) {

        getActionPermissions().put(GET_QUARTERLY, getQuarterlyReportActionPermissions);
        getActionPermissions().put(CREATE_QUARTERLY, createQuarterlyReportActionPermissions);
        getActionPermissions().put(UPDATE_QUARTERLY, updateQuarterlyReportActionPermissions);
        getActionPermissions().put(DELETE_QUARTERLY, deleteQuarterlyReportActionPermissions);
        getActionPermissions().put(EXPORT_QUARTERLY, exportQuarterlyReportActionPermissions);
    }

}
