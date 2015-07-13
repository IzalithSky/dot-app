package org.cytoscape.intern.mapper;

import java.awt.Color;
import java.util.HashMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class NetworkPropertyMapper extends Mapper {
	
	/**
	 * Contructs NetworkPropertyMapper object
	 * 
	 * @param view of network we are converting
	 */
	public NetworkPropertyMapper(CyNetworkView view) {
		super(view);
		
		simpleVisPropsToDot = new HashMap< VisualProperty<?>, String>();
		
		populateMaps();		
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		/**
		 * all properties we need to write
		 * 
		 * directed
		 * bgcolor-- NETWORK_BACKGROUND_POINT
		 * fixedsize -- true
		 * fontpath -- maybe something
		 * scale -- NETWORK_SCALE_FACTOR -- try ignoring first
		 * label -- NETWORK_TITLE -- maybe -- test
		 * 
		 * pseudocode -- NOTE TABS ARE OFF
		 * 
		 * String output = "";
		 * 
		 * output += getDirectedString(view);
		 * output += ", bgcolor = " + 
		 * 		nodeMapper.mapColorToDot( view.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_COLOR), 255 );
		 * output += ", label = " + view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		 * 
		 * return output;
		 */
		//Get network name from model. Remove spaces from name
		CyNetwork network = (CyNetwork)view.getModel();
		String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		networkName = networkName.replace(" ", "");

		//Build the network properties string
		StringBuilder elementString = new StringBuilder();
		
		//Header of the dot file of the form (di)graph [NetworkName] {
		String graphDeclaration = String.format("%s %s {\n", getDirectedString(), networkName);
		elementString.append(graphDeclaration);
		
		//bgcolor attribute of graph
		Color netBgColor = (Color)view.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		String dotBgColor = String.format("bgcolor = \"%s\"\n", mapColorToDot(netBgColor, netBgColor.getAlpha()));
		elementString.append(dotBgColor);
		
		//label attribute of graph
		String label = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		String dotLabel = String.format("label = \"%s\"\n", label);
		elementString.append(dotLabel);
		
		return elementString.toString();
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		
	}

	/**
	 * Returns dot string that represents if graph is directed or not
	 * 
	 * @return String that is either "graph" or "digraph"
	 */
	private String getDirectedString() {
		String output = (isDirected()) ? "digraph":"graph";
		return output;
	}
	
	/**
	 * Determines whether the graph is visibly directed or not
	 * 
	 * @return true if graph is directed, false otherwise
	 */
	private boolean isDirected() {
		/**
		 * pseudocode
		 * 
		 * CyNetwork network = view.getModel();
		 * ArrayList<CyEdge> edgeList = network.getEdgeList();
		 * 
		 * M-- note that indenting is off below
		 * for(CyEdge edge: edgeList) {
		 * 		if(edge.isDirected()) {
		 * 			return true;
		 * 		}
		 * }
		 * return false;
		 */
		return false;
	}
}