package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.manager.SearchMenuManager;

@Service
public class SearchMenuManagerImpl implements SearchMenuManager {

	@Autowired
	private CertificationBodyDAO certificationBodyDAO;
	
	@Autowired
	private CertifiedProductDAO certifiedProductDAO;
	
	@Autowired
	private CertificationResultDAO certificationResultDAO;
	
	@Autowired
	private CQMResultDAO cqmResultDAO;
	
	@Autowired
	private CertificationEditionDAO certificationEditionDAO;
	
	@Autowired
	private ProductClassificationTypeDAO productClassificationTypeDAO;
	
	@Autowired
	private ProductVersionDAO productVersionDAO;
	
	@Autowired
	private ProductDAO productDAO;
	
	@Autowired
	private PracticeTypeDAO practiceTypeDAO;
	
	@Autowired
	private VendorDAO vendorDAO;
	
	

	@Override
	public List<String> getClassificationNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getEditionNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getPracticeTypeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getProductNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getVendorNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getCertBodyNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PopulateSearchOptions getPopulateSearchOptions() {
		// TODO Auto-generated method stub
		return null;
	}

}
