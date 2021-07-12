package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.search.domain.SearchSetOperator;

@Deprecated
public class SearchRequestLegacy implements Serializable {
    private static final long serialVersionUID = 1179207628639701580L;

    public static final String CERTIFICATION_DATE_SEARCH_FORMAT = "yyyy-MM-dd";
    public static final int MAX_PAGE_SIZE = 100;
    public static final String ORDER_BY_DEVELOPER = "developer";
    public static final String ORDER_BY_PRODUCT = "product";
    public static final String ORDER_BY_VERSION = "version";
    public static final String ORDER_BY_CERTIFICATION_EDITION = "certificationEdition";
    public static final String ORDER_BY_CERTIFICATION_BODY = "certificationBody";

    String searchTerm = null;

    // search for any of these
    Set<String> certificationStatuses = new HashSet<String>();
    // search for any of these
    Set<String> certificationEditions = new HashSet<String>();

    Set<String> certificationCriteria = new HashSet<String>();
    //AND or OR the certification criteria together
    SearchSetOperator certificationCriteriaOperator = SearchSetOperator.OR;

    Set<String> cqms = new HashSet<String>();
    //AND or OR the cqms together
    SearchSetOperator cqmsOperator = SearchSetOperator.OR;

    // search for any of these
    Set<String> certificationBodies = new HashSet<String>();

    String developer = null;
    String product = null;
    String version = null;
    String practiceType = null;
    String certificationDateStart = null;
    String certificationDateEnd = null;
    SurveillanceSearchFilter surveillance = new SurveillanceSearchFilter();

    String orderBy = ORDER_BY_PRODUCT;
    Boolean sortDescending = false;
    Integer pageNumber = 0;
    Integer pageSize = 20;

    public void cleanAllParameters() {
        if (!StringUtils.isEmpty(searchTerm)) {
            setSearchTerm(searchTerm.trim());
        }

        if (!StringUtils.isEmpty(developer)) {
            setDeveloper(developer.trim());
        }

        if (!StringUtils.isEmpty(product)) {
            setProduct(product.trim());
        }

        if (!StringUtils.isEmpty(version)) {
            setVersion(version.trim());
        }

        if (!StringUtils.isEmpty(practiceType)) {
            setPracticeType(practiceType.trim());
        }

        if (!StringUtils.isEmpty(certificationDateStart)) {
            setCertificationDateStart(certificationDateStart.trim());
        }

        if (!StringUtils.isEmpty(certificationDateEnd)) {
            setCertificationDateEnd(certificationDateEnd.trim());
        }

        if (!StringUtils.isEmpty(orderBy)) {
            setOrderBy(orderBy.trim());
        }

        if (certificationStatuses != null && certificationStatuses.size() > 0) {
            Set<String> trimmedCertificationStatuses = new HashSet<String>(certificationStatuses.size());
            for (String certificationStatus : certificationStatuses) {
                trimmedCertificationStatuses.add(certificationStatus.trim());
            }
            setCertificationStatuses(trimmedCertificationStatuses);
        }

        if (certificationEditions != null && certificationEditions.size() > 0) {
            Set<String> trimmedCertificationEditions = new HashSet<String>(certificationEditions.size());
            for (String certificationEdition : certificationEditions) {
                trimmedCertificationEditions.add(certificationEdition.trim());
            }
            setCertificationEditions(trimmedCertificationEditions);
        }

        if (certificationBodies != null && certificationBodies.size() > 0) {
            Set<String> trimmedCertificationBodies = new HashSet<String>(certificationBodies.size());
            for (String certificationBody : certificationBodies) {
                trimmedCertificationBodies.add(certificationBody.trim());
            }
            setCertificationBodies(trimmedCertificationBodies);
        }

        if (certificationCriteria != null && certificationCriteria.size() > 0) {
            Set<String> trimmedCertificationCriteria = new HashSet<String>(certificationCriteria.size());
            for (String certificationCriterion : certificationCriteria) {
                trimmedCertificationCriteria.add(certificationCriterion.trim());
            }
            setCertificationCriteria(trimmedCertificationCriteria);
        }

        if (cqms != null && cqms.size() > 0) {
            Set<String> trimmedCqms = new HashSet<String>(cqms.size());
            for (String cqm : cqms) {
                trimmedCqms.add(cqm.trim());
            }
            setCqms(trimmedCqms);
        }
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(final String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Set<String> getCertificationStatuses() {
        return certificationStatuses;
    }

    public void setCertificationStatuses(final Set<String> certificationStatuses) {
        this.certificationStatuses = certificationStatuses;
    }

    public Set<String> getCertificationEditions() {
        return certificationEditions;
    }

    public void setCertificationEditions(final Set<String> certificationEditions) {
        this.certificationEditions = certificationEditions;
    }

    public Set<String> getCertificationCriteria() {
        return certificationCriteria;
    }

    public void setCertificationCriteria(final Set<String> certificationCriteria) {
        this.certificationCriteria = certificationCriteria;
    }

    public Set<String> getCqms() {
        return cqms;
    }

    public void setCqms(final Set<String> cqms) {
        this.cqms = cqms;
    }

    public Set<String> getCertificationBodies() {
        return certificationBodies;
    }

    public void setCertificationBodies(final Set<String> certificationBodies) {
        this.certificationBodies = certificationBodies;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(final String developer) {
        this.developer = developer;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(final String product) {
        this.product = product;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getPracticeType() {
        return practiceType;
    }

    public void setPracticeType(final String practiceType) {
        this.practiceType = practiceType;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(final String orderBy) {
        this.orderBy = orderBy;
    }

    public Boolean getSortDescending() {
        return sortDescending;
    }

    public void setSortDescending(final Boolean sortDescending) {
        this.sortDescending = sortDescending;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(final Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(final Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getCertificationDateStart() {
        return certificationDateStart;
    }

    public void setCertificationDateStart(final String certificationDateStart) {
        this.certificationDateStart = certificationDateStart;
    }

    public String getCertificationDateEnd() {
        return certificationDateEnd;
    }

    public void setCertificationDateEnd(final String certificationDateEnd) {
        this.certificationDateEnd = certificationDateEnd;
    }

    public SurveillanceSearchFilter getSurveillance() {
        return surveillance;
    }

    public void setSurveillance(SurveillanceSearchFilter surveillance) {
        this.surveillance = surveillance;
    }

    public SearchSetOperator getCertificationCriteriaOperator() {
        return certificationCriteriaOperator;
    }

    public void setCertificationCriteriaOperator(SearchSetOperator certificationCriteriaOperator) {
        this.certificationCriteriaOperator = certificationCriteriaOperator;
    }

    public SearchSetOperator getCqmsOperator() {
        return cqmsOperator;
    }

    public void setCqmsOperator(SearchSetOperator cqmsOperator) {
        this.cqmsOperator = cqmsOperator;
    }
}
