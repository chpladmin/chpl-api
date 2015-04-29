package org.chpl.etl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Future;

import org.jetel.graph.Node;
import org.jetel.graph.Result;
import org.jetel.graph.TransformationGraph;
import org.jetel.graph.TransformationGraphXMLReaderWriter;
import org.jetel.graph.runtime.EngineInitializer;
import org.jetel.graph.runtime.GraphRuntimeContext;
import org.jetel.main.runGraph;
import org.joda.time.Partial.Property;

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
        
        FileInputStream inGraph = null;
        try{
        	inGraph=new FileInputStream(App.class.getResource("/chpl.grf").toURI().getPath());
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
            return;
        } catch (URISyntaxException e) {
			e.printStackTrace();
			return;
		}

        try{
            graph = graphReader.read(inGraph);
        }catch(Exception ex){
            ex.printStackTrace();
            return;
        }

        graph.dumpGraphConfiguration();
        Map<String, Node> nodes = graph.getNodes();
        Node n;
        for (String s : nodes.keySet()) {
        	n = nodes.get(s);
        	if (n.getName().equals("Read in CSV")) {

            	System.out.println(n.getName());
        		for (String name : n.getAttributes().stringPropertyNames()) {
        			
        			System.out.println(name);
        		}
        		System.out.println(n.getAttributes().getProperty("fileURL"));
        		n.getAttributes().setProperty("fileURL", "c:/Users/alarned/git/chpl-api/chpl/chpl-etl/src/main/resources/CHPL_pipe.csv");
        		System.out.println(n.getAttributes().getProperty("fileURL"));
        	}
        }
        
        //execute graph
        Future<Result> result;
        try{
            result = runGraph.executeGraph(graph, runtimeContext);
            if (!result.get().equals(Result.FINISHED_OK)){
                System.out.println("Failed graph execution!\n");
                return;
            }
        }catch (Exception e) {
            System.out.println("Failed graph execution!\n" + e.getMessage());
            return;
        }

	}
}
