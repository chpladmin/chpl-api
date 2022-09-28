package gov.healthit.chpl.developer.search;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest implements Serializable {
    private static final long serialVersionUID = 11792092166701580L;
    public static final String DATE_SEARCH_FORMAT = "yyyy-MM-dd";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private String searchTerm;
    private String developerName;
    private String developerCode;

    @Builder.Default
    private Set<String> statuses = new HashSet<String>();

    @Builder.Default
    private Set<String> certificationBodies = new HashSet<String>();

    private String decertificationDateStart;
    private String decertificationDateEnd;

    @JsonIgnore
    @XmlTransient
    private String orderByString;
    private OrderByOption orderBy;
    @Builder.Default
    private Boolean sortDescending = false;
    @Builder.Default
    private Integer pageNumber = 0;
    @Builder.Default
    private Integer pageSize = DEFAULT_PAGE_SIZE;
}
