package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import gov.healthit.chpl.dao.FuzzyChoicesDAO;
import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.FuzzyChoicesManager;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

@Service
public class FuzzyChoicesManagerImpl extends ApplicationObjectSupport implements FuzzyChoicesManager {

    @Autowired
    private FuzzyChoicesDAO fuzzyChoicesDao;
    @Autowired
    private Environment env;

    public String getTopFuzzyChoice(String query, FuzzyType type) {
        int limit = Integer.parseInt(env.getProperty("fuzzyChoiceLimit"));
        int cutoff = Integer.parseInt(env.getProperty("fuzzyChoiceThreshold"));
        List<ExtractedResult> results = null;
        try {
            results = FuzzySearch.extractTop(query, getFuzzyChoicesByType(type), limit, cutoff);
        } catch (EntityRetrievalException | IOException e) {
            e.printStackTrace();
        }
        String result = null;
        if (results != null) {
            for (ExtractedResult er : results) {
                result = er.getString();
            }
        }
        return result;
    }

    public List<String> getFuzzyChoicesByType(FuzzyType type)
            throws JsonParseException, JsonMappingException, EntityRetrievalException, IOException {
        FuzzyChoicesDTO choices = getByType(type);
        return choices.getChoices();
    }

    @Transactional(readOnly = true)
    public FuzzyChoicesDTO getByType(FuzzyType type)
            throws EntityRetrievalException, JsonParseException, JsonMappingException, IOException {
        return fuzzyChoicesDao.getByType(type);
    }

    public void setFuzzyChoicesDAO(final FuzzyChoicesDAO FuzzyChoicesDAO) {
        this.fuzzyChoicesDao = FuzzyChoicesDAO;
    }
}
