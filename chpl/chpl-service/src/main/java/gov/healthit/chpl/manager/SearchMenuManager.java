package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.FuzzyChoices;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.SurveillanceRequirementOptions;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.domain.UploadTemplateVersion;

public interface SearchMenuManager {

    Set<KeyValueModel> getJobTypes();

    Set<FuzzyChoices> getFuzzyChoices() throws EntityRetrievalException, JsonParseException, JsonMappingException, IOException;

    FuzzyChoices updateFuzzyChoices(FuzzyChoicesDTO fuzzyChoicesDTO) throws EntityRetrievalException, JsonProcessingException, EntityCreationException, IOException;

    Set<KeyValueModel> getClassificationNames();

    Set<KeyValueModel> getEditionNames(Boolean simple);

    Set<KeyValueModel> getCertificationStatuses();

    Set<KeyValueModel> getPracticeTypeNames();

    Set<KeyValueModelStatuses> getProductNames();

    Set<KeyValueModelStatuses> getDeveloperNames();

    Set<KeyValueModel> getCertBodyNames();

    Set<KeyValueModel> getAccessibilityStandards();

    Set<KeyValueModel> getUcdProcesses();

    Set<KeyValueModel> getQmsStandards();

    Set<KeyValueModel> getTargetedUesrs();

    Set<KeyValueModel> getEducationTypes();

    Set<KeyValueModel> getAgeRanges();

    Set<TestFunctionality> getTestFunctionality();

    Set<TestStandard> getTestStandards();

    Set<KeyValueModel> getTestTools();
    Set<CriteriaSpecificDescriptiveModel> getTestProcedures();
    Set<CriteriaSpecificDescriptiveModel> getTestData();

    Set<KeyValueModel> getDeveloperStatuses();

    Set<KeyValueModel> getSurveillanceTypes();

    Set<KeyValueModel> getSurveillanceRequirementTypes();

    Set<KeyValueModel> getSurveillanceResultTypes();

    Set<KeyValueModel> getNonconformityStatusTypes();

    SurveillanceRequirementOptions getSurveillanceRequirementOptions();

    Set<KeyValueModel> getNonconformityTypeOptions();

    Set<UploadTemplateVersion> getUploadTemplateVersions();

    Set<CriteriaSpecificDescriptiveModel> getMacraMeasures();

    Set<DescriptiveModel> getCertificationCriterionNumbers(Boolean simple) throws EntityRetrievalException;

    Set<CertificationCriterion> getCertificationCriterion();

    Set<DescriptiveModel> getCQMCriterionNumbers(Boolean simple);

}
