package src.jcfgonc.resultsvisualiser;

import java.awt.Color;
import java.awt.event.MouseAdapter;

import javax.swing.border.LineBorder;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.util.DefaultMouseManager;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.CloseFramePolicy;
import org.graphstream.ui.view.Viewer.ThreadingModel;

import graph.StringGraph;
import visual.GraphStreamUtils;

/**
 * Class containing stuff needed to render a graph using graphstream's API. Abstracts graphstream's specifics from the user who just wants to draw
 * some graphs.
 * 
 * @author jcgonc@gmail.com
 *
 */
public class VisualGraph {

	private MultiGraph multiGraph;
	private Viewer viewer;
	private DefaultView defaultView;
	/**
	 * unique id for this graph
	 */
	private int id;
	/**
	 * graph layout engine
	 */
	private Layout layout;
	/**
	 * mouse events handler for this renderer
	 */
	private MouseAdapter mouseAdapter;

	public VisualGraph(int uniqueID) {
		id = uniqueID;
		String id_str = Integer.toString(uniqueID);
		multiGraph = GraphStreamUtils.initializeGraphStream(id_str);
		// GraphStreamUtils.addEdgesToGraph(multiGraph, stringGraph.edgeSet());

		// its viewer/renderer
		viewer = new SwingViewer(multiGraph, ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.enableAutoLayout();
		viewer.setCloseFramePolicy(CloseFramePolicy.CLOSE_VIEWER);

		defaultView = (DefaultView) viewer.addView("graph" + id_str, new SwingGraphRenderer(), false);
		defaultView.setBorder(new LineBorder(Color.BLACK));
		// and setup the layout engine
		layout = Layouts.newLayoutAlgorithm();
		viewer.enableAutoLayout(layout);

		// disable mouse interaction
		DefaultMouseManager manager = new DefaultMouseManager();
		defaultView.setMouseManager(manager);
		manager.release();
		// addMouseListener();
	}

	/**
	 * called outside to set a mouse event (i.e. click selection event)
	 * 
	 * @param ma
	 */
	public void setMouseListener(MouseAdapter ma) {
		if (mouseAdapter != null) {
			defaultView.removeMouseListener(mouseAdapter);
		}
		this.mouseAdapter = ma;
		defaultView.addMouseListener(mouseAdapter);
	}

	public DefaultView getDefaultView() {
		return defaultView;
	}

	public int getId() {
		return id;
	}

	public MultiGraph getMultiGraph() {
		return multiGraph;
	}

	public Viewer getViewer() {
		return viewer;
	}

	/**
	 * shake a little the vertices of the graph
	 */
	public void shakeLayout() {
		layout.shake();
	}

	/**
	 * starts the layout of the graph from a random configuration
	 */
	public void restartLayout() {
		viewer.disableAutoLayout();
		viewer.enableAutoLayout();
	}

	/**
	 * clears the edges/nodes of the visual graph while mantaining the style sheet
	 */
	public void clear() {
		multiGraph.clear(); // only function graphstream has to clear nodes/edges (which also clears styles)
		GraphStreamUtils.setupStyleSheet(multiGraph); // dumb graphstream clears styles, recreate them
	}

	/**
	 * clears the edges/nodes of the visual graph replacing it the the given one
	 * 
	 * @param stringGraph
	 */
	public void refreshGraph(StringGraph stringGraph) {
		clear();
		GraphStreamUtils.addEdgesToGraph(multiGraph, stringGraph.edgeSet()); // copy edges from the data-graph to the visual-graph
	}
}
