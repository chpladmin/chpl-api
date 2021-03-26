package gov.healthit.chpl.scheduler.job.urlStatus.data;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlResult {
    private Long id;
    private String url;
    private UrlType urlType;
    private Date lastChecked;
    private Integer responseCode;
    private String responseMessage;

    public UrlResult(UrlResultEntity entity) {
        BeanUtils.copyProperties(entity, this);
        if (this.urlType == null) {
            if (entity.getUrlType() != null) {
                this.urlType = UrlType.findByName(entity.getUrlType().getName());
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UrlResult)) {
            return false;
        }
        UrlResult otherDto = (UrlResult) obj;
        //equal if ids are the same
        if (otherDto.getId() != null && this.getId() != null
                && otherDto.getId().equals(this.getId())) {
            return true;
        }
        //if ids aren't both there, check the url and the type to determine equality
        if (!StringUtils.isEmpty(otherDto.getUrl()) && !StringUtils.isEmpty(this.getUrl())
                && otherDto.getUrl().equals(this.getUrl())
                && otherDto.getUrlType() != null && this.getUrlType() != null
                && otherDto.getUrlType() == this.getUrlType()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (StringUtils.isEmpty(this.getUrl()) || this.getLastChecked() == null) {
            return -1;
        }
        return this.getUrl().hashCode() + this.getLastChecked().hashCode();
    }
}
