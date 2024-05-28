package gov.healthit.chpl.fuzzyMatching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import gov.healthit.chpl.accessibilityStandard.AccessibilityStandardDAO;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.optionalStandard.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.qmsStandard.QmsStandardDAO;
import gov.healthit.chpl.ucdProcess.UcdProcessDAO;
import lombok.extern.log4j.Log4j2;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

@Service
@Log4j2
public class FuzzyChoicesManager extends SecuredManager {

    private UcdProcessDAO ucdProcessDao;
    private AccessibilityStandardDAO accessibilityStandardDao;
    private QmsStandardDAO qmsStandardDao;
    private OptionalStandardDAO optionalStandardDao;
    private Environment env;

    @Autowired
    public FuzzyChoicesManager(UcdProcessDAO ucdProcessDao,
            AccessibilityStandardDAO accessibilityStandardDao,
            QmsStandardDAO qmsStandardDao,
            OptionalStandardDAO optionalStandardDao,
            Environment env) {
        this.ucdProcessDao = ucdProcessDao;
        this.accessibilityStandardDao = accessibilityStandardDao;
        this.qmsStandardDao = qmsStandardDao;
        this.optionalStandardDao = optionalStandardDao;
        this.env = env;
    }

    public String getTopFuzzyChoice(String query, FuzzyType type) {
        int limit = Integer.parseInt(env.getProperty("fuzzyChoiceLimit"));
        int cutoff = Integer.parseInt(env.getProperty("fuzzyChoiceThreshold"));
        List<ExtractedResult> results = null;
        try {
            results = FuzzySearch.extractTop(query, getFuzzyChoices(type), limit, cutoff);
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

    public String getTopFuzzyChoice(String query, FuzzyType type, CertificationCriterion criterion) {
        int limit = Integer.parseInt(env.getProperty("fuzzyChoiceLimit"));
        int cutoff = Integer.parseInt(env.getProperty("fuzzyChoiceThreshold"));
        List<ExtractedResult> results = null;
        try {
            results = FuzzySearch.extractTop(query,
                    getFuzzyChoices(type, criterion.getId()),
                    limit, cutoff);
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

    private List<String> getFuzzyChoices(FuzzyType type)
            throws JsonParseException, JsonMappingException, EntityRetrievalException, IOException {
        if (type.equals(FuzzyType.UCD_PROCESS)) {
            return ucdProcessDao.getAll().stream()
                .map(ucdProcess -> ucdProcess.getName())
                .toList();
        } else if (type.equals(FuzzyType.ACCESSIBILITY_STANDARD)) {
            return accessibilityStandardDao.getAll().stream()
                    .map(accStd -> accStd.getName())
                    .toList();
        } else if (type.equals(FuzzyType.QMS_STANDARD)) {
            return qmsStandardDao.getAll().stream()
                    .map(qmsStd -> qmsStd.getName())
                    .toList();
        }
        return new ArrayList<String>();
    }

    private List<String> getFuzzyChoices(FuzzyType type, Long criterionId)
            throws JsonParseException, JsonMappingException, EntityRetrievalException, IOException {
        if (type.equals(FuzzyType.OPTIONAL_STANDARD)) {
            List<OptionalStandardCriteriaMap> oscms = optionalStandardDao.getAllOptionalStandardCriteriaMap();
            return oscms.stream()
                .filter(oscm -> oscm.getCriterion().getId().equals(criterionId))
                .map(oscm -> oscm.getOptionalStandard().getDisplayValue())
                .toList();
        }
        return new ArrayList<String>();
    }
}
