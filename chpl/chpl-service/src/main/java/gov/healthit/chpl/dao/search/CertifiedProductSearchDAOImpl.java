package gov.healthit.chpl.dao.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
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
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchSetOperator;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.domain.search.NonconformitySearchOptions;
import gov.healthit.chpl.entity.search.CertifiedProductBasicSearchResultEntity;
import gov.healthit.chpl.entity.search.CertifiedProductListingSearchResultEntity;

@Repository("certifiedProductSearchDAO")
public class CertifiedProductSearchDAOImpl extends BaseDAOImpl implements CertifiedProductSearchDAO {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductSearchDAOImpl.class);

    @Override
    public Long getListingIdByUniqueChplNumber(String chplProductNumber) {
        Long id = null;
        Query query = entityManager.createQuery(
                "SELECT cps " + "FROM CertifiedProductBasicSearchResultEntity cps "
                        + "WHERE chplProductNumber = :chplProductNumber",
                CertifiedProductBasicSearchResultEntity.class);
        query.setParameter("chplProductNumber", chplProductNumber);
        List<CertifiedProductBasicSearchResultEntity> results = query.getResultList();
        if (results != null && results.size() > 0) {
            CertifiedProductBasicSearchResultEntity result = results.get(0);
            id = result.getId();
        }
        return id;
    }

    @Override
    public CertifiedProduct getByChplProductNumber(String chplProductNumber) throws EntityNotFoundException {
        Query query = entityManager.createQuery(
                "SELECT cps " + "FROM CertifiedProductBasicSearchResultEntity cps "
                        + "WHERE cps.chplProductNumber = :chplProductNumber",
                CertifiedProductBasicSearchResultEntity.class);
        query.setParameter("chplProductNumber", chplProductNumber);

        List<CertifiedProductBasicSearchResultEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            throw new EntityNotFoundException(
                    "No listing with CHPL Product Number " + chplProductNumber + " was found.");
        }
        CertifiedProduct result = new CertifiedProduct();
        result.setCertificationDate(results.get(0).getCertificationDate().getTime());
        result.setChplProductNumber(results.get(0).getChplProductNumber());
        result.setEdition(results.get(0).getEdition());
        result.setId(results.get(0).getId());
        return result;
    }
    
    @Override
    public IcsFamilyTreeNode getICSFamilyTree(Long certifiedProductId) {
        Long id = null;
        Query query = entityManager.createQuery(
                "SELECT cps " + "FROM CertifiedProductBasicSearchResultEntity cps "
                        + "WHERE certified_product_id = :certifiedProductId",
                CertifiedProductBasicSearchResultEntity.class);
        query.setParameter("certifiedProductId", certifiedProductId);
        List<CertifiedProductBasicSearchResultEntity> searchResult = query.getResultList();
        CertifiedProductBasicSearchResultEntity result = null;
        if (searchResult.size() > 0) {
            result = searchResult.get(0);
        }
        return convertIcs(result);
    }

    @Override
    public List<CertifiedProductFlatSearchResult> getAllCertifiedProducts() {
        LOGGER.info("Starting basic search query.");
        Query query = entityManager.createQuery("SELECT cps " + 
                    "FROM CertifiedProductBasicSearchResultEntity cps ",
                CertifiedProductBasicSearchResultEntity.class);

        Date startDate = new Date();
        List<CertifiedProductBasicSearchResultEntity> results = query.getResultList();
        Date endDate = new Date();
        LOGGER.info("Got query results in " + (endDate.getTime() - startDate.getTime()) + " millis");
        return convertToFlatListings(results);
    }

    @Override
    public List<CertifiedProductBasicSearchResult> search(SearchRequest searchRequest) {
        List<CertifiedProductBasicSearchResult> results = 
                new ArrayList<CertifiedProductBasicSearchResult>();

        int firstResult = searchRequest.getPageNumber() * searchRequest.getPageSize();
        int lastResult = firstResult + searchRequest.getPageSize();
        Query query = entityManager.createNativeQuery(
                "{CALL SearchListings(?,?)}",
                CertifiedProductListingSearchResultEntity.class)
                .setParameter(1, firstResult)
                .setParameter(2, lastResult);

        List<CertifiedProductListingSearchResultEntity> queryResults = query.getResultList();
        results = convertToListings(queryResults);
        return results;
    }
    
    private String buildSearchTermFilter(SearchRequest searchRequest) {
        String result = "";
        if (searchRequest.getSearchTerm() != null) {
            if (searchRequest.getSearchTerm().toUpperCase().startsWith("CHP-") || 
                searchRequest.getSearchTerm().split("\\.").length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
                result += " AND UPPER(cps.chplProductNumber) LIKE :searchTerm ";
            } else {
                result += " AND (" 
                            + "(UPPER(cps.developer) LIKE :searchTerm) " 
                            + "OR "
                            + "(UPPER(cps.product) LIKE :searchterm) " 
                            + "OR "
                            + "(UPPER(cps.acbCertificationId) LIKE :searchTerm) " 
                        + ")";
            }
        }
        return result;
    }

    private void populateSearchTermParameter(SearchRequest searchRequest, Query query) {
        if (searchRequest.getSearchTerm() != null) {
            query.setParameter("searchTerm", "%" + searchRequest.getSearchTerm().toUpperCase() + "%");
        }
    }

    private String buildCertificationDateFilter(SearchRequest searchRequest) {
        String result = "";
        if (!StringUtils.isEmpty(searchRequest.getCertificationDateStart())) {
            result += " AND (cps.certificationDate >= :certificationDateStart) ";
        }
        if (!StringUtils.isEmpty(searchRequest.getCertificationDateEnd())) {
            result += " AND (cps.certificationDate <= :certificationDateEnd) ";
        }
        return result;
    }

    private String populateCertificationDateFilter(SearchRequest searchRequest, Query query) {
        SimpleDateFormat format = new SimpleDateFormat(SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);

        String result = "";
        if (!StringUtils.isEmpty(searchRequest.getCertificationDateStart())) {
            Date start = null;
            try {
                start = format.parse(searchRequest.getCertificationDateStart());
                Calendar beginningOfDay = Calendar.getInstance();
                beginningOfDay.setTime(start);
                beginningOfDay.set(Calendar.HOUR, 0);
                beginningOfDay.set(Calendar.MINUTE, 0);
                beginningOfDay.set(Calendar.SECOND, 0);
                beginningOfDay.set(Calendar.MILLISECOND, 0);
                query.setParameter("certificationDateStart", beginningOfDay.getTime());
            } catch (final ParseException ex) {
                LOGGER.error("Could not parse " + searchRequest.getCertificationDateStart() + " as date in the format "
                        + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
            }
        }
        if (!StringUtils.isEmpty(searchRequest.getCertificationDateEnd())) {
            Date end = null;
            try {
                end = format.parse(searchRequest.getCertificationDateEnd());
                Calendar endOfDay = Calendar.getInstance();
                endOfDay.setTime(end);
                endOfDay.set(Calendar.HOUR, 23);
                endOfDay.set(Calendar.MINUTE, 59);
                endOfDay.set(Calendar.SECOND, 59);
                endOfDay.set(Calendar.MILLISECOND, 999);
                query.setParameter("certificationDateEnd", endOfDay.getTime());
            } catch (final ParseException ex) {
                LOGGER.error("Could not parse " + searchRequest.getCertificationDateStart() + " as date in the format "
                        + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
            }
        }
        return result;
    }

    private String buildCertificationResultFilter(SearchRequest searchRequest) {
        String result = "";
        if (searchRequest.getCertificationCriteria() != null && 
                searchRequest.getCertificationCriteria().size() > 0) {
            for(int i = 0; i < searchRequest.getCertificationCriteria().size(); i++) {
                if(i == 0) {
                    result += " AND ( ";
                }
                if(i > 1) {
                    if(searchRequest.getCertificationCriteriaOperator() != null && 
                            searchRequest.getCertificationCriteriaOperator() == SearchSetOperator.AND) {
                        result += " AND ";
                    } else {
                        result += " OR ";
                    }
                }
                result += " UPPER(cps.certs) LIKE %:criteriaNumber" + i + "% ";
                if(i == searchRequest.getCertificationCriteria().size()-1) {
                    result += " ) ";
                }
            }
        }
        return result;
    }
    
    private void populateCertificationResultFilter(SearchRequest searchRequest, Query query) {
        if (searchRequest.getCertificationCriteria() != null && 
                searchRequest.getCertificationCriteria().size() > 0) {
            Iterator<String> criteriaIter = searchRequest.getCertificationCriteria().iterator();
            int i = 0; 
            while(criteriaIter.hasNext()) {
                String criteria = criteriaIter.next().trim().toUpperCase();
                query.setParameter("criteriaNumber"+i, criteria);
            }
            i++;
        }
    }
    
    private String buildCqmsFilter(SearchRequest searchRequest) {
        String result = "";
        if (searchRequest.getCqms() != null && searchRequest.getCqms().size() > 0) {
            for(int i = 0; i < searchRequest.getCqms().size(); i++) {
                if(i == 0) {
                    result += " AND ( ";
                }
                if(i > 1) {
                    if(searchRequest.getCqmsOperator() != null && 
                            searchRequest.getCqmsOperator() == SearchSetOperator.AND) {
                        result += " AND ";
                    } else {
                        result += " OR ";
                    }
                }
                result += " UPPER(cps.cqms) LIKE %:cqm" + i + "% ";
                if(i == searchRequest.getCqms().size()-1) {
                    result += " ) ";
                }
            }
        }
        return result;
    }
    
    private void populateCqmsFilter(SearchRequest searchRequest, Query query) {
        if (searchRequest.getCqms() != null && searchRequest.getCqms().size() > 0) {
            Iterator<String> cqmIter = searchRequest.getCqms().iterator();
            int i = 0; 
            while(cqmIter.hasNext()) {
                String cqm = cqmIter.next().trim().toUpperCase();
                query.setParameter("cqm"+i, cqm);
            }
            i++;
        }
    }
    
    private String buildPracticeTypeFilter(SearchRequest searchRequest) {
        String result = "";
        if (searchRequest.getPracticeType() != null) {
            result += " AND (UPPER(cps.practiceTypeName) = UPPER(:practiceType)) ";
        }
        return result;
    }

    private void populatePracticeTypeParameter(SearchRequest searchRequest, Query query) {
        if (searchRequest.getPracticeType() != null) {
            query.setParameter("practiceType", searchRequest.getPracticeType());
        }
    }

    private String buildDeveloperFilter(SearchRequest searchRequest) {
        String result = "";
        if (searchRequest.getDeveloper() != null) {
            result += " AND (UPPER(cps.developer) LIKE UPPER(:developerName)) ";
        }
        return result;
    }

    private void populateDeveloperParameter(SearchRequest searchRequest, Query query) {
        if (searchRequest.getDeveloper() != null) {
            query.setParameter("developerName", "%" + searchRequest.getDeveloper() + "%");
        }
    }

    private String buildProductFilter(SearchRequest searchRequest) {
        String result = "";
        if (searchRequest.getProduct() != null) {
            result += " AND (UPPER(cps.product) LIKE UPPER(:productName)) ";
        }
        return result;
    }

    private void populateProductParameter(SearchRequest searchRequest, Query query) {
        if (searchRequest.getProduct() != null) {
            query.setParameter("productName", "%" + searchRequest.getProduct() + "%");
        }
    }

    private String buildProductVersionFilter(SearchRequest searchRequest) {
        String result = "";
        if (searchRequest.getVersion() != null) {
            result += " AND (UPPER(cps.version) LIKE UPPER(:version)) ";
        }
        return result;
    }

    private void populateProductVersionParameter(SearchRequest searchRequest, Query query) {
        if (searchRequest.getVersion() != null) {
            query.setParameter("version", "%" + searchRequest.getVersion() + "%");
        }
    }

    private String buildCertificationStatusFilter(SearchRequest searchRequest) {
        String result = "";
        if (searchRequest.getCertificationStatuses() != null && searchRequest.getCertificationStatuses().size() > 0) {
            result += " AND UPPER(cps.certificationStatus) IN (UPPER(:certificationStatus)) ";
        } else {
            result += " AND (UPPER(cps.certificationStatus) NOT LIKE 'RETIRED')";
        }
        return result;
    }

    private void populateCertificationStatusParameter(SearchRequest searchRequest, Query query) {
        if (searchRequest.getCertificationStatuses() != null && searchRequest.getCertificationStatuses().size() > 0) {
            query.setParameter("certificationStatus", searchRequest.getCertificationStatuses());
        }
    }

    private String buildCertificationBodiesFilter(SearchRequest searchRequest) {
        String result = "";
        if (searchRequest.getCertificationBodies() != null && searchRequest.getCertificationBodies().size() > 0) {
            result += " AND UPPER(cps.acbName) IN (UPPER(:certificationBody)) ";
        }
        return result;
    }

    private void populateCertificationBodiesParameter(SearchRequest searchRequest, Query query) {
        if (searchRequest.getCertificationBodies() != null && searchRequest.getCertificationBodies().size() > 0) {
            query.setParameter("certificationBody", searchRequest.getCertificationBodies());
        }
    }

    private String buildCertificationEditionsFilter(SearchRequest searchRequest) {
        String result = "";
        if (searchRequest.getCertificationEditions() != null && searchRequest.getCertificationEditions().size() > 0) {
            result += " AND cps.edition IN (:certificationEdition) ";
        } else {
            result += " AND cps.edition NOT LIKE '2011'";
        }
        return result;
    }

    private void populateCertificationEditionsParameter(SearchRequest searchRequest, Query query) {
        if (searchRequest.getCertificationEditions() != null && searchRequest.getCertificationEditions().size() > 0) {
            query.setParameter("certificationEdition", searchRequest.getCertificationEditions());
        }
    }

    private String buildSurveillanceFilter(SearchRequest searchRequest) {
        String result = "";
        if (searchRequest.getSurveillance() != null &&
                searchRequest.getSurveillance().getHasHadSurveillance() != null) {
            if (searchRequest.getSurveillance().getHasHadSurveillance().booleanValue() == true) {
                result += " AND cps.surveillanceCount > 0 ";
            } else if (searchRequest.getSurveillance().getHasHadSurveillance().booleanValue() == false) {
                result += " AND cps.surveillanceCount = 0 ";
            }
        }

        if (searchRequest.getSurveillance() != null && 
                searchRequest.getSurveillance().getNonconformityOptions() != null && 
                        searchRequest.getSurveillance().getNonconformityOptions().size() > 0) {
            result += " AND (";
            Iterator<NonconformitySearchOptions> opts = 
                    searchRequest.getSurveillance().getNonconformityOptions().iterator();
            while (opts.hasNext()) {
                NonconformitySearchOptions opt = opts.next();
                switch (opt) {
                case NEVER_NONCONFORMITY:
                    result += " cps.openNonconformityCount = 0 AND cps.closedNonconformityCount = 0 ";
                    break;
                case OPEN_NONCONFORMITY:
                    result += " cps.openNonconformityCount > 0 ";
                    break;
                case CLOSED_NONCONFORMITY:
                    result += " cps.closedNonconformityCount > 0 ";
                    break;
                }

                if (opts.hasNext()) {
                    if(searchRequest.getSurveillance().getNonconformityOptionsOperator() != null && 
                           searchRequest.getSurveillance().getNonconformityOptionsOperator() == SearchSetOperator.AND) {
                        result += " AND ";
                    } else {
                        result += " OR ";
                    }
                }
            }
            result += ") ";
        }
        return result;
    }
    
    private List<CertifiedProductBasicSearchResult> convertToListings(List<CertifiedProductListingSearchResultEntity> dbResults) {
        List<CertifiedProductBasicSearchResult> results = new ArrayList<CertifiedProductBasicSearchResult>(
                dbResults.size());
        for (CertifiedProductListingSearchResultEntity dbResult : dbResults) {
            CertifiedProductBasicSearchResult result = new CertifiedProductBasicSearchResult();
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
            result.setDecertificationDate(
                    dbResult.getDecertificationDate() == null ? null : dbResult.getDecertificationDate().getTime());
            result.setCertificationDate(dbResult.getCertificationDate().getTime());
            result.setCertificationStatus(dbResult.getCertificationStatus());
            result.setTransparencyAttestationUrl(dbResult.getTransparencyAttestationUrl());
            if(dbResult.getPreviousDeveloperOwners() != null && dbResult.getPreviousDeveloperOwners().size() > 0) {
                for(String prevOwnerName : dbResult.getPreviousDeveloperOwners()) {
                    result.getPreviousDevelopers().add(prevOwnerName);
                }
            }
            if(dbResult.getCerts() != null && dbResult.getCerts().size() > 0) {
                for(String cert : dbResult.getCerts()) {
                    result.getCriteriaMet().add(cert);
                }
            }
            if(dbResult.getCqms() != null && dbResult.getCqms().size() > 0) {
                for(String cqm : dbResult.getCqms()) {
                    result.getCqmsMet().add(cqm);
                }
            }
            results.add(result);
        }
        return results;
    }
    
    private List<CertifiedProductFlatSearchResult> convertToFlatListings(List<CertifiedProductBasicSearchResultEntity> dbResults) {
        List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>(
                dbResults.size());
        for (CertifiedProductBasicSearchResultEntity dbResult : dbResults) {
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
            result.setDecertificationDate(
                    dbResult.getDecertificationDate() == null ? null : dbResult.getDecertificationDate().getTime());
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
        if (result.getChild() != null) {
            String[] children = result.getChild().split("\u263A");
            for (String child : children) {
                String[] childInfo = child.split("\u2639");
                CertifiedProduct cp = new CertifiedProduct();
                cp.setChplProductNumber(childInfo[0]);
                cp.setId(Long.decode(childInfo[1]));
                childrenList.add(cp);
            }
        }
        node.setChildren(childrenList);
        ArrayList<CertifiedProduct> parentList = new ArrayList<CertifiedProduct>();
        if (result.getParent() != null) {
            String[] parents = result.getParent().split("\u263A");
            for (String parent : parents) {
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
