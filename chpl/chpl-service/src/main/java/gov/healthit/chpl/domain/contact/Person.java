package gov.healthit.chpl.domain.contact;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class Person implements Serializable {
    private static final long serialVersionUID = 5376154206189674741L;

    @Schema(description = "Person's full name.")
    private String fullName;

    @Schema(description = "Email address of the person.")
    private String email;

    @Schema(description = "Phone number of the person.")
    private String phoneNumber;

    @Schema(description = "Title (Ms., Mr., Dr., etc) of the person.")
    private String title;

    public Person() {}

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
        return (StringUtils.isAllEmpty(this.fullName, anotherPerson.fullName)
                    || StringUtils.equals(this.fullName, anotherPerson.fullName))
                && (StringUtils.isAllEmpty(this.email, anotherPerson.email)
                    || StringUtils.equals(this.email, anotherPerson.email))
                && (StringUtils.isAllEmpty(this.phoneNumber, anotherPerson.phoneNumber)
                    || StringUtils.equals(this.phoneNumber, anotherPerson.phoneNumber))
                && (StringUtils.isAllEmpty(this.title, anotherPerson.title)
                    || StringUtils.equals(this.title, anotherPerson.title));
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
