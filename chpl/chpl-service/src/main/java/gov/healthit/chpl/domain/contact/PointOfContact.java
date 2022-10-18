package gov.healthit.chpl.domain.contact;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Log4j2
public class PointOfContact extends Person {
    private static final long serialVersionUID = -1945872096428814999L;

    /**
     * Database ID of a point of contact.
     */
    @XmlElement(required = true)
    private Long contactId;

    public PointOfContact() {
        super();
    }

    @Builder
    public PointOfContact(String fullName, String email, String phoneNumber, String title, Long contactId) {
        super(fullName, email, phoneNumber, title);
        this.contactId = contactId;
    }

    public PointOfContact(HashMap<String, Object> map) {
        super(map);
        if (map.containsKey("contactId") && map.get("contactId") != null) {
            try {
                this.contactId = Long.parseLong(map.get("contactId").toString());
            } catch (NumberFormatException ex) {
                LOGGER.warn("contactId in map = '" + map.get("contactId") + "' is not parseable into a Long");
            }
        }
    }

    public void normalizeSpaces() {
        this.setFullName(StringUtils.normalizeSpace(this.getFullName()));
        this.setEmail(StringUtils.normalizeSpace(this.getEmail()));
        this.setPhoneNumber(StringUtils.normalizeSpace(this.getPhoneNumber()));
        this.setTitle(StringUtils.normalizeSpace(this.getTitle()));
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PointOfContact)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        if (this.contactId != null) {
            hashCode += this.contactId.hashCode();
        }
        return hashCode;
    }
}
