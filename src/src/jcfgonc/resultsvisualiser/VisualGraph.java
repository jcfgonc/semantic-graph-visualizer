package src.jcfgonc.resultsvisualiser;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
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
	private Point dragStartingPoint;
	private Point dragEndingPoint;

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

		// disable graphstream's keyboard shortcuts (interfere with the GUI)
		removeListeners();

		addMotionListener();

		// and setup the layout engine
		layout = Layouts.newLayoutAlgorithm();
		viewer.enableAutoLayout(layout);
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

	private void addMotionListener() {
		defaultView.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				// unneeded
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				dragEndingPoint = e.getPoint();
				if (SwingUtilities.isLeftMouseButton(e)) {
					Point p = new Point(//
							dragEndingPoint.x - dragStartingPoint.x, //
							dragEndingPoint.y - dragStartingPoint.y);
					System.out.println(p);
				} else if (SwingUtilities.isRightMouseButton(e)) {
					double d = dragEndingPoint.distance(dragStartingPoint);
					System.out.println(d);
				}
			}
		});
		defaultView.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// unneeded
			}

			@Override
			public void mousePressed(MouseEvent e) {
				dragStartingPoint = e.getPoint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// unneeded
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// unneeded
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// unneeded
			}
		});
	}

	/**
	 * remove damned graphstream's listeners, we don't serve their kind here
	 */
	private void removeListeners() {
		for (MouseListener listener : defaultView.getMouseListeners()) {
			defaultView.removeMouseListener(listener);
		}

		for (MouseMotionListener listener : defaultView.getMouseMotionListeners()) {
			defaultView.removeMouseMotionListener(listener);
		}

		for (KeyListener listener : defaultView.getKeyListeners()) {
			defaultView.removeKeyListener(listener);
		}
	}
}
