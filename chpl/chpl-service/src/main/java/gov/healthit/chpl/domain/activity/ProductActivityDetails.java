package gov.healthit.chpl.domain.activity;

import java.io.Serializable;

import gov.healthit.chpl.domain.Developer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProductActivityDetails extends ActivityDetails implements Serializable {
    private static final long serialVersionUID = 6724369230954969251L;
    private Developer developer;

}
