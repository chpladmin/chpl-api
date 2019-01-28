package gov.healthit.chpl.web.controller;

import java.util.List;

public class CertificationIdVerificationBody {

    private List<String> ids;

    public CertificationIdVerificationBody() {
    }

    public CertificationIdVerificationBody(final List<String> ids) {
        this.ids = ids;
    }

    public List<String> getIds() {
        return this.ids;
    }

    public void setIds(final List<String> ids) {
        this.ids = ids;
    }

}
