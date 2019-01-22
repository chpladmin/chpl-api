package gov.healthit.chpl.domain.schedule;

import java.io.Serializable;
import java.util.Date;

public class ChplOneTimeTrigger implements Serializable{
    private static final long serialVersionUID = -3583084568526672515L;

    //private String name;
    //private String group;
    private ChplJob job;
    private Date runDate;

    //    public String getName() {
    //        return name;
    //    }
    //    public void setName(String name) {
    //        this.name = name;
    //    }
    //    public String getGroup() {
    //        return group;
    //    }
    //    public void setGroup(String group) {
    //        this.group = group;
    //    }
    public ChplJob getJob() {
        return job;
    }
    public void setJob(ChplJob job) {
        this.job = job;
    }
    public Date getRunDate() {
        return runDate;
    }
    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }

}
