package gov.healthit.chpl.app;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
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
		Token token = new Token();
		String url = props.getProperty("chplUrlBegin") + props.getProperty("basePath") + props.getProperty("authenticate");
		logger.info("Making REST HTTP POST call to " + url + 
				" using API-key=" + props.getProperty("apiKey"));
		try{
			String tokenResponse = Request.Post(url)
					.bodyString("{ \"userName\": \"" + props.getProperty("username") + "\","
							+ " \"password\": \"" + props.getProperty("password") + "\" }", ContentType.APPLICATION_JSON)
					.version(HttpVersion.HTTP_1_1)
					.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
					.addHeader("API-key", props.getProperty("apiKey"))
					.execute().returnContent().asString();
					JsonObject jobj = new Gson().fromJson(tokenResponse, JsonObject.class);
					logger.info("Retrieved the following JSON from " + url + ": \n" + jobj.toString());
					token.setToken(jobj.get("token").toString());
					logger.info("Retrieved token " + token.getToken());
					token.setTokenStartTime(System.currentTimeMillis());
		} catch (IOException e){
			logger.info("Failed to make call to " + url +
					" using API-key=" + props.getProperty("apiKey"));
		}
		return token;
	}
	
	private Token getRefreshedToken(Token token, Properties props) {
		String url = props.getProperty("chplUrlBegin") + props.getProperty("basePath") + props.getProperty("refreshToken");
		logger.info("Making REST HTTP GET call to " + url +
				" using API-key=" + props.getProperty("apiKey"));
		try{
			String tokenResponse = Request.Get(url)
					.version(HttpVersion.HTTP_1_1)
					.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
					.addHeader("API-key", props.getProperty("apiKey"))
					.addHeader("Authorization", "Bearer " + token.getToken())
					.execute().returnContent().asString();
			JsonObject jobj = new Gson().fromJson(tokenResponse, JsonObject.class);
			logger.info("Retrieved the following JSON from " + url + ": \n" + jobj.toString());
			token.setToken(jobj.get("token").toString());
			logger.info("Retrieved token " + token.getToken());
			token.setTokenStartTime(System.currentTimeMillis());
		} catch (IOException e){
			logger.info("Failed to make call to " + url + 
					" using API-key=" + props.getProperty("apiKey"));
			}
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
