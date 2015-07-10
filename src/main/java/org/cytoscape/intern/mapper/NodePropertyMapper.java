package org.cytoscape.intern.mapper;

import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.View;

import java.awt.Color;
import java.util.Map;
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
	 *  maps Cytoscape node shape types to the equivalent string used in .dot
	 */
	private HashMap<NodeShape, String> nodeShapeMap;
	
	/**
	 * Initializes and populates instance variables with mappings
	 * 
	 * @param view View of Node we are converting to .dot
	 */
	public NodePropertyMapper(View<?> view) {
		super(view);
		// initialize hash maps
		simpleVisPropsToDot = new HashMap< VisualProperty<?>, String>();
		nodeShapeMap = new HashMap<NodeShape, String>();
		
		populateMaps();
	}
	
	/**
	 * Creates string for .dot style attribute. Appends border lineStyle and shape style (rounded or not etc.) to 
	 * "style = filled"
	 * 
	 * @param nodeShape being converted
	 * @return String for style attribute
	 */
	private String mapDotStyle(NodeShape nodeShape) {
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
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		simpleVisPropsToDot.put(BasicVisualLexicon.NODE_LABEL, "label = ");
		//simpleVisPropsToDot.put(BasicVisualLexicon.NODE_BORDER_WIDTH, "penwidth = ");
		//simpleVisPropsToDot.put(BasicVisualLexicon.NODE_HEIGHT, "height = ");
		//simpleVisPropsToDot.put(BasicVisualLexicon.NODE_WIDTH, "width = ");
		//simpleVisPropsToDot.put(BasicVisualLexicon.NODE_TOOLTIP, "tooltip = ");
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		//TODO
		LOGGER.info("Preparing to get .dot declaration for element.");
		StringBuilder elementString = new StringBuilder("[");
		for (Map.Entry<VisualProperty<?>, String> keyAndVal : simpleVisPropsToDot.entrySet()) {
			VisualProperty<?> visualProp = keyAndVal.getKey();
			String dotString = keyAndVal.getValue();
			Object val = view.getVisualProperty(visualProp);
			String valString = "\"" + val.toString() + "\"";
			elementString.append(dotString + valString + ", ");
		}
		//Color borderColor = (Color) view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT);
		//Integer borderTransparency = view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY);
		//elementString.append("color = " + mapColorToDot(borderColor, borderTransparency));
		LOGGER.info("Built up .dot string from simple properties. Resulting string: " + elementString);
		elementString.setLength(elementString.length() - 2);
		LOGGER.info("Removed extraneous character. Resulting string: " + elementString);
		elementString.append("]");
		LOGGER.info("Created .dot string. Result: " + elementString);
		return elementString.toString();
		/**
		 * Pseudocode:
		 * elementString = ""
		 * For each prop in simpleVisPropsToDot do
		 * 		propVal = view.getVisualProperty(prop)
		 * 		elementString += mapVisToDot(prop, propVal)
		 * end
		 * 
		 * Get node border color and node border transparency values from view
		 * elementString += mapColor(nodeBorderColorVal, nodeBorderTransVal)
		 * 
		 * Get node fill color and node transparency (DOT ATTRIBUTE IS fillcolor)
		 * elementString += mapColor(nodeFillColor, nodeTransparency)
		 * 
		 * Get node label font face
		 * elementString += mapFont(nodeLabelFont)
		 * 
		 * return elementString
		 */
	}
}










