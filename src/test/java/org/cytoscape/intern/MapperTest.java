package org.cytoscape.intern;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Font;

import org.cytoscape.intern.write.mapper.EdgePropertyMapper;
import org.cytoscape.intern.write.mapper.Mapper;
import org.cytoscape.intern.write.mapper.NetworkPropertyMapper;
import org.cytoscape.intern.write.mapper.NodePropertyMapper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.junit.Test;
public class MapperTest {

	@Test
	public void testNodeGetElementString() {
		NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		CyNode node = network.addNode();
		network.getRow(node).set(CyNetwork.NAME, "\"Test\"Node1\"\"");
		CyNetworkView networkView = new TestNetworkView(network);
		View<CyNode> nodeView = networkView.getNodeView(node);
		String label = "\"Hello World!\"";
		String tooltip = "Hello!";
		Double height = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
		Double width = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
		Double bwidth = nodeView.getVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH);

		String escLabel = label.replace("\"", "\\\"");
		String labelString = String.format("label = \"%s\"", escLabel);
		String tooltipString = String.format("tooltip = \"%s\"", tooltip);
		String colorString = "color = \"#FF0000FF\"";
		String fillColorString = "fillcolor = \"#00DD99FF\"";
		String expectedDotString = null;
		String actualDotString = null;
		String heightString = String.format("height = \"%f\"", height/72);
		String widthString = String.format("width = \"%f\"", width/72);
		String bwidthString = String.format("penwidth = \"%f\"", bwidth);
		String fontString = String.format("fontname = \"%s\"", new Font(Font.DIALOG, Font.PLAIN, 12).getFontName());
		String fontSizeString = "fontsize = \"12\"";
		String fontColor = "fontcolor = \"#000000FF\"";

		
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL, label);
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT, new Color(0xFF, 0x00, 0x00));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, new Integer(0xFF));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_TOOLTIP, tooltip);
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, new Double(0));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, new Double(0));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, new Color(0x00, 0xDD, 0x99));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY, new Integer(0xFF));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_FACE, new Font(Font.DIALOG, Font.PLAIN, 12));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, new Integer(12));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR, new Color(0x00, 0x00, 0x00));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, new Integer(0xFF));
		
		
		
		
		
		expectedDotString = String.format("[%s,%s,%s,%s,%s,%s,%s,shape = \"ellipse\","
				+ "style = \"solid,filled\",pos = \"%f,%f\",%s,%s,%s,fixedsize = \"true\",labelloc = t]",
				labelString, bwidthString, heightString, widthString, tooltipString, colorString, fillColorString,
				new Double(0), new Double(0) * -1.0,fontString, fontSizeString, fontColor); 

		// todo
		Mapper mapper = new NodePropertyMapper(nodeView, "t");
		actualDotString = mapper.getElementString();

		assertEquals("Node Cytoscape property translation failed.", expectedDotString, actualDotString);
		
	}
	
	@Test 
	public void testEdgeGetElementString() {
		NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		CyNode node = network.addNode();
		CyNode node2 = network.addNode();
		CyEdge edge = network.addEdge(node, node2, true);
		network.getRow(node).set(CyNetwork.NAME, "TestNode1");
		network.getRow(node2).set(CyNetwork.NAME, "TestNode2");
		network.getRow(edge).set(CyNetwork.NAME, "TestEdge1");
		CyNetworkView networkView = new TestNetworkView(network);
		View<CyEdge> edgeView = networkView.getEdgeView(edge);
		String label = "Hello World!";
		String tooltip = "Hello!";
		Double width = edgeView.getVisualProperty(BasicVisualLexicon.EDGE_WIDTH);

		String labelString = String.format("label = \"%s\"", label);
		String tooltipString = String.format("tooltip = \"%s\"", tooltip);
		String colorString = "color = \"#FF0000FF\"";
		String expectedDotString = null;
		String actualDotString = null;
		String widthString = String.format("penwidth = \"%f\"", width);
		String fontString = String.format("fontname = \"%s\"", new Font(Font.DIALOG, Font.PLAIN, 12).getFontName());
		String fontSizeString = "fontsize = \"12\"";
		String fontColor = "fontcolor = \"#000000FF\"";

		
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_LABEL, label);
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_TOOLTIP, tooltip);
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(0x33, 0x33, 0x33));
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_TRANSPARENCY, new Integer(0xFF));
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_LABEL_FONT_FACE, new Font(Font.DIALOG, Font.PLAIN, 12));
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, new Integer(12));
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.EQUAL_DASH);
		labelString = String.format("label = \"%s\"", label);
		colorString = "color = \"#333333FF\"";
		expectedDotString = String.format("[%s,%s,%s,arrowhead = \"none\",arrowtail = \"none\",%s,%s,%s,%s,style = \"dashed\",dir = \"both\"]", labelString, widthString,
				tooltipString, colorString, fontString, fontSizeString, fontColor);

		Mapper mapper = new EdgePropertyMapper(edgeView, networkView);
		actualDotString = mapper.getElementString();

		assertEquals("Edge Cytoscape property translation failed", expectedDotString, actualDotString);
		
	}
	
	@Test
	public void testNetworkGetElementString() {
		NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		CyNode node = network.addNode();
		CyNode node2 = network.addNode();
		CyEdge edge = network.addEdge(node, node2, true);
		network.getRow(node).set(CyNetwork.NAME, "TestNode1");
		network.getRow(node2).set(CyNetwork.NAME, "TestNode2");
		network.getRow(edge).set(CyNetwork.NAME, "TestEdge1");
		network.getRow(network).set(CyNetwork.NAME, "TestNetwork");
		CyNetworkView networkView = new TestNetworkView(network);
		View<CyEdge> edgeView = networkView.getEdgeView(edge);
		String label = "Hello World!";
		networkView.setVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, new Color(0xAA, 0x95, 0x00, 0xFF));
		networkView.setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, label);
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.ARROW);
		String labelString = String.format("label = \"%s\"", label);
		String labelLocString = String.format("labelloc = %s", "b");
		String colorString = "bgcolor = \"#AA9500FF\"";
		String splinesString = "splines = \"false\"";
		String outputString = "outputorder = \"edgesfirst\"";
		String esepString = "esep = \"0\"";
		String marginString = "pad = \"2\"";
		String expectedDotString = String.format("digraph TestNetwork {\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n", 
				labelString, labelLocString, colorString, splinesString, outputString, esepString, marginString); 

		Mapper mapper = new NetworkPropertyMapper(networkView, NetworkPropertyMapper.isDirected(networkView), "false", "b");
		String actualDotString = mapper.getElementString();

		assertEquals("Edge Cytoscape property translation failed", expectedDotString, actualDotString);
	}
	
	@Test
	public void testModifyElementID() {
		assertEquals("ModifyElementId is wrong", "TestNode1", Mapper.modifyElementId("TestNode1"));
		assertEquals("ModifyElementId is wrong", ".59", Mapper.modifyElementId(".59"));
		assertEquals("ModifyElementId is wrong", "\"9.-\"", Mapper.modifyElementId("9.-"));
		assertEquals("ModifyElementId is wrong", "8.8", Mapper.modifyElementId("8.8"));
		assertEquals("ModifyElementId is wrong", "\"Hello\"", Mapper.modifyElementId("\"Hello\""));
		assertEquals("ModifyElementId is wrong", "<Hello>", Mapper.modifyElementId("<Hello>"));
		assertEquals("ModifyElementId is wrong", "\"123baba\"", Mapper.modifyElementId("123baba"));
		assertEquals("ModifyElementId is wrong", "\"\\\"Hi\\\"Harry\\\"\\\"\"", Mapper.modifyElementId("\"Hi\"Harry\"\""));
		
	}
}