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

public class GraphPanelHandler {
	/**
	 * the panel (grid) containing the graphs to render
	 */
	private JPanel graphPanel;
	/**
	 * array of graph windows to render
	 */
	private ArrayList<VisualGraph> graphViewports;

	public GraphPanelHandler(JPanel graphPanel, int numberOfGraphs) {
		this.graphPanel = graphPanel;
		this.graphViewports = new ArrayList<VisualGraph>(numberOfGraphs);

		graphPanel.removeAll();
		setNumberOfGraphs(numberOfGraphs);
	}

	public void setNumberOfGraphs(int amount) {
		int currentSize = graphViewports.size();
		if (amount < currentSize) {
			System.err.println("can't remove graphs because of performance issues (it's graphstream's fault!)");
		} else {
			for (int i = currentSize; i < amount; i++) {
				// create an empty graph
				VisualGraph vg = new VisualGraph(i);
				graphViewports.add(vg);
				// graph is added to panel here
				DefaultView defaultView = vg.getDefaultView();
				graphPanel.add(defaultView);
			}
		}
//		graphPanel.revalidate();
		// graphPanel.repaint();
	}

	public int getNumberOfViewports() {
		return graphViewports.size();
	}

	public void setupPanelSize(int panelWidth, int graphsPerColumn) {
		int graphSize = panelWidth / graphsPerColumn - 2;
		// graphs' views must be sized accordingly
		// updates all the graphs' DefaultView (JComponent) size (each is a square) according to the variable graphSize
		for (VisualGraph vgraph : graphViewports) {
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
		graphViewports.parallelStream().forEach(vgraph -> {
			String style = String.format("edge { text-size: %d; } node { text-size: %d; }", graphFontSize, graphFontSize);
			MultiGraph mgraph = vgraph.getMultiGraph();
			mgraph.setAttribute("ui.stylesheet", style);
		});
	}

	/**
	 * clears the graphs and updates them (adding edges from) the first N graphs of the given list
	 * 
	 * @param newGraphs the list of GraphData (coming from the GraphFilter)
	 */
	public void refreshGraphs(List<GraphData> newGraphs) {
		int numGraphs = newGraphs.size();
		for (int i = 0; i < graphViewports.size(); i++) {
			VisualGraph visualGraph = graphViewports.get(i);
			if (i < numGraphs) {
				GraphData gd = newGraphs.get(i);
				StringGraph stringGraph = gd.getStringGraph();
				visualGraph.setToolTip(gd.getToolTipText());
				visualGraph.refreshGraph(stringGraph);
			} else {
				visualGraph.clear();
				visualGraph.setToolTip(null);
			}
		}
	}

	/**
	 * Clears the graphs (currently unused).
	 */
	public void clearGraphs() {
		graphViewports.parallelStream().forEach(vgraph -> {
			vgraph.clear();
		});
	}

	/**
	 * sets up the node's size (CSS) for all the visible graphs
	 */
	public void updateNodesSize(int graphNodeSize) {
		graphViewports.parallelStream().forEach(vgraph -> {
			MultiGraph mgraph = vgraph.getMultiGraph();
			String style;
			if (graphNodeSize == 0) {
				style = String.format("node { stroke-mode: none; }");
			} else {
				style = String.format("node { stroke-mode: plain; size: %dpx; }", graphNodeSize, graphNodeSize);
			}
			mgraph.setAttribute("ui.stylesheet", style);
		});
	}

	public void restartGraphsLayout() {
		graphViewports.parallelStream().forEach(vgraph -> {
			vgraph.restartLayout();
		});
	}

	public void shakeGraphs() {
		graphViewports.parallelStream().forEach(vgraph -> {
			vgraph.shakeLayout();
		});
	}

	public void stopGraphsLayout() {
		graphViewports.parallelStream().forEach(vgraph -> {
			Viewer viewer = vgraph.getViewer();
			viewer.disableAutoLayout();
		});
	}

	/**
	 * @param factor in percent, 1...max int
	 */
	public void changeGraphsMagnification(int factor) {
		for (VisualGraph vgraph : graphViewports) {
			double mag = (double) 100.0 / factor;
			vgraph.changeMagnification(mag);
		}
	}

	/**
	 * @param angle in degrees, can be negative or positive
	 */
	public void changeGraphsRotation(int angleDegrees) {
		for (VisualGraph vgraph : graphViewports) {
			vgraph.changeRotationAbsolute(angleDegrees);
		}
	}

	public void resetViewGraphs() {
		for (VisualGraph vgraph : graphViewports) {
			vgraph.resetView();
		}
	}
}
