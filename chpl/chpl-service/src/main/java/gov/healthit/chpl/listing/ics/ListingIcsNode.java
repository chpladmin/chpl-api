package gov.healthit.chpl.listing.ics;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ListingIcsNode implements Serializable {
    private static final long serialVersionUID = 6675627839897851440L;

    private Long id;
    private String chplProductNumber;
    private Date certificationDate;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate certificationDay;
    private CertificationStatus certificationStatus;
    private List<ListingRelative> parents;
    private List<ListingRelative> children;
    private IdNamePair developer;
    private IdNamePair version;
    private IdNamePair product;

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ListingRelative implements Serializable {
        private static final long serialVersionUID = -2377078038387234130L;
        private Long id;
        private String chplProductNumber;
    }
}
