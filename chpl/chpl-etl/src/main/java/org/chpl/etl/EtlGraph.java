package org.chpl.etl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.concurrent.Future;

import org.jetel.graph.Result;
import org.jetel.graph.TransformationGraph;
import org.jetel.graph.TransformationGraphXMLReaderWriter;
import org.jetel.graph.runtime.EngineInitializer;
import org.jetel.graph.runtime.GraphRuntimeContext;
import org.jetel.main.runGraph;

public class EtlGraph {

	private FileInputStream graphResource;
	private GraphRuntimeContext runtimeContext;
	private TransformationGraph graph;
	private TransformationGraphXMLReaderWriter graphReader;

	public EtlGraph() throws URISyntaxException {
		this("/plugins");
	}

	public EtlGraph(String pluginDir) throws URISyntaxException {
		EngineInitializer.initEngine(App.class.getResource(pluginDir).toURI().getPath(), null, null);
	}

	public void setGraph(String graphResource) {
		try {
			this.graphResource = new FileInputStream(App.class.getResource(graphResource).toURI().getPath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		//prepare runtime parameters - JMX is turned off
		runtimeContext = new GraphRuntimeContext();
		runtimeContext.setUseJMX(false);

		//create transformation graph from xml file
		graph = new TransformationGraph();
		graphReader = new TransformationGraphXMLReaderWriter(runtimeContext);

		try {
			graph = graphReader.read(this.graphResource);
		} catch (Exception e) {
			e.printStackTrace();
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
			System.out.println("Failed graph execution!\n" + e.getMessage());
			return false;
		}
		return true;
	}

	public String getInputFile() {
		String retval = "";
		for (Node n : graph.getNodes().values()) {
			if (n.getId().equalsIgnoreCase(IN_NODE_ID)) {
				retval = n.getAttributes().getProperty(IN_FILE_URL_KEY);
			}
		}
		return retval;
	}

	public void setInputFile(String newInput) {
		for (Node n : graph.getNodes().values()) {
			if (n.getId().equalsIgnoreCase(IN_NODE_ID)) {
				n.getAttributes().setProperty(IN_FILE_URL_KEY, newInput);
			}
		}
	}
}
