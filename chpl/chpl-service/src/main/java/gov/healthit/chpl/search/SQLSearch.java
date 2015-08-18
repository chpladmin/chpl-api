package gov.healthit.chpl.search;

import java.util.List;

public interface SQLSearch<T> extends Search<T> {
	
	public String getQuery();
	public List<T> search();

}
