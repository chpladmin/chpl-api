package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectReview implements Serializable {
    private static final long serialVersionUID = 7018071377912371691L;

    /**
     * Direct Review JIRA internal key
     */
    @XmlElement(required = true)
    private String key;

    /**
     * Date direct review began
     */
    @XmlElement(required = true)
    private Date startDate;

    /**
     * Date direct review ended
     */
    @XmlElement(required = false, nillable = true)
    private Date endDate;

    /**
     * Date the direct review was created.
     */
    @XmlElement(required = true)
    private Date createdDate;

    /**
     * Date of the last modification of the direct review.
     */
    @XmlElement(required = true)
    private Date lastModifiedDate;

    public DirectReview() {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
