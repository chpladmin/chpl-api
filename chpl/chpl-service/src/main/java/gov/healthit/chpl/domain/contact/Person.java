package gov.healthit.chpl.domain.contact;

import java.io.Serializable;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.ContactDTO;
import lombok.AllArgsConstructor;

/**
 * Domain object representing a Person. It may partially represent a user with access to log into the system
 * or may represent a point of contact for a developer or product.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class Person implements Serializable {
    private static final long serialVersionUID = 5376154206189674741L;

    /**
     * Person's full name.
     */
    @XmlElement(required = true)
    private String fullName;

    /**
     * Email address of the person.
     */
    @XmlElement(required = true)
    private String email;

    /**
     * Phone number of the person.
     */
    @XmlElement(required = true)
    private String phoneNumber;

    /**
     * Title (Ms., Mr., Dr., etc) of the person.
     */
    @XmlElement(required = false, nillable = true)
    private String title;

    public Person() {}

    public Person(ContactDTO dto) {
        this.fullName = dto.getFullName();
        this.email = dto.getEmail();
        this.phoneNumber = dto.getPhoneNumber();
        this.title = dto.getTitle();
    }

    public Person(HashMap<String, Object> map) {
        if (map.containsKey("fullName") && map.get("fullName") != null) {
            this.fullName = map.get("fullName").toString();
        }
        if (map.containsKey("email") && map.get("email") != null) {
            this.email = map.get("email").toString();
        }
        if (map.containsKey("phoneNumber") && map.get("phoneNumber") != null) {
            this.phoneNumber = map.get("phoneNumber").toString();
        }
        if (map.containsKey("title") && map.get("title") != null) {
            this.title = map.get("title").toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Person)) {
            return false;
        }
        Person anotherPerson = (Person) obj;
        return ObjectUtils.equals(this.fullName, anotherPerson.fullName)
                && ObjectUtils.equals(this.email, anotherPerson.email)
                && ObjectUtils.equals(this.phoneNumber, anotherPerson.phoneNumber)
                && ObjectUtils.equals(this.title, anotherPerson.title);
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (!StringUtils.isEmpty(this.fullName)) {
            hashCode += this.fullName.hashCode();
        }
        if (!StringUtils.isEmpty(this.email)) {
            hashCode += this.email.hashCode();
        }
        if (!StringUtils.isEmpty(this.phoneNumber)) {
            hashCode += this.phoneNumber.hashCode();
        }
        if (!StringUtils.isEmpty(this.title)) {
            hashCode += this.title.hashCode();
        }
        return hashCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return String.format("[Person domain object: [Full Name: %s] [Email: %s],"
                + "[Phone Number: %s], [Title: %s]]", this.getFullName(),
                this.getEmail(), this.getPhoneNumber(), this.getTitle());

    }
}
