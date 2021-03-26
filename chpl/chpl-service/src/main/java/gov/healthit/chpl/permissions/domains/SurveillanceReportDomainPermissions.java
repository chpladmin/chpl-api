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
            GetAnnualReportActionPermissions getAnnualReportActionPermissions,
            @Qualifier("surveillanceReportCreateAnnualReportActionPermissions")
            CreateAnnualReportActionPermissions createAnnualReportActionPermissions,
            @Qualifier("surveillanceReportUpdateAnnualReportActionPermissions")
            UpdateAnnualReportActionPermissions updateAnnualReportActionPermissions,
            @Qualifier("surveillanceReportDeleteAnnualReportActionPermissions")
            DeleteAnnualReportActionPermissions deleteAnnualReportActionPermissions,
            @Qualifier("surveillanceReportExportAnnualReportActionPermissions")
            ExportAnnualReportActionPermissions exportAnnualReportActionPermissions,
            @Qualifier("surveillanceReportGetQuarterlyReportActionPermissions")
            GetQuarterlyReportActionPermissions getQuarterlyReportActionPermissions,
            @Qualifier("surveillanceReportCreateQuarterlyReportActionPermissions")
            CreateQuarterlyReportActionPermissions createQuarterlyReportActionPermissions,
            @Qualifier("surveillanceReportUpdateQuarterlyReportActionPermissions")
            UpdateQuarterlyReportActionPermissions updateQuarterlyReportActionPermissions,
            @Qualifier("surveillanceReportDeleteQuarterlyReportActionPermissions")
            DeleteQuarterlyReportActionPermissions deleteQuarterlyReportActionPermissions,
            @Qualifier("surveillanceReportExportQuarterlyReportActionPermissions")
            ExportQuarterlyReportActionPermissions exportQuarterlyReportActionPermissions) {

        getActionPermissions().put(GET_ANNUAL, getAnnualReportActionPermissions);
        getActionPermissions().put(CREATE_ANNUAL, createAnnualReportActionPermissions);
        getActionPermissions().put(UPDATE_ANNUAL, updateAnnualReportActionPermissions);
        getActionPermissions().put(DELETE_ANNUAL, deleteAnnualReportActionPermissions);
        getActionPermissions().put(EXPORT_ANNUAL, exportAnnualReportActionPermissions);

        getActionPermissions().put(GET_QUARTERLY, getQuarterlyReportActionPermissions);
        getActionPermissions().put(CREATE_QUARTERLY, createQuarterlyReportActionPermissions);
        getActionPermissions().put(UPDATE_QUARTERLY, updateQuarterlyReportActionPermissions);
        getActionPermissions().put(DELETE_QUARTERLY, deleteQuarterlyReportActionPermissions);
        getActionPermissions().put(EXPORT_QUARTERLY, exportQuarterlyReportActionPermissions);
    }

}
