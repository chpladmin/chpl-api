package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.TestingLabDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TestingLab implements Serializable {
    private static final long serialVersionUID = 7787353272569398682L;
    public static final String MULTIPLE_TESTING_LABS_CODE = "99";
    private Long id;
    private String atlCode;
    private String name;
    private String website;
    private Address address;
    private boolean retired;
    private Date retirementDate;

    public TestingLab(final TestingLabDTO dto) {
        this.id = dto.getId();
        this.atlCode = dto.getTestingLabCode();
        this.name = dto.getName();
        this.website = dto.getWebsite();
        this.retired = dto.isRetired();
        this.setRetirementDate(dto.getRetirementDate());
        this.address = dto.getAddress();
    }
}
