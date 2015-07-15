package org.cytoscape.intern.mapper;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains the mappings of edge VisualPropertys to their equivalent
 * dot attribute strings and performs the translations of Cytoscape edges to
 * dot edge declarations
 * @author Braxton Fitts
 * @author Massoud Maher
 * @author Ziran Zhang 
 * 
 */
public class EdgePropertyMapper extends Mapper {
	
	/**
	 * Maps Cytoscape arrowhead types to the equivalent dot attribute
	 */
	private static final HashMap<ArrowShape, String> ARROW_SHAPE_MAP = new HashMap<ArrowShape, String>(); // TODO fill in
	static {
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.ARROW, "vee");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.CIRCLE, "dot");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.DELTA, "normal");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.DIAMOND, "diamond");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.HALF_BOTTOM, "ornormal");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.HALF_TOP, "olnormal");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.NONE, "none");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.T, "tee");
	}
	
	private CyNetworkView networkView;
	
	/**
	 * Constructs EdgePropertyMapper object
	 * 
	 * @param view of edge we are converting
	 */
	public EdgePropertyMapper(View<CyEdge> view, CyNetworkView networkView) {
		super(view);
		//initialize data structure
		simpleVisPropsToDot = new ArrayList<String>();
		this.networkView = networkView;
		populateMaps();		
	}
	
	/**
	 * Translates the Cytoscape Bend property of the View<CyEdge> object to the
	 * equivalent dot attribute string
	 * 
	 * @return String that represents edge bend attribute
	 */
	@SuppressWarnings("unchecked")
	private String mapEdgeBend(){
		// TODO
		StringBuilder coordinatesString = new StringBuilder();
		Bend edgeBend = view.getVisualProperty(BasicVisualLexicon.EDGE_BEND);
		List<Handle> handles = edgeBend.getAllHandles();
		for (Handle handle : handles) {
			Point2D coords = handle.calculateHandleLocation(networkView, (View<CyEdge>)view);
			String singlePointString = mapPosition(coords.getX(), -coords.getY());
			coordinatesString.append(singlePointString + " ");
		}
		if (coordinatesString.length() == 0) {
			return "";
		}
		coordinatesString.deleteCharAt(coordinatesString.length() - 1);
		String dotPos = String.format("pos = \"%s\"", coordinatesString.toString());
		return dotPos;
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		//Put Simple Props Key/Values
		String edgeLabel = view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL);
		simpleVisPropsToDot.add(String.format("label = \"%s\"", edgeLabel));

		Double width = view.getVisualProperty(BasicVisualLexicon.EDGE_WIDTH);
		simpleVisPropsToDot.add(String.format("penwidth = \"%s\"", width));

		String tooltip = view.getVisualProperty(BasicVisualLexicon.EDGE_TOOLTIP);
		simpleVisPropsToDot.add(String.format("tooltip = \"%s\"", tooltip));
		
		ArrowShape targetArrow = view.getVisualProperty(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
		String dotTargetArrow = ARROW_SHAPE_MAP.get(targetArrow);
		simpleVisPropsToDot.add(String.format("arrowhead = \"%s\"", dotTargetArrow));

		ArrowShape sourceArrow = view.getVisualProperty(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE);
		String dotSourceArrow = ARROW_SHAPE_MAP.get(sourceArrow);
		simpleVisPropsToDot.add(String.format("arrowtail = \"%s\"", dotSourceArrow));
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		//TODO
		/**
		 * Pseudocode:
		 * elementString = ""
		 * For each BasicVisualLexiconProperty prop do
		 * 		propVal = edgeView.getVisualProperty(prop)
		 * 		elementString += edgeMapper.mapVisToDot(prop, propVal)
		 * end
		 * 
		 * Get stroke color and edge transparency values from view
		 * elementString += edgeMapper.mapColor(strokeColorVal, edgeTransVal)
		 * 
		 * Get Source Arrowhead color (DOT ATTRIBUTE IS fillcolor, NO TRANSPARENCY)
		 * elementString += edgeMapper.mapColor(sourceArrowColor, 255)
		 * 
		 * Get Target Arrowhead color (DOT ATTRIBUTE IS fillcolor, NO TRANSPARENCY)
		 * elementString += edgeMapper.mapColor(targetArrowColor, 255)
		 * 
		 * Get Source Arrowhead Shape
		 * elementString += edgeMapper.setSourceArrowShapes()
		 * 
		 * Get Edge Label Font Face
		 * elementString += edgeMapper.mapFont(edgeLabelFont)
		 * 
		 * set line style
		 * 
		 * return elementString
		 */
		LOGGER.info("Preparing to get .dot declaration for element.");

		//Build attribute string
		StringBuilder elementString = new StringBuilder("[");

		//Get .dot strings for simple dot attributes. Append to attribute string
		for (String dotAttribute : simpleVisPropsToDot) {
		        elementString.append(dotAttribute);
		        elementString.append(",");
		}
		LOGGER.info("Built up .dot string from simple properties. Resulting string: " + elementString);
		
		LOGGER.info("Preparing to get color properties");
		//Get the color and fillcolor .dot strings. Append to attribute string
		Color strokeColor = (Color) view.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		Integer strokeTransparency = view.getVisualProperty(BasicVisualLexicon.EDGE_TRANSPARENCY);
		String dotColor = String.format("color = \"%s\"", mapColorToDot(strokeColor, strokeTransparency));
		elementString.append(dotColor + ",");
		LOGGER.info("Appended color attributes to .dot string. Result: " + elementString);
		
		LOGGER.info("Preparing to map edge bends");
		String dotPos = mapEdgeBend();
		if (!(dotPos.equals(""))) {
			elementString.append(dotPos + ",");
		}
		LOGGER.info("Appended edge bend attributes to .dot string. Result: " + elementString);
		
		//Get label font information and append in proper format
		Color labelColor = (Color) view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_COLOR);
		Integer labelTransparency = view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY);
		Font labelFont = view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_FONT_FACE);
		Integer labelSize = view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE);
		elementString.append(mapFont(labelFont, labelSize, labelColor, labelTransparency));
		//Finish attribute string
		elementString.append("]");
		LOGGER.info("Created .dot string. Result: " + elementString);
		return elementString.toString();
	}
}
