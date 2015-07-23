package org.cytoscape.intern;

import org.cytoscape.intern.mapper.Mapper;
import org.cytoscape.intern.mapper.NetworkPropertyMapper;
import org.cytoscape.intern.mapper.NodePropertyMapper;
import org.cytoscape.intern.mapper.EdgePropertyMapper;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;
import org.cytoscape.work.Tunable;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Task object that writes the network view to a .dot file
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotWriterTask implements CyWriter {
	
	// handles mapping from CS to .dot of respective elements
	private NetworkPropertyMapper networkMapper;
	private NodePropertyMapper nodeMapper;
	private EdgePropertyMapper edgeMapper;
	
	// Object used to write the .dot file
	private OutputStreamWriter outputWriter;
		
	// NetworkView being converted to .dot if view export is selected
	private CyNetworkView networkView = null;
	
	// Network being converted to .dot if network export is selected
	private CyNetwork network = null;
	
	// debug logger
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.DotWriterTask");
	
	// whether or not the network view is directed
	private boolean directed = false;
	
	/*
	 * Tunable to prompt user for edge style
	 * curved, normal (segments) or splines
	 * (route around nodes)
	 */
	@Tunable(description="Pick edge style")
	public ListSingleSelection<String>  typer = new ListSingleSelection<String>(
			"Straight segments", "Curved segments", "Curved segments routed around nodes");

	// whether or not a name had to be modified
	private boolean nameModified = false;
	
	// value of splines attribute
	private String splinesVal;
	
	/**
	 * Constructs a DotWriterTask object for exporting network view
	 * 
	 * @param output OutputStream that is being written to
	 * @param networkView CyNetworkView that is being exported
	 */
	public DotWriterTask(OutputStream output, CyNetworkView networkView) {
		super();
		
		// Make logger write to file
		FileHandler handler = null;
		try {
			handler = new FileHandler("log_DotWriterTask.txt");
			handler.setLevel(Level.ALL);
			
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		
		outputWriter = new OutputStreamWriter(output);
		this.networkView = networkView;
		directed = NetworkPropertyMapper.isDirected(networkView);
		
		LOGGER.info("DotWriterTask constructed");
	}
	
	/**
	 * 
	 * Constructs a DotWriterTask object for exporting network only
	 * 
	 * @param output OutputStream that is being written to
	 * @param network that is being exported
	 */
	public DotWriterTask(OutputStream output, CyNetwork network){
		super();
		outputWriter = new OutputStreamWriter(output);
		this.network = network;
		
		// Make logger write to file
		FileHandler handler = null;
		try {
			handler = new FileHandler("log_DotWriterTask.txt");
			handler.setLevel(Level.ALL);
			
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		
		LOGGER.info("DotWriterTask constructed");	
	}

	
	/**
	 * Causes the task to begin execution.
	 * 
	 * @param taskMonitor The TaskMonitor provided by TaskManager to allow the
	 * Task to modify its user interface.
	 */
	@Override
	public void run(TaskMonitor taskMonitor) {	
		// set splines val
		splinesVal = typer.getSelectedValue();
		LOGGER.info("Raw splinesVal: " + splinesVal);
		switch(splinesVal) {
			case "Straight segments":
				splinesVal = "false";
				break;
			case "Curved segments":
				splinesVal = "curved";
				break;	
			case "Curved segments routed around nodes":
				splinesVal = "true";
				break;
		}
		LOGGER.info("Converted splinesVal: " + splinesVal);

		if(networkView != null) {
			// constructed here because splinesVal is needed, splinesVal can't be determined until run()
			this.networkMapper = new NetworkPropertyMapper(networkView, directed, splinesVal);
		}
		
		LOGGER.info("Writing .dot file...");
		writeProps();
		writeNodes();
		writeEdges();
		
		// Close off file and notify if needed
		try {

			outputWriter.write("}");
			outputWriter.close();
			LOGGER.info("Finished writing file");
			if (nameModified) {
				Notifier.showMessage("Some node names have been modified in order to comply to DOT syntax", Notifier.MessageType.WARNING);
			}
		} 
		catch(IOException e) {
			LOGGER.severe("Failed to close file, IOException in DotWriterTask");
		}	
	}
	
	/**
	 * Causes the task to stop execution.
	 */
	@Override
	public void cancel() {
		// TODO
	}
	
	/**
	 * Writes the network properties to file
	 */
	private void writeProps() {
		try {
			LOGGER.info("Writing network properties...");
			if(network == null) {
				network = (CyNetwork)networkView.getModel();
			}
						
			String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
			String networkProps;
			
			// if we are exporting network view
			if(networkView != null){
				networkProps = networkMapper.getElementString();
			}
			// if we are only exporting network
			else {
				networkProps = "graph "+ Mapper.filterString(networkName) + " {\nsplines = " + splinesVal +  "\n";  
			}
			// if network name was modified
			if (!networkProps.contains(networkName)) {
				nameModified = true;
			}
			
			outputWriter.write( networkProps );
			LOGGER.info("Finished writing network properties");
		}
		catch(IOException exception) {
			LOGGER.log(Level.SEVERE, "Write failed @ writeProps()");
		}
	}
	
	/**
	 * Writes the .dot declaration of each node to file
	 */
	private void writeNodes() {
		LOGGER.info("Writing node declarations...");
		
		// if the user passed in networkView
		if(networkView != null){
			// create list of all node views
			ArrayList< View<CyNode> > nodeViewList = new ArrayList< View<CyNode> >( networkView.getNodeViews() );
		
			// for each node, write declaration string
			for(View<CyNode> nodeView: nodeViewList) {
				nodeMapper = new NodePropertyMapper(nodeView);
	  		
				try {
					// Retrieve node name
					CyNode nodeModel = nodeView.getModel();
					CyNetwork networkModel = networkView.getModel();
					String nodeName = networkModel.getRow(nodeModel).get(CyNetwork.NAME, String.class);
	  
					String newNodeName = Mapper.filterString(nodeName);
					if (!newNodeName.equals(nodeName)) {
						nameModified = true;
					}

					String declaration = String.format("%s %s\n", newNodeName, nodeMapper.getElementString());

					outputWriter.write(declaration);
				}
				catch(IOException exception) {
					LOGGER.log(Level.SEVERE, "Write failed @ writeNodes()");
				}
			}	
			
		}
		// if the user passed in network
		else {
			List<CyNode> nodeList = network.getNodeList();
			
			for(CyNode node: nodeList){
				try{
					String nodeName = network.getRow(node).get(CyNetwork.NAME,String.class);
					String newNodeName = Mapper.filterString(nodeName);
				
					if(!newNodeName.equals(nodeName)) {
						nameModified = true;
					}
					String declaration = String.format("%s\n", newNodeName);

					outputWriter.write(declaration);
				}
				catch(IOException exception){
					LOGGER.log(Level.SEVERE, "Write failed @ writeNodes() passed in network instead of networkView");
				}
			}
		}
		LOGGER.info("Finished writing node declarations");
	}
	
	/**
	 * Writes the .dot declaration of each edge to file
	 */
	private void writeEdges() {
		LOGGER.info("Writing edge declarations...");
		
		// do the following if user passed in the networkView
		if(networkView != null){
			// create list of all edge views
			ArrayList< View<CyEdge> > edgeViewList = new ArrayList< View<CyEdge> >( networkView.getEdgeViews() );
			String edgeType = (directed) ? "->" : "--";
		
			// for each edge, write declaration string
			for(View<CyEdge> edgeView: edgeViewList) {
				edgeMapper = new EdgePropertyMapper(edgeView, networkView);
	  		
				try {
					// Retrieve source+target node names
					CyEdge edgeModel = edgeView.getModel();
					CyNetwork networkModel = networkView.getModel();

					CyNode sourceNode = edgeModel.getSource();
					CyNode targetNode = edgeModel.getTarget();
	  			
					String sourceName = networkModel.getRow(sourceNode).get(CyNetwork.NAME, String.class);
					// filter out disallowed chars
					sourceName = Mapper.filterString(sourceName);
	  			
					String targetName = networkModel.getRow(targetNode).get(CyNetwork.NAME, String.class);
					// filter out disallowed chars
					targetName = Mapper.filterString(targetName);

					String edgeName = String.format("%s %s %s", sourceName, edgeType, targetName);
					String declaration = String.format("%s %s\n", edgeName, edgeMapper.getElementString());

					outputWriter.write(declaration);
				}
				catch(IOException exception) {
					LOGGER.log(Level.SEVERE, "Write failed @ writeEdges()");
				}	
			}	
		}
		// do the following if user passed in the network
		else {
			List<CyEdge> edgeList = network.getEdgeList();
			
			for(CyEdge edge : edgeList){
				try{
					CyNode sourceNode = edge.getSource();
					CyNode targetNode = edge.getTarget();
					
					String sourceName = network.getRow(sourceNode).get(CyNetwork.NAME, String.class);
					sourceName = Mapper.filterString(sourceName);
				
					String targetName = network.getRow(targetNode).get(CyNetwork.NAME, String.class);
					targetName = Mapper.filterString(targetName);
				
					String edgeName = String.format("%s %s %s", sourceName, "--", targetName);
					String declaration = String.format("%s\n", edgeName);

					outputWriter.write(declaration);
				}
				catch(IOException exception){
					LOGGER.log(Level.SEVERE, "Write failed @ writeEdges() (passed in network instead of networkView)");
				}
			}
		}
		LOGGER.info("Finished writing edge declarations...");
	}
}
