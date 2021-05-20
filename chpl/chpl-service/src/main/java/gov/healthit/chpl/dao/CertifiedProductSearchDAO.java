package gov.healthit.chpl.dao;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResultLegacy;
import gov.healthit.chpl.domain.search.NonconformitySearchOptions;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchSetOperator;
import gov.healthit.chpl.entity.search.CertifiedProductBasicSearchResultEntity;
import gov.healthit.chpl.entity.search.CertifiedProductListingSearchResultEntity;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import lombok.extern.log4j.Log4j2;


@Repository("certifiedProductSearchDAO")
@Log4j2
public class CertifiedProductSearchDAO extends BaseDAOImpl {
    private final DateFormat certificationDateFormatter =
            new SimpleDateFormat(SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);

    public Long getListingIdByUniqueChplNumber(final String chplProductNumber) {
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

    public CertifiedProduct getByChplProductNumber(final String chplProductNumber) throws EntityNotFoundException {
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
        result.setCertificationStatus(results.get(0).getCertificationStatus());
        result.setChplProductNumber(results.get(0).getChplProductNumber());
        result.setEdition(results.get(0).getEdition());
        result.setCuresUpdate(results.get(0).getCuresUpdate());
        result.setId(results.get(0).getId());
        return result;
    }

    public IcsFamilyTreeNode getICSFamilyTree(final Long certifiedProductId) {
        Query query = entityManager.createQuery(
                "SELECT cps " + "FROM CertifiedProductBasicSearchResultEntity cps "
                        + "WHERE certified_product_id = :certifiedProductId",
                        CertifiedProductBasicSearchResultEntity.class);
        query.setParameter("certifiedProductId", certifiedProductId);
        List<CertifiedProductBasicSearchResultEntity> searchResult = query.getResultList();
        CertifiedProductBasicSearchResultEntity result = null;
        if (searchResult.size() > 0 && searchResult.get(0) != null) {
            result = searchResult.get(0);
            return convertIcs(result);
        } else {
            return null;
        }
    }

    public List<CertifiedProductFlatSearchResult> getAllCertifiedProducts() {
        LOGGER.info("Starting basic search query.");
        Query query = entityManager.createQuery("SELECT cps "
                + "FROM CertifiedProductBasicSearchResultEntity cps ",
                CertifiedProductBasicSearchResultEntity.class);

        Date startDate = new Date();
        List<CertifiedProductBasicSearchResultEntity> results = query.getResultList();
        Date endDate = new Date();
        LOGGER.info("Got query results in " + (endDate.getTime() - startDate.getTime()) + " millis");
        List<CertifiedProductFlatSearchResult> domainResults = null;

        try {
            domainResults = convertToFlatListings(results);
        } catch (Exception ex) {
            LOGGER.error("Could not convert to flat listings " + ex.getMessage(), ex);
        }
        return domainResults;
    }

    @Deprecated
    public List<CertifiedProductFlatSearchResultLegacy> getAllCertifiedProductsLegacy() {
        LOGGER.info("Starting basic search query.");
        Query query = entityManager.createQuery("SELECT cps "
                + "FROM CertifiedProductBasicSearchResultEntity cps ",
                CertifiedProductBasicSearchResultEntity.class);

        Date startDate = new Date();
        List<CertifiedProductBasicSearchResultEntity> results = query.getResultList();
        Date endDate = new Date();
        LOGGER.info("Got query results in " + (endDate.getTime() - startDate.getTime()) + " millis");
        List<CertifiedProductFlatSearchResultLegacy> domainResults = null;

        try {
            domainResults = convertToFlatListingsLegacy(results);
        } catch (Exception ex) {
            LOGGER.error("Could not convert to flat listings " + ex.getMessage(), ex);
        }
        return domainResults;
    }

    public int getTotalResultCount(final SearchRequest searchRequest) {
        int totalCount = -1;
        String sql = "SELECT count(*) FROM ";
        sql += createFilterQuery(searchRequest);
        Query query = entityManager.createNativeQuery(sql);
        populateFilterQuery(query, searchRequest);
        Object result = query.getSingleResult();
        if (result != null && result instanceof BigInteger) {
            totalCount = ((BigInteger) result).intValue();
        }
        return totalCount;
    }

    public Collection<CertifiedProductBasicSearchResult> search(final SearchRequest searchRequest) {
        //this is always the beginning of the query
        String sql = "SELECT row_number() OVER() as \"unique_id\", certified_product_search_result.* "
                + "FROM "
                + "(SELECT * "
                + "FROM ";
        //this next bit dynamically creates a query to filter the list of
        //certified product ids based on all of the passed-in query parameters
        sql += createFilterQuery(searchRequest);
        //add in paging here because it's search-specific; we don't want it in the total count query
        sql += "WHERE listing_row >= :firstResult AND listing_row < :lastResult ";
        //this is the end, matches up with the beginning sql
        sql += ") filtered_listings_with_rows "
                + "INNER JOIN " + SCHEMA_NAME + ".certified_product_search_result "
                + "ON filtered_listings_with_rows.certified_product_id = "
                + "certified_product_search_result.certified_product_id "
                + "ORDER BY listing_row ";

        Query query = entityManager.createNativeQuery(sql,
                CertifiedProductListingSearchResultEntity.class);

        populateFilterQuery(query, searchRequest);
        populatePagingParams(query, searchRequest);

        List<CertifiedProductListingSearchResultEntity> queryResults = query.getResultList();
        //combine the results together into java objects
        Map<Long, CertifiedProductBasicSearchResult> listingResultsMap
        = new HashMap<Long, CertifiedProductBasicSearchResult>();
        for (CertifiedProductListingSearchResultEntity queryResult : queryResults) {
            CertifiedProductBasicSearchResult currListing = listingResultsMap.get(queryResult.getId());
            if (currListing != null) {
                convertToListing(queryResult, currListing);
            } else {
                CertifiedProductBasicSearchResult newListing = new CertifiedProductBasicSearchResult();
                convertToListing(queryResult, newListing);
                listingResultsMap.put(queryResult.getId(), newListing);
            }
        }
        return listingResultsMap.values();
    }

    private String createFilterQuery(SearchRequest searchRequest) {
        String sql = "(SELECT DISTINCT "
                + "cp.certified_product_id, "
                //all fields that can be ordered by need to go here
                //and they would have to always be joined into the below section
                + "product_name, "
                + "vendor_name, "
                + "product_version, "
                + "year, "
                + "certification_body_name, "
                + "DENSE_RANK() OVER(ORDER BY ";
        if (!StringUtils.isEmpty(searchRequest.getOrderBy())) {
            String orderBy = searchRequest.getOrderBy().trim();
            if (orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_PRODUCT)) {
                sql += " product_name ";
            } else if (orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_DEVELOPER)) {
                sql += " vendor_name ";
            } else if (orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_VERSION)) {
                sql += " product_version ";
            } else if (orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_CERTIFICATION_BODY)) {
                sql += " certification_body_name ";
            } else if (orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_CERTIFICATION_EDITION)) {
                sql += " year ";
            }
            if (searchRequest.getSortDescending() != null && searchRequest.getSortDescending()) {
                sql += " DESC ";
            } else {
                sql += " ASC ";
            }
        }
        sql += ", cp.certified_product_id) listing_row "
                + "FROM " + SCHEMA_NAME + ".certified_product cp ";

        //join in developer, product, and vendor for use in creating chpl id
        sql += "INNER JOIN (SELECT product_version_id, version as \"product_version\", product_id "
                + "FROM " + SCHEMA_NAME + ".product_version) version "
                + "ON cp.product_version_id = version.product_version_id ";
        if (!StringUtils.isEmpty(searchRequest.getVersion())) {
            sql += " AND UPPER(product_version) LIKE :versionName ";
        }

        sql += "INNER JOIN (SELECT product_id, vendor_id, name as \"product_name\" "
                + "FROM " + SCHEMA_NAME + ".product) product "
                + "ON version.product_id = product.product_id ";
        if (!StringUtils.isEmpty(searchRequest.getProduct())) {
            sql += " AND UPPER(product_name) LIKE :productName ";
        }

        sql += "INNER JOIN (SELECT vendor_id, name as \"vendor_name\", vendor_code "
                + "FROM " + SCHEMA_NAME + ".vendor) vendor "
                + "ON product.vendor_id = vendor.vendor_id ";
        if (!StringUtils.isEmpty(searchRequest.getDeveloper())) {
            sql += " AND UPPER(vendor_name) LIKE :developerName ";
        }

        //join in certification editions for creating chpl id
        sql += " INNER JOIN "
                + "(SELECT certification_edition_id, year "
                + "FROM " + SCHEMA_NAME + ".certification_edition) edition "
                + "ON cp.certification_edition_id = edition.certification_edition_id ";
        if (searchRequest.getCertificationEditions() != null && searchRequest.getCertificationEditions().size() > 0) {
            sql += "AND year IN (:editions) ";
        }

        //join in acb for creating chpl id
        sql += "INNER JOIN "
                + "(SELECT certification_body_id, name as \"certification_body_name\", "
                + "acb_code as \"certification_body_code\" "
                + "FROM " + SCHEMA_NAME + ".certification_body) acb "
                + "ON cp.certification_body_id = acb.certification_body_id ";
        if (searchRequest.getCertificationBodies() != null && searchRequest.getCertificationBodies().size() > 0) {
            sql += "AND UPPER(certification_body_name) IN (:acbNames) ";
        }

        //join in developer owner history for use in the searchTerm search; left join b/c not all listings have this
        sql += "LEFT JOIN "
                + "(SELECT name as \"history_vendor_name\", "
                + "product_owner_history_map.product_id as \"history_product_id\" "
                + "FROM " + SCHEMA_NAME + ".vendor "
                + "JOIN " + SCHEMA_NAME + ".product_owner_history_map "
                + "ON vendor.vendor_id = product_owner_history_map.vendor_id "
                + "WHERE product_owner_history_map.deleted = false) prev_vendor_owners "
                + "ON prev_vendor_owners.history_product_id = product.product_id ";

        //certification status
        if (searchRequest.getCertificationStatuses() != null && searchRequest.getCertificationStatuses().size() > 0) {
            sql += "INNER JOIN "
                    + "(SELECT certStatus.certification_status as \"certification_status_name\", "
                    + "cse.certified_product_id as \"certified_product_id\" "
                    + "FROM " + SCHEMA_NAME + ".certification_status_event cse "
                    + "INNER JOIN " + SCHEMA_NAME + ".certification_status certStatus ON "
                    + "cse.certification_status_id = certStatus.certification_status_id "
                    + "INNER JOIN "
                    + "(SELECT certified_product_id, extract(epoch from MAX(event_date)) event_date "
                    + "FROM " + SCHEMA_NAME + ".certification_status_event "
                    + "GROUP BY certified_product_id) maxCse "
                    + "ON cse.certified_product_id = maxCse.certified_product_id "
                    + "AND extract(epoch from cse.event_date) = maxCse.event_date "
                    + ") lastCertStatusEvent "
                    + "ON lastCertStatusEvent.certified_product_id = cp.certified_product_id "
                    + "AND UPPER(certification_status_name) IN (:certStatuses)";
        }

        //certification date range
        if (!StringUtils.isEmpty(searchRequest.getCertificationDateStart())
                || !StringUtils.isEmpty(searchRequest.getCertificationDateEnd())) {
            sql += "INNER JOIN "
                    + "(SELECT MIN(event_date) as \"certification_date\", certified_product_id "
                    + "FROM " + SCHEMA_NAME + ".certification_status_event "
                    + "WHERE certification_status_id = 1 "
                    + "GROUP BY (certified_product_id)) certStatusEvent "
                    + "ON cp.certified_product_id = certStatusEvent.certified_product_id ";
            if (!StringUtils.isEmpty(searchRequest.getCertificationDateStart())) {
                sql += " AND certification_date > :certificationDateStart ";
            }
            if (!StringUtils.isEmpty(searchRequest.getCertificationDateEnd())) {
                sql += " AND certification_date < :certificationDateEnd ";
            }
        }

        //practice type
        if (!StringUtils.isEmpty(searchRequest.getPracticeType())) {
            sql += "INNER JOIN "
                    + "(SELECT practice_type_id, name as \"practice_type_name\" "
                    + "FROM " + SCHEMA_NAME + ".practice_type) prac "
                    + "ON cp.practice_type_id = prac.practice_type_id "
                    + "AND UPPER(practice_type_name) LIKE :practiceTypeName ";
        }

        //criteria
        if (searchRequest.getCertificationCriteria() != null && searchRequest.getCertificationCriteria().size() > 0) {
            String criteriaSql = "INNER JOIN "
                    + "(SELECT certification_criterion.number as \"cert_number\", "
                    + "certification_result.certified_product_id "
                    + "FROM " + SCHEMA_NAME + ".certification_result "
                    + "JOIN " + SCHEMA_NAME + ".certification_criterion "
                    + "ON certification_criterion.certification_criterion_id = "
                    + "certification_result.certification_criterion_id "
                    + "AND certification_criterion.deleted = false "
                    + "WHERE certification_result.success = true "
                    + "AND certification_result.deleted = false) ";
            if (searchRequest.getCertificationCriteriaOperator() == null
                    || searchRequest.getCertificationCriteriaOperator() == SearchSetOperator.OR) {
                //ORing together
                criteriaSql += " certs "
                        + "ON certs.certified_product_id = cp.certified_product_id "
                        + "AND UPPER(certs.cert_number) IN (:criteriaList) ";
                sql += criteriaSql;
            } else {
                //ANDing together
                for (int i = 0; i < searchRequest.getCertificationCriteria().size(); i++) {
                    //aliased table name has to be unique so we are appending the index to it
                    sql += criteriaSql
                            + " certs_" + i + " "
                            + "ON certs_" + i + ".certified_product_id = cp.certified_product_id "
                            + "AND UPPER(certs_" + i + ".cert_number) LIKE :criteria" + i + " ";
                }
            }
        }

        //cqms
        if (searchRequest.getCqms() != null && searchRequest.getCqms().size() > 0) {
            String cqmSql = "INNER JOIN "
                    + "(SELECT COALESCE(cms_id, 'NQF-'||nqf_number) as \"cqm_number\", certified_product_id "
                    + "FROM " + SCHEMA_NAME + ".cqm_result, " + SCHEMA_NAME + ".cqm_criterion "
                    + "WHERE cqm_criterion.cqm_criterion_id = cqm_result.cqm_criterion_id "
                    + "AND cqm_criterion.deleted = false "
                    + "AND cqm_result.success = true "
                    + "AND cqm_result.deleted = false) ";
            if (searchRequest.getCqmsOperator() == null
                    || searchRequest.getCqmsOperator() == SearchSetOperator.OR) {
                //ORing together
                cqmSql += " cqms "
                        + "ON cqms.certified_product_id = cp.certified_product_id "
                        + " AND UPPER(cqms.cqm_number) IN (:cqmList) ";
                sql += cqmSql;
            } else {
                //ANDing together
                for (int i = 0; i < searchRequest.getCqms().size(); i++) {
                    sql += cqmSql
                            + " cqms_" + i + " "
                            + "ON cqms_" + i + ".certified_product_id = cp.certified_product_id "
                            + "AND UPPER(cqms_" + i + ".cqm_number) LIKE :cqm" + i + " ";
                }
            }
        }

        //surveillance and nonconformity counts
        sql += "LEFT JOIN "
                + "(SELECT certified_product_id, count(*) as \"count_surveillance_activities\" "
                + "FROM " + SCHEMA_NAME + ".surveillance "
                + "WHERE " + SCHEMA_NAME + ".surveillance.deleted <> true "
                + "GROUP BY certified_product_id) survs "
                + "ON cp.certified_product_id = survs.certified_product_id "
                + "LEFT JOIN "
                + "(SELECT certified_product_id, count(*) as \"count_open_nonconformities\" "
                + "FROM " + SCHEMA_NAME + ".surveillance surv "
                + "JOIN " + SCHEMA_NAME + ".surveillance_requirement surv_req ON surv.id = "
                + "surv_req.surveillance_id AND surv_req.deleted <> true "
                + "JOIN " + SCHEMA_NAME + ".surveillance_nonconformity surv_nc ON surv_req.id = "
                + "surv_nc.surveillance_requirement_id AND surv_nc.deleted <> true "
                + "JOIN " + SCHEMA_NAME + ".nonconformity_status nc_status ON surv_nc.nonconformity_status_id = nc_status.id "
                + "WHERE surv.deleted <> true AND nc_status.name = 'Open' "
                + "GROUP BY certified_product_id) nc_open "
                + "ON cp.certified_product_id = nc_open.certified_product_id "
                + "LEFT JOIN "
                + "(SELECT certified_product_id, count(*) as \"count_closed_nonconformities\" "
                + "FROM " + SCHEMA_NAME + ".surveillance surv "
                + "JOIN " + SCHEMA_NAME + ".surveillance_requirement surv_req ON surv.id = "
                + "surv_req.surveillance_id AND surv_req.deleted <> true "
                + "JOIN " + SCHEMA_NAME + ".surveillance_nonconformity surv_nc ON surv_req.id = "
                + "surv_nc.surveillance_requirement_id AND surv_nc.deleted <> true "
                + "JOIN " + SCHEMA_NAME + ".nonconformity_status nc_status ON surv_nc.nonconformity_status_id = nc_status.id "
                + "WHERE surv.deleted <> true AND nc_status.name = 'Closed' "
                + "GROUP BY certified_product_id) nc_closed "
                + "ON cp.certified_product_id = nc_closed.certified_product_id";

        //everything else is not joined in
        //but instead gets added after there WHERE
        sql +=
                " WHERE cp.deleted != true ";

        //search term is supplied and treated differently if it looks
        //like a chpl id or not
        if (!StringUtils.isEmpty(searchRequest.getSearchTerm())) {
            String searchTerm = searchRequest.getSearchTerm();
            if (searchTerm.startsWith("CHP-")
                    || Pattern.matches(ChplProductNumberUtil.CHPL_PRODUCT_NUMBER_SEARCH_REGEX, searchTerm.trim())) {
                sql += "AND "
                        + "UPPER(" + SCHEMA_NAME + ".get_chpl_product_number_as_text(cp.certified_product_id)) LIKE :searchTerm";
            } else {
                sql += "AND"
                        + "(UPPER(vendor_name) LIKE :searchTerm OR "
                        + "UPPER(history_vendor_name) LIKE :searchTerm OR "
                        + "UPPER(product_name) LIKE :searchTerm OR "
                        + "UPPER(acb_certification_id) LIKE :searchTerm)";
            }
        }

        //surveillance search options
        if (searchRequest.getSurveillance() != null) {
            if (searchRequest.getSurveillance().getHasHadSurveillance() != null
                    && searchRequest.getSurveillance().getHasHadSurveillance()) {
                sql += " AND count_surveillance_activities > 0 ";
            } else if (searchRequest.getSurveillance().getHasHadSurveillance() != null
                    && !searchRequest.getSurveillance().getHasHadSurveillance()) {
                sql += " AND count_surveillance_activities IS NULL ";
            }

            if (searchRequest.getSurveillance().getNonconformityOptions() != null
                    &&  searchRequest.getSurveillance().getNonconformityOptions().size() > 0) {
                sql += " AND (";
                int i = 0;
                for (NonconformitySearchOptions ncSearchOpt : searchRequest.getSurveillance().getNonconformityOptions()) {
                    if (ncSearchOpt == NonconformitySearchOptions.CLOSED_NONCONFORMITY) {
                        sql += " count_closed_nonconformities > 0 ";
                    } else if (ncSearchOpt == NonconformitySearchOptions.NEVER_NONCONFORMITY) {
                        sql += " (count_open_nonconformities IS NULL AND count_closed_nonconformities IS NULL) ";
                    } else if (ncSearchOpt == NonconformitySearchOptions.OPEN_NONCONFORMITY) {
                        sql += " count_open_nonconformities > 0 ";
                    }

                    if (i < searchRequest.getSurveillance().getNonconformityOptions().size() - 1) {
                        if (searchRequest.getSurveillance().getNonconformityOptionsOperator() == null
                                || searchRequest.getSurveillance()
                                .getNonconformityOptionsOperator() == SearchSetOperator.OR) {
                            sql += " OR ";
                        } else {
                            sql += " AND ";
                        }
                    }
                    i++;
                }
                sql += ")";
            }
        }

        //order and paging
        sql += " ORDER BY ";
        if (!StringUtils.isEmpty(searchRequest.getOrderBy())) {
            String orderBy = searchRequest.getOrderBy().trim();
            if (orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_PRODUCT)) {
                sql += " product_name ";
            } else if (orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_DEVELOPER)) {
                sql += " vendor_name ";
            } else if (orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_VERSION)) {
                sql += " product_version ";
            } else if (orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_CERTIFICATION_BODY)) {
                sql += " certification_body_name ";
            } else if (orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_CERTIFICATION_EDITION)) {
                sql += " year ";
            }
            if (searchRequest.getSortDescending() != null && searchRequest.getSortDescending()) {
                sql += " DESC ";
            } else {
                sql += " ASC ";
            }
        }
        sql += ", certified_product_id) filtered_certified_product_ids ";
        return sql;
    }

    private void populateFilterQuery(final Query query, final SearchRequest searchRequest) {
        if (!StringUtils.isEmpty(searchRequest.getVersion())) {
            query.setParameter("versionName", "%" + searchRequest.getVersion().toUpperCase() + "%");
        }
        if (!StringUtils.isEmpty(searchRequest.getProduct())) {
            query.setParameter("productName", "%" + searchRequest.getProduct().toUpperCase() + "%");
        }
        if (!StringUtils.isEmpty(searchRequest.getDeveloper())) {
            query.setParameter("developerName", "%" + searchRequest.getDeveloper().toUpperCase() + "%");
        }
        if (searchRequest.getCertificationEditions() != null && searchRequest.getCertificationEditions().size() > 0) {
            query.setParameter("editions", searchRequest.getCertificationEditions());
        }
        if (searchRequest.getCertificationBodies() != null && searchRequest.getCertificationBodies().size() > 0) {
            Set<String> acbsUppercase = new HashSet<String>(searchRequest.getCertificationBodies().size());
            for (String acb : searchRequest.getCertificationBodies()) {
                acbsUppercase.add(acb.toUpperCase());
            }
            query.setParameter("acbNames", acbsUppercase);
        }
        if (searchRequest.getCertificationStatuses() != null && searchRequest.getCertificationStatuses().size() > 0) {
            Set<String> certStatusesUppercase = new HashSet<String>(searchRequest.getCertificationStatuses().size());
            for (String status : searchRequest.getCertificationStatuses()) {
                certStatusesUppercase.add(status.toUpperCase());
            }
            query.setParameter("certStatuses", certStatusesUppercase);
        }
        if (!StringUtils.isEmpty(searchRequest.getCertificationDateStart())) {
            try {
                Date date = certificationDateFormatter.parse(searchRequest.getCertificationDateStart());
                query.setParameter("certificationDateStart", date);
            } catch (Exception ex) {
                LOGGER.error("Could not parse " + searchRequest.getCertificationDateStart()
                + " as a date in the format "
                + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
            }
        }
        if (!StringUtils.isEmpty(searchRequest.getCertificationDateEnd())) {
            try {
                Date date = certificationDateFormatter.parse(searchRequest.getCertificationDateEnd());
                query.setParameter("certificationDateEnd", date);
            } catch (Exception ex) {
                LOGGER.error("Could not parse " + searchRequest.getCertificationDateEnd()
                + " as a date in the format "
                + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
            }
        }
        if (!StringUtils.isEmpty(searchRequest.getPracticeType())) {
            query.setParameter("practiceTypeName", "%" + searchRequest.getPracticeType().toUpperCase() + "%");
        }
        if (searchRequest.getCertificationCriteria() != null && searchRequest.getCertificationCriteria().size() > 0) {
            if (searchRequest.getCertificationCriteriaOperator() == null
                    || searchRequest.getCertificationCriteriaOperator() == SearchSetOperator.OR) {
                Set<String> criteriaUppercase = new HashSet<String>(searchRequest.getCertificationCriteria().size());
                for (String criteria : searchRequest.getCertificationCriteria()) {
                    criteriaUppercase.add(criteria.toUpperCase());
                }
                query.setParameter("criteriaList", criteriaUppercase);
            } else {
                int i = 0;
                for (String criteria : searchRequest.getCertificationCriteria()) {
                    query.setParameter("criteria" + i, criteria.toUpperCase());
                    i++;
                }
            }
        }
        if (searchRequest.getCqms() != null && searchRequest.getCqms().size() > 0) {
            if (searchRequest.getCqmsOperator() == null
                    || searchRequest.getCqmsOperator() == SearchSetOperator.OR) {
                Set<String> cqmsUppercase = new HashSet<String>(searchRequest.getCqms().size());
                for (String cqm : searchRequest.getCqms()) {
                    cqmsUppercase.add(cqm.toUpperCase());
                }
                query.setParameter("cqmList", cqmsUppercase);
            } else {
                int i = 0;
                for (String cqm : searchRequest.getCqms()) {
                    query.setParameter("cqm" + i, cqm.toUpperCase());
                    i++;
                }
            }
        }
        if (!StringUtils.isEmpty(searchRequest.getSearchTerm())) {
            query.setParameter("searchTerm", "%" + searchRequest.getSearchTerm().toUpperCase() + "%");
        }
    }

    private void populatePagingParams(final Query query, final SearchRequest searchRequest) {
        int firstResult = (searchRequest.getPageNumber() * searchRequest.getPageSize()) + 1;
        int lastResult = firstResult + searchRequest.getPageSize();
        query.setParameter("firstResult", firstResult);
        query.setParameter("lastResult", lastResult);
    }

    private List<CertifiedProductFlatSearchResult> convertToFlatListings(List<CertifiedProductBasicSearchResultEntity> dbResults) {
        List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>(
                dbResults.size());
        return dbResults.stream()
            .map(dbResult -> buildFlatSearchResult(dbResult))
            .collect(Collectors.toList());
    }

    private CertifiedProductFlatSearchResult buildFlatSearchResult(CertifiedProductBasicSearchResultEntity entity) {
        return CertifiedProductFlatSearchResult.builder()
                .id(entity.getId())
                .chplProductNumber(entity.getChplProductNumber())
                .edition(entity.getEdition())
                .curesUpdate(entity.getCuresUpdate())
                .acb(entity.getAcbName())
                .acbCertificationId(entity.getAcbCertificationId())
                .practiceType(entity.getPracticeTypeName())
                .developerId(entity.getDeveloperId())
                .developer(entity.getDeveloper())
                .developerStatus(entity.getDeveloperStatus())
                .product(entity.getProduct())
                .version(entity.getVersion())
                .numMeaningfulUse(entity.getMeaningfulUseUserCount())
                .numMeaningfulUseDate(entity.getMeaningfulUseUserDate() != null
                    ? entity.getMeaningfulUseUserDate().getTime() : null)
                .decertificationDate(entity.getDecertificationDate() == null ? null : entity.getDecertificationDate().getTime())
                .certificationDate(entity.getCertificationDate().getTime())
                .certificationStatus(entity.getCertificationStatus())
                .transparencyAttestationUrl(entity.getTransparencyAttestationUrl())
                .apiDocumentation(entity.getApiDocumentation())
                .serviceBaseUrlList(entity.getServiceBaseUrlList() != null ? entity.getServiceBaseUrlList() : "")
                .surveillanceCount(entity.getSurveillanceCount())
                .openSurveillanceCount(entity.getOpenSurveillanceCount())
                .closedSurveillanceCount(entity.getClosedSurveillanceCount())
                .openSurveillanceNonConformityCount(entity.getOpenSurveillanceNonConformityCount())
                .closedSurveillanceNonConformityCount(entity.getClosedSurveillanceNonConformityCount())
                .surveillanceDates(entity.getSurveillanceDates())
                .statusEvents(entity.getStatusEvents())
                .criteriaMet(entity.getCerts())
                .cqmsMet(entity.getCqms())
                .previousDevelopers(entity.getPreviousDevelopers())
                .build();
    }

    @Deprecated
    private void convertToListing(CertifiedProductListingSearchResultEntity queryResult,
            CertifiedProductBasicSearchResult listing) {
        listing.setId(queryResult.getId());
        listing.setChplProductNumber(queryResult.getChplProductNumber());
        listing.setCertificationStatus(queryResult.getCertificationStatus());
        listing.setNumMeaningfulUse(queryResult.getMeaningfulUseUserCount());
        listing.setNumMeaningfulUseDate(
                queryResult.getMeaningfulUseUsersDate() != null ? queryResult.getMeaningfulUseUsersDate().getTime() : null);
        listing.setTransparencyAttestationUrl(queryResult.getTransparencyAttestationUrl());
        listing.setEdition(queryResult.getEdition());
        listing.setCuresUpdate(queryResult.getCuresUpdate());
        listing.setAcb(queryResult.getAcbName());
        listing.setAcbCertificationId(queryResult.getAcbCertificationId());
        listing.setPracticeType(queryResult.getPracticeTypeName());
        listing.setVersion(queryResult.getVersion());
        listing.setProduct(queryResult.getProduct());
        listing.setDeveloper(queryResult.getDeveloper());
        listing.setSurveillanceCount(queryResult.getCountSurveillance().longValue());
        listing.setOpenNonconformityCount(queryResult.getCountOpenSurveillanceNonconformities().longValue());
        listing.setClosedNonconformityCount(queryResult.getCountClosedSurveillanceNonconformities().longValue());

        if (queryResult.getCertificationDate() != null) {
            listing.setCertificationDate(queryResult.getCertificationDate().getTime());
        }

        if (queryResult.getDecertificationDate() != null) {
            listing.setDecertificationDate(queryResult.getDecertificationDate().getTime());
        }

        if (queryResult.getPreviousDeveloperOwner() != null) {
            listing.getPreviousDevelopers().add(queryResult.getPreviousDeveloperOwner());
        }

        if (queryResult.getCert() != null) {
            listing.getCriteriaMet().add(queryResult.getCert());
        }

        if (queryResult.getCqm() != null) {
            listing.getCqmsMet().add(queryResult.getCqm());
        }
    }

    @Deprecated
    private List<CertifiedProductFlatSearchResultLegacy> convertToFlatListingsLegacy(
            List<CertifiedProductBasicSearchResultEntity> dbResults) {
        List<CertifiedProductFlatSearchResultLegacy> results = new ArrayList<CertifiedProductFlatSearchResultLegacy>(
                dbResults.size());
        for (CertifiedProductBasicSearchResultEntity dbResult : dbResults) {
            CertifiedProductFlatSearchResultLegacy result = new CertifiedProductFlatSearchResultLegacy();
            result.setId(dbResult.getId());
            result.setChplProductNumber(dbResult.getChplProductNumber());
            result.setEdition(dbResult.getEdition());
            result.setCuresUpdate(dbResult.getCuresUpdate());
            result.setAcb(dbResult.getAcbName());
            result.setAcbCertificationId(dbResult.getAcbCertificationId());
            result.setPracticeType(dbResult.getPracticeTypeName());
            result.setDeveloper(dbResult.getDeveloper());
            result.setDeveloperStatus(dbResult.getDeveloperStatus());
            result.setProduct(dbResult.getProduct());
            result.setVersion(dbResult.getVersion());
            result.setNumMeaningfulUse(dbResult.getMeaningfulUseUserCount());
            result.setNumMeaningfulUseDate(dbResult.getMeaningfulUseUserDate() != null
                    ? dbResult.getMeaningfulUseUserDate().getTime() : null);
            result.setDecertificationDate(
                    dbResult.getDecertificationDate() == null ? null : dbResult.getDecertificationDate().getTime());
            result.setCertificationDate(dbResult.getCertificationDate().getTime());
            result.setCertificationStatus(dbResult.getCertificationStatus());
            result.setTransparencyAttestationUrl(dbResult.getTransparencyAttestationUrl());
            result.setApiDocumentation(dbResult.getApiDocumentation());
            result.setSurveillanceCount(dbResult.getSurveillanceCount());
            result.setOpenSurveillanceCount(dbResult.getOpenSurveillanceCount());
            result.setClosedSurveillanceCount(dbResult.getClosedSurveillanceCount());
            result.setOpenNonconformityCount(dbResult.getOpenSurveillanceNonConformityCount());
            result.setClosedNonconformityCount(dbResult.getClosedSurveillanceNonConformityCount());
            result.setSurveillanceDates(dbResult.getSurveillanceDates());
            result.setCriteriaMet(dbResult.getCerts());
            result.setCqmsMet(dbResult.getCqms());
            result.setPreviousDevelopers(dbResult.getPreviousDevelopers());

            results.add(result);
        }
        return results;
    }

    private IcsFamilyTreeNode convertIcs(final CertifiedProductBasicSearchResultEntity result) {
        IcsFamilyTreeNode node = new IcsFamilyTreeNode();
        node.setId(result.getId());
        node.setChplProductNumber(result.getChplProductNumber());
        node.setCertificationDate(result.getCertificationDate());
        CertificationStatus cs = new CertificationStatus();
        cs.setName(result.getCertificationStatus());
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
