package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.AnnouncementDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;

@Service
public class AnnouncementManager extends ApplicationObjectSupport {

    @Autowired
    private AnnouncementDAO announcementDAO;

    @Autowired
    private ActivityManager activityManager;

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ANNOUNCEMENT, "
            + "T(gov.healthit.chpl.permissions.domains.AnnouncementDomainPermissions).CREATE)")
    public AnnouncementDTO create(final AnnouncementDTO announcement)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
        // Create the announcement itself
        AnnouncementDTO result = announcementDAO.create(announcement);

        String activityMsg = "Created announcement: " + announcement.getTitle();
        activityManager.addActivity(ActivityConcept.ANNOUNCEMENT, result.getId(), activityMsg, null,
                result);

        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ANNOUNCEMENT, "
            + "T(gov.healthit.chpl.permissions.domains.AnnouncementDomainPermissions).UPDATE)")
    public AnnouncementDTO update(final AnnouncementDTO announcement)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {

        AnnouncementDTO result = null;
        AnnouncementDTO toUpdate = announcementDAO.getByIdToUpdate(announcement.getId(), false);

        result = announcementDAO.update(announcement, false);

        String activityMsg = "Updated announcement: " + announcement.getTitle();
        activityManager.addActivity(ActivityConcept.ANNOUNCEMENT, result.getId(), activityMsg,
                toUpdate, result);

        return result;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ANNOUNCEMENT, "
            + "T(gov.healthit.chpl.permissions.domains.AnnouncementDomainPermissions).DELETE)")
    public void delete(final AnnouncementDTO announcement)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        // mark the announcement deleted
        announcementDAO.delete(announcement.getId());
        // log announcement delete activity
        String activityMsg = "Deleted announcement: " + announcement.getTitle();
        activityManager.addActivity(ActivityConcept.ANNOUNCEMENT, announcement.getId(), activityMsg,
                announcement, null);
    }

    @Transactional(readOnly = true)
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ANNOUNCEMENT, "
            + "T(gov.healthit.chpl.permissions.domains.AnnouncementDomainPermissions).GET_ALL, filterObject)")
    public List<AnnouncementDTO> getAll() {
        return announcementDAO.findCurrent();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ANNOUNCEMENT, "
            + "T(gov.healthit.chpl.permissions.domains.AnnouncementDomainPermissions).GET_BY_ID, #id)")
    public AnnouncementDTO getById(final Long id) throws EntityRetrievalException, AccessDeniedException {
        return getById(id, false);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ANNOUNCEMENT, "
            + "T(gov.healthit.chpl.permissions.domains.AnnouncementDomainPermissions).GET_BY_ID, #id)")
    public AnnouncementDTO getById(final Long id, final boolean includeDeleted)
            throws EntityRetrievalException, AccessDeniedException {
        return announcementDAO.getById(id, includeDeleted);
    }

    public void setAnnouncementDAO(final AnnouncementDAO announcementDAO) {
        this.announcementDAO = announcementDAO;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ANNOUNCEMENT, "
            + "T(gov.healthit.chpl.permissions.domains.AnnouncementDomainPermissions).GET_ALL_INCLUDING_FUTURE)")
    public List<AnnouncementDTO> getAllFuture() {
        return announcementDAO.findAllFuture();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ANNOUNCEMENT, "
            + "T(gov.healthit.chpl.permissions.domains.AnnouncementDomainPermissions).GET_ALL_INCLUDING_FUTURE)")
    public List<AnnouncementDTO> getAllCurrentAndFuture() {
        return announcementDAO.findAllCurrentAndFuture();
    }
}
