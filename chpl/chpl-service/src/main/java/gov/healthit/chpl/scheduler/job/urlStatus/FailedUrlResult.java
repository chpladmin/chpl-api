package gov.healthit.chpl.scheduler.job.urlStatus;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FailedUrlResult {
    private String url;
    private UrlType urlType;
    private Date lastChecked;
    private Integer responseCode;
    private String responseMessage;

    //not all of the below fields are relevant to every failure
    //it depends on the type of url (developer website, listing report, etc)
    private String atlName;
    private String acbName;
    private String developerName;
    private String productName;
    private String version;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String chplProductNumber;
    private String edition;
    private String certificationStatus;
    private Date certificationDate;
    private String criteria;

    public FailedUrlResult(UrlResult urlResult) {
        BeanUtils.copyProperties(urlResult, this);
    }
}
