package gov.healthit.chpl.servicebaseurllist;

import java.io.Serializable;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Immutable
@Table(name = "service_base_url_list_by_developer")
public class ServiceBaseUrlListUrlsByDeveloperEntity {

    @EmbeddedId
    private ServiceBaseUrlListUrlsId id;

    @Column(name = "service_base_url_list", insertable = false, updatable = false)
    private String sbulUrl;

    @Column(name = "developer_id", insertable = false, updatable = false)
    private Long developerId;

    public ServiceBaseUrlListByDeveloper toDomain() {
        return ServiceBaseUrlListByDeveloper.builder()
                .url(sbulUrl)
                .build();
    }
}

@Embeddable
@Data
class ServiceBaseUrlListUrlsId implements Serializable {
    private static final long serialVersionUID = 377917248715518861L;

    @Column(name = "service_base_url_list", insertable = false, updatable = false)
    private String sbulUrl;

    @Column(name = "developer_id", insertable = false, updatable = false)
    private Long developerId;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sbulUrl == null) ? 0 : sbulUrl.hashCode());
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
        ServiceBaseUrlListUrlsId other = (ServiceBaseUrlListUrlsId) obj;
        if (sbulUrl == null) {
            if (other.sbulUrl != null) {
                return false;
            }
        } else if (!sbulUrl.equals(other.sbulUrl)) {
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


