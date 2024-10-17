package gov.healthit.chpl.complaint.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintType implements Serializable {
    private static final long serialVersionUID = 1518763181984424604L;

    private Long id;
    private String name;
}
