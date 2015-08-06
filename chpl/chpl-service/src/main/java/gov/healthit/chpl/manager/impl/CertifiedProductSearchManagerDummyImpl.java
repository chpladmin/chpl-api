package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import gov.healthit.chpl.domain.CQMResult;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.ModificationItem;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;

@Service
public class CertifiedProductSearchManagerDummyImpl implements CertifiedProductSearchManager {

	
	static final Map<Long, CertifiedProductSearchDetails> details = new HashMap<Long, CertifiedProductSearchDetails>();
	static {
		
		List<String> additionalSoftware = new ArrayList();
		additionalSoftware.add("MedTechSoft");
		additionalSoftware.add("Office");
		
		ModificationItem lastModifiedItem = new ModificationItem();
		
		List<CertificationResult> certs = new ArrayList<CertificationResult>();
		
		CertificationResult cert1 = new CertificationResult();
		
		cert1.setNumber("1242214");
		cert1.setTitle("SADSAFDSAFSAD");
		cert1.setSuccess(true);
		certs.add(cert1);
		
		List<CQMResult> cqms = new ArrayList<CQMResult>();
		
		CQMResult cqm1 = new CQMResult();
		cqm1.setCmsId("asdf");
		cqm1.setCqmDomain("dsafsafdsdaf");
		cqm1.setCqmVersion("2014E1");
		cqm1.setNqfNumber("3245432");
		cqm1.setNumber("2325");
		cqm1.setSuccess(true);
		cqm1.setTitle("CQM-2325: ASdfsadfdsaf");
		cqms.add(cqm1);
		
		
		CertifiedProductSearchDetails obj1 = new CertifiedProductSearchDetails();
		obj1.setId(new Long(1));
		obj1.setCertificationEdition("2011");
		obj1.setCertifyingBody("CB1");
		obj1.setCertsAndCQMs("12 / 15");
		obj1.setChplNum("123345");
		obj1.setClassification("Something");
		obj1.setPracticeType("Ambulatory");
		obj1.setProduct("MedProduct");
		obj1.setVendor("SuperMed");
		obj1.setVersion("1");
		obj1.setEdition("2014");
		obj1.setLastModifiedDate(new Date(1438623894156L));
		obj1.setAdditionalSoftware(additionalSoftware);
		obj1.setLastModifiedItem(lastModifiedItem);
		obj1.setCerts(certs);
		obj1.setCqms(cqms);
		
		CertifiedProductSearchDetails obj2 = new CertifiedProductSearchDetails();
		obj2.setId(new Long(2));
		obj2.setCertificationEdition("2014");
		obj2.setCertifyingBody("CB1");
		obj2.setCertsAndCQMs("14 / 17");
		obj2.setChplNum("1233214");
		obj2.setClassification("Something");
		obj2.setPracticeType("Ambulatory");
		obj2.setProduct("MedProduct2");
		obj2.setVendor("SuperMed");
		obj2.setVersion("2");
		obj2.setEdition("2014");
		obj2.setLastModifiedDate(new Date(1438623894156L));
		obj2.setAdditionalSoftware(additionalSoftware);
		obj2.setLastModifiedItem(lastModifiedItem);
		obj2.setCerts(certs);
		obj2.setCqms(cqms);
		
		CertifiedProductSearchDetails obj3 = new CertifiedProductSearchDetails();
		obj3.setId(new Long(3));
		obj3.setCertificationEdition("2014");
		obj3.setCertifyingBody("CB2");
		obj3.setCertsAndCQMs("14 / 19");
		obj3.setChplNum("1233452323");
		obj3.setClassification("Something");
		obj3.setPracticeType("Ambulatory");
		obj3.setProduct("MedPro");
		obj3.setVendor("ProMed");
		obj3.setVersion("1");
		obj3.setEdition("2014");
		obj3.setLastModifiedDate(new Date(1438623894156L));
		obj3.setAdditionalSoftware(additionalSoftware);
		obj3.setLastModifiedItem(lastModifiedItem);
		obj3.setCerts(certs);
		obj3.setCqms(cqms);
		
		CertifiedProductSearchDetails obj4 = new CertifiedProductSearchDetails();
		obj4.setId(new Long(4));
		obj4.setCertificationEdition("2014");
		obj4.setCertifyingBody("CB3");
		obj4.setCertsAndCQMs("14 / 20");
		obj4.setChplNum("123345");
		obj4.setClassification("SomethingElse");
		obj4.setPracticeType("Ambulatory");
		obj4.setProduct("AmbulatoryMedLite");
		obj4.setVendor("ProMed");
		obj4.setVersion("0.8");
		obj4.setEdition("2014");
		obj4.setLastModifiedDate(new Date(1438623894156L));
		obj4.setAdditionalSoftware(additionalSoftware);
		obj4.setLastModifiedItem(lastModifiedItem);
		obj4.setCerts(certs);
		obj4.setCqms(cqms);
		
		
		CertifiedProductSearchDetails obj5 = new CertifiedProductSearchDetails();
		obj5.setId(new Long(5));
		obj5.setCertificationEdition("CE1");
		obj5.setCertifyingBody("CB1");
		obj5.setCertsAndCQMs("12 / 15");
		obj5.setChplNum("123345");
		obj5.setClassification("SomethingElse");
		obj5.setPracticeType("Ambulatory");
		obj5.setProduct("HospitalIT");
		obj5.setVendor("CVB");
		obj5.setVersion("1");
		obj5.setEdition("2014");
		obj5.setLastModifiedDate(new Date(1438623894156L));
		obj5.setAdditionalSoftware(additionalSoftware);
		obj5.setLastModifiedItem(lastModifiedItem);
		obj5.setCerts(certs);
		obj5.setCqms(cqms);
		
		details.put(1L, obj1);
		details.put(2L, obj2);
		details.put(3L, obj3);
		details.put(4L, obj4);
		details.put(5L, obj5);
		
	}
	
	static List<CertifiedProductSearchResult> certifiedProducts = new ArrayList<>();
	static {
		
		CertifiedProductSearchResult obj1 = new CertifiedProductSearchResult();
		obj1.setId(new Long(1));
		obj1.setCertificationEdition("2011");
		obj1.setCertifyingBody("CB1");
		obj1.setCertsAndCQMs("12 / 15");
		obj1.setChplNum("123345");
		obj1.setClassification("Something");
		obj1.setPracticeType("Ambulatory");
		obj1.setProduct("MedProduct");
		obj1.setVendor("SuperMed");
		obj1.setVersion("1");
		
		CertifiedProductSearchResult obj2 = new CertifiedProductSearchResult();
		obj2.setId(new Long(2));
		obj2.setCertificationEdition("2014");
		obj2.setCertifyingBody("CB1");
		obj2.setCertsAndCQMs("14 / 17");
		obj2.setChplNum("1233214");
		obj2.setClassification("Something");
		obj2.setPracticeType("Ambulatory");
		obj2.setProduct("MedProduct2");
		obj2.setVendor("SuperMed");
		obj2.setVersion("2");
		
		CertifiedProductSearchResult obj3 = new CertifiedProductSearchResult();
		obj3.setId(new Long(3));
		obj3.setCertificationEdition("2014");
		obj3.setCertifyingBody("CB2");
		obj3.setCertsAndCQMs("14 / 19");
		obj3.setChplNum("1233452323");
		obj3.setClassification("Something");
		obj3.setPracticeType("Ambulatory");
		obj3.setProduct("MedPro");
		obj3.setVendor("ProMed");
		obj3.setVersion("1");
		
		CertifiedProductSearchResult obj4 = new CertifiedProductSearchResult();
		obj4.setId(new Long(4));
		obj4.setCertificationEdition("2014");
		obj4.setCertifyingBody("CB3");
		obj4.setCertsAndCQMs("14 / 20");
		obj4.setChplNum("123345");
		obj4.setClassification("SomethingElse");
		obj4.setPracticeType("Ambulatory");
		obj4.setProduct("AmbulatoryMedLite");
		obj4.setVendor("ProMed");
		obj4.setVersion("0.8");
		
		CertifiedProductSearchResult obj5 = new CertifiedProductSearchResult();
		obj5.setId(new Long(5));
		obj5.setCertificationEdition("CE1");
		obj5.setCertifyingBody("CB1");
		obj5.setCertsAndCQMs("12 / 15");
		obj5.setChplNum("123345");
		obj5.setClassification("SomethingElse");
		obj5.setPracticeType("Ambulatory");
		obj5.setProduct("HospitalIT");
		obj5.setVendor("CVB");
		obj5.setVersion("1");
		
		certifiedProducts.add(obj1);
		certifiedProducts.add(obj2);
		certifiedProducts.add(obj3);
		certifiedProducts.add(obj4);
		certifiedProducts.add(obj5);
	}
	
	@Override
	public List<CertifiedProductSearchResult> search(String query) {
		return null;
	}

	@Override
	public List<CertifiedProductSearchResult> getAllCertifiedProducts() {		
		return certifiedProducts;
	}

	@Override
	public CertifiedProductSearchDetails getCertifiedProductDetails(
			Long certifiedProductId) {
		return details.get(certifiedProductId);
	}

	@Override
	public List<CertificationResult> getCertifications() {
		
		List<CertificationResult> certResults = new ArrayList<CertificationResult>();
		
		CertificationResult result1 = new CertificationResult();
		result1.setNumber("123");
		result1.setSuccess(true);
		result1.setTitle("1214 Cert for something");
		
		CertificationResult result2 = new CertificationResult();
		result2.setNumber("124");
		result2.setSuccess(true);
		result2.setTitle("1215 Cert for something else");
		
		certResults.add(result1);
		certResults.add(result2);
		
		return certResults;
	}

	@Override
	public List<CQMResult> getCQMResults() {
		List<CQMResult> cqmResults = new ArrayList<CQMResult>();
		
		CQMResult result1 = new CQMResult();
		result1.setNumber("123");
		result1.setSuccess(true);
		result1.setTitle("1214 Cert for something");
		result1.setCmsId("123434");
		result1.setCqmDomain("some domain");
		result1.setCqmVersion("2014");
		result1.setNqfNumber("12335");
		
		CQMResult result2 = new CQMResult();
		result2.setNumber("124");
		result2.setSuccess(true);
		result2.setTitle("1215 Cert for something else");
		result2.setCmsId("64341234");
		result2.setCqmDomain("some domain");
		result2.setCqmVersion("2011");
		result2.setNqfNumber("1233111");
		
		cqmResults.add(result1);
		cqmResults.add(result2);
		
		return cqmResults;
	}

	@Override
	public List<String> getClassificationNames() {
		List<String> classificationNames = new ArrayList<String>();
		
		classificationNames.add("Complete EHR");
		classificationNames.add("Modular EHR");
		
		return classificationNames;
	}

	@Override
	public List<String> getEditionNames() {
		
		List<String> editionNames = new ArrayList<String>();
		
		editionNames.add("2011");
		editionNames.add("2014");
		
		return editionNames;
	}

	@Override
	public List<String> getPracticeTypeNames() {
		
		List<String> names = new ArrayList<String>();
		names.add("Inpatient");
		names.add("Ambulatory");
		return names;
	}

	@Override
	public List<String> getProductNames() {
		List<String> names = new ArrayList<String>();
		names.add("MedProduct");
		names.add("MedProduct2");
		names.add("MedPro");
		names.add("AmbulatoryMedLite");
		names.add("HospitalIT");
		return names;
	}

	@Override
	public List<String> getVendorNames() {
		List<String> names = new ArrayList<String>();
		names.add("SuperMed");	
		names.add("ProMed");
		names.add("CVB");
		return names;
	}

	@Override
	public List<String> getCertBodyNames() {
		List<String> names = new ArrayList<String>();
		names.add("Drummond");	
		names.add("SomeACB");
		names.add("OtherACB");
		return names;
	}

	@Override
	public PopulateSearchOptions getPopulateSearchOptions() {
		// TODO Auto-generated method stub
		return null;
	}

}
