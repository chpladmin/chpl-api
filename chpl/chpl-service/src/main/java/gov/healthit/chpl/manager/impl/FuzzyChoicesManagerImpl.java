package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.List;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.AnnouncementDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.FuzzyChoicesDAO;
import gov.healthit.chpl.dao.PendingCertifiedProductSystemUpdateDAO;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductSystemUpdateDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductSystemUpdateEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.AnnouncementManager;
import gov.healthit.chpl.manager.FuzzyChoicesManager;

@Service
public class FuzzyChoicesManagerImpl extends ApplicationObjectSupport implements FuzzyChoicesManager {

    @Autowired
    private FuzzyChoicesDAO fuzzyChoicesDao;
    @Autowired
    private PendingCertifiedProductSystemUpdateDAO systemUpdateDao;
    private int limit = 1;
    private int cutoff = 80;
    
    public String getTopFuzzyChoice(String query, FuzzyType type, Long pendingCertId){
    	List<ExtractedResult> results = null;
		try {
			results = FuzzySearch.extractTop(query, getFuzzyChoicesByType(type), limit, cutoff);
		} catch (EntityRetrievalException | IOException e) {
			e.printStackTrace();
		}
    	for(ExtractedResult er : results){
    		String result = er.getString();
    		PendingCertifiedProductSystemUpdateEntity entity = new PendingCertifiedProductSystemUpdateEntity();
    		entity.setChangeMade("Changed " + type.toString() + " name from " + query + " to " + er.getString());
    		entity.setPendingCertifiedProductId(pendingCertId);
    		PendingCertifiedProductSystemUpdateDTO dto = new PendingCertifiedProductSystemUpdateDTO(entity);
    		try {
				systemUpdateDao.create(dto);
			} catch (EntityRetrievalException | EntityCreationException e) {
				e.printStackTrace();
			}
    		return result;
    	}
    	return null;
    }
    
    public List<String> getFuzzyChoicesByType(FuzzyType type) throws JsonParseException, JsonMappingException, EntityRetrievalException, IOException{
    	FuzzyChoicesDTO choices = getByType(type);
    	return choices.getChoices();
    }

    @Transactional(readOnly = true)
    public FuzzyChoicesDTO getByType(FuzzyType type) throws EntityRetrievalException, JsonParseException, JsonMappingException, IOException {
        return fuzzyChoicesDao.getByType(type);
    }

    public void setFuzzyChoicesDAO(final FuzzyChoicesDAO FuzzyChoicesDAO) {
        this.fuzzyChoicesDao = FuzzyChoicesDAO;
    }
}
