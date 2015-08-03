package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import gov.healthit.chpl.domain.CQMResultJSONObject;
import gov.healthit.chpl.domain.CertificationResultJSONObject;
import gov.healthit.chpl.domain.CertifiedProductSearchDetailsJSONObject;
import gov.healthit.chpl.domain.CertifiedProductSearchResultJSONObject;
import gov.healthit.chpl.domain.ModificationItemJSONObject;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;

@Service
public class CertifiedProductSearchManagerDummyImpl implements CertifiedProductSearchManager {

	
	static final Map<Long, CertifiedProductSearchDetailsJSONObject> details = new HashMap<Long, CertifiedProductSearchDetailsJSONObject>();
	static {
		
		List<String> additionalSoftware = new ArrayList();
		additionalSoftware.add("MedTechSoft");
		additionalSoftware.add("Office");
		
		ModificationItemJSONObject lastModifiedItem = new ModificationItemJSONObject();
		
		List<CertificationResultJSONObject> certs = new ArrayList<CertificationResultJSONObject>();
		
		CertificationResultJSONObject cert1 = new CertificationResultJSONObject();
		
		cert1.setNumber("1242214");
		cert1.setTitle("SADSAFDSAFSAD");
		cert1.setSuccessful(true);
		certs.add(cert1);
		
		List<CQMResultJSONObject> cqms = new ArrayList<CQMResultJSONObject>();
		
		CQMResultJSONObject cqm1 = new CQMResultJSONObject();
		cqm1.setCmsId("asdf");
		cqm1.setCqmDomain("dsafsafdsdaf");
		cqm1.setCqmVersion("2014E1");
		cqm1.setNqfNumber("3245432");
		cqm1.setNumber("2325");
		cqm1.setSuccess(true);
		cqm1.setTitle("CQM-2325: ASdfsadfdsaf");
		cqms.add(cqm1);
		
		
		CertifiedProductSearchDetailsJSONObject obj1 = new CertifiedProductSearchDetailsJSONObject();
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
		
		CertifiedProductSearchDetailsJSONObject obj2 = new CertifiedProductSearchDetailsJSONObject();
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
		
		CertifiedProductSearchDetailsJSONObject obj3 = new CertifiedProductSearchDetailsJSONObject();
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
		
		CertifiedProductSearchDetailsJSONObject obj4 = new CertifiedProductSearchDetailsJSONObject();
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
		
		
		CertifiedProductSearchDetailsJSONObject obj5 = new CertifiedProductSearchDetailsJSONObject();
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
	
	static List<CertifiedProductSearchResultJSONObject> certifiedProducts = new ArrayList<>();
	static {
		
		CertifiedProductSearchResultJSONObject obj1 = new CertifiedProductSearchResultJSONObject();
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
		
		CertifiedProductSearchResultJSONObject obj2 = new CertifiedProductSearchResultJSONObject();
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
		
		CertifiedProductSearchResultJSONObject obj3 = new CertifiedProductSearchResultJSONObject();
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
		
		CertifiedProductSearchResultJSONObject obj4 = new CertifiedProductSearchResultJSONObject();
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
		
		CertifiedProductSearchResultJSONObject obj5 = new CertifiedProductSearchResultJSONObject();
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
	public List<CertifiedProductSearchResultJSONObject> search(String query) {
		return null;
	}

	@Override
	public List<CertifiedProductSearchResultJSONObject> getAll() {		
		return certifiedProducts;
	}

	@Override
	public CertifiedProductSearchDetailsJSONObject getDetails(
			Long certifiedProductId) {
		return details.get(certifiedProductId);
	}

}
