package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.QmsStandardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class QmsStandardDTO implements Serializable {
    private static final long serialVersionUID = 5091557483274894084L;
    private Long id;
    private String name;

    public QmsStandardDTO(QmsStandardEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }
}
