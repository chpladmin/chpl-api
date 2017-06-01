package gov.healthit.chpl.app;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Token {
	private String token;
	private Properties props;
	
	private static final Logger logger = LogManager.getLogger(Token.class);
	
	public Token(){}
	
	public Token(Properties props){
		setProps(props);
	}
	
	public Token getNewToken(Properties props) {
		logger.info("Get new token");
		Token token = new Token();
		String url = props.getProperty("chplUrlBegin") + props.getProperty("basePath") + props.getProperty("authenticate");
		String bodyJson = "{ \"userName\": \"" + props.getProperty("username") + "\","
							+ " \"password\": \"" + props.getProperty("password") + "\" }";
		String tokenResponse = HttpUtil.postBodyRequest(url, null, props, bodyJson);
		JsonObject jobj = new Gson().fromJson(tokenResponse, JsonObject.class);
		token.setToken(jobj.get("token").toString());
		logger.info("Finished getting new token");
		return token;
	}
	
	public Token getRefreshedToken(Token token, Properties props) {
		logger.info("Get refreshed token");
		String url = props.getProperty("chplUrlBegin") + props.getProperty("basePath") + props.getProperty("refreshToken");
		String tokenResponse = HttpUtil.getAuthenticatedRequest(url, null, props, token.getToken());
		JsonObject jobj = new Gson().fromJson(tokenResponse, JsonObject.class);
		token.setToken(jobj.get("token").toString());
		logger.info("Finished getting refreshed token");
		return token;
	}
	

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}
}
