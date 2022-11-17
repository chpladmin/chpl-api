package gov.healthit.chpl.ucdProcess;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "ucd_process")
public class UcdProcessEntity implements Serializable {
    private static final long serialVersionUID = -8588994880748379730L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ucd_process_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public UcdProcess toDomain() {
        return UcdProcess.builder()
                .id(getId())
                .name(getName())
                .build();
    }
}
