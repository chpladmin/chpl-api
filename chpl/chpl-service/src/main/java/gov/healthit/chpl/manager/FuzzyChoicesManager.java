package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import gov.healthit.chpl.dao.FuzzyChoicesDAO;
import gov.healthit.chpl.domain.FuzzyChoices;
import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import lombok.extern.log4j.Log4j2;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

@Service
@Log4j2
public class FuzzyChoicesManager extends SecuredManager {

    private FuzzyChoicesDAO fuzzyChoicesDao;
    private Environment env;

    @Autowired
    public FuzzyChoicesManager(FuzzyChoicesDAO fuzzyChoicesDao, Environment env) {
        this.fuzzyChoicesDao = fuzzyChoicesDao;
        this.env = env;
    }

    public String getTopFuzzyChoice(String query, FuzzyType type) {
        int limit = Integer.parseInt(env.getProperty("fuzzyChoiceLimit"));
        int cutoff = Integer.parseInt(env.getProperty("fuzzyChoiceThreshold"));
        List<ExtractedResult> results = null;
        try {
            results = FuzzySearch.extractTop(query, getFuzzyChoicesByType(type), limit, cutoff);
        } catch (EntityRetrievalException | IOException e) {
            LOGGER.error(e.getMessage(), e);
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

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUZZY_MATCH, "
            + "T(gov.healthit.chpl.permissions.domains.FuzzyMatchPermissions).GET_ALL)")
    @Deprecated
    public Set<FuzzyChoices> getFuzzyChoices() throws EntityRetrievalException, JsonParseException,
    JsonMappingException, IOException {
        List<FuzzyChoicesDTO> fuzzyChoices = fuzzyChoicesDao.findAllTypes();
        Set<FuzzyChoices> results = new HashSet<FuzzyChoices>();
        for (FuzzyChoicesDTO dto : fuzzyChoices) {
            results.add(new FuzzyChoices(dto));
        }
        return results;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUZZY_MATCH, "
            + "T(gov.healthit.chpl.permissions.domains.FuzzyMatchPermissions).UPDATE)")
    public FuzzyChoices updateFuzzyChoices(final FuzzyChoicesDTO fuzzyChoicesDTO)
        throws EntityRetrievalException, JsonProcessingException, EntityCreationException, IOException {

        FuzzyChoices result = new FuzzyChoices(fuzzyChoicesDao.update(fuzzyChoicesDTO));
        return result;
    }
}
