package gov.healthit.chpl.domain.contact;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.ContactDTO;
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
    private Long id;

    public PointOfContact() {
        super();
    }

    public PointOfContact(ContactDTO dto) {
        super(dto);
        this.id = dto.getId();
    }

    @Builder
    public PointOfContact(String fullName, String email, String phoneNumber, String title, Long id) {
        super(fullName, email, phoneNumber, title);
        this.id = id;
    }

    public PointOfContact(HashMap<String, Object> map) {
        super(map);
        if (map.containsKey("contactId") && map.get("contactId") != null) {
            try {
                this.id = Long.parseLong(map.get("contactId").toString());
            } catch (NumberFormatException ex) {
                LOGGER.warn("contactId in map = '" + map.get("contactId") + "' is not parseable into a Long");
            }
        } else if (map.containsKey("id") && map.get("id") != null) {
            try {
                this.id = Long.parseLong(map.get("id").toString());
            } catch (NumberFormatException ex) {
                LOGGER.warn("id in map = '" + map.get("id") + "' is not parseable into a Long");
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PointOfContact)) {
            return false;
        }
        PointOfContact anotherContact = (PointOfContact) obj;
        if ((this.id != null && anotherContact.id != null
                && this.id.longValue() == anotherContact.id.longValue())
                || (this.id == null && anotherContact.id == null)) {
            return super.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        if (this.id != null) {
            hashCode += this.id.hashCode();
        }
        return hashCode;
    }
}
