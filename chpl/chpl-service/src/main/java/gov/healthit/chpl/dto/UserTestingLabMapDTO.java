package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.UserTestingLabMapEntity;

public class UserTestingLabMapDTO implements Serializable {
    private static final long serialVersionUID = -4237553934302170475L;

    private Long id;
    private UserDTO user;
    private TestingLabDTO testingLab;
    private Boolean retired;

    public UserTestingLabMapDTO(UserTestingLabMapEntity entity) {
        this.id = entity.getId();
        this.testingLab = new TestingLabDTO(entity.getTestingLab());
        this.user = new gov.healthit.chpl.dto.auth.UserDTO(entity.getUser());
        this.retired = entity.getRetired();
    }

    public UserTestingLabMapDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public TestingLabDTO getTestingLab() {
        return testingLab;
    }

    public void setTestingLab(TestingLabDTO testingLab) {
        this.testingLab = testingLab;
    }

    public Boolean getRetired() {
        return retired;
    }

    public void setRetired(Boolean retired) {
        this.retired = retired;
    }

}
