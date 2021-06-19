package gov.healthit.chpl.search.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "searchResults")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse implements Serializable {
    private static final long serialVersionUID = 5130424471766571329L;
    private Integer recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    private List<CertifiedProductBasicSearchResult> results = new ArrayList<CertifiedProductBasicSearchResult>();
}
