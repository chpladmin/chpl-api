package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.AnnouncementManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.manager.TestingLabManager;
import gov.healthit.chpl.web.controller.exception.ValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "activity")
@RestController
@RequestMapping("/activity")
public class ActivityController {
	private static final Logger LOGGER = LogManager.getLogger(ActivityController.class);

	@Autowired Environment env;

	@Autowired private ActivityManager activityManager;
	@Autowired private CertificationBodyManager acbManager;
	@Autowired private AnnouncementManager announcementManager;
	@Autowired private TestingLabManager atlManager;
	@Autowired private CertifiedProductManager cpManager;
	@Autowired private CertificationIdManager certificationIdManager;
	@Autowired private PendingCertifiedProductManager pcpManager;
	@Autowired private DeveloperManager developerManager;
	@Autowired private ProductManager productManager;
	@Autowired private ProductVersionManager versionManager;
	@Autowired private UserManager userManager;

	@ApiOperation(value="Get auditable data for certification bodies.",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
			+ "Only users calling this API with ROLE_ADMIN may set the 'showDeleted' flag to true. "
			+ "Those users are allowed to see activity for all certification bodies including that have been deleted. "
			+ "The default behavior is to show all activity for non-deleted ACBs.")
	@RequestMapping(value="/acbs", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForACBs(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end,
			@RequestParam(value = "showDeleted", required = false, defaultValue="false") boolean showDeleted) throws JsonParseException, IOException, ValidationException {

		if(!Util.isUserRoleAdmin() && showDeleted) {
			LOGGER.warn("Non-admin user " + Util.getUsername() + " tried to see activity for deleted ACBs");
			throw new AccessDeniedException("Only Admins can see deleted ACB's");
		}else {
			if (start == null && end == null) {
				return getActivityEventsForACBs(showDeleted);
			} else {
				Date startDate = null;
				Date endDate = null;
				if(start != null) {
					startDate = new Date(start);
				}
				if(end != null) {
					endDate = new Date(end);
				}
				validateActivityDates(start, end);
				return getActivityEventsForACBs(showDeleted, startDate, endDate);
			}
		}
	}

	@ApiOperation(value="Get auditable data for a specific certification body.",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
			+ "Only users calling this API with ROLE_ADMIN may set the 'showDeleted' flag to true and should "
			+ "do so if the certification body specified in the path has been deleted. ")
	@RequestMapping(value="/acbs/ {id}", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForACBById(@PathVariable("id") Long id,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end,
			@RequestParam(value = "showDeleted", required = false, defaultValue="false") boolean showDeleted)
		throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
		acbManager.getById(id, showDeleted); //throws 404 if ACB doesn't exist

		if(!Util.isUserRoleAdmin() && showDeleted) {
			LOGGER.warn("Non-admin user " + Util.getUsername() + " tried to see activity for deleted ACB " + id);
			throw new AccessDeniedException("Only Admins can see deleted ACB's");
		} else {
			if (start == null && end == null) {
				return getActivityEventsForACBs(showDeleted, id);
			} else {
				Date startDate = null;
				Date endDate = null;
				if(start != null) {
					startDate = new Date(start);
				}
				if(end != null) {
					endDate = new Date(end);
				}
				validateActivityDates(start, end);
				return getActivityEventsForACBs(showDeleted, id, startDate, endDate);
			}
		}
	}

	@ApiOperation(value="Get auditable data for all announcements",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
					+ "The default behavior is to return announcement activity across all dates.")
	@RequestMapping(value="/announcements", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForAnnoucements(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end) throws JsonParseException, IOException, ValidationException {

		if (start == null && end == null) {
			return getActivityEventsForAnnouncements();
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForAnnouncements(startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for a specific announcement",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return all activity for the specified announcement across all dates.")
	@RequestMapping(value="/announcements/ {id}", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForAnnouncementById(@PathVariable("id") Long id,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end)
			throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
		announcementManager.getById(id); //throws 404 if bad id

		if (start == null && end == null) {
			return getActivityEventsForAnnouncements(id);
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForAnnouncements(id, startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for testing labs.",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
			+ "Only users calling this API with ROLE_ADMIN may set the 'showDeleted' flag to true. "
			+ "Those users are allowed to see activity for all testing labs including that have been deleted. "
			+ "The default behavior is to show all activity for non-deleted ATLs.")
	@RequestMapping(value="/atls", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityforATLs(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end,
			@RequestParam(value = "showDeleted", required = false, defaultValue="false") boolean showDeleted) throws JsonParseException, IOException, ValidationException {

		if(!Util.isUserRoleAdmin() && showDeleted) {
			LOGGER.warn("Non-admin user " + Util.getUsername() + " tried to see activity for deleted ATLs");
			throw new AccessDeniedException("Only Admins can see deleted ATL's");
		}else {
			if (start == null && end == null) {
				return getActivityEventsForATLs(showDeleted);
			} else {
				Date startDate = null;
				Date endDate = null;
				if(start != null) {
					startDate = new Date(start);
				}
				if(end != null) {
					endDate = new Date(end);
				}
				validateActivityDates(start, end);
				return getActivityEventsForATLs(showDeleted, startDate, endDate);
			}
		}
	}

	@ApiOperation(value="Get auditable data for a specific testing lab.",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
			+ "Only users calling this API with ROLE_ADMIN may set the 'showDeleted' flag to true and should "
			+ "do so if the testing lab specified in the path has been deleted. ")
	@RequestMapping(value="/atls/ {id}", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForATLById(@PathVariable("id") Long id,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end,
			@RequestParam(value = "showDeleted", required = false, defaultValue="false") boolean showDeleted)
			throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
		atlManager.getById(id, showDeleted); //throws 404 if bad id

		if(!Util.isUserRoleAdmin() && showDeleted) {
			LOGGER.warn("Non-admin user " + Util.getUsername() + " tried to see activity for deleted ATL " + id);
			throw new AccessDeniedException("Only Admins can see deleted ATL's");
		}else {
			if (start == null && end == null) {
				return getActivityEventsForATLs(showDeleted, id);
			} else {
				Date startDate = null;
				Date endDate = null;
				if(start != null) {
					startDate = new Date(start);
				}
				if(end != null) {
					endDate = new Date(end);
				}
				validateActivityDates(start, end);
				return getActivityEventsForATLs(showDeleted, id, startDate, endDate);
			}
		}
	}

	@ApiOperation(value="Get auditable data for all API keys",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return API key activity across all dates.")
	@RequestMapping(value="/api_keys", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForApiKeys(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end) throws JsonParseException, IOException, ValidationException {

		if (start == null && end == null) {
			return getActivityEventsForApiKeys();
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForApiKeys(startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for certified products",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return certified product activity across all dates.")
	@RequestMapping(value="/certified_products", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForCertifiedProducts(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end) throws JsonParseException, IOException, ValidationException {

		if (start == null && end == null) {
			return getActivityEventsForCertifiedProducts();
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForCertifiedProducts(startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for a specific certified product",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return activity for the specified certified product across all dates.")
	@RequestMapping(value="/certified_products/ {id}", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForCertifiedProductById(@PathVariable("id") Long id,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end)
			throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
		cpManager.getById(id); //throws 404 if bad id

		if (start == null && end == null) {
			return getActivityEventsForCertifiedProducts(id);
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForCertifiedProducts(id, startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for all certifications",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return activity for all certifications across all dates.")
	@RequestMapping(value="/certifications", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForCertifications(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end) throws JsonParseException, IOException, ValidationException {

		if (start == null && end == null) {
			return getActivityEventsForCertifications();
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForCertifications(startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for a specific certification",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return activity for the specified certification across all dates.")
	@RequestMapping(value="/certifications/ {id}", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForCertificationById(@PathVariable("id") Long id,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end)
			throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
		certificationIdManager.getById(id); //throws 404 if bad id

		if (start == null && end == null) {
			return getActivityEventsForCertifications(id);
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForCertifications(id, startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for all pending certified products",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return activity for all pending certified products across all dates.")
	@RequestMapping(value="/pending_certified_products", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForPendingCertifiedProducts(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end) throws JsonParseException, IOException, ValidationException {

		if (start == null && end == null) {
			return getActivityEventsForPendingCertifiedProducts();
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForPendingCertifiedProducts(startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for a specific pending certified product",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return activity for the specified pending certified product across all dates.")
	@RequestMapping(value="/pending_certified_products/ {id}", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForPendingCertifiedProductById(@PathVariable("id") Long id,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end)
			throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
		List<CertificationBodyDTO> acbs = acbManager.getAllForUser(false);
		pcpManager.getById(acbs, id); //returns 404 if bad id

		if (start == null && end == null) {
			return getActivityEventsForPendingCertifiedProducts(id);
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForPendingCertifiedProducts(id, startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for all products",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return activity for all products across all dates.")
	@RequestMapping(value="/products", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForProducts(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end) throws JsonParseException, IOException, ValidationException {

		if (start == null && end == null) {
			return getActivityEventsForProducts();
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForProducts(startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for a specific product",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return activity for the specified product across all dates.")
	@RequestMapping(value="/products/ {id}", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForProducts(@PathVariable("id") Long id,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end)
			throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
		productManager.getById(id); //returns 404 if bad id

		if (start == null && end == null) {
			return getActivityEventsForProducts(id);
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForProducts(id, startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for all versions",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return activity for all versions across all dates.")
	@RequestMapping(value="/versions", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForVersions(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end) throws JsonParseException, IOException, ValidationException {

		if (start == null && end == null) {
			return getActivityEventsForVersions();
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForVersions(startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for a specific version",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return activity for the specified version across all dates.")
	@RequestMapping(value="/versions/ {id}", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForVersions(@PathVariable("id") Long id,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end)
			throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
		versionManager.getById(id); //returns 404 if bad id

		if (start == null && end == null) {
			return getActivityEventsForVersions(id);
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForVersions(id, startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data about all CHPL user accounts",
			notes="API users can optionally specify to only get activity a certain number of days into the past with the 'lastNDays' parameter. "
				+ "The default behavior is to return activity for all CHPL user across all dates.")
	@RequestMapping(value="/users", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForUsers(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end) throws JsonParseException, IOException, ValidationException {

		if (start == null && end == null) {
			return getActivityEventsForUsers();
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForUsers(startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data about a specific CHPL user account",
			notes="API users can optionally specify to only get activity a certain number of days into the past with the 'lastNDays' parameter. "
				+ "The default behavior is to return activity for the specified CHPL user across all dates.")
	@RequestMapping(value="/users/ {id}", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForUsers(@PathVariable("id") Long id,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end)
			throws JsonParseException, IOException, UserRetrievalException, ValidationException {
		userManager.getById(id); //throws 404 if bad id

		if (start == null && end == null) {
			return getActivityEventsForUsers(id);
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForUsers(id, startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data about all developers",
				notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return the all developer activity across all dates.")
	@RequestMapping(value="/developers", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForDevelopers(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end) throws JsonParseException, IOException, ValidationException {

		if (start == null && end == null) {
			return getActivityEventsForDevelopers();
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForDevelopers(startDate, endDate);
		}
	}

	@ApiOperation(value="Get auditable data for a specific developer",
			notes="Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return activity for the specified developer across all dates.")
	@RequestMapping(value="/developers/ {id}", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityForDeveloperById(@PathVariable("id") Long id,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end)
		throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
		developerManager.getById(id); //returns 404 if bad id

		if (start == null && end == null) {
			return getActivityEventsForDevelopers(id);
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return getActivityEventsForDevelopers(id, startDate, endDate);
		}
	}

	@ApiOperation(value="Track the actions of all users in the system",
			notes="The authenticated user calling this method must have ROLE_ADMIN. "
				+ "Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return the all user activity across all dates.")
	@RequestMapping(value="/user_activities", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<UserActivity> activityByUser(@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end) throws JsonParseException, IOException, UserRetrievalException, ValidationException {

		if (start == null && end == null) {
			return activityManager.getActivityByUser();
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return activityManager.getActivityByUserInDateRange(startDate, endDate);
		}
	}

	@ApiOperation(value="Track the actions of a specific user in the system",
			notes="The authenticated user calling this method must have ROLE_ADMIN. "
				+ "Users can optionally specify 'start' and 'end' parameters to restrict the date range of the results. "
				+ "The default behavior is to return the specified user's activity across all dates.")
	@RequestMapping(value="/user_activities/ {id}", method = RequestMethod.GET, produces="application/json; charset = utf-8")
	public List<ActivityEvent> activityByUser(@PathVariable("id") Long id,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end)
			throws JsonParseException, IOException, UserRetrievalException, ValidationException {
		userManager.getById(id); //throws 404 if bad id

		if (start == null && end == null) {
			return activityManager.getActivityForUser(id);
		} else {
			Date startDate = null;
			Date endDate = null;
			if(start != null) {
				startDate = new Date(start);
			}
			if(end != null) {
				endDate = new Date(end);
			}
			validateActivityDates(start, end);
			return activityManager.getActivityForUserInDateRange(id, startDate, endDate);
		}
	}


	private List<ActivityEvent> getActivityEventsForACBs(boolean showDeleted, Long id) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForObject(showDeleted, concept, id);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForATLs(boolean showDeleted, Long id) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_ATL;
		events = getActivityEventsForObject(showDeleted, concept, id);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForCertifications(Long id) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForObject(false, concept, id);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForCertifiedProducts(Long id) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(false, concept, id);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts(Long id) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(false, concept, id);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForProducts(Long id) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForObject(false, concept, id);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForUsers(Long id) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;

		Set<GrantedPermission> permissions = Util.getCurrentUser().getPermissions();

		// Only return data if the user has ROLE_ADMIN
		Boolean hasAdmin = false;
		for (GrantedPermission permission : permissions) {
			if (permission.getAuthority().equals("ROLE_ADMIN")) {
				hasAdmin = true;
			}
		}
		if (!hasAdmin) {
			LOGGER.warn("Non-admin user " + Util.getUsername() + " to see user activity");
			throw new AccessDeniedException("Insufficient permissions to access User activity.");
		} else {
			events = getActivityEventsForObject(false, concept, id);
		}

		return events;
	}

	private List<ActivityEvent> getActivityEventsForDevelopers(Long id) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER;
		events = getActivityEventsForObject(false, concept, id);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForVersions(Long id) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForObject(false, concept, id);
		return events;

	}

	private List<ActivityEvent> getActivityEventsForACBs(boolean showDeleted, Long id, Date startDate, Date endDate) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForObject(showDeleted, concept, id, startDate, endDate);
		return events;
	}

	private List<ActivityEvent> getActivityEventsForATLs(boolean showDeleted, Long id, Date startDate, Date endDate) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_ATL;
		events = getActivityEventsForObject(showDeleted, concept, id, startDate, endDate);
		return events;
	}

	private List<ActivityEvent> getActivityEventsForCertifications(Long id, Date startDate, Date endDate) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForObject(false, concept, id, startDate, endDate);
		return events;
	}


	private List<ActivityEvent> getActivityEventsForCertifiedProducts(Long id, Date startDate, Date endDate) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(false, concept, id, startDate, endDate);
		return events;
	}

	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts(Long id, Date startDate, Date endDate) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForObject(false, concept, id, startDate, endDate);
		return events;
	}

	private List<ActivityEvent> getActivityEventsForProducts(Long id, Date startDate, Date endDate) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForObject(false, concept, id, startDate, endDate);
		return events;
	}

	private List<ActivityEvent> getActivityEventsForUsers(Long id, Date startDate, Date endDate) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;
		events = getActivityEventsForObject(false, concept, id, startDate, endDate);
		return events;
	}

	private List<ActivityEvent> getActivityEventsForDevelopers(Long id, Date startDate, Date endDate) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER;
		events = getActivityEventsForObject(false, concept, id, startDate, endDate);
		return events;
	}

	private List<ActivityEvent> getActivityEventsForVersions(Long id, Date startDate, Date endDate) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForObject(false, concept, id, startDate, endDate);
		return events;

	}

	private List<ActivityEvent> getActivityEventsForACBs(boolean showDeleted, Date startDate, Date endDate) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested ACB activity between " + startDate + " and " + endDate);

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForConcept(showDeleted, concept, startDate, endDate);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForATLs(boolean showDeleted, Date startDate, Date endDate) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested ATL activity between " + startDate + " and " + endDate);

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_ATL;
		events = getActivityEventsForConcept(showDeleted, concept, startDate, endDate);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForCertifications(Date startDate, Date endDate) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested certification activity between " + startDate + " and " + endDate);

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForConcept(false, concept, startDate, endDate);

		return events;
	}


	private List<ActivityEvent> getActivityEventsForCertifiedProducts(Date startDate, Date endDate) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested certified product activity between " + startDate + " and " + endDate);

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(false, concept, startDate, endDate);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts(Date startDate, Date endDate) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested pending certified product activity between " + startDate + " and " + endDate);

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(false, concept, startDate, endDate);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForProducts(Date startDate, Date endDate) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested product activity between " + startDate + " and " + endDate);

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForConcept(false, concept, startDate, endDate);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForUsers(Date startDate, Date endDate) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested user activity between " + startDate + " and " + endDate);

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;
		events = getActivityEventsForConcept(false, concept, startDate, endDate);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForDevelopers(Date startDate, Date endDate) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested developer activity between " + startDate + " and " + endDate);

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER;
		events = getActivityEventsForConcept(false, concept, startDate, endDate);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForVersions(Date startDate, Date endDate) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested version activity between " + startDate + " and " + endDate);

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForConcept(false, concept, startDate, endDate);
		return events;

	}

	private List<ActivityEvent> getActivityEventsForApiKeys(Date startDate, Date endDate) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested API key activity between " + startDate + " and " + endDate);

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_API_KEY;
		events = getActivityEventsForConcept(false, concept, startDate, endDate);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForACBs(boolean showDeleted) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all ACB activity");

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION_BODY;
		events = getActivityEventsForConcept(showDeleted, concept);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForATLs(boolean showDeleted) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all ATL activity");

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_ATL;
		events = getActivityEventsForConcept(showDeleted, concept);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForCertifications() throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all certification activity");

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATION;
		events = getActivityEventsForConcept(false, concept);

		return events;
	}


	private List<ActivityEvent> getActivityEventsForCertifiedProducts() throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all certified product activity");

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(false, concept);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForPendingCertifiedProducts() throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all pending certified product activity");

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT;
		events = getActivityEventsForConcept(false, concept);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForProducts() throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all product activity");

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_PRODUCT;
		events = getActivityEventsForConcept(false, concept);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForUsers() throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all user activity");

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_USER;
		events = getActivityEventsForConcept(false, concept);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForDevelopers() throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all developer activity");

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER;
		events = getActivityEventsForConcept(false, concept);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForVersions() throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all version activity");

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_VERSION;
		events = getActivityEventsForConcept(false, concept);
		return events;

	}

	private List<ActivityEvent> getActivityEventsForApiKeys() throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all API key activity");

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_API_KEY;
		events = getActivityEventsForConcept(false, concept);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForAnnouncements() throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all announcement activity");

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT;
		events = getActivityEventsForConcept(false, concept);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForAnnouncements(Date startDate, Date endDate) throws JsonParseException, IOException {
		LOGGER.info("User " + Util.getUsername() + " requested all announcement activity between " + startDate + " and " + endDate);

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT;
		events = getActivityEventsForConcept(false, concept, startDate, endDate);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForAnnouncements(Long id) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT;
		events = getActivityEventsForObject(false, concept, id);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForAnnouncements(Long id, Date startDate, Date endDate) throws JsonParseException, IOException {

		List<ActivityEvent> events = null;
		ActivityConcept concept = ActivityConcept.ACTIVITY_CONCEPT_ANNOUNCEMENT;
		events = getActivityEventsForObject(false, concept, id, startDate, endDate);

		return events;
	}

	private List<ActivityEvent> getActivityEventsForConcept(boolean showDeleted, ActivityConcept concept) throws JsonParseException, IOException {
		return activityManager.getActivityForConcept(showDeleted, concept);
	}

	private List<ActivityEvent> getActivityEventsForConcept(boolean showDeleted, ActivityConcept concept, Date startDate, Date endDate) throws JsonParseException, IOException {
		return activityManager.getActivityForConcept(showDeleted, concept, startDate, endDate);
	}

	private List<ActivityEvent> getActivityEventsForObject(boolean showDeleted, ActivityConcept concept, Long objectId) throws JsonParseException, IOException {
		return activityManager.getActivityForObject(showDeleted, concept, objectId);
	}

	private List<ActivityEvent> getActivityEventsForObject(boolean showDeleted, ActivityConcept concept, Long objectId, Date startDate, Date endDate) throws JsonParseException, IOException {
		return activityManager.getActivityForObject(showDeleted, concept, objectId, startDate, endDate);
	}

	private void validateActivityDates(Long startDate, Long endDate) throws ValidationException {
		Calendar calendarCounter = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
		Integer maxActivityRangeInDays = Integer.getInteger(env.getProperty("maxActivityRangeInDays"), 60);
		calendarCounter.setTime(new Date(endDate));
		if(new Date(startDate).compareTo(calendarCounter.getTime()) > 0) {
			throw new ValidationException("Cannot search for activity with the start date after the end date");
		}

		calendarCounter.add(Calendar.DATE, -maxActivityRangeInDays);
		if(new Date(startDate).compareTo(calendarCounter.getTime()) < 0) {
			throw new ValidationException("Cannot search for activity with a date range more than " + maxActivityRangeInDays + " days.");
		}
	}

}
