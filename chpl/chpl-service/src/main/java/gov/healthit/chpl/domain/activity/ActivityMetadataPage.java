package gov.healthit.chpl.domain.activity;

import java.io.Serializable;
import java.util.Set;

/**
 * A page of activity metadata including the page number, page size,
 * and total result set size.
 * @author kekey
 *
 */
public class ActivityMetadataPage implements Serializable {
    private static final long serialVersionUID = -3855142961571461535L;

    private Integer pageNum;
    private Integer pageSize;
    private Long resultSetSize;
    private Set<ActivityMetadata> activities;

    public ActivityMetadataPage() {
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getResultSetSize() {
        return resultSetSize;
    }

    public void setResultSetSize(Long resultSetSize) {
        this.resultSetSize = resultSetSize;
    }

    public Set<ActivityMetadata> getActivities() {
        return activities;
    }

    public void setActivities(Set<ActivityMetadata> activities) {
        this.activities = activities;
    }
}
