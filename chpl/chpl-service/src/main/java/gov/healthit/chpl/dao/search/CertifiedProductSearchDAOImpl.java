package gov.healthit.chpl.dao.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.entity.search.CertifiedProductBasicSearchResultEntity;

@Repository("certifiedProductSearchDAO")
public class CertifiedProductSearchDAOImpl extends BaseDAOImpl implements CertifiedProductSearchDAO {
	private static final Logger LOGGER = LogManager.getLogger(CertifiedProductSearchDAOImpl.class);

	@Override
	public Long getListingIdByUniqueChplNumber(String chplProductNumber) {
		Long id = null;
		Query query = entityManager.createQuery("SELECT cps "
				+ "FROM CertifiedProductBasicSearchResultEntity cps "
				+ "WHERE chplProductNumber = :chplProductNumber",
				CertifiedProductBasicSearchResultEntity.class);
		query.setParameter("chplProductNumber", chplProductNumber);
		List<CertifiedProductBasicSearchResultEntity> results = query.getResultList();
		if(results != null && results.size() > 0) {
			CertifiedProductBasicSearchResultEntity result = results.get(0);
			id = result.getId();
		}
		return id;
	}

	@Override
	public IcsFamilyTreeNode getICSFamilyTree(Long certifiedProductId) {
		Long id = null;
		Query query = entityManager.createQuery("SELECT cps "
				+ "FROM CertifiedProductBasicSearchResultEntity cps "
				+ "WHERE certified_product_id = :certifiedProductId",
				CertifiedProductBasicSearchResultEntity.class);
		query.setParameter("certifiedProductId", certifiedProductId);
		List<CertifiedProductBasicSearchResultEntity> searchResult = query.getResultList();
		CertifiedProductBasicSearchResultEntity result = null;
		if (searchResult.size() > 0){
			result = searchResult.get(0);
		}
		return convertIcs(result);
	}

	@Override
	public List<CertifiedProductFlatSearchResult> getAllCertifiedProducts() {
		LOGGER.info("Starting basic search query.");
		Query query = entityManager.createQuery("SELECT cps "
				+ "FROM CertifiedProductBasicSearchResultEntity cps "
				, CertifiedProductBasicSearchResultEntity.class);

		Date startDate = new Date();
		List<CertifiedProductBasicSearchResultEntity> results = query.getResultList();
		Date endDate = new Date();
		LOGGER.info("Got query results in " + (endDate.getTime() - startDate.getTime()) + " millis");
		return convert(results);
	}

	@Override
	public CertifiedProduct getByChplProductNumber(String chplProductNumber)
		throws EntityNotFoundException {
		Query query = entityManager.createQuery("SELECT cps "
				+ "FROM CertifiedProductBasicSearchResultEntity cps "
				+ "WHERE cps.chplProductNumber = :chplProductNumber"
				, CertifiedProductBasicSearchResultEntity.class);
		query.setParameter("chplProductNumber", chplProductNumber);

		List<CertifiedProductBasicSearchResultEntity> results = query.getResultList();
		if(results == null || results.size() == 0) {
			throw new EntityNotFoundException("No listing with CHPL Product Number " + chplProductNumber + " was found.");
		}
		CertifiedProduct result = new CertifiedProduct();
		result.setCertificationDate(results.get(0).getCertificationDate().getTime());
		result.setChplProductNumber(results.get(0).getChplProductNumber());
		result.setEdition(results.get(0).getEdition());
		result.setId(results.get(0).getId());
		return result;
	}

	private List<CertifiedProductFlatSearchResult> convert(List<CertifiedProductBasicSearchResultEntity> dbResults) {
		List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>(dbResults.size());
		for(CertifiedProductBasicSearchResultEntity dbResult : dbResults) {
			//CertifiedProductBasicSearchResult result = new CertifiedProductBasicSearchResult();
			CertifiedProductFlatSearchResult result = new CertifiedProductFlatSearchResult();
			result.setId(dbResult.getId());
			result.setChplProductNumber(dbResult.getChplProductNumber());
			result.setEdition(dbResult.getEdition());
			result.setAtl(dbResult.getAtlName());
			result.setAcb(dbResult.getAcbName());
			result.setAcbCertificationId(dbResult.getAcbCertificationId());
			result.setPracticeType(dbResult.getPracticeTypeName());
			result.setDeveloper(dbResult.getDeveloper());
			result.setProduct(dbResult.getProduct());
			result.setVersion(dbResult.getVersion());
			result.setNumMeaningfulUse(dbResult.getMeaningfulUseUserCount());
			result.setDecertificationDate(dbResult.getDecertificationDate() == null ? null : dbResult.getDecertificationDate().getTime());
			result.setCertificationDate(dbResult.getCertificationDate().getTime());
			result.setCertificationStatus(dbResult.getCertificationStatus());
			result.setTransparencyAttestationUrl(dbResult.getTransparencyAttestationUrl());
			result.setApiDocumentation(dbResult.getApiDocumentation());
			result.setSurveillanceCount(dbResult.getSurveillanceCount());
			result.setOpenNonconformityCount(dbResult.getOpenNonconformityCount());
			result.setClosedNonconformityCount(dbResult.getClosedNonconformityCount());
			result.setCriteriaMet(dbResult.getCerts());
			result.setCqmsMet(dbResult.getCqms());
			result.setPreviousDevelopers(dbResult.getPreviousDevelopers());

//			if(!StringUtils.isEmpty(dbResult.getPreviousDevelopers())) {
//				String[] splitDevelopers = dbResult.getPreviousDevelopers().split("\u263A");
//				if(splitDevelopers != null && splitDevelopers.length > 0) {
//					for(int i = 0; i < splitDevelopers.length; i++) {
//						result.getPreviousDevelopers().add(splitDevelopers[i].trim());
//					}
//				}
//			}
//
//			if(!StringUtils.isEmpty(dbResult.getCerts())) {
//				String[] splitCerts = dbResult.getCerts().split("\u263A");
//				if(splitCerts != null && splitCerts.length > 0) {
//					for(int i = 0; i < splitCerts.length; i++) {
//						result.getCriteriaMet().add(splitCerts[i].trim());
//					}
//				}
//			}
//
//			if(!StringUtils.isEmpty(dbResult.getCqms())) {
//				String[] splitCqms = dbResult.getCqms().split("\u263A");
//				if(splitCqms != null && splitCqms.length > 0) {
//					for(int i = 0; i < splitCqms.length; i++) {
//						result.getCqmsMet().add(splitCqms[i].trim());
//					}
//				}
//			}

			results.add(result);
		}
		return results;
	}

	private IcsFamilyTreeNode convertIcs(CertifiedProductBasicSearchResultEntity result) {
		IcsFamilyTreeNode node = new IcsFamilyTreeNode();
		node.setId(result.getId());
		node.setChplProductNumber(result.getChplProductNumber());
		node.setCertificationDate(result.getCertificationDate());
		CertificationStatus cs = new CertificationStatus(result.getCertificationStatus());
		node.setCertificationStatus(cs);
		Developer dev = new Developer();
		dev.setName(result.getDeveloper());
		node.setDeveloper(dev);
		Product prod = new Product();
		prod.setName(result.getProduct());
		node.setProduct(prod);
		ProductVersion pv = new ProductVersion();
		pv.setVersion(result.getVersion());
		node.setVersion(pv);
		ArrayList<CertifiedProduct> childrenList = new ArrayList<CertifiedProduct>();
		if(result.getChild() != null){
			String[] children = result.getChild().split("\u263A");
			for(String child : children){
				String[] childInfo = child.split("\u2639");
				CertifiedProduct cp = new CertifiedProduct();
				cp.setChplProductNumber(childInfo[0]);
				cp.setId(Long.decode(childInfo[1]));
				childrenList.add(cp);
			}
		}
		node.setChildren(childrenList);
		ArrayList<CertifiedProduct> parentList = new ArrayList<CertifiedProduct>();
		if(result.getParent() != null){
			String[] parents = result.getParent().split("\u263A");
			for(String parent : parents){
				String[] parentInfo = parent.split("\u2639");
				CertifiedProduct cp = new CertifiedProduct();
				cp.setChplProductNumber(parentInfo[0]);
				cp.setId(Long.decode(parentInfo[1]));
				parentList.add(cp);
			}
		}
		node.setParents(parentList);
		return node;
	}
}
