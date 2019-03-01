package gov.healthit.chpl.auth.json;

public class ErrorJSONObject {
	
	String error;

	public ErrorJSONObject(String errorMessage){
		error = errorMessage;
	}
	
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
