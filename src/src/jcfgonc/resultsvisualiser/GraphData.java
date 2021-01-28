package src.jcfgonc.resultsvisualiser;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.border.LineBorder;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.util.DefaultMouseManager;

import graph.GraphReadWrite;
import graph.StringGraph;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import visual.GraphStreamUtils;

public class GraphData {

	private MultiGraph multiGraph;
	private Viewer viewer;
	private DefaultView defaultView;
	private boolean selected;
	private Int2ObjectMap<String> detailsMap;
	private Object2IntMap<String> detailsHeader;
	private StringGraph stringGraph;
	private boolean loaded;
	private String id;
	private MouseAdapter mouseAdapter;
	private HashMap<String, String> columnKey2Description;
	private Layout layout;
	private Object2IntOpenHashMap<String> integerProperties;
	private Object2DoubleOpenHashMap<String> doubleProperties;
	private HashMap<String, String> stringProperties;

	public GraphData(String id, StringGraph graph) {
		this.id = id;
		this.stringGraph = new StringGraph(graph);
		this.loaded = false;
		this.selected = false;
		this.multiGraph = null; // created lazily
		this.viewer = null; // created lazily
		this.defaultView = null; // created lazily
		this.integerProperties = new Object2IntOpenHashMap<String>();
		this.doubleProperties = new Object2DoubleOpenHashMap<String>();
		this.stringProperties = new HashMap<String, String>();
	}

	public boolean isSelected() {
		return selected;
	}

	@Override
	public String toString() {
		return id;
	}

	public String getId() {
		return id;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		if (!loaded)
			return;
		if (isSelected()) {
			multiGraph.addAttribute("ui.stylesheet", "graph { fill-color: rgba(192,224,255,128); }");
		} else {
			multiGraph.addAttribute("ui.stylesheet", "graph { fill-color: rgba(192,224,255,0); }");
		}
	}

	public MultiGraph getMultiGraph() {
		lazyLoad();
		return multiGraph;
	}

	public Viewer getViewer() {
		lazyLoad();
		return viewer;
	}

	/**
	 * this is the swing component that is to be added to a jcomponent/jpanel
	 * 
	 * @return
	 */
	public DefaultView getDefaultView() {
		lazyLoad();
		return defaultView;
	}

	private void lazyLoad() {
		if (!loaded) {
			this.multiGraph = GraphStreamUtils.initializeGraphStream();
			GraphStreamUtils.addEdgesToVisualGraph(multiGraph, stringGraph.edgeSet());

			// viewer = new Viewer(multiGraph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
			viewer = new Viewer(multiGraph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
			GraphRenderer renderer = Viewer.newGraphRenderer();
//			defaultView = (DefaultView) viewer.addDefaultView(false);
			defaultView = (DefaultView) viewer.addView(Viewer.DEFAULT_VIEW_ID, renderer, false);
			defaultView.setBorder(new LineBorder(Color.BLACK));
			layout = Layouts.newLayoutAlgorithm();
			layout.setQuality(1);
			layout.setForce(0.1);
			viewer.enableAutoLayout(layout);

			// defaultView.setName(id);
			createToolTipText();
//			defaultView.addComponentListener(new ComponentAdapter() {
//				@Override
//				public void componentShown(ComponentEvent e) {
//					super.componentShown(e);
//					defaultView.setEnabled(true);
//					defaultView.setVisible(true);
//				}
//
//				@Override
//				public void componentHidden(ComponentEvent e) {
//					// this is never called
//				}
//			});
			DefaultMouseManager manager = new DefaultMouseManager();
			defaultView.setMouseManager(manager);
			manager.release();

			// addMouseListener();
			loaded = true;
		}

	}

	public void toggleSelected() {
		lazyLoad();
		setSelected(!isSelected());
	}

	@Override
	public int hashCode() {
		if (id == null)
			return 0;
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphData other = (GraphData) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	private void createToolTipText() {
		if (detailsHeader == null)
			return;
		if (detailsMap == null)
			return;
		if (columnKey2Description == null)
			return;

		// n:time n:relationTypes n:relationTypesStd n:cycles n:patternEdges n:patternVertices n:matches s:query s:pattern s:conceptVarMap s:hash
		String text = "<html>";
		for (String column : sortColumnsAscendingDescription(detailsHeader.keySet(), columnKey2Description)) {
			String columnDescription = columnKey2Description.get(column);
			if (columnDescription == null)
				continue;
			int columnId = detailsHeader.getInt(column);
			String value = detailsMap.get(columnId);
			text += String.format("%s:\t%s<br>", columnDescription, value);
		}
		defaultView.setToolTipText(text);
	}

	private ArrayList<String> sortColumnsAscendingDescription(Collection<String> columnIds, HashMap<String, String> col2Description) {
		ArrayList<String> c = new ArrayList<>(columnIds);
		c.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				String d1 = col2Description.get(o1);
				String d2 = col2Description.get(o2);
				if (d1 == null && d2 == null) {
					return 0;
				}
				if (d1 == null)
					return -1;
				if (d2 == null)
					return 1;
				return d1.compareTo(d2);
			}
		});
		return c;
	}

	public String getDetails(String column) {
		int columnId = detailsHeader.getInt(column);
		String value = detailsMap.get(columnId);
		return value;
	}

	public Object2IntMap<String> getDetailsHeader() {
		return detailsHeader;
	}

	private void addMouseListener() {
		if (defaultView == null)
			return;
		defaultView.addMouseListener(mouseAdapter);
	}

	public void saveGraphCSV(String filename) throws IOException {
		GraphReadWrite.writeCSV(filename, stringGraph);
	}

	public void setMouseListener(MouseAdapter ma) {
		if (mouseAdapter != null) {
			defaultView.removeMouseListener(mouseAdapter);
		}
		this.mouseAdapter = ma;
		addMouseListener();
	}

	/**
	 * use this or check this (and remember this code) to add this GraphData's graph to a gui
	 * 
	 * @param guiContainer
	 */
	public void addToGUI(Container guiContainer) {
		guiContainer.add(getDefaultView());
	}

	public void updateGraph(StringGraph newStringGraph) {
		StringGraph g = new StringGraph(newStringGraph);
		boolean changed = GraphStreamUtils.detectChangesVisualGraph(multiGraph, stringGraph, g);
		if (changed) {
			getLayout().shake();
		}

		this.stringGraph = g;
	}

	public Layout getLayout() {
		return layout;
	}

	public StringGraph getStringGraph() {
		return stringGraph;
	}

	public void addIntegerProperty(String variable, int value) {
		this.integerProperties.put(variable, value);
	}

	public void addDoubleProperty(String variable, double value) {
		this.doubleProperties.put(variable, value);
	}

	public void addStringProperty(String variable, String value) {
		this.stringProperties.put(variable, value);
	}

}
