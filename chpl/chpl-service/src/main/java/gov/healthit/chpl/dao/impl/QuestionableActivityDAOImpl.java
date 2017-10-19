package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityCertificationResultEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityDeveloperEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityListingEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityProductEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityTriggerEntity;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityVersionEntity;

@Repository("testToolDAO")
public class QuestionableActivityDAOImpl extends BaseDAOImpl implements QuestionableActivityDAO {
    private static final Logger LOGGER = LogManager.getLogger(QuestionableActivityDAOImpl.class);
    
    @Override
    public QuestionableActivityDTO create(QuestionableActivityDTO dto) {
        QuestionableActivityDTO created = null;
        QuestionableActivityEntity toCreate = null;
        if(dto instanceof QuestionableActivityVersionDTO) {
            toCreate = new QuestionableActivityVersionEntity();
            QuestionableActivityVersionEntity versionActivity = (QuestionableActivityVersionEntity)toCreate;
            versionActivity.setVersionId(((QuestionableActivityVersionDTO)dto).getVersionId());
        } else if(dto instanceof QuestionableActivityProductDTO) {
            toCreate = new QuestionableActivityProductEntity();
            QuestionableActivityProductEntity productActivity = (QuestionableActivityProductEntity)toCreate;
            productActivity.setProductId(((QuestionableActivityProductDTO)dto).getProductId());
        } else if(dto instanceof QuestionableActivityDeveloperDTO) {
            toCreate = new QuestionableActivityDeveloperEntity();
            QuestionableActivityDeveloperEntity developerActivity = (QuestionableActivityDeveloperEntity)toCreate;
            developerActivity.setDeveloperId(((QuestionableActivityDeveloperDTO)dto).getDeveloperId());
        } else if(dto instanceof QuestionableActivityListingDTO) {
            toCreate = new QuestionableActivityListingEntity();
            QuestionableActivityListingEntity listingActivity = (QuestionableActivityListingEntity)toCreate;
            listingActivity.setListingId(((QuestionableActivityListingDTO)dto).getListingId());
        } else if(dto instanceof QuestionableActivityCertificationResultDTO) {
            toCreate = new QuestionableActivityCertificationResultEntity();
            QuestionableActivityCertificationResultEntity certResultActivity = (QuestionableActivityCertificationResultEntity)toCreate;
            certResultActivity.setCertResultId(((QuestionableActivityCertificationResultDTO)dto).getCertResultId());
        } else {
            LOGGER.error("Unknown class of questionable activity passed in: " + dto.getClass().getName());
            return null;
        }
        
        toCreate.setActivityDate(dto.getActivityDate());
        toCreate.setMessage(dto.getMessage());
        toCreate.setTriggerId(dto.getTriggerId());
        toCreate.setUserId(dto.getUserId());
        toCreate.setDeleted(false);
        toCreate.setLastModifiedUser(Util.getCurrentUser().getId());
        entityManager.persist(toCreate);
        entityManager.flush();
        
        if(toCreate instanceof QuestionableActivityVersionEntity) {
            created = new QuestionableActivityVersionDTO((QuestionableActivityVersionEntity)toCreate);
        } else if(toCreate instanceof QuestionableActivityProductEntity) {
            created = new QuestionableActivityProductDTO((QuestionableActivityProductEntity)toCreate);
        } else if(toCreate instanceof QuestionableActivityDeveloperEntity) {
            created = new QuestionableActivityDeveloperDTO((QuestionableActivityDeveloperEntity)toCreate);
        } else if(toCreate instanceof QuestionableActivityListingEntity) {
            created = new QuestionableActivityListingDTO((QuestionableActivityListingEntity)toCreate);
        } else if(toCreate instanceof QuestionableActivityCertificationResultEntity) {
            created = new QuestionableActivityCertificationResultDTO((QuestionableActivityCertificationResultEntity)toCreate);
        }
        return created;
    }

    @Override
    public List<QuestionableActivityTriggerDTO> getAllTriggers() {
        Query query = entityManager.createQuery(
                "SELECT trigger " + 
                "FROM QuestionableActivityTriggerEntity trigger " +
                "WHERE trigger.deleted <> true",
                QuestionableActivityTriggerEntity.class);
        List<QuestionableActivityTriggerEntity> queryResults = query.getResultList();
        List<QuestionableActivityTriggerDTO> results = new ArrayList<QuestionableActivityTriggerDTO>(queryResults.size());
        for(QuestionableActivityTriggerEntity queryResult : queryResults) {
            results.add(new QuestionableActivityTriggerDTO(queryResult));
        }
        return results;
    }
    
    @Override
    public List<QuestionableActivityDTO> findBetweenDates(Date start, Date end) {
        List<QuestionableActivityDTO> result = new ArrayList<QuestionableActivityDTO>();
        //TODO
        return result;

    }
}
