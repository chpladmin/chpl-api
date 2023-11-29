package gov.healthit.chpl.realworldtesting.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUrlByDeveloper;
import lombok.Data;

@Entity
@Data
@Immutable
@Table(name = "rwt_results_by_developer")
public class RealWorldTestingResultsUrlsByDeveloper {

    @EmbeddedId
    private RealWorldTestingResultsUrlsId id;

    @Column(name = "rwt_results_url", insertable = false, updatable = false)
    private String rwtResultsUrl;

    @Column(name = "developer_id", insertable = false, updatable = false)
    private Long developerId;

    @Column(name = " active_certificate_count", insertable = false, updatable = false)
    private Long activeCertificateCount;

    public RealWorldTestingUrlByDeveloper toDomain() {
        return RealWorldTestingUrlByDeveloper.builder()
                .url(rwtResultsUrl)
                .activeCertificateCount(activeCertificateCount)
                .build();
    }
}

@Embeddable
@Data
class RealWorldTestingResultsUrlsId implements Serializable {
    private static final long serialVersionUID = 47791724871718281L;

    @Column(name = "rwt_results_url", insertable = false, updatable = false)
    private String rwtResultsUrl;

    @Column(name = "developer_id", insertable = false, updatable = false)
    private Long developerId;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rwtResultsUrl == null) ? 0 : rwtResultsUrl.hashCode());
        result = prime * result + ((developerId == null) ? 0 : developerId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RealWorldTestingResultsUrlsId other = (RealWorldTestingResultsUrlsId) obj;
        if (rwtResultsUrl == null) {
            if (other.rwtResultsUrl != null) {
                return false;
            }
        } else if (!rwtResultsUrl.equals(other.rwtResultsUrl)) {
            return false;
        }
        if (developerId == null) {
            if (other.developerId != null) {
                return false;
            }
        } else if (!developerId.equals(other.developerId)) {
            return false;
        }
        return true;
    }

}


