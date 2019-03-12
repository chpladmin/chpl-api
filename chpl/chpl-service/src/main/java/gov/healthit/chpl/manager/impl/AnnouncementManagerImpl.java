package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.AnnouncementDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.AnnouncementManager;

@Service
public class AnnouncementManagerImpl extends ApplicationObjectSupport implements AnnouncementManager {

    @Autowired
    private AnnouncementDAO announcementDAO;

    @Autowired
    private ActivityManager activityManager;

    @Autowired private MessageSource messageSource;

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public AnnouncementDTO create(AnnouncementDTO announcement)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
        // Create the announcement itself
        AnnouncementDTO result = announcementDAO.create(announcement);

        String activityMsg = "Created announcement: " + announcement.getTitle();
        activityManager.addActivity(ActivityConcept.ANNOUNCEMENT, result.getId(), activityMsg, null,
                result);

        return result;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public AnnouncementDTO update(AnnouncementDTO announcement)
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public void delete(AnnouncementDTO announcement)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        // mark the announcement deleted
        announcementDAO.delete(announcement.getId());
        // log announcement delete activity
        String activityMsg = "Deleted announcement: " + announcement.getTitle();
        activityManager.addActivity(ActivityConcept.ANNOUNCEMENT, announcement.getId(), activityMsg,
                announcement, null);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDTO> getAll() {
        boolean isLoggedIn = Util.getCurrentUser() == null ? false : true;
        return announcementDAO.findAll(isLoggedIn);
    }

    @Transactional(readOnly = true)
    public AnnouncementDTO getById(Long id) throws EntityRetrievalException, AccessDeniedException {
        return getById(id, false);
    }

    @Transactional(readOnly = true)
    public AnnouncementDTO getById(Long id, boolean includeDeleted)
            throws EntityRetrievalException, AccessDeniedException {
        AnnouncementDTO result =  announcementDAO.getById(id, includeDeleted);
        boolean isLoggedIn = Util.getCurrentUser() == null ? false : true;
        if (!result.getIsPublic().booleanValue() && !isLoggedIn) {
            String msg = String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("announcement.accessDenied"),
                    LocaleContextHolder.getLocale()), id);
            throw new AccessDeniedException(msg);
        }
        return result;
    }

    public void setAnnouncementDAO(final AnnouncementDAO announcementDAO) {
        this.announcementDAO = announcementDAO;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<AnnouncementDTO> getAllFuture() {
        return announcementDAO.findAllFuture();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<AnnouncementDTO> getAllCurrentAndFuture() {
        return announcementDAO.findAllCurrentAndFuture();
    }
}
