package org.chpl.etl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;

import org.jetel.graph.Result;
import org.jetel.graph.TransformationGraph;
import org.jetel.graph.TransformationGraphXMLReaderWriter;
import org.jetel.graph.runtime.EngineInitializer;
import org.jetel.graph.runtime.GraphRuntimeContext;
import org.jetel.main.runGraph;

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
