package gov.healthit.chpl.api.domain;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey implements Serializable {
    private static final long serialVersionUID = -3412202704187626073L;
    private Long id;
    private String name;
    private String email;
    private String key;
    private Date lastUsedDate;
    private Date deleteWarningSentDate;

    @JsonIgnore
    @XmlTransient
    private boolean unrestricted;
}
