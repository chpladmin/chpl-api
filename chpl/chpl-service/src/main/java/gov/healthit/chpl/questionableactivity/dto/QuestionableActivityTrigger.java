package gov.healthit.chpl.questionableactivity.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionableActivityTrigger implements Serializable {
    private static final long serialVersionUID = -7627129167248983500L;

    private Long id;
    private String name;
    private String level;
}
