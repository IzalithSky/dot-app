package org.cytoscape.intern.write.mapper;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.ROUND_RECTANGLE;

import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualStyle;

/**
 * Handles mapping of CyNode properties to .dot equivalent Strings
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class NodePropertyMapper extends Mapper {
	
	// location of node label
	private String labelLoc;
	
	private static final int TRANSPARENT = 0x00;
	/**
	 * Initializes and populates instance variables with mappings
	 * 
	 * @param view View of Node we are converting to .dot
	 */
	public NodePropertyMapper(View<CyNode> view, VisualStyle vizStyle, String labelLoc) {
		super(view, vizStyle);
		// initialize data structure
		simpleVisPropsToDot = new ArrayList<String>();
		this.labelLoc = labelLoc;
		
		populateMaps();
	}
	
	/**
	 * Creates string for .dot style attribute. Appends border lineStyle and shape style (rounded or not etc.) to 
	 * "style = filled"
	 * 
	 * @return String for style attribute
	 */
	protected String mapDotStyle() {
		if (!isEqualToDefault(NODE_BORDER_LINE_TYPE) ||
			!isEqualToDefault(NODE_SHAPE)) {
			StringBuilder dotStyle = new StringBuilder(super.mapDotStyle());
			NodeShape shape = view.getVisualProperty(NODE_SHAPE);
		
			if (shape.equals(ROUND_RECTANGLE)) {
				dotStyle.append("rounded,");
			}
		
			dotStyle.append("filled\"");
			return dotStyle.toString();
		}
		return null;
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		LOGGER.info("Populating HashMaps with values");

		// Put Simple Props Key/Values
		
		// determine if using exlabel attribute or not
		String nodeLabel = view.getVisualProperty(NODE_LABEL);
		// Replace quotes with escaped quotes if any
		nodeLabel = nodeLabel.replace("\"", "\\\"");
		// if internal label
		if(!labelLoc.equals("ex")) {
			simpleVisPropsToDot.add(String.format("label = \"%s\"", nodeLabel));
		}
		// if external label
		else {
			simpleVisPropsToDot.add("label = \"\"");
			simpleVisPropsToDot.add(String.format("xlabel = \"%s\"", nodeLabel));
		}
		
		if (!isEqualToDefault(NODE_BORDER_WIDTH)) {
			Double borderWidth = view.getVisualProperty(NODE_BORDER_WIDTH);
			simpleVisPropsToDot.add(String.format("penwidth = \"%f\"", borderWidth));
		}
		
		if (!isEqualToDefault(NODE_HEIGHT)) {
			Double height = view.getVisualProperty(NODE_HEIGHT);
			simpleVisPropsToDot.add(String.format("height = \"%f\"", height/PPI));
		}

		if (!isEqualToDefault(NODE_WIDTH)) {
			Double width = view.getVisualProperty(NODE_WIDTH);
			simpleVisPropsToDot.add(String.format("width = \"%f\"", width/PPI));
		}

		if (!isEqualToDefault(NODE_TOOLTIP)) {
			String tooltip = view.getVisualProperty(NODE_TOOLTIP);
			simpleVisPropsToDot.add(String.format("tooltip = \"%s\"", tooltip));
		}
		
		// Put Node Shape Key/Values
		LOGGER.info("HashMaps populated");
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		LOGGER.info("Preparing to get .dot declaration for a node.");

		// Build attribute string
		StringBuilder elementString = new StringBuilder("[");

		// Get .dot strings for simple dot attributes. Append to attribute string
		for (String dotAttribute : simpleVisPropsToDot) {
		        elementString.append(dotAttribute);
		        elementString.append(",");
		}
		LOGGER.info("Built up .dot string from simple properties. Resulting string: " + elementString);
		
		// Write fillcolor and color attribute
		String colorsString = mapColors();
		if (!colorsString.equals("")) {
			elementString.append(mapColors() + ",");
		}
		LOGGER.info("Appended color attributes to .dot string. Result: " + elementString);

		// Write nodeShape
		String shapeString = mapShape();
		if (shapeString != null) {
			elementString.append(mapShape() + ",");
		}
		LOGGER.info("Appended shape attribute to .dot string. Result: " + elementString);
		

		// Get the .dot string for the node style. Append to attribute string
		String styleString = mapDotStyle();
		if (styleString != null) {
			elementString.append(styleString + ",");
		}
		LOGGER.info("Style info appended. Resulting String: " + elementString);
		
		// Get node location and append in proper format
		Double xLoc = view.getVisualProperty(NODE_X_LOCATION);
		Double yLoc = view.getVisualProperty(NODE_Y_LOCATION);
		String dotPosition = String.format("pos = \"%s\"", mapPosition(xLoc, yLoc));
		elementString.append(dotPosition + ",");
		


		// Append font name+size+color attributes
		LOGGER.info("Appending font data");
		String fontString = mapFontHelper();
		if (!fontString.equals("")) {
			elementString.append(mapFontHelper());
		}

		// Finish Attribute List
		elementString.append("]");
		LOGGER.info("Created .dot string. Result: " + elementString);
		return elementString.toString();
	}
	
	/**
	 * Helper method that returns String that defines color attribute including "fillcolor=" part
	 * 
	 * @return String in form "color = <color>,fillcolor = <color>"
	 */
	private String mapColors() {
		StringBuilder elementString = null;
		boolean visible = view.getVisualProperty(NODE_VISIBLE);
		
		LOGGER.info("Preparing to get color properties");
		// Get the color string (border color). Append to attribute string
		if (!isEqualToDefault(NODE_BORDER_PAINT) || !isEqualToDefault(NODE_BORDER_TRANSPARENCY)) {
			Color borderColor = (Color) view.getVisualProperty(NODE_BORDER_PAINT);
			// Set alpha (opacity) to 0 if node is invisible, translate alpha otherwise
			Integer borderTransparency = (visible) ? ((Number)view.getVisualProperty(NODE_BORDER_TRANSPARENCY)).intValue()
												: TRANSPARENT;
			String dotBorderColor = String.format("color = \"%s\"", mapColorToDot(borderColor, borderTransparency));
			elementString = new StringBuilder(dotBorderColor + ",");
		}
		
		// Write node fill color
		if (!isEqualToDefault(NODE_FILL_COLOR) || !isEqualToDefault(NODE_TRANSPARENCY)) {
			Color fillColor = (Color) view.getVisualProperty(NODE_FILL_COLOR);
			// Set alpha (opacity) to 0 if node is invisible, translate alpha otherwise
			Integer transparency = (visible) ? ((Number)view.getVisualProperty(NODE_TRANSPARENCY)).intValue()
												: TRANSPARENT;
			String dotFillColor = String.format("fillcolor = \"%s\"", mapColorToDot(fillColor, transparency));
			if (elementString != null) {
				elementString.append(dotFillColor);
			} else {
				elementString = new StringBuilder(dotFillColor);
			}
		}
		if (elementString == null) {
			return null;
		}
		return elementString.toString();
		
	}
	
	/**
	 * Helper method that returns String that represents nodeShape 
	 * 
	 * @return String in form in form "shape = <shape>"
	 */
	private String mapShape() {
		LOGGER.info("Preparing to get shape property");
		
		// Get the .dot string for the node shape. Append to attribute string
		if (isEqualToDefault(NODE_SHAPE)) {
			return null;
		}
		NodeShape shape = view.getVisualProperty(NODE_SHAPE);
		String shapeStr = NODE_SHAPE_MAP.get(shape);
		
		// default if there is no match
		if (shapeStr == null) {
			shapeStr = "rectangle"; 
			LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
		}
		
		String dotShape = String.format("shape = \"%s\"", shapeStr);
		LOGGER.info("Appended shape attribute to .dot string. Result: " + dotShape);
		
		return dotShape;
	}
	
	/**
	 * Helper method that returns String that contains font face, size, color and transparency
	 * 
	 * @return String that defines fontname, fontcolor and fontsize attributes
	 */
	private String mapFontHelper() {
		final boolean visible = view.getVisualProperty(NODE_VISIBLE);
		Font fontName = view.getVisualProperty(NODE_LABEL_FONT_FACE);
		LOGGER.info("Retrieving font size...");
		Integer fontSize = ((Number)view.getVisualProperty(NODE_LABEL_FONT_SIZE)).intValue();
		Color fontColor = (Color)(view.getVisualProperty(NODE_LABEL_COLOR));
		Integer fontTransparency = (visible) ? ((Number)view.getVisualProperty(NODE_LABEL_TRANSPARENCY)).intValue()
											 : TRANSPARENT;
		
		return mapFont(fontName, fontSize, fontColor, fontTransparency);
	}
}
