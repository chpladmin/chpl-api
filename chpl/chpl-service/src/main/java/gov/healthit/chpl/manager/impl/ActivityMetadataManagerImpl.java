package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;

import gov.healthit.chpl.activity.ListingActivityMetadataBuilder;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.ActivityMetadataManager;
import gov.healthit.chpl.permissions.Permissions;

@Service("activityMetadataManager")
public class ActivityMetadataManagerImpl implements ActivityMetadataManager {
    private static final Logger LOGGER = LogManager.getLogger(ActivityMetadataManagerImpl.class);

    private ActivityDAO activityDAO;
    private ListingActivityMetadataBuilder listingActivityBuilder;
    private Permissions permissions;

    @Autowired
    public ActivityMetadataManagerImpl(final ActivityDAO activityDAO, final Permissions permissions) {
        this.activityDAO = activityDAO;
        this.permissions = permissions;
        listingActivityBuilder = new ListingActivityMetadataBuilder();
    }

    @Transactional
    public List<ActivityMetadata> getListingActivityMetadata(final Date startDate, final Date endDate)
            throws JsonParseException, IOException {

        LOGGER.info("Getting listing activity from " + startDate + " through " + endDate);
        //get the activity
        List<ActivityDTO> activityDtos = activityDAO.findByConcept(ActivityConcept.CERTIFIED_PRODUCT,
                startDate, endDate);
        //convert to domain object
        List<ActivityMetadata> activityMetas = new ArrayList<ActivityMetadata>();
        for (ActivityDTO dto : activityDtos) {
            ActivityMetadata activityMeta = listingActivityBuilder.build(dto);
            activityMetas.add(activityMeta);
        }
        return activityMetas;
    }

    @Transactional
    public List<ActivityMetadata> getListingActivityMetadata(final Long listingId, final Date startDate, final Date endDate)
            throws JsonParseException, IOException {

        LOGGER.info("Getting activity for listing " + listingId + " from " + startDate + " through " + endDate);
        //get the activity
        List<ActivityDTO> activityDtos = activityDAO.findByObjectId(listingId, ActivityConcept.CERTIFIED_PRODUCT,
                startDate, endDate);
        //convert to domain object
        List<ActivityMetadata> activityMetas = new ArrayList<ActivityMetadata>();
        for (ActivityDTO dto : activityDtos) {
            ActivityMetadata activityMeta = listingActivityBuilder.build(dto);
            activityMetas.add(activityMeta);
        }
        return activityMetas;
    }
}
