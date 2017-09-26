package gov.healthit.chpl.manager;

import java.util.Set;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.SurveillanceRequirementOptions;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.domain.notification.NotificationType;

public interface SearchMenuManager {
	public Set<NotificationType> getNotificationTypes();
	public Set<KeyValueModel> getJobTypes();
	public Set<KeyValueModel> getClassificationNames();
	public Set<KeyValueModel> getEditionNames(Boolean simple);
	public Set<KeyValueModel> getCertificationStatuses();
	public Set<KeyValueModel> getPracticeTypeNames();
	public Set<KeyValueModelStatuses> getProductNames();
	public Set<KeyValueModelStatuses> getDeveloperNames();
	public Set<KeyValueModel> getCertBodyNames(Boolean showDeleted);
	public Set<KeyValueModel> getAccessibilityStandards();
	public Set<KeyValueModel> getUcdProcesses();
	public Set<KeyValueModel> getQmsStandards();
	public Set<KeyValueModel> getTargetedUesrs();
	public Set<KeyValueModel> getEducationTypes();
	public Set<KeyValueModel> getAgeRanges();
	public Set<TestFunctionality> getTestFunctionality();
	public Set<TestStandard> getTestStandards();
	public Set<KeyValueModel> getTestTools();
	public Set<KeyValueModel> getDeveloperStatuses();
	public Set<KeyValueModel> getSurveillanceTypes();
	public Set<KeyValueModel> getSurveillanceRequirementTypes();
	public Set<KeyValueModel> getSurveillanceResultTypes();
	public Set<KeyValueModel> getNonconformityStatusTypes();
	public SurveillanceRequirementOptions getSurveillanceRequirementOptions();
	public Set<KeyValueModel> getNonconformityTypeOptions();
	public Set<CriteriaSpecificDescriptiveModel> getMacraMeasures();
	public Set<DescriptiveModel> getCertificationCriterionNumbers(Boolean simple) throws EntityRetrievalException;
	public Set<CertificationCriterion> getCertificationCriterion();
	public Set<DescriptiveModel> getCQMCriterionNumbers(Boolean simple);
	
}
