package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * The targeted users of a Health IT Module, as identified by the developer. For
 * example, "Ambulatory pediatricians"
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertifiedProductTargetedUser implements Serializable {
    private static final long serialVersionUID = -2078691100124619582L;

    /**
     * Targeted user to listing mapping internal ID
     */
    @Schema(description = "Targeted user to listing mapping internal ID")
    @XmlElement(required = true)
    private Long id;

    /**
     * Targeted user internal ID
     */
    @Schema(description = "Targeted user internal ID")
    @XmlElement(required = true)
    private Long targetedUserId;

    /**
     * Targeted user name
     */
    @Schema(description = "Targeted user name")
    @XmlElement(required = true)
    private String targetedUserName;

    /**
     * Default constructor.
     */
    public CertifiedProductTargetedUser() {
        super();
    }

    /**
     * Constructor from DTO.
     * @param dto
     */
    public CertifiedProductTargetedUser(final CertifiedProductTargetedUserDTO dto) {
        this.id = dto.getId();
        this.targetedUserId = dto.getTargetedUserId();
        this.targetedUserName = dto.getTargetedUserName();
    }

    public boolean matches(final CertifiedProductTargetedUser other) {
        boolean result = false;
        if (this.getTargetedUserId() != null && other.getTargetedUserId() != null
                && this.getTargetedUserId().longValue() == other.getTargetedUserId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getTargetedUserName()) && !StringUtils.isEmpty(other.getTargetedUserName())
                && this.getTargetedUserName().equals(other.getTargetedUserName())) {
            result = true;
        }
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getTargetedUserId() {
        return targetedUserId;
    }

    public void setTargetedUserId(final Long targetedUserId) {
        this.targetedUserId = targetedUserId;
    }

    public String getTargetedUserName() {
        return targetedUserName;
    }

    public void setTargetedUserName(final String targetedUserName) {
        this.targetedUserName = targetedUserName;
    }

}
