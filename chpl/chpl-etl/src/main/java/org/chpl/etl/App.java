package org.chpl.etl;

import java.net.URISyntaxException;

import org.jetel.graph.TransformationGraph;
import org.jetel.graph.TransformationGraphXMLReaderWriter;
import org.jetel.graph.runtime.EngineInitializer;
import org.jetel.graph.runtime.GraphRuntimeContext;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main( String[] args )
	{
        //initialization; must be present
        try {
			EngineInitializer.initEngine(App.class.getResource("/plugins").toURI().getPath(), null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        
        //prepare runtime parameters - JMX is turned off
        GraphRuntimeContext runtimeContext = new GraphRuntimeContext();
        runtimeContext.setUseJMX(false);

        //create transformation graph from xml file
        TransformationGraph graph= new TransformationGraph();
        TransformationGraphXMLReaderWriter graphReader=new TransformationGraphXMLReaderWriter(runtimeContext);

	}
}
