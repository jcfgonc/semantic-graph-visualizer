package src.jcfgonc.resultsvisualiser;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.view.Viewer;

import graph.StringGraph;
import visual.GraphStreamUtils;

public class GraphPanelHandler {
	/**
	 * the panel (grid) containing the graphs to render
	 */
	private JPanel graphPanel;
	/**
	 * array of graph windows to render
	 */
	private ArrayList<VisualGraph> vgraphs;

	public GraphPanelHandler(JPanel graphPanel, int numberOfGraphs) {
		this.graphPanel = graphPanel;
		this.vgraphs = new ArrayList<VisualGraph>(numberOfGraphs);

		setNumberOfGraphs(numberOfGraphs);
	}

	public void setNumberOfGraphs(int amount) {
		int currentSize = vgraphs.size();
		if (amount < currentSize) {
			System.err.println("can't remove graphs because of performance issues (it's graphstream's fault!)");
		} else {
			for (int i = currentSize; i < amount; i++) {
				// create an empty graph
				VisualGraph vg = new VisualGraph(i);
				vgraphs.add(vg);
			}
		}
	}

	public int getNumberOfGraphs() {
		return vgraphs.size();
	}

	public void setupPanelSize(int panelWidth, int graphsPerColumn) {
		int graphSize = panelWidth / graphsPerColumn - 2;
		// graphs' views must be sized accordingly
		// updates all the graphs' DefaultView (JComponent) size (each is a square) according to the variable graphSize
		for (VisualGraph vgraph : vgraphs) {
			DefaultView view = vgraph.getDefaultView();
			Dimension size = new Dimension(graphSize, graphSize);
			view.setPreferredSize(size);
		}
		GridLayout layout = (GridLayout) graphPanel.getLayout();
		layout.setColumns(graphsPerColumn);
		layout.setRows(0);

		graphPanel.revalidate();
		graphPanel.repaint();
	}

	/**
	 * sets up the font's size (CSS) for all the visible graphs
	 */
	public void updateFontsSize(int graphFontSize) {
		for (VisualGraph vgraph : vgraphs) {
			String style = String.format("edge { text-size: %d; } node { text-size: %d; }", graphFontSize, graphFontSize);
			MultiGraph mgraph = vgraph.getMultiGraph();
			mgraph.setAttribute("ui.stylesheet", style);
		}
	}

	/**
	 * clears the graphs and updates them (adding edges from) the first N graphs of the given list
	 * 
	 * @param multiGraphs
	 */
	public void refreshGraphs(List<GraphData> newGraphs) {
		clearGraphs();

		for (int i = 0; i < vgraphs.size() && i < newGraphs.size(); i++) {
			GraphData gd = newGraphs.get(i);
			StringGraph stringGraph = gd.getStringGraph();
			VisualGraph visualGraph = vgraphs.get(i);
			MultiGraph multiGraph = visualGraph.getMultiGraph();
			GraphStreamUtils.addEdgesToGraph(multiGraph, stringGraph.edgeSet()); // copy edges from the data-graph to the visual-graph
			visualGraph.shakeLayout();
		}
//		graphPanel.revalidate();
//		graphPanel.repaint();
	}

	public void clearGraphs() {
		for (VisualGraph graph : vgraphs) {
			MultiGraph multiGraph = graph.getMultiGraph();
			multiGraph.clear(); // only function graphstream has to clear nodes/edges (which also clears styles)
			GraphStreamUtils.setupStyleSheet(multiGraph); // dumb graphstream clears styles, recreate them
		}
	}

	/**
	 * sets up the node's size (CSS) for all the visible graphs
	 */
	public void updateNodesSize(int graphNodeSize) {
		for (VisualGraph vgraph : vgraphs) {
			MultiGraph mgraph = vgraph.getMultiGraph();
			String style;
			if (graphNodeSize == 0) {
				style = String.format("node { stroke-mode: none; }");
			} else {
				style = String.format("node { stroke-mode: plain; size: %dpx; }", graphNodeSize, graphNodeSize);
			}
			mgraph.setAttribute("ui.stylesheet", style);
		}
	}

	public void restartGraphsLayout() {
		for (VisualGraph vgraph : vgraphs) {
			vgraph.getLayout().shake();
//			Viewer viewer = vgraph.getViewer();
//			viewer.disableAutoLayout();
//			viewer.enableAutoLayout();
		}
	}

	public void stopGraphsLayout() {
		for (VisualGraph vgraph : vgraphs) {
			Viewer viewer = vgraph.getViewer();
			viewer.disableAutoLayout();
		}
	}
}
