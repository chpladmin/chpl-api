package gov.healthit.chpl.search;

import java.util.List;

public class SimpleSearch<T> implements SQLSearch<T> {

	private String searchTerm;
	
	SimpleSearch(String searchTerm){
		this.searchTerm = searchTerm;
	}
	
	
	@Override
	public String getQuery() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<T> search() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
