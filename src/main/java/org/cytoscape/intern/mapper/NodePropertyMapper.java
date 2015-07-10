package org.cytoscape.intern.mapper;

import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.View;

import java.util.HashMap;


/**
 * Handles mapping of CyNode properties to .dot equivalent Strings
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class NodePropertyMapper extends Mapper {
	
	/**
	 * maps Cytoscape properties by their ID strings to their .dot equivalents if relationship is simple
	 */
	private HashMap< VisualProperty, String> simpleVisPropsToDot;
	
	/**
	 * maps Cytoscape VisualProperty TYPES by their String ID to a HashMap that contains the 
	 * cytoscape to *.dot mappings for that type
	 */
	private HashMap<String, HashMap<VisualPropertyValue, String> > discreteMappingTypes; // TODO
	
	/**
	 *  maps Cytoscape node shape types to the equivalent string used in .dot
	 */
	private HashMap<NodeShape, String> nodeShapeMap; // TODO
	
	/**
	 * Creates string for .dot style attribute. Appends border lineStyle and shape style (rounded or not etc.) to 
	 * "style = filled"
	 * 
	 * @param lineStyle lineStyle being converted
	 * @param nodeShape being converted
	 * @return String for style attribute
	 */
	private String mapDotStyle(LineType lineStyle, NodeShape nodeShape) {
		// TODO
		/**
		 * Pseudocode
		 * Call Mapper.mapDotStyle(lineStyle) to retrieve 'style="lineStyle"' string
		 * Ignore final " (get first n-1 characters of string)
		 * Join with "filled" and "rounded" separated by ","
		 * return created String
		 */
		return null;
	}

	/**
	 * Initializes and populates instance variables with mappings
	 */
	public NodePropertyMapper(View<?> view) {
		super(view);
		// initialize hash maps
		simpleVisPropsToDot = new HashMap< VisualProperty, String>();
		discreteMappingTypes = new HashMap<String, HashMap<VisualPropertyValue, String> >();
		nodeShapeMap = new HashMap<NodeShape, String>();
		
		populateMaps();
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		simpleVisPropsToDot.put(BasicVisualLexicon.NODE_LABEL, "label = ");
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	public String getElementString() {
		//TODO

		/**
		 * Pseudocode:
		 * elementString = ""
		 * For each BasicVisualLexiconProperty prop do
		 * 		propVal = view.getVisualProperty(prop)
		 * 		elementString += mapVisToDot(prop, propVal)
		 * end
		 * 
		 * Get node border color and node border transparency values from view
		 * elementString += mapColor(strokeColorVal, edgeTransVal)
		 * 
		 * Get node fill color and node transparency (DOT ATTRIBUTE IS fillcolor)
		 * elementString += mapColor(sourceArrowColor, nodeTransparency)
		 * 
		 * Get node label font face
		 * elementString += mapFont(edgeLabelFont)
		 * 
		 * return elementString
		 */
		return null;
	}
}










