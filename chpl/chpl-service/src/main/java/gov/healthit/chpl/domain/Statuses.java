package gov.healthit.chpl.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Statuses implements Serializable {
    private static final long serialVersionUID = -7091471050071142764L;
    private Integer active;
    private Integer retired;
    private Integer withdrawnByDeveloper;
    private Integer withdrawnByAcb;
    private Integer suspendedByAcb;
    private Integer suspendedByOnc;
    private Integer terminatedByOnc;

    public Statuses() {
    }

    public Statuses(Integer active, Integer retired, Integer withdrawnByDeveloper, Integer withdrawnByAcb,
            Integer suspendedByAcb, Integer suspendedByOnc, Integer terminatdByOnc) {
        this.active = active;
        this.retired = retired;
        this.withdrawnByDeveloper = withdrawnByDeveloper;
        this.withdrawnByAcb = withdrawnByAcb;
        this.suspendedByAcb = suspendedByAcb;
        this.suspendedByOnc = suspendedByOnc;
        this.terminatedByOnc = terminatdByOnc;
    }
}
