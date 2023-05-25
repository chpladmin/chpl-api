package gov.healthit.chpl.questionableactivity.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionableActivitySearchResponse implements Serializable {
    private static final long serialVersionUID = 5130424471725198329L;
    private Integer recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    private List<QuestionableActivitySearchResult> results = new ArrayList<QuestionableActivitySearchResult>();
}
