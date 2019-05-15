package gov.healthit.chpl.domain.activity;

public class UserMaintenanceActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = -3518832572761720950L;

    private String email;
    private String subjectName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}
