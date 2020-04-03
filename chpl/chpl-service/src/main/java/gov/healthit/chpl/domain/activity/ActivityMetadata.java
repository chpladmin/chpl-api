package gov.healthit.chpl.domain.activity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.util.Util;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ActivityMetadata implements Serializable {
    private static final long serialVersionUID = -3855142961571082535L;

    private Long id;
    private ActivityConcept concept;
    @Singular
    private Set<ActivityCategory> categories = new HashSet<ActivityCategory>();
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date date;
    private Long objectId;
    private User responsibleUser;
    private String description;


    public Date getDate() {
        return Util.getNewDate(date);
    }

    public void setDate(final Date date) {
        this.date = Util.getNewDate(date);
    }
}
