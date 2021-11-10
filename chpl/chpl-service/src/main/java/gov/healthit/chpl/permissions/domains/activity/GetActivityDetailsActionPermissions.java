package gov.healthit.chpl.permissions.domains.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.surveillance.report.AnnualReportDAO;
import gov.healthit.chpl.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.surveillance.report.domain.AnnualReport;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportRelevantListingDTO;

@Component("actionGetActivityDetailsActionPermissions")
public class GetActivityDetailsActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(GetActivityDetailsActionPermissions.class);

    private UserDAO userDao;
    private QuarterlyReportDAO quarterlyReportDao;
    private AnnualReportDAO annualReportDao;
    private ObjectMapper jsonMapper;

    @Autowired
    public GetActivityDetailsActionPermissions(final UserDAO userDao,
            final QuarterlyReportDAO quarterlyReportDao,
            final AnnualReportDAO annualReportDao) {
        jsonMapper = new ObjectMapper();
        this.userDao = userDao;
        this.quarterlyReportDao = quarterlyReportDao;
        this.annualReportDao = annualReportDao;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof ActivityDetails)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else {
            ActivityDetails details = (ActivityDetails) obj;
            // There are different security rules depending on what type
            // of activity this is.
            switch (details.getConcept()) {
            case ANNOUNCEMENT:
                if (details.getNewData() != null) {
                    return hasAccessToAnnouncement(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToAnnouncement(details.getOriginalData());
                }
                break;
            case TESTING_LAB:
                if (details.getNewData() != null) {
                    return hasAccessToTestingLab(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToTestingLab(details.getOriginalData());
                }
                break;
            case CERTIFICATION_BODY:
                if (details.getNewData() != null) {
                    return hasAccessToCertificationBody(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToCertificationBody(details.getOriginalData());
                }
                break;
            case API_KEY:
                // Admin and Onc can see this activity.
                // Others should get access denied.
                return (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc());
            case PENDING_CERTIFIED_PRODUCT:
                if (details.getNewData() != null) {
                    return hasAccessToPendingListing(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToPendingListing(details.getOriginalData());
                }
                break;
            case USER:
                if (details.getNewData() != null) {
                    return hasAccessToUser(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToUser(details.getOriginalData());
                }
                break;
            case COMPLAINT:
                if (details.getNewData() != null) {
                    return hasAccessToComplaint(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToComplaint(details.getOriginalData());
                }
                break;
            case QUARTERLY_REPORT:
                if (details.getNewData() != null) {
                    return hasAccessToQuarterlyReport(details.getActivityObjectId(), details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToQuarterlyReport(details.getActivityObjectId(), details.getOriginalData());
                }
                break;
            case QUARTERLY_REPORT_LISTING:
                if (details.getNewData() != null) {
                    return hasAccessToQuarterlyReportListing(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToQuarterlyReportListing(details.getOriginalData());
                }
                break;
            case ANNUAL_REPORT:
                if (details.getNewData() != null) {
                    return hasAccessToAnnualReport(details.getActivityObjectId(), details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToAnnualReport(details.getActivityObjectId(), details.getOriginalData());
                }
                break;
            case CHANGE_REQUEST:
                if (details.getNewData() != null) {
                    return hasAccessToChangeRequest(details.getNewData());
                } else if (details.getOriginalData() != null) {
                    return hasAccessToChangeRequest(details.getOriginalData());
                }
                break;
            default:
                // all other types of activity
                // are accessible to any logged-in or anonymous user
                return true;
            }
        }
        return false;
    }

    private boolean hasAccessToComplaint(final JsonNode complaintJson) {
        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Complaint complaint = null;
            try {
                complaint = jsonMapper.convertValue(complaintJson, Complaint.class);
                return isAcbValidForCurrentUser(complaint.getCertificationBody().getId());
            } catch (Exception e) {
                LOGGER.error("Could not parse complaint activity as ComplaintDTO. JSON was: " + complaintJson, e);
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean hasAccessToQuarterlyReport(final Long reportId, final JsonNode quarterlyReportJson) {
        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            // data could be a QuarterlyReportDTO or a UserDTO if the action was
            // to export the report
            try {
                QuarterlyReportDTO report = jsonMapper.convertValue(quarterlyReportJson, QuarterlyReportDTO.class);
                return isAcbValidForCurrentUser(report.getAcb().getId());
            } catch (Exception e) {
                LOGGER.warn(
                        "Could not parse complaint activity as QuarterlyReportDTO. JSON was: " + quarterlyReportJson);
            }
            // if we haven't returned then it's not a quarterly report object
            // so must be an export action - look up the quarterly report by id
            // to see if the user has access
            try {
                QuarterlyReportDTO report = quarterlyReportDao.getById(reportId);
                return isAcbValidForCurrentUser(report.getAcb().getId());
            } catch (Exception ignore) {
                LOGGER.warn("Could find quarterly report with ID " + reportId);
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean hasAccessToQuarterlyReportListing(final JsonNode quarterlyReportListingJson) {
        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            QuarterlyReportRelevantListingDTO listing = null;
            try {
                listing = jsonMapper.convertValue(quarterlyReportListingJson, QuarterlyReportRelevantListingDTO.class);
                return isAcbValidForCurrentUser(listing.getCertificationBodyId());
            } catch (Exception e) {
                LOGGER.error("Could not parse complaint activity as QuarterlyReportRelevantListingDTO. JSON was: "
                        + quarterlyReportListingJson);
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean hasAccessToAnnualReport(final Long reportId, final JsonNode annualReportJson) {
        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            // json could be an annual report object or a user object if the
            // action was an export
            try {
                AnnualReport report = jsonMapper.convertValue(annualReportJson, AnnualReport.class);
                return isAcbValidForCurrentUser(report.getAcb().getId());
            } catch (Exception e) {
                LOGGER.warn("Could not parse complaint activity as AnnualReport. JSON was: " + annualReportJson);
            }
            // if we haven't returned then it's not an annual report object
            // so must be an export action - look up the annual report by id to
            // see if the user has access
            try {
                AnnualReport report = annualReportDao.getById(reportId);
                return isAcbValidForCurrentUser(report.getAcb().getId());
            } catch (Exception ignore) {
                LOGGER.warn("Could find annual report with ID " + reportId);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Non-logged in user can only see activity for public announcements. Other
     * users can see all activity.
     *
     * @param announcementJson
     * @return
     */
    private boolean hasAccessToAnnouncement(final JsonNode announcementJson) {
        boolean hasAccess = false;
        AnnouncementDTO announcement = null;
        try {
            announcement = jsonMapper.convertValue(announcementJson, AnnouncementDTO.class);
        } catch (final Exception ex) {
            LOGGER.error("Could not parse announcement activity as AnnouncementDTO." + "JSON was: " + announcementJson,
                    ex);
        }

        if (announcement != null && announcement.getIsPublic()) {
            hasAccess = true;
        } else if (announcement != null && !announcement.getIsPublic() && !getResourcePermissions().isUserAnonymous()) {
            hasAccess = true;
        }
        return hasAccess;
    }

    /**
     * Admin and onc can see all atl activity including for retired atls. Atl
     * user can see activity for their own atl. Others should get access denied.
     *
     * @param atlJson
     * @return
     */
    private boolean hasAccessToTestingLab(final JsonNode atlJson) {
        boolean hasAccess = getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();

        if (!hasAccess) {
            TestingLabDTO atl = null;
            try {
                atl = jsonMapper.convertValue(atlJson, TestingLabDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity as TestingLabDTO. " + "JSON was: " + atlJson, ex);
            }

            if (atl != null && atl.getId() != null) {
                hasAccess = isAtlValidForCurrentUser(atl.getId());
            }
        }
        return hasAccess;
    }

    /**
     * Admin and onc can see all acb activity including for retired acbs. Acb
     * user can see activity for their own acb. Others should get access denied.
     *
     * @param acbJson
     * @return
     */
    private boolean hasAccessToCertificationBody(final JsonNode acbJson) {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    /**
     * Admin and onc can see all pending listing activity. Acb user can see
     * activity for listing uploaded to their Acb. Others should get access
     * denied.
     *
     * @param pendingListingJson
     * @return
     */
    private boolean hasAccessToPendingListing(final JsonNode pendingListingJson) {
        boolean hasAccess = getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();

        if (!hasAccess) {
            PendingCertifiedProductDTO pcp = null;
            try {
                pcp = jsonMapper.convertValue(pendingListingJson, PendingCertifiedProductDTO.class);
            } catch (final Exception ex) {
                LOGGER.error(
                        "Could not parse activity as PendingCertifiedProductDTO. " + "JSON was: " + pendingListingJson,
                        ex);
            }

            if (pcp != null && pcp.getCertificationBodyId() != null) {
                hasAccess = isAcbValidForCurrentUser(pcp.getCertificationBodyId());
            }
        }
        return hasAccess;
    }

    /**
     * Admin and Onc can see activity for any user. Acb, Atl, and Cms staff
     * users can see activity for any user they have access to. Non-logged in
     * users get access denied.
     *
     * @param userJson
     * @return
     */
    private boolean hasAccessToUser(final JsonNode userJson) {
        boolean hasAccess = getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff();

        if (!hasAccess) {
            UserDTO user = null;
            try {
                user = jsonMapper.convertValue(userJson, UserDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity as UserDTO. " + "JSON was: " + userJson, ex);
            }

            if (user != null) {
                List<UserDTO> accessibleUsers = new ArrayList<UserDTO>();
                if (getResourcePermissions().isUserRoleAcbAdmin()) {
                    // find all users on the acbs that this user has access to
                    // and see if the user in the activity is in that list
                    List<CertificationBodyDTO> accessibleAcbs = getResourcePermissions().getAllAcbsForCurrentUser();
                    for (CertificationBodyDTO acb : accessibleAcbs) {
                        accessibleUsers.addAll(getResourcePermissions().getAllUsersOnAcb(acb));
                    }
                }
                if (getResourcePermissions().isUserRoleAtlAdmin()) {
                    // find all users on the atls that this user has access to
                    // and see if the user in the activity is in that list
                    List<TestingLabDTO> accessibleAtls = getResourcePermissions().getAllAtlsForCurrentUser();
                    for (TestingLabDTO atl : accessibleAtls) {
                        accessibleUsers.addAll(getResourcePermissions().getAllUsersOnAtl(atl));
                    }
                }
                if (getResourcePermissions().isUserRoleCmsStaff()) {
                    List<UserDTO> cmsUsers = userDao.getUsersWithPermission(Authority.ROLE_CMS_STAFF);
                    accessibleUsers.addAll(cmsUsers);
                }

                for (UserDTO accessibleUser : accessibleUsers) {
                    if (accessibleUser.getId().longValue() == user.getId().longValue()) {
                        hasAccess = true;
                    }
                }
            }
        }
        return hasAccess;
    }

    private boolean hasAccessToChangeRequest(final JsonNode crJson) {
        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            ChangeRequest cr = null;
            try {
                cr = jsonMapper.convertValue(crJson, ChangeRequest.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity as ChangeReqest. " + "JSON was: " + crJson, ex);
            }

            if (cr != null && cr.getCertificationBodies() != null) {
                for (CertificationBody acb : cr.getCertificationBodies()) {
                    if (isAcbValidForCurrentUser(acb.getId())) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return false;
        }

    }
}
