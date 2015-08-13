package org.cytoscape.intern.read;

import org.cytoscape.intern.read.reader.Reader; 

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.cytoscape.intern.FileHandlerManager;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.view.vizmap.VisualStyle;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.PortNode;

/**
 * Task object that reads a dot file into a network/ network view
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotReaderTask extends AbstractCyNetworkReader {
	
	// debug logger
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.read.DotReaderTask");
	private FileHandler handler = null;
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();

	// InputStreamReader used as input to the JPGD Parser 
	private InputStreamReader inStreamReader;
	
	// VisualMappingManager to which the new visual style will be added	
	private VisualMappingManager vizMapMgr;
	
	// VisualStyleFactory that will create the VisualStyle for the CyNetwork
	private VisualStyleFactory vizStyleFact;
	
	// Maps the Node in Graph to the CyNode in CyNetwork
	private Map<Node, CyNode> nodeMap;

	// Maps the Node in Graph to the CyNode in CyNetwork
	private Map<Edge, CyEdge> edgeMap;

	// Maps the created CyNetworks to their JPGD Graph object
	private Map<Graph, CyNetwork> graphMap;
	
	// Array of CyNetwork for getNetworks
	private CyNetwork[] outNetworks;

	// list of all relevant attributes
	private static final ArrayList<String> ATTRIBUTES = new ArrayList<String>();
	
	/**
	 * Constructs a DotReaderTask object for importing a dot file
	 * 
	 * @param inStream the stream to be read from
	 * @param netViewFact instance of CyNetworkViewFactory
	 * @param netFact instance of CyNetworkFactory
	 * @param netMgr instance of CyNetworkManager
	 * @param rootNetMgr instance of CyRootNetworkManager
	 * @param vizMapMgr instance of VisualMappingManager
	 * @param vizStyleFact instance of VisualStyleFactory
	 */
	public DotReaderTask(InputStream inStream, CyNetworkViewFactory netViewFact,
			CyNetworkFactory netFact, CyNetworkManager netMgr,
			CyRootNetworkManager rootNetMgr, VisualMappingManager vizMapMgr, VisualStyleFactory vizStyleFact) {
		
		super(inStream, netViewFact, netFact, netMgr, rootNetMgr);
		
		// Make logger write to file
		try {
			handler = new FileHandler("log_DotReaderTask.txt");
			handler.setLevel(Level.ALL);
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);


		// Initialize variables
		inStreamReader = new InputStreamReader(inStream);
		this.vizMapMgr = vizMapMgr;
		this.vizStyleFact = vizStyleFact;
		
		graphMap = new HashMap<Graph, CyNetwork>();
		nodeMap = new HashMap<Node, CyNode>();
		edgeMap = new HashMap<Edge, CyEdge>();
	}

	
	/**
	 * Causes the task to begin execution.
	 * 
	 * @param taskMonitor The TaskMonitor provided by TaskManager to allow the
	 * Task to modify its user interface.
	 */
	@Override
	public void run(TaskMonitor monitor) {
		// TODO 
		/*
		 * Steps:
		 * 1. Use Parser to generate Graph objects representing
		 * the graphs from the InputStreamReader
		 * 2. Create CyNetwork[] "networks" the size equaling the number of Graph objects
		 * 2. For each graph object do the following:
		 * 		a. Create a CyNetwork
		 * 		b. Set the name of the CyNetwork to the Graph's Id
		 * 		c. Retrieve the list of Node objects
		 * 		d. For each node do the following:
		 * 			- Create a CyNode
		 * 			- Set the name and shared_name of the CyNode to the Node's Id
		 * 		e. For each edge do the following:
		 *			- Create a CyEdge
		 *			- If digraph, set interaction to "interaction", else set to undirected
		 *			- set the name and shared_name of CyEdge to Sourcename (interaction) Targetname
		 * 3. Add CyNetwork to "networks"
		 * 4. Add <CyNetwork, Graph> pair to HashMap
		 */
		/**
		 * Everything we need to do (to help design-- not necessarily in this order):
		 * 1. Use parser to generate Graph objects representing
		 * the graphs from the InputStreamReader
		 * 2. For each graph object do the following:
		 * 		a. Create a CyNetwork
		 * 		b. Set the name of the CyNetwork to the Graph's Id
		 * 		-  Set all network properties
		 * 		[-  Add all CyNodes to network
		 * 		-  set default VPs if exists defaults in .dot file. using getGenericAttriubte()
		 * 		-  If not, use cytoscape defaults (do nothing)
		 * 		-  Set default Visual Properties for nodes
		 * 		-]  Set any bypass VPs
		 * 		-  Do bracketed points for CyEdges. And also:
		 * 			-  If digraph, set interaction to "interaction", else set to undirected
		 * 			-  set the name and shared_name of CyEdge to Sourcename (interaction) Targetname
		 */
		
		LOGGER.info("Running run() function...");
		
		//Initialize the parser
		Parser parser = new Parser();
		
		try {
			
		    LOGGER.info("Begin parsing the input...");
			parser.parse(inStreamReader);
			
			// Get list of graphs
			ArrayList<Graph> graphList = parser.getGraphs();
			CyNetwork [] networks = new CyNetwork [graphList.size()];
			
			int networkCounter = 0;
			for (Graph graph : graphList) {
				
				LOGGER.info("Iterating graph in graphList...");
				CyNetwork network = cyNetworkFactory.createNetwork();
				
				// set the name for the network
				String networkName = graph.getId().toString(); 
				network.getRow(network).set(CyNetwork.NAME, networkName);
			
				// import nodes
				ArrayList<Node> nodeList = graph.getNodes(true);
				for (Node node : nodeList) {
					importNode(node, network);
				}
				
				// import edges
				ArrayList<Edge> edgeList = graph.getEdges();
				for(Edge edge : edgeList) {
					importEdge(edge, network);
				}
				
				/*********************************************************************************
				 * Not sure whether the ArrayList graphList might contain empty graph (The unsure
				 * code is: ArrayList <Graph> graphList = parser.getGraphs(); at the beginning of 
				 * second try{}), if it's possible for graphList to have empty graph, we probably 
				 * need to check whether network is null(has not been created) before we add it to 
				 * the network array (networks) and before we add it to 
				 * graphMap (ArrayList <graph, CyNetwork>)
				 **********************************************************************************
				 */
				
				//at the end of each graph iteration, add the created CyNetwork into the CyNetworks array
				networks[networkCounter++] = network;
				
				//add the graph and the created CyNetwork based on that graph into the graphMap hashmap
				graphMap.put(graph, network);
								
			}
		}
		catch(ParseException e){
			//avoid compiling error
			LOGGER.log(Level.SEVERE, "CyNetwork/CyEdge/CyNode initialization failed @ for-each loop in run()");
		}
		this.outNetworks = networks;
	}
	
	
	/**
	 * build an instance of CyNetworkView based on the passed in CyNetwork instance
	 * 
	 * @param CyNetwork network from which we want to build the CyNetworkView
	 */
	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		// TODO
		/*
		 * Steps:
		 * Retrieve Graph object corresponding to input(CyNetwork network) from HashMap
		 * Create new VisualStyle with VisualStyleFactory
		 * Create new CyNetworkView with CyNetworkViewFactory
		 * Pass Graph Object, VisualStyle, CyNetworkView into Reader
		 * in order to set Visual Properties
		 * add VisualStyle to VisualMappingManager
		 * return CyNetworkView
		 */
		
		// codes start at below
		
		LOGGER.info("Executing buildCyNetworkView()...");
		
		// initialize the graph object
		Graph graph = null;
		
		// the for loop's purpose below is to get the corresponding graph
		// based on the input network from the hashmap
		for (HashMap.Entry<Graph, CyNetwork> entry: graphMap.entrySet()){
			//loop through each entry in hashmap until the corresponding graph is found
			if(network.equals( entry.getValue() )) {
				graph = entry.getKey();
				break;
			}
		}
		
		// error checking if the graph object is not found
		if (graph == null){
			LOGGER.log(Level.SEVERE, "Graph is null, either it's a empty graph or is not found in HashMap");
			return null;
		}
		
		//created a new VisualStyle based on the visualStyleFactory
		VisualStyle visualStyle = vizStyleFact.createVisualStyle("new visual style");
		
		//created a new CyNetworkView based on the cyNetworkViewFactory
		CyNetworkView networkView = cyNetworkViewFactory.createNetworkView(network);
		
		//add the created visualStyle to VisualMappingManager
		vizMapMgr.addVisualStyle(visualStyle);
		
		/******************************************************************
		 *Somewhere in this method, we need to call the Reader() from 
		 *org.cytoscape.intern.read.reader package (already imported this 
		 *package) by passing in the networkView and visualStyle just 
		 *created above, in order to set all the VPs
		 ******************************************************************
		 */
		
		//Reader.Reader(networkView, visualStyle); //It actually generates a compiler error
		//It's syntaxially wrong.
		
		/*
		 * NetworkReader netReader = new NetworkReader(networkView, visualStyle, graph.getAttributes());
		 * 
		 */
		
		//return the created cyNetworkView at the end
		return networkView;
	}

	/**
	 * Returns array of CyNetworks read
	 * 
	 * @return array of CyNetworks read
	 */
	public CyNetwork[] getNetworks() {
		return networks;
	}
	
	/**
	 * Adds edge into given cytoscape network, sets name and interaction table data.
	 * Also adds edge to edgeMap
	 * 
	 * @param edge Edge that is being added to network
	 * @param network CyNetwork that edge is being added to
	 */
	private void importEdge(Edge edge, CyNetwork network) {

		// get the source and target Nodes from the edge
		Node source = edge.getSource().getNode();
		Node target = edge.getTarget().getNode();
		
		// get the name of both nodes
		String sourceName = source.getId().toString();
		String targetName = target.getId().toString();
		
		// get the CyNode of the source and target node from the hashmap
		CyNode sourceCyNode = nodeMap.get(source);
		CyNode targetCyNode = nodeMap.get(target);
		
		CyEdge cyEdge = null;
		/*
		 * if getType returns 2, it's directed, else it's undirected
		 * set the cyEdge and add the cyEdge into the network
		 */
		if (edge.getType() == 2) {
			cyEdge = network.addEdge(sourceCyNode, targetCyNode, true);
		}
		else {
			cyEdge = network.addEdge(sourceCyNode, targetCyNode, false);
		}
		
		//set the interaction, a attribute of table, to be "interaction"
		network.getDefaultEdgeTable().getRow(cyEdge.getSUID()).set("interaction", "interaction");
		
		//set the edge name
		network.getDefaultEdgeTable().getRow(cyEdge.getSUID()).set("name", sourceName + " (interaction) "+ targetName);
		
		edgeMap.put(edge, cyEdge);
	}
	
	/**
	 * Adds node into given cytoscape network, sets name.
	 * Also adds node to nodeMap
	 * 
	 * @param node Node being added
	 * @param network CyNetwork it is being added to
	 */
	private void importNode(Node node, CyNetwork network) {
		// add cyNode and set name
		CyNode cyNode = network.addNode();
		String nodeName = node.getId().toString();
		network.getDefaultNodeTable().getRow(cyNode.getSUID()).set("name", nodeName);

		// add the node and the corresponding cyNode into a hashmap for later tracking
		nodeMap.put(node, cyNode);
	}
	
	/**
	 * Returns Map of default attributes and their values for nodes
	 * 
	 * @param graph Graph whose defaults are being returend
	 * @return Map<String, String> where key is attribute name and value
	 * is attribute value
	 */
	private Map<String, String> getNodeDefaultMap(Graph graph) {
		
		/*
		 * Map output = new HashMap<String, String>();
		 * for each element attr in ATTRIBUTES
		 * 		val = graph.getGenericNodeAttribute(attr)
		 * 		if(val != null)
		 * 			output.put(attr, val);
		 *
		 * 	return output;
		 */
		return null;
	}

	/**
	 * Returns Map of default attributes and their values for edges
	 * 
	 * @param graph Graph whose defaults are being returend
	 * @return Map<String, String> where key is attribute name and value
	 * is attribute value
	 */
	private Map<String, String> getEdgeDefaultMap(Graph graph) {
		
		/*
		 * Map output = new HashMap<String, String>();
		 * for each element attr in ATTRIBUTES
		 * 		val = graph.getGenericEdgeAttribute(attr)
		 * 		if(val != null)
		 * 			output.put(attr, val);
		 *
		 * 	return output;
		 */
		return null;
	}
	
	/**
	 * Fills ATTRIBUTES variables with all attributes
	 */
	private void populateAttributes() {
		
	}
}







