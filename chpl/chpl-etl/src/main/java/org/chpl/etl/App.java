package org.chpl.etl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jetel.graph.runtime.EngineInitializer;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main( String[] args )
	{
		String plugins = getPlugins();
		System.out.println( plugins );
		
        //initialization; must be present
        EngineInitializer.initEngine(plugins, null, null);

	}

	public static String getPlugins() {
		Properties arguments = new Properties();
		InputStream fileS = null;
		fileS = App.class.getResourceAsStream("/params.txt");
		try {
			arguments.load(fileS);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (arguments.getProperty("plugins"));
	}
}
