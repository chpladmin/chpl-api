package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;

import gov.healthit.chpl.activity.ActivityMetadataBuilder;
import gov.healthit.chpl.activity.ActivityMetadataBuilderFactory;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityMetadataManager;
import gov.healthit.chpl.permissions.Permissions;

@Service("activityMetadataManager")
public class ActivityMetadataManagerImpl implements ActivityMetadataManager {
    private static final Logger LOGGER = LogManager.getLogger(ActivityMetadataManagerImpl.class);

    private ActivityDAO activityDAO;
    private CertificationBodyDAO acbDao;
    private ActivityMetadataBuilderFactory metadataBuilderFactory;
    private Permissions permissions;

    @Autowired
    public ActivityMetadataManagerImpl(final ActivityDAO activityDAO,
            final CertificationBodyDAO acbDao,
            final ActivityMetadataBuilderFactory metadataBuilderFactory,
            final Permissions permissions) {
        this.activityDAO = activityDAO;
        this.acbDao = acbDao;
        this.permissions = permissions;
        this.metadataBuilderFactory = metadataBuilderFactory;
    }

    @Override
    @Transactional
    public List<ActivityMetadata> getActivityMetadataByConcept(
            final ActivityConcept concept, final Date startDate, final Date endDate)
            throws JsonParseException, IOException {

        LOGGER.info("Getting " + concept.name() + " activity from " + startDate + " through " + endDate);
        //get the activity
        List<ActivityDTO> activityDtos = activityDAO.findByConcept(concept, startDate, endDate);
        List<ActivityMetadata> activityMetas = new ArrayList<ActivityMetadata>();
        ActivityMetadataBuilder builder = null;
        if (activityDtos != null && activityDtos.size() > 0) {
            //excpect all dtos to have the same
            //since we've searched based on activity concept
            builder = metadataBuilderFactory.getBuilder(activityDtos.get(0));
            //convert to domain object
            for (ActivityDTO dto : activityDtos) {
                ActivityMetadata activityMeta = builder.build(dto);
                activityMetas.add(activityMeta);
            }
        }
        return activityMetas;
    }

    @Override
    @Transactional
    public List<ActivityMetadata> getActivityMetadataByObject(
            final Long objectId, final ActivityConcept concept,
            final Date startDate, final Date endDate)
            throws JsonParseException, IOException {

        LOGGER.info("Getting " + concept.name() + " activity for id " + objectId + " from " + startDate + " through " + endDate);
        //get the activity
        List<ActivityDTO> activityDtos = activityDAO.findByObjectId(objectId, concept, startDate, endDate);
        List<ActivityMetadata> activityMetas = new ArrayList<ActivityMetadata>();
        ActivityMetadataBuilder builder = null;
        if (activityDtos != null && activityDtos.size() > 0) {
            //excpect all dtos to have the same
            //since we've searched based on activity concept
            builder = metadataBuilderFactory.getBuilder(activityDtos.get(0));
            //convert to domain object
            for (ActivityDTO dto : activityDtos) {
                ActivityMetadata activityMeta = builder.build(dto);
                activityMetas.add(activityMeta);
            }
        }
        return activityMetas;
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ACB')")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_ACB_METADATA, filterObject)")
    @Transactional
    public List<ActivityMetadata> getCertificationBodyActivityMetadata(final Date startDate, final Date endDate)
            throws JsonParseException, IOException {
        //there is very little ACB activity so just get it all for the date range
        //and apply a post filter to remove whatever the current user should not see.
        return getActivityMetadataByConcept(ActivityConcept.CERTIFICATION_BODY, startDate, endDate);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC') or "
            + "@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).GET_METADATA_BY_ACB, #acbId)")
    @Transactional
    public List<ActivityMetadata> getCertificationBodyActivityMetadata(final Long acbId, final Date startDate, final Date endDate)
            throws EntityRetrievalException, JsonParseException, IOException {
        acbDao.getById(acbId); //throws not found exception for invalid id
        //there is very little ACB activity so just get it all for the date range
        //and apply a post filter to remove whatever the current user should not see.
        return getActivityMetadataByObject(acbId, ActivityConcept.CERTIFICATION_BODY, startDate, endDate);
    }
}
