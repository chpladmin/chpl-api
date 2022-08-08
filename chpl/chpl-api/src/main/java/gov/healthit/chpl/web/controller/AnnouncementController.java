package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.Announcement;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.AnnouncementManager;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.CacheControl;
import gov.healthit.chpl.web.controller.annotation.CacheMaxAge;
import gov.healthit.chpl.web.controller.annotation.CachePolicy;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
import gov.healthit.chpl.web.controller.results.AnnouncementResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "announcements", description = "Allows CRUD operations on announcements.")
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementManager announcementManager;

    @Operation(summary = "Get all announcements.",
            description = "Security Restrictions: ROLE_ADMIN and ROLE_ONC can retrieve future scheduled announcements "
                    + "and private announcements.  ROLE_ACB, ROLE_ATL, and ROLE_CMS_STAFF can retrieve private "
                    + "announcements.  All users can retrieve public announcements.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @DeprecatedApiResponseFields(responseClass = AnnouncementResults.class)
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.FOUR_HOURS)
    public @ResponseBody AnnouncementResults getAnnouncements(
            @RequestParam(required = false, defaultValue = "false") boolean future) {
        AnnouncementResults results = new AnnouncementResults();
        List<Announcement> announcements = null;
        if (!future) {
            announcements = announcementManager.getAll();
        } else {
            announcements = announcementManager.getAllCurrentAndFuture();
        }
        if (!CollectionUtils.isEmpty(announcements)) {
            results.setAnnouncements(announcements);
        }
        return results;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/announcements/{announcementId}",
        removalDate = "2022-10-15",
        message = "This endpoint is deprecated and will be removed in a future release.")
    @Operation(summary = "Get a specific announcement.",
            description = "Security Restrictions: ROLE_ADMIN and ROLE_ONC can retrieve future scheduled "
                    + "announcements and private announcements.  ROLE_ACB, ROLE_ATL, and ROLE_CMS_STAFF "
                    + "can retrieve private announcements.  All users can retrieve public announcements.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{announcementId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.FOUR_HOURS)
    public @ResponseBody Announcement getAnnouncementById(@PathVariable("announcementId") Long announcementId)
            throws EntityRetrievalException {
        Announcement announcement = announcementManager.getById(announcementId);

        return announcement;
    }

    @Operation(summary = "Create a new announcement.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ONC",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @DeprecatedApiResponseFields(responseClass = Announcement.class)
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public Announcement create(@RequestBody Announcement announcement) throws InvalidArgumentsException,
            UserRetrievalException, EntityRetrievalException, EntityCreationException, JsonProcessingException {

        if (!StringUtils.hasText(announcement.getTitle())) {
            throw new InvalidArgumentsException("A title is required for a new announcement");
        }
        if (announcement.getStartDateTime() == null) {
            throw new InvalidArgumentsException("A start date is required for a new announcement");
        }
        if (announcement.getEndDateTime() == null) {
            throw new InvalidArgumentsException("An end date is required for a new announcement");
        }
        Announcement toCreate = Announcement.builder()
                .title(announcement.getTitle())
                .text(announcement.getText())
                .startDateTime(announcement.getStartDateTime())
                .endDateTime(announcement.getEndDateTime())
                .isPublic(announcement.getIsPublic())
                .build();

        Announcement result = announcementManager.create(toCreate);
        return result;
    }

    @Operation(summary = "Change an existing announcement.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ONC",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @DeprecatedApiResponseFields(responseClass = Announcement.class)
    @RequestMapping(value = "/{announcementId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public Announcement updateAnnouncement(@PathVariable("announcementId") Long announcementId, @RequestBody Announcement announcement)
            throws InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateCertifiedBodyException {

        if (!StringUtils.hasText(announcement.getTitle())) {
            throw new InvalidArgumentsException("A title is required when editing an announcement");
        }
        if (announcement.getStartDateTime() == null) {
            throw new InvalidArgumentsException("A start date is required when editing an announcement");
        }
        if (announcement.getEndDateTime() == null) {
            throw new InvalidArgumentsException("An end date is required when editing an announcement");
        }
        Announcement toUpdate = Announcement.builder()
                .id(announcementId)
                .title(announcement.getTitle())
                .text(announcement.getText())
                .startDateTime(announcement.getStartDateTime())
                .endDateTime(announcement.getEndDateTime())
                .isPublic(announcement.getIsPublic())
                .build();
        Announcement result = announcementManager.update(toUpdate);
        return result;
    }

    @Operation(summary = "Delete an existing announcement.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ONC",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{announcementId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public String deleteAnnouncement(@PathVariable("announcementId") Long announcementId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        Announcement toDelete = announcementManager.getById(announcementId, false);
        announcementManager.delete(toDelete);
        return "{\"deletedAnnouncement\" : true}";
    }
}
