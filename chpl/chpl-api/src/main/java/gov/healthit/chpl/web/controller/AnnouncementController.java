package gov.healthit.chpl.web.controller;

import java.util.List;

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
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.FOUR_HOURS)
    public @ResponseBody AnnouncementResults getAnnouncements(
            @RequestParam(required = false, defaultValue = "false") final boolean future) {
        AnnouncementResults results = new AnnouncementResults();
        List<Announcement> announcements = null;
        if (!future) {
            announcements = announcementManager.getAll();
        } else {
            announcements = announcementManager.getAllCurrentAndFuture();
        }
        if (announcements != null) {
            for (Announcement announcement : announcements) {
                results.getAnnouncements().add(announcement);
            }
        }
        return results;
    }

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
    public @ResponseBody Announcement getAnnouncementById(@PathVariable("announcementId") final Long announcementId)
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
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public Announcement create(@RequestBody final Announcement announcementInfo) throws InvalidArgumentsException,
            UserRetrievalException, EntityRetrievalException, EntityCreationException, JsonProcessingException {

        return createAnnouncement(announcementInfo);
    }

    private Announcement createAnnouncement(final Announcement announcementInfo) throws InvalidArgumentsException,
            UserRetrievalException, EntityRetrievalException, EntityCreationException, JsonProcessingException {

        Announcement toCreate = new Announcement();
        if (StringUtils.isEmpty(announcementInfo.getTitle())) {
            throw new InvalidArgumentsException("A title is required for a new announcement");
        } else {
            toCreate.setTitle(announcementInfo.getTitle());
        }
        toCreate.setText(announcementInfo.getText());
        if (StringUtils.isEmpty(announcementInfo.getStartDate())) {
            throw new InvalidArgumentsException("A start date is required for a new announcement");
        } else {
            toCreate.setStartDate(announcementInfo.getStartDate());
        }
        if (StringUtils.isEmpty(announcementInfo.getEndDate())) {
            throw new InvalidArgumentsException("An end date is required for a new announcement");
        } else {
            toCreate.setEndDate(announcementInfo.getEndDate());
        }
        toCreate.setIsPublic(announcementInfo.getIsPublic() != null ? announcementInfo.getIsPublic() : Boolean.FALSE);
        toCreate = announcementManager.create(toCreate);
        return toCreate;
    }

    @Operation(summary = "Change an existing announcement.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ONC",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{announcementId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public Announcement updateAnnouncement(@RequestBody final Announcement announcementInfo)
            throws InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateCertifiedBodyException {

        return update(announcementInfo);
    }

    private Announcement update(final Announcement announcementInfo) throws InvalidArgumentsException,
            EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException {
        Announcement toUpdate = new Announcement();
        toUpdate.setId(announcementInfo.getId());
        toUpdate.setTitle(announcementInfo.getTitle());
        toUpdate.setText(announcementInfo.getText());
        toUpdate.setIsPublic(announcementInfo.getIsPublic());
        toUpdate.setStartDate(announcementInfo.getStartDate());
        toUpdate.setEndDate(announcementInfo.getEndDate());

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
    public String deleteAnnouncement(@PathVariable("announcementId") final Long announcementId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        return delete(announcementId);
    }

    private String delete(final Long announcementId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        Announcement toDelete = announcementManager.getById(announcementId, false);
        announcementManager.delete(toDelete);
        return "{\"deletedAnnouncement\" : true}";
    }
}
