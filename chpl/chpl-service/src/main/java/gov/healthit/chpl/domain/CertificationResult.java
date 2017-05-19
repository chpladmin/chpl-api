package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationResult implements Serializable {
	private static final long serialVersionUID = -4917413876078419868L;
	public static final String PRIVACY_SECURITY_FRAMEWORK_DELIMITER = ";";
	
	@XmlElement(required = true)
	private String number;
	
	@XmlElement(required = true)
	private String title;
	
	@XmlElement(required = true)
	private Boolean success;
	
	@XmlElement(required = false, nillable=true)
	private Boolean gap;
	
	@XmlElement(required = false, nillable=true)
	private Boolean sed;
	
	@XmlElement(required = false, nillable=true)
	private Boolean g1Success;
	
	@XmlElement(required = false, nillable=true)
	private Boolean g2Success;
	
	@XmlElement(required = false, nillable=true)
	private String apiDocumentation;
	
	@XmlElement(required = false, nillable=true)
	private String privacySecurityFramework;
	
	@XmlTransient
	private List<MacraMeasure> allowedMacraMeasures;
	
	@XmlElementWrapper(name = "ucdProcesses", nillable = true, required = false)
	@XmlElement(name = "ucdProcess")
	private List<CertificationResultUcdProcess> ucdProcesses;
	
	@XmlElementWrapper(name = "testFunctionalityList", nillable = true, required = false)
	@XmlElement(name = "testFunctionality")
	private List<CertificationResultTestFunctionality> testFunctionality;
	
	@XmlElementWrapper(name = "testProcedures", nillable = true, required = false)
	@XmlElement(name = "testProcedure")
	private List<CertificationResultTestProcedure> testProcedures;
	
	@XmlElementWrapper(name = "testDataList", nillable = true, required = false)
	@XmlElement(name = "testData")
	private List<CertificationResultTestData> testDataUsed;
	
	@XmlElementWrapper(name = "additionalSoftwareList", nillable = true, required = false)
	@XmlElement(name = "additionalSoftware")
	private List<CertificationResultAdditionalSoftware> additionalSoftware;
	
	@XmlElementWrapper(name = "testStandards", nillable = true, required = false)
	@XmlElement(name = "testStandard")
	private List<CertificationResultTestStandard> testStandards;
	
	@XmlElementWrapper(name = "testTools", nillable = true, required = false)
	@XmlElement(name = "testTool")
	private List<CertificationResultTestTool> testToolsUsed;
	
	@XmlElementWrapper(name = "g1MacraMeasures", nillable = true, required = false)
	@XmlElement(name = "macraMeasure")
	private List<MacraMeasure> g1MacraMeasures;
	
	@XmlElementWrapper(name = "g2MacraMeasures", nillable = true, required = false)
	@XmlElement(name = "macraMeasure")
	private List<MacraMeasure> g2MacraMeasures;
	
	@XmlElementWrapper(name = "testTasks", nillable = true, required = false)
	@XmlElement(name = "testTask")
	private List<CertificationResultTestTask> testTasks;
	
	public CertificationResult(){
		allowedMacraMeasures = new ArrayList<MacraMeasure>();
		ucdProcesses = new ArrayList<CertificationResultUcdProcess>();
		 additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
		 testStandards = new ArrayList<CertificationResultTestStandard>();
		 testToolsUsed = new ArrayList<CertificationResultTestTool>();
		 testDataUsed = new ArrayList<CertificationResultTestData>();
		 testProcedures = new ArrayList<CertificationResultTestProcedure>();
		 testFunctionality = new ArrayList<CertificationResultTestFunctionality>();
		 testTasks = new ArrayList<CertificationResultTestTask>();
		 g1MacraMeasures = new ArrayList<MacraMeasure>();
		 g2MacraMeasures = new ArrayList<MacraMeasure>();
	}

	public List<MacraMeasure> getAllowedMacraMeasures() {
		return allowedMacraMeasures;
	}

	public void setAllowedMacraMeasures(List<MacraMeasure> allowedMacraMeasures) {
		this.allowedMacraMeasures = allowedMacraMeasures;
	}

	public CertificationResult(CertificationResultDetailsDTO certResult) {
		this();
		this.setNumber(certResult.getNumber());
		this.setSuccess(certResult.getSuccess());
		this.setTitle(certResult.getTitle());
		this.setGap(certResult.getGap() == null ? Boolean.FALSE : certResult.getGap());
		this.setSed(certResult.getSed() == null ? Boolean.FALSE : certResult.getSed());
		this.setG1Success(certResult.getG1Success() == null ? Boolean.FALSE : certResult.getG1Success());
		this.setG2Success(certResult.getG2Success() == null ? Boolean.FALSE : certResult.getG2Success());
		this.setApiDocumentation(certResult.getApiDocumentation());
		this.setPrivacySecurityFramework(certResult.getPrivacySecurityFramework());
	}
	
	public List<CertificationResultTestProcedure> getTestProcedures() {
		return testProcedures;
	}

	public void setTestProcedures(List<CertificationResultTestProcedure> testProcedures) {
		this.testProcedures = testProcedures;
	}
	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Boolean isSuccess() {
		return success;
	}
	public void setSuccess(Boolean successful) {
		this.success = successful;
	}
	public List<CertificationResultAdditionalSoftware> getAdditionalSoftware() {
		return additionalSoftware;
	}
	public void setAdditionalSoftware(List<CertificationResultAdditionalSoftware> additionalSoftware) {
		this.additionalSoftware = additionalSoftware;
	}
	public Boolean isGap() {
		return gap;
	}
	public void setGap(Boolean gap) {
		this.gap = gap;
	}
	public Boolean isSed() {
		return sed;
	}
	public void setSed(Boolean sed) {
		this.sed = sed;
	}
	public Boolean isG1Success() {
		return g1Success;
	}
	public void setG1Success(Boolean g1Success) {
		this.g1Success = g1Success;
	}
	public Boolean isG2Success() {
		return g2Success;
	}
	public void setG2Success(Boolean g2Success) {
		this.g2Success = g2Success;
	}

	public List<CertificationResultTestTool> getTestToolsUsed() {
		return testToolsUsed;
	}

	public void setTestToolsUsed(List<CertificationResultTestTool> testToolsUsed) {
		this.testToolsUsed = testToolsUsed;
	}

	public List<CertificationResultTestStandard> getTestStandards() {
		return testStandards;
	}

	public void setTestStandards(List<CertificationResultTestStandard> testStandards) {
		this.testStandards = testStandards;
	}
	
	public List<CertificationResultTestData> getTestDataUsed() {
		return testDataUsed;
	}

	public void setTestDataUsed(List<CertificationResultTestData> testDataUsed) {
		this.testDataUsed = testDataUsed;
	}

	public List<CertificationResultTestFunctionality> getTestFunctionality() {
		return testFunctionality;
	}

	public void setTestFunctionality(List<CertificationResultTestFunctionality> testFunctionality) {
		this.testFunctionality = testFunctionality;
	}

	public List<CertificationResultUcdProcess> getUcdProcesses() {
		return ucdProcesses;
	}

	public void setUcdProcesses(List<CertificationResultUcdProcess> ucdProcesses) {
		this.ucdProcesses = ucdProcesses;
	}

	public List<CertificationResultTestTask> getTestTasks() {
		return testTasks;
	}

	public void setTestTasks(List<CertificationResultTestTask> testTasks) {
		this.testTasks = testTasks;
	}

	public String getApiDocumentation() {
		return apiDocumentation;
	}

	public void setApiDocumentation(String apiDocumentation) {
		this.apiDocumentation = apiDocumentation;
	}

	public String getPrivacySecurityFramework() {
		return privacySecurityFramework;
	}

	public void setPrivacySecurityFramework(String privacySecurityFramework) {
		this.privacySecurityFramework = privacySecurityFramework;
	}
	
	public List<MacraMeasure> getG1MacraMeasures() {
		return g1MacraMeasures;
	}

	public void setG1MacraMeasures(List<MacraMeasure> g1MacraMeasures) {
		this.g1MacraMeasures = g1MacraMeasures;
	}

	public List<MacraMeasure> getG2MacraMeasures() {
		return g2MacraMeasures;
	}

	public void setG2MacraMeasures(List<MacraMeasure> g2MacraMeasures) {
		this.g2MacraMeasures = g2MacraMeasures;
	}
	
	public static String formatPrivacyAndSecurityFramework(String privacyAndSecurityFramework){
		if(StringUtils.isEmpty(privacyAndSecurityFramework)) {
			return privacyAndSecurityFramework;
		}
		privacyAndSecurityFramework = privacyAndSecurityFramework.replace(",", PRIVACY_SECURITY_FRAMEWORK_DELIMITER);
		StringBuilder result = new StringBuilder();
		String[] frameworks = privacyAndSecurityFramework.split(PRIVACY_SECURITY_FRAMEWORK_DELIMITER);
		for(int i = 0; i < frameworks.length; i++) {
			if(result.length() > 0) {
				result.append(PRIVACY_SECURITY_FRAMEWORK_DELIMITER);
			}
			result.append(frameworks[i].trim());
		}
		return result.toString();
	}
}
