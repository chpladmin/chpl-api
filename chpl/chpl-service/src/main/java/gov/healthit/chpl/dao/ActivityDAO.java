package gov.healthit.chpl.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Interface for database access to activity tables.
 * @author kekey
 *
 */
public interface ActivityDAO {

    ActivityDTO create(ActivityDTO dto) throws EntityCreationException, EntityRetrievalException;

    ActivityDTO getById(Long id) throws EntityRetrievalException;

    List<ActivityDTO> findByObjectId(Long objectId, ActivityConcept concept, Date startDate,
            Date endDate);

    List<ActivityDTO> findPublicAnnouncementActivity(Date startDate, Date endDate);

    List<ActivityDTO> findPublicAnnouncementActivityById(Long announcementId, Date startDate, Date endDate);

    List<ActivityDTO> findAcbActivity(List<CertificationBodyDTO> acbs, Date startDate, Date endDate);

    List<ActivityDTO> findAtlActivity(List<TestingLabDTO> atls, Date startDate, Date endDate);

    List<ActivityDTO> findPendingListingActivity(List<CertificationBodyDTO> pendingListingAcbs,
            Date startDate, Date endDate);

    List<ActivityDTO> findPendingListingActivity(Long pendingListingId,
            Date startDate, Date endDate);

    List<ActivityDTO> findUserActivity(List<Long> userIds, Date startDate, Date endDate);

    List<ActivityDTO> findByConcept(ActivityConcept concept, Date startDate, Date endDate);

    List<ActivityDTO> findByUserId(Long userId, Date startDate, Date endDate);

    Map<Long, List<ActivityDTO>> findAllByUserInDateRange(Date startDate, Date endDate);

}
