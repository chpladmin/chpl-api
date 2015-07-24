package org.chpl.etl;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetel.graph.Result;
import org.jetel.graph.TransformationGraph;
import org.jetel.graph.TransformationGraphXMLReaderWriter;
import org.jetel.graph.runtime.EngineInitializer;
import org.jetel.graph.runtime.GraphRuntimeContext;
import org.jetel.main.runGraph;

public class EtlGraph {

	private InputStream graphStream;
	private GraphRuntimeContext runtimeContext;
	private TransformationGraph graph;
	private TransformationGraphXMLReaderWriter graphReader;

	public EtlGraph() throws URISyntaxException {
		this("./src/main/resources/plugins");
	}

	public EtlGraph(String pluginDir) throws URISyntaxException {
		EngineInitializer.initEngine(new File(pluginDir).toURI().getPath(), "./src/main/resources/defaultProperties", null);
	}

	public void setGraph(String graphResource) {
		try {
			this.graphStream = getClass().getResourceAsStream(graphResource);
		} catch (NullPointerException e) {
			Logger.getLogger(EtlGraph.class.getName()).log(Level.SEVERE, null, e);
		}

		//prepare runtime parameters - JMX is turned off
		runtimeContext = new GraphRuntimeContext();
		runtimeContext.setUseJMX(false);
		runtimeContext.setDebugMode(false);
		runtimeContext.setLogLevel(org.apache.log4j.Level.FATAL);

		//create transformation graph from xml file
		graph = new TransformationGraph();
		graphReader = new TransformationGraphXMLReaderWriter(runtimeContext);

		try {
			graph = graphReader.read(this.graphStream);
		} catch (Exception e) {
			Logger.getLogger(EtlGraph.class.getName()).log(Level.SEVERE, null, e);
			return;
		}
	}

	public TransformationGraph getGraph() {
		return this.graph;
	}

	public boolean execute() {
		Future<Result> result;
		try {
			result = runGraph.executeGraph(this.graph, this.runtimeContext);
			if (!result.get().equals(Result.FINISHED_OK)) {
				Logger.getLogger(EtlGraph.class.getName()).log(Level.SEVERE, "Failed graph execution!\n");
				return false;
			}
		} catch (Exception e) {
			Logger.getLogger(EtlGraph.class.getName()).log(Level.SEVERE, null, e);
			return false;
		}
		return true;
	}
}
