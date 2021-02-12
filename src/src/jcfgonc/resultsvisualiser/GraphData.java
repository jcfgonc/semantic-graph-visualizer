package src.jcfgonc.resultsvisualiser;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javax.swing.border.LineBorder;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.thread.ThreadProxyPipe;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.util.DefaultMouseManager;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.ThreadingModel;

import graph.GraphReadWrite;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.DataType;
import visual.GraphStreamUtils;

/**
 * Holds data/properties regarding a Semantic Graph (class StringGraph) and related GUI functions using GraphStream's API
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class GraphData {

	private MultiGraph multiGraph;
	private Viewer viewer;
	private DefaultView defaultView;
	private boolean selected;
	private StringGraph stringGraph;
	private boolean loaded;
	private String id;
	private MouseAdapter mouseAdapter;
	private Layout layout;
	private Object2IntOpenHashMap<String> integerProperties;
	private Object2DoubleOpenHashMap<String> doubleProperties;
	private HashMap<String, String> stringProperties;
	private DualHashBidiMap<String, Integer> variable2columnNumber;
	private HashMap<String, DataType> variableTypes;
	private static final DecimalFormat df = new DecimalFormat("#.######");

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
			multiGraph.setAttribute("ui.stylesheet", "graph { fill-color: rgba(192,224,255,128); }");
		} else {
			multiGraph.setAttribute("ui.stylesheet", "graph { fill-color: rgba(192,224,255,0); }");
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
			this.multiGraph = GraphStreamUtils.initializeGraphStream(this.id);
			GraphStreamUtils.addEdgesToVisualGraph(multiGraph, stringGraph.edgeSet());

			viewer = new SwingViewer(multiGraph, ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
			viewer.enableAutoLayout();
			defaultView = (DefaultView) viewer.addView("graph" + this.id, new SwingGraphRenderer(), false);

			defaultView.setBorder(new LineBorder(Color.BLACK));
			layout = Layouts.newLayoutAlgorithm();
//			layout.setQuality(1);
//			layout.setForce(0.1);
			viewer.enableAutoLayout(layout);

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

			// disable mouse interaction
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
		// n:time n:relationTypes n:relationTypesStd n:cycles n:patternEdges n:patternVertices n:matches s:query s:pattern s:conceptVarMap s:hash
		String text = "<html>";
		for (int id = 0; id < variable2columnNumber.size(); id++) { // use tsv/user order
			String var = variable2columnNumber.getKey(id);
			String value = "NULL";
			DataType dataType = variableTypes.get(var);
			switch (dataType) {
			case INTEGER:
				value = Integer.toString(integerProperties.getInt(var));
				break;
			case DOUBLE:
				value = df.format(doubleProperties.getDouble(var));
				break;
			case STRING:
				value = stringProperties.get(var);
				break;
			default:
				break;
			}
			text += String.format("%s:\t%s<br>", var, value); // var:\tvalue
		}
		defaultView.setToolTipText(text);
	}

	private void addMouseListener() {
		if (defaultView == null)
			return;
		defaultView.addMouseListener(mouseAdapter);
	}

	public void saveGraphCSV(String filename) throws IOException {
		GraphReadWrite.writeCSV(filename, stringGraph);
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

	public int addIntegerProperty(String variable, int value) {
		return integerProperties.put(variable, value);
	}

	public double addDoubleProperty(String variable, double value) {
		return doubleProperties.put(variable, value);
	}

	public String addStringProperty(String variable, String value) {
		return stringProperties.put(variable, value);
	}

	public int getIntegerProperty(String variable) {
		return integerProperties.getInt(variable);
	}

	public double getDoubleProperty(String variable) {
		return doubleProperties.getDouble(variable);
	}

	public String getStringProperty(String variable) {
		return stringProperties.get(variable);
	}

	public Set<String> getIntegerVariables() {
		return Collections.unmodifiableSet(integerProperties.keySet());
	}

	public Set<String> getDoubleVariables() {
		return Collections.unmodifiableSet(doubleProperties.keySet());
	}

	public Set<String> getStringVariables() {
		return Collections.unmodifiableSet(stringProperties.keySet());
	}

	public void setVariable2ColumnNumber(DualHashBidiMap<String, Integer> variable2columnNumber) {
		this.variable2columnNumber = variable2columnNumber;
	}

	public void setVariableTypes(HashMap<String, DataType> variableTypes) {
		this.variableTypes = variableTypes;
	}

	public DataType getVariableType(String var) {
		return variableTypes.get(var);
	}

	public int getNumberOfVars() {
		return variable2columnNumber.size();
	}

	public int getColumnNumberFromVariable(String var) {
		return variable2columnNumber.get(var).intValue();
	}

	public String getVariableFromColumnNumber(int column) {
		return variable2columnNumber.inverseBidiMap().get(Integer.valueOf(column));
	}

	public boolean isVariableNumeric(String variable) {
		DataType type = variableTypes.get(variable);
		if (type == null) {
			throw new RuntimeException("variable " + variable + "does not exist");
		}
		if (type == DataType.DOUBLE || type == DataType.INTEGER)
			return true;
		return false;
	}

	public double getNumericVariable(String variable) {
		if (isVariableNumeric(variable)) {
			if (integerProperties.containsKey(variable))
				return integerProperties.getInt(variable);
			else if (doubleProperties.containsKey(variable))
				return doubleProperties.getDouble(variable);
			throw new RuntimeException("variable " + variable + "does not exist");
		}
		throw new RuntimeException("variable " + variable + "is not numeric");
	}

	public boolean isVariableString(String variable) {
		DataType type = variableTypes.get(variable);
		if (type == null) {
			throw new RuntimeException("variable " + variable + "does not exist");
		}
		return (type == DataType.STRING);
	}

}
