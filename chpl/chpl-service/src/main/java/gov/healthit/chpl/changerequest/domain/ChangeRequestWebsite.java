package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsExclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
public class ChangeRequestWebsite implements Serializable, ChangeRequestDetails {
    private static final long serialVersionUID = -5572794875424284955L;

    @EqualsExclude
    private Long id;

    private String website;

    @Override
    public boolean isEqual(Object obj) {
        return equals(obj);
    }

}
