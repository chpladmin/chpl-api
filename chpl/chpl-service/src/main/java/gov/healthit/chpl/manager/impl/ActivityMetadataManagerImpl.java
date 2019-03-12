package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonFactoryBean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.activity.ListingActivityMetadataBuilder;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ListingActivityMetadata;
import gov.healthit.chpl.domain.activity.ProductActivityEvent;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.ActivityMetadataManager;
import gov.healthit.chpl.permissions.Permissions;
import gov.healthit.chpl.util.JSONUtils;

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
}
