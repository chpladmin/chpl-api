package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import gov.healthit.chpl.json.CertifiedProductSearchDetailsJSONObject;
import gov.healthit.chpl.json.CertifiedProductSearchResultJSONObject;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;

@Service
public class CertifiedProductSearchManagerDummyImpl implements CertifiedProductSearchManager {

	@Override
	public List<CertifiedProductSearchResultJSONObject> search(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CertifiedProductSearchResultJSONObject> getAll() {
		
		List<CertifiedProductSearchResultJSONObject> certifiedProducts = new ArrayList<>();
		
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
		obj1.setId(new Long(2));
		obj1.setCertificationEdition("2014");
		obj1.setCertifyingBody("CB1");
		obj1.setCertsAndCQMs("14 / 17");
		obj1.setChplNum("1233214");
		obj1.setClassification("Something");
		obj1.setPracticeType("Ambulatory");
		obj1.setProduct("MedProduct2");
		obj1.setVendor("SuperMed");
		obj1.setVersion("2");
		
		
		CertifiedProductSearchResultJSONObject obj3 = new CertifiedProductSearchResultJSONObject();
		obj1.setId(new Long(3));
		obj1.setCertificationEdition("2014");
		obj1.setCertifyingBody("CB2");
		obj1.setCertsAndCQMs("14 / 19");
		obj1.setChplNum("1233452323");
		obj1.setClassification("Something");
		obj1.setPracticeType("Ambulatory");
		obj1.setProduct("MedPro");
		obj1.setVendor("ProMed");
		obj1.setVersion("1");
		
		CertifiedProductSearchResultJSONObject obj4 = new CertifiedProductSearchResultJSONObject();
		obj1.setId(new Long(4));
		obj1.setCertificationEdition("2014");
		obj1.setCertifyingBody("CB3");
		obj1.setCertsAndCQMs("14 / 20");
		obj1.setChplNum("123345");
		obj1.setClassification("SomethingElse");
		obj1.setPracticeType("Ambulatory");
		obj1.setProduct("AmbulatoryMedLite");
		obj1.setVendor("ProMed");
		obj1.setVersion("0.8");
		
		CertifiedProductSearchResultJSONObject obj5 = new CertifiedProductSearchResultJSONObject();
		obj1.setId(new Long(5));
		obj1.setCertificationEdition("CE1");
		obj1.setCertifyingBody("CB1");
		obj1.setCertsAndCQMs("12 / 15");
		obj1.setChplNum("123345");
		obj1.setClassification("SomethingElse");
		obj1.setPracticeType("Ambulatory");
		obj1.setProduct("HospitalIT");
		obj1.setVendor("CVB");
		obj1.setVersion("1");
		
		certifiedProducts.add(obj1);
		certifiedProducts.add(obj2);
		certifiedProducts.add(obj3);
		certifiedProducts.add(obj4);
		certifiedProducts.add(obj5);
		
		return certifiedProducts;
	}

	@Override
	public CertifiedProductSearchDetailsJSONObject getDetails(
			Long certifiedProductId) {
		return null;
	}

}
