package gov.healthit.chpl.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.support.AbstractApplicationContext;

public abstract class App {
	private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	
	protected abstract void initiateSpringBeans(AbstractApplicationContext context, Properties props);
	
	protected void setLocalContext(Properties props) throws Exception{
		LocalContext ctx = LocalContextFactory.createLocalContext(props.getProperty("dbDriverClass"));
		ctx.addDataSource(props.getProperty("dataSourceName"),props.getProperty("dataSourceConnection"), 
						 props.getProperty("dataSourceUsername"), props.getProperty("dataSourcePassword"));
	}
	
	protected String getDownloadFolderPath(String[] args, Properties props){
		String downloadFolderPath;
        if (args.length > 0) {
        	downloadFolderPath = args[0];
        } else {
        	downloadFolderPath = props.getProperty("downloadFolderPath");
        }
        return downloadFolderPath;
	}
	
	protected File getDownloadFolder(String downloadFolderPath){
		File downloadFolder = new File(downloadFolderPath);
        if(!downloadFolder.exists()) {
        	downloadFolder.mkdirs();
        }
        return downloadFolder;
	}
	
	protected Properties getProperties() throws IOException{
		Properties props = null;
		InputStream in = SurveillanceOversightReportDailyApp.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		if (in == null) {
			props = null;
			throw new FileNotFoundException("Environment Properties File not found in class path.");
		} else {
			props = new Properties();
			props.load(in);
			in.close();
		}
		return props;
	}
}
