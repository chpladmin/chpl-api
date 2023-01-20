package gov.healthit.chpl.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class IdNamePair implements Serializable {
    private static final long serialVersionUID = -237707803683286810L;

    private Long id;
    private String name;
}
