package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.util.Util;

public class DateRange implements Serializable {
    private static final long serialVersionUID = 6702794674787630221L;
    Date startDate;
    Date endDate;

    public DateRange(Date startDate, Date endDate) {
        setStartDate(startDate);
        setEndDate(endDate);
    }

    public Date getStartDate() {
        return Util.getNewDate(startDate);
    }

    public void setStartDate(final Date startDate) {
        this.startDate = Util.getNewDate(startDate);
    }

    public Date getEndDate() {
        return Util.getNewDate(endDate);
    }

    public void setEndDate(final Date endDate) {
        this.endDate = Util.getNewDate(endDate);
    }

}
