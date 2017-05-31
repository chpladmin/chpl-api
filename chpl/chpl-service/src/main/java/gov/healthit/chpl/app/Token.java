package gov.healthit.chpl.app;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Token {
	private String token;
	private Properties props;
	private Long tokenStartTime;
	
	private static final Logger logger = LogManager.getLogger(Token.class);
	
	public Token(){}
	
	public Token(Properties props){
		setToken(getValidToken(null, props).getToken());
		setProps(props);
		if(getTokenStartTime() == null){
			setTokenStartTime(System.currentTimeMillis());
		}
	}
	
	public Token getValidToken(Token token, Properties props){
		// Get new token if there is no existing token
		if(token == null || StringUtils.isEmpty(token.getToken())){
			return getNewToken(props);
		} 
		
		// Get new token if existing token is invalid due to elapsed time greater than expiration time
		if((System.currentTimeMillis() - token.getTokenStartTime()) > Long.parseLong(props.getProperty("tokenExpirationTimeInMillis"))){
			return getNewToken(props);
		}
		
		// Get refreshed token if current token is valid but greater than the refresh delay
		if((System.currentTimeMillis() - token.getTokenStartTime()) > Long.parseLong(props.getProperty("tokenRefreshDelayInMillis"))){
			return getRefreshedToken(token, props);
		}
		
		return token;
	}
	
	private Token getNewToken(Properties props) {
		logger.info("Get new token");
		Token token = new Token();
		String url = props.getProperty("chplUrlBegin") + props.getProperty("basePath") + props.getProperty("authenticate");
		String bodyJson = "{ \"userName\": \"" + props.getProperty("username") + "\","
							+ " \"password\": \"" + props.getProperty("password") + "\" }";
		String tokenResponse = HttpUtil.postBodyRequest(url, null, props, bodyJson);
		JsonObject jobj = new Gson().fromJson(tokenResponse, JsonObject.class);
		token.setToken(jobj.get("token").toString());
		token.setTokenStartTime(System.currentTimeMillis());
		return token;
	}
	
	private Token getRefreshedToken(Token token, Properties props) {
		logger.info("Get refreshed token");
		String url = props.getProperty("chplUrlBegin") + props.getProperty("basePath") + props.getProperty("refreshToken");
		String tokenResponse = HttpUtil.getAuthenticatedRequest(url, null, props, token);
		JsonObject jobj = new Gson().fromJson(tokenResponse, JsonObject.class);
		token.setToken(jobj.get("token").toString());
		token.setTokenStartTime(System.currentTimeMillis());
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

	public Long getTokenStartTime() {
		return tokenStartTime;
	}

	public void setTokenStartTime(Long tokenStartTime) {
		this.tokenStartTime = tokenStartTime;
	}
}
