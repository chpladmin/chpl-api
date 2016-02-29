package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;

public class CertificationResult {
	private String number;
	private String title;
	private Boolean success;
	private Boolean gap;
	private Boolean sed;
	private Boolean g1Success;
	private Boolean g2Success;
	
	private List<CertificationResultUcdProcess> ucdProcesses;
	private List<CertificationResultTestFunctionality> testFunctionality;
	private List<CertificationResultTestProcedure> testProcedures;
	private List<CertificationResultTestData> testDataUsed;
	private List<CertificationResultAdditionalSoftware> additionalSoftware;
	private List<CertificationResultTestStandard> testStandards;
	private List<CertificationResultTestTool> testToolsUsed;
	
	public CertificationResult(){
			ucdProcesses = new ArrayList<CertificationResultUcdProcess>();
		 additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
		 testStandards = new ArrayList<CertificationResultTestStandard>();
		 testToolsUsed = new ArrayList<CertificationResultTestTool>();
		 testDataUsed = new ArrayList<CertificationResultTestData>();
		 testProcedures = new ArrayList<CertificationResultTestProcedure>();
		 testFunctionality = new ArrayList<CertificationResultTestFunctionality>();
	}
	
	public List<CertificationResultTestProcedure> getTestProcedures() {
		return testProcedures;
	}

	public void setTestProcedures(List<CertificationResultTestProcedure> testProcedures) {
		this.testProcedures = testProcedures;
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
}
