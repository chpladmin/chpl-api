package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.util.Util;

public class ModificationItem implements Serializable {
    private static final long serialVersionUID = 3049027227760878239L;
    private String subject;
    private String action;
    private Date date;

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public Date getDate() {
        return Util.getNewDate(date);
    }

    public void setDate(final Date date) {
        this.date = Util.getNewDate(date);
    }

}
