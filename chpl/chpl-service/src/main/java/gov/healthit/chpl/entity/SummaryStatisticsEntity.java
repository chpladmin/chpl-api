package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import gov.healthit.chpl.entity.datatypes.StringJsonUserType;

@Entity
@Table(name = "summary_statistics")
@TypeDefs( {@TypeDef( name= "StringJsonObject", typeClass = StringJsonUserType.class)})
public class SummaryStatisticsEntity implements Serializable{
    private static final long serialVersionUID = 4752929481454934958L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_statistics_id", nullable = false)
    private Long summaryStatisticsId;
    
    @Column(name = "end_Date", nullable = false)
    private Date endDate;
    
    @Column(name = "summary_statistics")
    @Type(type = "StringJsonObject")
    private String summaryStatistics;
    
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;
    
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;
    
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;
    
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
    
    public Long getSummaryStatisticsId() {
        return summaryStatisticsId;
    }
    
    public void setSummaryStatisticsId(Long summaryStatisticsId) {
        this.summaryStatisticsId = summaryStatisticsId;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public String getSummaryStatistics() {
        return summaryStatistics;
    }
    
    public void setSummaryStatistics(String summaryStatistics) {
        this.summaryStatistics = summaryStatistics;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    
    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }
    
    public void setLastModifiedUser(Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
