package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.TestingLabEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TestingLabDTO implements Serializable {
    private static final long serialVersionUID = 3772645398248735019L;
    private Long id;
    private String testingLabCode;
    private AddressDTO address;
    private String name;
    private String website;
    private String accredidationNumber;
    private boolean retired;
    private Date retirementDate;

    public TestingLabDTO(TestingLabEntity entity) {
        this.id = entity.getId();
        this.testingLabCode = entity.getTestingLabCode();
        if (entity.getAddress() != null) {
            this.address = new AddressDTO(entity.getAddress());
        }
        this.name = entity.getName();
        this.website = entity.getWebsite();
        this.accredidationNumber = entity.getAccredidationNumber();
        this.retired = entity.getRetired();
        this.retirementDate = entity.getRetirementDate();
    }
}
